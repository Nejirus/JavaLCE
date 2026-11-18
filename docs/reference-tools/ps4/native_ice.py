"""Original blue ice, snow patches and ice spikes with canonical voxel inputs."""
from native_wells import WellOracle,C,P,U,I
from pathlib import Path
import json


class IceOracle(WellOracle):
    def __init__(self,executable,library):
        super().__init__(executable,library)
        block=self.alloc(1024);self.blocks[273]=block;self.w(block,'Q',self.blockvt);self.w(block+8,'I',273);self.w(block+0x170,'Q',self.read(self.blocks[79]+0x170,'Q'));self.w(block+0x190,'Q',self.state(273<<4))
        registry=json.loads(Path(__file__).with_name('block-registry-native.json').read_text());ids={r['name']:r['id'] for r in registry['registrations']};ids['air']=0
        for alias in registry['aliases']:
            if ids[alias['name']] in self.blocks:self.game.w(int(alias['alias'],16),'Q',self.blocks[ids[alias['name']]])
        directions=self.alloc(48)
        for i,d in enumerate(self.directions):self.w(directions+8*i,'Q',d)
        self.game.w(0x1413cc8,'Q',directions);self.game.w(0x1413cd0,'Q',directions+48)
        self.w(self.vt+0x50,'Q',self.callback(I,[P,I,I,I],lambda world,x,y,z:self.get_block(world,x,y,z)>>4))
        self.w(self.blocks[273]+0x190,'Q',self.state(273<<4))

    def initial(self,x,y,z):
        ox,oy,oz=self.position;dx,dy,dz=x-ox,y-oy,z-oz
        if self.kind=='blue':
            if self.profile=='air':return 0
            if self.profile=='flowing':return 8
            if self.profile=='ice_below' and (dx,dy,dz)==(0,-1,0):return 174
            if self.profile in ['ice_above','current_air','mixed','crowded'] and (dx,dy,dz)==(0,1,0):return 174
            if self.profile=='ice_side' and (dx,dy,dz)==(1,0,0):return 174
            if self.profile=='blue_only' and (dx,dy,dz)==(1,0,0):return 273
            if self.profile=='current_air' and (dx,dy,dz)==(0,0,0):return 0
            if self.profile=='crowded' and 1<=dx<=3 and 1<=dz<=3 and -1<=dy<=1:return 273 if (dx,dy,dz)!=(2,0,2) else 9
            if self.profile=='mixed' and (x*7+y*11+z*3)%7==0:return [0,1,8,9,79,174,273][(x+y+z)%7]
            return 9
        if y<0 or y>=256 or self.profile=='air':return 0
        floor=oy-4+(3 if self.profile=='buried' else 0)
        if self.profile=='overhang' and y==oy+2 and abs(dx)+abs(dz)<3:return 1
        if y>floor:return 0
        if y==floor:return {'dirt':3,'grass':2,'ice':79,'stone':1,'snow_layers':78,'water':9}.get(self.profile,80)
        if self.profile=='snow_only':return 80
        if self.profile=='root_holes' and (x*7+y*3+z*11)%5==0:return 0
        if self.profile=='mixed':return [0,1,3,3|(2<<16),79,80,174,9][(x*7+y*3+z*11)%8]
        return 3

    def ice(self,kind,seed,position,profile='snow',size=6,state=174,updates=False,sea_level=63):
        self.kind=kind;self.reset_world(profile,256,position);self.fluid_queries=[];self.w(self.world+8,'i',sea_level)
        self.w(self.feature,'Q',self.treevt);self.w(self.feature+8,'B',int(updates));self.w(self.feature+0x10,'Q',self.blocks[state&65535]);self.w(self.feature+0x18,'i',size)
        pos=self.alloc(12)
        for i,value in enumerate(position):self.w(pos+4*i,'i',value)
        self.call(0x596c90,None,[P,U],[self.random,seed&0xffffffffffffffff])
        result=bool(self.call({'blue':0xd1300,'patch':0x374370,'spike':0x3745d0}[kind],I,[P,P,P,P],[self.feature,self.world,self.random,pos])&255)
        return {'kind':kind,'seed':seed,'position':position,'profile':profile,'size':size,'state':state,'updates':updates,'sea_level':sea_level,'result':result,**self.world_result(),'fluid_queries':self.fluid_queries,'light_queries':self.light_queries,'random_after':C.c_int64(self.call(0x5970e0,U,[P],[self.random])).value}
