"""Bounded PS4 InfoTask input and map-decoration regression oracles.

Executes only 0x979160 and 0x44a610. Map dictionary storage and profiler calls
are host callbacks; no platform, renderer or game startup is executed.
"""
import argparse,ctypes as C,json,platform,struct
from pathlib import Path
from native_module import Module
from read_block_constructors import Source

p=argparse.ArgumentParser();p.add_argument('--eboot',type=Path,required=True);p.add_argument('--output',type=Path,required=True);a=p.parse_args()
if platform.machine() not in ['x86_64','AMD64']:p.error('Original routines require x86-64')
s=Source(a.eboot);m=Module(a.eboot);keep=[]
def alloc(n):
 b=C.create_string_buffer(n);keep.append(b);return C.addressof(b)
def put(p,fmt,*v):C.memmove(p,struct.pack('<'+fmt,*v),struct.calcsize('<'+fmt))
def get(p,fmt):return struct.unpack('<'+fmt,C.string_at(p,struct.calcsize('<'+fmt)))

info=m.function(0x979160,None,[C.c_void_p,C.c_int]);input_cases=[]
for active in [False,True]:
 for required in [0,1,2]:
  for pressed in [0,1,2,3,9]:
   task,head,node=alloc(0x80),alloc(24),alloc(24)
   put(task+0x19,'B',active);put(task+0x50,'Q',head)
   put(head,'Q',node);put(node,'Q',head);put(node+16,'i',required)
   info(task,pressed)
   input_cases.append(dict(active=active,required=required,pressed=pressed,completed=bool(get(node+20,'B')[0])))

counter=alloc(8);m.w(0x134bae8,'Q',counter);record=alloc(32);removed=False
@C.CFUNCTYPE(None,C.c_int)
def profiler(category):pass
@C.CFUNCTYPE(C.c_void_p,C.c_void_p,C.c_void_p)
def lookup(dictionary,key):return record
@C.CFUNCTYPE(None,C.c_void_p,C.c_void_p)
def remove(dictionary,key):
 global removed
 removed=True
m.patch(0xaff530,profiler);m.patch(0x44b230,lookup);m.patch(0x44b090,remove)
allowed={0xaff530,0x44b230,0x44b090,0xe6d030}
for it in s.instructions(0x44a610):
 if it.mnemonic=='call' and it.op_str.startswith('0x') and int(it.op_str,16) not in allowed:raise ValueError(('Unexpected callback',hex(it.address),it.op_str))
project=m.function(0x44a610,None,[C.c_void_p,C.c_void_p,C.c_void_p,C.c_int,C.c_double,C.c_double,C.c_double,C.c_double,C.c_int])
map_cases=[];player_type=alloc(16);put(player_type+2,'B',1)
for scale in range(5):
 for axis in ['x','z']:
  for pixel in [-65,-64,-63.99,-63.25,-63,-62.99,-1,0,1,62.99,63,63.25,63.99,64,65]:
   for yaw in [-181,0,181]:
    data=alloc(0x200);put(data+0x44,'ii',-512,512);put(data+0x4e,'BB',1,scale)
    x=-512+(pixel*(1<<scale) if axis=='x' else 0);z=512+(pixel*(1<<scale) if axis=='z' else 0)
    C.memset(record,0,32);removed=False;project(data,player_type,None,1,x,64,z,yaw,1)
    if removed:raise ValueError('Near-edge cursor unexpectedly removed')
    kind=get(record,'Q')[0];px,pz=get(record+8,'ii');rot=get(record+16,'b')[0]&15
    map_cases.append(dict(scale=scale,x=x,z=z,yaw=yaw,type='PLAYER' if kind==player_type else 'PLAYER_OFF_MAP',iconX=px,iconZ=pz,rotation=rot))

sizes=[]
for pc,register in [(0xaf2e90,'r15d'),(0xaf34a2,'r10d')]:
 it=next(s.instructions(pc,8));assert it.mnemonic=='mov' and it.op_str==register+', 0x2d'
 sizes.append(dict(pc=hex(pc),register=register,size=it.operands[1].imm,bytes=s.read(pc,it.size).hex()))
out=dict(repository='Nejirus/mcps4',revision='37e258085773f11599cf0ffe29119926769554b7',info_input='0x979160',info_completion='0x978ed0',map_projection='0x44a610',inline_glyphs=sizes,input_cases=input_cases,map_cases=map_cases)
a.output.write_text(json.dumps(out,indent=2)+'\n');print(json.dumps({'input_cases':len(input_cases),'map_cases':len(map_cases),'inline_height':sizes[0]['size']}))
