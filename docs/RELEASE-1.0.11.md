# Release 1.0.11

Online Game is now saved separately for each world in
`data/legacyconsole_online.dat`. Turning it on for one world leaves every
other world's setting unchanged. The load menu saves a toggle immediately,
including when backing out without loading. The choice survives leaving a
world, restarting Minecraft and moving or renaming the save folder.

A new create-world menu starts offline and keeps its own draft choice when
opening world options or resizing the screen. Once the world starts, its
selected choice is written to that world's save before the LAN publishing
step. Creating a different world starts with a separate choice.

Release 1.0.10's client-wide `world.onlineGame` preference is ignored. There
is no reliable way to identify which world that shared preference was meant
for, so existing worlds without an individual setting initially show Off.
Enable Online Game once on each world you want to host; the choice then
stays with that world. Existing PS4 item-popup content is retained.

The hotbar popup also keeps its authored font size when more enchantment
lines appear. Release 1.0.10's vertical fitting could shrink ordinary
five-line popups dramatically when the HUD left little space below the
crosshair. It now moves the text above the crosshair when necessary,
rather than reducing the font to fill that narrow gap. Extremely long
custom names still use the existing Java horizontal viewport safeguard.

The PS4 `FJ_Label` virtual SetText entry at `0x1300928 + 0x50` points to
`0x7dae40`, which restores the normal label properties and enters
`0x7d1170`. That text path restores the authored scale relative to its
parent; it does not choose a font size from the gap between the HUD and
crosshair. The 30-point selected-label asset remains the font authority.
Crosshair avoidance and exceptionally long Java names are renderer
adaptations, rather than claims about additional PS4 layout rules.

This corrects the Java save/launch adapter; it does not introduce a new PS4
UI implementation. The original PS4 checkbox assets and LAN hosting path
are unchanged.

Update the main mod and Join Compatibility to Release 1.0.11 together.
LCE Look remains version 2.0.0.

Validation: all 452 automated tests passed. The production jars ran with
Forge 37.1.1, Java 17 and the supplied OptiFine H1, with shaders off.
The client kept opposite choices for two existing worlds, hosted only the
online world, and saved a newly created world's separate choice. All three
choices survived a full client-process restart. Reversing the first two
worlds' choices then left the third unchanged. The obsolete global setting
was explicitly set to true during this check and did not affect defaults.

Twenty-one five-line popup captures covered all three HUD sizes at 720p,
1080p, 1440p, 4K, 4:3, 16:10 and 3024x1964. Every capture retained the
authored scale and cleared the crosshair; seven menu captures verify the
separate saved choices and new-world default. The 3024x1964 check covers
Retina-shaped framebuffer geometry on Linux, rather than native macOS.
The private QA harness is excluded from all downloads.
