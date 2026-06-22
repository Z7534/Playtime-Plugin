package de.Z7534.playtime.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import de.Z7534.playtime.Playtime;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class DatabaseManager {

    private final Playtime plugin;
    private HikariDataSource dataSource;

    public DatabaseManager(Playtime plugin) {
        this.plugin = plugin;
    }

    public boolean connect() {
        try {
            String host = plugin.getConfig().getString("database.host", "localhost");
            int port = plugin.getConfig().getInt("database.port", 3306);
            String database = plugin.getConfig().getString("database.database", "minecraft");
            String username = plugin.getConfig().getString("database.username", "root");
            String password = plugin.getConfig().getString("database.password", "password");

            HikariConfig config = new HikariConfig();
            config.setJdbcUrl("jdbc:mariadb://" + host + ":" + port + "/" + database);
            config.setUsername(username);
            config.setPassword(password);
            config.setDriverClassName("org.mariadb.jdbc.Driver");

            // Pool Einstellungen aus Config
            config.setMaximumPoolSize(plugin.getConfig().getInt("database.pool.maximum-pool-size", 10));
            config.setMinimumIdle(plugin.getConfig().getInt("database.pool.minimum-idle", 2));
            config.setConnectionTimeout(plugin.getConfig().getLong("database.pool.connection-timeout", 30000));
            config.setIdleTimeout(plugin.getConfig().getLong("database.pool.idle-timeout", 600000));
            config.setMaxLifetime(plugin.getConfig().getLong("database.pool.max-lifetime", 1800000));

            config.setPoolName("Playtime-Pool");
            config.addDataSourceProperty("cachePrepStmts", "true");
            config.addDataSourceProperty("prepStmtCacheSize", "250");
            config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");

            dataSource = new HikariDataSource(config);
            plugin.getLogger().info("Datenbankverbindung hergestellt!");
            return true;
        } catch (Exception e) {
            plugin.getLogger().severe("Fehler beim Verbinden zur Datenbank: " + e.getMessage());
            return false;
        }
    }

    public void disconnect() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
            plugin.getLogger().info("Datenbankverbindung geschlossen!");
        }
    }

    public Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    public void createTables() {
        String sql = """
            CREATE TABLE IF NOT EXISTS playtime (
                uuid VARCHAR(36) PRIMARY KEY,
                player_name VARCHAR(16) NOT NULL,
                playtime_ms BIGINT NOT NULL DEFAULT 0,
                first_join TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                last_seen TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
            )
            """;

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.executeUpdate();
            plugin.getLogger().info("Datenbank-Tabellen erstellt/überprüft!");
        } catch (SQLException e) {
            plugin.getLogger().severe("Fehler beim Erstellen der Tabellen: " + e.getMessage());
        }
    }

    public void addPlaytime(UUID uuid, String playerName, long milliseconds) {
        CompletableFuture.runAsync(() -> {
            String sql = """
                INSERT INTO playtime (uuid, player_name, playtime_ms)
                VALUES (?, ?, ?)
                ON DUPLICATE KEY UPDATE
                    playtime_ms = playtime_ms + VALUES(playtime_ms),
                    player_name = VALUES(player_name)
                """;

            try (Connection conn = getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, uuid.toString());
                stmt.setString(2, playerName);
                stmt.setLong(3, milliseconds);
                stmt.executeUpdate();
            } catch (SQLException e) {
                plugin.getLogger().severe("Fehler beim Speichern der Spielzeit: " + e.getMessage());
            }
        });
    }

    public CompletableFuture<Long> getPlaytime(UUID uuid) {
        return CompletableFuture.supplyAsync(() -> {
            String sql = "SELECT playtime_ms FROM playtime WHERE uuid = ?";

            try (Connection conn = getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, uuid.toString());
                ResultSet rs = stmt.executeQuery();

                if (rs.next()) {
                    return rs.getLong("playtime_ms");
                }
            } catch (SQLException e) {
                plugin.getLogger().severe("Fehler beim Abrufen der Spielzeit: " + e.getMessage());
            }
            return 0L;
        });
    }

    public CompletableFuture<Long> getPlaytimeByName(String playerName) {
        return CompletableFuture.supplyAsync(() -> {
            String sql = "SELECT playtime_ms FROM playtime WHERE LOWER(player_name) = LOWER(?)";

            try (Connection conn = getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, playerName);
                ResultSet rs = stmt.executeQuery();

                if (rs.next()) {
                    return rs.getLong("playtime_ms");
                }
            } catch (SQLException e) {
                plugin.getLogger().severe("Fehler beim Abrufen der Spielzeit: " + e.getMessage());
            }
            return -1L; // -1 bedeutet Spieler nicht gefunden
        });
    }

    public CompletableFuture<Boolean> playerExists(String playerName) {
        return CompletableFuture.supplyAsync(() -> {
            String sql = "SELECT 1 FROM playtime WHERE LOWER(player_name) = LOWER(?)";

            try (Connection conn = getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, playerName);
                ResultSet rs = stmt.executeQuery();
                return rs.next();
            } catch (SQLException e) {
                plugin.getLogger().severe("Fehler beim Überprüfen des Spielers: " + e.getMessage());
            }
            return false;
        });
    }
}
