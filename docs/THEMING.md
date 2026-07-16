# Theme packs

Kairos keeps theme data separate from components. A theme controls typography IDs,
the complete surface/text/accent palette, window/component radius, spacing, glass blur,
and fast motion duration. `ThemeRegistry.kairosDefaults()` includes:

- `kairos-modern` — the default blue-black glass and violet accent;
- `obsidian-violet` — darker neutral surfaces and a brighter purple;
- `arctic-glass` — cool blue surfaces and a cyan accent.

Scenes constructed with a `ThemeRegistry` listen for changes and update without rebuilding
the module catalog, setting values, panel positions, or HUD data.

## In game

```text
.kairos themes
.kairos theme arctic-glass
.kairos themes reload
```

Any punctuation prefix works in the standalone endpoint. Integrations with a customer
prefix call `KairosMod.handleCommand(rawMessage, customerPrefix)`.

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

ModernWorkbench gui = new ModernWorkbench(catalog, state, themes);
HudScene hud = new HudScene(hudModel, themes);
```

`ThemeCodec` reads and writes the dependency-free properties format. `ThemeDirectory`
handles a folder, reloads packs, and persists the active ID.

Font values are logical IDs. Preview and Minecraft backends resolve those IDs; a client
may supply Inter, JetBrains Mono, and CJK assets through its font-atlas implementation.
