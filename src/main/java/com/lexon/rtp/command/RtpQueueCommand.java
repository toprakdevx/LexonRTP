package com.lexon.rtp.command;

import com.lexon.rtp.LexonRTP;
import com.lexon.rtp.gui.QueueMenu;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Locale;

public final class RtpQueueCommand extends BaseRtpCommand {
    public RtpQueueCommand(LexonRTP plugin) {
        super(plugin);
    }

    @Override
    protected boolean execute(Player player, String[] args) {
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
        plugin.rtpService().request(player, args[0].toLowerCase(Locale.ROOT), true);
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            String prefix = args[0].toLowerCase(Locale.ROOT);
            List<String> matches = worldKeys(prefix);
            if (!matches.contains("leave") && "leave".startsWith(prefix)) {
                matches.add("leave");
            }
            return matches;
        }
        return List.of();
    }
}
