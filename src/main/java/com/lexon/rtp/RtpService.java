package com.lexon.rtp;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import com.lexon.rtp.config.WorldSettings;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import java.util.List;
public final class RtpService {
    private final LexonRTP plugin;
    public RtpService(LexonRTP plugin) {
        this.plugin = plugin;
    }
    public void request(Player player, String worldKey, boolean matchmaking) {
        WorldSettings target = plugin.config().getWorld(worldKey);
        if (target == null) {
            plugin.messages().send(player, "invalid-world");
            return;
        }
        if (!target.isEnabled()) {
            plugin.messages().send(player, "world-disabled");
            return;
        }
        String targetServer = target.getTargetServer();
        if (targetServer != null && !targetServer.isEmpty()) {
            if (!plugin.redis().isConnected()) {
                plugin.messages().send(player, "invalid-world");
                return;
            }
            plugin.redis().savePendingRtp(player.getUniqueId(), worldKey, matchmaking);
            plugin.messages().send(player, "rtp-queued");
            ByteArrayDataOutput out = ByteStreams.newDataOutput();
            out.writeUTF("Connect");
            out.writeUTF(targetServer);
            player.sendPluginMessage(plugin, "BungeeCord", out.toByteArray());
            return;
        }
        if (Bukkit.getWorld(target.getWorldName()) == null) {
            plugin.messages().send(player, "world-not-found");
            return;
        }
        if (plugin.queue().isQueued(player.getUniqueId())) {
            plugin.messages().send(player, "queue-already",
                    "%queue%", String.valueOf(plugin.queue().positionOf(player.getUniqueId())));
            return;
        }
        if (!player.hasPermission("lexonrtp.cooldown.bypass")) {
            long remaining = plugin.redis().getRemaining(player.getUniqueId());
            if (remaining > 0) {
                plugin.messages().send(player, "cooldown-active", "%time%", String.valueOf(remaining));
                return;
            }
        }
        if (!plugin.queue().enqueue(player, target, matchmaking)) {
            plugin.messages().send(player, "queue-already",
                    "%queue%", String.valueOf(plugin.queue().positionOf(player.getUniqueId())));
            return;
        }
        if (!matchmaking) {
            plugin.messages().send(player, "rtp-queued");
            return;
        }
        int count = plugin.queue().matchSize(target.getKey());
        int required = plugin.config().getRequiredPlayers();
        plugin.messages().send(player, "queue-joined",
                "%count%", String.valueOf(count),
                "%required%", String.valueOf(required),
                "%queue%", String.valueOf(plugin.queue().positionOf(player.getUniqueId())));
        if (count < required && plugin.config().isAnnounceWaiting()) {
            broadcastWaiting(player, count, required);
        }
    }
    public void processCrossServer(Player player) {
        String data = plugin.redis().getPendingRtp(player.getUniqueId());
        if (data == null) return;
        plugin.redis().removePendingRtp(player.getUniqueId());
        String[] parts = data.split(":", 2);
        if (parts.length < 2) return;
        String worldKey = parts[0];
        boolean matchmaking = Boolean.parseBoolean(parts[1]);
        request(player, worldKey, matchmaking);
    }
    private void broadcastWaiting(Player waiting, int count, int required) {
        List<String> lines = plugin.messages().getList("queue-broadcast",
                "%player%", waiting.getName(),
                "%count%", String.valueOf(count),
                "%required%", String.valueOf(required));
        if (lines.isEmpty()) {
            return;
        }
        for (Player online : Bukkit.getOnlinePlayers()) {
            for (String line : lines) {
                online.sendMessage(line);
            }
        }
    }
}
