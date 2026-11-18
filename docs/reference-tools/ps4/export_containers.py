"""Extract mcps4 inventory and chest display lists without generating artwork.

Extends ps4_workstations.json, produced by export_workstations/export_frontend.
Input files must be the pinned MediaOrbis.arc resources recorded by their hashes.
"""
import argparse
import json
import struct
from pathlib import Path
from read_fui import Fui
from export_workstations import rgba, sha


def compose(a, b):
    return [a[0]*b[0]+a[2]*b[1], a[1]*b[0]+a[3]*b[1],
            a[0]*b[2]+a[2]*b[3], a[1]*b[2]+a[3]*b[3],
            a[0]*b[4]+a[2]*b[5]+a[4], a[1]*b[4]+a[3]*b[5]+a[5]]


def export(folder, project):
    assets = project / 'src/main/resources/assets/legacyconsole'
    path = assets / 'ps4_workstations.json'
    data = json.loads(path.read_text())
    graphics = Fui(folder / 'skinHDGraphicsInGame.fui')
    assert sha(graphics.b) == data['graphics_sha256']
    textures = assets / 'textures/ps4/workstations'
    for name in ['FJ_PanelRecess']:
        kind, index = graphics.exports[name]
        assert kind == 2
        d = graphics.defs[index]
        frame = graphics.frames[d[1]]
        node = graphics.geometry[frame['start']]
        nd, _, np, nsh, nb, *_ = graphics.counts
        shapes = 0x98 + nd*28 + np*132
        batches = shapes + nsh*28
        vertices = batches + nb*44
        _, first, count = struct.unpack_from('<3I', graphics.b, shapes+node['header'][3]*28)
        assert count == 9
        pieces = []
        for i in range(count):
            offset = batches+(first+i)*44
            _, _, bitmap = struct.unpack_from('<3I', graphics.b, offset)
            matrix = struct.unpack_from('<6f', graphics.b, offset+12)
            first_vertex, vertex_count = struct.unpack_from('<2I', graphics.b, offset+36)
            points = [struct.unpack_from('<2f', graphics.b, vertices+(first_vertex+j)*8)
                      for j in range(vertex_count)]
            xx, yy = zip(*points)
            original = graphics.images[bitmap]['bytes']
            converted = rgba(original)
            filename = name.lower()+'_'+str(i)
            (textures/(filename+'.png')).write_bytes(converted)
            pieces.append(dict(texture=filename, rect=[min(xx), min(yy), max(xx)-min(xx), max(yy)-min(yy)],
                               matrix=matrix, source_sha256=sha(original), sha256=sha(converted)))
        grid = [struct.unpack('<f', struct.pack('<I', v))[0] for v in d[5:9]]
        data['panels'][name] = dict(grid=grid, pieces=pieces)

    for name in ['ChestMenu', 'ChestLargeMenu', 'InventoryMenu']:
        f = Fui(folder/(name+'1080.fui'))
        def children(index):
            d = f.defs[index]
            frame = f.frames[d[1]]
            return f.geometry[frame['start']:frame['start']+frame['count']]
        def flatten(index, matrix):
            out = []
            for node in children(index):
                kind, child = node['header'][1]&255, node['header'][3]
                transform = compose(matrix, node['matrix'])
                if kind == 4:
                    out.append(dict(name=node['name'], symbol=f.imports[child], matrix=transform))
                elif kind == 2:
                    out.extend(flatten(child, transform))
            return out
        root = next(g for g in f.geometry if g['name']=='MainPanel')
        scene = dict(sha256=sha(f.b), root=list(root['matrix'][4:]), controls={}, static=[], objects={})
        for node in children(root['header'][3]):
            kind, child = node['header'][1]&255, node['header'][3]
            key = node['name']
            if key == 'BackgroundPanel':
                scene['static'].extend(flatten(child, node['matrix']))
            elif key:
                scene['controls'][key] = dict(matrix=list(node['matrix']),
                    symbol=f.imports[child] if kind==4 else 'local:'+str(child))
                if kind == 2:
                    scene['objects'][key] = flatten(child, node['matrix'])
            if key == 'iggy_player':
                leaf = children(child)[0]
                nd, _, np, *_ = f.counts
                bounds = list(struct.unpack_from('<4f', f.b, 0x98+nd*28+np*132+leaf['header'][3]*28+12))
                scene['player_bounds'] = bounds
        if name == 'InventoryMenu':
            scene['empty_icons'] = {}
            for key in ['EquipmentHolder', 'ShieldSlot']:
                for node in scene['objects'][key]:
                    if node['symbol'].startswith('Armour_Slot_'):
                        scene['empty_icons'][node['symbol']] = node
        data['scenes'][name] = scene
    path.write_text(json.dumps(data, indent=2)+'\n')
    print('Extracted ChestMenu, ChestLargeMenu, InventoryMenu and original recessed panel')


if __name__ == '__main__':
    p = argparse.ArgumentParser(description=__doc__)
    p.add_argument('--input-dir', type=Path, required=True)
    p.add_argument('--project-root', type=Path, required=True)
    a = p.parse_args()
    export(a.input_dir, a.project_root)
