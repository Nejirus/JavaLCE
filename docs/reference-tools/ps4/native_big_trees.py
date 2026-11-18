"""Original big oak constructor and geometry; no platform startup is executed."""
from native_trees import TreeOracle,C,P,U,I


class BigTreeOracle(TreeOracle):
    def __init__(self,executable,library):
        super().__init__(executable,library)
        self.game.w(0x1409f90,'Q',self.blocks[18])
        self.game.w(0x1418e40,'Q',self.log_axes[1])
        self.properties['leaf_wood']=self.alloc(16)
        self.game.w(0x142dbf8,'Q',self.properties['leaf_wood'])
        for name,address in [('decayable',0x1418790),('check_decay',0x1418798)]:
            self.properties[name]=self.alloc(16)
            self.game.w(address,'Q',self.properties[name])
        # Random's clock-based constructor is an unused input: place always
        # replaces all its fields with Random(populationRandom.nextLong()).
        self.patch(0x596c00,None,[P],lambda random:self.call(0x596c90,None,[P,U],[random,0]))
        for pc in [0xbe80b0,0xbe80c0]:self.patch(pc,None,[P],lambda *args:None)

    def property(self,state,prop):
        meta=self.read(state+8,'I')&15
        if prop==self.properties.get('leaf_wood'):return self.type_boxes[meta&3]
        if prop==self.properties.get('decayable'):return self.bool_boxes[int(not(meta&4))]
        if prop==self.properties.get('check_decay'):return self.bool_boxes[int(bool(meta&8))]
        return super().property(state,prop)

    def set_property(self,state,prop,boxed):
        if prop==self.properties.get('check_decay'):
            packed=self.read(state+8,'I')
            return self.state(packed|8 if self.read(boxed+16,'B') else packed&~8)
        return super().set_property(state,prop,boxed)

    def initial(self,x,y,z):
        ox,oy,oz=self.position
        if self.profile=='high_obstacle' and (x,y,z)==(ox,oy+7,oz):return 1
        if self.profile=='off_axis' and oy+3<=y<oy+9 and (x,z)==(ox+2,oz+1):return 1
        if self.profile=='sky_obstacle' and y==oy+8 and abs(x-ox)<=5 and abs(z-oz)<=5:return 20
        return super().initial(x,y,z)

    def config(self):
        return {'height_limit':self.read(self.feature+0x54,'i'),'trunk_height':self.read(self.feature+0x58,'i'),
                'attenuation':self.read(self.feature+0x60,'d'),'branch_slope':self.read(self.feature+0x68,'d'),
                'width_scale':self.read(self.feature+0x70,'d'),'leaf_density':self.read(self.feature+0x78,'d'),
                'trunk_size':self.read(self.feature+0x80,'i'),'height_range':self.read(self.feature+0x84,'i'),
                'leaf_distance':self.read(self.feature+0x88,'i')}

    def construct(self,updates,log_meta=0,leaves_meta=0,prepared=False):
        # Canonical supplied block defaults; their actual registry initializer
        # is outside this bounded constructor/feature capture.
        self.w(self.blocks[17]+0x190,'Q',self.state((17<<4)|log_meta))
        self.w(self.blocks[18]+0x190,'Q',self.state((18<<4)|leaves_meta))
        C.memset(self.feature,0,8192)
        self.call(0x7e020,None,[P,I],[self.feature,int(updates)])
        result=self.config()
        if prepared:self.call(0x7f040,None,[P],[self.feature])
        return result

    def place(self,seed,position,profile):
        self.reset_world(profile,position[1],position)
        self.fluid_queries=[]
        self.support_queries=[]
        pos=self.alloc(12)
        for i,value in enumerate(position):self.w(pos+4*i,'i',value)
        self.call(0x596c90,None,[P,U],[self.random,seed&0xffffffffffffffff])
        result=bool(self.call(0x7f050,I,[P,P,P,P],[self.feature,self.world,self.random,pos])&255)
        begin,end=self.read(self.feature+0x98,'Q'),self.read(self.feature+0xa0,'Q')
        nodes=[[self.read(p+i*4,'i') for i in range(4)] for p in range(begin,end,16)]
        after=C.c_int64(self.call(0x5970e0,U,[P],[self.random])).value
        internal=self.read(self.feature+0x28,'Q')
        internal_after=C.c_int64(self.call(0x5970e0,U,[P],[self.feature+0x28])).value
        self.w(self.feature+0x28,'Q',internal)
        return {'seed':seed,'position':position,'profile':profile,'result':result,'config':self.config(),'nodes':nodes,
                **self.world_result(),'support_queries':self.support_queries,'fluid_queries':self.fluid_queries,
                'light_queries':self.light_queries,'random_after':after,'internal_random_after':internal_after}

    def leaf_data_cases(self):
        return [{'state':18|(meta<<16),'native_data':self.call(0x502cc0,I,[P,P],[self.blocks[18],self.state((18<<4)|meta)])} for meta in range(16)]
