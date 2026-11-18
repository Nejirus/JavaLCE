"""Original biome-specific decoration with controlled feature geometry.
Tree selection/constructors and biome noise remain original code. Feature
callbacks consume one original nextLong, except the RNG-free desert well.
"""
from native_flora_selection import FloraSelectionOracle,C,P,U,I
from native_decoration import DecorationOracle


class BiomeWrapperOracle(FloraSelectionOracle):
    WRAPPERS={'FOREST':0x2f6ad0,'SAVANNA':0x5c6ae0,'MUTATED_SAVANNA':0x4a23f0,'PLAINS':0x53d4c0,
              'SWAMP':0x6ca350,'TAIGA':0x6d1950,'JUNGLE':0x3c5010,'BAMBOO_JUNGLE':0x41880,
              'DESERT':0x1fa410,'HILLS':0x29fe00}
    DOUBLE_TYPES=[(0x1414260,0x2478ac),(0x1414310,0x247a22),(0x14143c0,0x247b98),
                  (0x1414470,0x247d0e),(0x1414520,0x247e84),(0x14145d0,0x247ffb)]

    def __init__(self,executable,library):
        super().__init__(executable,library)
        self.double_inputs=[]
        for address,pc in self.DOUBLE_TYPES:
            if C.string_at(self.game.base+pc,3)!=b'\xc7\x43\x08':raise ValueError('Unexpected original double-plant enum assignment')
            id=self.game.read(pc+3,'I');self.game.w(address,'Q',self.types[id]);self.double_inputs.append({'alias':hex(address),'initializer':hex(pc),'id':id})
        self.decorator=self.alloc(352);self.w(self.biome+0x60,'Q',self.decorator)
        dvt=self.alloc(32);self.w(self.decorator,'Q',dvt);self.w(dvt+0x10,'Q',self.callback(None,[P,P,P,P,P],lambda deco,world,random,biome,pos:self.base(biome,world,random,pos)))
        self.bvt=self.alloc(256);self.w(self.biome,'Q',self.bvt);self.w(self.bvt+0x98,'Q',self.game.base+0x85990)
        self.fvt=self.alloc(64);self.feature_info={}
        self.w(self.fvt+8,'Q',self.callback(None,[P],lambda obj:None));self.w(self.fvt+0x10,'Q',self.callback(I,[P,P,P,P],self.feature_place))
        self.double=self.make_feature('DOUBLE_PLANT',175);self.game.w(0x140ab10,'Q',self.double)
        self.berries=self.make_feature('BERRY_BUSH',363);self.silverfish=self.make_feature('SILVERFISH',97)
        self.w(self.blocks[129]+0x190,'Q',self.state(129<<4));self.game.w(0x140b5e0,'Q',self.blocks[129])
        self.patch(0x85430,None,[P,P,P,P],self.base)
        for kind,pc in [('MELON',0x4642c0),('VINES',0x775260),('BAMBOO',0x40e40),('PODZOL',0x5548a0),('FOSSIL',0x2f6ef0),('BOULDER',0xb85d0),('HUGE_MUSHROOM',0x370120)]:
            self.patch(pc,I,[P,P,P,P],lambda obj,world,random,pos,kind=kind:self.direct_feature(kind,random,pos))
        self.patch(0x1fb400,None,[P],lambda obj:None)
        # Generic feature ABI retains an unused Random* third argument; this
        # RNG-free kernel receives its position in the fourth argument.
        self.patch(0x1fa530,I,[P,P,P,P],lambda obj,world,unused_random,pos:self.direct_feature('DESERT_WELL',None,pos))
        for pc in [0x6f5eb0,0x7f050,0x8f390,0x5345c0,0x672fb0,0x33b160,0x6ca560,0x5c6ec0,0x5bfc60,0x4517f0,0x450720]:self.patch(pc,I,[P,P,P,P],self.tree_place)
        self.patch(0x2a4950,None,[P],self.prepare)
        self.patch(0x7f040,None,[P],self.prepare_big)
        self.patch(0x451c40,None,[P,P,P,P],self.tree_post)
        self.position_adapter(0x3f1f40,'height')

    position_adapter=DecorationOracle.position_adapter

    def make_feature(self,kind,state):
        obj=self.alloc(64);self.w(obj,'Q',self.fvt);self.feature_info[obj]=(kind,state);return obj

    def next_long(self,random):return C.c_int64(self.call(0x5970e0,U,[P],[random])).value

    def base(self,biome,world,random,pos):self.events.append(['base',*self.xyz(pos),self.read(self.decorator+0x124,'i'),self.read(self.decorator+0x128,'i'),self.next_long(random)])

    def result(self,value):return self.outcome=='always' or self.outcome=='mixed' and value&1!=0

    def direct_feature(self,kind,random,pos):
        draw=self.next_long(random) if random else None
        self.events.append(['feature',kind,*self.xyz(pos),0,draw]);return self.result(draw or 0)

    def feature_place(self,feature,world,random,pos):
        kind,state=self.feature_info[feature]
        if kind=='DOUBLE_PLANT':state|=self.read(self.read(feature+0x10,'Q')+8,'i')<<16
        draw=self.next_long(random);self.events.append(['feature',kind,*self.xyz(pos),state,draw]);return self.result(draw)

    def prepare(self,feature):self.events.append(['prepare',self.descriptor(feature)])

    def prepare_big(self,feature):self.w(feature+0x88,'i',5);self.prepare(feature)

    def tree_place(self,feature,world,random,pos):
        draw=self.next_long(random);self.events.append(['tree',self.descriptor(feature),*self.xyz(pos),draw]);return self.result(draw)

    def tree_post(self,feature,world,random,pos):self.events.append(['post',self.descriptor(feature),*self.xyz(pos),self.next_long(random)])

    def height_value_at(self,x,z):return {'zero':0,'negative':-32,'one':1,'flat':64}.get(self.height_profile,32+((3*x+z)&31))

    def height_y(self,x,y,z):
        value=self.height_value_at(x,z);self.events.append(['height',x,y,z,value]);return value

    def height(self,world,x,z):
        value=self.height_value_at(x,z);self.events.append(['height_xz',x,z,value]);return value

    def get_block(self,world,x,y,z):
        # Explicit source inputs for the mountain emerald pass.
        value=1 if (x+z+y)%3==0 else 3
        self.events.append(['block',x,y,z,value]);return value<<4

    def set_block(self,world,x,y,z,tile,meta,flags):
        self.events.append(['set',x,y,z,tile|(meta<<16),flags]);return 1

    def decorate(self,kind,variant,temperature,seed,origin,height_profile,outcome,override=None):
        self.w(self.biome+0x1d0,'Q',self.silverfish if kind=='HILLS' else variant)
        self.w(self.biome+0x1d8,'Q',self.berries);self.w(self.biome+0x18,'f',temperature)
        selector=kind if kind in self.SELECTORS else 'DEFAULT';self.w(self.bvt+0x10,'Q',self.game.base+self.SELECTORS[selector])
        self.w(self.decorator+0x124,'i',2);self.w(self.decorator+0x128,'i',1)
        self.events=[];self.noise_queries=[];self.override=override;self.height_profile=height_profile;self.outcome=outcome
        pos=self.alloc(12)
        for i,value in enumerate(origin):self.w(pos+4*i,'i',value)
        self.call(0x596c90,None,[P,U],[self.random,seed&0xffffffffffffffff])
        self.call(self.WRAPPERS[kind],None,[P,P,P,P],[self.biome,self.world,self.random,pos])
        return {'kind':kind,'variant':variant,'temperature':self.read(self.biome+0x18,'f'),'seed':seed,'origin':origin,'height_profile':height_profile,'outcome':outcome,'noise_override':override,
                'events':self.events,'noise_queries':self.noise_queries,'flowers_after':self.read(self.decorator+0x124,'i'),'grass_after':self.read(self.decorator+0x128,'i'),'random_after':self.next_long(self.random)}
