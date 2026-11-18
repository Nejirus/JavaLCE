"""Capture PS4 slot rectangles with the original x86-64 geometry instructions.

Only scene access, allocation, strings and UI registration are callbacks. The
rounding routine and rectangle arithmetic run unmodified on the host CPU.
Requires the pinned mcps4 eboot, its extracted skinHDInGame.fui, ctypes/Pillow.
"""
import argparse
import ctypes as C
import hashlib
import json
import struct
from pathlib import Path
from native_module import Module, P, I, F
from read_fui import Fui


def capture(eboot, widgets):
    m = Module(eboot)
    buffers = []

    def alloc(size):
        b = C.create_string_buffer(size)
        buffers.append(b)
        return C.addressof(b)

    def patch(addr, ret, args, fn):
        m.patch(addr, C.CFUNCTYPE(ret, *args)(fn))

    guard, scene, dummy = alloc(8), alloc(0x80), alloc(0x100)
    C.memmove(guard, struct.pack('<Q', 0x12345678), 8)
    m.w(0x134bae8, 'Q', guard)
    C.memmove(scene + 0x60, struct.pack('<Q', dummy), 8)
    width = height = scale = 0.
    observed = []
    patch(0x7d18d0, F, [P], lambda _: scale)
    patch(0x7d0b10, F, [P], lambda _: width)
    patch(0x7d18a0, F, [P], lambda _: height)
    patch(0x676a0, I, [P, P, P], lambda *_: 0)
    patch(0x7cff60, P, [P, P], lambda *_: dummy)
    patch(0x7cffa0, P, [P], lambda _: dummy)
    patch(0xe6d210, I, [P, P, I], lambda *_: 0)
    patch(0xe6cfd0, P, [P], alloc)
    patch(0x7cfdc0, P, [P], lambda _: dummy)
    patch(0x86d3f0, None, [P, P, P, P],
          lambda _a, _b, rect, _c: observed.append(list(struct.unpack('<4f', C.string_at(rect, 16)))))
    patch(0x7d0710, None, [P, P, I], lambda *_: None)
    patch(0x7d0cc0, None, [P, P], lambda *_: None)
    patch(0x7d5b20, P, [P], lambda _: dummy)
    patch(0x7d1140, I, [P], lambda _: 0)
    patch(0x7d9c70, None, [P, P, I], lambda *_: None)
    routine = m.function(0x83b9b0, None, [P, I])
    cases = []
    for width, height in [(20., 20.), (31.5, 31.5), (42., 42.), (48., 48.),
                          (63., 63.), (70., 70.), (81., 81.), (82., 82.),
                          (88., 88.), (96., 96.), (108., 108.), (63., 62.98)]:
        scale = width / 20
        routine(scene, 0)
        cases.append(dict(width=width, height=height, scale=scale, rectangle=observed[-1]))
    f = Fui(widgets)
    definition = f.defs[f.exports['fourj.Cursor.FJ_Cursor_Normal'][1]]
    frame = f.frames[definition[1]]
    pointer = next(n for n in f.geometry[frame['start']:frame['start']+frame['count']]
                   if n['name'] == 'iggy_pointerIcon')
    # This is the original placeholder's shape bounds, rather than CursorImage.
    d = f.defs[pointer['header'][3]]
    shape_node = f.geometry[f.frames[d[1]]['start']]
    nd, _, np, *_ = f.counts
    shapes = 0x98 + nd*28 + np*132
    bounds = struct.unpack_from('<4f', f.b, shapes+shape_node['header'][3]*28+12)
    return dict(repository='Nejirus/mcps4', revision='37e258085773f11599cf0ffe29119926769554b7',
                eboot_sha256=hashlib.sha256(eboot.read_bytes()).hexdigest(),
                widgets_sha256=hashlib.sha256(f.b).hexdigest(), slot_routine='0x83b9b0',
                rounding_routine='0x3c0ea0', slot_cases=cases,
                pointer_item=dict(matrix=pointer['matrix'], shape_bounds=bounds))


if __name__ == '__main__':
    p = argparse.ArgumentParser()
    p.add_argument('--eboot', type=Path, required=True)
    p.add_argument('--widgets', type=Path, required=True)
    p.add_argument('--output', type=Path, required=True)
    a = p.parse_args()
    data = capture(a.eboot, a.widgets)
    a.output.write_text(json.dumps(data, indent=2)+'\n')
    print('Captured', len(data['slot_cases']), 'native slot rectangles and the original pointer-item placeholder')
