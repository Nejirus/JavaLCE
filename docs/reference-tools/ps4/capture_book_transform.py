"""Observe the original PS4 book draw matrices, stubbing graphics/platform calls only."""
import argparse,ctypes as C,json,struct,hashlib
from pathlib import Path
from native_module import Module,P,I,F
parser=argparse.ArgumentParser(description=__doc__);parser.add_argument('--eboot',required=True);parser.add_argument('--output',required=True);args=parser.parse_args()
assert hashlib.sha256(Path(args.eboot).read_bytes()).hexdigest()=='c6089834fe1372854eed8f9acf2cb307f7009aaeb32ee303ce5bf6a1160786c8'
m=Module(args.eboot);keep=[]
def alloc(n):
 b=C.create_string_buffer(n);keep.append(b);return C.addressof(b)
def w(p,f,v):C.memmove(p,struct.pack('<'+f,v),struct.calcsize('<'+f))
def patch(a,r,t,fn):m.patch(a,C.CFUNCTYPE(r,*t)(fn))
guard=alloc(8);w(guard,'Q',0x12345678);m.w(0x134bae8,'Q',guard)
scale=[];translate=[]
for a in [0xbe6f60,0xbe6f70,0xbe7020,0xc4d8e0,0xc4de80,0xbe7010,0xbe7c90]:patch(a,None,[],lambda:None)
patch(0xbe6ff0,None,[F,F,F],lambda x,y,z:scale.append([x,y,z]))
patch(0xbe6fb0,None,[F,F,F],lambda x,y,z:translate.append([x,y,z]))
patch(0xbe6fd0,None,[F,F,F,F],lambda *v:None)
patch(0x9e0fd0,None,[P],lambda *v:None)
app=alloc(0x220);w(app+0x1f0,'Q',alloc(0x20));patch(0xc34710,P,[],lambda:app)
patch(0xd622f0,I,[P,I],lambda *v:0);patch(0xd623b0,None,[P,I],lambda *v:None)
obj=alloc(0xc0);model=alloc(0x20);vt=alloc(0x30);w(model,'Q',vt);w(obj+0x68,'Q',model)
render=C.CFUNCTYPE(None,P,P,I,F,F,F,F,F,F)(lambda *v:None);keep.append(render);w(vt+0x10,'Q',C.cast(render,P).value)
# Original transformed 65x59 shape in EnchantingMenu1080.fui.
width=65*2.815338134765625;height=59*1.9828338623046875
w(obj+0x58,'f',width);w(obj+0x5c,'f',height);w(obj+0x9c,'f',1)
fn=m.function(0x9e0b10,None,[P,P]);bounds=alloc(16);rows=[]
for ratio in [.5,1,4/3,2]:
 for k,v in enumerate([0,width*ratio,0,height*ratio]):w(bounds+4*k,'f',v)
 scale.clear();translate.clear();fn(obj,bounds)
 rows.append(dict(view_ratio=ratio,scales=scale.copy(),translations=translate.copy(),
                  combined_x=scale[0][0]*scale[1][0],combined_y=scale[0][1]*scale[1][1]))
assert all(abs(abs(r['combined_x'])-57)<.0001 for r in rows)
with open(args.output,'w') as f:json.dump(dict(routine='0x009e0b10',placeholder=[65,59],rows=rows),f,indent=2);f.write('\n')
print('Captured original book transform at',len(rows),'viewport ratios')
