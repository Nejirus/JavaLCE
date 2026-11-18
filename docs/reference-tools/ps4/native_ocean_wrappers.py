"""Original frozen/warm ocean and ice-biome decoration wrappers.

Original placement coordinates, counts, order and RNG execute. Base decoration,
feature geometry/outcomes and world heights are supplied; every feature/base
callback consumes one original nextLong, matching the other wrapper captures.
"""
from native_biome_wrappers import BiomeWrapperOracle,C,P,U,I


class OceanWrapperOracle(BiomeWrapperOracle):
    WRAPPERS={'FROZEN_OCEAN':0x4d74f0,'WARM_OCEAN':0x4fb580,'ICE':0x373b20}
    def __init__(self,executable,library):
        super().__init__(executable,library)
        self.spike=self.make_feature('ICE_SPIKE',0);self.patch_feature=self.make_feature('ICE_PATCH',0)
        self.feature_bindings=[]
        bindings=[(0x110,'BLUE_ICE',0x86ee7,0x121d988),(0xe0,'CORAL',0x86e63,0x1234368),(0xd8,'CORAL_CRUST',0x86f1b,0x12342b8),(0xe8,'CORAL_HANG',0x86f4f,0x12343b8),(0xf0,'SEA_ANEMONE',0x86f83,0x12c0848),(0x100,'SEA_PICKLES',0x86e97,0x12c0978)]
        for off,kind,pc,vt in bindings:
            if C.string_at(self.game.base+pc,3)!=b'\x48\x8d\x05' or pc+7+self.game.read(pc+3,'i')+16!=vt:raise ValueError('Original decorator feature binding changed')
            self.feature_bindings.append({'field':hex(off),'kind':kind,'initializer':hex(pc),'vtable':hex(vt),'kernel':hex(self.game.read(vt+0x10,'Q')-self.game.base)})
            self.w(self.decorator+off,'Q',self.make_feature(kind,0))
        self.w(self.decorator+0x108,'Q',self.make_feature('ICEBERG',0))
    def decorate(self,kind,variant,seed,origin,height_profile,outcome,pickles):
        self.w(self.biome+0x1d0,'B',variant);self.w(self.biome+0x1d8,'Q',self.spike);self.w(self.biome+0x1e0,'Q',self.patch_feature)
        self.w(self.decorator+0x124,'i',2);self.w(self.decorator+0x128,'i',1);self.w(self.decorator+0x150,'i',pickles)
        self.events=[];self.noise_queries=[];self.override=None;self.height_profile=height_profile;self.outcome=outcome
        pos=self.alloc(12)
        for i,value in enumerate(origin):self.w(pos+4*i,'i',value)
        self.call(0x596c90,None,[P,U],[self.random,seed&0xffffffffffffffff])
        self.call(self.WRAPPERS[kind],None,[P,P,P,P],[self.biome,self.world,self.random,pos])
        return {'kind':kind,'variant':variant,'seed':seed,'origin':origin,'height_profile':height_profile,'outcome':outcome,'sea_pickles':pickles,
                'events':self.events,'noise_queries':self.noise_queries,'flowers_after':self.read(self.decorator+0x124,'i'),'grass_after':self.read(self.decorator+0x128,'i'),'random_after':self.next_long(self.random)}
