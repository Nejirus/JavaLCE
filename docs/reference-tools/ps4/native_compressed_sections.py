"""Original pure compressed-section emptiness body, without its TLS wrapper."""
import struct,hashlib
from native_carvers import Oracle,C,P,I

class CompressedSectionsOracle(Oracle):
    def __init__(self,executable,library):
        super().__init__(executable,library)
        self.storage=self.alloc(0x40);self.raw=self.alloc(65536);self.w(self.storage+0x38,'Q',self.raw);self.output=self.alloc(4)
    def load(self,section_y,allocated,section_offset,headers,patches):
        section=int(section_y/16)
        if not 0<=section<16 or not 0<=section_offset<=31 or len(headers)!=64:raise ValueError('Unbounded section fixture input')
        C.memset(self.storage+8,0,0x30);self.w(self.storage+0x38,'Q',self.raw);C.memset(self.raw,0,65536)
        self.w(self.storage+0x28+section,'B',allocated);self.w(self.storage+8+section*2,'H',section_offset)
        base=section_offset*256;C.memmove(self.raw+base,struct.pack('<64H',*headers),128)
        for offset,contents in patches:
            data=bytes.fromhex(contents)
            if offset<128 or base+offset+len(data)>65536:raise ValueError('Unbounded payload patch')
            C.memmove(self.raw+base+offset,data,len(data))
    def empty(self,section_y,allocated,section_offset,headers,patches,has_output,sample_index):
        self.load(section_y,allocated,section_offset,headers,patches);section=int(section_y/16)
        self.w(self.output,'I',0x12345678)
        result=self.call(0x35a090,I,[P,I,P],[self.storage,section_y,self.output if has_output else 0])&255
        reported_top=self.read(self.output,'i');origin=[((sample_index>>4)&3)*4-32,section*16+(sample_index&3)*4,((sample_index>>2)&3)*4+16];read_hash=hashlib.sha256()
        for x in range(4):
            for z in range(4):
                for y in range(4):
                    self.w(self.output,'B',0xa5)
                    value=self.call(0x35bc60,I,[P,I,I,I,P],[self.storage,origin[0]+x,origin[1]+y,origin[2]+z,self.output if has_output else 0])&65535
                    read_hash.update(struct.pack('<HH',value,self.read(self.output,'B')))
        return {'section_y':section_y,'allocated':allocated,'section_offset':section_offset,'headers':headers,'patches':patches,'has_output':has_output,'empty':bool(result),'reported_top':reported_top,'read_origin':origin,'read_samples':64,'read_sha256':read_hash.hexdigest()}
