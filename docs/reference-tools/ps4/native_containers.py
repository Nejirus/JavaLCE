"""Execute mcps4's inventory recentering and chest/inventory section routes.

Only FUI scene lookups/getters/setters and the final slot-position callback are
replaced. The original float arithmetic, rounding and routing execute on CPU.
"""
import argparse
import ctypes as C
import json
import struct
from pathlib import Path
from native_module import Module, P, I, F


def capture(eboot, assets):
    m = Module(eboot)
    buffers, names, events = [], {}, []
    def alloc(n):
        b=C.create_string_buffer(n); buffers.append(b); return C.addressof(b)
    def w(p, fmt, value): C.memmove(p, struct.pack('<'+fmt,value),struct.calcsize('<'+fmt))
    def patch(a, ret, args, fn): m.patch(a,C.CFUNCTYPE(ret,*args)(fn))
    guard=alloc(8);w(guard,'Q',12345);m.w(0x134bae8,'Q',guard)
    patch(0xe6cfb0,None,[P],lambda *_:None)
    def reserve(p,n,_):
        if n>15:w(p+8,'Q',alloc(n+1));w(p+32,'Q',n)
        return 1
    patch(0x676a0,I,[P,P,P],reserve)
    def string(p):
        capacity=C.c_uint64.from_address(p+32).value
        start=C.c_void_p.from_address(p+8).value if capacity>15 else p+8
        return C.string_at(start).decode()
    nodes={};vtable=alloc(0x40)
    def visible(p,value):events.append(['visible',names[p],value])
    cb=C.CFUNCTYPE(None,P,I)(visible);m.callbacks.append(cb);w(vtable+0x30,'Q',C.cast(cb,P).value)
    data=json.loads(assets.read_text());scene=data['scenes']['InventoryMenu']
    last=data['panels']['FJ_Panel9Grid']['pieces'][-1]['rect']
    geometries={'MainPanel':(0,0),'BackgroundPanel':(0,(last[0]+last[2])*scene['static'][0]['matrix'][0])}
    for name,node in scene['controls'].items():
        geometries[name]=(node['matrix'][4],0)
    geometries['EquipmentHolder']=(geometries['EquipmentHolder'][0],63)
    geometries['iggy_player']=(geometries['iggy_player'][0],scene['player_bounds'][1])
    for name in geometries:
        nodes[name]=alloc(0x100);names[nodes[name]]=name;w(nodes[name],'Q',vtable)
    patch(0x7cff60,P,[P,P],lambda _,s:nodes[string(s).split('.')[-1]])
    patch(0x7cffa0,P,[P],lambda p:p)
    patch(0x7cffe0,F,[P],lambda p:geometries[names[p]][0])
    patch(0x7d0b10,F,[P],lambda p:geometries[names[p]][1])
    patch(0x7d0710,None,[P,P,I],lambda _,p,v:events.append(['remove',names[p],v]))
    patch(0x7da6e0,None,[P,F],lambda p,dx:events.append(['offset_x',names[p],dx]))
    m.function(0x81e150,None,[P,I])(nodes['MainPanel'],0)
    layout=events.copy()
    nav_scene,nav_table=alloc(0x3000),alloc(0x400)
    w(nav_scene,'Q',nav_table+0x40);w(nav_table+0x40-0x18,'q',0)
    patch(0x9a2610,None,[P,I,I,I,P,P,I,I],lambda _,old,new,d,x,y,dx,dy:events.append([old,new,d,dx,dy]))
    navigation={}
    for name,pc,sections,rows in [('inventory',0xa66550,range(10,14),4),('chest',0xa17a60,range(3),6)]:
        fn=m.function(pc,I,[P,I,I,P,P]);result=[]
        for split in [0,1]:
            w(nav_scene+0xd8,'B',split)
            for section in sections:
                for d in range(1,5):
                    for x in range(9):
                        for y in range(rows):
                            cx,cy=C.c_int(x),C.c_int(y);events.clear()
                            to=fn(nav_scene,section,d,C.byref(cx),C.byref(cy))
                            assert len(events)==1
                            result.append([split,section,d,x,y,to,*events[0][3:]])
        navigation[name]=result
    return dict(repository=data['repository'],revision=data['revision'],inventory_mode='console (classic crafting disabled)',
                layout_routine='0x81e150',layout_inputs=geometries,layout=layout,navigation=navigation)


if __name__=='__main__':
    p=argparse.ArgumentParser(description=__doc__);p.add_argument('--eboot',type=Path,required=True)
    p.add_argument('--assets',type=Path,required=True);p.add_argument('--output',type=Path,required=True)
    a=p.parse_args();data=capture(a.eboot,a.assets);a.output.write_text(json.dumps(data,indent=2)+'\n')
    print('Captured original inventory recentering and',sum(map(len,data['navigation'].values())),'section routes')
