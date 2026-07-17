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
public final class RtpMenu implements InventoryHolder {
    private final LexonRTP plugin;
    private final Inventory inventory;
    private final Map<Integer, String> slotWorlds = new HashMap<>();
    public RtpMenu(LexonRTP plugin) {
        this.plugin = plugin;
        FileConfiguration config = plugin.getConfig();
        String title = Text.color(config.getString("gui.title", "&8» &aʀᴀɴᴅᴏᴍ &fᴛᴇʟᴇᴘᴏʀᴛ"));
        int size = config.getInt("gui.size", 27);
        if (size % 9 != 0 || size < 9 || size > 54) {
            size = 27;
        }
        this.inventory = Bukkit.createInventory(this, size, title);
        build(config, size);
    }
    private void build(FileConfiguration config, int size) {
        if (config.getBoolean("gui.filler.enabled", false)) {
            Material fillerMat = Material.matchMaterial(config.getString("gui.filler.material", "GRAY_STAINED_GLASS_PANE"));
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
            int slot = s.getInt("slot", -1);
            if (slot < 0 || slot >= size) {
                continue;
            }
            Material material = Material.matchMaterial(s.getString("material", "STONE"));
            if (material == null) {
                material = Material.STONE;
            }
            inventory.setItem(slot, item(material, s.getString("name", key), s.getStringList("lore")));
            slotWorlds.put(slot, s.getString("world", key));
        }
    }
    private ItemStack item(Material material, String name, List<String> lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            if (name != null) {
                meta.setDisplayName(Text.color(name));
            }
            if (lore != null && !lore.isEmpty()) {
                List<String> colored = new ArrayList<>(lore.size());
                for (String line : lore) {
                    colored.add(Text.color(line));
                }
                meta.setLore(colored);
            }
            item.setItemMeta(meta);
        }
        return item;
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
