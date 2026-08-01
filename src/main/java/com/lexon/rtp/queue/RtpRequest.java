package com.lexon.rtp.queue;

import com.lexon.rtp.config.WorldSettings;
import org.bukkit.command.CommandSender;

import java.util.UUID;

public final class RtpRequest {
    private final UUID playerId;
    private final WorldSettings target;
    private final CommandSender requester;

    public RtpRequest(UUID playerId, WorldSettings target) {
        this(playerId, target, null);
    }

    public RtpRequest(UUID playerId, WorldSettings target, CommandSender requester) {
        this.playerId = playerId;
        this.target = target;
        this.requester = requester;
    }

    public UUID getPlayerId() {
        return playerId;
    }

    public WorldSettings getTarget() {
        return target;
    }

    public CommandSender getRequester() {
        return requester;
    }
}
