"""Export the PS4 WitchModel's inherited villager_v2 helmet, without new geometry."""
import argparse
import json
from pathlib import Path
from read_models import read_models
from read_block_constructors import Source


def export(models, eboot, output):
    data = read_models(models)
    source = Source(eboot)
    # VillagerModel's name getter (0xd84340) is used by the WitchModel
    # constructor (0xd7eff0) before applying villager.witch's extra parts.
    assert source.read(0xf1c2e0, 22).decode('utf-16le') == 'villager_v2'
    assert source.read(0xf1c352, 28).decode('utf-16le') == 'villager.witch'
    part = next(p for p in data['models']['villager_v2']['parts'] if p['name'] == 'helmet')
    assert part['parent'] == 'head' and not any(part['transform'])
    result = dict(repository=data['repository'], revision=data['revision'], source=data['source'],
                  models_sha256=data['sha256'], constructor='0xd7eff0', base_name_getter='0xd84340',
                  base_model='villager_v2', variant='villager.witch',
                  texture_size=data['models']['villager.witch']['texture_size'], part=part)
    output.parent.mkdir(parents=True, exist_ok=True)
    output.write_text(json.dumps(result, indent=2) + '\n')


if __name__ == '__main__':
    parser = argparse.ArgumentParser()
    parser.add_argument('--models', type=Path, required=True)
    parser.add_argument('--eboot', type=Path, required=True)
    parser.add_argument('--output', type=Path, required=True)
    args = parser.parse_args()
    export(args.models, args.eboot, args.output)
