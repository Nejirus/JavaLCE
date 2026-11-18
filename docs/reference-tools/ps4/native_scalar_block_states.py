"""Actual scalar properties, class definitions, states and getData virtuals.

Property initializer arguments are read from pinned code, not executed startup.
Concrete class vtables are explicit inputs. No property/count/box/definition/
metadata callbacks run. Static guard locking is supplied for one-thread capture.
"""
from native_boolean_properties import BooleanPropertiesOracle,C,P,I
from read_block_constructors import Source

class ScalarBlockStatesOracle(BooleanPropertiesOracle):
    def __init__(self,executable,library,recovered):
        super().__init__(executable,library)
        self.source=Source(executable);self.specs=recovered['properties'];self.properties={};by_initializer={}
        for spec in self.specs:
            key=spec['factory_pc']
            if key not in by_initializer:by_initializer[key]=self.boolean_property(spec['name']) if spec['type']=='boolean' else self.property(spec['name'],spec['minimum'],spec['maximum'])
            prop=by_initializer[key];alias=int(spec['alias'],16);self.properties[alias]=prop;self.game.w(alias,'Q',prop)
        self.aliases={}
        for alias,prop in self.properties.items():self.aliases.setdefault(prop,alias)
        self.patch(0xe6cff0,I,[P],lambda guard:0 if self.read(guard,'B') else 1)
        self.patch(0xe6d000,None,[P],lambda guard:self.w(guard,'B',1))
        # Remove inherited synthetic packed/default lookups. Selected actual
        # getData methods must not silently reach prototype state bindings.
        for pc in [0x91c30,0x7a4d0]:C.memmove(self.game.base+pc,self.source.read(pc,12),12)
    def text(self,string):
        length=self.read(string+24,'Q');pointer=string+8 if self.read(string+32,'Q')<8 else self.read(string+8,'Q');return C.string_at(pointer,length*2).decode('utf-16le')
    def property_summaries(self):
        summaries=[]
        for spec in self.specs:
            alias=int(spec['alias'],16);prop=self.properties[alias];vt=self.read(prop,'Q');count=self.call(self.read(vt+0x40,'Q')-self.game.base,I,[P],[prop]);begin=self.read(prop+0x48,'Q');end=self.read(prop+0x50,'Q')
            summaries.append({'alias':spec['alias'],'identity':hex(self.aliases[prop]),'type':spec['type'],'name':self.text(prop+0x18),'count':count,'values':[self.unbox(self.read(p,'Q')) for p in range(begin,end,8)]})
        return summaries
    def definition(self,vtable):
        owner=self.alloc(4096);self.w(owner,'Q',self.game.base+vtable);method=self.read(self.game.base+vtable+0x478,'Q')-self.game.base;encoder=self.read(self.game.base+vtable+0x50,'Q')-self.game.base
        definition=self.call(method,P,[P],[owner]);self.w(owner+0x188,'Q',definition);capacity=self.read(definition+0x30,'I');table=self.read(definition+0x28,'Q');states=[self.read(table+i*8,'Q') for i in range(capacity)];self.w(owner+0x190,'Q',states[0]);begin=self.read(definition+0x10,'Q');end=self.read(definition+0x18,'Q');props=[self.read(p,'Q') for p in range(begin,end,8)]
        if capacity>4096 or any(prop not in self.aliases for prop in props):raise ValueError('Unbounded or non-scalar class definition')
        values=[];data=[];owners=True
        for state in states:
            if not state:continue
            code=self.read(state+0x28,'H');values.append([code,[self.unbox(self.getter(state,prop)) for prop in props]]);data.append([code,self.call(encoder,I,[P,P],[owner,state])]);owners&=self.read(state+0x18,'Q')==owner
        selector=None
        if encoder==0x1ec1b0:
            prop=self.call(self.read(self.game.base+vtable+0x4e0,'Q')-self.game.base,P,[P],[owner]);selector=hex(self.aliases[prop])
        rtti=self.read(self.game.base+vtable-8,'Q');class_name=C.string_at(self.read(rtti+8,'Q')).decode()
        return {'vtable':hex(vtable),'class':class_name,'method':hex(method),'get_data':hex(encoder),'virtual_property':selector,'properties':[hex(self.aliases[prop]) for prop in props],'capacity':capacity,'state_codes':[None if not state else self.read(state+0x28,'H') for state in states],'shifts':[self.read(self.read(definition+0x38,'Q')+i,'B') for i in range(len(props))],'masks':[self.read(self.read(definition+0x40,'Q')+i*2,'H') for i in range(len(props))],'owner_matches':owners,'default_code':self.read(states[0]+0x28,'H'),'values':values,'data':data}
