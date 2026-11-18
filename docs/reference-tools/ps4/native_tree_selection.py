"""Original biome tree selectors, tree-state initializers and constructor stores.
Block base construction/StateDefinition storage is controlled; original final
default-property assignments, species enum and cached feature states execute.
"""
from native_big_trees import BigTreeOracle,C,P,U,I


class TreeSelectionOracle(BigTreeOracle):
    SELECTORS={'DEFAULT':0x84eb0,'HILLS':0x29fdb0,'FOREST':0x2f6950,'MUTATED_FOREST':0x4a2200,
               'JUNGLE':0x3c4ea0,'BAMBOO_JUNGLE':0x41720,'SAVANNA':0x5c6a30,
               'PLAINS':0x53d770,'SWAMP':0x6ca0e0,'TAIGA':0x6d1790}
    EXTRA_SELECTORS={'ICE':0x373dc0,'MESA':0x46a9d0}
    TYPES={0x12e6598:'BASIC',0x1216130:'BIG_OAK',0x1216470:'BIRCH',0x12a1950:'PINE',0x12d0c58:'SPRUCE',
           0x1261638:'GROUND_BUSH',0x12de058:'SWAMP',0x12b7030:'SAVANNA',0x12b43b8:'ROOF',
           0x127d598:'MEGA_PINE',0x127d538:'MEGA_JUNGLE'}
    STATE_INITIALIZERS=[(0x6f5980,[0x1433970,0x1433978]),(0x8f1d0,[0x140acb0,0x140acb8]),
                        (0x534400,[0x142e2c0,0x142e2c8]),(0x672e00,[0x14320a8,0x14320b0]),
                        (0x5bfa80,[0x142f830,0x142f838]),(0x5c6ce0,[0x142fb38,0x142fb40]),
                        (0x6ca3f0,[0x14332c0,0x14332c8]),(0x4515d0,[0x14192a8,0x14192b0,0x14192b8]),
                        (0x3c48d0,[0x14181c0,0x14181c8,0x14181d0]),(0x41170,[0x1409778,0x1409780,0x1409788])]

    def __init__(self,executable,library):
        super().__init__(executable,library)
        self.game.w(0x140ae68,'Q',self.blocks[161])
        for name,address in [('new_log_wood',0x14199a0),('new_leaf_wood',0x1419970)]:
            self.properties[name]=self.alloc(16)
            self.game.w(address,'Q',self.properties[name])
        self.patch(0x4b9c40,P,[P,P,P,P],lambda *args:self.alloc(64))
        self.call(0x53d910,None,[],[])
        self.wood_types=[self.game.read(a,'Q') for a in [0x142dc30,0x142dc38,0x142dc40,0x142dc48,0x142e618,0x142dc78]]
        self.wood_inputs=[{'alias':hex(a),'id':self.read(t+8,'i')} for a,t in zip([0x142dc30,0x142dc38,0x142dc40,0x142dc48,0x142e618,0x142dc78],self.wood_types)]
        self.wood_boxes=[]
        for enum in self.wood_types:
            box=self.alloc(32)
            self.w(box,'Q',self.read(self.type_boxes[0],'Q'))
            self.w(box+16,'Q',enum)
            self.wood_boxes.append(box)
        for pc in [0x3e5ce0,0x3e5d60,0x438ff0,0x924c0]:self.patch(pc,None,[P],lambda obj:None)
        self.patch(0x7a050,P,[P],lambda block:self.state((self.read(block+8,'I')<<4)|self.base_meta))
        self.patch(0x7a040,None,[P,P],lambda block,state:self.w(block+0x190,'Q',state))
        self.patch(0x7a4d0,P,[P],lambda block:self.read(block+0x190,'Q') or self.state(self.read(block+8,'I')<<4))
        self.default_cases=[]
        for meta in range(16):
            self.base_meta=meta
            for tile,pc in [(17,0x503330),(18,0x502590),(162,0x4baa60),(161,0x4b9e40)]:
                self.w(self.blocks[tile]+0x188,'Q',self.blocks[tile])
                self.call(pc,None,[P],[self.blocks[tile]])
                state=self.read(self.blocks[tile]+0x190,'Q')
                self.default_cases.append({'base':tile|(meta<<16),'state':self.unpacked(state),'native_data':self.call(self.read(self.read(self.blocks[tile],'Q')+0x50,'Q')-self.game.base,I,[P,P],[self.blocks[tile],state])})
        self.initialized_states=[]
        for pc,globals in self.STATE_INITIALIZERS:
            self.call(pc,None,[],[])
            for address in globals:self.initialized_states.append({'initializer':hex(pc),'alias':hex(address),'state':self.unpacked(self.game.read(address,'Q'))})

    def property(self,state,prop):
        packed=self.read(state+8,'I');meta=packed&15
        if prop in [self.properties.get('wood'),self.properties.get('leaf_wood')]:return self.wood_boxes[meta&3]
        if prop in [self.properties.get('new_log_wood'),self.properties.get('new_leaf_wood')]:return self.wood_boxes[4+(meta&1)]
        return super().property(state,prop)

    def set_property(self,state,prop,boxed):
        packed=self.read(state+8,'I')
        if prop in [self.properties.get('wood'),self.properties.get('leaf_wood')]:
            return self.state((packed&~3)|self.read(self.read(boxed+16,'Q')+8,'I'))
        if prop in [self.properties.get('new_log_wood'),self.properties.get('new_leaf_wood')]:
            return self.state((packed&~3)|(self.read(self.read(boxed+16,'Q')+8,'I')-4))
        if prop==self.properties.get('decayable'):return self.state(packed&~4 if self.read(boxed+16,'B') else packed|4)
        if prop==self.properties.get('dirt'):return self.state((packed&~15)|self.read(self.read(boxed+16,'Q')+8,'I'))
        return super().set_property(state,prop,boxed)

    def unpacked(self,state):
        packed=self.read(state+8,'I')
        return (packed>>4)|((packed&15)<<16)

    def descriptor(self,feature):
        kind=self.TYPES[self.read(feature,'Q')-self.game.base]
        log=self.read(feature+(0x30 if kind=='GROUND_BUSH' else 0x18),'Q')
        leaves=self.read(feature+(0x28 if kind=='GROUND_BUSH' else 0x20),'Q')
        return {'kind':kind,'base_height':self.read(feature+0xc,'i'),'extra_range':self.read(feature+0x28,'i') if kind in ['MEGA_PINE','MEGA_JUNGLE'] else 0,
                'log':self.unpacked(log),'leaves':self.unpacked(leaves),'jungle':bool(self.read(feature+0x10,'B')),
                'tall':bool(self.read(feature+(0x2c if kind=='MEGA_PINE' else 0x28),'B')) if kind in ['BIRCH','MEGA_PINE'] else False,
                'updates':bool(self.read(feature+8,'B'))}

    def selection(self,kind,variant,seed,position=None,profile='grass'):
        self.w(self.biome+0x1d0,'i',variant)
        self.call(0x596c90,None,[P,U],[self.random,seed&0xffffffffffffffff])
        feature=self.call({**self.SELECTORS,**self.EXTRA_SELECTORS}[kind],P,[P,P],[self.biome,self.random])
        description=self.descriptor(feature)
        result={'biome':kind,'variant':variant,'seed':seed,'selection':description}
        if position is not None:
            self.reset_world(profile,position[1],position);self.support_queries=[];self.fluid_queries=[]
            pos=self.alloc(12)
            for i,value in enumerate(position):self.w(pos+4*i,'i',value)
            vt=self.read(feature,'Q')
            self.call(self.read(vt+0x18,'Q')-self.game.base,None,[P],[feature])
            placed=bool(self.call(self.read(vt+0x10,'Q')-self.game.base,I,[P,P,P,P],[feature,self.world,self.random,pos])&255)
            if placed:self.call(self.read(vt+0x28,'Q')-self.game.base,None,[P,P,P,P],[feature,self.world,self.random,pos])
            result.update({'position':position,'profile':profile,'result':placed,**self.world_result(),'support_queries':self.support_queries,'fluid_queries':self.fluid_queries,'light_queries':self.light_queries})
        result['random_after']=C.c_int64(self.call(0x5970e0,U,[P],[self.random])).value
        if description['kind']=='BIG_OAK':
            begin=self.read(feature+0x98,'Q')
            if begin:self.host.free(begin)
        self.host.free(feature)
        return result
