package com.lexon.rtp.command;

import com.lexon.rtp.LexonRTP;
import com.lexon.rtp.gui.RtpMenu;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public final class RtpCommand extends BaseRtpCommand {
    public RtpCommand(LexonRTP plugin) {
        super(plugin);
    }

    @Override
    protected boolean execute(Player player, String[] args) {
        if (args.length == 0) {
            new RtpMenu(plugin).open(player);
            return true;
        }
        if (args.length == 1 && args[0].equalsIgnoreCase("cancel")) {
            if (plugin.queue().cancelSoloCountdown(player.getUniqueId())) {
                plugin.messages().send(player, "rtp-cancelled");
            } else {
                plugin.messages().send(player, "rtp-cancel-none");
            }
            return true;
        }
        if (args.length == 2) {
            if (!player.hasPermission("lexonrtp.admin")) {
                plugin.messages().send(player, "no-permission");
                return true;
            }
            Player target = Bukkit.getPlayer(args[0]);
            if (target == null) {
                plugin.messages().send(player, "player-not-found");
                return true;
            }
            plugin.rtpService().request(target, args[1].toLowerCase(), false);
            return true;
        }
        plugin.rtpService().request(player, args[0].toLowerCase(), false);
        return true;
    }

    @Override
    protected boolean executeConsole(CommandSender sender, String[] args) {
        if (args.length >= 2 && sender.hasPermission("lexonrtp.admin")) {
            Player target = Bukkit.getPlayer(args[0]);
            if (target == null) {
                plugin.messages().send(sender, "player-not-found");
                return true;
            }
            plugin.rtpService().request(target, args[1].toLowerCase(), false);
            return true;
        }
        plugin.messages().send(sender, "players-only");
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            String prefix = args[0].toLowerCase();
            List<String> matches = worldKeys(prefix);
            if ("cancel".startsWith(prefix)) {
                matches.add("cancel");
            }
            return matches;
        }
        if (args.length == 2 && sender.hasPermission("lexonrtp.admin")) {
            return worldKeys(args[1].toLowerCase());
        }
        return List.of();
    }
}
