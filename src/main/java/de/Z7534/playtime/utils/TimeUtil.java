package de.Z7534.playtime.utils;

public class TimeUtil {

    /**
     * Konvertiert Millisekunden in ein Array mit [days, hours, minutes, seconds, totalHours, totalMinutes]
     */
    public static long[] parseTime(long milliseconds) {
        long seconds = milliseconds / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;

        return new long[] {
                days,                    // Tage
                hours % 24,              // Stunden (0-23)
                minutes % 60,            // Minuten (0-59)
                seconds % 60,            // Sekunden (0-59)
                hours,                   // Gesamte Stunden
                minutes                  // Gesamte Minuten
        };
    }

    /**
     * Formatiert Zeit als lesbaren String
     */
    public static String formatTime(long milliseconds) {
        long[] time = parseTime(milliseconds);

        if (time[0] > 0) {
            return String.format("%d Tage, %d Stunden, %d Minuten", time[0], time[1], time[2]);
        } else if (time[1] > 0) {
            return String.format("%d Stunden, %d Minuten", time[1], time[2]);
        } else {
            return String.format("%d Minuten", time[2]);
        }
    }
}
