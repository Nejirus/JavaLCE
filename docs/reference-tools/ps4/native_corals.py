"""Original coral kernels over explicit world, property and registry storage.

The original property constructors, block default assignments, metadata getters,
survival routines and mutable Direction::Plane array execute. Materials, face
support, collision placement, loaded chunks and successful writes are inputs.
"""
from native_aquatics import AquaticOracle,C,P,U,I


class CoralOracle(AquaticOracle):
    def __init__(self,executable,library):
        super().__init__(executable,library)
        self.coral_properties={}
        self.coral_property_inputs=[]
        # Original LEA RSI (UTF-16 name), MOV EDX (upper bound), property alias.
        specs=[('plant',0x1e87ee,0x1e880f,0x14133a8),
               ('block',0x1e8a77,0x1e8a98,0x1413560),
               ('block_dead',0x1e8abf,None,0x1413568),
               ('fan',0x1e8d4d,0x1e8d71,0x1413588),
               ('fan_direction',0x1e8d98,0x1e8dcf,0x1413590),
               ('hang',0x1e8e31,0x1e8e64,0x1413598),
               ('hang_direction',0x1e8e8b,0x1e8ec2,0x14135a0),
               ('hang_dead',0x1e8ee9,None,0x1413768)]
        for key,pc,bound_pc,alias in specs:
            if C.string_at(self.game.base+pc,3)!=b'\x48\x8d\x35':raise ValueError('Unexpected original property name LEA')
            pointer=self.game.base+pc+7+self.game.read(pc+3,'i')
            raw=C.string_at(pointer,64);name_text=raw.decode('utf-16le').split('\0')[0]
            name=self.alloc(40);self.w(name+32,'Q',7)
            self.call(0x67840,None,[P,P,U],[name,pointer,len(name_text)])
            if bound_pc is None:prop=self.call(0xdabb0,P,[P],[name])
            else:
                if self.game.read(bound_pc,'B')!=0xba:raise ValueError('Unexpected property upper-bound argument')
                bound=self.game.read(bound_pc+1,'I')
                prop=self.call(0x37a180,P,[P,I,I],[name,0,bound])
            self.coral_properties[key]=prop;self.game.w(alias,'Q',prop)
            count=self.call(self.read(self.read(prop,'Q')+0x40,'Q')-self.game.base,I,[P],[prop])
            self.coral_property_inputs.append({'key':key,'alias':hex(alias),'name':name_text,'count':count,'name_pc':hex(pc),'bound_pc':None if bound_pc is None else hex(bound_pc)})
        self.patch(0x92480,None,[P],lambda block:None)
        self.patch(0xcf850,None,[P,P,P,I],self.definition)
        self.default_cases=[];self.definition_inputs=[]
        for tile,pc in [(259,0x1c6c50),(263,0x1c62b0),(264,0x1c9b90),(265,0x1c9b90),(266,0x1cb1f0),(267,0x1cb1f0),(268,0x1cb1f0)]:
            for meta in range(16):
                self.base_meta=meta;self.w(self.blocks[tile]+0x190,'Q',self.state((tile<<4)|meta))
                self.call(pc,None,[P],[self.blocks[tile]])
                state=self.read(self.blocks[tile]+0x190,'Q');vt=self.read(self.blocks[tile],'Q')
                definition=self.call(self.read(vt+0x478,'Q')-self.game.base,P,[P],[self.blocks[tile]])
                self.host.free(definition)
                self.default_cases.append({'base':tile|(meta<<16),'state':self.unpacked(state),'native_data':self.call(self.read(vt+0x50,'Q')-self.game.base,I,[P,P],[self.blocks[tile],state])})
        self.data_cases=[]
        for tile,metas in [(259,list(range(6))+list(range(8,14))),(263,range(5)),(264,list(range(5))+list(range(8,13))),(265,list(range(5))+list(range(8,13))),(266,range(16)),(267,range(16)),(268,range(16))]:
            pc=self.read(self.read(self.blocks[tile],'Q')+0x50,'Q')-self.game.base
            for meta in metas:self.data_cases.append({'state':tile|(meta<<16),'native_data':self.call(pc,I,[P,P],[self.blocks[tile],self.state((tile<<4)|meta)])})
        # The supplied StateDefinition starts from metadata zero. Original
        # constructors leave plant unchanged, assign fan=3/direction=1 and
        # hanging fan=0/direction=2/dead=false.
        for tile,meta in [(259,0),(263,0),(264,11),(265,11),(266,8),(267,8),(268,8)]:self.w(self.blocks[tile]+0x190,'Q',self.state((tile<<4)|meta))
        self.w(self.statevt+0x98,'Q',self.callback(I,[P,P,I],self.supports_face))
        self.w(self.vt+0xb0,'Q',self.game.base+0x3eecb0)
        self.patch(0x3fc380,I,[P,P,P],self.can_place_state)
        self.chunk_source=self.alloc(32);chunk_vt=self.alloc(0x100)
        self.w(self.chunk_source,'Q',chunk_vt);self.w(self.world+0x1f8,'Q',self.chunk_source)
        self.w(chunk_vt+0x50,'Q',self.callback(I,[P,I,I],self.has_chunk))
        self.w(self.blocks[263]+0x170,'Q',self.read(self.blocks[31]+0x170,'Q'))
        for tile in [264,265,266,267,268]:self.w(self.blocks[tile]+0x170,'Q',self.read(self.blocks[31]+0x170,'Q'))
        self.w(self.statevt+0x170,'Q',self.callback(P,[P],lambda state:self.blocks[self.read(state+8,'I')>>4]))

    def definition(self,obj,block,properties,count):
        keys=[self.read(properties+8*i,'Q') for i in range(count)]
        self.definition_inputs.append({'tile':self.read(block+8,'H'),'properties':[next(k for k,v in self.coral_properties.items() if v==p) for p in keys]})

    def property(self,state,prop):
        meta=self.read(state+8,'I')&15;p=getattr(self,'coral_properties',{})
        if prop in [p.get('plant'),p.get('block'),p.get('fan')]:return self.boxes[meta&7]
        if prop==p.get('block_dead'):return self.bool_boxes[(meta>>3)&1]
        if prop==p.get('fan_direction'):return self.boxes[(meta>>3)&1]
        if prop==p.get('hang'):return self.boxes[(meta>>1)&1]
        if prop==p.get('hang_direction'):return self.boxes[(meta>>2)&3]
        if prop==p.get('hang_dead'):return self.bool_boxes[meta&1]
        return super().property(state,prop)

    def set_property(self,state,prop,boxed):
        packed=self.read(state+8,'I');value=self.read(boxed+16,'I');p=getattr(self,'coral_properties',{})
        if prop in [p.get('plant'),p.get('block'),p.get('fan')]:return self.state((packed&~7)|value)
        if prop in [p.get('block_dead'),p.get('fan_direction')]:return self.state((packed&~8)|((value&1)<<3))
        if prop==p.get('hang'):return self.state((packed&~2)|((value&1)<<1))
        if prop==p.get('hang_direction'):return self.state((packed&~12)|((value&3)<<2))
        if prop==p.get('hang_dead'):return self.state((packed&~1)|(value&1))
        return super().set_property(state,prop,boxed)

    def supports_face(self,state,direction,kind):
        value=self.unpacked(state);result=self.face_support if self.profile=='coral_rule' else self.solid_blocking_state(value)
        self.face_queries.append([value,self.read(direction+8,'I'),kind,bool(result)]);return result

    def can_place_state(self,world,pos,state):
        result=self.placement=='all' or self.placement=='checker' and sum(self.xyz(pos))%3!=0
        self.state_placement_queries.append([*self.xyz(pos),self.unpacked(state),bool(result)]);return result

    def has_chunk(self,source,x,z):
        result=self.loaded=='all' or self.loaded=='checker' and (x+z)%3!=0
        self.chunk_queries.append([x,z,bool(result)]);return result

    def effective(self,x,y,z):
        if self.profile=='coral_rule':
            self.effective_queries.append([x,y,z,self.rule_effective]);return self.rule_effective
        return super().effective(x,y,z)

    def solid_render(self,tile):return tile not in {0,8,9,10,11,258,263,264,265,266,267,268,270,271,18,161,20,52,54}

    def initial(self,x,y,z):
        if self.profile=='coral_rule':return self.rule_blocks.get((x,y,z),0)
        ox,oy,oz=self.position;dx,dz=x-ox,z-oz
        if self.profile=='well_sand':return 0 if y>oy else 12 if y==oy else 1
        if self.profile=='air' or y<0 or y>=256:return 0
        floor=oy-1 if self.profile.startswith('anemone_') else oy if self.profile.startswith('hang_') else oy-2
        if self.profile=='steps':floor+=((x*3+z*7)%5)-2
        if y<floor:return 1
        if y==floor:
            if self.profile.startswith('hang_') and (x,z)!=(ox,oz):
                if self.profile=='hang_dry':return 0
                if self.profile=='hang_north' and (dx,dz)!=(0,-1):return 1
                if self.profile=='hang_east' and (dx,dz)!=(1,0):return 1
                if self.profile=='hang_checker' and (x+z)%3==0:return 1
                return 9
            if self.profile.endswith('dirt'):return 3
            if self.profile.endswith('live'):return 259|(2<<16)
            if self.profile.endswith('dead'):return 259|(10<<16)
            return 1
        if y>=64:return 0
        if self.profile=='hang_dry':return 0
        if self.profile=='hang_north' and (dx,dz)!=(0,-1):return 1
        if self.profile=='hang_east' and (dx,dz)!=(1,0):return 1
        if self.profile=='hang_checker' and (x+z)%3==0:return 1
        if self.profile=='flowing':return 8|(((x+z)%10)<<16)
        if self.profile=='waterlogged' and (x+z)%3==0:return 20
        return 9

    def begin_coral(self,profile,position,placement,loaded,plane):
        pos=self.begin(profile,position,placement);self.loaded=loaded
        self.support_queries=[];self.face_queries=[];self.chunk_queries=[];self.state_placement_queries=[]
        for i,direction in enumerate(plane):self.game.w(0x1413e50+8*i,'Q',self.directions[direction])
        return pos

    def plane(self):return [self.read(self.game.read(0x1413e50+8*i,'Q')+8,'I') for i in range(4)]

    def coral_result(self):return {**self.result(),'support_queries':self.support_queries,'face_queries':self.face_queries,'chunk_queries':self.chunk_queries,'state_placement_queries':self.state_placement_queries,'plane_after':self.plane()}

    def coral(self,kind,seed,position,profile,placement='all',loaded='all',client=False,plane=(2,5,3,4),direction=2):
        pos=self.begin_coral(profile,position,placement,loaded,plane)
        self.w(self.world+0x248,'B',int(client));self.w(self.feature,'Q',self.treevt);self.w(self.feature+8,'B',0)
        self.call(0x596c90,None,[P,U],[self.random,seed&0xffffffffffffffff])
        if kind=='fan':result=self.call(0x1cc550,None,[P,P,P,P],[self.feature,self.world,pos,self.random])
        elif kind=='side_fan':result=self.call(0x1cc720,None,[P,P,P,P,P],[self.feature,self.world,pos,self.random,self.directions[direction]])
        else:result=bool(self.call({'anemone':0x5f4f30,'hang':0x1d1a50}[kind],I,[P,P,P,P],[self.feature,self.world,self.random,pos])&255)
        return {'kind':kind,'seed':seed,'position':position,'profile':profile,'placement':placement,'loaded':loaded,'client':client,'plane':list(plane),'direction':direction,'result':result,**self.coral_result(),'random_after':C.c_int64(self.call(0x5970e0,U,[P],[self.random])).value}

    def coral_rule(self,kind,below,above,effective,face_support,placement):
        position=[0,64,0];pos=self.begin_coral('coral_rule',position,placement,'all',self.HORIZONTAL)
        self.rule_blocks={(0,63,0):below,(0,65,0):above};self.rule_effective=effective;self.face_support=face_support
        # AquaticOracle.effective accepts a separate explicit fluid input.
        self.rule_blocking=face_support
        pc=0x1c6420 if kind=='plant' else 0x1390c0
        tile=263 if kind=='plant' else 264
        result=bool(self.call(pc,I,[P,P,P,P],[self.blocks[tile],self.world,pos,self.state(tile<<4)])&255)
        return {'kind':kind,'position':position,'profile':'coral_rule','below':below,'above':above,'effective':effective,'face_support':face_support,'placement':placement,'result':result,**self.coral_result()}

    def sequence(self,seed,steps):
        self.call(0x596c90,None,[P,U],[self.random,seed&0xffffffffffffffff]);plane=self.HORIZONTAL;results=[]
        for kind,profile,position in steps:
            pos=self.begin_coral(profile,position,'all','all',plane);self.w(self.world+0x248,'B',0)
            self.w(self.feature,'Q',self.treevt);self.w(self.feature+8,'B',0)
            if kind=='well':
                self.call(0x1fb400,None,[P],[self.feature]);pc=0x1fa530
            else:pc=0x1d1a50
            result=bool(self.call(pc,I,[P,P,P,P],[self.feature,self.world,self.random,pos])&255)
            results.append({'kind':kind,'profile':profile,'position':position,'placement':'all','loaded':'all','client':False,'plane':list(plane),'result':result,**self.coral_result()})
            plane=self.plane()
        return {'seed':seed,'steps':results,'random_after':C.c_int64(self.call(0x5970e0,U,[P],[self.random])).value}
