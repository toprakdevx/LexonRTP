package com.lexon.rtp.listener;

import com.lexon.rtp.LexonRTP;
import com.lexon.rtp.gui.GuiMenu;
import com.lexon.rtp.gui.QueueMenu;
import com.lexon.rtp.gui.RtpMenu;
import com.lexon.rtp.util.Text;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

public final class MenuListener implements Listener {
    private final LexonRTP plugin;

    public MenuListener(LexonRTP plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        InventoryHolder holder = event.getInventory().getHolder();
        if (holder instanceof RtpMenu menu) {
            handleClick(event, menu, false);
            return;
        }
        if (holder instanceof QueueMenu menu) {
            handleClick(event, menu, true);
        }
    }

    private void handleClick(InventoryClickEvent event, GuiMenu menu, boolean matchmaking) {
        event.setCancelled(true);
        Player player = clicker(event, menu.getInventory());
        if (player == null) {
            return;
        }
        String worldKey = menu.worldFor(event.getRawSlot());
        if (worldKey != null) {
            player.closeInventory();
            plugin.rtpService().request(player, worldKey, matchmaking);
        }
    }

    @EventHandler
    public void onDrag(InventoryDragEvent event) {
        if (event.getInventory().getHolder() instanceof GuiMenu) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        plugin.queue().remove(event.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        Location to = event.getTo();
        if (to == null) {
            return;
        }
        Location from = event.getFrom();
        if (from.getBlockX() == to.getBlockX()
                && from.getBlockY() == to.getBlockY()
                && from.getBlockZ() == to.getBlockZ()) {
            return;
        }
        if (plugin.queue().cancelSoloCountdown(event.getPlayer().getUniqueId())) {
            Text.resetTitle(event.getPlayer());
            plugin.messages().send(event.getPlayer(), "moved-cancelled");
        }
    }

    private Player clicker(InventoryClickEvent event, Inventory menuInventory) {
        if (!(event.getWhoClicked() instanceof Player player)) {
            return null;
        }
        if (event.getClickedInventory() == null
                || !event.getClickedInventory().equals(menuInventory)) {
            return null;
        }
        return player;
    }
}
