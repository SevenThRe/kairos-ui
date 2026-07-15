# Kairos UI Engine

Version-independent retained-mode UI foundations for a modern Minecraft client UI.

The engine supports both accepted Kairos compositions: a fixed three-column workspace
with category navigation, module cards, and settings edited in context; and a floating
category-panel desktop that keeps traditional ClickGUI muscle memory. They share the
same module model, visual primitives, typography, glass effects, and theme tokens.

## Design constraints

- Public core targets Java 8 bytecode.
- `ui-api`, `ui-core`, and `ui-components` contain no Minecraft, Forge, Fabric, LWJGL,
  GLFW, `MatrixStack`, `PoseStack`, or `GuiGraphics` references.
- Minecraft lifecycle, coordinates, input, textures, framebuffers, and GL state live
  behind `platform-api`.
- The render core emits semantic drawing operations. Platform renderers decide how
  those operations are batched and submitted.
- Mode-specific settings use visibility rules and remain inside the selected module.

## Modules

| Module | Responsibility |
| --- | --- |
| `ui-api` | Geometry, theme tokens, drawing and input contracts |
| `ui-core` | Node tree, layout, focus, pointer capture and animation |
| `ui-components` | Module/setting model and the Kairos three-column workbench |
| `platform-api` | Screen, clock and render host boundary |
| `platform-1.12.2-forge` | Legacy Forge/LWJGL2 integration boundary |
| `platform-1.20.1-common` | Modern Screen/GLFW integration boundary |
| `examples/modern-clickgui` | Version-free executable scene example |

The platform projects intentionally do not pretend to contain Forge bindings yet.
Those bindings must be implemented against the real Kairos repository and its chosen
mappings/build system.

## Verify without Gradle

The repository has no external runtime dependencies in this milestone. On a JDK:

```bash
./scripts/verify.sh
```

The script compiles with `--release 8`, runs engine tests, scans core sources for
forbidden platform imports, and executes the workbench example.

## Next integration milestone

1. Map Kairos modules and values to `UiModule` and `UiSetting`.
2. Implement `Legacy112PlatformHost` using Forge 1.12.2 `GuiScreen` and LWJGL2 input.
3. Implement the first OpenGL 2.1 command renderer with scissor and state restoration.
4. Add font atlas, SDF rounded rectangles, shared blur pass, and capability fallback.
5. Port the host to the current Kairos Minecraft version without modifying core UI.
