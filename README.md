# Kairos UI Engine

Version-independent client UI foundations with a WebView ClickGUI and native HUD/ESP runtime.

Kairos ships a finished modern default rather than an unstyled widget kit: an offline
HTML/CSS ClickGUI rendered by Chromium, blue-black glass surfaces, violet controls,
animated notifications, a right-aligned ModuleList, HUD widgets, combat HUD primitives,
and entity/world-object ESP foundations. Client authors can override the entire ClickGUI
palette and typography with CSS and configure HUD/ESP styles without forking the runtime.

The production Forge 1.12.2 endpoint uses one WebView composition: category navigation,
a compact module list, and an in-context settings inspector. The earlier native
`MinecraftFallbackCanvas` path has been deleted and cannot reappear when WebView setup
fails. Native HUD and ESP rendering remain separate because they execute every game frame.

## Design constraints

- Public core targets Java 8 bytecode.
- `ui-api`, `ui-core`, and `ui-components` contain no Minecraft, Forge, Fabric, LWJGL,
  GLFW, `MatrixStack`, `PoseStack`, or `GuiGraphics` references.
- Minecraft lifecycle, coordinates, input, textures, framebuffers, and GL state live
  behind `platform-api`.
- The render core emits semantic drawing operations. Platform renderers decide how
  those operations are batched and submitted.
- Mode-specific settings use visibility rules and remain inside the selected module.

## Implemented modules

| Module | Responsibility |
| --- | --- |
| `ui-api` | Geometry, theme tokens, drawing and input contracts |
| `ui-core` | Node tree, layout, focus, pointer capture and animation |
| `ui-components` | Module/setting model, modern HUD and competitive combat HUD foundations |
| `ui-esp` | Entity/world-object snapshots, 2D projection, native 3D sink, boxes, health, equipment and tags |
| `platform-api` | Screen, clock and render host boundary |
| `ui-render-opengl` | Frame planning, shape batches, font atlas packing and GLSL 1.20 resources |
| `ui-preview-awt` / `ui-preview-svg` | Deterministic headless visual preview backends |
| `platform-1.12.2-forge` | Tested LWJGL2 coordinate, key and scissor conversion |
| `platform-1.20.1-common` | Tested GLFW coordinate, key and scissor conversion |
| `minecraft-build` | EGT Forge endpoints, MCEF WebView host, secure JS bridge and bundled Web UI |
| `examples/modern-clickgui` | Headless model/HUD reference scenes retained for engine tests |

The Minecraft build opens/closes with `Right Ctrl`. `.kairos gui`, `!kairos gui`, `/kairos gui`, and other
punctuation prefixes are recognized by the standalone bridge. Theme commands are:

```text
.kairos themes
.kairos theme arctic-glass
.kairos themes reload
```

Custom theme files live in `.minecraft/kairos-ui/themes/*.properties`; the selected
theme is persisted. Consuming clients can call `KairosMod.openGui()` or
`KairosMod.handleCommand(message, prefix)` from an existing command manager.

The Forge 1.12.2 `0.4.0` endpoint uses a persistent live module manager.
Sprint, AutoJump, AutoRespawn, FastPlace, FullBright, PlayerESP, HUD, ModuleList,
Coordinates, and Notifications are connected to Forge tick/render events and toggled by
the same module objects displayed by Kairos. This is a clean-room Forge implementation;
no LiquidBounce GPL source is copied into the repository.

The ClickGUI requires the separate MCEF `1.12.2-1.11` Forge mod. Bundled assets are
served only from `kairos://ui/`; bridge calls are origin checked and scheduled back onto
Minecraft's client thread. Missing/virtual MCEF produces an explicit chat error—there is
no native fallback. Copy `minecraft-build/web-theme.example.css` to
`.minecraft/kairos-ui/web-theme.css` to override the Web UI theme.

## Preview truth

The old Java ClickGUI PNG/SVG previews and their scene classes have been deleted because
they no longer represented the endpoint. The production menu is exactly the bundled
`index.html`, `app.css`, `theme.css`, and `app.js` rendered by MCEF and connected to the
live Java bridge. Remaining generated PNG/SVG artifacts cover native HUD/ESP scene code
only; they are not presented as ClickGUI screenshots. See [docs/PREVIEWS.md](docs/PREVIEWS.md).

## Guides

- [Theme packs and hot switching](docs/THEMING.md)
- [ESP data, matrices and renderer integration](docs/ESP.md)
- [Architecture and platform boundaries](docs/ARCHITECTURE.md)

## Verify without Gradle

The shared engine has no external runtime dependencies. On a JDK:

```bash
./scripts/verify.sh
```

The script compiles with `--release 8`, runs engine, component, renderer and endpoint
mapping tests, checks both preprocessor branches, scans shared sources for forbidden
platform imports, and emits PNG/SVG previews under `out/verify/previews`.

## Minecraft endpoint build

`minecraft-build` is independent so Forge mappings and downloads cannot destabilize
the Java-8 engine build. With Gradle 9.2 running on JDK 21 plus JDK 8 and JDK 17
available as compile toolchains:

```bash
gradle -p minecraft-build :1.12.2-forge:build :1.20.1-forge:build
```

GitHub Actions performs endpoint compilation in addition to the dependency-free engine verification.
