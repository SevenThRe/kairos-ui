# ESP framework

`ui-esp` is the cross-version visual layer. It does not import Minecraft. The host endpoint
provides two things per frame:

1. `EspEntitySource` returns interpolated `EspEntity` snapshots and world AABBs.
2. `WorldToScreenProjector` maps world coordinates into the scaled GUI viewport.

`MatrixProjector` already implements the common OpenGL column-major view-projection path.
`EspRenderer` projects all eight AABB corners and supports Full or Corner boxes, translucent
fill, health bars, friend colors, names, distances, held-item tags, armor percentage, and
hard black outlines. `EspStyle.competitivePixel()` is the high-contrast default inspired by
the supplied gameplay reference, while `kairosModern()` remains available.

```java
EspEntitySource entities = partialTicks -> collectEntities(partialTicks);
WorldToScreenProjector projector = new MatrixProjector(
    viewProjection, 0, 0, scaledWidth, scaledHeight
);
EspOverlayScene overlay = new EspOverlayScene(entities, projector, themes);
overlay.layout(new Rect(0, 0, scaledWidth, scaledHeight));
overlay.renderTree(canvas);
```

Beds, chests, dropped items, projectiles, and custom objectives use `WorldObjectEsp`.
The same source drives two passes: `renderTree(canvas)` draws projected 2D boxes/tags and
`overlay.renderWorld(sink, throughWalls)` sends AABBs to the endpoint-owned
`WorldOverlaySink` during the native world render event. This makes depth testing and GL
state explicit instead of attempting 3D rendering from a 2D GUI callback.

The entity collector is intentionally endpoint-owned because 1.12.2 and 1.20.1 expose
different world/entity/camera APIs. Filtering teams, friends, invisibles, players, mobs,
and items belongs there. Style and rendering do not fork by Minecraft version.

## Target HUD visual bridge

`TargetSnapshot` carries health history, absorption, distance, hurt ticks, held item and
four armor visuals. `CombatHudScene` draws both the inspection card and compact bottom
TargetHUD. Its `GameVisualRenderer` is the only native hook:

- on 1.12.2, bind entity IDs to the captured `EntityLivingBase`, then use
  `GuiInventory.drawEntityOnScreen` and `RenderItem`;
- on 1.20.1, bind IDs to `LivingEntity`/`ItemStack`, then use the version's inventory
  entity helper and item renderer through `GuiGraphics`.

`SemanticGameVisualRenderer` produces `entity:*` and `item:*` commands for native/custom
backends. Headless previews use `NoopGameVisualRenderer`, so they reserve the correct slots
without drawing counterfeit player or item art. Keep the ID-to-native-object map
endpoint-local and clear it after each frame so shared code never retains Minecraft objects.
