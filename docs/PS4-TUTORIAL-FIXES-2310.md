# Tutorial progression, bounds and world options — 2.3.10

Reference: [Nejirus/mcps4](https://github.com/Nejirus/mcps4), revision
`37e258085773f11599cf0ffe29119926769554b7`, eboot SHA-256
`c6089834fe1372854eed8f9acf2cb307f7009aaeb32ee303ce5bf6a1160786c8e3`.

## Furnace lesson completion

The recovered gameplay graph opens a furnace at task 33 (`0x965e63`), sets
progress flag 8 at task 34 (`0x965f9d`), and waits for furnace-menu state 4
at task 35 (`0x965fff`). The guided furnace sequence ends with a glass
CraftTask (`0x96b39e`), rather than an InfoTask with a confirm button. Charcoal
and glass are also pre-completable gameplay tasks 36 and 37.

The Java adapter previously reported menu completion only from its confirmation
handlers. After glass, the automatic-task loop exhausted the menu without
reporting state 4. Gameplay stayed on task 35, whose empty description caused
the renderer to repeat task 33's place/open-furnace instruction.

The same completion reporter now runs after the automatic-task loop. It records
the completed menu once and sends the existing state-completion packet. The
authoritative task graph, flags, crafting history and native task order are
preserved. Finishing glass can therefore advance through the already-completed
charcoal/glass gameplay tasks to the door lesson.

The final glass task has the original pre-completion flag enabled. If the
player closes the furnace immediately after collecting glass, the adapter
retains that activated final task until the server's crafting-history update
arrives. It then reports the same menu completion. Unread confirmation tasks
are not completed by this handling.

## Starting-area height and edge correction

The original tutorial rule's `tutorialArea` bounds are
`(-36, 0, 62) .. (20, 75, 113)`. Those numbers alone do not specify which
movement axes PS4 restricts.

In FullTutorial, `0x9669ba` writes `0x0101` to AreaConstraint `+0x20` and
`0x9669c1` writes zero to `+0x22`: contains=true, horizontal=true,
vertical=false. The original constraint's `0x961d00` satisfaction check and
`0x961df0` movement classifier consult these separate horizontal/vertical
flags. The mod previously enforced all three dimensions and rejected height
above Y=75.

`Ps4TutorialBounds` translates the horizontal correction from `0x961df0`.
Crossing a horizontal edge clamps the affected axis to the original bound.
Vertical motion and motion on the other axis continue. A player outside the
area can move toward it, and the boundary itself is reachable. The original
area metadata is retained; no replacement ceiling height is invented.

`docs/reference-tools/ps4/native_tutorial_bounds2310.py` executes the original
movement classifier/correction body with the tutorial flags and bounds. Only
the Vec3 pool allocator is replaced by a host callback. Its 141 checked-in
cases cover unrestricted heights, both horizontal axes, corners, exact edges,
movement toward the area and movement away from it. The Java regression compares
all corrected vectors against those original-code outputs.

## Saved tutorial loading

The bundled tutorial uses the registered Legacy generator, which Minecraft
1.17.1 treats as experimental when loading a save. Fresh tutorial installation
already continued past that generated-world dialog; the saved-world route did
not use the same handling.

Both routes now use one loader. It recognizes
`data/legacyconsole_ps4_tutorial.dat` with `data.PS4Tutorial=true`, including
renamed tutorial folders, and continues the experimental-generator dialog for
that marked save. Folder names alone do not identify tutorials. The existing
world-version compatibility handling remains in the saved-world UI.

## World Options

When Legacy is selected, its type button reads `Legacy` and the Superflat
control is absent from the button list. Switching to another world type restores
its Superflat control. A stale Superflat action cannot switch a Legacy world
to Flat. Seed, biome scale and world size settings retain their existing values.

## Validation

The full Java 17 / Forge 37.1.1 suite passes 384 tests. Final production-client
results and artifact input hashes are recorded in
`NATIVE-2310-TUTORIAL-RESULTS.json`.

The production client with the supplied OptiFine H1 and LCE Look 2.0.0 passes
four integration checks and records 28 captures. World Options covers eight
framebuffer sizes with shaders off and LCE Look. The focused furnace regression
starts at the original furnace prerequisite, then uses actual charcoal/glass
smelting and output transactions. It closes the furnace immediately after
glass is collected and verifies progression to the door task.

Movement checks exercise positions above Y=75, horizontal edge clamping and
vertical motion along the edge while the tutorial remains restricted. The
same save then resumes through Load Game without the experimental warning,
retaining its completed furnace history. These interaction captures cover
720p/1440p with both shader settings. Physical controllers and Apple Retina
hardware were not exercised.
