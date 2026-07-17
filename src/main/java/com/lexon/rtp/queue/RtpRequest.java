package com.lexon.rtp.queue;
import com.lexon.rtp.config.WorldSettings;
import java.util.UUID;
public final class RtpRequest {
    private final UUID playerId;
    private final WorldSettings target;
    private final boolean matchmaking;
    private final long queuedAt;
    public RtpRequest(UUID playerId, WorldSettings target, boolean matchmaking) {
        this.playerId = playerId;
        this.target = target;
        this.matchmaking = matchmaking;
        this.queuedAt = System.currentTimeMillis();
    }
    public UUID getPlayerId() {
        return playerId;
    }
    public WorldSettings getTarget() {
        return target;
    }
    public boolean isMatchmaking() {
        return matchmaking;
    }
    public long getQueuedAt() {
        return queuedAt;
    }
}
