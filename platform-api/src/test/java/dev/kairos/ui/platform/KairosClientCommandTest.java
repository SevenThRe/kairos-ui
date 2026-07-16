package dev.kairos.ui.platform;

public final class KairosClientCommandTest {
    public static void main(String[] args) {
        require(KairosClientCommand.parseAnyPunctuationPrefix(".kairos gui").getAction()
            == KairosClientCommand.Action.OPEN_GUI, "gui command");
        require(KairosClientCommand.parseAnyPunctuationPrefix("!KAIROS themes").getAction()
            == KairosClientCommand.Action.LIST_THEMES, "themes command");
        KairosClientCommand theme = KairosClientCommand.parse("::kairos theme Arctic-Glass", "::");
        require(theme.getAction() == KairosClientCommand.Action.SET_THEME, "theme action");
        require("arctic-glass".equals(theme.getArgument()), "theme id normalized");
        require(KairosClientCommand.parseAnyPunctuationPrefix(".kairos themes reload").getAction()
            == KairosClientCommand.Action.RELOAD_THEMES, "reload themes command");
        require(KairosClientCommand.parseAnyPunctuationPrefix("hello kairos gui") == null, "normal chat ignored");
        System.out.println("KairosClientCommandTest passed");
    }

    private static void require(boolean value, String message) {
        if (!value) throw new AssertionError(message);
    }
}
