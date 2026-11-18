"""Original plant scatters AND their block-survival virtuals, with voxel APIs.
Materials, fluid states, dimension flags and world-protection boxes are supplied
inputs. This does not substitute a Java survival callback in the original code.
"""
from native_dungeons import DungeonOracle,C,P,U,I
import struct


class FloraOracle(DungeonOracle):
    FEATURES={'flower':0x2ea4c0,'tall_grass':0x6d3a90,'dead_bush':0x1f72d0,
              'waterlily':0x7813d0,'cactus':0x159aa0,'reeds':0x5b48a0,'double_plant':0x215bf0,'mushroom':0x139230}
    BLOCK_VTABLES={37:0x1258458,38:0x1258458,31:0x12dec30,32:0x1236d98,
                   81:0x1220a48,83:0x12b2e98,111:0x12f2e58,175:0x123ca68,39:0x128b798,40:0x128b798}
    NONSOLID={0,8,9,10,11,31,32,37,38,39,40,83,111,175}
    REPLACEABLE={0,8,9,10,11,31,32,37,38,39,40,83,175}

    def __init__(self,executable,library):
        super().__init__(executable,library)
        self.matvt=self.alloc(128)
        self.w(self.matvt+0x10,'Q',self.callback(I,[P],lambda mat:self.read(mat+8,'I')==2))
        self.w(self.matvt+0x20,'Q',self.callback(I,[P],lambda mat:self.read(mat+8,'I')==1))
        self.w(self.matvt+0x60,'Q',self.callback(I,[P],lambda mat:self.read(mat+12,'I')))
        # Controlled material identities, shared where the native rules compare
        # pointer identity. No claim is made to have initialized every block.
        for tile,block in self.blocks.items():
            mat=self.read(block+0x170,'Q')
            self.w(mat,'Q',self.matvt)
            self.w(mat+8,'I',2 if tile in [8,9,10,11] else 0 if tile in self.NONSOLID else 1)
            self.w(mat+12,'I',int(tile in self.REPLACEABLE))
        for tiles,global_address in [([0],0x14188d0),([18,161],0x14187a0),([79,212],0x14188e8),([8,9],0x1418330),([10,11],0x1418328)]:
            mat=self.read(self.blocks[tiles[0]]+0x170,'Q')
            for tile in tiles:self.w(self.blocks[tile]+0x170,'Q',mat)
            self.game.w(global_address,'Q',mat)
        for tile,vt in self.BLOCK_VTABLES.items():self.w(self.blocks[tile],'Q',self.game.base+vt)
        for address,tile in [(0x1409fb0,2),(0x1409fa8,3),(0x1409fb8,60),(0x140ab38,12),
                             (0x140b6d8,172),(0x140b6b8,159),(0x140b430,81),(0x140b440,83),
                             (0x140ab98,32),(0x1409f78,31),(0x140b558,111),(0x140b700,175),
                             (0x140b7b0,212)]:self.game.w(address,'Q',self.blocks[tile])
        self.w(self.vt+0x100,'Q',self.callback(P,[P,P],lambda level,pos:self.blocks[self.get_block(level,*self.xyz(pos))>>4]))
        self.w(self.vt+0x110,'Q',self.callback(P,[P,P],self.fluid))
        self.patch(0x3f1a90,I,[P,P],self.raw_brightness)
        self.dimension=self.alloc(128)
        self.w(self.world+0x1c8,'Q',self.dimension)
        self.patch(0xaf4a20,P,[P],lambda app:self.rules if self.profile=='protected' else 0)
        # The original Direction::Plane method returns the same literal N,E,S,W
        # cached enum table used by dungeons and cactus.
        self.game.w(0x1413e70,'B',1)
        self.properties={}
        for key,address in [('half',0x1414178),('variant',0x1414170),('level',0x14188e0),('dirt',0x1413ef8)]:
            self.properties[key]=self.alloc(16)
            self.game.w(address,'Q',self.properties[key])
        self.types=[]
        self.type_boxes=[]
        boxvt=self.alloc(64)
        self.w(boxvt+0x18,'Q',self.callback(I,[P,P],lambda box,type:1))
        for id in range(6):
            enum=self.alloc(32)
            self.w(enum+8,'I',id)
            self.types.append(enum)
            box=self.alloc(32)
            self.w(box,'Q',boxvt)
            self.w(box+16,'Q',enum)
            self.type_boxes.append(box)
        self.game.w(0x1413f18,'Q',self.types[2])
        self.halves=[]
        self.half_boxes=[]
        for id,address in enumerate([0x1414188,0x1414190]):
            enum=self.alloc(32)
            self.w(enum+8,'I',id)
            self.game.w(address,'Q',enum)
            self.halves.append(enum)
            box=self.alloc(32)
            self.w(box,'Q',boxvt)
            self.w(box+16,'Q',enum)
            self.half_boxes.append(box)
        self.w(self.statevt+0x150,'Q',self.callback(P,[P,P],self.property))
        self.w(self.statevt+0x158,'Q',self.callback(P,[P,P,P],self.set_property))
        self.patch(0x7a4d0,P,[P],lambda block:self.state(self.read(block+8,'I')<<4))
        for tile in [39,40]:self.w(self.blocks[tile]+0x190,'Q',self.state(tile<<4))

    def property(self,state,prop):
        meta=self.read(state+8,'I')&15
        if prop==self.properties['level']:return self.boxes[meta]
        if prop==self.properties['half']:return self.half_boxes[0 if meta&8 else 1]
        if prop==self.properties['variant']:return self.type_boxes[meta&7]
        if prop==self.properties['dirt']:return self.type_boxes[meta]
        raise ValueError('Unbound plant state property')

    def solid_render(self,tile):return tile not in self.NONSOLID and tile not in [18,20,52,54,161]

    def set_property(self,state,prop,boxed):
        meta=self.read(state+8,'I')&15
        enum=self.read(boxed+16,'Q')
        if prop==self.properties['half']:meta=8 if enum==self.halves[0] else meta&7
        elif prop==self.properties['variant']:meta=(meta&8)|self.read(enum+8,'I')
        else:raise ValueError('Unbound plant state setter')
        return self.state((175<<4)|meta)

    def initial(self,x,y,z):
        if self.profile=='rule':return self.rule_blocks.get((x,y,z),0)
        if y<0 or y>=256:return 0
        # Surface height follows the supplied position so ceiling/height edges
        # exercise the actual original scatter branches, not only empty space.
        surface=self.position[1]-1
        if self.profile=='canopy' and surface+1<=y<=surface+3:return 18
        if y>surface:return 0
        if y<surface:return 1
        if self.profile in ['water','flowing','flowing_level','ice','packed_ice']:
            return {'water':9,'flowing':8,'flowing_level':8|(1<<16),'ice':79,'packed_ice':174}[self.profile]
        if self.profile in ['sand','wet_sand','waterlogged','frosted','lava_side']:
            if (x+z)%4==0:
                return 9 if self.profile=='wet_sand' else 20 if self.profile=='waterlogged' else 212 if self.profile=='frosted' else 10 if self.profile=='lava_side' else 12
            return 12
        if self.profile=='terracotta':return 172 if x&1 else 159|(3<<16)
        if self.profile=='dirt':return 3|(1<<16)
        if self.profile=='podzol':return 3|(2<<16)
        if self.profile=='mycelium':return 110
        if self.profile=='farmland':return 60|(7<<16)
        if self.profile=='stone':return 1
        return 2

    def reset_world(self,profile,height,position):
        super().reset_world(profile,height,position)
        self.operations=[]
        self.light_queries=[]

    def get_block(self,world,x,y,z):
        state=self.changed.get((x,y,z),self.initial(x,y,z))
        self.read_hash.update(struct.pack('<4i',x,y,z,state))
        self.read_count+=1
        return ((state&65535)<<4)|((state>>16)&15)

    def fluid(self,world,pos):
        x,y,z=self.xyz(pos)
        # Distinct controlled fluid lookups are recorded separately from block
        # probes, because they need not have the block's material identity.
        block=self.changed.get((x,y,z),self.initial(x,y,z))&65535
        water=block in [8,9] or self.profile=='waterlogged' and block==20
        if self.profile=='rule':water=(x,y,z) in self.rule_fluids
        self.fluid_queries.append([x,y,z,int(water)])
        return self.state((9 if water else 0)<<4)

    def raw_brightness(self,world,pos):
        value=self.brightness if self.profile=='rule' else 15 if self.profile=='bright' else 12
        self.light_queries.append([*self.xyz(pos),value])
        return value

    def scatter(self,kind,seed,position,profile,state,ceiling=False):
        self.reset_world(profile,256,position)
        self.fluid_queries=[]
        self.w(self.dimension+0x29,'B',int(ceiling))
        self.w(self.feature+0x10,'Q',self.blocks[state&65535] if kind in ['flower','mushroom'] else self.types[state>>16] if kind=='double_plant' else self.state(((state&65535)<<4)|(state>>16)))
        self.w(self.feature+0x18,'Q',self.state(((state&65535)<<4)|(state>>16)))
        pos=self.alloc(12)
        for i,value in enumerate(position):self.w(pos+4*i,'i',value)
        self.call(0x596c90,None,[P,U],[self.random,seed&0xffffffffffffffff])
        result=self.call(self.FEATURES[kind],I,[P,P,P,P],[self.feature,self.world,self.random,pos])&255
        after=self.call(0x5970e0,U,[P],[self.random])
        return {'kind':kind,'seed':seed,'position':position,'profile':profile,'state':state,'ceiling':ceiling,
                'result':bool(result),**self.world_result(),'fluid_queries':self.fluid_queries,'light_queries':self.light_queries,'random_after':C.c_int64(after).value}

    def rule(self,kind,state,position,blocks,fluids,brightness=12):
        self.reset_world('rule',256,position)
        self.rule_blocks={tuple(row[:3]):row[3] for row in blocks}
        self.rule_fluids={tuple(row) for row in fluids}
        self.fluid_queries=[]
        self.brightness=brightness
        pos=self.alloc(12)
        for i,value in enumerate(position):self.w(pos+4*i,'i',value)
        block=self.blocks[state&65535]
        pc=self.read(self.read(block,'Q')+(0x2a0 if kind=='may_place' else 0x358),'Q')-self.game.base
        result=self.call(pc,I,[P,P,P,P],[block,self.world,pos,self.state(((state&65535)<<4)|(state>>16))])&255
        return {'kind':kind,'state':state,'position':position,'blocks':blocks,'fluids':fluids,
                'brightness':brightness,'result':bool(result),**self.world_result(),'fluid_queries':self.fluid_queries,'light_queries':self.light_queries}
