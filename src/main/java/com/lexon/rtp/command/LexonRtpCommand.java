package com.lexon.rtp.command;

import com.lexon.rtp.LexonRTP;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;

import java.util.List;

public final class LexonRtpCommand implements TabExecutor {
    private final LexonRTP plugin;

    public LexonRtpCommand(LexonRTP plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("lexonrtp.admin")) {
            plugin.messages().send(sender, "no-permission");
            return true;
        }
        if (args.length == 0) {
            plugin.messages().sendList(sender, "lexonrtp-help");
            return true;
        }
        if (args[0].equalsIgnoreCase("reload")) {
            plugin.reloadPlugin();
            plugin.messages().send(sender, "reloaded");
            return true;
        }
        plugin.messages().sendList(sender, "lexonrtp-help");
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!sender.hasPermission("lexonrtp.admin")) {
            return List.of();
        }
        if (args.length == 1) {
            String prefix = args[0].toLowerCase();
            if ("reload".startsWith(prefix)) {
                return List.of("reload");
            }
        }
        return List.of();
    }
}
