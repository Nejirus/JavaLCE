"""Original vine kernel, attachment rule, RTTI cast and exception identities."""
from native_trees import TreeOracle,C,P,U,I
from pathlib import Path
import json


class VineOracle(TreeOracle):
    def __init__(self,executable,library):
        super().__init__(executable,library)
        registry=json.loads(Path(__file__).with_name('block-registry-native.json').read_text())
        self.ids={row['name']:row['id'] for row in registry['registrations']};self.ids['air']=0
        vt=self.alloc(528);self.w(vt+8,'Q',self.game.base+0x1218338);C.memmove(vt+16,self.blockvt,512)
        self.blockvt=vt+16
        for tile in self.ids.values():
            if tile not in self.blocks:
                block=self.alloc(512);self.blocks[tile]=block;self.w(block+8,'I',tile);self.w(block+0x170,'Q',self.read(self.blocks[1]+0x170,'Q'))
        for block in self.blocks.values():self.w(block,'Q',self.blockvt)
        for alias in registry['aliases']:self.game.w(int(alias['alias'],16),'Q',self.blocks[self.ids[alias['name']]])
        for name,tile in self.ids.items():
            if name.endswith('shulker_box'):self.w(self.blocks[tile],'Q',self.game.base+0x12bfa10)
        self.w(self.blocks[106],'Q',self.game.base+0x12ef910)
        # Execute libc's original __dynamic_cast with original game RTTI.
        # Data-vtable relocations retain their +16 addends in Module.bind.
        for nid in ['hMAe+TWS9mQ','byV+FWlAnB4','pZ9WXcClPO8','9ByRMdo7ywg','aeHxLWwq0gQ']:self.game.bind(nid,P(self.lib.base+self.lib.exports[nid]))
        self.w(self.statevt+0x138,'Q',self.callback(I,[P,P,P,P],self.face_shape))
        self.exception_inputs=[{'name':name,'state':tile,'result':bool(self.call(0x7729e0,I,[P],[self.blocks[tile]])&255)} for name,tile in self.ids.items()]

    def initial(self,x,y,z):
        if self.profile=='vine_rules':return self.rule_blocks.get((x,y,z),0)
        ox,oy,oz=self.position
        if y<0 or y>=256:return 0
        if self.profile in ['stone_wall','leaf_wall','glass_wall','trapdoor_wall','shulker_wall'] and z==oz+1 and -4<=x-ox<=4:return {'stone_wall':1,'leaf_wall':18,'glass_wall':20,'trapdoor_wall':274,'shulker_wall':260}[self.profile]
        if self.profile=='mixed_walls' and ((x-ox+3)%7==0 or (z-oz+2)%5==0):return [1,18,20,33,260,274][(x*3+z+y)%6]
        if self.profile=='old_vines' and x==ox and z==oz and y<oy+5:return 106
        return super().initial(x,y,z)

    def face_shape(self,state,world,pos,direction):
        xyz=self.xyz(pos);facing=self.read(direction+8,'i');tile=self.read(state+8,'I')>>4
        result=self.face_map.get((*xyz,facing),1) if self.profile=='vine_rules' else 1 if tile in [0,8,9,10,11,50,75,76,106] else 0
        self.face_queries.append([*xyz,facing,result]);return result

    def rule(self,tile,above,direction,side_shape,top_shape):
        position=[0,64,0];self.reset_world('vine_rules',128,position);self.fluid_queries=[];self.support_queries=[];self.face_queries=[]
        dx,dy,dz=self.DIRECTIONS[direction^1];neighbor=(dx,64+dy,dz)
        self.rule_blocks={(0,65,0):above,neighbor:tile};self.face_map={(*neighbor,direction):side_shape,(0,65,0,1):top_shape}
        pos=self.alloc(12);self.w(pos+4,'i',64)
        result=bool(self.call(0x772800,I,[P,P,P,P],[self.blocks[106],self.world,pos,self.directions[direction]])&255)
        return {'position':position,'profile':'vine_rules','neighbor':tile,'above':above,'direction':direction,'side_shape':side_shape,'top_shape':top_shape,'result':result,
                **self.world_result(),'face_queries':self.face_queries,'fluid_queries':self.fluid_queries,'light_queries':self.light_queries}

    def vines(self,seed,position,profile):
        self.reset_world(profile,128,position);self.face_queries=[];self.fluid_queries=[];self.support_queries=[]
        pos=self.alloc(12)
        for i,v in enumerate(position):self.w(pos+4*i,'i',v)
        self.call(0x596c90,None,[P,U],[self.random,seed&0xffffffffffffffff])
        result=bool(self.call(0x775260,I,[P,P,P,P],[self.feature,self.world,self.random,pos])&255)
        return {'seed':seed,'position':position,'profile':profile,'result':result,**self.world_result(),'face_queries':self.face_queries,
                'fluid_queries':self.fluid_queries,'light_queries':self.light_queries,'random_after':C.c_int64(self.call(0x5970e0,U,[P],[self.random])).value}
