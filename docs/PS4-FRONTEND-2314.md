# PS4 panorama, anvil/furnace and host options — 2.3.14

The implementation reference is **Nejirus/mcps4**, revision
`37e258085773f11599cf0ffe29119926769554b7`. The new screens use that build's
resources and decompiled routines. The LCE menus retained in earlier releases
are not the reference for these replacements.

## Panorama

`skinHDOrbis.fui` supplies `Panorama_Background_S` and
`Panorama_Background_N`: the original 1230×216 day/night JPEGs. Their bytes are
shipped unchanged, including the original blur. `skinHD.fui` supplies the two
4,100-frame timelines, each drawing two copies at 5× size, separated by 6,150
source pixels. Every exported horizontal position is retained; the last frame
ends at −6,150. The day layer is the default selected by `0x828eb0`.
The FUI time/frame conversion is 30 frames per second (the frame/time operations
at `0x817630` and `0x81a760`).

`Ps4Panorama` plays those positions and images instead of the former Java cube
map. Fitting/cropping the authored 1920×1080 viewport for other display shapes
is a Java rendering adapter. The panorama is independent of Console UI Size.
`ps4_panorama.json` records original asset hashes and all timeline positions.

## Anvil and furnace

| Scene | Original routines | Adapter |
| --- | --- | --- |
| Anvil | constructor `0xa036f0`, result/cost display `0xa0c970`, section navigation `0xa0dbf0`, input item/name update `0xa0de40` / `0xa0dfe0` | `Ps4AnvilScreen` |
| Furnace | constructor `0xa02ed0`, progress `0xa406b0`, section navigation `0xa40e10` | `Ps4FurnaceScreen` |

The original `AnvilMenu1080.fui` / `FurnaceMenu1080.fui` roots, labels, holders,
progress art, cross, hammer and text field replace the earlier LCE/Legacy4J
renderers. Furnace fuel and cooking use the synchronized menu values with the
native current/maximum calculation. Blast furnaces and smokers use the same
scene with their native Java names, recipe times and outputs.

The anvil retains native red/green cost styles, the 40-level Too Expensive
condition and invalid-result cross. The existing Java on-screen keyboard is
the platform-keyboard adapter, retaining the mod's 31-character input cap.
Minecraft handles authoritative repair/rename costs, recipes, experience,
result pickup and inventory synchronization. Input name synchronization preserves
named tools during repairs before any explicit rename. Both menus use Console UI Size,
including the original PS4 section routes. The native comparison now covers
3,024 transitions across the six workstation scene types.

## Host and player options

The screens use `InGameInfoMenu1080.fui`, `InGameHostOptions1080.fui`,
`InGamePlayerOptions1080.fui` and `InGameTeleportMenu1080.fui`. Their original
panel/list positions and PS4 checkbox, button, slider and typography assets
are retained. The original constructors at `0xa57a20` / `0xa57c30` and
`0xa5b4e0` / `0xa5d010` provide the option IDs, order and English labels.
Input/apply handling is referenced at `0xa58cd0` and `0xa5e260`. Original
player-icon frames are indexed by the Java player list. Weather status retains
`0x403fa0`'s visual rain/thunder thresholds; an edited slider retains its local
selection while the Java weather transition completes.

Select/Back/Share opens the player/host menu from gameplay or pause. Square no
longer opens host options from pause, and that pause-menu glyph is removed.

The translated option set includes fire spread, TNT explosions, daylight and
weather cycles, keeping inventory, mob spawning/griefing/loot, tile drops,
natural regeneration, immediate respawn, day/night, difficulty, game mode,
weather, world/player spawn points, both teleport directions, flight,
invisibility, exhaustion, per-player interaction/attack permissions,
moderators and kicking.

Forge's server, game rules, abilities, player list and synchronized packets
replace the console networking/platform APIs. Controls issue validated Java
requests as they are activated. Player settings and the additional TNT rule
are saved per world in `legacyconsole_host_options.dat`. The console platform's
system invitation UI is not provided by Forge; this implementation covers the
in-world player list and host/player actions.

Host privileges must be enabled in the world, and Hardcore/origin restrictions
must permit them. Every request is checked on the server. Personal ability grants do
not authorize global settings or editing another player's permissions/mode.
A teleport grant exposes the teleport controls without enabling global rules.
Moderator grants can only be changed by the owner. Disabled privileges reject direct packets
as well as UI opening. Player permissions also cover ranged attacks,
container entities and placement/use items through Forge events. Disabling
privileges removes the host flight/invisibility effects and exhaustion bypass.

## Difficulty correction

Ordinary Survival still retains its existing origin game-mode/privilege rules,
but difficulty is independent of that lock. Loading a non-Hardcore world clears
the old difficulty lock, and the world policy no longer restores its creation
difficulty every tick. The load-screen slider and selected launch difficulty
use that separation. Hardcore alone retains forced Hard difficulty and locking.

## Validation and compatibility

The Java suite covers asset hashes, the native routes, all three menu-size
stops across 720p/1080p/1440p/4K/4:3/16:10/Retina geometry profiles, panorama
images/timeline, saved world policy, and host authorization/persistence.
`NATIVE-2314-FRONTEND-RESULTS.json` records production-client results with
Forge 37.1.1, Java 17, the supplied OptiFine H1 and LCE Look 2.0.0.
Controller input is synthetic; physical controllers and Apple hardware are not
available in the test environment.

Update the main mod and optional Join Compatibility add-on together. Network
protocol is **7** for the added host-state/request messages. LCE Look stays at
2.0.0. Reproduce asset extraction with `export_frontend2314.py`; it requires
original archive entries named above, the shared graphics/label libraries and
an `english-full.json` exported from that build's language resource.
