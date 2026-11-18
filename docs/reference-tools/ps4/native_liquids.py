"""Original liquid tick/flow helpers over explicit two-layer voxel contexts.

Material/face/drop classifications, game rules, thread-local storage and world
side effects are supplied. Original fluid geometry and RNG execute unchanged.
"""
from native_tree_selection import TreeSelectionOracle,C,P,U,I
from pathlib import Path
import json


class LiquidOracle(TreeSelectionOracle):
    def __init__(self,executable,library):
        super().__init__(executable,library)
        generic=self.alloc(0x510);C.memmove(generic,self.blockvt,512)
        self.w(generic+0x150,'Q',self.callback(I,[P],lambda block:self.solid_render(self.read(block+8,'I'))))
        self.w(generic+0x170,'Q',self.callback(I,[P,P,P,P],self.pass_face))
        self.w(generic+0x338,'Q',self.callback(I,[P],lambda block:self.drop_plants and self.read(block+8,'I') in [31,37,38,39,40,50,106]))
        self.w(generic+0x250,'Q',self.callback(None,[P,P,P,P,I],self.drop))
        for block in self.blocks.values():self.w(block,'Q',generic)
        for tile in [8,10]:
            old=self.blocks[tile];block=self.alloc(0x300);C.memmove(block,old,512);self.blocks[tile]=block
            # Supplied MSVC deque proxy/owner storage; original queue operations run.
            proxy=self.alloc(16);self.w(proxy,'Q',block+0x1f8);self.w(block+0x1f8,'Q',proxy)
        for tile in [8,9,10,11]:
            vt=self.alloc(0x510);C.memmove(vt,self.game.base+(0x123fb80 if tile in [8,10] else 0x12d35a0),0x510)
            self.w(vt+0x150,'Q',self.read(generic+0x150,'Q'));self.w(vt+0x170,'Q',self.read(generic+0x170,'Q'))
            self.w(vt+0x4f0,'Q',self.callback(None,[P,P,P],lambda block,world,pos:self.events.append(['fizz',*self.xyz(pos)])))
            self.w(self.blocks[tile],'Q',vt)
        registry=self.game.read(0x140ad68,'Q');registry_vt=self.alloc(64);self.w(registry,'Q',registry_vt)
        self.w(registry_vt+0x38,'Q',self.callback(I,[P,P],lambda registry,block:self.read(block+8,'I')))
        for tile,block in self.blocks.items():self.w(registry+0x150+tile*8,'Q',block)
        bindings=json.loads(Path(__file__).with_name('block-registry-native.json').read_text());ids={r['name']:r['id'] for r in bindings['registrations']};ids['air']=0
        for alias in bindings['aliases']:
            if ids[alias['name']] in self.blocks:self.game.w(int(alias['alias'],16),'Q',self.blocks[ids[alias['name']]])
        self.w(self.vt+0x50,'Q',self.callback(I,[P,I,I,I],lambda world,x,y,z:self.get_block(world,x,y,z)>>4))
        self.w(self.vt+0xe8,'Q',self.callback(I,[P,I,I,I],lambda world,x,y,z:self.read_secondary(x,y,z)&65535))
        self.w(self.vt+0x88,'Q',self.callback(None,[P,I,I,I,P,P],self.paired))
        self.w(self.vt+0x90,'Q',self.callback(P,[P,P],lambda world,pos:self.blocks[self.get_block(world,*self.xyz(pos))>>4]))
        self.w(self.vt+0x160,'Q',self.callback(I,[P,P,P,I,I],self.set_primary))
        self.w(self.vt+0x168,'Q',self.callback(I,[P,P,P,I,I],self.set_secondary))
        self.w(self.vt+0x188,'Q',self.callback(I,[P,P],self.remove_primary))
        self.w(self.vt+0x198,'Q',self.game.base+0x3f01f0)
        self.w(self.vt+0x208,'Q',self.callback(None,[P,P,P,I],self.schedule))
        info=self.alloc(1024);ivt=self.alloc(0x230);self.w(info,'Q',ivt);self.w(self.world+0x218,'Q',info)
        self.w(ivt+0x218,'Q',self.callback(P,[P],lambda obj:info))
        self.patch(0x2f4f60,I,[P],lambda rules:False)
        self.patch(0x3edc40,I,[],lambda:self.immediate)
        self.patch(0x3e9340,None,[P,P,P,I],self.notify)
        self.patch(0x3dadc0,None,[P,I,P,I],lambda world,id,pos,data:self.events.append(['level_event',id,*self.xyz(pos),data]))
        self.patch(0xc59150,I,[I,I],lambda key,default:False)

    def set_property(self,state,prop,boxed):
        if prop==self.properties.get('level'):
            packed=self.read(state+8,'I');return self.state((packed&~15)|self.read(boxed+16,'I'))
        return super().set_property(state,prop,boxed)

    def initial(self,x,y,z):
        ox,oy,oz=self.position
        if self.profile=='empty':return 0
        return 1 if y<oy else 0

    def primary(self,x,y,z):return self.primary_changed.get((x,y,z),self.initial(x,y,z))
    def secondary(self,x,y,z):return self.secondary_changed.get((x,y,z),0)

    def get_block(self,world,x,y,z):
        state=self.primary(x,y,z);self.events.append(['primary',x,y,z,state]);return ((state&65535)<<4)|(state>>16)

    def read_secondary(self,x,y,z):
        state=self.secondary(x,y,z);self.events.append(['secondary',x,y,z,state]);return state

    def paired(self,world,x,y,z,primary,secondary):
        a=self.primary(x,y,z);b=self.secondary(x,y,z);self.events.append(['pair',x,y,z,a,b])
        self.w(primary,'H',((a&65535)<<4)|(a>>16));self.w(secondary,'H',((b&65535)<<4)|(b>>16))

    def set_primary(self,world,pos,state,flags,unused):
        xyz=self.xyz(pos);value=self.unpacked(state);self.primary_changed[tuple(xyz)]=value;self.events.append(['set_primary',*xyz,value,flags]);return 1

    def set_secondary(self,world,pos,state,flags,unused):
        xyz=self.xyz(pos);value=self.unpacked(state);self.secondary_changed[tuple(xyz)]=value;self.events.append(['set_secondary',*xyz,value,flags]);return 1

    def remove_primary(self,world,pos):
        xyz=self.xyz(pos);self.primary_changed[tuple(xyz)]=0;self.events.append(['remove',*xyz]);return 1

    def schedule(self,world,pos,block,delay):
        xyz=self.xyz(pos);self.events.append(['schedule',*xyz,self.read(block+8,'I'),delay])
        if self.replay:
            state=self.secondary(*xyz) or self.primary(*xyz)
            self.call(0x221e90,None,[P,P,P,P,P],[block,world,pos,self.state(((state&65535)<<4)|(state>>16)),self.random])
    def notify(self,world,pos,block,flag):self.events.append(['notify',*self.xyz(pos),self.read(block+8,'I'),flag])
    def drop(self,block,world,pos,state,fortune):self.events.append(['drop',*self.xyz(pos),self.unpacked(state),fortune])

    def pass_face(self,block,world,pos,direction):
        result=not self.closed_faces;self.events.append(['face',*self.xyz(pos),self.read(block+8,'I'),self.read(direction+8,'I'),result]);return result

    def tick(self,kind,seed,position,level=0,profile='floor',primary=None,secondary=None,chunk_count=64,hot_step=False,ultra_warm=False,closed_faces=False,drop_plants=False,immediate=False,replay=False,horizontal_plane=None):
        if horizontal_plane is not None:
            for i,direction in enumerate(horizontal_plane):self.game.w(0x1413e50+8*i,'Q',self.directions[direction])
        self.position=position;self.profile=profile;self.events=[];self.primary_changed={tuple(r[:3]):r[3] for r in primary or []};self.secondary_changed={tuple(r[:3]):r[3] for r in secondary or []}
        tile=8 if kind=='water' else 10;self.primary_changed.setdefault(tuple(position),tile|(level<<16))
        self.closed_faces=closed_faces;self.drop_plants=drop_plants;self.immediate=immediate;self.replay=replay
        self.w(self.world+0x2f0,'i',chunk_count);self.w(self.dimension+0x28,'B',int(hot_step));self.w(self.dimension+0x29,'B',int(ultra_warm))
        pos=self.alloc(12)
        for i,value in enumerate(position):self.w(pos+4*i,'i',value)
        self.call(0x596c90,None,[P,U],[self.random,seed&0xffffffffffffffff])
        state=self.state((tile<<4)|level);self.call(0x221e90,None,[P,P,P,P,P],[self.blocks[tile],self.world,pos,state,self.random])
        return {**({'plane':list(horizontal_plane)} if horizontal_plane is not None else {}),'kind':kind,'seed':seed,'position':position,'level':level,'profile':profile,'primary':primary or [],'secondary':secondary or [],'chunk_count':chunk_count,'hot_step':hot_step,'ultra_warm':ultra_warm,
                'closed_faces':closed_faces,'drop_plants':drop_plants,'immediate':immediate,'replay':replay,'events':self.events,'random_after':C.c_int64(self.call(0x5970e0,U,[P],[self.random])).value}
