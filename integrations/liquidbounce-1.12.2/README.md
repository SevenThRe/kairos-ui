# Kairos UI for LiquidBounce 1.12.2

This is the real Forge 1.12.2 integration target. It is not a mock catalog and
does not contain a native ClickGUI fallback.

The reproducible build:

1. clones the pinned GPL-3.0 LiquidBounce 1.12.2 source revision;
2. replaces its `ClickGUI` module with the Kairos MCEF screen;
3. exposes the live LiquidBounce `ModuleManager`, module states, keybinds and
   `Value` objects to the bundled HTML/CSS/JavaScript UI;
4. adds the configurable-prefix `kairos gui` command and binds Right Control by
   default;
5. embeds MCEF 1.12.2-1.11 and its shutdown coremod into the same Forge JAR;
6. includes the complete upstream module implementation, HUD and render modules.

At runtime, press Right Control or enter `.kairos gui`. If the LiquidBounce
command prefix was changed, the same command uses that prefix automatically.

MCEF downloads its platform-specific Chromium/JCEF native files on first launch.
No separate MCEF Forge mod JAR is required. Kairos replaces the expired certificate
resource in MCEF 1.11 with the current ISRG Root X1 trust anchor so its HTTPS mirror
bootstrap remains usable.

## Local preparation

```bash
git clone https://github.com/Kamiya1337/LiquidBounce-1.12.2.git /tmp/lb112
git -C /tmp/lb112 checkout 23e11be9b078a931edc68078f4be62a1c48724a5
curl -fL https://github.com/montoyo/mcef/releases/download/1.12.2-1.11/mcef-1.12.2-1.11-api.jar \
  -o /tmp/mcef-api.jar
./integrations/liquidbounce-1.12.2/prepare.sh /tmp/lb112 /tmp/mcef-api.jar
JAVA_HOME=/path/to/jdk8 /tmp/lb112/gradlew -p /tmp/lb112 :1.12.2-Forge:build
```

GitHub Actions performs the same pinned build and validates the single JAR's
LiquidBounce modules, Kairos bridge/assets, MCEF implementation and nested coremod.

