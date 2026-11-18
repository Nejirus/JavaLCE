"""Unmodified original main coral geometry, lists and dispatch over voxel inputs."""
from native_corals import CoralOracle,C,P,U,I
from native_carvers import F


class CoralShapeOracle(CoralOracle):
    def initial(self,x,y,z):
        if not self.profile.startswith('shape_'):return super().initial(x,y,z)
        ox,oy,oz=self.position
        if self.profile=='shape_air' or y<0 or y>=256:return 0
        floor=oy-(10 if self.profile=='shape_unsupported' else 1)
        if self.profile=='shape_terrain':floor+=((x*3+z*7)%5)-2
        if y<floor:return 1
        if y==floor:return 1
        if self.profile in ['shape_matching','shape_foreign','shape_dead'] and (x+3*y+7*z)%5==0:
            color=(self.shape_state>>16)&7
            if self.profile=='shape_foreign':color=(color+1)%5
            return 259|((color|(8 if self.profile=='shape_dead' else 0))<<16)
        if self.profile=='shape_obstacle' and (3*x+y+5*z)%7==0:return 1
        return 9 if y<64 else 0

    def vector(self,vector,stride):
        begin=self.read(vector+8,'Q');end=self.read(vector+16,'Q');result=[]
        for entry in range(begin,end,stride):
            row=self.xyz(entry)
            if stride==24:row.append(self.read(self.read(entry+16,'Q')+8,'I'))
            result.append(row)
        return result

    def shape(self,kind,seed,position,profile,state=259,radius=1,increasing=True,chance=.5,direction=2,short=False,placement='all',client=False):
        self.shape_state=state;pos=self.begin_coral(profile,position,placement,'all',self.HORIZONTAL)
        self.w(self.world+0x248,'B',int(client));self.w(self.feature,'Q',self.treevt);self.w(self.feature+8,'B',0)
        self.call(0x596c90,None,[P,U],[self.random,seed&0xffffffffffffffff])
        native_state=self.canonical_state(state);tops=self.alloc(32);sides=self.alloc(32);result=None
        if kind in ['cross','diagonal']:
            self.call(0x1cf6f0 if kind=='cross' else 0x1cfe00,None,[P,P,P,P,P,I,I,F],[self.feature,self.world,self.random,pos,native_state,radius,int(increasing),chance])
        elif kind=='branch':self.call(0x1d04c0,None,[P,P,P,P,P,P,P,P,I],[self.feature,self.world,self.random,pos,native_state,tops,sides,self.directions[direction],int(short)])
        elif kind=='full':result=bool(self.call(0x1cc130,I,[P,P,P,P],[self.feature,self.world,self.random,pos])&255)
        elif kind=='dispatch':self.call(0x1cc3d0,None,[P,P,P,P,P,P,I],[self.feature,self.world,pos,self.random,tops,sides,state>>16&7])
        else:self.call({'shape0':0x1cc8e0,'shape1':0x1ccf80,'rare':0x1cd950,'patch':0x1ce3d0}[kind],None,[P,P,P,P,P,P,P],[self.feature,self.world,pos,self.random,tops,sides,native_state])
        r={'kind':kind,'seed':seed,'position':position,'profile':profile,'state':state,'radius':radius,'increasing':increasing,'chance':chance,'direction':direction,'short':short,'placement':placement,'client':client,'plane':self.HORIZONTAL,'loaded':'all','result':result,
           'top_positions':self.vector(tops,12),'side_positions':self.vector(sides,24),**self.coral_result(),'random_after':C.c_int64(self.call(0x5970e0,U,[P],[self.random])).value}
        for vector in [tops,sides]:
            begin=self.read(vector+8,'Q')
            if begin:self.host.free(begin)
        return r
