package de.Z7534.playtime.commands;

import de.Z7534.playtime.Playtime;
import de.Z7534.playtime.utils.MessageUtil;
import de.Z7534.playtime.utils.TimeUtil;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class PlaytimeCommand implements CommandExecutor, TabCompleter {

    private final Playtime plugin;

    public PlaytimeCommand(Playtime plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, @NotNull String[] args) {

        // Keine Argumente = eigene Spielzeit
        if (args.length == 0) {
            if (!(sender instanceof Player player)) {
                sender.sendMessage(plugin.getMessage("player-only"));
                return true;
            }

            if (!player.hasPermission("playtime.use")) {
                player.sendMessage(plugin.getMessage("no-permission"));
                return true;
            }

            showOwnPlaytime(player);
            return true;
        }

        // Mit Argument = Spielzeit eines anderen Spielers
        if (args.length == 1) {
            if (!sender.hasPermission("playtime.other")) {
                sender.sendMessage(plugin.getMessage("no-permission-other"));
                return true;
            }

            String targetName = args[0];
            showOtherPlaytime(sender, targetName);
            return true;
        }

        return true;
    }

    private void showOwnPlaytime(Player player) {
        // Aktuelle Session-Zeit hinzurechnen
        Long joinTime = plugin.getJoinTimes().get(player.getUniqueId());
        long currentSessionTime = joinTime != null ? System.currentTimeMillis() - joinTime : 0;

        plugin.getDatabaseManager().getPlaytime(player.getUniqueId()).thenAccept(playtime -> {
            long totalPlaytime = playtime + currentSessionTime;

            if (totalPlaytime <= 0) {
                String message = plugin.getRawPrefix() + plugin.getRawMessage("no-playtime")
                        .replace("%player%", player.getName());
                player.sendMessage(MessageUtil.colorize(message));
                return;
            }

            Component message = formatPlaytimeMessage("own-playtime", player.getName(), totalPlaytime);
            player.sendMessage(message);
        }).exceptionally(e -> {
            player.sendMessage(plugin.getMessage("database-error"));
            plugin.getLogger().severe("Fehler beim Abrufen der Spielzeit: " + e.getMessage());
            return null;
        });
    }

    private void showOtherPlaytime(CommandSender sender, String targetName) {
        // Prüfen ob der Spieler online ist
        Player onlineTarget = Bukkit.getPlayerExact(targetName);

        if (onlineTarget != null) {
            // Online Spieler - Session-Zeit hinzurechnen
            Long joinTime = plugin.getJoinTimes().get(onlineTarget.getUniqueId());
            long currentSessionTime = joinTime != null ? System.currentTimeMillis() - joinTime : 0;

            plugin.getDatabaseManager().getPlaytime(onlineTarget.getUniqueId()).thenAccept(playtime -> {
                long totalPlaytime = playtime + currentSessionTime;
                Component message = formatPlaytimeMessage("other-playtime", onlineTarget.getName(), totalPlaytime);
                sender.sendMessage(message);
            }).exceptionally(e -> {
                sender.sendMessage(plugin.getMessage("database-error"));
                return null;
            });
        } else {
            // Offline Spieler - aus Datenbank laden
            plugin.getDatabaseManager().getPlaytimeByName(targetName).thenAccept(playtime -> {
                if (playtime < 0) {
                    String message = plugin.getRawPrefix() + plugin.getRawMessage("player-not-found")
                            .replace("%player%", targetName);
                    sender.sendMessage(MessageUtil.colorize(message));
                    return;
                }

                if (playtime == 0) {
                    String message = plugin.getRawPrefix() + plugin.getRawMessage("no-playtime")
                            .replace("%player%", targetName);
                    sender.sendMessage(MessageUtil.colorize(message));
                    return;
                }

                Component message = formatPlaytimeMessage("other-playtime", targetName, playtime);
                sender.sendMessage(message);
            }).exceptionally(e -> {
                sender.sendMessage(plugin.getMessage("database-error"));
                return null;
            });
        }
    }

    private Component formatPlaytimeMessage(String messagePath, String playerName, long playtimeMs) {
        long[] time = TimeUtil.parseTime(playtimeMs);
        long days = time[0];
        long hours = time[1];
        long minutes = time[2];
        long seconds = time[3];
        long totalHours = time[4];
        long totalMinutes = time[5];

        String message = plugin.getRawPrefix() + plugin.getRawMessage(messagePath)
                .replace("%player%", playerName)
                .replace("%days%", String.valueOf(days))
                .replace("%hours%", String.valueOf(hours))
                .replace("%minutes%", String.valueOf(minutes))
                .replace("%seconds%", String.valueOf(seconds))
                .replace("%total_hours%", String.valueOf(totalHours))
                .replace("%total_minutes%", String.valueOf(totalMinutes));

        return MessageUtil.colorize(message);
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command,
                                                @NotNull String alias, @NotNull String[] args) {
        if (args.length == 1 && sender.hasPermission("playtime.other")) {
            String input = args[0].toLowerCase();
            return Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .filter(name -> name.toLowerCase().startsWith(input))
                    .collect(Collectors.toList());
        }
        return new ArrayList<>();
    }
}
