"""Original aquatic kernels/rules over explicit canonical world boundaries.

Original growth-state constructors/getData and placement rules execute. Block
defaults/property storage, solid-blocking/slab/byte10 classifications, effective
fluid lookups, collision placement and successful writes are supplied inputs.
"""
from native_wells import WellOracle,C,P,U,I
from pathlib import Path
import json


class AquaticOracle(WellOracle):
    SLABS={44,126,182,205,312}
    def __init__(self,executable,library):
        super().__init__(executable,library)
        registry=json.loads(Path(__file__).with_name('block-registry-native.json').read_text())
        ids={r['name']:r['id'] for r in registry['registrations']};ids['air']=0
        for tile in sorted(set(ids.values())-self.blocks.keys()):
            block=self.alloc(1024);self.blocks[tile]=block;self.w(block,'Q',self.blockvt);self.w(block+8,'I',tile)
            self.w(block+0x170,'Q',self.read(self.blocks[31 if tile in [258,270,271] else 1]+0x170,'Q'));self.w(block+0x188,'Q',block)
        for alias in registry['aliases']:self.game.w(int(alias['alias'],16),'Q',self.blocks[ids[alias['name']]])
        generic=self.alloc(0x510);C.memmove(generic,self.blockvt,512)
        self.w(generic+0x148,'Q',self.callback(I,[P],lambda block:self.read(block+8,'H') in self.SLABS))
        for block in self.blocks.values():self.w(block,'Q',generic)
        self.patch(0x138d90,None,[P,P],lambda *args:None)
        self.patch(0x91fb0,None,[P,P],lambda *args:None)
        self.property_counts={}
        # Bounds copied from the original argument stores, and passed through
        # the original IntegerProperty constructors. No grouped startup runs.
        for key,alias,lo,hi,pc,name_text in [('kelp_age',0x14182a8,0,15,0x379b40,'age'),('sea_grass',0x142fc68,0,2,0x37a180,'type'),('pickle_count',0x142fc88,0,3,0x37a180,'count'),('pickle_flange',0x142fc90,0,1,0x37a180,'flange')]:
            name=self.alloc(40);self.w(name+32,'Q',7);encoded=C.create_string_buffer(name_text.encode('utf-16le')+b'\0\0');self.keep.append(encoded)
            self.call(0x67840,None,[P,P,U],[name,C.addressof(encoded),len(name_text)])
            if pc==0x37a180:prop=self.call(pc,P,[P,I,I],[name,lo,hi])
            else:
                prop=self.alloc(96);self.call(pc,None,[P,P,I,I],[prop,name,lo,hi])
            self.properties[key]=prop;self.game.w(alias,'Q',prop)
            self.property_counts[key]=self.call(self.read(self.read(prop,'Q')+0x40,'Q')-self.game.base,I,[P],[prop])
        self.default_cases=[]
        for meta in range(16):
            self.base_meta=meta
            for tile,pc in [(258,0x3c5370),(270,0x5f5210),(271,0x5f6e60)]:
                self.w(self.blocks[tile]+0x190,'Q',self.state((tile<<4)|meta));self.call(pc,None,[P],[self.blocks[tile]])
                state=self.read(self.blocks[tile]+0x190,'Q')
                self.default_cases.append({'base':tile|(meta<<16),'state':self.unpacked(state),'native_data':self.call(self.read(self.read(self.blocks[tile],'Q')+0x50,'Q')-self.game.base,I,[P,P],[self.blocks[tile],state])})
        # SeaPickle's constructor leaves the StateDefinition default untouched;
        # its definition's default storage is an explicit input, not a claim
        # that all supplied metadata values are valid defaults.
        self.w(self.blocks[271]+0x190,'Q',self.state(271<<4))
        self.data_cases=[{'state':tile|(meta<<16),'native_data':self.call(pc,I,[P,P],[self.blocks[tile],self.state((tile<<4)|meta)])} for tile,pc,metas in [(258,0x3c5df0,range(16)),(270,0x5f5710,range(3)),(271,0x5f7810,list(range(4))+list(range(8,12)))] for meta in metas]
        self.call(0x3c6400,None,[P],[self.feature]);self.kelp_cached=[self.unpacked(self.read(self.feature+0x10+8*i,'Q')) for i in range(16)]
        self.w(self.statevt+0x90,'Q',self.callback(I,[P],lambda state:self.solid_blocking_state(self.unpacked(state))))
        self.w(self.vt+0x50,'Q',self.callback(I,[P,I,I,I],lambda level,x,y,z:self.get_block(level,x,y,z)>>4))
        self.w(self.vt+0x90,'Q',self.callback(P,[P,P],lambda level,pos:self.blocks[self.get_block(level,*self.xyz(pos))>>4]))
        self.w(self.vt+0xb0,'Q',self.game.base+0x3eecb0)
        self.w(self.vt+0x100,'Q',self.callback(P,[P,P],lambda level,pos:self.blocks[self.effective(*self.xyz(pos))&65535]))
        self.w(self.vt+0x108,'Q',self.callback(P,[P,P],lambda level,pos:self.canonical_state(self.effective(*self.xyz(pos)))))
        self.patch(0x3fc310,I,[P,P,I,I],self.can_place)

    def canonical_state(self,value):return self.state(((value&65535)<<4)|(value>>16))
    def property(self,state,prop):
        meta=self.read(state+8,'I')&15
        if prop in [self.properties.get(k) for k in ['kelp_age','sea_grass']]:return self.boxes[meta]
        if prop==self.properties.get('pickle_count'):return self.boxes[meta&7]
        if prop==self.properties.get('pickle_flange'):return self.boxes[(meta>>3)&1]
        return super().property(state,prop)
    def set_property(self,state,prop,boxed):
        packed=self.read(state+8,'I');value=self.read(boxed+16,'I')
        if prop in [self.properties.get(k) for k in ['kelp_age','sea_grass']]:return self.state((packed&~15)|value)
        if prop==self.properties.get('pickle_count'):return self.state((packed&~7)|value)
        if prop==self.properties.get('pickle_flange'):return self.state((packed&~8)|(value<<3))
        return super().set_property(state,prop,boxed)
    def solid_blocking_state(self,state):
        if self.profile=='aquatic_rule':return self.rule_blocking
        return (state&65535) not in self.NONSOLID|{18,161,258,270,271,44,126,182,205,312}
    def initial(self,x,y,z):
        if self.profile=='aquatic_rule':return self.rule_blocks.get((x,y,z),0)
        ox,oy,oz=self.position
        if y<0 or y>=256 or self.profile=='air':return 0
        if self.profile=='column_kelp' and (x,y,z)==(ox,oy-1,oz):return 258|(14<<16)
        if self.profile=='column_pickle' and (x,y,z)==(ox,oy-1,oz):return 271
        floor=oy-4+((x*7+z*3)%5 if self.profile=='steps' else 0)
        if y<floor:return 1
        if y==floor:
            return {'ice_floor':79,'magma':213,'soul':88,'glass':20,'top_slabs':44|(8<<16),'bottom_slabs':44}.get(self.profile,[1,3,12,13,20,44|(8<<16),213,88][(x+z)%8] if self.profile=='mixed' else 1)
        if y>floor+(2 if self.profile=='shallow' else 24):return 0
        if self.profile=='flowing':return 8|(((x+z)%10)<<16)
        if self.profile=='waterlogged' and (x+z)%3==0:return 20
        return 9
    def effective(self,x,y,z):
        value=self.rule_effective if self.profile=='aquatic_rule' else self.changed.get((x,y,z),self.initial(x,y,z))
        if self.profile=='waterlogged' and value==20:value=9
        self.effective_queries.append([x,y,z,value]);return value
    def can_place(self,world,pos,tile,meta):
        result=self.placement=='all' or self.placement=='checker' and sum(self.xyz(pos))%3!=0
        self.placement_queries.append([*self.xyz(pos),tile|(meta<<16),bool(result)]);return result
    def begin(self,profile,position,placement):
        self.reset_world(profile,256,position);self.fluid_queries=[];self.effective_queries=[];self.placement_queries=[];self.placement=placement
        pos=self.alloc(12)
        for i,value in enumerate(position):self.w(pos+4*i,'i',value)
        return pos
    def result(self):return {**self.world_result(),'fluid_queries':self.fluid_queries,'light_queries':self.light_queries,'effective_queries':self.effective_queries,'placement_queries':self.placement_queries}
    def aquatic(self,kind,seed,position,profile,placement='all',client=False,busy=False,reentry=False):
        pos=self.begin(profile,position,placement);self.w(self.world+0x248,'B',int(client));self.w(self.world+0x220,'B',int(reentry))
        self.w(self.feature,'Q',self.treevt);self.w(self.feature+8,'B',0);self.w(self.feature+9,'B',int(busy))
        if kind=='kelp':self.call(0x3c6400,None,[P],[self.feature])
        self.call(0x596c90,None,[P,U],[self.random,seed&0xffffffffffffffff])
        result=bool(self.call({'kelp':0x3c64e0,'seagrass':0x5f6730,'pickles':0x5f8c60}[kind],I,[P,P,P,P],[self.feature,self.world,self.random,pos])&255)
        return {'kind':kind,'seed':seed,'position':position,'profile':profile,'placement':placement,'client':client,'busy':busy,'reentry':reentry,'busy_after':bool(self.read(self.feature+9,'B')),'result':result,**self.result(),'random_after':C.c_int64(self.call(0x5970e0,U,[P],[self.random])).value}
    def rule(self,kind,state,below,current,effective,blocking,placement,flag=False,skip=False,above=0):
        pos=self.begin('aquatic_rule',[0,64,0],placement);self.rule_blocks={(0,64,0):current,(0,63,0):below,(0,65,0):above};self.rule_effective=effective;self.rule_blocking=blocking
        block=self.blocks[below&65535];self.w(block+10,'B',int(flag))
        tile={'kelp':258,'seagrass':270,'pickles':271}[kind]
        try:
            pc={'kelp':0x3c59b0,'seagrass':0x5f58a0,'pickles':0x5f7310}[kind]
            result=bool(self.call(pc,I,[P,P,P,P,I],[self.blocks[tile],self.world,pos,self.canonical_state(state),int(skip)])&255)
        finally:self.w(block+10,'B',0)
        return {'kind':kind,'state':state,'below':below,'current':current,'effective':effective,'blocking':blocking,'placement':placement,'flag':flag,'skip':skip,'above':above,'profile':'aquatic_rule','position':[0,64,0],'result':result,**self.result()}
