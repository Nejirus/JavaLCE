# Legacy Console 2.3.17

Server-details player buttons now fit inside the original recessed frame.
Host-menu navigation wraps in both directions. Multiplayer shares PS4 player
color assignments between clients, name-tag borders and host-menu icons.
Regular menu guides retain partial progress and completion after reload.

Use Java 17, Minecraft 1.17.1 and Forge 37.1.1. Update the main mod and optional
Join Compatibility add-on together to 2.3.17; the network protocol is 8.
LCE Look remains the supplied 2.0.0 shaderpack.

## PS4 source and translation

The authoritative reference is Nejirus/mcps4 at commit
`37e258085773f11599cf0ffe29119926769554b7`, using its original `eboot.bin`
and the unwrapped ELF. The repository contains executable code rather than
the C++ source for these routines. The Java implementation translates the
recovered instruction behavior; it does not substitute Legacy4J color rules.

| PS4 executable location | Recovered behavior | Java implementation |
| --- | --- | --- |
| `0xcc3a78..0xcc3b85` | Mark sixteen occupied player slots, assign the first unused index, use zero when all are occupied, store it at player offset `0xab4` | `Ps4PlayerColors.firstFree`, `Slots.join` |
| `0xcc9a40` | Copy the old player's slot during respawn | UUID slot retained by `Ps4PlayerColors.Events.respawn` |
| `0xb8b3e0`, `0xb90c48` | Receive local/remote slot indices from player packets | `PlayerColorState`, `ClientPlayerColors` |
| `0xcd2c10`, `0xcd1a00`, palette `0xf0e8a0` | Player renderer reads the assigned slot and obtains its original label-border color | `Ps4PlayerColors.nameTagColor` |
| `0xbd7b70`, `0xc098b0` | Forward the player color and draw the rectangular name-label border | `Ps4NameTagLayout`, `Ps4NameTagBorder`, `PlayerNameTagBorderMixin` |
| `0xc0a18d..0xc0a214` | Five-point closed border at `(-width/2-1, baseline-1)` through `(width/2+1, baseline+9)`, Z zero | Four connected Java line segments at the same bounds |

The source uses a one-pixel line and a 0.025 world-space label scale. Its table
has eight defined RGB colors: black, green, red, blue, magenta, orange, yellow
and cyan. The remaining eight raw entries are zero; their RGB is black when
the draw call supplies opacity. This port preserves those entries and the
all-occupied fallback rather than creating an additional palette.

The slot allocator is session-local, as in the source. A player's UUID keeps
its slot during respawn and dimension changes. Leaving frees only that slot.
New players can reuse holes without renumbering others. A fresh server session
assigns slots again. Host privileges do not control color assignment.

Minecraft's existing name text, visibility rules, scoreboard behavior and
background remain the Forge integration points. The translated PS4 rectangle
is added to the visible player-name render pass, rather than replacing the
font with an invented outline. OptiFine H1 recognizes only the two built-in
line types by identity, so the custom one-pixel type invokes its same balanced
`Shaders.beginLines` / `endLines` program hooks when a shaderpack is active.
The geometry uses native one-pixel lines (`DEBUG_LINES` in Minecraft), not
Minecraft's shader-expanded line triangles. This preserves the border with
LCE Look enabled without changing the source width or geometry. The original palette and geometry are used;
the rest of the Java name-tag renderer is not claimed as a complete PS4 port.

The server sends a complete UUID-to-slot snapshot to modded clients on login
and logout and to a respawning modded player. Disconnect clears client state.
Optional Join Compatibility continues to admit unmodded clients; packets are
sent only where the mod channel is present. Unmodded clients retain their own
normal renderer.

## Layout and input

`panel_recess.png.mcmeta` specifies the existing eight-unit nine-slice frame.
Player rows now use its interior with a one-unit inset. Row height and pitch
match Settings. The visible row count and final scroll offset are computed
from that space. Narrow GUI profiles also reduce the two-column panel widths
so the details panel fits the viewport.

Host-menu wheel and controller navigation use the same enabled-row cycle.
They wrap first-to-last and last-to-first, skip disabled rows, include the
player-list header, and scroll the focused control into view. The previous
focused-confirmation fix and Settings widget artwork remain in use.

## Hint history

Generic and block hint IDs already persisted in the client installation's
`config/legacyconsole-hints.txt`. Normal menu guides previously kept their
progress only until disconnect. They now store `menu-progress:<context>:<step>`
and `menu:<context>` completion records in that same profile. Existing generic
and block IDs are preserved. This starts retaining regular-menu progress from
this build onward; earlier builds did not save that information.

An unfinished normal guide resumes at its saved step. Completing or declining
a guide suppresses its automatic return, including after world reload or
client restart. This profile applies across ordinary worlds on the same
installation. Tutorial-world tasks continue to use their existing world/player
server state and are not suppressed by a normal-world completion marker.
The Hints setting and manually requested What's This? descriptions keep their
existing behavior.

## Verification

The native reference exporter
`docs/reference-tools/ps4/player_colors.py` executes the bounded PS4 routines
with Unicorn. It reproduces the checked-in golden vectors byte for byte:
1,029 allocation cases, 25 palette cases and 24 border vertex streams, for
1,078 comparisons. Run it with the pinned unwrapped executable and original
executable path; neither executable is included in the release source archive.

The full Java suite passes 420 tests with no failures, errors or skips,
including slot lifecycle, packet/client state, frame containment, enabled-row
wrapping and hint-profile file round trips. The release result JSON records
the actual client checks and capture hashes. Client QA uses Forge 37.1.1,
the supplied OptiFine 1.17.1 HD U H1 and LCE Look 2.0.0. Resolution checks
cover 720p, 1080p, 1440p, 4K, 4:3, 16:10 and a Retina-shaped viewport.
The Retina check is viewport geometry, not an actual Apple display test.
Controller checks drive the production dispatcher; physical hardware is not
part of this automated run.
