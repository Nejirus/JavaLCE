# JavaLCE
## About
A mod for Minecraft: Java Edition that mimic the look, feel, and functionality of the legacy console editions of Minecraft.
**It includes:**
1. Controller support, with xbox and playstation glyphs
2. Legacy World type based on the sizes and generation of the legacy console versions
3. An overhauled UI that mimics the look of the legacy console versions
4. The last tutorial world
5. Console style maps with coordinates
## Requirements
- Minecraft: Java Edition 1.17.1
- Java 17
- Forge 37.1.1
- Optifine H1 (For LCE Look)
## Optional join compatibility

Add `legacy-join-compat-1.17.1-2.3.3.jar` alongside the main mod **on the server** to admit clients without Legacy Console. They use normal Java menus. Matching Legacy Console clients keep console crafting.

The add-on makes only the Legacy Console channel optional and omits its unused crafting-menu registry entry for Forge peers lacking the mod. Other mods' requirements and registry synchronization remain active. It does not supply another mod's missing content.
## Building
- Java 17
- Gradle 7.3.3
```
gradle build
```

The compiler emits Java 16 bytecode. `build/libs` contains the reobfuscated mod and join add-on. `gradle test` runs the included regression tests. See TESTING.md and PORT-1171.md for verification and adaptations; licenses and source notices are retained.
