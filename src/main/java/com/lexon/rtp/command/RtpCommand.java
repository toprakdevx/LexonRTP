package com.lexon.rtp.command;

import com.lexon.rtp.LexonRTP;
import com.lexon.rtp.gui.RtpMenu;
import org.bukkit.entity.Player;

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
        plugin.rtpService().request(player, args[0].toLowerCase(), false);
        return true;
    }
}
