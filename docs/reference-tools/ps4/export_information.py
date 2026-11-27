"""Read PS4 effect widgets, item-label animation and inventory effect anchors.

Only original display-list data is exported. Bitmaps are already reproduced by
export_workstations.py, with their source and converted pixel hashes.
"""
import argparse
import hashlib
import json
import struct
from pathlib import Path
from read_fui import Fui
from read_block_constructors import Source


def export(widgets, graphics, inventory, hud, eboot):
    w, g, inv, h = map(Fui, (widgets, graphics, inventory, hud))
    source = Source(eboot)
    def children(f, index):
        frame = f.frames[f.defs[index][1]]
        return f.geometry[frame['start']:frame['start'] + frame['count']]
    def nodes(symbol):
        return {n['name']: list(n['matrix']) for n in children(w, w.exports[symbol][1])}
    def label(symbol):
        n = children(g, g.exports[symbol][1])[0]
        return dict(g.textfields[n['header'][3]], matrix=n['matrix'])
    # Timeline changes contain removals as well as placements. Only opcode 5
    # places the new icon; reading every import would duplicate the old icon.
    icons = []
    d = w.defs[w.exports['skinHDInGame_fla.MobEffectIcon_40'][1]]
    for frame in w.frames[d[1]:d[1] + d[2]]:
        placed = [w.imports[n['header'][3]] for n in w.geometry[frame['start']:frame['start'] + frame['count']]
                  if n['header'][0] & 7 == 5 and n['header'][1] & 255 == 4]
        icons.append(placed[-1] if placed else '')
    mapping = struct.unpack('<30i', source.read(0xed7a00, 30 * 4))
    effect_icons = {str(i): icons[frame + 1] for i, frame in enumerate(mapping)
                    if 0 <= frame + 1 < len(icons) and icons[frame + 1]}
    d = h.defs[next(n for n in h.geometry if n['name'] == 'SelectedLabel')['header'][3]]
    fade = []
    for frame in h.frames[d[1] + 1:d[1] + d[2]]:
        n = h.geometry[frame['start']]
        fade.append(struct.unpack_from('<f', h.b, h.geometryStart + n['index'] * 72 + 64)[0])
    group = next(n for n in inv.geometry if n['name'] == 'EffectsGroup')
    root = next(n for n in inv.geometry if n['name'] == 'MainPanel')
    anchors = {n['name']: n['matrix'] for n in children(inv, group['header'][3])}
    return dict(repository='Nejirus/mcps4', revision='37e258085773f11599cf0ffe29119926769554b7',
                files={p.name: hashlib.sha256(p.read_bytes()).hexdigest()
                       for p in (widgets, graphics, inventory, hud)},
                effect_icons=effect_icons, effect_order={str(i):v for i,v in enumerate(mapping) if v>=0}, icon_frames=icons,
                normal=nodes('fourj.MobEffect.FJ_MobEffect_Normal'),
                small=nodes('fourj.MobEffect.FJ_MobEffect_Small'),
                tile=nodes('fourj.MobEffect.FJ_MobEffect_Tile_Normal'),
                effect_labels={n: label(n) for n in ('FJ_Label_MobEffectName', 'FJ_Label_MobEffectTime',
                                                    'FJ_Label_MobEffectNameSmall', 'FJ_Label_MobEffectTimeSmall')},
                inventory_offset=[group['matrix'][4] - root['matrix'][4],
                                  group['matrix'][5] - root['matrix'][5]],
                inventory_anchors=anchors, selected_fade=fade,
                cursor=nodes('fourj.Cursor.FJ_Cursor_Normal'),
                cursor_small=nodes('fourj.Cursor.FJ_Cursor_Small'),
                routines=dict(hotbar='0xc78a90', selected_label='0xa4e3f0', position='0x813030',
                              effect_tiles='0xa4f690 / 0x8191c0', effect_bars='0xa65460 / 0x81dfa0'))


if __name__ == '__main__':
    p = argparse.ArgumentParser()
    for name in ('widgets', 'graphics', 'inventory', 'hud', 'eboot', 'output'):
        p.add_argument('--' + name, type=Path, required=True)
    a = p.parse_args()
    result = export(a.widgets, a.graphics, a.inventory, a.hud, a.eboot)
    a.output.write_text(json.dumps(result, indent=2) + '\n')
