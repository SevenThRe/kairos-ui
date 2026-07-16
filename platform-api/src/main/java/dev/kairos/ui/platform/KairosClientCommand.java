package dev.kairos.ui.platform;

import java.util.Locale;

/** Parser for the standalone punctuation-prefix command surface. */
public final class KairosClientCommand {
    public enum Action { OPEN_GUI, LIST_THEMES, RELOAD_THEMES, SET_THEME }

    private final Action action;
    private final String argument;

    private KairosClientCommand(Action action, String argument) {
        this.action = action;
        this.argument = argument;
    }

    public Action getAction() { return action; }
    public String getArgument() { return argument; }

    public static KairosClientCommand parse(String message, String customerPrefix) {
        if (message == null || customerPrefix == null) return null;
        String trimmed = message.trim();
        if (!trimmed.startsWith(customerPrefix)) return null;
        return parseBody(trimmed.substring(customerPrefix.length()));
    }

    public static KairosClientCommand parseAnyPunctuationPrefix(String message) {
        if (message == null) return null;
        String trimmed = message.trim();
        int commandStart = trimmed.toLowerCase(Locale.ROOT).indexOf("kairos");
        if (commandStart <= 0) return null;
        String prefix = trimmed.substring(0, commandStart);
        for (int index = 0; index < prefix.length(); index++) {
            char character = prefix.charAt(index);
            if (Character.isLetterOrDigit(character) || Character.isWhitespace(character)) return null;
        }
        return parseBody(trimmed.substring(commandStart));
    }

    private static KairosClientCommand parseBody(String body) {
        String[] parts = body.trim().split("\\s+");
        if (parts.length < 2 || !"kairos".equalsIgnoreCase(parts[0])) return null;
        if (parts.length == 2 && "gui".equalsIgnoreCase(parts[1])) {
            return new KairosClientCommand(Action.OPEN_GUI, "");
        }
        if (parts.length == 2 && "themes".equalsIgnoreCase(parts[1])) {
            return new KairosClientCommand(Action.LIST_THEMES, "");
        }
        if (parts.length == 3 && "themes".equalsIgnoreCase(parts[1])
            && "reload".equalsIgnoreCase(parts[2])) {
            return new KairosClientCommand(Action.RELOAD_THEMES, "");
        }
        if (parts.length == 3 && "theme".equalsIgnoreCase(parts[1])) {
            return new KairosClientCommand(Action.SET_THEME, parts[2].toLowerCase(Locale.ROOT));
        }
        return null;
    }
}
