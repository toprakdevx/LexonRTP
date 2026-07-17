package com.lexon.rtp.storage;
import com.lexon.rtp.LexonRTP;
import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;
public final class SQLiteStorage {
    private final LexonRTP plugin;
    private Connection connection;
    public SQLiteStorage(LexonRTP plugin) {
        this.plugin = plugin;
    }
    public void init() {
        try {
            File file = new File(plugin.getDataFolder(), "storage.db");
            File parent = file.getParentFile();
            if (parent != null && !parent.exists()) {
                parent.mkdirs();
            }
            Class.forName("org.sqlite.JDBC");
            this.connection = DriverManager.getConnection("jdbc:sqlite:" + file.getAbsolutePath());
            try (PreparedStatement stmt = connection.prepareStatement(
                    "CREATE TABLE IF NOT EXISTS cooldowns (" +
                    "uuid TEXT PRIMARY KEY, " +
                    "expiry BIGINT NOT NULL)")) {
                stmt.execute();
            }
        } catch (Exception e) {
            plugin.getLogger().severe("Failed to initialize SQLite storage: " + e.getMessage());
        }
    }
    public long getRemaining(UUID uuid) {
        if (connection == null) return 0L;
        long now = System.currentTimeMillis();
        try (PreparedStatement stmt = connection.prepareStatement(
                "SELECT expiry FROM cooldowns WHERE uuid = ?")) {
            stmt.setString(1, uuid.toString());
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                long expiry = rs.getLong("expiry");
                if (expiry <= now) {
                    remove(uuid);
                    return 0L;
                }
                return (expiry - now + 999) / 1000;
            }
        } catch (SQLException e) {
            plugin.getLogger().warning("SQLite read error: " + e.getMessage());
        }
        return 0L;
    }
    public void setCooldown(UUID uuid, long seconds) {
        if (connection == null) return;
        long expiry = System.currentTimeMillis() + seconds * 1000L;
        try (PreparedStatement stmt = connection.prepareStatement(
                "INSERT OR REPLACE INTO cooldowns (uuid, expiry) VALUES (?, ?)")) {
            stmt.setString(1, uuid.toString());
            stmt.setLong(2, expiry);
            stmt.execute();
        } catch (SQLException e) {
            plugin.getLogger().warning("SQLite write error: " + e.getMessage());
        }
    }
    public void remove(UUID uuid) {
        if (connection == null) return;
        try (PreparedStatement stmt = connection.prepareStatement(
                "DELETE FROM cooldowns WHERE uuid = ?")) {
            stmt.setString(1, uuid.toString());
            stmt.execute();
        } catch (SQLException e) {
            plugin.getLogger().warning("SQLite delete error: " + e.getMessage());
        }
    }
    public void close() {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException ignored) {
            }
        }
    }
}
