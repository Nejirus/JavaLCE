"""Read the original big-endian PS4 models.bin, preserving every model field."""
import argparse
import hashlib
import json
import struct
from pathlib import Path


def read_models(path):
    data = path.read_bytes()
    offset = 0

    def read(format):
        nonlocal offset
        result = struct.unpack_from('>' + format, data, offset)
        offset += struct.calcsize('>' + format)
        return result[0] if len(result) == 1 else list(result)

    def text():
        nonlocal offset
        length = read('H')
        result = data[offset:offset + length].decode('utf-8')
        offset += length
        return result

    version, count = read('I'), read('I')
    assert version == 2
    models = {}
    for _ in range(count):
        name = text()
        model = dict(texture_size=read('2I'), parts=[])
        parts = read('I')
        for _ in range(parts):
            part = dict(name=text(), parent=text(), transform=read('9f'), cubes=[])
            cubes = read('I')
            for _ in range(cubes):
                values = read('3f3i3f?')
                part['cubes'].append(dict(position=values[:3], size=values[3:6],
                                          uv=values[6:8], inflate=values[8], mirror=values[9]))
            model['parts'].append(part)
        models[name] = model
    assert offset == len(data), (offset, len(data))
    return dict(repository='Nejirus/mcps4', revision='37e258085773f11599cf0ffe29119926769554b7',
                source='Common/res/TitleUpdate/res/models.bin', sha256=hashlib.sha256(data).hexdigest(),
                version=version, models=models)


if __name__ == '__main__':
    parser = argparse.ArgumentParser()
    parser.add_argument('--input', type=Path, required=True)
    parser.add_argument('--output', type=Path, required=True)
    args = parser.parse_args()
    args.output.write_text(json.dumps(read_models(args.input), indent=2) + '\n')
