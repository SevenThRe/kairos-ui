# Minecraft integration build

This independent Essential Gradle Toolkit build preprocesses one client source tree into:

- Forge 1.12.2 (Java 8, LWJGL2)
- Forge 1.20.1 (Java 17, GLFW/GuiGraphics)

The standard `engine` subproject compiles all Minecraft-independent code once as Java 8;
each endpoint depends on and embeds that output, so the preprocessor only touches the
small loader/screen bridge.

Press `Right Ctrl` in game to open or close Kairos. Press `F6` inside the screen to
switch between the fixed workbench and draggable panel desktop. The standalone bridge
also recognizes `<punctuation-prefix>kairos gui`, for example `.kairos gui`,
`!kairos gui`, and `/kairos gui`. A consuming client may call `KairosMod.openGui()`
from its own command manager, or call `handleGuiCommand(message, prefix)` for exact
prefix parsing. `setOpenKeyCode(...)` connects the activation key to the client's own
keybind/config service, while Right Ctrl remains the default.

The included `MinecraftFallbackCanvas` is the always-available compatibility path. It deliberately uses opaque/translucent native GUI primitives; production clients should replace it with the `ui-render-opengl` command pipeline to enable SDF corners, font atlases and the shared Kawase blur pass.

Build from this directory with Gradle 9.2 running on JDK 21 and both JDK 8 and JDK 17
available as compile toolchains:

```bash
gradle :1.12.2-forge:build :1.20.1-forge:build
```
