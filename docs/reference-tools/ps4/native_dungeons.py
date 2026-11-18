"""Original dungeon and chest-facing routines with controlled world/entity APIs."""
from native_lakes import LakeOracle,P,U,I,D
from native_carvers import C
import struct


class DungeonOracle(LakeOracle):
    DIRECTIONS=[(0,-1,0),(0,1,0),(0,0,-1),(0,0,1),(-1,0,0),(1,0,0)]
    HORIZONTAL=[2,5,3,4]

    def __init__(self,executable,library):
        super().__init__(executable,library)
        self.directions=[]
        self.direction_boxes={}
        boxvt=self.alloc(64)
        self.w(boxvt+0x18,'Q',self.callback(I,[P,P],lambda box,type:1))
        # Literal direction data from 0x1ff830 and its Plane table at 0x200f70.
        for id,delta in enumerate(self.DIRECTIONS):
            direction=self.alloc(0x68)
            self.w(direction+8,'I',id)
            self.w(direction+0xc,'I',id^1)
            for axis,value in enumerate(delta):self.w(direction+0x5c+axis*4,'i',value)
            self.directions.append(direction)
            self.game.w(0x1413d00+id*8,'Q',direction)
            box=self.alloc(32)
            self.w(box,'Q',boxvt)
            self.w(box+16,'Q',direction)
            self.direction_boxes[id]=box
        for addr,id in [(0x1413bf0,0),(0x1413bf8,1),(0x1413ce0,2),(0x1413ce8,3),(0x1413cf0,4),(0x1413cf8,5)]:self.game.w(addr,'Q',self.directions[id])
        self.game.w(0x1413c20,'Q',self.alloc(16))
        self.game.w(0x1413e70,'B',1)
        for i,id in enumerate(self.HORIZONTAL):self.game.w(0x1413e50+i*8,'Q',self.directions[id])
        self.w(self.statevt+8,'Q',self.callback(I,[P],lambda state:self.solid_render(self.read(state+8,'I')>>4)))
        self.w(self.statevt+0x150,'Q',self.callback(P,[P,P],lambda state,prop:self.direction_boxes[self.read(state+8,'I')&15]))
        def set_facing(state,prop,boxed):
            direction=self.read(boxed+16,'Q')
            return self.state((54<<4)|self.read(direction+8,'I'))
        self.w(self.statevt+0x158,'Q',self.callback(P,[P,P,P],set_facing))
        self.patch(0x7a4d0,P,[P],lambda block:self.state((self.read(block+8,'I')<<4)|(2 if self.read(block+8,'I')==54 else 0)))
        for addr,tile in [(0x140b248,4),(0x140b310,48),(0x140b330,52),(0x140b340,54)]:self.game.w(addr,'Q',self.blocks[tile])
        self.w(self.vt+0x188,'Q',self.callback(I,[P,P],self.remove))
        self.w(self.vt+0x10,'Q',self.callback(P,[P,P,P],self.entity))
        for pc in [0x14e4b0,0x4c0120]:self.patch(pc,P,[P,P,P],self.shared_cast)
        self.patch(0x597390,None,[P,P,U],self.loot)
        # Preserve the original mob-choice routine and key weights. Entity-name
        # lookup is supplied as token strings, rather than claiming to replay
        # the entire global entity registry and ResourceLocation implementation.
        self.mob_choice_keys=[]
        def lookup(out,key):
            self.mob_choice_keys.append(key)
            self.w(out,'I',key)
            return out
        def name_string(target,resource):
            key=self.read(resource,'I')
            self.w(target+32,'Q',7)
            data=C.create_string_buffer(f'{key:08x}'.encode('utf-16le')+b'\0\0')
            self.keep.append(data)
            self.call(0x67840,None,[P,P,U],[target,C.addressof(data),8])
            return target
        self.patch(0x27df50,P,[P,U],lookup)
        self.patch(0xcdacb0,P,[P,P],name_string)
        self.patch(0xe6cff0,I,[P],lambda guard:0 if self.read(guard,'B') else 1)
        self.patch(0xe6d000,None,[P],lambda guard:self.w(guard,'B',1))
        self.patch(0xe6cfe0,I,[P,P,P],lambda *args:0)
        def resource(out,string):
            length=self.read(string+24,'Q')
            data=self.read(string+8,'Q') if self.read(string+32,'Q')>=8 else string+8
            key=int(C.string_at(data,length*2).decode('utf-16le'),16)
            self.w(out,'I',key)
            return out
        self.patch(0xcda2f0,P,[P,P],resource)
        self.patch(0xcdab00,None,[P],lambda string:None)

    def solid_render(self,tile):
        return tile not in [0,8,9,10,11,20,52,54]

    def initial(self,x,y,z):
        ox,oy,oz=self.position
        dx,dy,dz=x-ox,y-oy,z-oz
        if self.profile=='orient':
            for i,id in enumerate(self.HORIZONTAL):
                delta=self.DIRECTIONS[id]
                if [dx,dy,dz]==list(delta):return self.neighbors[i]
            return 0
        if self.profile=='floor_hole' and dy==-1:return 0
        if self.profile=='ceiling_hole' and dy==4:return 0
        if self.profile=='unsupported' and dy==-2:return 0
        if self.profile=='many_holes' and dy in [0,1]:return 0
        if self.profile=='no_holes':return 1
        if self.profile=='old_chests' and (dx,dy,dz) in [(0,0,1),(1,0,0)]:return 54|(2<<16)
        if self.profile=='old_spawner' and (dx,dy,dz)==(0,0,0):return 52
        tunnel=dz==0 and dy in [0,1] and (self.profile!='one_hole' or dx>=0)
        if self.profile=='four_holes':tunnel=tunnel or dx==0 and dy in [0,1]
        return 0 if tunnel else 1

    def get_block(self,world,x,y,z):
        state=self.changed.get((x,y,z),self.initial(x,y,z))
        self.read_hash.update(struct.pack('<4i',x,y,z,state))
        self.read_count+=1
        return ((state&65535)<<4)|((state>>16)&15)

    def remove(self,level,pos):
        xyz=self.xyz(pos)
        self.removals.append(xyz)
        self.operations.append(['remove',*xyz])
        self.changed[tuple(xyz)]=0
        return 1

    def set_block(self,level,x,y,z,tile,meta,flags):
        result=super().set_block(level,x,y,z,tile,meta,flags)
        self.operations.append(['set',x,y,z,tile|(meta<<16),flags])
        return result

    def entity(self,out,level,pos):
        xyz=self.xyz(pos)
        tile=self.changed.get(tuple(xyz),self.initial(*xyz))&65535
        kind=0 if self.profile=='wrong_entities' else 0x1e00201 if tile==54 else 0x1200201 if tile==52 else 0
        self.entity_queries.append([*xyz,kind])
        self.operations.append(['entity',*xyz,kind])
        obj=self.alloc(128)
        vt=self.alloc(16)
        self.w(vt,'Q',self.callback(U,[P],lambda obj,kind=kind:kind))
        self.w(obj,'Q',vt)
        self.entity_positions[obj]=xyz
        logic=self.alloc(64)
        lvt=self.alloc(64)
        self.w(lvt+0x18,'Q',self.callback(None,[P,P],self.mob))
        self.w(logic,'Q',lvt)
        self.w(obj+0x40,'Q',logic)
        self.entity_positions[logic]=xyz
        self.w(out,'Q',obj)
        self.w(out+8,'Q',0)
        return out

    def shared_cast(self,out,obj,control):
        self.w(out,'Q',obj)
        self.w(out+8,'Q',0)
        return out

    def loot(self,obj,table,seed):
        if table!=self.game.base+0x140c3a0:raise ValueError('Unexpected original dungeon loot table')
        self.loots.append([*self.entity_positions[obj],C.c_int64(seed).value])
        self.operations.append(['loot',*self.entity_positions[obj],C.c_int64(seed).value])

    def mob(self,logic,resource):
        self.mobs.append([*self.entity_positions[logic],self.read(resource,'I')])
        self.operations.append(['mob',*self.entity_positions[logic],self.read(resource,'I')])

    def dungeon(self,seed,position,profile):
        self.reset_world(profile,128,position)
        self.removals=[]
        self.operations=[]
        self.entity_queries=[]
        self.entity_positions={}
        self.loots=[]
        self.mobs=[]
        pos=self.alloc(12)
        for i,value in enumerate(position):self.w(pos+4*i,'i',value)
        self.call(0x596c90,None,[P,U],[self.random,seed&0xffffffffffffffff])
        result=self.call(0x497e40,I,[P,P,P,P],[self.feature,self.world,self.random,pos])&255
        after=self.call(0x5970e0,U,[P],[self.random])
        return {'seed':seed,'position':position,'profile':profile,'result':bool(result),
                **self.world_result(),'removals':self.removals,'entity_queries':self.entity_queries,
                'loot_seeds':self.loots,'spawner_mobs':self.mobs,'operations':self.operations,'random_after':C.c_int64(after).value}

    def chest(self,neighbors,facing):
        self.reset_world('orient',128,[0,64,0])
        self.neighbors=neighbors
        pos=self.alloc(12)
        self.w(pos+4,'i',64)
        state=self.call(0x1746f0,P,[P,P,P,P],[self.blocks[54],self.world,pos,self.state((54<<4)|facing)])
        packed=self.read(state+8,'I')
        data=self.call(0x176000,I,[P,P],[self.blocks[54],state])
        return {'neighbors':neighbors,'facing':facing,'state':(packed>>4)|((packed&15)<<16),
                'native_data':data,'read_count':self.read_count,'read_sha256':self.read_hash.hexdigest()}
