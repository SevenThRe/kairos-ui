package dev.kairos.ui.platform;

import java.util.Locale;

/** Stable activation contract shared by standalone Forge and consuming client integrations. */
public final class KairosGuiActivation {
    public static final int LWJGL2_RIGHT_CONTROL = 157;
    public static final int GLFW_RIGHT_CONTROL = 345;

    private KairosGuiActivation() {}

    /** Matches an exact customer prefix, including an empty prefix when a command manager already stripped it. */
    public static boolean matches(String message, String customerPrefix) {
        if (message == null || customerPrefix == null) return false;
        String trimmed = message.trim();
        if (!trimmed.startsWith(customerPrefix)) return false;
        return isCommandBody(trimmed.substring(customerPrefix.length()));
    }

    /** Standalone fallback for common punctuation prefixes such as '.', ',', '!', '#', and '/'. */
    public static boolean matchesAnyPunctuationPrefix(String message) {
        if (message == null) return false;
        String trimmed = message.trim();
        String lower = trimmed.toLowerCase(Locale.ROOT);
        int commandStart = lower.indexOf("kairos");
        if (commandStart <= 0) return false;
        String prefix = trimmed.substring(0, commandStart);
        for (int index = 0; index < prefix.length(); index++) {
            char character = prefix.charAt(index);
            if (Character.isLetterOrDigit(character) || Character.isWhitespace(character)) return false;
        }
        return isCommandBody(trimmed.substring(commandStart));
    }

    private static boolean isCommandBody(String value) {
        String[] parts = value.trim().toLowerCase(Locale.ROOT).split("\\s+");
        return parts.length == 2 && "kairos".equals(parts[0]) && "gui".equals(parts[1]);
    }
}
