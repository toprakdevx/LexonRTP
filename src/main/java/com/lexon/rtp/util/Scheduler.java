package com.lexon.rtp.util;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.java.JavaPlugin;
import java.util.concurrent.TimeUnit;
public final class Scheduler {
    private final JavaPlugin plugin;
    private final boolean folia;
    public Scheduler(JavaPlugin plugin) {
        this.plugin = plugin;
        this.folia = detectFolia();
    }
    private static boolean detectFolia() {
        try {
            Class.forName("io.papermc.paper.threadedregions.RegionizedServer");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }
    public boolean isFolia() {
        return folia;
    }
    public String name() {
        return folia ? "Folia" : "Paper/Spigot";
    }
    public ScheduledTask globalTimer(Runnable task, long delayTicks, long periodTicks) {
        long delay = Math.max(1L, delayTicks);
        long period = Math.max(1L, periodTicks);
        return Bukkit.getGlobalRegionScheduler().runAtFixedRate(plugin, t -> task.run(), delay, period);
    }
    public void async(Runnable task) {
        Bukkit.getAsyncScheduler().runNow(plugin, t -> task.run());
    }
    public void asyncLater(Runnable task, long delayMillis) {
        Bukkit.getAsyncScheduler().runDelayed(plugin, t -> task.run(), delayMillis, TimeUnit.MILLISECONDS);
    }
    public void region(Location location, Runnable task) {
        Bukkit.getRegionScheduler().execute(plugin, location, task);
    }
    public void entity(Entity entity, Runnable task) {
        entity.getScheduler().run(plugin, t -> task.run(), null);
    }
    public void entityLater(Entity entity, Runnable task, long delayTicks) {
        entity.getScheduler().runDelayed(plugin, t -> task.run(), null, Math.max(1L, delayTicks));
    }
    public void shutdown() {
        try {
            Bukkit.getGlobalRegionScheduler().cancelTasks(plugin);
            Bukkit.getAsyncScheduler().cancelTasks(plugin);
        } catch (Throwable ignored) {
        }
    }
}
