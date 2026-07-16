# Minecraft integration build

This independent Essential Gradle Toolkit build preprocesses one client source tree into:

- Forge 1.12.2 (Java 8, LWJGL2)
- Forge 1.20.1 (Java 17, GLFW/GuiGraphics)

The standard `engine` subproject compiles all Minecraft-independent code once as Java 8;
each endpoint depends on and embeds that output, so the preprocessor only touches the
small loader/screen bridge.

## Playable 1.12.2 preview

The 0.3.0 Forge 1.12.2 artifact contains a real module runtime rather than the old static
demo catalog. These modules are currently implemented and visible in the ClickGUI:

- Movement: Sprint, AutoJump
- Player: AutoRespawn, FastPlace
- Render: FullBright, PlayerESP, HUD, ModuleList, Coordinates
- Misc: Notifications

Clicking a module in either Kairos layout changes the live runtime state. State is stored
in `.minecraft/kairos-ui/modules.properties`. Forge client ticks execute movement/player
modules, `RenderGameOverlayEvent` draws HUD widgets, and `RenderWorldLastEvent` draws
PlayerESP. The 1.20.1 endpoint still compiles as an engine integration target, but this
specific functional preview is intentionally 1.12.2-first.

Press `Right Ctrl` in game to open or close Kairos. Press `F6` inside the screen to
switch between the fixed workbench and draggable panel desktop. The standalone bridge
also recognizes `<punctuation-prefix>kairos gui`, for example `.kairos gui`,
`!kairos gui`, and `/kairos gui`. A consuming client may call `KairosMod.openGui()`
from its own command manager, or call `handleGuiCommand(message, prefix)` for exact
prefix parsing. `setOpenKeyCode(...)` connects the activation key to the client's own
keybind/config service, while Right Ctrl remains the default.

Theme commands use the same prefix rule:

```text
.kairos themes
.kairos theme obsidian-violet
.kairos themes reload
```

Runtime module commands also accept any single punctuation prefix:

```text
.kairos modules
.kairos toggle sprint
.kairos toggle player-esp
```

Theme packs are read from `.minecraft/kairos-ui/themes/*.properties`. A consuming client
can route its exact prefix to `KairosMod.handleCommand(message, prefix)`.

The included `MinecraftFallbackCanvas` is the always-available compatibility path. It
draws rounded/tinted geometry, scaled native text and nested scissors. Production clients
can bind the `ui-render-opengl` command pipeline for custom font atlases, SDF batching and
the shared Kawase framebuffer blur pass.

Build from this directory with Gradle 9.2 running on JDK 21 and both JDK 8 and JDK 17
available as compile toolchains:

```bash
gradle :1.12.2-forge:build :1.20.1-forge:build
```
