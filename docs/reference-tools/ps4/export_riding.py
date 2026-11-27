"""Extract the original PS4 horse scene and horse HUD display lists and bitmaps."""
import argparse
import json
import struct
from pathlib import Path
from read_fui import Fui
from export_workstations import rgba, sha
from export_containers import compose
from read_block_constructors import Source


def children(f, index):
    d = f.defs[index]
    frame = f.frames[d[1]]
    return f.geometry[frame['start']:frame['start'] + frame['count']]


def flatten(f, index, matrix):
    out = []
    for n in children(f, index):
        kind, child = n['header'][1] & 255, n['header'][3]
        transform = compose(matrix, n['matrix'])
        if kind == 4:
            out.append(dict(name=n['name'], symbol=f.imports[child], matrix=transform))
        elif kind == 2:
            out.extend(flatten(f, child, transform))
    return out


def export(scene_path, hud_folder, eboot, project):
    f = Fui(scene_path)
    hud = Fui(hud_folder / 'skinHDHud.fui')
    graphics = Fui(hud_folder / 'skinHDGraphicsHud.fui')
    root = next(g for g in f.geometry if g['name'] == 'MainPanel')
    scene = dict(sha256=sha(f.b), root=list(root['matrix'][4:]), controls={}, static=[], objects={})
    for n in children(f, root['header'][3]):
        kind, child = n['header'][1] & 255, n['header'][3]
        key = n['name']
        if key == 'BackgroundPanel':
            scene['static'].extend(flatten(f, child, n['matrix']))
        elif key:
            scene['controls'][key] = dict(matrix=list(n['matrix']),
                symbol=f.imports[child] if kind == 4 else 'local:' + str(child))
            if kind == 2:
                scene['objects'][key] = flatten(f, child, n['matrix'])
        elif kind == 4:
            # 0x809540 removes DonkeyInventoryList when unchested, leaving the authored recess.
            node = dict(name='', symbol=f.imports[child], matrix=list(n['matrix']))
            scene['static'].append(node)
        if key == 'iggy_horse':
            leaf = children(f, child)[0]
            nd, _, np, *_ = f.counts
            scene['player_bounds'] = list(struct.unpack_from('<4f', f.b,
                0x98 + nd*28 + np*132 + leaf['header'][3]*28 + 12))
    # Preserve each original equipment placeholder separately for its native visibility state.
    scene['empty_icons'] = {n['symbol']: n for n in scene['objects']['SlotTypes']
                            if n['symbol'] != 'IconHolder_1x1border'}
    data = dict(repository='Nejirus/mcps4', revision='37e258085773f11599cf0ffe29119926769554b7',
                scene=scene, hud_sha256=sha(hud.b), graphics_sha256=sha(graphics.b), sprites={}, hud={})
    source = Source(eboot)
    value = lambda pc: struct.unpack('<f', source.read(pc, 4))[0]
    data['portrait'] = dict(routine='0x9f6ba0', aspect=1920/1080,
        foot_divisor=value(0xee6524), scale=value(0xee652c), mouse_y=value(0xee6538))
    assets = project / 'src/main/resources/assets/legacyconsole'
    textures = assets / 'textures/ps4/riding'
    textures.mkdir(parents=True, exist_ok=True)
    for name in ['HorseJump_bar_empty', 'HorseJump_bar_full', 'Health_Background',
                 'Health_Background_Flash', 'HorseHealth_Full', 'HorseHealth_Half',
                 'HorseHealth_Full_Flash', 'HorseHealth_Half_Flash']:
        kind, index = graphics.exports[name]
        assert kind == 3
        original = graphics.images[index]
        converted = rgba(original['bytes'])
        filename = name.lower()
        (textures / (filename + '.png')).write_bytes(converted)
        data['sprites'][name] = dict(texture=filename, size=original['size'],
            source_sha256=sha(original['bytes']), sha256=sha(converted))
    for name in ['HorseJumpBar', 'FJ_HorseHealthBar']:
        node = next(g for g in hud.geometry if g['name'] == name)
        data['hud'][name] = dict(matrix=list(node['matrix']),
            children=flatten(hud, node['header'][3], [1, 0, 0, 1, 0, 0]))
        if name == 'FJ_HorseHealthBar':
            data['hud'][name]['slots'] = [dict(name=n['name'], matrix=list(n['matrix']))
                                        for n in children(hud, node['header'][3])]
    (assets / 'ps4_riding.json').write_text(json.dumps(data, indent=2) + '\n')
    print('Extracted HorseInventoryMenu1080 and', len(data['sprites']), 'original riding HUD sprites')


if __name__ == '__main__':
    p = argparse.ArgumentParser(description=__doc__)
    p.add_argument('--scene', type=Path, required=True)
    p.add_argument('--hud-dir', type=Path, required=True)
    p.add_argument('--eboot', type=Path, required=True)
    p.add_argument('--project-root', type=Path, required=True)
    a = p.parse_args()
    export(a.scene, a.hud_dir, a.eboot, a.project_root)
