package de.Z7534.playtime.listeners;

import de.Z7534.playtime.Playtime;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerListener implements Listener {

    private final Playtime plugin;

    public PlayerListener(Playtime plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event) {
        // Join-Zeit speichern
        plugin.getJoinTimes().put(event.getPlayer().getUniqueId(), System.currentTimeMillis());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        Long joinTime = plugin.getJoinTimes().remove(event.getPlayer().getUniqueId());

        if (joinTime != null) {
            // Spielzeit berechnen und speichern
            long sessionTime = System.currentTimeMillis() - joinTime;
            plugin.getDatabaseManager().addPlaytime(
                    event.getPlayer().getUniqueId(),
                    event.getPlayer().getName(),
                    sessionTime
            );
        }
    }
}
