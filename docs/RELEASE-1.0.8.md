# Release 1.0.8

The Xbox 360 Dismount glyph was using its controller-diagram bitmap's full
64-pixel height, almost twice the 33-pixel face-button row. Its display now
fits that row at both glyph scales, preserving the original 67:64 aspect
ratio and the existing vertical centering.

The original LCE `ButtonRS.png` is unchanged. Its source is
[`Minecraft.Client/Common/Media/Graphics/X360ControllerIcons/ButtonRS.png`](https://github.com/chrisGrando/MinecraftLCE-7thGen/blob/acff48c2c7e18406fd6000b56945746ca9a6eb69/Minecraft.Client/Common/Media/Graphics/X360ControllerIcons/ButtonRS.png).
The row height comes from the same revision's Xbox 360 XUI A-button geometry.
The exporter reproduces the display sizing, and `LCE-XBOX360-2312.json`
records the source hashes and adaptation. Other glyphs are unchanged.

439 automated tests passed. The mounted HUD was checked at 720p, 1440p,
4K, 4:3, and 3024×1964 using Java 17, Forge 37.1.1, and the supplied
OptiFine H1 with shaders off. The runtime check simulated a DualShock 4
device name with Xbox 360 selected, confirming the selected glyph family
and the RS icon's height and alignment in each Linux framebuffer.

Use Release 1.0.8 of Join Compatibility alongside the main mod if your
server uses the add-on. LCE Look remains version 2.0.0.
