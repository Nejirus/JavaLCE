"""Observe PS4's original normal-crafting row loop, with data/UI adapters.

Only 0x9b6890 executes. Recipe outputs and platform UI calls use isolated
buffers; the game's startup, renderer and network are never invoked.
"""
import argparse
import ctypes as C
import json
import struct
from pathlib import Path
from native_module import Module, P, I


def capture(path):
    m = Module(path)
    buffers = []

    def alloc(n):
        b = C.create_string_buffer(n)
        buffers.append(b)
        return C.addressof(b)

    def put(p, fmt, value):
        C.memmove(p, struct.pack('<' + fmt, value), struct.calcsize('<' + fmt))

    def patch(address, result, args, fn):
        m.patch(address, C.CFUNCTYPE(result, *args)(fn))

    guard, scene, vtable, recipes, output = [alloc(n) for n in [8, 0x3000, 0x200, 0x48 * 12, 0x40]]
    put(guard, 'Q', 0x12345678)
    m.w(0x134bae8, 'Q', guard)
    put(scene, 'Q', vtable)
    put(scene + 0xe10, 'i', 12)
    emitted = []

    def callback(offset, result, args, fn):
        cb = C.CFUNCTYPE(result, *args)(fn)
        m.callbacks.append(cb)
        put(vtable + offset, 'Q', C.cast(cb, P).value)

    callback(0x10, I, [P], lambda _: 0)
    callback(0x30, None, [P, I, I, P, I], lambda _, player, column, item, flags: emitted.append(column))
    item_vtable = alloc(0x40)
    cb = C.CFUNCTYPE(None, P, P, P)(lambda dest, item, unused: (put(dest, 'Q', output), put(dest + 8, 'Q', 0)) and None)
    m.callbacks.append(cb)
    put(item_vtable + 0x18, 'Q', C.cast(cb, P).value)
    for i in range(12):
        item = alloc(8)
        put(item, 'Q', item_vtable)
        put(recipes + i * 0x48 + 0x28, 'Q', item)
    patch(0x5ac910, P, [P, I], lambda *_: recipes)
    patch(0x9b3570, I, [P, I, I], lambda _, group, variant: group)
    patch(0xac8540, I, [I, I], lambda *_: 0)
    patch(0x356110, P, [P], lambda _: 0x1234)
    patch(0x66b00, None, [P, P, P], lambda dest, item, ref: (put(dest, 'Q', item or 0), put(dest + 8, 'Q', ref or 0)) and None)
    update = m.function(0x9b6890, None, [P])
    cases = []
    for context in [0, 1]:
        put(scene + 0xbb4, 'i', context)
        for count in [0, 1, 3, 7, 12]:
            for i in range(12):
                put(scene + 8 + i * 0xf8, 'i', int(i < count))
            emitted.clear()
            update(scene)
            assert emitted == list(range(count)), (context, count, emitted)
            cases.append(dict(context='personal' if context == 0 else 'table',
                              occupied_families=count, emitted_columns=list(emitted)))
    return dict(repository='Nejirus/mcps4', revision='37e258085773f11599cf0ffe29119926769554b7',
                routine='0x9b6890', cases=cases)


if __name__ == '__main__':
    p = argparse.ArgumentParser()
    p.add_argument('--eboot', type=Path, required=True)
    p.add_argument('--output', type=Path, required=True)
    a = p.parse_args()
    a.output.write_text(json.dumps(capture(a.eboot), indent=2) + '\n')
    print('Captured 10 original PS4 recipe-row cases')
