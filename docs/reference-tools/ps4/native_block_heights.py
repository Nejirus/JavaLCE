"""Original height scans using captured constructor-selected native materials.

Column states/section occupancy/cache are supplied. Original block constructor
prefixes/base stores, BlockState material/block getters, all material virtuals,
and the complete height selection/scan/cache routines execute. No synthetic
solid/liquid classification callbacks are installed in this comparison.
"""
import hashlib,struct
from native_block_materials import BlockMaterialOracle,C,P,I,U
from native_precipitation import Pos

class BlockHeightOracle(BlockMaterialOracle):
    def __init__(self,executable,library,flow):
        super().__init__(executable,library,flow,0)
        self.registrations();self.block_states={}
        for row in flow['registrations']:self.block_states[row['id']]=self.state_argument(self.pointers[row['object'][1]])
        down=self.alloc(0x68);self.w(down+0x60,'i',-1);self.game.w(0x1413bf0,'Q',down)
        self.chunk=self.alloc(0x300);vt=self.alloc(0x100);self.w(self.chunk,'Q',vt);self.w(self.chunk+0x20,'Q',self.alloc(16))
        self.w(vt+0xf0,'Q',self.callback(P,[P,P],lambda chunk,pos:self.sample(1,pos)))
        self.w(vt+0xf8,'Q',self.callback(P,[P,P],lambda chunk,pos:self.sample(2,pos)))
        self.patch(0x223f10,I,[P,I,P],self.section_empty)
    def event(self,*values):self.events+=1;self.event_hash.update(struct.pack('<5i',*values))
    def section_empty(self,data,y,output):
        if output:raise ValueError('Unexpected nonnull section height output')
        empty=self.section_mask&(1<<(y//16))==0;self.event(0,y,int(empty),0,0);return empty
    def sample(self,layer,pos):
        x,y,z=[self.read(pos+4*i,'i') for i in range(3)];top,block=self.primary if layer==1 else self.secondary
        value=block if y<=top else 0;self.event(layer,x,y,z,value);return self.block_states[value]
    def heights(self,position,cached,section_mask,primary,secondary):
        self.section_mask=section_mask;self.primary=primary;self.secondary=secondary;self.events=0;self.event_hash=hashlib.sha256();C.memset(self.chunk+0x29,128,256);index=((position[2]&15)<<4)|(position[0]&15);self.w(self.chunk+0x29+index,'B',cached)
        pos=self.alloc(12)
        for i,v in enumerate(position):self.w(pos+4*i,'i',v)
        results=[]
        for _ in range(2):
            result=self.call(0x3f7870,Pos,[P,P],[self.chunk,pos]);results.append([result.x,result.y,result.z])
        return {'position':position,'cached':cached,'section_mask':section_mask,'primary':primary,'secondary':secondary,'results':results,'events':self.events,'event_sha256':self.event_hash.hexdigest(),'cache_sha256':hashlib.sha256(C.string_at(self.chunk+0x29,256)).hexdigest()}
