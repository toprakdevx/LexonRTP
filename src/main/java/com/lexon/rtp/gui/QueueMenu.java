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
public final class QueueMenu implements InventoryHolder {
    private final LexonRTP plugin;
    private final Inventory inventory;
    private final Map<Integer, String> slotWorlds = new HashMap<>();
    private final String required;
    public QueueMenu(LexonRTP plugin) {
        this.plugin = plugin;
        this.required = String.valueOf(plugin.config().getRequiredPlayers());
        FileConfiguration config = plugin.getConfig();
        String title = Text.color(config.getString("queue-gui.title", "&8» &aʀᴛᴘǫᴜᴇᴜᴇ &f1ᴠ1"));
        int size = config.getInt("queue-gui.size", 27);
        if (size % 9 != 0 || size < 9 || size > 54) {
            size = 27;
        }
        this.inventory = Bukkit.createInventory(this, size, title);
        build(config, size);
    }
    private void build(FileConfiguration config, int size) {
        ConfigurationSection items = config.getConfigurationSection("queue-gui.items");
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
            String worldKey = s.getString("world", key);
            String current = String.valueOf(plugin.queue().matchSize(worldKey));
            inventory.setItem(slot, item(material, s.getString("name", key), s.getStringList("lore"), current));
            slotWorlds.put(slot, worldKey);
        }
    }
    private ItemStack item(Material material, String name, List<String> lore, String current) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(Text.color(fill(name, current)));
            List<String> colored = new ArrayList<>(lore.size());
            for (String line : lore) {
                colored.add(Text.color(fill(line, current)));
            }
            meta.setLore(colored);
            item.setItemMeta(meta);
        }
        return item;
    }
    private String fill(String text, String current) {
        if (text == null) {
            return "";
        }
        return text.replace("%current%", current).replace("%required%", required);
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
