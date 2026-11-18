"""Original constructor prefixes, base stores and BlockState material getters.

The static trace supplies invocation arguments/identity links, never the
material result. Color/enum/string/state-definition startup is outside this
capture: unused arguments and enum/color inputs are explicit synthetic context.
The original prefix, Material hierarchy, base constructor and state accessors
execute. No module startup or constructor property/shape initialization runs.
"""
import subprocess,tempfile
from pathlib import Path
from native_materials import MaterialOracle,C,P,I
from native_module import U

class BlockMaterialOracle(MaterialOracle):
    def __init__(self,executable,library,flow,color_ordinal):
        super().__init__(executable,library)
        self.flow=flow;self.objects={int(k):v for k,v in flow['objects'].items()};self.owners={int(k):v for k,v in flow['state_owners'].items()};self.results={};self.pointers={};self.explicit_globals={}
        self.temporary=tempfile.TemporaryDirectory(prefix='ps4-material-prefix-')
        compiled=Path(self.temporary.name)/'capture.so'
        subprocess.run(['cc','-shared','-fPIC','-O2',str(Path(__file__).with_name('material_prefix_capture.c')),'-o',str(compiled)],check=True)
        self.capture=C.CDLL(str(compiled));self.capture.capture_material.argtypes=[P,P,P];self.capture.capture_material.restype=I
        self.original={pc:C.string_at(self.game.base+pc,12) for pc in [0x91fb0,0x92150]}
        self.color=self.read(self.game.read(0x1418fc0,'Q')+color_ordinal*8,'Q')
        # A color lookup preceding GlazedTerracotta's base call. The static
        # trace proves its result reaches the color argument, never material.
        self.patch(0x44d830,P,[P],lambda dye:self.color)
        for nid in ['fL3O02ypZFE','Noj9PsJrsa8','QJ5xVfKkni0']:self.game.bind(nid,P(self.lib.base+self.lib.exports[nid]))
        # RedSandstone reads a color from this enum before its base call.
        self.game.w(0x142f4b0,'Q',self.enum_argument())
        self.material_aliases={pointer:alias for alias,pointer in self.materials.items()}
    def enum_argument(self):
        pointer=self.alloc(128);self.w(pointer+0x38,'Q',self.color);self.w(pointer+0x40,'Q',self.color);return pointer
    def state_argument(self,block):
        state=self.alloc(56);self.w(state,'Q',self.game.base+0x1218f90);self.w(state+0x18,'Q',block);return state
    def argument(self,value):
        if value is None:return 0
        if isinstance(value,int):return value&0xffffffffffffffff
        if value[0]=='global':
            pointer=self.game.read(value[1],'Q')
            if pointer:return pointer
            if value[1] not in self.explicit_globals:
                self.explicit_globals[value[1]]=self.enum_argument();self.game.w(value[1],'Q',self.explicit_globals[value[1]])
            return self.explicit_globals[value[1]]
        if value[0]=='data':return self.game.base+value[1]
        if value[0]=='stack':return self.alloc(1024)
        if value[0]=='state':
            obj=value[1][1];self.material(obj);return self.state_argument(self.pointers[obj])
        if value[0]=='object':
            obj=value[1]
            if self.objects[obj]['size']==56:
                owner=self.owners[obj];self.material(owner);return self.state_argument(self.pointers[owner])
            self.material(obj);return self.pointers[obj]+value[2]
        raise ValueError(('Unresolved constructor argument',value))
    def material(self,obj):
        if obj in self.results:return self.results[obj]
        allocation=self.objects[obj];call=allocation['calls'][0];block=self.alloc(max(allocation['size'],512));self.pointers[obj]=block
        arguments=[block]+[self.argument(v) for v in call['args'][1:]];captured=(U*4)()
        self.game.patch(0x91fb0,self.capture.material_base);self.game.patch(0x92150,self.capture.material_color_base)
        ok=self.capture.capture_material(P(self.game.base+int(call['target'],16)),(U*6)(*arguments),captured)
        for pc,original in self.original.items():C.memmove(self.game.base+pc,original,len(original))
        if not ok or captured[0]!=block or captured[1] not in self.material_aliases:raise ValueError(('Invalid captured material',allocation,ok,list(captured)))
        # Execute the selected complete original base constructor, including
        # its actual material store and original material virtual calls.
        self.call(captured[3],None,[P,P,P],[block,captured[1],captured[2]])
        stored=self.read(block+0x170,'Q');state=self.state_argument(block)
        state_material=self.call(0xd06a0,P,[P],[state]);state_block=self.call(0xd0670,P,[P],[state])
        if stored!=captured[1] or state_material!=stored or state_block!=block:raise ValueError('Original base/state material linkage disagrees')
        result={'material_alias':hex(self.material_aliases[stored]),'base_constructor':hex(captured[3]),'virtuals':self.virtuals(stored),'base_light_passes':bool(self.read(block+0x134,'B')),'base_solid_or_motion':bool(self.read(block+0x14b,'B'))}
        self.results[obj]=result;return result
    def registrations(self):
        return [{k:row[k] for k in ['id','name','register_pc','constructor_chain']}|self.material(row['object'][1]) for row in self.flow['registrations']]
