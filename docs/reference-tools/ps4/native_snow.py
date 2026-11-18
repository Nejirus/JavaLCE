"""Original snow rules, placement, layer constructor/default/metadata and state
increment over explicit canonical defaults/materials, climate, game rule and
cached/uncached chunk heights. Original Level block lookup executes. Full material/biome
registry initialization, compressed section occupancy and physics are supplied.
"""
import json,hashlib
from pathlib import Path
from native_freezing import FreezingOracle
from native_corals import C,P,U,I
class SnowOracle(FreezingOracle):
 def __init__(self,executable,library):
  super().__init__(executable,library)
  self.snow_property=self.alloc(16);self.game.w(0x1430430,'Q',self.snow_property)
  self.patch(0x2a0f50,P,[P,P],lambda block,material:block)
  self.patch(0x924c0,None,[P],lambda block:None)
  self.patch(0x7a050,P,[P],lambda definition:self.state(78<<4))
  # Restore original default-state getter; the inherited feature harness has
  # supplied getter callbacks for its tree-specific cached-state inputs.
  segment=next(s for s in self.game.ph if s[0]==1 and s[3]<=0x7a4d0<s[3]+s[5]);offset=segment[2]+0x7a4d0-segment[3]
  C.memmove(self.game.base+0x7a4d0,bytes(self.game.file[offset:offset+12]),12)
  for tile,block in self.blocks.items():self.w(block+0x190,'Q',self.state(tile<<4))
  self.w(self.statevt,'Q',self.callback(P,[P],lambda state:self.read(self.blocks[self.read(state+8,'I')>>4]+0x170,'Q')))
  self.w(self.matvt+0x30,'Q',self.callback(I,[P],lambda mat:self.read(mat+8,'I')==1))
  self.call(0x638810,None,[P],[self.blocks[78]])
  self.snow_default=self.unpacked(self.read(self.blocks[78]+0x190,'Q'))
  self.snow_data=[self.call(0x6394a0,I,[P,P],[self.blocks[78],self.state((78<<4)|meta)]) for meta in range(8)]
  registry=self.game.read(0x140ad68,'Q')
  for tile,block in self.blocks.items():self.w(registry+0x150+8*tile,'Q',block)
  source=json.loads(Path(__file__).with_name('block-registry-native.json').read_text());ids={r['name']:r['id'] for r in source['registrations']};ids['air']=0
  for alias in source['aliases']:self.game.w(int(alias['alias'],16),'Q',self.blocks[ids[alias['name']]])
  self.w(self.vt+0x50,'Q',self.callback(I,[P,I,I,I],lambda world,x,y,z:self.get_block(world,x,y,z)>>4))
  self.w(self.vt+0x90,'Q',self.game.base+0x3f2e90)
  self.level_data=self.alloc(64);data_vt=self.alloc(0x220);self.w(self.level_data,'Q',data_vt);self.w(self.world+0x218,'Q',self.level_data)
  self.w(data_vt+0x218,'Q',self.callback(P,[P],lambda data:self.rules));self.patch(0x2f4f60,I,[P,I],self.snow_rule)
  self.chunk=self.alloc(0x300);chunkvt=self.alloc(0x200);self.w(self.chunk,'Q',chunkvt);self.w(self.chunk+0x20,'Q',self.alloc(16))
  self.w(chunkvt+0xf0,'Q',self.callback(P,[P,P],lambda chunk,pos:self.state(self.get_block(self.world,*self.xyz(pos)))))
  self.w(chunkvt+0xf8,'Q',self.callback(P,[P,P],lambda chunk,pos:self.state(0)))
  self.patch(0x223f10,I,[P,I,P],self.section_empty)
  source=self.read(self.world+0x1f8,'Q');self.w(self.read(source,'Q')+0x58,'Q',self.callback(P,[P,I,I],self.snow_chunk))
 def set_block(self,world,x,y,z,tile,meta,flags):
  accepted=self.write_mode=='all';self.write_attempts.append([x,y,z,tile|(meta<<16),flags,accepted])
  return super().set_block(world,x,y,z,tile,meta,flags) if accepted else 0
 def property(self,state,prop):
  if prop==getattr(self,'snow_property',-1):return self.boxes[(self.read(state+8,'I')&7)+1]
  return super().property(state,prop)
 def set_property(self,state,prop,boxed):
  if prop==getattr(self,'snow_property',-1):return self.state((78<<4)|(self.read(boxed+16,'I')-1))
  return super().set_property(state,prop,boxed)
 def initial(self,x,y,z):return self.voxels.get((x,y,z),0)
 def snow_rule(self,rules,key):
  if key!=18:raise ValueError('Unexpected original snow rule key')
  self.events.append(['rule',key,self.snow_enabled]);return self.snow_enabled
 def section_empty(self,data,y,output):
  if output:raise ValueError('Unexpected nonnull snow height output')
  empty=self.section_mask&(1<<(y//16))==0;self.events.append(['section',y,bool(empty)]);return empty
 def snow_chunk(self,source,x,z):
  self.events.append(['chunk',x,z,self.chunk_present]);return self.chunk if self.chunk_present else 0
 def snow(self,kind,position,blocks,temperature_bits=0,light=0,cap=0.,check_height=True,accumulate=True,enabled=True,chunk_present=True,cached_height=64,write_mode='all',default_snow_meta=0,section_mask=0):
  self.w(self.blocks[78]+0x190,'Q',self.state((78<<4)|default_snow_meta));self.section_mask=section_mask;self.voxels={tuple(b[:3]):b[3] for b in blocks};self.temperature_bits=temperature_bits;self.light_value=light;self.snow_enabled=enabled;self.chunk_present=chunk_present;self.write_mode=write_mode;self.write_attempts=[]
  pos=self.begin_coral('snow',position,'all','all',self.HORIZONTAL);self.events=[];self.w(self.biome+0x1bc,'f',cap)
  for i in range(256):self.w(self.chunk+0x29+i,'B',cached_height)
  out=self.alloc(4)
  state_result=None
  if kind=='state':state_result=self.unpacked(self.call(0x6386a0,P,[P,P,I,I],[self.world,pos,1,int(accumulate)]));result=True
  elif kind=='rule':result=bool(self.call(0x3fe7a0,I,[P,P,I,I,P],[self.world,pos,int(check_height),int(accumulate),out])&255)
  else:result=bool(self.call(0x3fede0,I,[P,P,I,I],[self.world,pos,int(check_height),int(accumulate)])&255)
  return {'kind':kind,'section_mask':section_mask,'cache_sha256':hashlib.sha256(C.string_at(self.chunk+0x29,256)).hexdigest(),'default_snow_meta':default_snow_meta,'position':position,'blocks':blocks,'temperature_bits':f'{temperature_bits:08x}','light':light,'cap':cap,'check_height':check_height,'accumulate':accumulate,'enabled':enabled,'chunk_present':chunk_present,'cached_height':cached_height,'result':result,'state_result':state_result,'write_mode':write_mode,'write_attempts':self.write_attempts,'maximum_depth':self.call(0x852c0,I,[P],[self.biome]),'depth':self.read(out,'i'),'events':self.events,**self.world_result()}
