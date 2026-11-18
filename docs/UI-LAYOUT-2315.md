# Anvil and host-menu layout fixes — 2.3.15

The anvil retains the original mcps4 `AnvilTextInput` artwork and placement.
Its item name now renders white and is centered horizontally and vertically
inside the input face. Editing still uses the existing keyboard adapter and
Minecraft's synchronized anvil rename/result transaction.

As requested, host buttons and sliders now use the **existing Settings menu
renderers and assets**: `LceWidgets.button` / `LceMenuButton.draw` and
`LceWidgets.slider`. This replaces the separate host button/slider rendering
introduced in 2.3.14. The original PS4 host panels, player icons, checkbox
artwork, option order and actions remain the basis of the host scenes.

Host, player and action-button text is centered using the Settings bitmap-font
metrics. Sliders use all the Settings layers, including the outline, clipped
highlight, handle and tint. Their labels are centered on the track. The adapter
applies Console UI Size to the widget and text together. Player names retain
space for the original icon without shifting their center away from the button.

Buttons and sliders share the Settings 20-unit height and 25-unit pitch;
checkbox rows use its 12-unit height, 15-unit pitch and mixed-row gaps. These
logical units are converted to the PS4 scene's three-times source coordinates.

`HostOptionsLayout` measures the mixed-height rows against the actual scene
viewport. Mouse scrolling clamps to the earliest start that fills the last
page. Controller selection reveals its row in either direction. Rebuilding
rows after a network snapshot retains the scroll offset and focus instead of
restarting the menu's entry animation. Slider clicking and dragging select an
absolute stop, with left/right navigation retaining discrete adjustments.
Server-side privilege checks and saved option handling remain as documented
in [the PS4 frontend reference](PS4-FRONTEND-2314.md).

The Java suite includes mixed-row spacing, viewport limits, every row's
reachability, final-page clamping and snapshot-layout stability. The production
client check uses Forge 37.1.1, Java 17, the supplied OptiFine H1 and LCE Look
2.0.0, exercising anvil renaming, slider dragging through the real network,
mouse-wheel and controller navigation, and all three Console UI Size stops.
`UI-LAYOUT-2315-RESULTS.json` records the final results and screenshot hashes.
Physical controllers and Apple display hardware were not available.

Main mod and optional Join Compatibility add-on versions are **2.3.15**.
Network protocol remains **7**; LCE Look remains **2.0.0**.
