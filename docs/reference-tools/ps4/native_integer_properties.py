"""Original integer properties, real typed boxes and canonical definitions.

No property/boxed-value/count/equality/hash callbacks are installed. Original
RTTI data for int and pure string/allocator imports are linked explicitly.
"""
from native_carvers import Oracle,C,P,I,U

class IntegerPropertiesOracle(Oracle):
    def __init__(self,executable,library):
        super().__init__(executable,library)
        for nid in ['fL3O02ypZFE','Noj9PsJrsa8','QJ5xVfKkni0']:self.game.bind(nid,P(self.lib.base+self.lib.exports[nid]))
        nid='St4apgcBNfo';address=self.lib.exports[nid];segment=next(p for p in self.lib.ph if p[0] in [1,0x61000010] and p[3]<=address<p[3]+p[6])
        if segment[1]&1:raise ValueError('Expected non-executable original int RTTI data')
        self.type_info=self.lib.base+address;self.game.bind(nid,P(self.type_info))
        self.factory=self.game.function(0x37a180,P,[P,I,I]);self.count_function=self.game.function(0x37a260,I,[P]);self.value_function=self.game.function(0x37a270,P,[P,I]);self.index_function=self.game.function(0x37a2a0,I,[P,P]);self.getter=self.game.function(0xd04f0,P,[P,P]);self.setter=self.game.function(0xd0550,P,[P,P,P]);self.missing_property=self.alloc(8)
    def string(self,text):
        data=text.encode('utf-16le');length=len(data)//2;value=self.alloc(40)
        if length<=7:C.memmove(value+8,data+b'\0\0',len(data)+2);self.w(value+32,'Q',7)
        else:
            raw=self.alloc(len(data)+2);C.memmove(raw,data+b'\0\0',len(data)+2);self.w(value+8,'Q',raw);self.w(value+32,'Q',length)
        self.w(value+24,'Q',length);return value
    def property(self,name,minimum,maximum):
        if minimum<=maximum and maximum-minimum>64:raise ValueError('Unbounded integer property fixture')
        return self.factory(self.string(name),minimum,maximum)
    def scalar(self,value):
        boxed=self.alloc(24);self.call(0xb7a930,None,[P,P],[boxed,self.type_info]);self.w(boxed,'Q',self.game.base+0x1215038);self.w(boxed+16,'i',value);return boxed
    def values(self,prop):
        start=self.read(prop+0x48,'Q');end=self.read(prop+0x50,'Q');return [self.read(self.read(p,'Q')+16,'i') for p in range(start,end,8)]
    def property_case(self,name,minimum,maximum,queries):
        prop=self.property(name,minimum,maximum);count=self.count_function(prop)
        string=prop+0x18;length=self.read(string+24,'Q');source=string+8 if self.read(string+32,'Q')<8 else self.read(string+8,'Q');stored_name=C.string_at(source,length*2).decode('utf-16le')
        return {'name':name,'minimum':minimum,'maximum':maximum,'queries':queries,'stored_name':stored_name,'count':count,'possible_values':self.values(prop),'indexed_values':[self.read(self.value_function(prop,i)+16,'i') for i in range(count)],'indices':[self.index_function(prop,self.scalar(value)) for value in queries]}
    def definition_case(self,specs,queries):
        props=[self.property(s['name'],s['minimum'],s['maximum']) for s in specs]
        if any(self.count_function(prop)==0 for prop in props):raise ValueError('Canonical definition requires nonempty integer properties')
        owner=self.alloc(512);definition=self.alloc(0x48);ordered=self.alloc(max(8,len(props)*8))
        for i,prop in enumerate(props):self.w(ordered+i*8,'Q',prop)
        self.call(0xcf850,None,[P,P,P,U],[definition,owner,ordered,len(props)])
        capacity=self.read(definition+0x30,'I');table=self.read(definition+0x28,'Q');states=[self.read(table+i*8,'Q') for i in range(capacity)];getters=[];setters=[]
        for state in states:
            if not state:continue
            code=self.read(state+0x28,'H')
            for index,prop in enumerate(props+[self.missing_property]):
                boxed=self.getter(state,prop);getters.append([code,index,None if not boxed else self.read(boxed+16,'i')])
            for index,prop in enumerate(props):
                for value in queries:
                    after=self.setter(state,prop,self.scalar(value));setters.append([code,index,value,-1 if not after else self.read(after+0x28,'H'),after==state])
        return {'properties':specs,'queries':queries,'counts':[self.count_function(prop) for prop in props],'capacity':capacity,'shifts':[self.read(self.read(definition+0x38,'Q')+i,'B') for i in range(len(props))],'masks':[self.read(self.read(definition+0x40,'Q')+i*2,'H') for i in range(len(props))],'state_codes':[None if not state else self.read(state+0x28,'H') for state in states],'owner_matches':all(not state or self.read(state+0x18,'Q')==owner for state in states),'getters':getters,'setters':setters}
