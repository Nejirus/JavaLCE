"""Original table-first state lookup and distinct item encoding/decoding.

State/property conversion is an explicit stub returning tagged records. Both
tables are intentionally larger than original startup allocations so all uint16
selector inputs can execute safely; this is not a populated original registry.
"""
import hashlib,struct
from native_carvers import Oracle,C,P,I

class StateIdsOracle(Oracle):
    def __init__(self,executable,library):
        super().__init__(executable,library)
        # Oracle's generic state bridge is not used: execute the original
        # 0x91c30 wrapper and actual state-map vtable/lookup implementation.
        pc=0x91c30;segment=next(s for s in self.game.ph if s[0]==1 and s[3]<=pc<s[3]+s[5]);offset=segment[2]+pc-segment[3];C.memmove(self.game.base+pc,bytes(self.game.file[offset:offset+12]),12)
        self.registry=self.alloc(0x150+4096*8);registry_vt=self.alloc(0x40);self.w(self.registry,'Q',registry_vt);self.w(registry_vt+0x38,'Q',self.callback(I,[P,P],self.block_id));self.game.w(0x140ad68,'Q',self.registry)
        self.block_vt=self.alloc(0x58);self.w(self.block_vt+0x48,'Q',self.callback(P,[P,I],self.from_data));self.w(self.block_vt+0x50,'Q',self.callback(I,[P,P],self.get_data));self.stub_blocks={}
        for index in range(4096):
            block=self.alloc(24);self.w(block,'Q',self.block_vt);self.w(block+8,'i',index);self.w(block+12,'i',index);self.stub_blocks[index]=block;self.w(self.registry+0x150+index*8,'Q',block)
        self.stub_state_vt=self.alloc(0x178);self.w(self.stub_state_vt+0x170,'Q',self.game.base+0xd0670)
        self.state_map=self.alloc(8+65536*8);self.w(self.state_map,'Q',self.game.base+0x12188c0);self.game.w(0x140ad70,'Q',self.state_map)
        self.lookup=self.game.function(0x91c30,P,[I]);self.item_lookup=self.game.function(0x91c50,P,[I]);self.item_encode=self.game.function(0x91bc0,I,[P]);self.base_encode=self.game.function(0x91ba0,I,[P]);self.interned={};self.events=None
    def event(self,op,a,b):
        if self.events is not None:self.events.update(struct.pack('<3i',op,a,b))
    def state_record(self,kind,slot,data):
        key=(kind,slot,data)
        if key not in self.interned:
            state=self.alloc(32);self.w(state,'Q',self.stub_state_vt);self.w(state+8,'i',kind);self.w(state+12,'i',slot);self.w(state+0x14,'i',data);self.w(state+0x18,'Q',self.stub_blocks[slot]);self.interned[key]=state
        return self.interned[key]
    def fields(self,state):return [self.read(state+offset,'i') for offset in [8,12,0x14]]
    def from_data(self,block,data):
        slot=self.read(block+12,'i');self.event(0,slot,data);return self.state_record(0,slot,data)
    def block_id(self,registry,block):
        slot=self.read(block+12,'i');value=self.read(block+8,'i');self.event(1,slot,value);return value
    def get_data(self,block,state):
        slot=self.read(block+12,'i');data=self.read(state+0x14,'i');self.event(2,slot,data);return data
    def configure_cache(self,entries):
        C.memset(self.state_map+8,0,65536*8)
        for entry in entries:self.w(self.state_map+8+entry['id']*8,'Q',self.state_record(1,entry['slot'],entry['data']))
    def ranges(self):
        rows=[]
        for start in range(0,65536,256):
            results={}
            for name,fn in [('table',self.lookup),('item',self.item_lookup)]:
                digest=hashlib.sha256();self.events=hashlib.sha256()
                for value in range(start,start+256):digest.update(struct.pack('<3i',*self.fields(fn(value))))
                results[name+'_sha256']=digest.hexdigest();results[name+'_events_sha256']=self.events.hexdigest()
            rows.append({'start':start,**results})
        self.events=None;return rows
    def encode(self,registry_id,data):
        block=self.stub_blocks[0];self.w(block+8,'i',registry_id);state=self.state_record(2,0,data);self.events=hashlib.sha256();item=self.item_encode(state);item_events=self.events.hexdigest();self.events=hashlib.sha256();base=self.base_encode(block);base_events=self.events.hexdigest();self.events=None;self.w(block+8,'i',0)
        return {'registry_id':registry_id,'data':data,'item_value':item,'block_base':base,'item_events_sha256':item_events,'base_events_sha256':base_events}
