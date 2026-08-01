package com.lexon.rtp.storage;

import com.lexon.rtp.LexonRTP;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.UUID;

public final class SQLiteStorage {
    private final LexonRTP plugin;
    private Connection connection;
    private PreparedStatement select;
    private PreparedStatement insert;
    private PreparedStatement delete;

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
            // Relocated by maven-shade-plugin (see pom.xml relocations)
            Class.forName("com.lexon.rtp.libs.sqlite.JDBC");
            this.connection = DriverManager.getConnection("jdbc:sqlite:" + file.getAbsolutePath());
            try (Statement stmt = connection.createStatement()) {
                stmt.execute("PRAGMA journal_mode=WAL");
                stmt.execute("PRAGMA synchronous=NORMAL");
                stmt.execute("PRAGMA busy_timeout=3000");
                stmt.execute("CREATE TABLE IF NOT EXISTS cooldowns (" +
                        "uuid TEXT PRIMARY KEY, expiry BIGINT NOT NULL)");
            }
            this.select = connection.prepareStatement("SELECT expiry FROM cooldowns WHERE uuid = ?");
            this.insert = connection.prepareStatement("INSERT OR REPLACE INTO cooldowns (uuid, expiry) VALUES (?, ?)");
            this.delete = connection.prepareStatement("DELETE FROM cooldowns WHERE uuid = ?");
        } catch (Exception e) {
            plugin.getLogger().severe("Failed to initialize SQLite storage: " + e.getMessage());
        }
    }

    // A single SQLite connection is not thread-safe. Methods can be called from
    // different region threads on Folia, so all access is synchronized.
    public synchronized long getRemaining(UUID uuid) {
        if (select == null) {
            return 0L;
        }
        long now = System.currentTimeMillis();
        try {
            select.setString(1, uuid.toString());
            try (ResultSet rs = select.executeQuery()) {
                if (rs.next()) {
                    long expiry = rs.getLong("expiry");
                    if (expiry <= now) {
                        remove(uuid);
                        return 0L;
                    }
                    return (expiry - now + 999) / 1000;
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().warning("SQLite read error: " + e.getMessage());
        }
        return 0L;
    }

    public synchronized void setCooldown(UUID uuid, long seconds) {
        if (insert == null) {
            return;
        }
        long expiry = System.currentTimeMillis() + seconds * 1000L;
        try {
            insert.setString(1, uuid.toString());
            insert.setLong(2, expiry);
            insert.execute();
        } catch (SQLException e) {
            plugin.getLogger().warning("SQLite write error: " + e.getMessage());
        }
    }

    public synchronized void remove(UUID uuid) {
        if (delete == null) {
            return;
        }
        try {
            delete.setString(1, uuid.toString());
            delete.execute();
        } catch (SQLException e) {
            plugin.getLogger().warning("SQLite delete error: " + e.getMessage());
        }
    }

    public synchronized void close() {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException ignored) {
            }
        }
    }
}
