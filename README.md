# Kairos UI Engine

Version-independent UI foundations plus a real LiquidBounce Forge 1.12.2 Web ClickGUI integration.

Kairos ships a finished modern default rather than an unstyled widget kit: an offline
HTML/CSS ClickGUI rendered by Chromium, blue-black glass surfaces, violet controls,
animated notifications, a right-aligned ModuleList, HUD widgets, combat HUD primitives,
and entity/world-object ESP foundations. Client authors can override the entire ClickGUI
palette and typography with CSS and configure HUD/ESP styles without forking the runtime.

The production Forge 1.12.2 artifact now uses the GPL LiquidBounce b73 1.12.2 port as
its client runtime. Kairos is connected directly to its real `ModuleManager`, module
states and `Value` objects. The earlier standalone demo module catalog and native
ClickGUI have been removed from the deliverable. MCEF and its shutdown coremod are
embedded in the same Forge JAR, so there is no separate `mcef` mod dependency.

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
| `integrations/liquidbounce-1.12.2` | Pinned GPL source overlay, MCEF host, live module/value bridge and reproducible single-JAR build |
| `examples/modern-clickgui` | Headless model/HUD reference scenes retained for engine tests |

The integrated client opens/closes with `Right Ctrl`. Its command is `kairos gui` under
LiquidBounce's currently configured prefix, so the default is:

```text
.kairos gui
```

Changing the LiquidBounce prefix automatically changes the Kairos command prefix. Theme,
blur and animation values are persisted through LiquidBounce's normal value config.
The complete palette and component appearance can be overridden with
`.minecraft/kairos-ui/custom.css`. Bundled assets are served from `kairos://ui/`;
bridge calls are origin checked and scheduled onto Minecraft's client thread.

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

## LiquidBounce 1.12.2 build

The integration is reproducible from a pinned upstream commit and MCEF release. See
[`integrations/liquidbounce-1.12.2/README.md`](integrations/liquidbounce-1.12.2/README.md)
for local steps. GitHub Actions builds and inspects the combined artifact for real
LiquidBounce modules, Kairos classes/assets, MCEF/JCEF classes and the nested MCEF
shutdown coremod.

```bash
./integrations/liquidbounce-1.12.2/prepare.sh /path/to/liquidbounce /path/to/mcef-api.jar
JAVA_HOME=/path/to/jdk8 /path/to/liquidbounce/gradlew -p /path/to/liquidbounce :1.12.2-Forge:build
```
