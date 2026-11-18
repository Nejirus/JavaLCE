"""Original bamboo, podzol and berry kernels with controlled voxel storage.

Original block constructors, property lists, property-range construction and
placement rules run. StateDefinition/canonical state storage and materials are
supplied inputs, as in the tree captures.
"""
from native_tree_selection import TreeSelectionOracle,C,P,U,I
from pathlib import Path
import json


class TallVegetationOracle(TreeSelectionOracle):
    def __init__(self,executable,library):
        super().__init__(executable,library)
        registry=json.loads(Path(__file__).with_name('block-registry-native.json').read_text())
        ids={r['name']:r['id'] for r in registry['registrations']};ids['air']=0
        for tile in [ids['bamboo'],ids['bamboo_sapling'],ids['sweet_berry_bush'],ids['scaffolding']]:
            block=self.alloc(1024);self.blocks[tile]=block;self.w(block,'Q',self.blockvt);self.w(block+8,'I',tile)
            self.w(block+0x170,'Q',self.read(self.blocks[31]+0x170,'Q'));self.w(block+0x188,'Q',block)
        for alias in registry['aliases']:
            if ids[alias['name']] in self.blocks:self.game.w(int(alias['alias'],16),'Q',self.blocks[ids[alias['name']]])
        # The original Block::getId still calls its registry lookup virtual.
        block_registry=self.game.read(0x140ad68,'Q');registry_vt=self.alloc(64)
        self.w(block_registry,'Q',registry_vt);self.w(registry_vt+0x38,'Q',self.callback(I,[P,P],lambda registry,block:self.read(block+8,'I')))
        for nid in ['Noj9PsJrsa8','QJ5xVfKkni0']:self.game.bind(nid,P(self.lib.base+self.lib.exports[nid]))
        self.properties['bamboo_leaves']=self.alloc(16);self.properties['bamboo_age']=self.alloc(16)
        self.game.w(0x1409750,'Q',self.properties['bamboo_leaves']);self.game.w(0x1409758,'Q',self.properties['bamboo_age'])
        # 0x707944..0x707976 constructs the berry AGE property with bounds 0,3.
        # Execute the original property constructor, not a supplied RNG bound.
        if C.string_at(self.game.base+0x707944,10)!=b'\x31\xf6\xba\x03\x00\x00\x00\x48\x89\xdf':raise ValueError('Berry property bounds changed')
        name=self.alloc(40);self.w(name+32,'Q',7);text=C.create_string_buffer('age'.encode('utf-16le')+b'\0\0');self.keep.append(text)
        self.call(0x67840,None,[P,P,U],[name,C.addressof(text),3])
        self.properties['berry_age']=self.call(0x37a180,P,[P,I,I],[name,0,3]);self.game.w(0x14332d0,'Q',self.properties['berry_age'])
        age=self.properties['berry_age'];agevt=self.read(age,'Q')
        self.berry_age_count=self.call(self.read(agevt+0x40,'Q')-self.game.base,I,[P],[age])
        self.w(self.statevt+0x148,'Q',self.callback(I,[P,P],self.has_property))
        self.w(self.vt+0x90,'Q',self.callback(P,[P,P],lambda level,pos:self.blocks[self.get_block(level,*self.xyz(pos))>>4]))
        self.patch(0x91fb0,None,[P,P],lambda *args:None)
        self.patch(0x170,P,[C.c_double]*6,lambda *args:self.alloc(48))
        self.patch(0xcf850,None,[P,P,P,I],self.definition)
        self.definitions={}
        for tile,pc in [(319,0x3ea00),(363,0x6cc260)]:
            result=self.call(pc,P,[P],[self.blocks[tile]]);self.host.free(result)
        self.default_cases=[]
        for meta in range(16):
            self.base_meta=meta
            for tile,pc in [(319,0x3e8c0),(363,0x6cb170)]:
                self.w(self.blocks[tile]+0x190,'Q',self.state((tile<<4)|meta))
                self.call(pc,None,[P],[self.blocks[tile]]);state=self.read(self.blocks[tile]+0x190,'Q')
                self.default_cases.append({'base':tile|(meta<<16),'state':self.unpacked(state),'native_data':self.call(0x40250 if tile==319 else 0x6cc380,I,[P,P],[self.blocks[tile],state])})
        self.data_cases=[{'state':tile|(meta<<16),'native_data':self.call(pc,I,[P,P],[self.blocks[tile],self.state((tile<<4)|meta)])} for tile,pc,metas in [(319,0x40250,[0,1,2,8,9,10]),(363,0x6cc380,range(4))] for meta in metas]
        self.call(0x1722d0,None,[P,P],[self.feature,self.blocks[363]])

    def definition(self,obj,block,properties,count):
        keys=[self.read(properties+8*i,'Q') for i in range(count)];tile=self.read(block+8,'I')
        self.definitions[tile]=keys

    def property(self,state,prop):
        meta=self.read(state+8,'I')&15
        if prop==self.properties.get('bamboo_leaves'):return self.boxes[meta&3]
        if prop==self.properties.get('bamboo_age'):return self.boxes[(meta>>3)&1]
        if prop==self.properties.get('berry_age'):return self.boxes[meta&3]
        return super().property(state,prop)

    def set_property(self,state,prop,boxed):
        packed=self.read(state+8,'I');value=self.read(boxed+16,'I')
        if prop==self.properties.get('bamboo_leaves'):return self.state((packed&~15)|(packed&8)|value)
        if prop==self.properties.get('bamboo_age'):return self.state((packed&~15)|(packed&3)|(value<<3))
        if prop==self.properties.get('berry_age'):return self.state((packed&~15)|value)
        return super().set_property(state,prop,boxed)

    def has_property(self,state,prop):
        result=prop in self.definitions[self.read(state+8,'I')>>4]
        self.property_queries.append([self.unpacked(state),'berry_age' if prop==self.properties['berry_age'] else 'horizontal_facing',result]);return result

    def initial(self,x,y,z):
        if self.profile=='vegetation_rules':return self.rule_blocks.get((x,y,z),0)
        ox,oy,oz=self.position
        if self.profile=='roof' and x==ox and z==oz and y==oy+self.roof:return 1
        if self.profile=='mixed_soil' and y==oy-1:return [2,3,3|(2<<16),12,13,110,1,60][(x+z)%8]
        if self.profile=='empty':return 0
        if self.profile=='replaceable' and y==oy and (x+z)%3==0:return 31|(2<<16)
        return super().initial(x,y,z)

    def begin(self,profile,position):
        self.reset_world(profile,position[1],position);self.fluid_queries=[];self.support_queries=[];self.property_queries=[]
        pos=self.alloc(12)
        for i,value in enumerate(position):self.w(pos+4*i,'i',value)
        return pos

    def vegetation(self,kind,seed,position,profile,roof=256):
        pos=self.begin(profile,position);self.roof=roof
        self.call(0x596c90,None,[P,U],[self.random,seed&0xffffffffffffffff])
        self.w(self.feature,'Q',self.treevt)
        self.w(self.feature+8,'B',0)
        result=bool(self.call({'bamboo':0x40e40,'podzol':0x5548a0,'berries':0x172300}[kind],I,[P,P,P,P],[self.feature,self.world,self.random,pos])&255)
        return {'kind':kind,'seed':seed,'position':position,'profile':profile,'roof':roof,'result':result,**self.world_result(),'property_queries':self.property_queries,
                'fluid_queries':self.fluid_queries,'light_queries':self.light_queries,'random_after':C.c_int64(self.call(0x5970e0,U,[P],[self.random])).value}

    def rule(self,kind,current,below):
        pos=self.begin('vegetation_rules',[0,64,0]);self.rule_blocks={(0,64,0):current,(0,63,0):below}
        tile=319 if kind=='bamboo' else 363
        result=bool(self.call(0x3fb40 if tile==319 else 0x6cb360,I,[P,P,P],[self.blocks[tile],self.world,pos])&255)
        return {'kind':kind,'position':[0,64,0],'profile':'vegetation_rules','current':current,'below':below,'result':result,**self.world_result(),
                'fluid_queries':self.fluid_queries,'light_queries':self.light_queries}
