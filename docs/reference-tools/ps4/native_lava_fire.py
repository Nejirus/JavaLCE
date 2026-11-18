"""Original lava ignition, with explicit material/default-state/chunk inputs."""
from native_liquids import LiquidOracle,C,P,U,I


class LavaFireOracle(LiquidOracle):
    def __init__(self,executable,library):
        super().__init__(executable,library)
        self.flammable_materials={self.read(self.blocks[tile]+0x170,'Q') for tile in [17,18,31]}
        self.w(self.matvt+0x50,'Q',self.callback(I,[P],lambda mat:mat in self.flammable_materials))
        self.patch(0x2f4f60,I,[P],lambda rules:self.fire_enabled)
        self.w(self.vt+0x148,'Q',self.callback(I,[P,I,I,I],self.chunk_loaded))
        directions=self.alloc(48)
        for i,direction in enumerate(self.directions):self.w(directions+i*8,'Q',direction)
        self.game.w(0x1413cc8,'Q',directions);self.game.w(0x1413cd0,'Q',directions+48)
        self.default_inputs=[]
        for tile in [0,1,9,10,11,17,18,20,31,51,161]:
            state=self.call(0x7a4d0,P,[P],[self.blocks[tile]]);self.default_inputs.append({'tile':tile,'state':self.unpacked(state),'flammable':self.read(self.blocks[tile]+0x170,'Q') in self.flammable_materials})

    def initial(self,x,y,z):
        ox,oy,oz=self.position
        if (x,y,z)==(ox,oy,oz):return 10
        if self.profile=='air':return 0
        if self.profile=='blocked_above' and (x,y,z)==(ox,oy+1,oz):return 1
        if self.profile=='leaf_shell':return 18 if oy<=y<=oy+1 else 0
        if self.profile=='mixed':return [0,1,17,18,31,51][(x*7+y*3+z*11)%6]
        return (17|(3<<16) if self.profile=='metadata_wood' else 17) if y<oy else 0

    def chunk_loaded(self,world,x,z,allow_empty):
        result=self.loaded=='all' or self.loaded=='alternating' and ((x^z)&1)==0
        self.events.append(['chunk',x,z,bool(allow_empty),result]);return result

    def fire(self,seed,position,profile,enabled,loaded,integrated=False):
        self.fire_enabled=enabled;self.loaded=loaded
        if integrated:
            result=self.tick('lava',seed,position,level=0,profile=profile)
            return {**result,'fire_enabled':enabled,'loaded':loaded,'integrated':True}
        self.position=position;self.profile=profile;self.events=[];self.primary_changed={};self.secondary_changed={};self.immediate=False;self.replay=False
        self.closed_faces=False;self.drop_plants=False
        pos=self.alloc(12)
        for i,value in enumerate(position):self.w(pos+4*i,'i',value)
        self.call(0x596c90,None,[P,U],[self.random,seed&0xffffffffffffffff])
        self.call(0x41c980,None,[P,P,P,P],[self.blocks[10],self.world,pos,self.random])
        return {'seed':seed,'position':position,'profile':profile,'fire_enabled':enabled,'loaded':loaded,'integrated':False,'events':self.events,'random_after':C.c_int64(self.call(0x5970e0,U,[P],[self.random])).value}
