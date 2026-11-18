# Release 1.0.0

Release 1.0.0 is the renamed 2.3.18 build. The release name, main-menu version
label, Forge metadata, compatibility dependency and downloadable filenames
now use 1.0.0. Features, assets, world data and network protocol 8 retain the
validated 2.3.18 implementation.

This release includes the PS4 map-cursor drawing-layer fix and world-select
artwork for server-details player buttons. Host Options keeps its existing
style. Earlier improvements, including multiplayer player colors and saved
menu-hint progress, are included.

Install `legacy-console-1.17.1-1.0.0.jar` and, when using the optional server
compatibility add-on, `legacy-join-compat-1.17.1-1.0.0.jar`. Update the two jars
together. Use Minecraft 1.17.1, Java 17 and Forge 37.1.1. The original LCE Look
shaderpack has its own version, 2.0.0.

The source ZIP contains the full project and earlier source-evidence notes.
The sources JAR contains the main mod's Java source and resources.

## Verification

The rebuilt JAR entries are compared with the published 2.3.18 artifacts.
Class-file constant pools and text metadata are normalized only for the
release number and the main-menu's added Release label; all remaining
content must match. Forge versions and the compatibility dependency are
checked explicitly. Source archive checksums and live downloads are also
verified.

The original 2.3.18 validation remains in
`MAP-CURSOR-2318-RESULTS.json`: 421 passing tests, 64 captured PS4 layer cases,
256 cursor-visibility views and 31 client captures using Forge 37.1.1 and the
supplied OptiFine H1, with shaders OFF and LCE Look 2.0.0. These results are
retained as provenance rather than presented as a new gameplay test run for
the release-name change.
