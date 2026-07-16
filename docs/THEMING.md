# Theme packs

## Web ClickGUI

The production ClickGUI uses CSS variables. Create `.minecraft/kairos-ui/custom.css`;
the private resource scheme loads it after the bundled defaults. Accent, typeface, radii,
surfaces and text colors can therefore change without recompiling Java. The theme button
cycles violet, cyan and rose variants through LiquidBounce's persisted `Theme` value.

The properties system below remains the native HUD/ESP theme API.

Kairos keeps theme data separate from components. A theme controls typography IDs,
the complete surface/text/accent palette, window/component radius, spacing, glass blur,
and fast motion duration. `ThemeRegistry.kairosDefaults()` includes:

- `kairos-modern` — the default blue-black glass and violet accent;
- `obsidian-violet` — darker neutral surfaces and a brighter purple;
- `arctic-glass` — cool blue surfaces and a cyan accent.

Scenes constructed with a `ThemeRegistry` listen for changes and update without rebuilding
the module catalog, setting values, panel positions, or HUD data.

## In game

Open the menu with `.kairos gui`. The leading character is LiquidBounce's configurable
command prefix, so it changes automatically with the client's prefix command.

Custom files are loaded from `.minecraft/kairos-ui/themes/*.properties`. Copy
`examples/themes/kairos-modern.properties`, change its `id`, and edit ARGB colors in
eight-digit hexadecimal form. `selected-theme.txt` is managed automatically.

## Java API

```java
ThemeRegistry themes = ThemeRegistry.kairosDefaults();
ThemePack custom = new ThemePack(
    "customer-purple",
    "Customer Purple",
    ThemeTokens.kairosDark().withAccent(0xFFA970FF)
);
themes.register(custom);
themes.activate("customer-purple");

HudScene hud = new HudScene(hudModel, themes);
```

`ThemeCodec` reads and writes the dependency-free properties format. `ThemeDirectory`
handles a folder, reloads packs, and persists the active ID.

Font values are logical IDs. Preview and Minecraft backends resolve those IDs; a client
may supply Inter, JetBrains Mono, and CJK assets through its font-atlas implementation.

## Per-surface themes

ClickGUI, HUD, and ESP do not have to pretend they are the same surface. The default
combination is modern glass for menus and a hard-edged `competitive-pixel` profile for
combat information. This keeps the menu contemporary while making the in-game overlay
small, crisp, and readable over motion.

- `examples/themes/kairos-modern.properties` controls the ClickGUI and glass HUD.
- `examples/themes/combat-pixel-hud.properties` is loaded by `CombatHudProfileCodec`.
- `examples/themes/competitive-esp.properties` is loaded by `EspStyleCodec`.

`CombatHudScene` receives a `CombatHudProfile` and `GameVisualRenderer`; `EspOverlayScene`
receives an `EspStyle`. A client may expose those property keys in its config UI or hot
reload the files. The two codecs target Java 8 and do not depend on Minecraft.
