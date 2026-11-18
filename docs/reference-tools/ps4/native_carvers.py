"""Execute bounded original mcps4 routines on the host CPU with synthetic world data.
No game/module startup, platform services, filesystem, or network callbacks run.
The supplied libc.prx provides the original PS4 mathematical functions.
"""
from native_module import Module,C,U,I,F,D,P
import struct,math,hashlib,json,os
from pathlib import Path
class Oracle:
 def __init__(self,executable,library):
  self.game=Module(executable);self.lib=Module(library);self.host=C.CDLL(None);self.keep=[];self.callbacks=[]
  self.host.malloc.restype=P;self.host.malloc.argtypes=[U];self.host.free.argtypes=[P];self.host.memset.argtypes=[P,I,U];self.host.memset.restype=P;self.host.memmove.argtypes=[P,P,U];self.host.memmove.restype=P;self.host.memcpy.argtypes=[P,P,U];self.host.memcpy.restype=P
  self.lib.trap_unbound()
  self.game.bind('+P6FRGH4LfA',self.host.memmove);self.game.bind('Q3VBxCXhUHs',self.host.memcpy)
  def bindpc(pc,fn):
   off=struct.unpack('<i',C.string_at(self.game.base+pc+2,4))[0];self.game.w(pc+6+off,'Q',C.cast(fn,P).value)
  for pc in [0xe6cfc0,0xe6cfd0]:bindpc(pc,self.host.malloc)
  for pc in [0xe6cfb0,0xe6cfa0]:bindpc(pc,self.host.free)
  bindpc(0xe6d0b0,self.host.memset)
  # Only original math routines needed by the selected entrypoints are bound.
  for pc,nid in [(0xe6ce40,'ZtjspkJQ+vw'),(0xe6d3d0,'weDug8QD-lE')]:bindpc(pc,P(self.lib.base+self.lib.exports[nid]))
  guard=self.alloc(8);self.game.w(0x134bae8,'Q',guard)
  for pc in [0xe6d930,0xe6d950]:bindpc(pc,P(self.callback(I,[U,U,U,U,U,U],lambda *args:0)))
  self.game.trap_unbound()
  self.world=self.alloc(1024);self.biome=self.alloc(1024);self.feature=self.alloc(8192);self.container=self.alloc(64);self.buffer=self.alloc(65536);self.vt=self.alloc(2048);self.states={};self.blocks={}
  self.w(self.world,'Q',self.vt);self.w(self.vt+0xb8,'Q',self.callback(P,[P,P],lambda a,b:self.biome));self.w(self.biome+0x50,'i',2);self.w(self.biome+0x10,'f',.125);self.w(self.biome+0x18,'f',.8);self.w(self.biome+0x1ac,'i',11);self.w(self.feature+8,'i',8)
  self.w(self.world+8,'i',63);self.w(self.container,'Q',self.buffer);self.w(self.container+8,'I',32768)
  blockvt=self.alloc(512);statevt=self.alloc(512);self.statevt=statevt;self.blockvt=blockvt
  self.w(blockvt+0x50,'Q',self.callback(I,[P,P],lambda a,b:self.read(b+8,'I')&15))
  self.w(statevt+0x20,'Q',self.callback(I,[P],lambda a:15 if self.read(a+8,'I')>>4 in [10,11] else 0))
  self.w(statevt+0x170,'Q',self.callback(P,[P],lambda a:self.blocks[self.read(a+8,'I')>>4]))
  registry=self.alloc(4096)
  for id in range(256):
   block=self.alloc(512);self.blocks[id]=block;self.w(block,'Q',blockvt);self.w(block+8,'I',id);self.w(block+0x170,'Q',self.alloc(16));self.w(registry+0x150+id*8,'Q',block)
  for ptr,id in [(0x1409fa8,3),(0x1409fb0,2),(0x140aba0,8),(0x140aba8,10),(0x140b268,9),(0x140b270,11),(0x140b318,49),(0x140b7b8,213)]:self.game.w(ptr,'Q',self.blocks[id])
  water=self.alloc(16)
  self.game.w(0x1418330,'Q',water)
  for id in [8,9]:self.w(self.blocks[id]+0x170,'Q',water)
  self.game.w(0x140ad68,'Q',registry)
  self.patch(0x3df750,U,[P],lambda world:self.worldSeed&0xffffffffffffffff)
  self.patch(0x91c30,P,[I],lambda packed:self.state(packed&65535))
  self.patch(0x7a4d0,P,[P],lambda block:self.state(self.read(block+8,'I')<<4))
  for addr in [0xaff530,0x3796f0]:self.patch(addr,None,[U,U,U,U,U,U],lambda *args:None)
 def alloc(self,n):
  buf=C.create_string_buffer(n);self.keep.append(buf);return C.addressof(buf)
 def w(self,p,fmt,value):C.memmove(p,struct.pack('<'+fmt,value),struct.calcsize('<'+fmt))
 def read(self,p,fmt):return struct.unpack('<'+fmt,C.string_at(p,struct.calcsize('<'+fmt)))[0]
 def callback(self,ret,args,fn):
  def guarded(*a):
   try:return fn(*a)
   except BaseException as e:print('BOUND WORLD CALLBACK FAILED',str(e),flush=True);os._exit(92)
  cb=C.CFUNCTYPE(ret,*args)(guarded);self.callbacks.append(cb);return C.cast(cb,P).value
 def patch(self,p,ret,args,fn):self.game.patch(p,P(self.callback(ret,args,fn)))
 def state(self,packed):
  if packed not in self.states:
   p=self.alloc(32);self.w(p,'Q',self.statevt);self.w(p+8,'I',packed);self.states[packed]=p
  return self.states[packed]
 def fill(self,height=70):
  data=[(7 if y==0 else 2 if y==height-1 else 3 if height-4<=y<height-1 else 1 if y<height else 0)<<4 for x in range(16) for z in range(16) for y in range(128)];C.memmove(self.buffer,struct.pack('<32768H',*data),65536)
 def output(self):return [x>>4 for x in struct.unpack('<32768H',C.string_at(self.buffer,65536))]
 def call(self,pc,ret,args,values):return self.game.function(pc,ret,args)(*values)
 def carve(self,seed,cx,cz,kind='caves'):
  self.worldSeed=seed;info=self.alloc(64);ivt=self.alloc(64);self.w(self.world+0x218,'Q',info);self.w(info,'Q',ivt);self.w(ivt+0x28,'Q',self.callback(U,[P],lambda a:seed&0xffffffffffffffff))
  table={'caves':0x127d6c8,'canyon':0x12290a8,'underwater_caves':0x12fde88,'underwater_canyon':0x12fde28}[kind]
  self.w(self.feature,'Q',self.game.base+table)
  entry=self.game.read(table+16,'Q')-self.game.base
  return self.call(entry,None,[P,P,I,I,P],[self.feature,self.world,cx,cz,self.container])
 def tunnel(self,seed,x,y,z,thickness=3,yaw=0,pitch=0,step=-1,distance=-1,yScale=1):
  self.call(0x3de090,None,[P,P,U,I,I,P,D,D,D,F,F,F,I,I,D],[self.feature,self.world,seed&0xffffffffffffffff,0,0,self.container,x,y,z,thickness,yaw,pitch,step,distance,yScale])
