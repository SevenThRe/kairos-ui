# Roadmap

## M0 — repository skeleton (current)

- Java 8 API boundary
- retained node tree and basic layouts
- deterministic animation
- pointer capture and focus
- version-independent module/settings model
- fixed three-column Kairos workbench scene
- command-recording renderer and smoke tests

## M1 — real 1.12.2 host

- Forge screen lifecycle adapter
- LWJGL2 mouse/keyboard conversion
- GL state guard and scissor stack
- shape/text batches
- module/value adapter for the actual Kairos codebase

## M2 — visual parity

- font atlas with CJK fallback
- GLSL 1.20 SDF shapes
- nine-slice/SDF shadows
- shared framebuffer blur with fallback
- icons, search, profiles and persisted layout
- HUD widget primitives

## M3 — modern host

- current Kairos platform host
- GLFW input conversion
- modern render-state integration
- screenshot parity and interaction tests across both endpoint versions

## M4 — expansion

- 1.16.5 and 1.18.2 platform islands
- Forge/Fabric split where required
- optional compact and legacy panel scene compositions
- theme packages and accessibility controls
