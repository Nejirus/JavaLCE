"""Extract PS4's four original Wither heart bitmaps and health frame labels."""
import argparse
import hashlib
import json
from pathlib import Path
from read_fui import Fui
from export_workstations import rgba


def export(folder, project):
    hud = Fui(folder / 'skinHDHud.fui')
    graphics = Fui(folder / 'skinHDGraphicsHud.fui')
    assets = project / 'src/main/resources/assets/legacyconsole'
    textures = assets / 'textures/lce/hud'
    textures.mkdir(parents=True, exist_ok=True)
    sha = lambda b: hashlib.sha256(b).hexdigest()
    data = dict(repository='Nejirus/mcps4',
                revision='37e258085773f11599cf0ffe29119926769554b7',
                hud_sha256=sha(hud.b), graphics_sha256=sha(graphics.b),
                conversion='Original BGRA to RGBA; no other pixel changes', sprites={})
    for name in ['Health_Full_Wither', 'Health_Half_Wither',
                 'Health_Full_Wither_Flash', 'Health_Half_Wither_Flash']:
        kind, index = graphics.exports[name]
        assert kind == 3
        image = graphics.images[index]
        converted = rgba(image['bytes'])
        filename = name.lower() + '.png'
        (textures / filename).write_bytes(converted)
        data['sprites'][name] = dict(file='textures/lce/hud/' + filename,
                                    size=image['size'], source_sha256=sha(image['bytes']),
                                    sha256=sha(converted))
    kind, index = hud.exports['skinHDHud_fla.FJ_Health_21']
    assert kind == 2
    definition = hud.defs[index]
    data['health_frames'] = [f['label'] for f in
                             hud.frames[definition[1]:definition[1] + definition[2]]]
    (assets / 'ps4_status_hud.json').write_text(json.dumps(data, indent=2) + '\n')
    return data


if __name__ == '__main__':
    p = argparse.ArgumentParser()
    p.add_argument('--input-dir', type=Path, required=True)
    p.add_argument('--project-root', type=Path, required=True)
    a = p.parse_args()
    result = export(a.input_dir, a.project_root)
    print('Exported', len(result['sprites']), 'original Wither heart sprites')
