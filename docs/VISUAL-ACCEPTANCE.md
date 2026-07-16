# Visual acceptance contract

Modern appearance is a milestone requirement, not a post-processing extra.

## Glass

- One shared scene capture per frame, not one framebuffer blur per component.
- Downsampled separable/Kawase blur behind registered glass regions.
- Dark translucent tint remains above the blur so text contrast is stable.
- One-pixel low-alpha borders separate overlapping glass surfaces.
- When framebuffer blur is unavailable, preserve tint, border, hierarchy and opacity;
  never replace the UI with opaque Minecraft-style boxes.

## Typography

- Inter for Latin UI copy, JetBrains Mono only for values that benefit from fixed width.
- Noto Sans CJK fallback for Chinese glyph coverage.
- Font IDs come from theme tokens; components cannot hard-code a renderer or font file.
- Baselines, glyph metrics and pixel alignment must match at every GUI scale.
- The first font renderer may use a raster atlas; MSDF is the parity target.

## Color and spacing

- Neutral blue-black surfaces; purple is an accent, not a full-screen gradient.
- Selected, hovered, pressed and disabled states must be distinguishable without RGB effects.
- Base spacing unit is 4 px; common radius is 6–12 px; motion is 150–220 ms ease-out.
- ClickGUI and HUD widgets use the same palette, typography, radius and motion tokens.

## ClickGUI layout

The production WebView uses a fixed responsive composition: category sidebar, dense
module list, and settings inspector. CSS custom properties are the ClickGUI design-token
contract. A browser initialization failure must remain an explicit error and must never
replace the UI with a native approximation.
