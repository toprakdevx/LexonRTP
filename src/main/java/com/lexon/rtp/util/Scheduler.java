package com.lexon.rtp.util;

import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.java.JavaPlugin;

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

    public void region(Location location, Runnable task) {
        Bukkit.getRegionScheduler().execute(plugin, location, task);
    }

    public void entity(Entity entity, Runnable task) {
        try {
            entity.getScheduler().run(plugin, t -> task.run(), null);
        } catch (IllegalStateException ex) {
            Bukkit.getGlobalRegionScheduler().run(plugin, t -> task.run());
        }
    }

    public void entityLater(Entity entity, Runnable task, long delayTicks) {
        long delay = Math.max(1L, delayTicks);
        try {
            entity.getScheduler().runDelayed(plugin, t -> task.run(), null, delay);
        } catch (IllegalStateException ex) {
            Bukkit.getGlobalRegionScheduler().runDelayed(plugin, t -> task.run(), delay);
        }
    }

    public void shutdown() {
        try {
            Bukkit.getGlobalRegionScheduler().cancelTasks(plugin);
            Bukkit.getAsyncScheduler().cancelTasks(plugin);
        } catch (Throwable ignored) {
        }
    }
}
