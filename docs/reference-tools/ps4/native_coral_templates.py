"""Original crust template geometry, marker collection and complete feature.

Clock values, canonical storage, NBT fields, template cache load order and voxel
APIs are explicit inputs. Original dispatcher, placement, processors, native
marker map and marker decoration execute together over the same mutable world.
"""
from native_coral_palette import CoralPaletteOracle,C,P,U,I
from native_coral_crust import CoralCrustOracle
from native_coral_decoration import CoralDecorationOracle
from nbt_input import read_nbt
import hashlib,json


class CoralTemplateOracle(CoralPaletteOracle,CoralCrustOracle,CoralDecorationOracle):
    def __init__(self,executable,library,assets):
        super().__init__(executable,library,assets=assets,controlled_templates=False,ordered_marker_input=False)
        self.counter=123456789000000;self.frequency=1000000000
        self.game.bind('BNowx2l588E',P(self.callback(U,[],lambda:self.frequency)))
        self.game.bind('fgxnMeTNUtY',P(self.callback(U,[],lambda:self.counter)))
        self.nbt_strings={};self.template_tags={}
        for entry in self.template_inputs:
            path=assets/'templates'/(entry['path']+'.nbt')
            if hashlib.sha256(path.read_bytes()).hexdigest()!=entry['sha256']:raise ValueError('Modified original crust template')
            self.template_tags[entry['id']]=read_nbt(path)
        self.patch(0x1af2a0,P,[P,P,P],self.nbt_string)
        for block in self.blocks.values():
            vt=self.read(block,'Q')
            if not self.game.base<=vt<self.game.base+0xf00000:
                self.w(vt+0x68,'Q',self.game.base+0x91f50);self.w(vt+0x70,'Q',self.game.base+0x91f50)
        self.chunk=self.alloc(0x300);vt=self.alloc(0x220);self.w(self.chunk,'Q',vt);self.w(self.chunk+0x1b0,'Q',self.world)
        self.w(vt+0x208,'Q',self.callback(I,[P],self.empty_chunk))
        provider=self.read(self.world+0x1f8,'Q');self.w(self.read(provider,'Q')+0x58,'Q',self.callback(P,[P,I,I],self.get_chunk))
        self.w(self.vt+0x168,'Q',self.read(self.vt+0x160,'Q'));self.w(self.vt+0x10,'Q',self.callback(P,[P,P,P],self.no_entity))
        self.patch(0xc34710,P,[],lambda:self.world);self.patch(0xc80600,P,[P],lambda game:self.world)
        self.patch(0x3eea50,I,[P,P],self.column_available)
        self.patch(0x3ef4b0,None,[P,P,P,I],self.notify)
        registry=self.game.read(0x140ad68,'Q');rvt=self.alloc(64);self.w(registry,'Q',rvt);self.w(rvt+0x38,'Q',self.callback(I,[P,P],lambda registry,block:self.read(block+8,'I')))
        for tile,block in self.blocks.items():self.w(registry+0x150+8*tile,'Q',block)
        self.settings=self.alloc(176);self.call(0x6c1d60,None,[P],[self.settings]);self.call(0x6c2ed0,None,[P,I],[self.settings,1])

    def load_templates(self,clock_tick,order):
        self.initialize_math_random(clock_tick);palettes=[]
        for id in order:
            tag=self.template_tags[id];palette=[]
            for state in tag['palette']:
                name=state['Name'];props=state.get('Properties',{})
                if name=='minecraft:coral_block':
                    if props!={'variant':'dead_coral'}:raise ValueError('Unexpected original coral palette '+str(state))
                    value=self.dead_coral()
                elif name=='minecraft:stone':value=1|({'granite':1,'diorite':3,'andesite':5}[props['variant']]<<16)
                elif name=='minecraft:sand':value=12
                # Marker ID/NBT are used by the original collector and ignored
                # for geometry. Its state metadata is a controlled zero input.
                elif name=='minecraft:structure_block':value=255
                else:raise ValueError('Unexpected original crust palette '+str(state))
                palette.append(value)
            palettes.append({'id':id,'states':palette});template=self.templates[id]
            for axis,value in enumerate(tag['size']):self.w(template+0x88+4*axis,'i',value)
            pointers=self.alloc(len(tag['blocks'])*8)
            for i,block in enumerate(tag['blocks']):
                info=self.alloc(40)
                for axis,value in enumerate(block['pos']):self.w(info+4*axis,'i',value)
                self.w(info+0x10,'Q',self.canonical_state(palette[block['state']]));self.w(info+0x18,'Q',self.state(0))
                if 'nbt' in block:
                    nbt=self.alloc(8);self.nbt_strings[nbt]=block['nbt']['metadata'];self.w(info+0x20,'Q',nbt)
                self.w(pointers+8*i,'Q',info)
            self.w(template+0x50,'Q',pointers);self.w(template+0x58,'Q',pointers+len(tag['blocks'])*8)
            self.call(0x6ff170,None,[P,P,U],[pointers,pointers+len(tag['blocks'])*8,len(tag['blocks'])])
        return {'clock_tick':clock_tick,'load_order':order,'palettes':palettes,'math_random_after':C.c_int64(self.call(0x5970e0,U,[P],[self.game.base+0x1418180])).value}

    def text(self,obj):
        length=self.read(obj+24,'Q');pointer=self.read(obj+8,'Q') if self.read(obj+32,'Q')>=8 else obj+8
        return C.string_at(pointer,2*length).decode('utf-16le')

    def nbt_string(self,out,nbt,key):
        if self.text(key)!='metadata':raise ValueError('Unexpected marker NBT key')
        C.memset(out,0,40);self.w(out+32,'Q',7);value=self.nbt_strings[nbt];buffer=C.create_string_buffer(value.encode('utf-16le')+b'\0\0');self.keep.append(buffer)
        self.call(0x67840,None,[P,P,U],[out,C.addressof(buffer),len(value)]);return out

    def column_available(self,world,pos):
        x,y,z=self.xyz(pos);accepted=self.column_mode=='all' or self.column_mode=='checker' and (x+z)%3!=0
        self.column_queries.append([x,y,z,bool(accepted)]);return accepted

    def get_chunk(self,provider,x,z):
        self.template_chunk_queries.append([x,z]);self.current_chunk=(x,z);return self.chunk

    def empty_chunk(self,chunk):
        x,z=self.current_chunk;empty=self.empty_mode=='all' or self.empty_mode=='checker' and (x+z)%3==0
        self.empty_queries.append([x,z,bool(empty)]);return empty

    def no_entity(self,out,world,pos):
        self.entity_queries.append(self.xyz(pos));self.w(out,'Q',0);self.w(out+8,'Q',0);return out

    def notify(self,world,pos,block,flags):self.notifications.append([*self.xyz(pos),self.read(block+8,'I'),flags])

    def begin_template(self,position,profile,write_mode,column_mode,empty_mode,counter,frequency):
        self.shape_state=259;pos=self.begin_coral(profile,position,'all','all',self.HORIZONTAL)
        self.write_mode=write_mode;self.write_attempts=[];self.entity_queries=[];self.notifications=[]
        self.column_mode=column_mode;self.empty_mode=empty_mode;self.column_queries=[];self.template_chunk_queries=[];self.empty_queries=[];self.template_events=[]
        self.counter=counter;self.frequency=frequency;return pos

    def template_result(self):
        return {'placement':'all','loaded':'all','write_attempts':self.write_attempts,'column_queries':self.column_queries,'template_chunk_queries':self.template_chunk_queries,'empty_queries':self.empty_queries,'entity_queries':self.entity_queries,'notifications':self.notifications,'events':self.template_events,**self.coral_result()}

    def geometry(self,id,position,rotation,profile,write_mode='all',column_mode='all',empty_mode='none',counter=123456789000000,frequency=1000000000):
        pos=self.begin_template(position,profile,write_mode,column_mode,empty_mode,counter,frequency)
        self.call(0x6c2ec0,None,[P,P],[self.settings,self.rotations[rotation]])
        self.call(0x6c5060,None,[P,P,P,P],[self.templates[id],self.world,pos,self.settings])
        return {'kind':'geometry','template':id,'position':position,'rotation':rotation,'profile':profile,'write_mode':write_mode,'column_mode':column_mode,'empty_mode':empty_mode,'counter':counter,'frequency':frequency,**self.template_result()}

    def complete_crust(self,seed,position,profile,rotation_order=(0,1,2,3),write_mode='all',column_mode='all',empty_mode='none',counter=123456789000000,frequency=1000000000,client=False):
        self.begin_template(position,profile,write_mode,column_mode,empty_mode,counter,frequency)
        result=self.crust(seed,position,profile,rotation_order,geometry_draws=0,post_draws=0,client=client)
        return {**result,'kind':'crust','write_mode':write_mode,'column_mode':column_mode,'empty_mode':empty_mode,'counter':counter,'frequency':frequency,**self.template_result()}
