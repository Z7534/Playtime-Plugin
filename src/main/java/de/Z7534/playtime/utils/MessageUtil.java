package de.Z7534.playtime.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MessageUtil {

    private static final Pattern HEX_PATTERN = Pattern.compile("&#([A-Fa-f0-9]{6})");
    private static final LegacyComponentSerializer LEGACY_SERIALIZER = LegacyComponentSerializer.builder()
            .character('&')
            .hexCharacter('#')
            .hexColors()
            .build();

    /**
     * Konvertiert eine Nachricht mit &-Farbcodes und &#RRGGBB Hex-Farben
     * in eine formatierte Component
     */
    public static Component colorize(String message) {
        if (message == null || message.isEmpty()) {
            return Component.empty();
        }

        // Hex-Farben konvertieren: &#RRGGBB -> &#RRGGBB (Adventure format)
        message = translateHexCodes(message);

        return LEGACY_SERIALIZER.deserialize(message);
    }

    /**
     * Konvertiert &#RRGGBB zu einem Format, das Adventure versteht
     */
    private static String translateHexCodes(String message) {
        Matcher matcher = HEX_PATTERN.matcher(message);
        StringBuilder builder = new StringBuilder();

        while (matcher.find()) {
            String hex = matcher.group(1);
            matcher.appendReplacement(builder, "&#" + hex);
        }
        matcher.appendTail(builder);

        return builder.toString();
    }
}
