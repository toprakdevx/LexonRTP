package com.lexon.rtp.config;
public final class WorldSettings {
    private final String key;
    private final String worldName;
    private final boolean enabled;
    private final int minRadius;
    private final int maxRadius;
    private final int centerX;
    private final int centerZ;
    private final String targetServer;
    public WorldSettings(String key, String worldName, boolean enabled,
                         int minRadius, int maxRadius, int centerX, int centerZ,
                         String targetServer) {
        this.key = key;
        this.worldName = worldName;
        this.enabled = enabled;
        this.minRadius = Math.max(0, minRadius);
        this.maxRadius = Math.max(this.minRadius + 1, maxRadius);
        this.centerX = centerX;
        this.centerZ = centerZ;
        this.targetServer = targetServer;
    }
    public String getKey() {
        return key;
    }
    public String getWorldName() {
        return worldName;
    }
    public boolean isEnabled() {
        return enabled;
    }
    public int getMinRadius() {
        return minRadius;
    }
    public int getMaxRadius() {
        return maxRadius;
    }
    public int getCenterX() {
        return centerX;
    }
    public int getCenterZ() {
        return centerZ;
    }
    public String getTargetServer() {
        return targetServer;
    }
}
