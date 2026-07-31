package com.lexon.rtp.gui;

import com.lexon.rtp.LexonRTP;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

public final class QueueMenu extends GuiMenu {
    private static final String DEFAULT_TITLE = "&8» &aʀᴛᴘǫᴜᴇᴜᴇ &f1ᴠ1";

    private final String required;

    public QueueMenu(LexonRTP plugin) {
        super(plugin, "queue-gui.title", DEFAULT_TITLE, "queue-gui.size", 27);
        this.required = String.valueOf(plugin.config().getRequiredPlayers());
        build(plugin.getConfig(), inventory.getSize());
    }

    @Override
    protected void build(FileConfiguration config, int size) {
        ConfigurationSection items = config.getConfigurationSection("queue-gui.items");
        if (items == null) {
            return;
        }
        for (String key : items.getKeys(false)) {
            ConfigurationSection s = items.getConfigurationSection(key);
            if (s == null) {
                continue;
            }
            Material material = Material.matchMaterial(s.getString("material", "STONE"));
            if (material == null) {
                material = Material.STONE;
            }
            String worldKey = s.getString("world", key);
            int slot = registerSlot(s, size, worldKey);
            if (slot < 0) {
                continue;
            }
            String current = String.valueOf(plugin.queue().matchSize(worldKey));
            inventory.setItem(slot, item(material, s.getString("name", key), s.getStringList("lore"), current, required));
        }
    }
}
