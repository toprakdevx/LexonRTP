package com.lexon.rtp.location;

import com.lexon.rtp.LexonRTP;
import com.lexon.rtp.config.WorldSettings;
import com.lexon.rtp.util.Scheduler;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadLocalRandom;

public final class LocationFinder {
    private final LexonRTP plugin;
    private final Scheduler scheduler;
    private final Set<Material> unsafeBlocks;

    public LocationFinder(LexonRTP plugin) {
        this.plugin = plugin;
        this.scheduler = plugin.scheduler();
        this.unsafeBlocks = plugin.config().getUnsafeBlocks();
    }

    public CompletableFuture<Location> find(WorldSettings settings) {
        CompletableFuture<Location> result = new CompletableFuture<>();
        World world = Bukkit.getWorld(settings.getWorldName());
        if (world == null) {
            result.complete(null);
            return result;
        }
        attempt(world, settings, 0, result);
        return result;
    }

    public CompletableFuture<List<Location>> findGroup(WorldSettings settings, int count, int spacing) {
        CompletableFuture<List<Location>> out = new CompletableFuture<>();
        find(settings).whenComplete((base, error) -> {
            if (base == null) {
                out.complete(null);
                return;
            }
            List<Location> result = new ArrayList<>(count);
            result.add(base);
            resolveMember(base, count, spacing, 1, result, out);
        });
        return out;
    }

    private void resolveMember(Location base, int count, int spacing, int index,
                               List<Location> result, CompletableFuture<List<Location>> out) {
        if (index >= count) {
            out.complete(result);
            return;
        }
        int x = base.getBlockX() + index * spacing;
        int z = base.getBlockZ();
        safeAtColumn(base.getWorld(), x, z).whenComplete((loc, error) -> {
            result.add(loc != null ? loc : base.clone().add(index * spacing, 0, 0));
            resolveMember(base, count, spacing, index + 1, result, out);
        });
    }

    private CompletableFuture<Location> safeAtColumn(World world, int x, int z) {
        CompletableFuture<Location> future = new CompletableFuture<>();
        world.getChunkAtAsync(x >> 4, z >> 4, true).whenComplete((chunk, error) -> {
            if (error != null || chunk == null) {
                future.complete(null);
                return;
            }
            Location probe = new Location(world, x + 0.5, world.getMaxHeight() - 1, z + 0.5);
            scheduler.region(probe, () -> future.complete(computeSafe(chunk, x, z)));
        });
        return future;
    }

    private void attempt(World world, WorldSettings settings, int tries, CompletableFuture<Location> result) {
        if (tries >= plugin.config().getMaxAttempts()) {
            result.complete(null);
            return;
        }
        ThreadLocalRandom rng = ThreadLocalRandom.current();
        int range = settings.getMaxRadius() - settings.getMinRadius();
        int offsetX = settings.getMinRadius() + rng.nextInt(range);
        int offsetZ = settings.getMinRadius() + rng.nextInt(range);
        int x = settings.getCenterX() + (rng.nextBoolean() ? offsetX : -offsetX);
        int z = settings.getCenterZ() + (rng.nextBoolean() ? offsetZ : -offsetZ);
        world.getChunkAtAsync(x >> 4, z >> 4, true).whenComplete((chunk, error) -> {
            if (error != null || chunk == null) {
                attempt(world, settings, tries + 1, result);
                return;
            }
            Location probe = new Location(world, x + 0.5, world.getMaxHeight() - 1, z + 0.5);
            scheduler.region(probe, () -> {
                Location safe = computeSafe(chunk, x, z);
                if (safe != null) {
                    result.complete(safe);
                } else {
                    attempt(world, settings, tries + 1, result);
                }
            });
        });
    }

    private Location computeSafe(Chunk chunk, int x, int z) {
        World world = chunk.getWorld();
        int localX = x & 15;
        int localZ = z & 15;
        World.Environment env = world.getEnvironment();
        int startY;
        int bottomY;
        if (env == World.Environment.NETHER) {
            startY = Math.min(plugin.config().getNetherScanTop(), world.getMaxHeight() - 2);
            bottomY = Math.max(plugin.config().getNetherScanBottom(), world.getMinHeight() + 1);
        } else {
            startY = world.getHighestBlockYAt(x, z);
            bottomY = world.getMinHeight() + 1;
            if (startY <= bottomY) {
                return null;
            }
        }
        for (int y = startY; y > bottomY; y--) {
            Block ground = chunk.getBlock(localX, y, localZ);
            Material groundType = ground.getType();
            if (!groundType.isSolid()) {
                continue;
            }
            if (unsafeBlocks.contains(groundType)) {
                return null;
            }
            if (isPassable(chunk, localX, y + 1, localZ) && isPassable(chunk, localX, y + 2, localZ)) {
                return new Location(world, x + 0.5, y + 1, z + 0.5);
            }
            if (env != World.Environment.NETHER) {
                return null;
            }
        }
        return null;
    }

    private boolean isPassable(Chunk chunk, int x, int y, int z) {
        Block block = chunk.getBlock(x, y, z);
        Material type = block.getType();
        if (type.isAir()) {
            return true;
        }
        if (unsafeBlocks.contains(type)) {
            return false;
        }
        return !type.isSolid() && !block.isLiquid();
    }
}
