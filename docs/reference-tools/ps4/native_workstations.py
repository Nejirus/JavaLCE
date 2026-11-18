"""Execute original PS4 menu routines with bounded inventory/UI platform adapters.

Does not invoke game or SDK startup. UI callbacks only observe display-list state;
the arithmetic and section routing are executed by the original machine code.
"""
import argparse
import ctypes as C
import json
import struct
from pathlib import Path
from native_module import Module, P, I, F


def capture(eboot):
    module = Module(eboot)
    buffers = []
    def alloc(size):
        b = C.create_string_buffer(size)
        buffers.append(b)
        return C.addressof(b)
    def write(p, fmt, value):
        C.memmove(p, struct.pack('<'+fmt, value), struct.calcsize('<'+fmt))
    def read(p, fmt):
        return struct.unpack('<'+fmt, C.string_at(p, struct.calcsize('<'+fmt)))[0]
    def callback(addr, restype, types, fn):
        cb = C.CFUNCTYPE(restype, *types)(fn)
        module.patch(addr, cb)

    # End of the brewing tick tail-calls its shared scene tick (platform bookkeeping).
    callback(0xa0adf0, None, [P], lambda *_: None)
    scene, menu, vtable = alloc(0x3000), alloc(0x100), alloc(0x200)
    values = [0, 0]
    get_data = C.CFUNCTYPE(I, P, I)(lambda _, index: values[index])
    module.callbacks.append(get_data)
    write(menu, 'Q', vtable)
    write(vtable+0x90, 'Q', C.cast(get_data, P).value)
    write(scene+0xf30, 'Q', menu)
    for begin, maximum, control in [(0x2134, 400, 0x1ef0),
                                     (0x23d4, 30, 0x2190), (0x2674, 20, 0x2430)]:
        write(scene+begin, 'i', 0)
        write(scene+begin+4, 'i', maximum)
        write(scene+control, 'Q', alloc(0x40))
    tick = module.function(0xa12830, None, [P])
    brewing = []
    for remaining in range(401):
        values[:] = [remaining, remaining % 21]
        tick(scene)
        brewing.append(dict(remaining=remaining, fuel=values[1],
                            arrow_bits=read(scene+0x2140, 'I'),
                            bubble_value=read(scene+0x23dc, 'i'),
                            bubble_bits=read(scene+0x23e0, 'I'),
                            fuel_bits=read(scene+0x2680, 'I')))

    # Arrow override changes remaining progress into a top-anchored fill. Only
    # observe the float forwarded to the shared FJ_ProgressBar implementation.
    forwarded=[]
    callback(0x7e02c0,None,[P,F],lambda _,value:forwarded.append(value))
    arrow=module.function(0x843d40,None,[P,F])
    for row in brewing:
        arrow(scene,struct.unpack('<f',struct.pack('<I',row['arrow_bits']))[0])
        row['arrow_fill_bits']=struct.unpack('<I',struct.pack('<f',forwarded[-1]))[0]

    # Beacon widget's two UI calls set the icon and display-list frame.
    widget, ui = alloc(0x100), alloc(0x100)
    write(widget+0x40, 'Q', ui)
    write(ui+0xf8, 'Q', alloc(0x100))
    callback(0x7e3b70, None, [P, I], lambda *_: None)
    callback(0x7e3bd0, None, [P, I], lambda *_: None)
    update = module.function(0x9df160, None, [P, I, I, I, I])
    beacon = []
    for enabled in [False, True]:
        for selected in [False, True]:
            for focused in [False, True]:
                write(widget+0x68, 'i', -1)
                write(widget+0x70, 'i', -1)
                write(widget+0x76, 'B', int(focused))
                update(widget, 100, 1, int(enabled), int(selected))
                beacon.append(dict(enabled=enabled, selected=selected, focused=focused,
                                   frame=read(widget+0x68, 'i')))

    # Native section routing calls the common slot-position adapter at 0x9a2610.
    # Capture the destination plus x/y offsets passed to it, without replacing routing decisions.
    nav_scene, nav_vtable = alloc(0x3000), alloc(0x300)
    write(nav_scene, 'Q', nav_vtable+0x40)
    write(nav_vtable+0x40-0x18, 'q', 0)
    events = []
    callback(0x9a2610, None, [P,I,I,I,P,P,I,I],
             lambda _, old, new, direction, x, y, dx, dy: events.append([old,new,direction,dx,dy]))
    callback(0x9df1f0, I, [P], lambda *_: 1)
    navigation = {}
    for name, address, sections in [('brewing',0xa12f70,range(0x29,0x30)),
                                    ('enchanting',0xa3bed0,range(0x21,0x28)),
                                    ('cartography',0xa146b0,range(0x59,0x5f)),
                                    ('beacon',0xa0f5d0,range(0x38,0x43)),
                                    ('furnace',0xa40e10,range(4,9)),
                                    ('anvil',0xa0dbf0,range(0x31,0x37))]:
        fn = module.function(address, I, [P,I,I,P,P])
        rows = []
        for split in [0,1]:
            write(nav_scene+0xd8,'B',split)
            for section in sections:
                for direction in range(1,5):
                    for col in range(9):
                        cx, cy = C.c_int(col), C.c_int(0)
                        events.clear()
                        target=fn(nav_scene,section,direction,C.byref(cx),C.byref(cy))
                        assert len(events)==1,(name,section,direction,events)
                        rows.append([split,section,direction,col,target,*events[0][3:]])
        navigation[name]=rows
    # The original name builder runs its own Random draws and word selection.
    # UTF16 append callbacks record only the selected source word indices.
    words_base=alloc(62*40)
    name_random,name_output=alloc(0x40),alloc(0x40)
    write(name_random+0x20,'Q',words_base);write(name_random+0x28,'Q',words_base+62*40)
    selected_words=[]
    callback(0x67840,P,[P,P,I],lambda out,*_:out)
    callback(0x6e090,P,[P,P,I],lambda out,*_:out)
    callback(0x6df50,P,[P,P,I,C.c_uint64],lambda out,word,*_:selected_words.append((word-words_base)//40) or out)
    seed_random=module.function(0x596c90,None,[P,C.c_uint64])
    next_name=module.function(0x9e1df0,P,[P,P])
    names=[]
    for seed in [0,1,2,17,234,123456789,0xffffffffffffffff]:
        seed_random(name_random,seed)
        for draw in range(12):
            selected_words.clear();next_name(name_output,name_random)
            names.append(dict(seed=str(seed),draw=draw,indices=selected_words.copy()))
    return dict(enchantment_names=names,repository='Nejirus/mcps4', revision='37e258085773f11599cf0ffe29119926769554b7',
                brewing=brewing, beacon_buttons=beacon, navigation=navigation)


if __name__ == '__main__':
    parser=argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--eboot',type=Path,required=True)
    parser.add_argument('--output',type=Path,required=True)
    args=parser.parse_args()
    output=capture(args.eboot)
    args.output.write_text(json.dumps(output,indent=2)+'\n')
    print('Captured',len(output['brewing']),'ticks and',
          sum(map(len,output['navigation'].values())),'navigation cases')
