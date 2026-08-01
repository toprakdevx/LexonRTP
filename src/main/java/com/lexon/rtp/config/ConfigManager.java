package com.lexon.rtp.config;

import com.lexon.rtp.LexonRTP;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;

import java.util.Collections;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public final class ConfigManager {
    private final LexonRTP plugin;

    private String language;
    private long cooldownSeconds;
    private boolean cooldownOnFail;
    private int rtpCountdown;
    private boolean queueEnabled;
    private int playersPerCycle;
    private long cycleIntervalTicks;
    private int requiredPlayers;
    private int matchCountdown;
    private int matchSpacing;
    private boolean announceWaiting;
    private int maxAttempts;
    private int netherScanTop;
    private int netherScanBottom;

    private final Set<Material> unsafeBlocks = EnumSet.noneOf(Material.class);
    private final Map<String, WorldSettings> worlds = new LinkedHashMap<>();

    public ConfigManager(LexonRTP plugin) {
        this.plugin = plugin;
    }

    public void load() {
        plugin.reloadConfig();
        var config = plugin.getConfig();
        this.language = config.getString("settings.language", "en");
        this.cooldownSeconds = config.getLong("settings.cooldown-seconds", 60L);
        this.cooldownOnFail = config.getBoolean("settings.cooldown-on-fail", false);
        this.rtpCountdown = Math.max(0, config.getInt("settings.rtp-countdown", 3));
        this.queueEnabled = config.getBoolean("queue.enabled", true);
        this.playersPerCycle = Math.max(1, config.getInt("queue.players-per-cycle", 2));
        this.cycleIntervalTicks = Math.max(1L, config.getLong("queue.cycle-interval-ticks", 10L));
        this.requiredPlayers = Math.max(1, config.getInt("queue.required-players", 2));
        this.matchCountdown = Math.max(0, config.getInt("queue.match-countdown", 10));
        this.matchSpacing = Math.max(1, config.getInt("queue.match-spacing", 10));
        this.announceWaiting = config.getBoolean("queue.announce-waiting", true);
        this.maxAttempts = Math.max(1, config.getInt("location-finder.max-attempts", 25));
        this.netherScanTop = config.getInt("location-finder.nether-scan-top", 120);
        this.netherScanBottom = config.getInt("location-finder.nether-scan-bottom", 8);
        unsafeBlocks.clear();
        for (String name : config.getStringList("location-finder.unsafe-blocks")) {
            Material material = Material.matchMaterial(name.trim().toUpperCase());
            if (material != null) {
                unsafeBlocks.add(material);
            } else {
                plugin.getLogger().warning("Unknown unsafe-block: " + name);
            }
        }
        worlds.clear();
        ConfigurationSection worldsSection = config.getConfigurationSection("worlds");
        if (worldsSection != null) {
            for (String key : worldsSection.getKeys(false)) {
                ConfigurationSection s = worldsSection.getConfigurationSection(key);
                if (s == null) {
                    continue;
                }
                worlds.put(key.toLowerCase(Locale.ROOT), new WorldSettings(
                        key,
                        s.getString("world-name", key),
                        s.getBoolean("enabled", true),
                        s.getInt("min-radius", 500),
                        s.getInt("max-radius", 10000),
                        s.getInt("center-x", 0),
                        s.getInt("center-z", 0),
                        s.getString("target-server", "")
                ));
            }
        }
    }

    public WorldSettings getWorld(String key) {
        return key == null ? null : worlds.get(key.toLowerCase(Locale.ROOT));
    }

    public Map<String, WorldSettings> getWorlds() {
        return Collections.unmodifiableMap(worlds);
    }

    public String getLanguage() {
        return language;
    }

    public long getCooldownSeconds() {
        return cooldownSeconds;
    }

    public boolean isCooldownOnFail() {
        return cooldownOnFail;
    }

    public int getRtpCountdown() {
        return rtpCountdown;
    }

    public boolean isQueueEnabled() {
        return queueEnabled;
    }

    public int getPlayersPerCycle() {
        return playersPerCycle;
    }

    public long getCycleIntervalTicks() {
        return cycleIntervalTicks;
    }

    public int getRequiredPlayers() {
        return requiredPlayers;
    }

    public int getMatchCountdown() {
        return matchCountdown;
    }

    public int getMatchSpacing() {
        return matchSpacing;
    }

    public boolean isAnnounceWaiting() {
        return announceWaiting;
    }

    public int getMaxAttempts() {
        return maxAttempts;
    }

    public int getNetherScanTop() {
        return netherScanTop;
    }

    public int getNetherScanBottom() {
        return netherScanBottom;
    }

    public Set<Material> getUnsafeBlocks() {
        return Collections.unmodifiableSet(unsafeBlocks);
    }
}
