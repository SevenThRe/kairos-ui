# Minecraft integration build

This independent Essential Gradle Toolkit build preprocesses one client source tree into:

- Forge 1.12.2 (Java 8, LWJGL2)
- Forge 1.20.1 (Java 17, GLFW/GuiGraphics)

Press `F8` in game to open Kairos. Press `F6` inside the screen to switch between the fixed workbench and draggable panel desktop.

The included `MinecraftFallbackCanvas` is the always-available compatibility path. It deliberately uses opaque/translucent native GUI primitives; production clients should replace it with the `ui-render-opengl` command pipeline to enable SDF corners, font atlases and the shared Kawase blur pass.

Build from this directory with Gradle 9.2 and both JDK 8 and JDK 17 available:

```bash
gradle :1.12.2-forge:build :1.20.1-forge:build
```
