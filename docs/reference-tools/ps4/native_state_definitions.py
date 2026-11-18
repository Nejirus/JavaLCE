"""Complete original canonical-definition constructor and property operations.

Property count/enumeration/ordinal methods and boxed scalar hash/equality are
explicit context bindings. Original layout, Cartesian iteration, sparse state
allocation, getter/setter algorithms and state identity execute unchanged.
"""
import hashlib,os,struct
from native_carvers import Oracle,C,P,I,U

class StateDefinitionsOracle(Oracle):
    def __init__(self,executable,library):
        super().__init__(executable,library)
        for nid in ['fL3O02ypZFE','Noj9PsJrsa8','QJ5xVfKkni0']:self.game.bind(nid,P(self.lib.base+self.lib.exports[nid]))
        self.prop_vt=self.alloc(0x60);self.box_vt=self.alloc(0x60);self.boxes={};self.props={};self.events=None;self.hash_mode='ordinal'
        for slot in range(0,0x60,8):
            self.w(self.prop_vt+slot,'Q',self.unknown('property',slot));self.w(self.box_vt+slot,'Q',self.unknown('box',slot))
        for slot,ret,args,fn in [(8,P,[P],self.possible),(0x40,I,[P],self.count),(0x48,P,[P,I],self.by_index),(0x50,I,[P,P],self.ordinal)]:self.w(self.prop_vt+slot,'Q',self.callback(ret,args,fn))
        self.w(self.box_vt+0x20,'Q',self.callback(U,[P,U,U,U,U,U],self.scalar_hash));self.w(self.box_vt+0x38,'Q',self.callback(I,[P,P],self.equal))
        self.getter=self.game.function(0xd04f0,P,[P,P]);self.setter=self.game.function(0xd0550,P,[P,P,P]);self.missing_property=self.alloc(8)
    def unknown(self,kind,slot):
        def fail(*args):print('UNBOUND CANONICAL',kind,hex(slot),flush=True);os._exit(95)
        return self.callback(P,[P,P,P,P,P,P],fail)
    def event(self,a,b,c):
        if self.events is not None:self.events.update(struct.pack('<3i',a,b,c))
    def box(self,value,fresh=False):
        if fresh or value not in self.boxes:
            pointer=self.alloc(32);self.w(pointer,'Q',self.box_vt);self.w(pointer+16,'i',value)
            if fresh:return pointer
            self.boxes[value]=pointer
        return self.boxes[value]
    def possible(self,prop):return self.props[prop]['possible']
    def count(self,prop):return self.props[prop]['count']
    def by_index(self,prop,index):self.event(0,self.props[prop]['id'],index);return self.box(index)
    def ordinal(self,prop,boxed):
        value=self.read(boxed+16,'i');self.event(1,self.props[prop]['id'],value);return value
    def scalar_hash(self,boxed,*unused):
        value=self.read(boxed+16,'I')
        return 0 if self.hash_mode=='zero' else value if self.hash_mode=='ordinal' else (value^0x123456789abcdef)&0xffffffffffffffff
    def equal(self,first,second):
        a,b=self.read(first+16,'i'),self.read(second+16,'i');self.event(2,a,b);return a==b
    def definition(self,properties,hash_mode):
        self.events=None;self.hash_mode=hash_mode;capacity_bound=1;ordered=[]
        for index,spec in enumerate(properties):
            count=spec['count'];indices=spec['indices']
            if not 1<=count<=17 or not indices or len(set(indices))!=len(indices) or any(not 0<=v<count for v in indices):raise ValueError('Unbounded supplied property context or empty value enumeration')
            capacity_bound*=1<<(count-1).bit_length()
            if capacity_bound>4096:raise ValueError('Unbounded state capacity')
            prop=self.alloc(32);self.w(prop,'Q',self.prop_vt);possible=self.alloc(32);vector=self.alloc(max(8,len(indices)*8))
            for j,value in enumerate(indices):self.w(vector+j*8,'Q',self.box(value))
            self.w(possible+8,'Q',vector);self.w(possible+0x10,'Q',vector+len(indices)*8);self.w(possible+0x18,'Q',vector+len(indices)*8);self.props[prop]={'id':index,'count':count,'possible':possible};ordered.append(prop)
        owner=self.alloc(512);definition=self.alloc(0x48);props=self.alloc(max(8,len(ordered)*8))
        for index,prop in enumerate(ordered):self.w(props+index*8,'Q',prop)
        self.call(0xcf850,None,[P,P,P,U],[definition,owner,props,len(ordered)])
        capacity=self.read(definition+0x30,'I');table=self.read(definition+0x28,'Q');states=[self.read(table+i*8,'Q') for i in range(capacity)];codes=[None if not state else self.read(state+0x28,'H') for state in states];default=self.call(0x7a050,P,[P],[definition])
        layout={'capacity':capacity,'shifts':[self.read(self.read(definition+0x38,'Q')+i,'B') for i in range(len(ordered))],'masks':[self.read(self.read(definition+0x40,'Q')+i*2,'H') for i in range(len(ordered))],'state_codes':codes,'default_code':-1 if not default else self.read(default+0x28,'H'),'owner_matches':all(not state or self.read(state+0x18,'Q')==owner for state in states)}
        self.events=hashlib.sha256();getters=[];setters=[]
        for state in states:
            if not state:continue
            code=self.read(state+0x28,'H')
            for index,prop in enumerate(ordered+[self.missing_property]):
                value=self.getter(state,prop);getters.append([code,index,None if not value else self.read(value+16,'i')])
        for state in states:
            if not state:continue
            code=self.read(state+0x28,'H')
            for index,prop in enumerate(ordered):
                count=properties[index]['count'];desired=sorted({-2147483648,-1,0,1,count-1,count,count+1,65535,65536,2147483647})
                for value in desired:
                    result=self.setter(state,prop,self.box(value,True));setters.append([code,index,value,-1 if not result else self.read(result+0x28,'H'),result==state])
        events=self.events.hexdigest();self.events=None
        return {'properties':properties,'hash_mode':hash_mode,**layout,'getters':getters,'setters':setters,'events_sha256':events}
