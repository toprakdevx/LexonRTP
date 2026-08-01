package com.lexon.rtp.command;

import com.lexon.rtp.LexonRTP;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public abstract class BaseRtpCommand implements TabExecutor {
    protected final LexonRTP plugin;

    protected BaseRtpCommand(LexonRTP plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            return executeConsole(sender, args);
        }
        if (!player.hasPermission("lexonrtp.use")) {
            plugin.messages().send(player, "no-permission");
            return true;
        }
        return execute(player, args);
    }

    protected abstract boolean execute(Player player, String[] args);

    protected boolean executeConsole(CommandSender sender, String[] args) {
        plugin.messages().send(sender, "players-only");
        return true;
    }

    protected List<String> worldKeys(String prefix) {
        List<String> matches = new ArrayList<>();
        for (String key : plugin.config().getWorlds().keySet()) {
            if (key.startsWith(prefix)) {
                matches.add(key);
            }
        }
        return matches;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        return args.length == 1 ? worldKeys(args[0].toLowerCase(Locale.ROOT)) : List.of();
    }
}
