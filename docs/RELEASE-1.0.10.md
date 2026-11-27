# Release 1.0.10

Online Game saves its On/Off choice immediately in the client profile as
`world.onlineGame`. Both create-world and load-world menus use that saved
choice, including after leaving a world or restarting Minecraft. LAN
hosting continues to use the world's selected mode and Host Privileges.

The hotbar item popup follows the original PS4 tooltip rules. PS4 does
show attack damage: a normal diamond sword shows `+7.0 Attack Damage`.
The Java equipment-slot headings and attack-speed line are omitted. The
PS4 attribute filter also excludes armor toughness and luck. Names,
enchantments, lore and supported attributes retain their ordering. Five
or more entries become four entries followed by `...` at the native threshold.
Inventory hover panels keep their separate behavior.

PS4's `HUD1080.fui` places `SelectedLabel` outside its HUD size frames.
It uses `FJ_Label_White_Centered` from `skinHDGraphicsLabels.fui`, with
the original 30-point Mojangles7 label. The popup now uses that authored
size without multiplying it by Small/Normal/Large HUD Size. The existing
viewport fitting still keeps long or tall information away from the
crosshair at supported resolutions and aspect ratios. Minecraft supplies
the live 1.17.1 item values, translations and compatible font metrics.

The mcps4 authority remains revision
[`37e258085773f11599cf0ffe29119926769554b7`](https://github.com/Nejirus/mcps4/tree/37e258085773f11599cf0ffe29119926769554b7).
The original tooltip builder is `0x3bb630`; its filter accepts attribute
IDs below seven and ID eight. The localization CRC table at `0xe72a90`
identifies their names. Number formatting comes from `0x3cda0` and the
original strings ` %ls%.1f %ls` and ` %ls%d%ls %ls`. Selected-label layout
and setting use `0x813030` / `0xa4e3f0`; the line limit is in `0xc78a90`.
`export_selected_information.py` reproduces the typography and source
format records, including input hashes, in the checked native fixture.

Update the main mod and Join Compatibility to Release 1.0.10 together.
LCE Look remains version 2.0.0.

Validation: all 448 automated tests passed. The production jars ran with
Forge 37.1.1, Java 17 and the supplied OptiFine H1, with shaders off.
Online Game remained enabled after loading and leaving a LAN world and
after a separate client-process restart; disabling it also saved correctly.
Twenty-one HUD captures covered all three HUD sizes at 720p, 1080p, 1440p,
4K, 4:3, 16:10 and 3024x1964. The latter checks Retina-shaped framebuffer
geometry on Linux, rather than a native macOS display. Four additional
menu captures verify the saved On/Off state. The private QA harness is
excluded from the downloadable jars and source archive.
