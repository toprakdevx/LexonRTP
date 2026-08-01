package com.lexon.rtp.queue;

import com.lexon.rtp.LexonRTP;
import com.lexon.rtp.config.WorldSettings;
import com.lexon.rtp.util.Scheduler;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

public final class QueueManager {
    private final LexonRTP plugin;
    private final Scheduler scheduler;
    private final ConcurrentLinkedQueue<RtpRequest> soloQueue = new ConcurrentLinkedQueue<>();
    private final Map<String, QueueState> matchQueues = new ConcurrentHashMap<>();
    private final Map<UUID, String> playerQueue = new ConcurrentHashMap<>();
    private final Set<UUID> inQueue = ConcurrentHashMap.newKeySet();
    private final Set<UUID> soloCountdowns = ConcurrentHashMap.newKeySet();
    private ScheduledTask task;

    public QueueManager(LexonRTP plugin) {
        this.plugin = plugin;
        this.scheduler = plugin.scheduler();
    }

    public void start() {
        stop();
        long interval = plugin.config().getCycleIntervalTicks();
        this.task = scheduler.globalTimer(this::processCycle, interval, interval);
    }

    public void stop() {
        if (task != null) {
            try {
                task.cancel();
            } catch (Throwable ignored) {
            }
            task = null;
        }
    }

    public boolean isQueued(UUID uuid) {
        return inQueue.contains(uuid);
    }

    public int matchSize(String worldKey) {
        QueueState state = matchQueues.get(worldKey.toLowerCase(Locale.ROOT));
        return state == null ? 0 : state.requests.size();
    }

    public int positionOf(UUID uuid) {
        String queueKey = playerQueue.get(uuid);
        if (queueKey == null) {
            return 0;
        }
        QueueState state = matchQueues.get(queueKey);
        if (state == null) {
            return 0;
        }
        int index = 1;
        for (RtpRequest request : state.requests) {
            if (request.getPlayerId().equals(uuid)) {
                return index;
            }
            index++;
        }
        return 0;
    }

    public boolean enqueue(Player player, WorldSettings target, boolean matchmaking, CommandSender requester) {
        if (!inQueue.add(player.getUniqueId())) {
            return false;
        }
        RtpRequest request = new RtpRequest(player.getUniqueId(), target, requester);
        if (matchmaking) {
            String key = target.getKey().toLowerCase(Locale.ROOT);
            QueueState state = matchQueues.computeIfAbsent(key, k -> new QueueState());
            state.requests.add(request);
            playerQueue.put(player.getUniqueId(), key);
        } else {
            soloQueue.add(request);
            playerQueue.put(player.getUniqueId(), "");
        }
        return true;
    }

    public void remove(UUID uuid) {
        if (!inQueue.remove(uuid)) {
            soloCountdowns.remove(uuid);
            return;
        }
        String queueKey = playerQueue.remove(uuid);
        if (queueKey == null) {
            soloCountdowns.remove(uuid);
            return;
        }
        if (queueKey.isEmpty()) {
            soloQueue.removeIf(r -> r.getPlayerId().equals(uuid));
        } else {
            QueueState state = matchQueues.get(queueKey);
            if (state != null) {
                state.requests.removeIf(r -> r.getPlayerId().equals(uuid));
            }
        }
        soloCountdowns.remove(uuid);
    }

    public boolean cancelSoloCountdown(UUID uuid) {
        boolean cancelled = soloCountdowns.remove(uuid);
        if (cancelled) {
            finish(uuid);
        }
        return cancelled;
    }

    public boolean cancelSolo(UUID uuid) {
        if (!inQueue.contains(uuid)) {
            return false;
        }
        String queueKey = playerQueue.get(uuid);
        if (queueKey == null || !queueKey.isEmpty()) {
            return false;
        }
        remove(uuid);
        return true;
    }

    private void finish(UUID uuid) {
        inQueue.remove(uuid);
        playerQueue.remove(uuid);
        soloCountdowns.remove(uuid);
    }

    private void processCycle() {
        int budget = plugin.config().getPlayersPerCycle();
        processSolo(budget);
        processMatch(budget);
    }

    private void processSolo(int budget) {
        for (int i = 0; i < budget; i++) {
            RtpRequest request = soloQueue.poll();
            if (request == null) {
                break;
            }
            processSoloRequest(request);
        }
    }

    private void processMatch(int budget) {
        int required = plugin.config().getRequiredPlayers();
        int spacing = plugin.config().getMatchSpacing();
        int limit = Math.max(required, budget);
        int processed = 0;
        for (QueueState state : matchQueues.values()) {
            while (state.requests.size() >= required && processed + required <= limit) {
                List<RtpRequest> group = new ArrayList<>(required);
                for (int i = 0; i < required; i++) {
                    RtpRequest request = state.requests.poll();
                    if (request == null) {
                        break;
                    }
                    group.add(request);
                }
                if (group.size() == required) {
                    processGroup(group, spacing);
                    processed += required;
                } else {
                    for (RtpRequest request : group) {
                        finish(request.getPlayerId());
                    }
                }
            }
        }
    }

    private void processSoloRequest(RtpRequest request) {
        Player player = Bukkit.getPlayer(request.getPlayerId());
        if (player == null || !player.isOnline()) {
            finish(request.getPlayerId());
            return;
        }
        WorldSettings target = request.getTarget();
        int seconds = plugin.config().getRtpCountdown();
        scheduler.entity(player, () -> plugin.messages().send(player, "searching"));
        plugin.locationFinder().find(target).whenComplete((location, error) -> {
            if (location == null) {
                handleFailure(request.getPlayerId(), target, request.getRequester());
                return;
            }
            startCountdown(request.getPlayerId(), location, target, seconds, false, request.getRequester());
        });
    }

    private void processGroup(List<RtpRequest> group, int spacing) {
        WorldSettings target = group.get(0).getTarget();
        int seconds = plugin.config().getMatchCountdown();
        for (RtpRequest request : group) {
            Player player = Bukkit.getPlayer(request.getPlayerId());
            if (player != null) {
                scheduler.entity(player, () -> plugin.messages().send(player, "searching"));
            }
        }
        plugin.locationFinder().findGroup(target, group.size(), spacing).whenComplete((locations, error) -> {
            if (locations == null || locations.size() < group.size()) {
                for (RtpRequest request : group) {
                    handleFailure(request.getPlayerId(), target, request.getRequester());
                }
                return;
            }
            for (int i = 0; i < group.size(); i++) {
                RtpRequest request = group.get(i);
                startCountdown(request.getPlayerId(), locations.get(i), target, seconds, true,
                        request.getRequester());
            }
        });
    }

    private void startCountdown(UUID uuid, Location location, WorldSettings target, int seconds,
                                boolean matchmaking, CommandSender requester) {
        Player player = Bukkit.getPlayer(uuid);
        if (player == null || !player.isOnline() || !inQueue.contains(uuid)) {
            finish(uuid);
            return;
        }
        boolean cancellable = !matchmaking && seconds > 0;
        if (cancellable) {
            soloCountdowns.add(uuid);
        }
        scheduler.entity(player, () -> tickCountdown(uuid, location, target, seconds, cancellable, requester));
    }

    private void tickCountdown(UUID uuid, Location location, WorldSettings target, int remaining,
                               boolean cancellable, CommandSender requester) {
        Player player = Bukkit.getPlayer(uuid);
        if (player == null || !player.isOnline()) {
            finish(uuid);
            return;
        }
        if (cancellable && !soloCountdowns.contains(uuid)) {
            return;
        }
        if (remaining <= 0) {
            soloCountdowns.remove(uuid);
            teleport(uuid, location, target, requester);
            return;
        }
        String title = plugin.messages().get("countdown-title", "%seconds%", String.valueOf(remaining));
        String subtitle = plugin.messages().get("countdown-subtitle", "%seconds%", String.valueOf(remaining));
        player.sendTitle(title, subtitle, 0, 25, 5);
        scheduler.entityLater(player,
                () -> tickCountdown(uuid, location, target, remaining - 1, cancellable, requester), 20L);
    }

    private void teleport(UUID uuid, Location location, WorldSettings target, CommandSender requester) {
        Player player = Bukkit.getPlayer(uuid);
        if (player == null || !player.isOnline()) {
            finish(uuid);
            return;
        }
        player.teleportAsync(location).whenComplete((success, error) -> scheduler.entity(player, () -> {
            if (Boolean.TRUE.equals(success)) {
                player.resetTitle();
                plugin.messages().send(player, "teleport-success");
                if (requester != null && requester != player) {
                    plugin.messages().send(requester, "admin-rtp-success",
                            "%player%", player.getName(),
                            "%world%", target.getWorldName());
                }
                plugin.redis().setCooldown(uuid, plugin.config().getCooldownSeconds());
                finish(uuid);
            } else {
                handleFailure(uuid, target, requester);
            }
        }));
    }

    private void handleFailure(UUID uuid, WorldSettings target, CommandSender requester) {
        Player player = Bukkit.getPlayer(uuid);
        if (player == null) {
            finish(uuid);
            return;
        }
        scheduler.entity(player, () -> {
            player.resetTitle();
            plugin.messages().send(player, "teleport-failed");
            if (requester != null && requester != player) {
                plugin.messages().send(requester, "admin-rtp-fail", "%player%", player.getName());
            }
            if (plugin.config().isCooldownOnFail()) {
                plugin.redis().setCooldown(uuid, plugin.config().getCooldownSeconds());
            }
            finish(uuid);
        });
    }

    private static final class QueueState {
        final ConcurrentLinkedQueue<RtpRequest> requests = new ConcurrentLinkedQueue<>();
    }
}
