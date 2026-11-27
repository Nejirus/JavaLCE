# Release 1.0.7

The Dismount prompt uses the existing LCE PlayStation R3 artwork at both
glyph sizes. Its height and vertical center match the face buttons in the
HUD. The PlayStation 4 selection applies even if the connected controller
reports an Xbox device name.

The PS4 witch model puts the top of the hood at −10.5 and the top of the brim
at −10.05 in head coordinates. That is about 0.45 model units of overlap.
The Java brim is at −10.03125 and the current hood matches the PS4 position.
The PS4 measurements come from the native part builder at `0xc8abb0` using
the existing `villager_v2` helmet and `villager.witch` hat data.
[PS4 reference revision](https://github.com/Nejirus/mcps4/tree/37e258085773f11599cf0ffe29119926769554b7)

438 automated tests passed. The mounted HUD rendered R3 at the correct size
and position in 720p, 1440p, 4K, 4:3, and 3024×1964 Linux framebuffers using
Java 17, Forge 37.1.1, and the supplied OptiFine H1 with shaders off. The
runtime check simulated an Xbox controller name with PlayStation 4 selected.

Use the Release 1.0.7 version of Join Compatibility alongside the main mod
if your server uses the add-on.
