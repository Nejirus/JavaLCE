# Release 1.0.3 — crafting menu copper cleanup

Waxed copper outputs are hidden from the console crafting-table and personal
crafting menus, in both Show All and Show Craftable modes. The filter uses
Minecraft 1.17.1's actual waxed-block mapping, covering every supported shape
and oxidation stage. This is the requested menu customization for Java's copper
blocks; no PS4 copper behavior is claimed.

In-world honeycomb waxing remains available. The recipes remain registered with
Minecraft, and the stonecutter retains its recipes for already-waxed copper.
Unwaxed copper and other crafting options keep their existing grouping.

Replace the main mod and optional Join Compatibility jar together with version
**1.0.3**. Network protocol stays **8**; LCE Look stays **2.0.0**.

Validation is recorded in `RELEASE-1.0.3-RESULTS.json`, including the existing
automated suite, a Forge **37.1.1** client with the supplied OptiFine **H1**,
the actual menu lists in both visibility modes, preserved stonecutter recipes,
and a server-side honeycomb interaction on a placed copper block.
