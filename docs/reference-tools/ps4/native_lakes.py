"""Original lake routine with controlled blocks, materials, lighting and biomes."""
from native_ores import OreOracle,P,U,I,D
from native_carvers import F
import struct


class LakeOracle(OreOracle):
    def __init__(self,executable,library):
        super().__init__(executable,library)
        # Direction initializer 0x1ff8b5..0x1ff902 supplies (0,-1,0).
        # Bind its vector fields; original BlockPos::below routines still run.
        down=self.alloc(0x68)
        self.w(down+0x5c,'Q',0xffffffff00000000)
        self.game.w(0x1413bf0,'Q',down)
        self.matvt=self.alloc(64)
        self.w(self.matvt+0x10,'Q',self.callback(I,[P],lambda mat:self.read(mat+8,'I')==2))
        self.w(self.matvt+0x20,'Q',self.callback(I,[P],lambda mat:self.read(mat+8,'I')==1))
        for tile,block in self.blocks.items():
            mat=self.read(block+0x170,'Q')
            self.w(mat,'Q',self.matvt)
            self.w(mat+8,'I',0 if tile==0 else 2 if tile in [8,9,10,11] else 1)
        lava=self.read(self.blocks[10]+0x170,'Q')
        self.w(self.blocks[11]+0x170,'Q',lava)
        self.game.w(0x1418328,'Q',lava)
        self.w(self.statevt,'Q',self.callback(P,[P],lambda s:self.read(self.blocks[self.read(s+8,'I')>>4]+0x170,'Q')))
        self.w(self.vt+0x38,'Q',self.callback(P,[P,P],lambda level,pos:self.state(self.get_block(level,*self.xyz(pos)))))
        self.w(self.vt+0xa8,'Q',self.callback(I,[P,P],lambda level,pos:self.get_block(level,*self.xyz(pos))>>4==0))
        def set_state(level,pos,state,flags,unused):
            packed=self.read(state+8,'I')
            return self.set_block(level,*self.xyz(pos),packed>>4,packed&15,flags)
        self.w(self.vt+0x160,'Q',self.callback(I,[P,P,P,I,I],set_state))
        self.w(self.vt+0x20,'Q',self.callback(I,[P,I,P],self.light))
        self.w(self.vt+0xb8,'Q',self.callback(P,[P,P],self.biome_at))
        biomevt=self.alloc(0x100)
        self.w(self.biome,'Q',biomevt)
        self.w(biomevt+0x50,'Q',self.callback(F,[P,P],self.temperature))
        self.boxes={}
        boxvt=self.alloc(64)
        self.w(boxvt+0x18,'Q',self.callback(I,[P,P],lambda obj,type:1))
        for meta in range(16):
            box=self.alloc(32)
            self.w(box,'Q',boxvt)
            self.w(box+16,'I',meta)
            self.boxes[meta]=box
        self.w(self.statevt+0x150,'Q',self.callback(P,[P,P],lambda state,prop:self.boxes[self.read(state+8,'I')&15]))
        for addr,tile in [(0x1409f50,0),(0x1409f60,1),(0x140b550,110),(0x140ae00,79)]:
            self.game.w(addr,'Q',self.blocks[tile])
        self.patch(0xaf4a20,P,[P],lambda app:self.rules if self.profile=='protected' else 0)

    def xyz(self,pos):
        return [self.read(pos+i*4,'i') for i in range(3)]

    def get_block(self,world,x,y,z):
        if self.profile=='empty':
            initial=0
        elif self.profile=='flooded' and y>=64 and y<80:
            initial=9
        elif self.profile=='leaking' and (x*17+y*11+z*31)%7==0:
            initial=0
        elif self.profile in ['cold','flowing','bright'] and y==63 and x-self.position[0]+8 in [0,15] and z-self.position[2]+8 in [0,15]:
            initial=8|(1<<16) if self.profile=='flowing' else 9
        else:
            initial=0 if y>=64 else 3|(1<<16) if y==63 else 3|(2<<16) if y==62 else 1
        state=self.changed.get((x,y,z),initial)
        self.read_hash.update(struct.pack('<4i',x,y,z,state))
        self.read_count+=1
        return ((state&65535)<<4)|((state>>16)&15)

    def light(self,world,kind,pos):
        xyz=self.xyz(pos)
        self.light_queries.append([kind,*xyz])
        return 15 if kind==15 or self.profile=='bright' else 0

    def biome_at(self,world,pos):
        self.biome_queries.append(self.xyz(pos))
        return self.biome

    def temperature(self,biome,pos):
        self.temperature_queries.append(self.xyz(pos))
        return 0. if self.profile in ['cold','flowing','bright'] else .8

    def lake(self,seed,position,profile,tile):
        self.reset_world(profile,128,position)
        self.light_queries=[]
        self.biome_queries=[]
        self.temperature_queries=[]
        self.w(self.biome+0x50,'I',110 if profile=='mushroom' else 2)
        feature=self.alloc(24)
        self.w(feature+0x10,'Q',self.blocks[tile])
        pos=self.alloc(12)
        for i,v in enumerate(position):self.w(pos+4*i,'i',v)
        self.call(0x596c90,None,[P,U],[self.random,seed&0xffffffffffffffff])
        result=self.call(0x3db900,I,[P,P,P,P],[feature,self.world,self.random,pos])&255
        after=self.call(0x58fd50,D,[P],[self.random])
        return {'seed':seed,'position':position,'profile':profile,'tile':tile,'result':bool(result),
                **self.world_result(),'light_queries':self.light_queries,'biome_queries':self.biome_queries,
                'temperature_queries':self.temperature_queries,'random_after':after}
