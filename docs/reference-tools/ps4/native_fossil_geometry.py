"""Original fossil templates and integrity processor over supplied canonical storage.

Original NBT block order, positions, palette and dimensions are supplied to the
original template routines. No Java placement/rotation/processor runs here.
"""
from native_fossils import FossilOracle,C,P,U,I
from nbt_input import read_nbt
import json
from pathlib import Path


class FossilGeometryOracle(FossilOracle):
    def __init__(self,executable,library,assets):
        super().__init__(executable,library,assets,controlled_geometry=False)
        self.properties['pillar_axis']=self.alloc(16);self.game.w(0x142f840,'Q',self.properties['pillar_axis'])
        for address,axis in [(0x1413db8,0),(0x1413c10,1),(0x1413db0,2)]:self.game.w(address,'Q',self.log_axes[axis])
        self.w(self.blocks[216],'Q',self.game.base+0x121a110)
        self.w(self.blockvt+0x68,'Q',self.game.base+0x91f50);self.w(self.blockvt+0x70,'Q',self.game.base+0x91f50)
        self.palette_inputs=[]
        for entry in self.template_inputs:
            data=read_nbt(assets/'templates'/(entry['path']+'.nbt'));states=[]
            for palette in data['palette']:
                if palette['Name']=='minecraft:bone_block':state=216|({'y':0,'x':4,'z':8}[palette['Properties']['axis']]<<16)
                elif palette['Name']=='minecraft:coal_ore':state=16
                else:raise ValueError('Unexpected original fossil palette '+str(palette))
                states.append(state)
            self.palette_inputs.append({'id':entry['id'],'states':states})
            pointers=self.alloc(len(data['blocks'])*8)
            for i,block in enumerate(data['blocks']):
                if 'nbt' in block:raise ValueError('Unexpected fossil block NBT')
                info=self.alloc(40)
                for axis,value in enumerate(block['pos']):self.w(info+4*axis,'i',value)
                state=states[block['state']];self.w(info+0x10,'Q',self.state(((state&65535)<<4)|(state>>16)));self.w(info+0x18,'Q',self.state(0));self.w(pointers+i*8,'Q',info)
            template=self.templates[entry['id']];self.w(template+0x50,'Q',pointers);self.w(template+0x58,'Q',pointers+len(data['blocks'])*8)
            self.call(0x6ff170,None,[P,P,U],[pointers,pointers+len(data['blocks'])*8,len(data['blocks'])])
        registry=json.loads(Path(__file__).with_name('block-registry-native.json').read_text());ids={r['name']:r['id'] for r in registry['registrations']};ids['air']=0
        for alias in registry['aliases']:
            if ids[alias['name']] in self.blocks:self.game.w(int(alias['alias'],16),'Q',self.blocks[ids[alias['name']]])
        block_registry=self.game.read(0x140ad68,'Q');registry_vt=self.alloc(64);self.w(block_registry,'Q',registry_vt)
        self.w(registry_vt+0x38,'Q',self.callback(I,[P,P],lambda registry,block:self.read(block+8,'I')))
        self.w(self.read(self.chunk,'Q')+0x208,'Q',self.callback(I,[P],lambda chunk:False))
        self.w(self.vt+0x168,'Q',self.read(self.vt+0x160,'Q'))
        self.w(self.vt+0x10,'Q',self.callback(P,[P,P,P],self.no_entity))
        self.patch(0x3eea50,I,[P,P],lambda world,pos:True)
        self.patch(0x3ef4b0,None,[P,P,P,I],self.notify)
        self.axis_data=[{'state':216|(meta<<16),'native_data':self.call(0x5c1b90,I,[P,P],[self.blocks[216],self.state((216<<4)|meta)])} for meta in [0,4,8]]

    def property(self,state,prop):
        if prop==self.properties.get('pillar_axis'):return self.axis_boxes[{0:1,4:0,8:2}[self.read(state+8,'I')&12]]
        return super().property(state,prop)

    def set_property(self,state,prop,boxed):
        if prop==self.properties.get('pillar_axis'):
            packed=self.read(state+8,'I');axis=self.read(self.read(boxed+16,'Q')+8,'I');return self.state((packed&~12)|[4,0,8,12][axis])
        return super().set_property(state,prop,boxed)

    def no_entity(self,out,world,pos):
        self.entity_queries.append(self.xyz(pos));self.w(out,'Q',0);self.w(out+8,'Q',0);return out

    def notify(self,world,pos,block,flag):self.notification_queries.append([*self.xyz(pos),self.read(block+8,'I'),flag])

    def initial(self,x,y,z):
        if self.voxel_profile=='air':return 0
        if self.voxel_profile=='water':return 9
        if self.voxel_profile=='mixed':return [1,3,9,8|(3<<16),10,16,216|(8<<16),0][(x+y+z)%8]
        return 1

    def geometry(self,world_seed,population_seed,position,height_profile,rotation_order,voxel_profile='stone'):
        self.voxel_profile=voxel_profile;self.reset_world('fossil_geometry',256,position);self.fluid_queries=[];self.support_queries=[];self.entity_queries=[];self.notification_queries=[]
        result=self.fossil(world_seed,population_seed,position,height_profile,0,rotation_order)
        return {**result,'voxel_profile':voxel_profile,**self.world_result(),'fluid_queries':self.fluid_queries,'light_queries':self.light_queries,'entity_queries':self.entity_queries,'notification_queries':self.notification_queries}
