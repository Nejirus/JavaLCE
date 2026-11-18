"""Observe original mcps4 navigation and XP arithmetic with platform adapters.

The row refresh callback ends the temporary offer vector after navigation. This
avoids issuing a PS4 network packet; no selection/scroll decisions are replaced.
"""
import argparse,ctypes as C,json,struct
from pathlib import Path
from native_module import Module,P,I,F

def capture(path):
 m=Module(path);buffers=[]
 def alloc(n):
  b=C.create_string_buffer(n);buffers.append(b);return C.addressof(b)
 def write(p,fmt,v):C.memmove(p,struct.pack('<'+fmt,v),struct.calcsize('<'+fmt))
 def read(p,fmt):return struct.unpack('<'+fmt,C.string_at(p,struct.calcsize('<'+fmt)))[0]
 def patch(a,ret,args,fn):m.patch(a,C.CFUNCTYPE(ret,*args)(fn))
 guard=alloc(8);write(guard,'Q',0x12345678);m.w(0x134bae8,'Q',guard)
 scene,game,ui,slots=alloc(0x3000),alloc(0x200),alloc(0x200),alloc(0x800)
 patch(0xc34710,P,[],lambda:game)
 patch(0x86c4c0,P,[P],lambda _:ui)
 patch(0x832a40,None,[P,I],lambda *_:None)
 patch(0xad2560,None,[P],lambda _:write(scene+0x1b0,'Q',slots))
 nav=m.function(0xad3ce0,I,[P,I,I]);navigation=[]
 for count in [0,3,10,13,20]:
  for offset in range(max(0,count-10)+1):
   for col in range(10):
    for direction in [6,7]:
     write(scene+0x1a8,'Q',slots);write(scene+0x1b0,'Q',slots+count*16)
     write(scene+0x1c4,'i',col);write(scene+0x1c8,'i',offset)
     nav(scene,0,direction)
     navigation.append([count,offset,col,direction,read(scene+0x1c8,'i'),read(scene+0x1c4,'i')])
 # Execute original XP preview, including its accesses to the offer-list XP table.
 trader,vt,offerlist,offer=alloc(0x100),alloc(0x100),alloc(0x100),alloc(0x100)
 write(scene+0x188,'Q',trader);write(trader,'Q',vt)
 level,current=0,0.
 callbacks=[C.CFUNCTYPE(I,P)(lambda _:level),C.CFUNCTYPE(F,P)(lambda _:current),C.CFUNCTYPE(P,P,P)(lambda *_:offerlist)]
 m.callbacks.extend(callbacks)
 for off,cb in zip([0x60,0x58,0x20],callbacks):write(vt+off,'Q',C.cast(cb,P).value)
 patch(0x680b0,None,[P,P,P],lambda *_:None)
 for i,xp in enumerate([0,10,70,150,250,0]):write(offerlist+0x20+i*4,'i',xp)
 preview=m.function(0xad5730,F,[P,P]);experience=[]
 for level in range(5):
  for current in [0.,.25,.9,1.]:
   for xp in [0,1,5,10,30]:
    write(offer+0x58,'i',xp)
    value=preview(scene,offer)
    experience.append(dict(level=level,current=current,xp=xp,range=[10,60,80,100,1][level],bits=struct.unpack('<I',struct.pack('<f',value))[0]))
 return dict(repository='Nejirus/mcps4',revision='37e258085773f11599cf0ffe29119926769554b7',
             navigation_routine='0xad3ce0',experience_routine='0xad5730',navigation=navigation,experience=experience)
if __name__=='__main__':
 p=argparse.ArgumentParser();p.add_argument('--eboot',type=Path,required=True);p.add_argument('--output',type=Path,required=True);a=p.parse_args()
 data=capture(a.eboot);a.output.write_text(json.dumps(data,indent=2)+'\n');print('Captured',len(data['navigation']),'navigation transitions and',len(data['experience']),'XP previews')
