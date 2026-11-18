"""Original Level freezing rule over explicit canonical/temperature/light inputs."""
import struct
from native_corals import CoralOracle,C,P,U,I
class FreezingOracle(CoralOracle):
 def __init__(self,executable,library):
  super().__init__(executable,library)
  self.bvt=self.alloc(256);self.w(self.biome,'Q',self.bvt)
  self.w(self.vt+0xb8,'Q',self.callback(P,[P,P],self.biome_at))
  self.w(self.bvt+0x50,'Q',self.callback(C.c_float,[P,P],self.temperature))
  self.w(self.vt+0x20,'Q',self.callback(I,[P,I,P],self.light))
 def biome_at(self,world,pos):
  self.events.append(['biome',*self.xyz(pos)]);return self.biome
 def initial(self,x,y,z):return self.primary if [x,y,z]==self.position else 0
 def temperature(self,biome,pos):
  self.events.append(['temperature',*self.xyz(pos),f'{self.temperature_bits:08x}']);return struct.unpack('<f',struct.pack('<I',self.temperature_bits))[0]
 def light(self,world,kind,pos):
  self.events.append(['light',kind,*self.xyz(pos),self.light_value]);return self.light_value
 def freeze(self,position,primary,temperature_bits,light):
  self.primary=primary;self.temperature_bits=temperature_bits;self.light_value=light
  pos=self.begin_coral('freeze',position,'all','all',self.HORIZONTAL);self.events=[]
  result=bool(self.call(0x3dcbc0,I,[P,P],[self.world,pos])&255)
  return {'position':position,'state':primary,'temperature_bits':f'{temperature_bits:08x}','light':light,'result':result,'events':self.events,**self.world_result()}
