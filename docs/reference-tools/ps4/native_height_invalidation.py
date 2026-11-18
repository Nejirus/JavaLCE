"""Original LevelChunk write entry and its precipitation-cache invalidation.

Both paths return before compressed writes/lighting/physics: the fast path has
an explicit null storage pointer; the ordinary path reads an unchanged supplied
voxel and an empty block-entity reference. The invalidation prefix itself is
original code and executes before either success/failure outcome.
"""
from native_carvers import Oracle,C,P,I,U
import hashlib

class HeightInvalidationOracle(Oracle):
    def __init__(self,executable,library):
        super().__init__(executable,library)
        self.chunk=self.alloc(0x300);vt=self.alloc(0x200);self.w(self.chunk,'Q',vt);self.w(self.chunk+0x1d8,'Q',self.alloc(256))
        self.w(vt+0x188,'Q',self.callback(P,[P,P,P,I],self.block_entity))
        self.patch(0x224540,I,[P,I,I,I,P],self.packed_read)
    def block_entity(self,out,chunk,pos,mode):
        if mode!=2:raise ValueError('Unexpected block-entity lookup mode')
        self.events.append(['block_entity']+[self.read(pos+4*i,'i') for i in range(3)]+[mode]);self.w(out,'Q',0);self.w(out+8,'Q',0);return out
    def packed_read(self,data,x,y,z,output):
        if output:raise ValueError('Unexpected nonnull packed-value constant output')
        self.events.append(['packed_read',x,y,z,0]);return 16
    def invalidate(self,position,cached,fast):
        self.events=[];C.memset(self.chunk+0x29,128,256);index=((position[2]&15)<<4)|(position[0]&15);self.w(self.chunk+0x29+index,'B',cached)
        result=self.call(0x40af70,I,[P,I,I,I,U,I,I,I],[self.chunk,*position,1,0,1,fast])&255
        return {'position':position,'cached':cached,'fast':bool(fast),'write_result':bool(result),'cache_after':self.read(self.chunk+0x29+index,'B'),'cache_sha256':hashlib.sha256(C.string_at(self.chunk+0x29,256)).hexdigest(),'events':self.events}
