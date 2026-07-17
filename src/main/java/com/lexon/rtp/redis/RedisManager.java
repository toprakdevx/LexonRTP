package com.lexon.rtp.redis;
import com.lexon.rtp.LexonRTP;
import com.lexon.rtp.storage.SQLiteStorage;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import java.util.UUID;
import java.util.logging.Level;
public final class RedisManager {
    private final LexonRTP plugin;
    private final SQLiteStorage sqlite;
    private boolean enabled;
    private String keyPrefix = "lexonrtp:";
    private JedisPool pool;
    private volatile boolean connected;
    public RedisManager(LexonRTP plugin) {
        this.plugin = plugin;
        this.sqlite = new SQLiteStorage(plugin);
    }
    public void connect() {
        var config = plugin.getConfig();
        this.enabled = config.getBoolean("redis.enabled", true);
        this.keyPrefix = config.getString("redis.key-prefix", "lexonrtp:");
        sqlite.init();
        if (!enabled) {
            this.connected = false;
            plugin.getLogger().info("Redis disabled - using SQLite (storage.db) for cooldown persistence.");
            return;
        }
        try {
            JedisPoolConfig poolConfig = new JedisPoolConfig();
            poolConfig.setMaxTotal(config.getInt("redis.pool.max-total", 16));
            poolConfig.setMaxIdle(config.getInt("redis.pool.max-idle", 8));
            poolConfig.setMinIdle(config.getInt("redis.pool.min-idle", 1));
            poolConfig.setTestOnBorrow(true);
            String host = config.getString("redis.host", "127.0.0.1");
            int port = config.getInt("redis.port", 6379);
            int timeout = config.getInt("redis.timeout-ms", 2000);
            String password = config.getString("redis.password", "");
            int database = config.getInt("redis.database", 0);
            if (password == null || password.isEmpty()) {
                this.pool = new JedisPool(poolConfig, host, port, timeout);
            } else {
                this.pool = new JedisPool(poolConfig, host, port, timeout, password, database);
            }
            try (Jedis jedis = pool.getResource()) {
                jedis.ping();
            }
            this.connected = true;
            plugin.getLogger().info("Redis connection successful (" + host + ":" + port + ").");
        } catch (Exception ex) {
            this.connected = false;
            plugin.getLogger().log(Level.WARNING,
                    "Redis connection failed, falling back to SQLite: " + ex.getMessage());
        }
    }
    public boolean isConnected() {
        return connected;
    }
    public String statusText() {
        if (!enabled) {
            return "&7sqlite";
        }
        return connected ? "&aredis" : "&csqlite-fallback";
    }
    private String cooldownKey(UUID uuid) {
        return keyPrefix + "cooldown:" + uuid;
    }
    public long getRemaining(UUID uuid) {
        if (connected) {
            try (Jedis jedis = pool.getResource()) {
                Long ttl = jedis.ttl(cooldownKey(uuid));
                return ttl == null || ttl < 0 ? 0L : ttl;
            } catch (Exception ex) {
                plugin.getLogger().warning("Redis read error, falling back to SQLite: " + ex.getMessage());
                this.connected = false;
            }
        }
        return sqlite.getRemaining(uuid);
    }
    public void setCooldown(UUID uuid, long seconds) {
        if (seconds <= 0) {
            return;
        }
        if (connected) {
            try (Jedis jedis = pool.getResource()) {
                jedis.setex(cooldownKey(uuid), seconds, String.valueOf(System.currentTimeMillis()));
                return;
            } catch (Exception ex) {
                plugin.getLogger().warning("Redis write error, falling back to SQLite: " + ex.getMessage());
                this.connected = false;
            }
        }
        sqlite.setCooldown(uuid, seconds);
    }
    public void clearCooldown(UUID uuid) {
        if (connected) {
            try (Jedis jedis = pool.getResource()) {
                jedis.del(cooldownKey(uuid));
            } catch (Exception ignored) {
            }
        }
        sqlite.remove(uuid);
    }
    public void savePendingRtp(UUID uuid, String worldKey, boolean matchmaking) {
        if (!connected) return;
        try (Jedis jedis = pool.getResource()) {
            jedis.setex(keyPrefix + "pending:" + uuid, 30, worldKey + ":" + matchmaking);
        } catch (Exception ignored) {
        }
    }
    public String getPendingRtp(UUID uuid) {
        if (!connected) return null;
        try (Jedis jedis = pool.getResource()) {
            return jedis.get(keyPrefix + "pending:" + uuid);
        } catch (Exception ignored) {
            return null;
        }
    }
    public void removePendingRtp(UUID uuid) {
        if (connected) {
            try (Jedis jedis = pool.getResource()) {
                jedis.del(keyPrefix + "pending:" + uuid);
            } catch (Exception ignored) {
            }
        }
        sqlite.remove(uuid);
    }
    public void close() {
        if (pool != null) {
            try {
                pool.close();
            } catch (Exception ignored) {
            }
        }
        sqlite.close();
        connected = false;
    }
}
