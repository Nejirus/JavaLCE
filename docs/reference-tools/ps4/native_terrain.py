"""Original PS4 noise, density and finite interpolation with controlled biome inputs."""
from native_surfaces import SurfaceOracle,C,P,U,I,D
import struct
class DensityOracle(SurfaceOracle):
 def __init__(self,executable,library,properties):
  super().__init__(executable,library);self.props={};self.biome_ptrs={}
  self.props={int(k):v for k,v in properties['values'].items()}
 def generator(self,seed):
  gen=self.alloc(1024);self.w(gen+0x90,'Q',self.world);self.w(gen+0xa0,'Q',self.alloc(32));rnd=self.alloc(24);self.call(0x596c90,None,[P,U],[rnd,seed&0xffffffffffffffff])
  for off,count,ctor in [(0x48,16,0x52a0f0),(0x50,16,0x52a0f0),(0x58,8,0x52a0f0),(0x60,4,0x52a300),(0x68,10,0x52a0f0),(0x70,16,0x52a0f0),(0x88,8,0x52a0f0)]:
   p=self.alloc(24);self.call(ctor,None,[P,P,I],[p,rnd,count]);self.w(gen+off,'Q',p)
  for dst,src,n in [(0xa8,0xea1300,32),(0xd0,0xe9f8e0,16),(0xe4,0xe9f900,16),(0xf8,0xe9f8f0,16)]:C.memmove(gen+dst,C.string_at(self.game.base+src,n),n)
  self.w(gen+0xc8,'Q',0x408c544c40d7be73)
  for off,v in [(0xe0,0x409c24de),(0xf4,0x408c544c),(0x108,0x405f7f69)]:self.w(gen+off,'I',v)
  cfg=self.alloc(0x124);self.call(0x1f1940,None,[P],[cfg]);self.w(gen+0x110,'Q',cfg)
  self.w(gen+0x118,'Q',self.state(9<<4));return gen
 def get_heights(self,gen,cx,cz,ids):
  container=self.alloc(24);region=self.alloc(800);self.w(container,'Q',region);self.w(container+8,'I',100)
  for i,id in enumerate(ids):
   if id not in self.biome_ptrs:
    p=self.alloc(64);self.w(p+0x10,'f',self.props[id][0]);self.w(p+0x14,'f',self.props[id][1]);self.biome_ptrs[id]=p
   self.w(region+i*8,'Q',self.biome_ptrs[id])
  target=self.alloc(425*8);output=self.alloc(24);self.w(output,'Q',target);self.w(output+8,'I',425)
  self.call(0x507470,None,[P,I,I,I,P,P],[gen,cx*4,0,cz*4,container,output]);return list(struct.unpack('<425d',C.string_at(target,425*8)))

class TerrainOracle(DensityOracle):
 def __init__(self,executable,library,properties):
  super().__init__(executable,library,properties)
  # Bind the platform's vector TLS pool to private memory. The original vector
  # arithmetic and point-to-segment distance functions still execute unchanged.
  fs=C.c_uint64()
  if self.host.syscall(158,0x1003,C.byref(fs))!=0:raise RuntimeError('Cannot read FS base')
  tls=self.alloc(64);pool=self.alloc(16);vectors=self.alloc(24*1024)
  self.w(tls,'Q',pool);self.w(pool,'Q',vectors);self.game.w(0x1433e98,'I',0);self.game.w(0x134c3d8,'Q',(tls-fs.value)&0xffffffffffffffff)
  self.source=self.alloc(64);svt=self.alloc(128);self.w(self.source,'Q',svt)
  self.w(self.vt+0xc0,'Q',self.callback(P,[P],lambda level:self.source))
  def biome_region(source,container,x,z,width,height):
   if width!=10 or height!=10:raise ValueError('Unexpected biome input size')
   region=self.host.malloc(100*8);self.w(container,'Q',region);self.w(container+8,'I',100)
   for i,id in enumerate(self.region_ids):self.w(region+i*8,'Q',self.biome_ptr(id))
  self.w(svt+0x20,'Q',self.callback(None,[P,P,I,I,I,I],biome_region))
 def biome_ptr(self,id):
  if id not in self.biome_ptrs:
   p=self.alloc(64);self.w(p+0x10,'f',self.props[id][0]);self.w(p+0x14,'f',self.props[id][1]);self.biome_ptrs[id]=p
  return self.biome_ptrs[id]
 def prepare(self,gen,width,cx,cz,ids):
  self.region_ids=ids;self.w(gen+0xc,'I',width//16);self.host.memset(self.buffer,0,65536)
  self.call(0x506dd0,None,[P,I,I,P],[gen,cx,cz,self.container])
  return bytes(2 if n==9 else n for n in self.output())
 def surface_noise(self,gen,cx,cz):
  wrapper=self.read(gen+0x60,'Q');octaves=self.read(wrapper+8,'Q');count=self.read(wrapper+0x10,'I');out=self.alloc(256*8)
  weight=self.game.read(0xea06c0,'d');amplitude=self.game.read(0xea06c8,'d');step=self.game.read(0xea06d0,'d');gain=self.game.read(0xea06d8,'d')
  for i in range(count):
   simplex=self.read(octaves+i*8,'Q')
   self.call(0x627bf0,None,[D,D,D,D,D,P,P,I,I,I],[cx*16.,cz*16.,step,step,amplitude/weight,simplex,out,256,16,16]);weight*=gain
  return list(struct.unpack('<256d',C.string_at(out,256*8)))
