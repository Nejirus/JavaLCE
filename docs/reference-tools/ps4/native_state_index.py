"""Original final state-ID enumeration loop with supplied definition vectors.

Host ABI shim supplies only the fragment's node/frame context; original
definition getter, loops, metadata/ID callback order and both stores execute.
The earlier constructor/property/RTTI initialization phases do not execute.
"""
import hashlib,struct,subprocess,tempfile
from pathlib import Path
from native_state_ids import StateIdsOracle,C,P,I

class StateIndexOracle(StateIdsOracle):
    def __init__(self,executable,library):
        super().__init__(executable,library)
        if C.string_at(self.game.base+0xb7737,6)!=b'\x48\x8b\x08\x48\x39\xc8' or C.string_at(self.game.base+0xb77ea,5)!=b'\xe8\x11\x65\x5c\x00':raise ValueError('State index body boundary changed')
        self.temporary=tempfile.TemporaryDirectory(prefix='ps4-state-index-');compiled=Path(self.temporary.name)/'capture.so';subprocess.run(['cc','-shared','-fPIC',str(Path(__file__).with_name('state_index_capture.S')),'-o',str(compiled)],check=True)
        self.capture=C.CDLL(str(compiled));self.capture.run_state_index_body.argtypes=[P,P];self.capture.run_state_index_body.restype=None;C.memmove(self.game.base+0xb77ea,b'\xc3',1)
        # This capture uses the actual original startup table capacity.
        self.state_map=self.alloc(8+5856*8);self.w(self.state_map,'Q',self.game.base+0x12188c0);self.game.w(0x140ad70,'Q',self.state_map)
        self.index_block_vt=self.alloc(0x488);self.w(self.index_block_vt+0x50,'Q',self.callback(I,[P,P],self.get_data));self.w(self.index_block_vt+0x480,'Q',self.game.base+0x95490)
    def index(self,name,values,blocks):
        C.memset(self.state_map+8,0,5856*8);states={};tokens={}
        for entry in values:
            token=entry['token'];data=entry['data']
            if token<0 or token in states or not 0<=data<16:raise ValueError('Invalid tagged state input')
            state=self.alloc(24);self.w(state+8,'i',token);self.w(state+0x10,'i',0x12345678);self.w(state+0x14,'i',data);states[token]=state;tokens[state]=token
        container=self.alloc(16);sentinel=self.alloc(144);self.w(container+8,'Q',sentinel);last=sentinel
        for slot,entry in enumerate(blocks):
            if not 0<=entry['id']<=365:raise ValueError('Index fixture would exceed original table')
            block=self.alloc(512);self.w(block,'Q',self.index_block_vt);self.w(block+8,'i',entry['id']);self.w(block+12,'i',slot)
            definition=self.alloc(64);vector=self.alloc(max(8,len(entry['states'])*8));self.w(definition+0x28,'Q',vector);self.w(definition+0x30,'I',len(entry['states']));self.w(block+0x188,'Q',definition)
            for j,token in enumerate(entry['states']):self.w(vector+j*8,'Q',0 if token is None else states[token])
            node=self.alloc(144);self.w(node+0x80,'Q',block);self.w(last,'Q',node);last=node
        self.w(last,'Q',sentinel);self.events=hashlib.sha256();self.capture.run_state_index_body(self.game.base+0xb7737,container);events=self.events.hexdigest();self.events=None;digest=hashlib.sha256()
        for index in range(5856):
            pointer=self.read(self.state_map+8+index*8,'Q');digest.update(struct.pack('<i',-1 if pointer==0 else tokens[pointer]))
        return {'name':name,'values':values,'blocks':blocks,'state_ids':[[entry['token'],self.read(states[entry['token']]+0x10,'i')] for entry in values],'table_sha256':digest.hexdigest(),'events_sha256':events}
