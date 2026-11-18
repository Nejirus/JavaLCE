"""Original precipitation-height scan and cache with explicit section/material inputs.

The TLS/lock-wrapped compressed-section emptiness call is supplied as a bool;
its wrapper is never executed. Original highest-section selection and height
scan/cache execute, including both primary and secondary state reads.
"""
from native_carvers import Oracle,C,P,U,I
import hashlib


class Pos(C.Structure):_fields_=[('x',I),('y',I),('z',I)]


class PrecipitationOracle(Oracle):
    def __init__(self,executable,library):
        super().__init__(executable,library)
        down=self.alloc(0x68);self.w(down+0x60,'i',-1);self.game.w(0x1413bf0,'Q',down)
        self.chunk=self.alloc(0x300);vt=self.alloc(0x100);self.w(self.chunk,'Q',vt);self.w(self.chunk+0x20,'Q',self.alloc(16))
        self.w(vt+0xf0,'Q',self.callback(P,[P,P],lambda chunk,pos:self.sample('primary',pos)))
        self.w(vt+0xf8,'Q',self.callback(P,[P,P],lambda chunk,pos:self.sample('secondary',pos)))
        self.patch(0x223f10,I,[P,I,P],self.section_empty)
        self.materials=[];mvt=self.alloc(0x38)
        self.w(mvt+0x30,'Q',self.callback(I,[P],lambda mat:self.material_flag(mat,'blocks_motion',1)))
        self.w(mvt+0x10,'Q',self.callback(I,[P],lambda mat:self.material_flag(mat,'liquid',2)))
        for flags in range(4):
            mat=self.alloc(16);self.w(mat,'Q',mvt);self.w(mat+8,'I',flags);self.materials.append(mat)
        self.w(self.statevt,'Q',self.callback(P,[P],lambda state:self.materials[self.read(state+8,'I')>>4]))

    def section_empty(self,data,y,output):
        if output:raise ValueError('Unexpected nonnull section height output')
        empty=self.section_mask&(1<<(y//16))==0;self.events.append(['section',y,bool(empty)]);return empty

    def sample(self,kind,pos):
        x,y,z=[self.read(pos+4*i,'i') for i in range(3)];top,state=self.primary_input if kind=='primary' else self.secondary_input
        value=state if y<=top else 0;self.events.append([kind,x,y,z,value]);return self.state(value<<4)

    def material_flag(self,mat,kind,mask):
        flags=self.read(mat+8,'I');value=flags&mask!=0;self.events.append([kind,flags,bool(value)]);return value

    def heights(self,position,cached,section_mask,primary,secondary):
        self.section_mask=section_mask;self.primary_input=primary;self.secondary_input=secondary;self.events=[]
        C.memset(self.chunk+0x29,128,256);index=((position[2]&15)<<4)|(position[0]&15);self.w(self.chunk+0x29+index,'B',cached)
        pos=self.alloc(12)
        for i,value in enumerate(position):self.w(pos+4*i,'i',value)
        results=[]
        for _ in range(2):
            result=self.call(0x3f7870,Pos,[P,P],[self.chunk,pos]);results.append([result.x,result.y,result.z])
        return {'position':position,'cached':cached,'section_mask':section_mask,'primary':primary,'secondary':secondary,'results':results,'events':self.events,'cache_sha256':hashlib.sha256(C.string_at(self.chunk+0x29,256)).hexdigest()}
