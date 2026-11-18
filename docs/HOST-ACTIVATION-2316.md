# Host-option activation fix — 2.3.16

In 2.3.15, host-menu navigation updated `MenuFocus` but left the controller's
cached click coordinates at an earlier row or the previous page. Cross/A
confirmation then fell through to a simulated mouse click at those coordinates.
The highlight could move correctly while Set Day, Set Night, Set World Spawn
Point and checkboxes failed to activate the selected control.

The published 2.3.15 jar was tested through `ControllerMenus.tick`, opening the
host page and navigating to Set Day before confirming. World time remained at
the fixture's 1234 instead of changing to 6000. Earlier backend checks and the
2.3.15 slider/scroll checks did not exercise that confirmation sequence.

`ConsoleHostOptions` now handles Cross/A by activating its enabled, focused row
directly, including the existing confirmation sound and button animation.
Keyboard Enter uses the same callback. Rebuilt rows also synchronize controller
focus through `ControllerMenus.focus`. Sliders retain left/right adjustment and
mouse dragging. This corrects the Java input adapter; the PS4 option IDs, server
actions and privilege checks are unchanged.

The production-client regression matrix activates 28 controls using each input
path at each resolution: controller and mouse, 720p without shaders and 1440p
with LCE Look 2.0.0. The resulting 112 actions cover:

- Set Day, Set Night and world spawn, checked against actual server time/position.
- All eleven global rule checkboxes, including TNT and immediate respawn.
- All three self-ability checkboxes and the player spawn button.
- All ten guest permission/ability checkboxes.

Each checkbox is checked against server state and the acknowledged client row.
Navigation crosses page boundaries and reveals controls through scrolling.
The fixture sends synthetic button samples through the production controller
dispatcher rather than sending action packets directly. Physical gamepads were
not available. Forge 37.1.1, Java 17 and the supplied OptiFine H1 are used for
the client run. `HOST-ACTIVATION-2316-RESULTS.json` records the final results,
individual actions, screenshot hashes and the earlier failing reproduction.

Update main mod and optional Join Compatibility together to **2.3.16**.
Network protocol remains **7**; LCE Look remains **2.0.0**.
