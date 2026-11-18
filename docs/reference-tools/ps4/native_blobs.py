"""Original BlockBlobFeature, including its original position iterator."""
from native_trees import TreeOracle,C,P,U,I


class BlobOracle(TreeOracle):
    def initial(self,x,y,z):
        if self.profile=='empty':return 0
        if self.profile=='sand' and y==self.position[1]-1:return 12
        return super().initial(x,y,z)

    def blob(self,seed,position,profile,radius,state):
        self.reset_world(profile,position[1],position);self.fluid_queries=[];self.support_queries=[]
        block=self.blocks[state&65535];self.w(block+0x190,'Q',self.state(((state&65535)<<4)|(state>>16)))
        self.w(self.feature+0x10,'Q',block);self.w(self.feature+0x18,'i',radius)
        pos=self.alloc(12)
        for i,v in enumerate(position):self.w(pos+4*i,'i',v)
        self.call(0x596c90,None,[P,U],[self.random,seed&0xffffffffffffffff])
        result=bool(self.call(0xb85d0,I,[P,P,P,P],[self.feature,self.world,self.random,pos])&255)
        return {'seed':seed,'position':position,'profile':profile,'radius':radius,'state':state,'result':result,**self.world_result(),
                'fluid_queries':self.fluid_queries,'light_queries':self.light_queries,'random_after':C.c_int64(self.call(0x5970e0,U,[P],[self.random])).value}
