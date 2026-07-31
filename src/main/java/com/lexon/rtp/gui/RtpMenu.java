package com.lexon.rtp.gui;

import com.lexon.rtp.LexonRTP;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;

public final class RtpMenu extends GuiMenu {
    private static final String DEFAULT_TITLE = "&8» &aʀᴀɴᴅᴏᴍ &fᴛᴇʟᴇᴘᴏʀᴛ";

    public RtpMenu(LexonRTP plugin) {
        super(plugin, "gui.title", DEFAULT_TITLE, "gui.size", 27);
        build(plugin.getConfig(), inventory.getSize());
    }

    @Override
    protected void build(FileConfiguration config, int size) {
        if (config.getBoolean("gui.filler.enabled", false)) {
            Material fillerMat = Material.matchMaterial(
                    config.getString("gui.filler.material", "GRAY_STAINED_GLASS_PANE"));
            if (fillerMat == null) {
                fillerMat = Material.GRAY_STAINED_GLASS_PANE;
            }
            ItemStack filler = item(fillerMat, config.getString("gui.filler.name", " "), null);
            for (int i = 0; i < size; i++) {
                inventory.setItem(i, filler);
            }
        }
        ConfigurationSection items = config.getConfigurationSection("gui.items");
        if (items == null) {
            return;
        }
        for (String key : items.getKeys(false)) {
            ConfigurationSection s = items.getConfigurationSection(key);
            if (s == null) {
                continue;
            }
            int slot = registerSlot(s, size, key);
            if (slot < 0) {
                continue;
            }
            Material material = Material.matchMaterial(s.getString("material", "STONE"));
            if (material == null) {
                material = Material.STONE;
            }
            inventory.setItem(slot, item(material, s.getString("name", key), s.getStringList("lore")));
        }
    }
}
