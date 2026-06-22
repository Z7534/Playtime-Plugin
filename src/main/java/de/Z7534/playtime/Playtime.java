package de.Z7534.playtime;

import de.Z7534.playtime.commands.PlaytimeCommand;
import de.Z7534.playtime.database.DatabaseManager;
import de.Z7534.playtime.listeners.PlayerListener;
import de.Z7534.playtime.utils.MessageUtil;
import net.kyori.adventure.text.Component;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Playtime extends JavaPlugin {

    private static Playtime instance;
    private DatabaseManager databaseManager;
    private final Map<UUID, Long> joinTimes = new HashMap<>();

    @Override
    public void onEnable() {
        instance = this;

        // Config speichern/laden
        saveDefaultConfig();

        // Datenbank initialisieren
        databaseManager = new DatabaseManager(this);
        if (!databaseManager.connect()) {
            getLogger().severe("Konnte keine Verbindung zur Datenbank herstellen! Plugin wird deaktiviert.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        // Tabellen erstellen
        databaseManager.createTables();

        // Listener registrieren
        getServer().getPluginManager().registerEvents(new PlayerListener(this), this);

        // Commands registrieren
        PlaytimeCommand playtimeCommand = new PlaytimeCommand(this);
        getCommand("playtime").setExecutor(playtimeCommand);
        getCommand("playtime").setTabCompleter(playtimeCommand);

        getLogger().info("Playtime Plugin wurde aktiviert!");
    }

    @Override
    public void onDisable() {
        // Alle noch eingeloggten Spieler speichern
        getServer().getOnlinePlayers().forEach(player -> {
            Long joinTime = joinTimes.remove(player.getUniqueId());
            if (joinTime != null) {
                long sessionTime = System.currentTimeMillis() - joinTime;
                databaseManager.addPlaytime(player.getUniqueId(), player.getName(), sessionTime);
            }
        });

        // Datenbank schließen
        if (databaseManager != null) {
            databaseManager.disconnect();
        }

        getLogger().info("Playtime Plugin wurde deaktiviert!");
    }

    public static Playtime getInstance() {
        return instance;
    }

    public DatabaseManager getDatabaseManager() {
        return databaseManager;
    }

    public Map<UUID, Long> getJoinTimes() {
        return joinTimes;
    }

    public Component getMessage(String path) {
        String prefix = getConfig().getString("messages.prefix", "");
        String message = getConfig().getString("messages." + path, "&cNachricht nicht gefunden: " + path);
        return MessageUtil.colorize(prefix + message);
    }

    public Component getMessageWithoutPrefix(String path) {
        String message = getConfig().getString("messages." + path, "&cNachricht nicht gefunden: " + path);
        return MessageUtil.colorize(message);
    }

    public String getRawMessage(String path) {
        return getConfig().getString("messages." + path, "&cNachricht nicht gefunden: " + path);
    }

    public String getRawPrefix() {
        return getConfig().getString("messages.prefix", "");
    }
}
