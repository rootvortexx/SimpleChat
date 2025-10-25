package me.rootvortex.simpleChat.managers;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;

import java.sql.*;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class DatabaseManager {
    private final Plugin plugin;
    private Connection connection;
    private String databaseType;

    public DatabaseManager(Plugin plugin) {
        this.plugin = plugin;
        FileConfiguration config = ((me.rootvortex.simpleChat.SimpleChat) plugin).getDatabaseConfig();
        this.databaseType = config.getString("database.type", "sqlite");
        initializeDatabase();
    }

    private void initializeDatabase() {
        try {
            if (databaseType.equalsIgnoreCase("mysql")) {
                setupMySQL();
            } else {
                setupSQLite();
            }
            createTables();
            plugin.getLogger().info("Database initialized successfully with " + databaseType);
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to initialize database: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void setupSQLite() throws SQLException {
        // Ensure plugin data folder exists
        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdirs();
        }

        String url = "jdbc:sqlite:" + plugin.getDataFolder().getAbsolutePath() + "/data.db";
        connection = DriverManager.getConnection(url);

        // Enable foreign keys for SQLite
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("PRAGMA foreign_keys = ON");
        }
    }

    private void setupMySQL() throws SQLException {
        FileConfiguration config = ((me.rootvortex.simpleChat.SimpleChat) plugin).getDatabaseConfig();
        String host = config.getString("database.mysql.host", "localhost");
        String port = config.getString("database.mysql.port", "3306");
        String database = config.getString("database.mysql.database", "minecraft");
        String username = config.getString("database.mysql.username", "root");
        String password = config.getString("database.mysql.password", "");
        boolean useSSL = config.getBoolean("database.mysql.use-ssl", false);

        String url = "jdbc:mysql://" + host + ":" + port + "/" + database +
                "?useSSL=" + useSSL +
                "&allowPublicKeyRetrieval=true" +
                "&autoReconnect=true" +
                "&characterEncoding=utf8";

        plugin.getLogger().info("Connecting to MySQL at: " + host + ":" + port + "/" + database);
        connection = DriverManager.getConnection(url, username, password);
    }

    private void createTables() throws SQLException {
        String createGlobalChatTable;
        String createIgnoreTable;
        String createLastMessengerTable;

        if (databaseType.equalsIgnoreCase("mysql")) {
            createGlobalChatTable = "CREATE TABLE IF NOT EXISTS simplechat_global_chat (" +
                    "player_uuid VARCHAR(36) PRIMARY KEY," +
                    "hidden BOOLEAN NOT NULL DEFAULT FALSE," +
                    "last_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP" +
                    ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4";

            createIgnoreTable = "CREATE TABLE IF NOT EXISTS simplechat_ignores (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY," +
                    "player_uuid VARCHAR(36) NOT NULL," +
                    "ignored_player_uuid VARCHAR(36) NOT NULL," +
                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                    "UNIQUE KEY unique_ignore (player_uuid, ignored_player_uuid)," +
                    "INDEX idx_player (player_uuid)" +
                    ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4";

            createLastMessengerTable = "CREATE TABLE IF NOT EXISTS simplechat_last_messengers (" +
                    "player_uuid VARCHAR(36) PRIMARY KEY," +
                    "last_messenger_uuid VARCHAR(36) NOT NULL," +
                    "last_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP" +
                    ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4";
        } else {
            createGlobalChatTable = "CREATE TABLE IF NOT EXISTS simplechat_global_chat (" +
                    "player_uuid TEXT PRIMARY KEY," +
                    "hidden BOOLEAN NOT NULL DEFAULT FALSE," +
                    "last_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                    ")";

            createIgnoreTable = "CREATE TABLE IF NOT EXISTS simplechat_ignores (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "player_uuid TEXT NOT NULL," +
                    "ignored_player_uuid TEXT NOT NULL," +
                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                    "UNIQUE(player_uuid, ignored_player_uuid)" +
                    ")";

            createLastMessengerTable = "CREATE TABLE IF NOT EXISTS simplechat_last_messengers (" +
                    "player_uuid TEXT PRIMARY KEY," +
                    "last_messenger_uuid TEXT NOT NULL," +
                    "last_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                    ")";
        }

        try (Statement stmt = connection.createStatement()) {
            stmt.execute(createGlobalChatTable);
            stmt.execute(createIgnoreTable);
            stmt.execute(createLastMessengerTable);
        }
    }

    // Global Chat Methods
    public boolean isGlobalChatHidden(UUID playerUUID) {
        String sql = "SELECT hidden FROM simplechat_global_chat WHERE player_uuid = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, playerUUID.toString());
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getBoolean("hidden");
            }
        } catch (SQLException e) {
            plugin.getLogger().warning("Failed to check global chat status for " + playerUUID + ": " + e.getMessage());
        }
        return false;
    }

    public void setGlobalChatHidden(UUID playerUUID, boolean hidden) {
        if (databaseType.equalsIgnoreCase("mysql")) {
            String sql = "INSERT INTO simplechat_global_chat (player_uuid, hidden) VALUES (?, ?) " +
                    "ON DUPLICATE KEY UPDATE hidden = ?, last_updated = CURRENT_TIMESTAMP";
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setString(1, playerUUID.toString());
                stmt.setBoolean(2, hidden);
                stmt.setBoolean(3, hidden);
                stmt.executeUpdate();
            } catch (SQLException e) {
                plugin.getLogger().warning("Failed to set global chat status for " + playerUUID + ": " + e.getMessage());
            }
        } else {
            String sql = "INSERT OR REPLACE INTO simplechat_global_chat (player_uuid, hidden) VALUES (?, ?)";
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setString(1, playerUUID.toString());
                stmt.setBoolean(2, hidden);
                stmt.executeUpdate();
            } catch (SQLException e) {
                plugin.getLogger().warning("Failed to set global chat status for " + playerUUID + ": " + e.getMessage());
            }
        }
    }

    // Ignore Methods
    public void addIgnore(UUID playerUUID, UUID ignoredUUID) {
        String sql = "INSERT INTO simplechat_ignores (player_uuid, ignored_player_uuid) VALUES (?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, playerUUID.toString());
            stmt.setString(2, ignoredUUID.toString());
            stmt.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().warning("Failed to add ignore for " + playerUUID + " -> " + ignoredUUID + ": " + e.getMessage());
        }
    }

    public void removeIgnore(UUID playerUUID, UUID ignoredUUID) {
        String sql = "DELETE FROM simplechat_ignores WHERE player_uuid = ? AND ignored_player_uuid = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, playerUUID.toString());
            stmt.setString(2, ignoredUUID.toString());
            stmt.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().warning("Failed to remove ignore for " + playerUUID + " -> " + ignoredUUID + ": " + e.getMessage());
        }
    }

    public void removeAllIgnores(UUID playerUUID) {
        String sql = "DELETE FROM simplechat_ignores WHERE player_uuid = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, playerUUID.toString());
            stmt.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().warning("Failed to remove all ignores for " + playerUUID + ": " + e.getMessage());
        }
    }

    public Set<UUID> getIgnoredPlayers(UUID playerUUID) {
        Set<UUID> ignoredPlayers = new HashSet<>();
        String sql = "SELECT ignored_player_uuid FROM simplechat_ignores WHERE player_uuid = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, playerUUID.toString());
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                try {
                    ignoredPlayers.add(UUID.fromString(rs.getString("ignored_player_uuid")));
                } catch (IllegalArgumentException e) {
                    plugin.getLogger().warning("Invalid UUID in database: " + rs.getString("ignored_player_uuid"));
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().warning("Failed to get ignored players for " + playerUUID + ": " + e.getMessage());
        }
        return ignoredPlayers;
    }

    // Last Messenger Methods
    public void setLastMessenger(UUID playerUUID, UUID lastMessengerUUID) {
        if (databaseType.equalsIgnoreCase("mysql")) {
            String sql = "INSERT INTO simplechat_last_messengers (player_uuid, last_messenger_uuid) VALUES (?, ?) " +
                    "ON DUPLICATE KEY UPDATE last_messenger_uuid = ?, last_updated = CURRENT_TIMESTAMP";
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setString(1, playerUUID.toString());
                stmt.setString(2, lastMessengerUUID.toString());
                stmt.setString(3, lastMessengerUUID.toString());
                stmt.executeUpdate();
            } catch (SQLException e) {
                plugin.getLogger().warning("Failed to set last messenger for " + playerUUID + ": " + e.getMessage());
            }
        } else {
            String sql = "INSERT OR REPLACE INTO simplechat_last_messengers (player_uuid, last_messenger_uuid) VALUES (?, ?)";
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setString(1, playerUUID.toString());
                stmt.setString(2, lastMessengerUUID.toString());
                stmt.executeUpdate();
            } catch (SQLException e) {
                plugin.getLogger().warning("Failed to set last messenger for " + playerUUID + ": " + e.getMessage());
            }
        }
    }

    public UUID getLastMessenger(UUID playerUUID) {
        String sql = "SELECT last_messenger_uuid FROM simplechat_last_messengers WHERE player_uuid = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, playerUUID.toString());
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                try {
                    return UUID.fromString(rs.getString("last_messenger_uuid"));
                } catch (IllegalArgumentException e) {
                    plugin.getLogger().warning("Invalid UUID in database: " + rs.getString("last_messenger_uuid"));
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().warning("Failed to get last messenger for " + playerUUID + ": " + e.getMessage());
        }
        return null;
    }

    public void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                plugin.getLogger().info("Database connection closed");
            }
        } catch (SQLException e) {
            plugin.getLogger().warning("Failed to close database connection: " + e.getMessage());
        }
    }

    public boolean testConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                try (Statement stmt = connection.createStatement()) {
                    stmt.executeQuery("SELECT 1");
                }
                return true;
            }
        } catch (SQLException e) {
            plugin.getLogger().warning("Database connection test failed: " + e.getMessage());
        }
        return false;
    }
}