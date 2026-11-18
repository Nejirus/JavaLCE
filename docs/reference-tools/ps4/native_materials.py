"""Original isolated Material/MaterialColor initializers and core virtuals.

Both initializer bodies have only allocator calls. Original stored fields,
class overrides, flag setters and getters execute. Padding and the uninitialized
byte read by slot 0x80 are deliberately outside this comparison.
"""
from native_carvers import Oracle,C,P,I


class MaterialOracle(Oracle):
    SLOTS=[0x10,0x18,0x20,0x28,0x30,0x50,0x60,0x68,0x70,0x78]
    SETTERS=[0x38,0x40,0x48,0x58]
    def __init__(self,executable,library):
        super().__init__(executable,library)
        self.call(0x44cd50,None,[],[])
        before={a:self.game.read(a,'Q') for a in range(0x1418000,0x1419130,8)}
        self.call(0x44c270,None,[],[]);self.materials={};self.types={}
        for alias,old in before.items():
            material=self.game.read(alias,'Q')
            if material==old:continue
            vt=self.read(material,'Q');rtti=self.read(vt-8,'Q');name=C.string_at(self.read(rtti+8,'Q')).decode()
            self.materials[alias]=material;self.types[name]=vt

    def fields(self,material):
        color=self.read(material+0x10,'Q')
        return {'flammable':bool(self.read(material+8,'B')),'replaceable':bool(self.read(material+9,'B')),'translucent':bool(self.read(material+0xa,'B')),'tool_not_required':bool(self.read(material+0x18,'B')),'push_reaction':self.read(material+0x1c,'i'),'color_code':self.read(color,'I'),'color_ordinal':self.read(color+4,'I')}

    def virtuals(self,material):
        vt=self.read(material,'Q');values={}
        for slot in self.SLOTS:
            value=self.call(self.read(vt+slot,'Q')-self.game.base,I,[P],[material]);values[hex(slot)]=value if slot==0x78 else bool(value&255)
        return values

    def initialized(self):
        return [{'alias':hex(alias),'class':next(name for name,vt in self.types.items() if vt==self.read(material,'Q')),'vtable':hex(self.read(material,'Q')-self.game.base),'fields':self.fields(material),'virtuals':self.virtuals(material)} for alias,material in self.materials.items()]

    def sample(self,name,flags,push,setters):
        original=next(p for p in self.materials.values() if self.read(p,'Q')==self.types[name]);material=self.alloc(40);C.memmove(material,original,40)
        for bit,offset in enumerate([8,9,0xa,0x18]):self.w(material+offset,'B',(flags>>bit)&1)
        self.w(material+0x1c,'i',push);initial={'fields':self.fields(material),'virtuals':self.virtuals(material)};steps=[]
        for slot in setters:
            returned=self.call(self.read(self.read(material,'Q')+slot,'Q')-self.game.base,P,[P],[material])
            steps.append({'setter':hex(slot),'returned_this':returned==material,'fields':self.fields(material),'virtuals':self.virtuals(material)})
        return {'class':name,'flags':flags,'push_reaction':push,'initial':initial,'steps':steps}
