"""Original PS4 AreaConstraint movement-axis oracle; no console startup.

The vector-pool allocator is a host callback. The original movement classifier
and correction body execute unchanged with FullTutorial's captured bounds/flags.
"""
import argparse,ctypes as C,json,platform,random,struct
from pathlib import Path
from native_module import Module
from read_block_constructors import Source

p=argparse.ArgumentParser();p.add_argument('--eboot',type=Path,required=True);p.add_argument('--output',type=Path,required=True);args=p.parse_args()
if platform.machine() not in ['x86_64','AMD64']:p.error('Native oracle requires x86-64')
s=Source(args.eboot);m=Module(args.eboot);keep=[]
def alloc(n):
 b=C.create_string_buffer(n);keep.append(b);return C.addressof(b)
def put(p,fmt,*v):C.memmove(p,struct.pack('<'+fmt,*v),struct.calcsize('<'+fmt))
@C.CFUNCTYPE(C.c_void_p,C.c_double,C.c_double,C.c_double)
def vector(x,y,z):
 out=alloc(24);put(out,'3d',x,y,z);return out
m.patch(0x712960,vector)
for i in s.instructions(0x961df0):
 if i.mnemonic=='call' and i.op_str!='0x712960':raise ValueError(('Unexpected native call',hex(i.address),i.op_str))
flags=next(s.instructions(0x9669ba,8));vertical=next(s.instructions(0x9669c1,8))
assert flags.op_str=='word ptr [r13 + 0x20], 0x101' and vertical.op_str=='byte ptr [r13 + 0x22], 0'
bounds=[-36.,0.,62.,20.,75.,113.];box=alloc(48);put(box,'6d',*bounds)
constraint=alloc(40);put(constraint+16,'Q',box);put(constraint+32,'3B',1,1,0)
fn=m.function(0x961df0,C.c_int,[C.c_void_p,C.c_void_p,C.c_int]+[C.c_double]*6)
cases=[]
samples=[([-15.,71.,85.],[-15.,y,85.]) for y in [-64,0,74.9,75,76,100,255,320]]
samples += [([-15.,74.,85.],[-40.,80.,85.]),([0.,74.,85.],[25.,80.,85.]),([30.,80.,120.],[25.,90.,121.]),([30.,80.,120.],[25.,90.,115.]),([-36.,100.,62.],[-36.,110.,62.])]
rng=random.Random(2310)
for _ in range(128):
 old=[rng.choice([-40.,-36.,-35.,0.,19.,20.,25.]),rng.choice([0.,71.,74.,75.,100.,255.]),rng.choice([60.,62.,63.,85.,112.,113.,120.])]
 target=[old[0]+rng.choice([-5.,0.,5.]),old[1]+rng.choice([-10.,0.,10.]),old[2]+rng.choice([-5.,0.,5.])];samples.append((old,target))
for old,target in samples:
 out=alloc(24);put(out,'3d',*target);mask=fn(constraint,out,0,*(old+target));corrected=list(struct.unpack('<3d',C.string_at(out,24)))
 cases.append(dict(origin=old,target=target,blocked_axes=mask,corrected=corrected))
args.output.write_text(json.dumps(dict(repository='Nejirus/mcps4',revision='37e258085773f11599cf0ffe29119926769554b7',constructor_flags=['0x9669ba','0x9669c1'],movement_function='0x961df0',bounds=bounds,contains=True,restrict_horizontal=True,restrict_vertical=False,cases=cases),indent=2)+'\n')
print(json.dumps(dict(cases=len(cases),vertical_restricted=False)))
