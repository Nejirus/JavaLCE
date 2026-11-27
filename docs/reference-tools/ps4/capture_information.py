"""Execute bounded original PS4 effect layout, fade-alpha and selection setters.

Platform, player/effect lookup and FUI draw calls use isolated callback buffers.
The original arithmetic/control flow runs; no game startup is called.
"""
import argparse
import ctypes as C
import json
import math
import struct
from pathlib import Path
from native_module import Module, P, I, F


def capture(eboot):
    m = Module(eboot)
    buffers = []
    def alloc(size):
        b = C.create_string_buffer(size); buffers.append(b); return C.addressof(b)
    def put(p, fmt, value):
        C.memmove(p, struct.pack('<' + fmt, value), struct.calcsize('<' + fmt))
    def patch(pc, result, args, fn):
        cb = C.CFUNCTYPE(result, *args)(fn); m.patch(pc, cb); return C.cast(cb, P).value
    def callback(result, args, fn):
        cb = C.CFUNCTYPE(result, *args)(fn); m.callbacks.append(cb); return C.cast(cb, P).value
    guard = alloc(8); put(guard, 'Q', 0x12345678); m.w(0x134bae8, 'Q', guard)
    patch(0xe6cfb0, None, [P], lambda _: None)
    patch(0xe6cfc0, P, [C.c_uint64], alloc)
    patch(0xe6ce40, F, [F], math.sin)
    patch(0xe6d3d0, F, [F], math.atan)
    # Capture the original inventory bar stacking routine.
    layout = alloc(0x200); vtable = alloc(0x40); ys = []
    setter = callback(None, [P, F], lambda _, y: ys.append(y))
    # C++ member ABI has y in xmm0, independent of the integer self register.
    put(vtable + 0x20, 'Q', setter)
    for index in range(29):
        widget = alloc(8); put(widget, 'Q', vtable); put(layout + 0x90 + index * 8, 'Q', widget)
    put(layout + 0x1b4, 'i', 567); put(layout + 0x1b8, 'i', 473); put(layout + 0x1bc, 'i', 0)
    arrange = m.function(0x81dfa0, None, [P]); layouts = []
    for count in range(1, 30):
        for i in range(29): put(layout + 0x178 + i, 'B', int(i < count))
        ys.clear(); arrange(layout)
        layouts.append(dict(count=count, y=list(ys)))
    # Selection starts the original 60-tick timer for nonempty text.
    scene = alloc(0x300); text = alloc(0x28); select = m.function(0xa4e3f0, None, [P, P])
    selections = []
    for length in (0, 1, 5, 250):
        put(scene + 0x1f0, 'i', 0); put(text + 0x18, 'Q', length); select(scene, text)
        selections.append(dict(length=length, ticks=C.c_int.from_address(scene + 0x1f0).value))
    order = []
    widgets = {C.c_uint64.from_address(layout + 0x90 + i * 8).value:i for i in range(29)}
    patch(0x7da2d0, P, [P], lambda p:p)
    patch(0x8429c0, P, [P, P, I], lambda widget, _, seconds: (order.append(widgets[widget]),widget)[1])
    patch(0x81dfa0, None, [P], lambda _: None)
    put(vtable + 0x30, 'Q', callback(None, [P, I], lambda *_:None))
    show = m.function(0x81e940, None, [P, I, P, I]); ordering = []
    for effect_id in range(1,30):
        if effect_id == 25:continue # Saturation has no icon and passes type 0.
        order.clear();show(layout,effect_id,text,120)
        ordering.append(dict(effect_id=effect_id,slot=order[0]))
    # The original HUD effect loop calculates the short-duration pulse.
    mc, player, player_vtable, vector, effect, effect_type, type_vtable, view = [alloc(n) for n in (0x120, 8, 0x960, 24, 8, 8, 0x40, 0x100)]
    array = alloc(8); put(array, 'Q', effect); put(vector + 8, 'Q', array); put(vector + 16, 'Q', array + 8)
    put(mc + 0x80, 'Q', player); put(player, 'Q', player_vtable); put(effect_type, 'Q', type_vtable)
    put(player_vtable + 0x950, 'Q', callback(P, [P], lambda _: vector))
    put(type_vtable + 0x30, 'Q', callback(C.c_ubyte, [P], lambda _: 1))
    put(scene + 0x30, 'Q', view)
    patch(0xc34710, P, [], lambda: mc)
    patch(0x3d56c0, None, [P, P, P], lambda dest, value, ref: (put(dest, 'Q', value), put(dest + 8, 'Q', 0)) and None)
    patch(0xaab260, None, [P, P, I, P], lambda *_: None)
    patch(0x491b80, P, [P], lambda _: effect_type)
    patch(0x4915a0, C.c_ubyte, [P], lambda _: 1)
    patch(0x4915b0, I, [P], lambda _: 14)
    duration = 200
    patch(0x491750, I, [P], lambda _: duration)
    patch(0x86c4c0, P, [P], lambda _: view)
    patch(0x7da5d0, P, [P], lambda _: view)
    emitted = []
    patch(0x8191c0, None, [F, P, I, I, I, C.c_ubyte],
          lambda alpha, _, count, index, icon, good: emitted.append(float(alpha)))
    m.trap_unbound()
    update = m.function(0xa4f690, None, [P]); pulses = []
    for duration in (1200, 200, 199, 180, 150, 100, 50, 20, 10, 1, 0):
        emitted.clear(); update(scene)
        assert len(emitted) == 1
        pulses.append(dict(ticks=duration, alpha=emitted[0]))
    return dict(repository='Nejirus/mcps4', revision='37e258085773f11599cf0ffe29119926769554b7',
                routines=['0x81dfa0', '0x81e940', '0xa4e3f0', '0xa4f690'], layouts=layouts,
                selections=selections, pulses=pulses, ordering=ordering)


if __name__ == '__main__':
    p = argparse.ArgumentParser(); p.add_argument('eboot', type=Path); p.add_argument('output', type=Path)
    a = p.parse_args(); a.output.write_text(json.dumps(capture(a.eboot), indent=2) + '\n')
