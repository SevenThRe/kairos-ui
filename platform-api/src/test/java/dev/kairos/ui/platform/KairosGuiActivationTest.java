package dev.kairos.ui.platform;

public final class KairosGuiActivationTest {
    public static void main(String[] args) {
        require(KairosGuiActivation.LWJGL2_RIGHT_CONTROL == 157, "LWJGL2 right-control code");
        require(KairosGuiActivation.GLFW_RIGHT_CONTROL == 345, "GLFW right-control code");
        require(KairosGuiActivation.matches("..kairos gui", ".."), "exact custom prefix");
        require(KairosGuiActivation.matches("kairos   GUI", ""), "prefix already stripped");
        require(KairosGuiActivation.matchesAnyPunctuationPrefix(".kairos gui"), "dot prefix");
        require(KairosGuiActivation.matchesAnyPunctuationPrefix("!KAIROS   GUI"), "case and spacing");
        require(KairosGuiActivation.matchesAnyPunctuationPrefix("/kairos gui"), "slash prefix");
        require(!KairosGuiActivation.matchesAnyPunctuationPrefix("hello kairos gui"), "chat is not swallowed");
        require(!KairosGuiActivation.matchesAnyPunctuationPrefix(".kairos config"), "unknown subcommand");
        System.out.println("KairosGuiActivationTest passed");
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
