"""Original tree kernels and shared tree helpers with controlled voxel APIs."""
from native_mushrooms import MushroomOracle,C,P,U,I


class TreeOracle(MushroomOracle):
    def __init__(self,executable,library):
        super().__init__(executable,library)
        for address,tile in [(0x1409f88,17),(0x140ae30,162),(0x140b258,6),(0x140b508,106)]:self.game.w(address,'Q',self.blocks[tile])
        self.treevt=self.alloc(64)
        self.w(self.treevt+0x20,'Q',self.game.base+0x2a4960)
        for address,tile,shared in [(0x14188f0,3,[60]),(0x1418fe0,2,[110,170]),(0x1419000,106,[])]:
            mat=self.read(self.blocks[tile]+0x170,'Q')
            self.game.w(address,'Q',mat)
            for id in shared:self.w(self.blocks[id]+0x170,'Q',mat)
        self.properties['wood']=self.alloc(16)
        self.properties['log_axis']=self.alloc(16)
        self.game.w(0x142dc70,'Q',self.properties['wood'])
        self.game.w(0x142dc40,'Q',self.types[2])
        self.game.w(0x1418e00,'Q',self.properties['log_axis'])
        self.log_axes=[]
        self.axis_boxes=[]
        bvt=self.alloc(64)
        self.w(bvt+0x18,'Q',self.callback(I,[P,P],lambda box,type:1))
        for id in range(4):
            axis=self.alloc(80)
            self.w(axis+8,'I',id)
            self.w(axis+0x40,'I',id)
            self.log_axes.append(axis)
            box=self.alloc(32)
            self.w(box,'Q',bvt)
            self.w(box+16,'Q',axis)
            self.axis_boxes.append(box)
            self.game.w(0x1418e10+id*8,'Q',axis)
        self.game.w(0x1418e38,'Q',self.log_axes[0])
        self.game.w(0x1418e30,'Q',self.log_axes[2])
        for id,direction in enumerate(self.directions):
            self.w(direction+0x40,'Q',self.log_axes[1 if id<2 else 2 if id<4 else 0])
            self.w(direction+0x10,'i',[-1,-1,2,0,1,3][id])
            normal=self.alloc(12)
            for i,value in enumerate(self.DIRECTIONS[id]):self.w(normal+4*i,'i',value)
            self.w(direction+0x50,'Q',normal)
        self.vine_props={}
        self.bool_boxes=[]
        for value in [0,1]:
            box=self.alloc(32)
            self.w(box,'Q',bvt)
            self.w(box+16,'B',value)
            self.bool_boxes.append(box)
        for i,address in enumerate([0x1434240,0x1434248,0x1434230,0x1434238]):
            prop=self.alloc(16)
            self.game.w(address,'Q',prop)
            self.vine_props[prop]=1<<i
        self.game.w(0x140ab50,'Q',self.blocks[39])
        self.game.w(0x140ab58,'Q',self.blocks[40])
        self.game.w(0x140b5d0,'Q',self.blocks[127])
        for key,address in [('cocoa_age',0x1410368),('cocoa_facing',0x1416e68)]:
            self.properties[key]=self.alloc(16)
            self.game.w(address,'Q',self.properties[key])
        self.patch(0x3fdc10,I,[P,P,I],self.solid_blocking)
        self.game.w(0x14192b8,'Q',self.state((3<<4)|2))

    def property(self,state,prop):
        meta=self.read(state+8,'I')&15
        if prop==self.properties.get('wood'):return self.type_boxes[meta&3]
        if prop==self.properties.get('log_axis'):return self.axis_boxes[{0:1,4:0,8:2,12:3}[meta&12]]
        if prop==self.properties.get('cocoa_age'):return self.boxes[meta>>2]
        if prop==self.properties.get('cocoa_facing'):return self.direction_boxes[[3,4,2,5][meta&3]]
        if prop in getattr(self,'vine_props',{}):return self.bool_boxes[int(bool(meta&self.vine_props[prop]))]
        return super().property(state,prop)

    def set_property(self,state,prop,boxed):
        packed=self.read(state+8,'I')
        if prop==self.properties.get('log_axis'):
            id=self.read(self.read(boxed+16,'Q')+8,'I')
            return self.state((packed&~12)|[4,0,8,12][id])
        if prop==self.properties.get('cocoa_age'):return self.state((packed&~12)|(self.read(boxed+16,'I')<<2))
        if prop==self.properties.get('cocoa_facing'):
            direction=self.read(self.read(boxed+16,'Q')+8,'I')
            return self.state((packed&~3)|{2:2,3:0,4:1,5:3}[direction])
        if prop in getattr(self,'vine_props',{}):
            bit=self.vine_props[prop]
            return self.state((packed|bit) if self.read(boxed+16,'B') else packed&~bit)
        return super().set_property(state,prop,boxed)

    def solid_blocking(self,world,pos,when_unloaded):
        x,y,z=self.xyz(pos)
        result=False if self.profile=='wide_gaps' else (x+z)%3==0 if self.profile=='gaps' else self.solid_render(self.changed.get((x,y,z),self.initial(x,y,z))&65535)
        self.support_queries.append([x,y,z,bool(when_unloaded),bool(result)])
        return result

    def initial(self,x,y,z):
        ox,oy,oz=self.position
        if self.profile in ['shallow_water','deep_water','flowing_surface']:
            if y<0 or y>=256:return 0
            depth=3 if self.profile=='deep_water' else 1
            return 0 if y>=oy else (8 if self.profile=='flowing_surface' else 9) if y>=oy-depth else 2 if y==oy-depth-1 else 1
        if self.profile=='stone_obstacle' and (x,y,z)==(ox,oy+2,oz):return 1
        if self.profile=='vine_column' and x==ox and z==oz and oy<=y<oy+5:return 106
        if self.profile=='sapling_column' and x==ox and z==oz and oy<=y<oy+5:return 6
        return super().initial(x,y,z)

    def intersects(self,rules,minimum,maximum):
        box=[self.read(minimum+i*4,'i') for i in range(3)]+[self.read(maximum+i*4,'i') for i in range(3)]
        self.rule_queries.append(box)
        return box[0]<=self.position[0]<=box[3]

    def tree(self,kind,seed,position,profile,base_height,log,leaves,updates,jungle=False,tall=False,extra_range=15):
        self.reset_world(profile,position[1]+(2 if profile=='raised' else 0),position)
        self.fluid_queries=[]
        self.support_queries=[]
        self.w(self.feature,'Q',self.treevt)
        self.w(self.feature+8,'B',int(updates))
        self.w(self.feature+0xc,'i',base_height)
        self.w(self.feature+0x10,'B',int(jungle))
        self.w(self.feature+0x18,'Q',self.state(((log&65535)<<4)|(log>>16)))
        self.w(self.feature+0x20,'Q',self.state(((leaves&65535)<<4)|(leaves>>16)))
        self.w(self.feature+0x28,'Q',self.state(((leaves&65535)<<4)|(leaves>>16)))
        self.w(self.feature+0x30,'Q',self.state(((log&65535)<<4)|(log>>16)))
        if kind=='birch':self.w(self.feature+0x28,'B',int(tall))
        if kind in ['mega_pine','mega_pine_post','mega_jungle']:
            self.w(self.feature,'Q',self.game.base+(0x127d538 if kind=='mega_jungle' else 0x127d598))
            self.w(self.feature+0x28,'i',extra_range)
            self.w(self.feature+0x2c,'B',int(tall))
        pos=self.alloc(12)
        for i,value in enumerate(position):self.w(pos+i*4,'i',value)
        self.call(0x596c90,None,[P,U],[self.random,seed&0xffffffffffffffff])
        if kind in ['ground_bush','pine','basic','birch','swamp','savanna','roof','spruce']:
            result=bool(self.call({'ground_bush':0x33b160,'pine':0x5345c0,'basic':0x6f5eb0,'birch':0x8f390,'swamp':0x6ca560,'savanna':0x5c6ec0,'roof':0x5bfc60,'spruce':0x672fb0}[kind],I,[P,P,P,P],[self.feature,self.world,self.random,pos])&255)
        elif kind in ['mega_pine','mega_pine_post','mega_jungle']:
            result=self.call(0x4517f0 if kind=='mega_pine' else 0x450720 if kind=='mega_jungle' else 0x451c40,None if kind=='mega_pine_post' else I,[P,P,P,P],[self.feature,self.world,self.random,pos])
            if kind!='mega_pine_post':result=bool(result&255)
        else:
            result=self.call(0x1c560 if kind=='trunk' else 0x1cb80,I if kind=='trunk' else None,[P,P,P,P,I],[self.feature,self.world,pos,self.random,base_height])
            if kind=='trunk':result=bool(result&255)
        after=self.call(0x5970e0,U,[P],[self.random])
        return {'kind':kind,'seed':seed,'position':position,'profile':profile,'base_height':base_height,**({'extra_range':extra_range} if kind in ['mega_pine','mega_pine_post','mega_jungle'] else {}),'log':log,'leaves':leaves,'updates':updates,'jungle':jungle,'tall':tall,
                'result':result,**self.world_result(),'support_queries':self.support_queries,'fluid_queries':self.fluid_queries,'light_queries':self.light_queries,'random_after':C.c_int64(after).value}

    def tree_predicates(self):
        return [{'state':tile|(meta<<16),'result':bool(self.call(0x1c390,I,[P,P],[self.feature,self.blocks[tile]])&255)} for tile in range(256) for meta in [0,1,3]]

    def log_data_cases(self):
        return [{'state':17|(meta<<16),'native_data':self.call(0x5036e0,I,[P,P],[self.blocks[17],self.state((17<<4)|meta)])} for meta in range(16)]

    def plant_data_cases(self):
        return [{'state':tile|(meta<<16),'native_data':self.call(pc,I,[P,P],[self.blocks[tile],self.state((tile<<4)|meta)])} for tile,pc,metas in [(106,0x774820,range(16)),(127,0x1a9010,range(12))] for meta in metas]
