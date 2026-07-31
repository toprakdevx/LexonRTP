package com.lexon.rtp.queue;

import com.lexon.rtp.config.WorldSettings;

import java.util.UUID;

public final class RtpRequest {
    private final UUID playerId;
    private final WorldSettings target;

    public RtpRequest(UUID playerId, WorldSettings target) {
        this.playerId = playerId;
        this.target = target;
    }

    public UUID getPlayerId() {
        return playerId;
    }

    public WorldSettings getTarget() {
        return target;
    }
}
