"""Original postProcess dispatcher with controlled feature/world callbacks.

Feature callbacks consume one original nextLong each. This validates dispatcher
ordering and shared RNG flow, not the missing feature implementations or mobs.
"""
from native_carvers import Oracle,C,P,U,I,D


class PopulationOracle(Oracle):
    STRUCTURES=[('MINESHAFT',0x148,0x4e),('VILLAGE',0x140,0x4d),
                ('STRONGHOLD',0x138,0x4c),('SCATTERED',0x150,0x4f),
                ('MONUMENT',0x170,0x51),('OCEAN_RUIN',0x190,0x50),
                ('MANSION',0x178,0x52),('BURIED_TREASURE',0x180,0x65),
                ('SHIPWRECK',0x188,0x53),('OUTPOST',0x198,0x66)]

    def __init__(self,executable,library,native_mobs=False):
        super().__init__(executable,library)
        down=self.alloc(0x68)
        self.w(down+0x5c,'Q',0xffffffff00000000)
        self.game.w(0x1413bf0,'Q',down)
        self.game.w(0x140ae00,'Q',self.blocks[79])
        self.gen=self.alloc(512)
        self.random=self.gen+0x30
        self.w(self.gen+0x90,'Q',self.world)
        self.biomes={}
        bvt=self.alloc(0x80)
        self.w(bvt+0x58,'Q',self.callback(None,[P,P,P,P],self.decorate))
        for id in [1,2,17,130]:
            b=self.alloc(512)
            self.w(b,'Q',bvt)
            self.w(b+8,'i',id)
            self.biomes[id]=b
        self.game.w(0x140a818,'Q',self.biomes[2])
        self.game.w(0x140a890,'Q',self.biomes[17])
        self.w(self.vt+0xb8,'Q',self.callback(P,[P,P],self.biome_at))
        self.chunk=self.alloc(512)
        # Original Level::getHeightmapPos (0x3f7830) reads these cached bytes.
        for z in range(16):
            for x in range(16):self.w(self.chunk+0x29+z*16+x,'B',64+(x*3+z)%4)
        source=self.alloc(32)
        svt=self.alloc(0x80)
        self.w(source,'Q',svt)
        self.w(svt+0x58,'Q',self.callback(P,[P,I,I],self.height_chunk))
        self.w(self.world+0x1f8,'Q',source)
        self.w(self.vt+0x150,'Q',self.callback(P,[P,I,I,I],self.outpost_chunk))
        self.patch(0x6bbac0,None,[P,P],self.finish_outpost)
        fvt=self.alloc(0x40)
        self.w(fvt+0x28,'Q',self.callback(I,[P,P,P,P,I],self.structure))
        for index,(name,field,flag) in enumerate(self.STRUCTURES):
            feature=self.alloc(16)
            self.w(feature,'Q',fvt)
            self.w(feature+8,'I',index)
            self.w(self.gen+field,'Q',feature)
        self.patch(0x3db900,I,[P,P,P,P],self.lake)
        self.patch(0x497e40,I,[P,P,P,P],self.dungeon)
        if native_mobs:
            if C.string_at(self.game.base+0x4a6450,1)!=b'\xc3':raise ValueError('Original population mob helper is no longer a RET')
        else:self.patch(0x4a6450,None,[P,P,I,I,I,I,P],self.mobs)
        self.patch(0x3dcbc0,I,[P,P],self.can_freeze)
        self.patch(0x3fede0,I,[P,P,I,I],self.snow)
        self.w(self.vt+0x160,'Q',self.callback(I,[P,P,P,I,I],self.ice))
        # A no-op profiling boundary lets us snapshot the untouched original
        # seed expression before structures or feature callbacks consume RNG.
        self.patch(0xbe80b0,None,[P],self.profile_boundary)

    def xyz(self,pos):
        return [self.read(pos+i*4,'i') for i in range(3)]

    def long(self,random):
        return C.c_int64(self.call(0x5970e0,U,[P],[random])).value

    def record(self,event,**values):
        if self.game.read(0x1416224,'B')!=1:
            raise ValueError('Population mode was not enabled during a callback')
        self.events.append({'event':event,**values})

    def profile_boundary(self,name):
        if name==self.game.base+0xea44a7:
            state=self.read(self.random,'Q')
            self.initial_longs=[self.long(self.random),self.long(self.random)]
            self.initial_double=self.call(0x58fd50,D,[P],[self.random])
            self.w(self.random,'Q',state)

    def biome_at(self,level,pos):
        self.record('biome',position=self.xyz(pos))
        return self.biomes[self.biome_id]

    def structure(self,feature,level,random,chunk,flag):
        index=self.read(feature+8,'I')
        name=self.STRUCTURES[index][0]
        self.record('structure',kind=name,chunk=[self.read(chunk,'i'),self.read(chunk+4,'i')],village_flag=bool(flag),draw=self.long(random))
        return name=='VILLAGE' and self.mode=='village'

    def outpost_chunk(self,level,x,z,create):
        self.record('outpost_chunk',chunk=[x,z],create=bool(create))
        return self.chunk

    def finish_outpost(self,feature,chunk):
        self.record('finish_outpost')

    def lake(self,feature,level,random,pos):
        tile=self.read(self.read(feature+0x10,'Q')+8,'I')
        self.record('lake',position=self.xyz(pos),tile=tile,draw=self.long(random))
        return 1

    def dungeon(self,feature,level,random,pos):
        self.record('dungeon',position=self.xyz(pos),draw=self.long(random))
        return 1

    def decorate(self,biome,level,random,pos):
        self.record('decorate',biome=self.read(biome+8,'i'),position=self.xyz(pos),draw=self.long(random))

    def mobs(self,level,biome,x,z,width,depth,random):
        self.record('mobs',biome=self.read(biome+8,'i'),area=[x,z,width,depth],draw=self.long(random))

    def height_chunk(self,source,x,z):
        self.record('height_chunk',chunk=[x,z])
        return self.chunk

    def can_freeze(self,level,pos):
        xyz=self.xyz(pos)
        self.record('can_freeze',position=xyz)
        return (xyz[0]+xyz[2])&3==0

    def ice(self,level,pos,state,flags,unused):
        self.record('ice',position=self.xyz(pos),state=self.read(state+8,'I')>>4,flags=flags)
        return 1

    def snow(self,level,pos,check_light,accumulate):
        self.record('snow',position=self.xyz(pos),check_light=bool(check_light),accumulate=bool(accumulate))
        return 1

    def populate(self,seed,cx,cz,mode):
        self.events=[]
        self.initial_longs=None
        self.mode=mode
        self.worldSeed=seed
        self.biome_id=2 if mode=='desert' else 17 if mode=='desert_hills' else 130 if mode=='mutated_desert' else 1
        cfg=self.alloc(0x124)
        self.call(0x1f1940,None,[P],[cfg])
        self.w(self.gen+0x110,'Q',cfg)
        structures=mode not in ['structures_off','no_features']
        self.w(self.gen+0x98,'B',structures)
        if mode in ['lakes_off','no_features','all_flags_off']:
            self.w(cfg+0x55,'B',0)
            self.w(cfg+0x5c,'B',0)
        if mode in ['no_features','all_flags_off']:
            self.w(cfg+0x45,'B',0)
        if mode=='all_flags_off':
            for name,field,flag in self.STRUCTURES:self.w(cfg+flag,'B',0)
        if mode=='alternating':
            for i,(name,field,flag) in enumerate(self.STRUCTURES):self.w(cfg+flag,'B',i%2)
        if mode in ['forced_lakes','low_sea']:
            self.w(cfg+0x58,'I',1)
            self.w(cfg+0x60,'I',10)
        if mode=='no_dungeons':self.w(cfg+0x48,'I',0)
        sea=0 if mode=='low_sea' else 63
        self.w(self.world+8,'I',sea)
        settings={'structures':[name for name,field,flag in self.STRUCTURES if self.read(cfg+flag,'B')],
                  'water_lakes':bool(self.read(cfg+0x55,'B')),'water_chance':self.read(cfg+0x58,'I'),
                  'lava_lakes':bool(self.read(cfg+0x5c,'B')),'lava_chance':self.read(cfg+0x60,'I'),
                  'dungeons':bool(self.read(cfg+0x45,'B')),'dungeon_count':self.read(cfg+0x48,'I')}
        self.call(0x508bb0,None,[P,I,I],[self.gen,cx,cz])
        if self.initial_longs is None:raise RuntimeError('Original seed boundary was not reached')
        return {'seed':seed,'chunk':[cx,cz],'mode':mode,'biome':self.biome_id,'structures':structures,
                'sea_level':sea,'settings':settings,'initial_longs':self.initial_longs,'initial_double':self.initial_double,
                'events':self.events,'after_longs':[self.long(self.random),self.long(self.random)],
                'after_double':self.call(0x58fd50,D,[P],[self.random]),'mode_after':bool(self.game.read(0x1416224,'B'))}
