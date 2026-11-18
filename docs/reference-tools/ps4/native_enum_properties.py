"""Original pointer sets, real enum constructors and mixed canonical states.

Pointer-set inputs are opaque uint64 keys, never dereferenced. Enum constructor
inputs are explicit ordered linked sets of actual initialized wood enums.
"""
from native_boolean_properties import BooleanPropertiesOracle,C,P,I,U

class EnumPropertiesOracle(BooleanPropertiesOracle):
    def __init__(self,executable,library):
        super().__init__(executable,library)
        # Only intercept the property factory during isolated enum creation.
        # The original six enum objects/names/IDs execute; this capture makes
        # no assumptions about host allocation addresses or set iteration.
        self.factory_bytes=C.string_at(self.game.base+0x4b9c40,12)
        self.patch(0x4b9c40,P,[P,P,P,P],lambda *unused:self.alloc(8))
        self.call(0x53d910,None,[],[])
        C.memmove(self.game.base+0x4b9c40,self.factory_bytes,len(self.factory_bytes))
        aliases=[0x142dc30,0x142dc38,0x142dc40,0x142dc48,0x142e618,0x142dc78];self.enums={};self.enum_descriptions=[]
        for alias in aliases:
            enum=self.game.read(alias,'Q');identity=self.read(enum+8,'i');self.enums[identity]=enum;self.enum_descriptions.append({'identity':identity,'id':identity,'name':self.text(enum+0x10)})
        clone=self.alloc(64);C.memmove(clone,self.enums[0],64);self.enums[6]=clone;self.enum_descriptions.append({'identity':6,'id':self.read(clone+8,'i'),'name':self.text(clone+0x10)})
        self.enum_identities={pointer:identity for identity,pointer in self.enums.items()};self.enum_count=self.game.function(0x4d1630,I,[P]);self.enum_value=self.game.function(0x4d1640,P,[P,I]);self.enum_index=self.game.function(0x4d1670,I,[P,P]);self.unknown_set_capture=None
        self.factory_target_bytes=C.string_at(self.game.base+0x4d01c0,12)
    def text(self,string):
        length=self.read(string+24,'Q');source=string+8 if self.read(string+32,'Q')<8 else self.read(string+8,'Q');return C.string_at(source,length*2).decode('utf-16le')
    @staticmethod
    def key(value):return f'{value&0xffffffffffffffff:016x}'
    def pointer_case(self,values):
        if not values or len(values)>1100:raise ValueError('Pointer-set fixture needs 1..1100 bounded opaque keys')
        snapshots=[]
        def capture(name,type_info,container,predicate):
            def snapshot(prefix,inserted):
                head=self.read(container+8,'Q');node=self.read(head,'Q');ordered=[]
                while node!=head:
                    ordered.append(self.key(self.read(node+16,'Q')));node=self.read(node,'Q')
                    if len(ordered)>len(values):raise ValueError('Corrupt original pointer-set traversal')
                snapshots.append({'prefix':prefix,'inserted':inserted,'size':self.read(container+16,'Q'),'buckets':self.read(container+64,'Q'),'order':ordered})
            snapshot(1,True)
            for index,value in enumerate(values[1:],2):
                out=self.alloc(32);key=self.alloc(8);self.w(key,'Q',value&0xffffffffffffffff);self.call(0x4d07b0,P,[P,P,P],[out,container,key])
                if index<=16 or index in [63,64,65,511,512,513,1023,1024,1025,len(values)]:snapshot(index,bool(self.read(out+24,'B')))
            return self.alloc(8)
        self.patch(0x4d01c0,P,[P,P,P,P],capture);array=self.alloc(48)
        for i in range(6):self.w(array+i*8,'Q',values[0]&0xffffffffffffffff)
        self.call(0x4b9c40,P,[P,P,P,P],[self.string('variant'),0,array,0]);C.memmove(self.game.base+0x4d01c0,self.factory_target_bytes,len(self.factory_target_bytes))
        return {'keys':[self.key(value) for value in values],'snapshots':snapshots}
    def enum_property(self,name,identities):
        if len(set(identities))!=len(identities):raise ValueError('Constructor input set must have unique enum identities')
        container=self.alloc(80);head=self.alloc(24);nodes=[self.alloc(24) for identity in identities];all_nodes=[head]+nodes+[head]
        for index,node in enumerate(nodes):self.w(node,'Q',all_nodes[index+2]);self.w(node+8,'Q',all_nodes[index]);self.w(node+16,'Q',self.enums[identities[index]])
        self.w(head,'Q',nodes[0] if nodes else head);self.w(head+8,'Q',nodes[-1] if nodes else head);self.w(container+8,'Q',head);prop=self.alloc(0xb0)
        self.call(0x4d0ea0,None,[P,P,P,P],[prop,self.string(name),self.game.base+0x12a7650,container]);return prop
    def mixed_scalar(self,value):
        if not isinstance(value,dict):return self.typed_scalar(value)
        boxed=self.alloc(24);self.call(0xb7a930,None,[P,P],[boxed,self.game.base+0x1214e30]);self.w(boxed,'Q',self.game.base+0x1215348);self.w(boxed+16,'Q',self.enums[value['enum']]);return boxed
    def mixed_unbox(self,boxed):
        if boxed and self.read(boxed+8,'Q')==self.game.base+0x1214e30:return {'enum':self.enum_identities[self.read(boxed+16,'Q')]}
        return self.unbox(boxed)
    def enum_case(self,name,identities,queries):
        prop=self.enum_property(name,identities);count=self.enum_count(prop);start=self.read(prop+0x48,'Q');end=self.read(prop+0x50,'Q')
        return {'name':name,'identities':identities,'stored_name':self.text(prop+0x18),'count':count,'possible_values':[self.mixed_unbox(self.read(p,'Q')) for p in range(start,end,8)],'indexed_values':[self.mixed_unbox(self.enum_value(prop,i)) for i in range(count)],'queries':queries,'indices':[self.enum_index(prop,self.mixed_scalar(value)) for value in queries]}
    def enum_definition_case(self,specs,queries):
        props=[]
        for s in specs:props.append(self.enum_property(s['name'],s['identities']) if s['type']=='enum' else self.boolean_property(s['name']) if s['type']=='boolean' else self.property(s['name'],s['minimum'],s['maximum']))
        def count(prop):return self.call(self.read(self.read(prop,'Q')+0x40,'Q')-self.game.base,I,[P],[prop])
        if any(count(prop)==0 for prop in props):raise ValueError('Canonical definition requires nonempty properties')
        owner=self.alloc(512);definition=self.alloc(0x48);ordered=self.alloc(max(8,len(props)*8))
        for i,prop in enumerate(props):self.w(ordered+i*8,'Q',prop)
        self.call(0xcf850,None,[P,P,P,U],[definition,owner,ordered,len(props)])
        capacity=self.read(definition+0x30,'I');table=self.read(definition+0x28,'Q');states=[self.read(table+i*8,'Q') for i in range(capacity)];getters=[];setters=[]
        for state in states:
            if not state:continue
            code=self.read(state+0x28,'H')
            for index,prop in enumerate(props+[self.missing_property]):getters.append([code,index,self.mixed_unbox(self.getter(state,prop))])
            for index,prop in enumerate(props):
                for value in queries:
                    after=self.setter(state,prop,self.mixed_scalar(value));setters.append([code,index,value,-1 if not after else self.read(after+0x28,'H'),after==state])
        return {'properties':specs,'queries':queries,'counts':[count(prop) for prop in props],'capacity':capacity,'shifts':[self.read(self.read(definition+0x38,'Q')+i,'B') for i in range(len(props))],'masks':[self.read(self.read(definition+0x40,'Q')+i*2,'H') for i in range(len(props))],'state_codes':[None if not state else self.read(state+0x28,'H') for state in states],'owner_matches':all(not state or self.read(state+0x18,'Q')==owner for state in states),'getters':getters,'setters':setters}
