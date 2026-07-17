package com.lexon.rtp.listener;
import com.lexon.rtp.LexonRTP;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
public final class CrossServerListener implements Listener {
    private final LexonRTP plugin;
    public CrossServerListener(LexonRTP plugin) {
        this.plugin = plugin;
    }
    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        if (!plugin.redis().isConnected()) return;
        plugin.rtpService().processCrossServer(event.getPlayer());
    }
}
