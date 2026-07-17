package com.lexon.rtp.command;
import com.lexon.rtp.LexonRTP;
import com.lexon.rtp.gui.QueueMenu;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import java.util.ArrayList;
import java.util.List;
public final class RtpQueueCommand implements TabExecutor {
    private final LexonRTP plugin;
    public RtpQueueCommand(LexonRTP plugin) {
        this.plugin = plugin;
    }
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            plugin.messages().send(sender, "players-only");
            return true;
        }
        if (!player.hasPermission("lexonrtp.use")) {
            plugin.messages().send(player, "no-permission");
            return true;
        }
        if (args.length == 0) {
            new QueueMenu(plugin).open(player);
            return true;
        }
        if (args[0].equalsIgnoreCase("leave")) {
            if (plugin.queue().isQueued(player.getUniqueId())) {
                plugin.queue().remove(player.getUniqueId());
                plugin.messages().send(player, "queue-left");
            } else {
                plugin.messages().send(player, "queue-not-in");
            }
            return true;
        }
        plugin.rtpService().request(player, args[0].toLowerCase(), true);
        return true;
    }
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            String prefix = args[0].toLowerCase();
            List<String> out = new ArrayList<>();
            for (String key : plugin.config().getWorlds().keySet()) {
                if (key.startsWith(prefix)) {
                    out.add(key);
                }
            }
            if ("leave".startsWith(prefix)) {
                out.add("leave");
            }
            return out;
        }
        return List.of();
    }
}
