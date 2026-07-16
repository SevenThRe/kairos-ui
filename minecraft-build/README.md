# Minecraft integration build

This independent Essential Gradle Toolkit build preprocesses one client source tree into:

- Forge 1.12.2 (Java 8, LWJGL2)
- Forge 1.20.1 (Java 17, GLFW/GuiGraphics)

The standard `engine` subproject compiles all Minecraft-independent code once as Java 8;
each endpoint depends on and embeds that output, so the preprocessor only touches the
small loader/screen bridge.

## Forge 1.12.2 Web UI endpoint

The 0.4.0 Forge 1.12.2 artifact contains a real module runtime and a browser-rendered
ClickGUI. It requires [MCEF 1.12.2-1.11](https://github.com/montoyo/mcef/releases/tag/1.12.2-1.11)
as a separate Forge mod. These modules are currently implemented and visible in the UI:

- Movement: Sprint, AutoJump
- Player: AutoRespawn, FastPlace
- Render: FullBright, PlayerESP, HUD, ModuleList, Coordinates
- Misc: Notifications

Clicking a module changes the live runtime state. Right click (or the three-dot action)
opens its settings in the inspector. State is stored
in `.minecraft/kairos-ui/modules.properties`. Forge client ticks execute movement/player
modules, `RenderGameOverlayEvent` draws HUD widgets, and `RenderWorldLastEvent` draws
PlayerESP. The 1.20.1 endpoint still compiles as an engine integration target, but this
specific functional preview is intentionally 1.12.2-first.

Press `Right Ctrl` in game to open or close Kairos. The standalone bridge also recognizes
`<punctuation-prefix>kairos gui`, for example `.kairos gui`,
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

Theme packs for HUD/ESP are read from `.minecraft/kairos-ui/themes/*.properties`. The
Web ClickGUI palette is CSS-native: copy `web-theme.example.css` to
`.minecraft/kairos-ui/web-theme.css` and override variables without rebuilding the mod.
A consuming client can route its exact prefix to `KairosMod.handleCommand(message, prefix)`.

There is intentionally no native ClickGUI fallback. The endpoint serves bundled HTML,
CSS and JavaScript from the private `kairos://ui/` origin, validates bridge calls by
origin, and renders through MCEF. If MCEF is missing or enters virtual mode, Kairos emits
an explicit error and does not display the deleted native UI. Minecraft's framebuffer
blur shader is activated behind the transparent browser while the screen is open.

Build from this directory with Gradle 9.2 running on JDK 21 and both JDK 8 and JDK 17
available as compile toolchains:

```bash
gradle :1.12.2-forge:build :1.20.1-forge:build
```
