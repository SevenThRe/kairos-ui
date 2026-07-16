# ESP framework

`ui-esp` is the cross-version visual layer. It does not import Minecraft. The host endpoint
provides two things per frame:

1. `EspEntitySource` returns interpolated `EspEntity` snapshots and world AABBs.
2. `WorldToScreenProjector` maps world coordinates into the scaled GUI viewport.

`MatrixProjector` already implements the common OpenGL column-major view-projection path.
`EspRenderer` projects all eight AABB corners and supports Full or Corner boxes, translucent
fill, health bars, friend colors, names, and distance labels.

```java
EspEntitySource entities = partialTicks -> collectEntities(partialTicks);
WorldToScreenProjector projector = new MatrixProjector(
    viewProjection, 0, 0, scaledWidth, scaledHeight
);
EspOverlayScene overlay = new EspOverlayScene(entities, projector, themes);
overlay.layout(new Rect(0, 0, scaledWidth, scaledHeight));
overlay.renderTree(canvas);
```

The entity collector is intentionally endpoint-owned because 1.12.2 and 1.20.1 expose
different world/entity/camera APIs. Filtering teams, friends, invisibles, players, mobs,
and items belongs there. Style and rendering do not fork by Minecraft version.
