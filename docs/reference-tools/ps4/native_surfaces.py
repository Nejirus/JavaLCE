"""Original mcps4 surface entry points with controlled block/biome inputs."""
from native_carvers import Oracle,C,P,U,I,F,D
import struct
class SurfaceOracle(Oracle):
 def __init__(self,executable,library):
  super().__init__(executable,library)
  # Literal initializers from 0x6ac2fe..0x6ac329; no module startup runs.
  self.game.w(0x1430180,'Q',0x3fd76cf5d0b09954);self.game.w(0x1430188,'Q',0x3fcb0cb174df99c8)
  self.patch(0xaf4a20,P,[P],lambda ptr:0)
  pc=0xe6d230;offset=self.game.read(pc+2,'i');self.game.w(pc+6+offset,'Q',self.lib.base+self.lib.exports['2WE3BTYVwKM'])
  air=self.read(self.blocks[0]+0x170,'Q');self.game.w(0x14188d0,'Q',air)
  self.w(self.statevt,'Q',self.callback(P,[P],lambda state:self.read(self.blocks[self.read(state+8,'I')>>4]+0x170,'Q')))
  self.w(self.statevt+0x158,'Q',self.callback(P,[P,P,P],self.set_value))
  for id in range(16):
   enum=self.alloc(24);self.w(enum+8,'I',id);self.game.w(0x1414778+id*8,'Q',enum)
  for addr,id in [(0x1413f10,1),(0x1413f18,2),(0x142f4b0,1)]:
   enum=self.alloc(24);self.w(enum+8,'I',id);self.game.w(addr,'Q',enum)
  for addr,id in [(0x1409f60,1),(0x140ab38,12),(0x140b6d8,172),(0x140b6b8,159),(0x140b200,174),(0x140b428,80)]:self.game.w(addr,'Q',self.blocks[id])
  for addr,id in [(0x140aa58,1),(0x140aa60,0),(0x140aa68,7),(0x140aa78,179),(0x140aa80,24),(0x140aa88,79),(0x140aa90,9),(0x14332b8,111)]:self.game.w(addr,'Q',id)
  self.call(0x46a4e0,None,[],[])
  self.game.w(0x140ab08,'Q',self.noise(2345,1));self.game.w(0x140ab00,'Q',self.noise(1234,1))
  self.random=self.alloc(24);self.surfaceBiomes={};self.worldSeed=0
 def set_value(self,state,prop,boxed):
  id=self.read(state+8,'I')>>4;enum=self.read(boxed+16,'Q');meta=self.read(enum+8,'I');return self.state((id<<4)|meta)
 def noise(self,seed,count):
  random=self.alloc(24);noise=self.alloc(24);self.call(0x596c90,None,[P,U],[random,seed&0xffffffffffffffff]);self.call(0x52a300,None,[P,P,I],[noise,random,count]);return noise
 def biome_for(self,id):
  if id in self.surfaceBiomes:return self.surfaceBiomes[id]
  b=self.alloc(1024);v=self.alloc(256);self.w(b,'Q',v);self.w(v+0x88,'Q',self.game.base+0x855e0);self.w(v+0x50,'Q',self.game.base+(0x4d7380 if id in [48,49] else 0x85a70))
  temps={2:2.,3:.2,14:.9,20:.2,25:.2,34:.2,131:.2,162:.2,5:.25,30:-.5,32:.3,33:.3,160:.25,161:.25,163:1.1,164:1.,48:0.,49:0.,12:0.,13:0.,140:0.,26:.05,42:.5,43:.5,44:.5,45:.5}
  self.w(b+0x18,'f',temps.get(id,2. if id in [37,38,39,165,166,167] else .8))
  depths={1:.125,6:-.2,134:-.1,37:.1,38:1.5,39:1.5,165:.1,166:.45,167:.45,48:-1.,49:-1.8,3:1.,32:.2,163:.3625,26:0.,14:.2,140:.425,42:-1.,45:-1.8}
  families={1:11,6:15,134:15,37:8,38:8,39:8,165:8,166:8,167:8,48:10,49:10,3:2,32:16,163:13,26:0,14:9,140:6,42:10,45:10}
  self.w(b+0x10,'f',depths.get(id,.125));self.w(b+0x1ac,'I',families.get(id,11));self.w(b+0x20,'B',id in [12,13,26,140]);self.w(b+0x1b0,'f',.125 if id in [48,49] else 0)
  self.w(b+0x50,'Q',2);self.w(b+0x58,'Q',3);self.w(b+0x1c0,'i',7);self.w(b+0x1c4,'i',1);self.w(b+0x1c8,'Q',13)
  if id in [2,16,26]:self.w(b+0x50,'Q',12);self.w(b+0x58,'Q',12)
  if id==25:self.w(b+0x50,'Q',1);self.w(b+0x58,'Q',1)
  if id==14:self.w(b+0x50,'Q',110)
  if id==140:self.w(b+0x50,'Q',80)
  if id in [42,43,44,45]:
   self.w(b+0x1c0,'I',3 if id in [42,43,44] else 7);self.w(b+0x1c8,'Q',12)
  if id in [3,20,34,131,162]:self.w(b+0x1d8,'i',1 if id==34 else 2 if id in [131,162] else 0)
  if id in [32,33,160,161]:self.w(b+0x1d0,'i',1 if id in [32,33] else 2)
  if id in [37,38,39,165,166,167]:
   self.w(b+0x50,'Q',self.game.read(0x14192f8,'Q'));self.w(b+0x58,'Q',self.game.read(0x14192e8,'Q'))
   self.w(b+0x1f8,'B',id==165);self.w(b+0x1f9,'B',id in [38,166])
  if id in [48,49]:self.w(b+0x1f8,'Q',self.noise(3456,3))
  self.surfaceBiomes[id]=b;return b
 def column(self,seed,id,x,z,noise,before,random_seed=1171,reset_random=True,ice_before=None):
  self.worldSeed=seed
  if reset_random:self.call(0x596c70,None,[P,U],[self.random,random_seed&0xffffffffffffffff])
  col=((z&15)<<11)|((x&15)<<7);ice=((x&15)<<11)|((z&15)<<7)
  self.host.memset(self.buffer,0,65536)
  if ice_before is None:ice_before=before
  if col!=ice:
   for y,state in enumerate(ice_before):self.w(self.buffer+(ice+y)*2,'H',((state&65535)<<4)|((state>>16)&15))
  for y,state in enumerate(before):self.w(self.buffer+(col+y)*2,'H',((state&65535)<<4)|((state>>16)&15))
  pc=0x46aa40 if id in [37,38,39,165,166,167] else 0x6ca1e0 if id in [6,134] else 0x4d7660 if id in [48,49] else 0x2a0240 if id in [3,20,34,131,162] else 0x6d1c50 if id in [5,30,32,33,160,161] else 0x4a2290 if id in [163,164] else 0x855d0
  self.call(pc,None,[D,P,P,P,P,I,I],[noise,self.biome_for(id),self.world,self.random,self.container,x,z])
  result=[]
  for y in range(128):
   v=self.read(self.buffer+(col+y)*2,'H');result.append((v>>4)|((v&15)<<16))
  state=self.read(self.random,'Q');after_rng=self.call(0x58fd50,D,[P],[self.random]);self.w(self.random,'Q',state)
  ice_result=[]
  for y in range(128):
   v=self.read(self.buffer+(ice+y)*2,'H');ice_result.append((v>>4)|((v&15)<<16))
  return result,ice_result,after_rng
