"""Real boolean/integer properties and mixed canonical state definitions.

All constructors, typed-box methods, property methods and definition operations
execute original code. Only pure imports and original readonly RTTI are linked.
"""
from native_integer_properties import IntegerPropertiesOracle,C,P,I,U

class BooleanPropertiesOracle(IntegerPropertiesOracle):
    def __init__(self,executable,library):
        super().__init__(executable,library)
        nid='nEuTkSQAQFw';address=self.lib.exports[nid];segment=next(p for p in self.lib.ph if p[0] in [1,0x61000010] and p[3]<=address<p[3]+p[6])
        if segment[1]&1:raise ValueError('Expected non-executable original bool RTTI data')
        self.bool_type_info=self.lib.base+address;self.game.bind(nid,P(self.bool_type_info))
        nid='Ovb2dSJOAuE';self.game.bind(nid,P(self.lib.base+self.lib.exports[nid]))
        self.bool_factory=self.game.function(0xdabb0,P,[P]);self.bool_count=self.game.function(0xdb0b0,I,[P]);self.bool_value=self.game.function(0xdb0c0,P,[P,I]);self.bool_index=self.game.function(0xdb100,I,[P,P])
    def boolean_property(self,name):return self.bool_factory(self.string(name))
    def typed_scalar(self,value):
        if type(value)is not bool:return self.scalar(value)
        boxed=self.alloc(24);self.call(0xb7a930,None,[P,P],[boxed,self.bool_type_info]);self.w(boxed,'Q',self.game.base+0x1214a38);self.w(boxed+16,'B',int(value));return boxed
    def unbox(self,boxed):
        if not boxed:return None
        type_info=self.read(boxed+8,'Q')
        if type_info==self.bool_type_info:return bool(self.read(boxed+16,'B'))
        if type_info==self.type_info:return self.read(boxed+16,'i')
        raise ValueError('Unexpected native box type')
    def boolean_case(self,name,queries):
        prop=self.boolean_property(name);count=self.bool_count(prop);string=prop+0x18;length=self.read(string+24,'Q');source=string+8 if self.read(string+32,'Q')<8 else self.read(string+8,'Q');stored_name=C.string_at(source,length*2).decode('utf-16le');start=self.read(prop+0x48,'Q');end=self.read(prop+0x50,'Q')
        return {'name':name,'stored_name':stored_name,'count':count,'possible_values':[self.unbox(self.read(p,'Q')) for p in range(start,end,8)],'indexed_values':[self.unbox(self.bool_value(prop,i)) for i in range(count)],'queries':queries,'indices':[self.bool_index(prop,self.typed_scalar(value)) for value in queries]}
    def mixed_definition_case(self,specs,queries):
        props=[self.boolean_property(s['name']) if s['type']=='boolean' else self.property(s['name'],s['minimum'],s['maximum']) for s in specs]
        def count(prop):return self.call(self.read(self.read(prop,'Q')+0x40,'Q')-self.game.base,I,[P],[prop])
        if any(count(prop)==0 for prop in props):raise ValueError('Canonical definition requires nonempty properties')
        owner=self.alloc(512);definition=self.alloc(0x48);ordered=self.alloc(max(8,len(props)*8))
        for i,prop in enumerate(props):self.w(ordered+i*8,'Q',prop)
        self.call(0xcf850,None,[P,P,P,U],[definition,owner,ordered,len(props)])
        capacity=self.read(definition+0x30,'I');table=self.read(definition+0x28,'Q');states=[self.read(table+i*8,'Q') for i in range(capacity)];getters=[];setters=[]
        for state in states:
            if not state:continue
            code=self.read(state+0x28,'H')
            for index,prop in enumerate(props+[self.missing_property]):getters.append([code,index,self.unbox(self.getter(state,prop))])
            for index,prop in enumerate(props):
                for value in queries:
                    after=self.setter(state,prop,self.typed_scalar(value));setters.append([code,index,value,-1 if not after else self.read(after+0x28,'H'),after==state])
        return {'properties':specs,'queries':queries,'counts':[count(prop) for prop in props],'capacity':capacity,'shifts':[self.read(self.read(definition+0x38,'Q')+i,'B') for i in range(len(props))],'masks':[self.read(self.read(definition+0x40,'Q')+i*2,'H') for i in range(len(props))],'state_codes':[None if not state else self.read(state+0x28,'H') for state in states],'owner_matches':all(not state or self.read(state+0x18,'Q')==owner for state in states),'getters':getters,'setters':setters}
