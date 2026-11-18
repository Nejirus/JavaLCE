"""Original natural-pumpkin feature, block placement rule and property list."""
from native_tree_selection import TreeSelectionOracle,C,P,U,I
from pathlib import Path
import json


class PumpkinOracle(TreeSelectionOracle):
    def __init__(self,executable,library):
        super().__init__(executable,library)
        registry=json.loads(Path(__file__).with_name('block-registry-native.json').read_text())
        self.pumpkin_id=next(r['id'] for r in registry['registrations'] if r['name']=='pumpkin')
        self.pumpkin=self.alloc(1024);self.blocks[self.pumpkin_id]=self.pumpkin
        self.w(self.pumpkin+8,'I',self.pumpkin_id);self.w(self.pumpkin+0x170,'Q',self.read(self.blocks[86]+0x170,'Q'));self.w(self.pumpkin+0x188,'Q',self.pumpkin)
        self.game.w(0x140ab60,'Q',self.pumpkin)
        self.age_property=self.alloc(16);self.game.w(0x14332d0,'Q',self.age_property)
        self.patch(0x367c40,None,[P,P,P],lambda *args:None)
        self.patch(0xcf850,None,[P,P,P,I],self.definition)
        definition=self.call(0x590450,P,[P],[self.pumpkin]);self.host.free(definition)
        self.w(self.statevt+0x148,'Q',self.callback(I,[P,P],self.has_property))
        self.w(self.statevt+0x98,'Q',self.callback(I,[P,P,I],self.supports_face))
        self.w(self.matvt+0x60,'Q',self.callback(I,[P],lambda mat:False if self.nonreplaceable_air and mat==self.game.read(0x14188d0,'Q') else self.read(mat+12,'I')))
        self.default_cases=[]
        for meta in range(16):
            self.base_meta=meta
            self.call(0x58e9f0,None,[P],[self.pumpkin]);state=self.read(self.pumpkin+0x190,'Q')
            self.default_cases.append({'base_meta':meta,'state':self.unpacked(state),'native_data':self.call(0x5903a0,I,[P,P],[self.pumpkin,state])})
        self.call(0x1722d0,None,[P,P],[self.feature,self.pumpkin])

    def definition(self,obj,block,properties,count):
        self.property_keys=[self.read(properties+8*i,'Q') for i in range(count)]
        self.property_inputs=[{'index':i,'property':'horizontal_facing' if p==self.properties['cocoa_facing'] else 'unexpected'} for i,p in enumerate(self.property_keys)]

    def set_property(self,state,prop,boxed):
        if self.read(state+8,'I')>>4==getattr(self,'pumpkin_id',None) and prop==self.properties.get('cocoa_facing'):
            direction=self.read(self.read(boxed+16,'Q')+8,'I')
            return self.state((self.pumpkin_id<<4)|{2:2,3:0,4:1,5:3}[direction])
        return super().set_property(state,prop,boxed)

    def has_property(self,state,property):
        result=property in self.property_keys
        self.property_queries.append([self.unpacked(state),'horizontal_facing' if property==self.properties['cocoa_facing'] else 'berry_age' if property==self.age_property else 'unexpected',result]);return result

    def supports_face(self,state,direction,kind):
        packed=self.unpacked(state);tile=packed&65535
        result=self.support_result if self.profile=='pumpkin_rules' else self.profile!='unsupported' and self.solid_render(tile)
        self.support_queries.append([packed,self.read(direction+8,'I'),kind,bool(result)]);return result

    def initial(self,x,y,z):
        if self.profile=='pumpkin_rules':return self.rule_blocks.get((x,y,z),0)
        return super().initial(x,y,z)

    def begin(self,profile,position,nonreplaceable_air=False):
        self.reset_world(profile,position[1],position);self.fluid_queries=[];self.support_queries=[];self.property_queries=[];self.nonreplaceable_air=nonreplaceable_air
        pos=self.alloc(12)
        for i,value in enumerate(position):self.w(pos+4*i,'i',value)
        return pos

    def pumpkins(self,seed,position,profile):
        pos=self.begin(profile,position,profile=='non_replaceable_air')
        self.call(0x596c90,None,[P,U],[self.random,seed&0xffffffffffffffff])
        result=bool(self.call(0x172300,I,[P,P,P,P],[self.feature,self.world,self.random,pos])&255)
        return {'seed':seed,'position':position,'profile':profile,'result':result,**self.world_result(),'support_queries':self.support_queries,
                'property_queries':self.property_queries,'fluid_queries':self.fluid_queries,'light_queries':self.light_queries,'random_after':C.c_int64(self.call(0x5970e0,U,[P],[self.random])).value}

    def rule(self,current,below,support,nonreplaceable_air):
        position=[0,64,0];pos=self.begin('pumpkin_rules',position,nonreplaceable_air);self.rule_blocks={(0,64,0):current,(0,63,0):below};self.support_result=support
        result=bool(self.call(0x58ff60,I,[P,P,P],[self.pumpkin,self.world,pos])&255)
        return {'position':position,'profile':'pumpkin_rules','current':current,'below':below,'support':support,'nonreplaceable_air':nonreplaceable_air,'result':result,**self.world_result(),
                'support_queries':self.support_queries,'property_queries':self.property_queries,'fluid_queries':self.fluid_queries,'light_queries':self.light_queries}
