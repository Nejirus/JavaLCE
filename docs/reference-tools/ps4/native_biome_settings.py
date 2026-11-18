"""Original complete biome initialization with feature construction supplied.

Biome constructors, climate arithmetic, count stores and class identities run
unchanged. Registry/default/canonical property storage and name-map nodes are
controlled; bounded feature constructors/deletion are object bindings. Numeric
presets execute only the original volatile-register store span, stopping before
the next allocation. No grouped module startup runs.
"""
from native_decoration import DecorationOracle,C,P,U,I
from native_tree_selection import TreeSelectionOracle
from pathlib import Path
import json


class BiomeSettingsOracle(DecorationOracle,TreeSelectionOracle):
    FIELDS=DecorationOracle.FIELDS[:-2]+[('seaPickles',0x150)]+DecorationOracle.FIELDS[-2:]
    def __init__(self,executable,library):
        super().__init__(executable,library)
        for nid in ['Noj9PsJrsa8','QJ5xVfKkni0']:self.game.bind(nid,P(self.lib.base+self.lib.exports[nid]))
        self.patch(0x141560,None,[P,P,P],lambda out,registry,name:self.w(out+16,'Q',self.alloc(128)))
        registry=self.game.read(0x140ad68,'Q');vt=self.alloc(64);self.w(registry,'Q',vt)
        self.w(vt+0x38,'Q',self.callback(I,[P,P],lambda registry,block:self.read(block+8,'I')))
        bindings=json.loads(Path(__file__).with_name('block-registry-native.json').read_text());ids={r['name']:r['id'] for r in bindings['registrations']};ids['air']=0
        for tile in sorted(set(ids.values())-self.blocks.keys()):
            block=self.alloc(1024);self.blocks[tile]=block;self.w(block,'Q',self.blockvt);self.w(block+8,'I',tile);self.w(block+0x170,'Q',self.read(self.blocks[31]+0x170,'Q'))
        for alias in bindings['aliases']:self.game.w(int(alias['alias'],16),'Q',self.blocks[ids[alias['name']]])
        for tile,block in self.blocks.items():
            if not self.read(block+0x190,'Q'):self.w(block+0x190,'Q',self.state(tile<<4))
        self.infested_prop=self.alloc(16);self.game.w(0x1419720,'Q',self.infested_prop)
        if C.string_at(self.game.base+0x4d41e6,3)!=b'\xc7\x43\x08':raise ValueError('Infested stone enum assignment changed')
        self.infested_enum_id=self.game.read(0x4d41e9,'I');variant=self.alloc(64);self.w(variant+8,'I',self.infested_enum_id);self.game.w(0x1419770,'Q',variant)
        fvt=self.alloc(64);self.w(fvt+8,'Q',self.callback(None,[P],lambda obj:None))
        def feature_binding(obj,*args):self.w(obj,'Q',fvt);return obj
        self.feature_constructor_bindings=[0x1813c0,0x5c3e30,0x2ea330,0x271a20,0x3700f0,0x3c6400,0x5f6700,0x372190,0x1722d0]
        for pc in self.feature_constructor_bindings:self.patch(pc,P,[P,U,U,U,U,U],feature_binding)
        # This audited span contains only LEA/MOV/MOVABS, writes only the
        # numeric preset globals, and touches volatile registers. End exactly
        # before its next original malloc call; return without platform startup.
        if C.string_at(self.game.base+0x15110d,3)!=b'\x48\x8d\x0d' or self.game.read(0x151281,'B')!=0xe8:raise ValueError('Biome preset span changed')
        self.game.w(0x151281,'B',0xc3);self.call(0x15110d,None,[],[])
        self.presets=[{'alias':hex(a),'depth_bits':f'{self.game.read(a,"I"):08x}','scale_bits':f'{self.game.read(a+4,"I"):08x}'} for a in range(0x140aa98,0x140ab00,8)]
        self.call(0x7f900,None,[],[])

    def set_property(self,state,prop,boxed):
        if prop==getattr(self,'infested_prop',-1):return self.state((self.read(state+8,'I')&~15)|self.read(self.read(boxed+16,'Q')+8,'I'))
        return super().set_property(state,prop,boxed)

    def biome_settings(self):
        result=[]
        for id in range(256):
            biome=self.game.read(0x1409fc0+8*id,'Q')
            if not biome:continue
            deco=self.read(biome+0x60,'Q');vt=self.read(biome,'Q');rtti=self.read(vt-8,'Q');name=C.string_at(self.read(rtti+8,'Q')).decode()
            values={name:self.read(biome+offset,'f') for name,offset in [('depth',0x10),('scale',0x14),('temperature',0x18),('rainfall',0x1c)]}
            bits={name:f'{self.read(biome+offset,"I"):08x}' for name,offset in [('depth',0x10),('scale',0x14),('temperature',0x18),('rainfall',0x1c)]}
            settings={name:bool(self.read(deco+offset,'B')) if name=='springs' else self.read(deco+offset,'f' if name=='extraTreeChance' else 'i') for name,offset in self.FIELDS}
            family=self.call(self.read(vt+0x90,'Q')-self.game.base,I,[P],[biome])
            group=self.call(self.read(vt+0x98,'Q')-self.game.base,I,[P],[biome])
            snow=bool(self.call(0x85300,I,[P],[biome])&255);rain=bool(self.call(0x85320,I,[P],[biome])&255)
            result.append({'id':id,'class':name,'vtable':hex(vt-self.game.base),'family':family,'temperature_group':group,'category':self.read(biome+0x1ac,'I'),**values,'float_bits':bits,'snow':snow,'rain':rain,'raw_snow_flag':bool(self.read(biome+0x20,'B')),'raw_rain_flag':bool(self.read(biome+0x21,'B')),'snow_accumulation':self.read(biome+0x1b0,'f'),'snow_maximum_bits':f'{self.read(biome+0x1bc,"I"):08x}','maximum_snow_depth':self.call(0x852c0,I,[P],[biome]),'settings':settings,
                           'top':self.read(biome+0x50,'I')|(self.read(biome+0x54,'I')<<16),'filler':self.read(biome+0x58,'I')|(self.read(biome+0x5c,'I')<<16)})
        return result
