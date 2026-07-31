package com.lexon.rtp.gui;

import com.lexon.rtp.LexonRTP;
import com.lexon.rtp.util.Text;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class GuiMenu implements InventoryHolder {
    protected final LexonRTP plugin;
    protected final Inventory inventory;
    private final Map<Integer, String> slotWorlds = new HashMap<>();

    protected GuiMenu(LexonRTP plugin, String titlePath, String defaultTitle, String sizePath, int defaultSize) {
        this.plugin = plugin;
        FileConfiguration config = plugin.getConfig();
        int size = config.getInt(sizePath, defaultSize);
        if (size % 9 != 0 || size < 9 || size > 54) {
            size = defaultSize;
        }
        this.inventory = Bukkit.createInventory(this, size, Text.color(config.getString(titlePath, defaultTitle)));
    }

    protected abstract void build(FileConfiguration config, int size);

    protected final int registerSlot(ConfigurationSection section, int size, String worldDefault) {
        int slot = section.getInt("slot", -1);
        if (slot < 0 || slot >= size) {
            return -1;
        }
        slotWorlds.put(slot, section.getString("world", worldDefault));
        return slot;
    }

    protected static ItemStack item(Material material, String name, List<String> lore) {
        return item(material, name, lore, null, null);
    }

    protected static ItemStack item(Material material, String name, List<String> lore, String current, String required) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return item;
        }
        if (name != null) {
            meta.setDisplayName(Text.color(fill(name, current, required)));
        }
        if (lore != null && !lore.isEmpty()) {
            List<String> colored = new ArrayList<>(lore.size());
            for (String line : lore) {
                colored.add(Text.color(fill(line, current, required)));
            }
            meta.setLore(colored);
        }
        item.setItemMeta(meta);
        return item;
    }

    private static String fill(String text, String current, String required) {
        if (text == null) {
            return "";
        }
        if (current != null) {
            text = text.replace("%current%", current);
        }
        if (required != null) {
            text = text.replace("%required%", required);
        }
        return text;
    }

    public String worldFor(int slot) {
        return slotWorlds.get(slot);
    }

    public void open(Player player) {
        plugin.scheduler().entity(player, () -> player.openInventory(inventory));
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }
}
