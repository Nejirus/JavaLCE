"""Original desert wells, constructor, sand predicate and slab metadata.

Enum-property/StateDefinition storage and half-enum pointer identities are
controlled. The original sand/slab variant initializers and predicate execute.
"""
from native_tree_selection import TreeSelectionOracle,C,P,U,I


class WellOracle(TreeSelectionOracle):
    def __init__(self,executable,library):
        super().__init__(executable,library)
        for nid in ['Noj9PsJrsa8','QJ5xVfKkni0']:self.game.bind(nid,P(self.lib.base+self.lib.exports[nid]))
        for pc in [0x684e50,0x5c35e0]:self.patch(pc,P,[P,P,P,P],lambda *args:self.alloc(16))
        self.call(0x684820,None,[],[]);self.call(0x5c33b0,None,[],[])
        self.properties['slab_variant']=self.game.read(0x1432998,'Q');self.properties['sand_variant']=self.game.read(0x142f8a8,'Q')
        self.properties['slab_half']=self.alloc(16);self.game.w(0x1416d20,'Q',self.properties['slab_half'])
        self.slab_enums=[self.game.read(0x14329b0+i*8,'Q') for i in range(8)]
        self.sand_enums=[self.game.read(0x142f8c0+i*8,'Q') for i in range(2)]
        self.slab_boxes=[self.enum_box(v) for v in self.slab_enums];self.sand_boxes=[self.enum_box(v) for v in self.sand_enums]
        self.slab_halves=[self.alloc(48),self.alloc(48)]
        for address,value in zip([0x1416d50,0x1416d58],self.slab_halves):self.game.w(address,'Q',value)
        self.slab_half_boxes=[self.enum_box(v) for v in self.slab_halves]
        # One-property StateDefinition supplied to the original predicate map.
        definition=self.alloc(64);properties=self.alloc(8);self.w(definition,'Q',self.blocks[12]);self.w(properties,'Q',self.properties['sand_variant'])
        self.w(definition+0x10,'Q',properties);self.w(definition+0x18,'Q',properties+8)
        sandvt=self.alloc(0x500);C.memmove(sandvt,self.blockvt,512);self.w(sandvt+0x480,'Q',self.callback(P,[P],lambda block:definition));self.w(self.blocks[12],'Q',sandvt)
        slabvt=self.alloc(0x500);C.memmove(slabvt,self.blockvt,512);self.w(slabvt+0x4e0,'Q',self.callback(I,[P],lambda block:False));self.w(self.blocks[44],'Q',slabvt)
        for address,tile in [(0x140b2f0,44),(0x140b290,24)]:self.game.w(address,'Q',self.blocks[tile])
        self.call(0x1fb370,None,[],[])
        self.variant_inputs={'sand':[self.read(v+8,'I') for v in self.sand_enums],'slabs':[self.read(v+8,'I') for v in self.slab_enums]}
        self.configurations=[]
        for meta in range(16):
            self.w(self.blocks[44]+0x190,'Q',self.state((44<<4)|meta));self.call(0x1fb400,None,[P],[self.feature])
            self.configurations.append({'slab_base_meta':meta,'states':[self.unpacked(self.read(self.feature+o,'Q')) for o in [0x10,0x18,0x20]],
                                        'slab_native_data':self.call(0x685550,I,[P,P],[self.blocks[44],self.read(self.feature+0x10,'Q')])})
        self.predicate_cases=[]
        for tile in [0,1,2,3,8,9,12,13,24,44]:
            for meta in ([0,1] if tile==12 else [0]):
                result=bool(self.call(0xd0ff0,I,[P,P],[self.game.read(0x1413c18,'Q'),self.state((tile<<4)|meta)])&255)
                self.predicate_cases.append({'state':tile|(meta<<16),'result':result})

    def enum_box(self,enum):
        box=self.alloc(32);self.w(box,'Q',self.read(self.type_boxes[0],'Q'));self.w(box+16,'Q',enum);return box

    def property(self,state,prop):
        meta=self.read(state+8,'I')&15
        if prop==self.properties.get('slab_variant'):return self.slab_boxes[meta&7]
        if prop==self.properties.get('slab_half'):return self.slab_half_boxes[0 if meta&8 else 1]
        if prop==self.properties.get('sand_variant'):return self.sand_boxes[meta&1]
        return super().property(state,prop)

    def set_property(self,state,prop,boxed):
        packed=self.read(state+8,'I');enum=self.read(boxed+16,'Q')
        if prop==self.properties.get('slab_variant'):return self.state((packed&~7)|self.read(enum+8,'I'))
        if prop==self.properties.get('slab_half'):return self.state((packed&~8)|(8 if enum==self.slab_halves[0] else 0))
        return super().set_property(state,prop,boxed)

    def initial(self,x,y,z):
        if self.profile=='well_context':return self.rule_blocks.get((x,y,z),0)
        ox,oy,oz=self.position
        if y==oy+self.surface_offset:return {'red_sand':12|(1<<16),'stone':1,'dirt':3}.get(self.profile,12)
        if y>oy+self.surface_offset:return 0
        if self.profile=='empty_below':return 0
        if self.profile=='holes' and (x-ox,y-oy-self.surface_offset,z-oz) in self.holes:return 0
        return 1

    def well(self,seed,position,profile,surface_offset=0,holes=()):
        self.surface_offset=surface_offset;self.holes={tuple(p) for p in holes}
        self.reset_world(profile,position[1],position);self.fluid_queries=[];self.support_queries=[]
        pos=self.alloc(12)
        for i,value in enumerate(position):self.w(pos+4*i,'i',value)
        self.call(0x596c90,None,[P,U],[self.random,seed&0xffffffffffffffff])
        result=bool(self.call(0x1fa530,I,[P,P,P,P],[self.feature,self.world,self.random,pos])&255)
        return {'seed':seed,'position':position,'profile':profile,'surface_offset':surface_offset,'holes':list(holes),'result':result,**self.world_result(),
                'fluid_queries':self.fluid_queries,'light_queries':self.light_queries,'random_after':C.c_int64(self.call(0x5970e0,U,[P],[self.random])).value}
