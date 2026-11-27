# Release 1.0.9

The Online Game checkbox is functional in both the create-world and
load-world menus. It defaults to Off and retains its selection when the
menu is resized or you return from More World Options.

With On selected, the world opens to LAN after the local player joins and
the launch settings have applied. Joining players use the effective game
mode, and commands follow Host Privileges. The normal LAN chat message
shows the chosen port or reports a failure. Leaving the world stops its
LAN server and advertisement through Minecraft's normal shutdown path.

The console checkbox retains the existing artwork. LAN hosting is a Java
platform adapter using Minecraft 1.17.1's `ShareToLanScreen` sequence:
`HttpUtil.getAvailablePort()`, `IntegratedServer.publishServer(...)`,
the `commands.publish.started` / `commands.publish.failed` chat messages,
and `Minecraft.updateTitle()`. It uses Java's local-network discovery.
Pending launch settings are cleared on logout and return to the title.

439 automated tests passed. Four actual world launches used Java 17,
Forge 37.1.1, and the supplied OptiFine H1 with shaders off: an existing
world offline, the same world online, an offline reload, and a new creative
world online. Both online sessions were discovered through LAN multicast
and answered Minecraft TCP status and ping/pong packets. The checks also
verified the selected game modes, Host Privileges off/on, checkbox state
after menu reinitialization, and LAN port closure after leaving the world.

Update the main mod and Join Compatibility to Release 1.0.9 together.
LCE Look remains version 2.0.0.
