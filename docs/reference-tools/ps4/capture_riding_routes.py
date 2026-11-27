"""Execute mcps4 0xa47bd0 against horse/chest/armor/saddle visibility callbacks."""
import argparse
import ctypes as C
import json
import struct
from pathlib import Path
from native_module import Module, P, I


def capture(eboot):
    m = Module(eboot)
    buffers = []
    state = {}
    events = []
    def alloc(n):
        b = C.create_string_buffer(n)
        buffers.append(b)
        return C.addressof(b)
    def w(p, fmt, value):
        C.memmove(p, struct.pack('<' + fmt, value), struct.calcsize('<' + fmt))
    def callback(ret, args, fn):
        cb = C.CFUNCTYPE(ret, *args)(fn)
        m.callbacks.append(cb)
        return C.cast(cb, P).value
    guard = alloc(8)
    w(guard, 'Q', 12345)
    m.w(0x134bae8, 'Q', guard)
    scene, table, horse, horse_table = alloc(0x3000), alloc(0x500), alloc(0x100), alloc(0x1100)
    w(scene, 'Q', table + 0x40)
    w(table + 0x40 - 0x18, 'q', 0)
    w(scene + 0xf60, 'Q', horse)
    w(horse, 'Q', horse_table)
    w(horse_table + 0x60, 'Q', callback(I, [P], lambda _: 0x2d698000))
    w(horse_table + 0x1068, 'Q', callback(I, [P], lambda _: state['armor']))
    w(horse_table + 0x1000, 'Q', callback(I, [P], lambda _: state['saddle']))
    w(table + 0x40 + 0x1f8, 'Q', callback(I, [P, I], lambda _, s: state['columns'] if s == 0x4a else 9 if s < 0x4a else 1))
    def shared(out, *_):
        w(out, 'Q', horse)
        w(out + 8, 'Q', 0)
    m.patch(0x3cfc10, C.CFUNCTYPE(None, P, P, P)(shared))
    m.patch(0x4870, C.CFUNCTYPE(I, P)(lambda _: state['chest']))
    def route(_, old, new, d, x, y, dx, dy):
        events.append([new, C.c_int.from_address(x).value, C.c_int.from_address(y).value, dx, dy])
    m.patch(0x9a2610, C.CFUNCTYPE(None, P, I, I, I, P, P, I, I)(route))
    fn = m.function(0xa47bd0, I, [P, I, I, P, P])
    rows = []
    for split in [0, 1]:
        w(scene + 0xd8, 'B', split)
        for chest in [0, 1]:
            for armor, saddle in [(1, 1), (0, 1), (1, 0)]:
                for columns in [1, 3, 5]:
                    state.update(chest=chest, armor=armor, saddle=saddle, columns=columns)
                    for section in range(0x48, 0x4d):
                        if section == 0x4a and not chest or section == 0x4b and not armor or section == 0x4c and not saddle:
                            continue
                        for d in range(1, 5):
                            for x, y in [(-1, 0), (0, -1), (3, 1), (4, 3), (8, 0), (9, 2)]:
                                cx, cy = C.c_int(x), C.c_int(y)
                                events.clear()
                                to = fn(scene, section, d, C.byref(cx), C.byref(cy))
                                assert len(events) == 1 and to == events[0][0]
                                rows.append([split, chest, armor, saddle, columns, section, d, x, y, *events[0]])
    return dict(repository='Nejirus/mcps4', revision='37e258085773f11599cf0ffe29119926769554b7',
                routine='0xa47bd0', columns='source horse inventory columns', rows=rows)


if __name__ == '__main__':
    p = argparse.ArgumentParser(description=__doc__)
    p.add_argument('--eboot', type=Path, required=True)
    p.add_argument('--output', type=Path, required=True)
    a = p.parse_args()
    result = capture(a.eboot)
    a.output.write_text(json.dumps(result, separators=(',', ':')) + '\n')
    print('Captured', len(result['rows']), 'native riding section routes')
