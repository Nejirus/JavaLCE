"""Original spring placement and immediate-tick dispatch, controlled fluid tick."""
from native_tree_selection import TreeSelectionOracle,C,P,U,I


class SpringOracle(TreeSelectionOracle):
    def __init__(self,executable,library):
        super().__init__(executable,library)
        self.w(self.vt+0x50,'Q',self.callback(I,[P,I,I,I],lambda world,x,y,z:self.get_block(world,x,y,z)>>4))
        self.w(self.blockvt+0x1f0,'Q',self.callback(None,[P,P,P,P,P],self.fluid_tick))
        self.game.w(0x14188c0,'I',9)
        self.patch(0xafcba0,I,[I,U],self.immediate_tick)
        self.patch(0xaf4a20,P,[P],lambda app:self.rules if self.protected else 0)

    def intersects(self,rules,minimum,maximum):
        box=[self.read(minimum+4*i,'i') for i in range(3)]+[self.read(maximum+4*i,'i') for i in range(3)]
        self.rule_queries.append(box);return self.reject_rule

    def initial(self,x,y,z):return self.context.get((x,y,z),0)

    def set_block(self,world,x,y,z,tile,meta,flags):
        result=super().set_block(world,x,y,z,tile,meta,flags);self.lifecycle.append(['set',x,y,z,tile|(meta<<16),flags]);return result

    def immediate_tick(self,key,value):self.lifecycle.append(['immediate',key,value]);self.immediate=bool(value);return 1

    def fluid_tick(self,block,world,pos,state,random):
        values=[C.c_int64(self.call(0x5970e0,U,[P],[random])).value for _ in range(self.tick_draws)]
        self.lifecycle.append(['tick',*self.xyz(pos),self.unpacked(state),self.immediate,values])

    def spring(self,seed,position,above,below,current,sides,target,protected=False,reject_rule=False,tick_draws=1):
        self.reset_world('spring',256,position);self.fluid_queries=[];self.support_queries=[];self.lifecycle=[];self.immediate=False
        self.protected=protected;self.reject_rule=reject_rule;self.tick_draws=tick_draws
        x,y,z=position;self.context={(x,y,z):current,(x,y+1,z):above,(x,y-1,z):below}
        for (dx,dz),value in zip([(-1,0),(1,0),(0,-1),(0,1)],sides):self.context[x+dx,y,z+dz]=value
        self.w(self.feature+0x10,'Q',self.blocks[target])
        pos=self.alloc(12)
        for i,value in enumerate(position):self.w(pos+4*i,'i',value)
        self.call(0x596c90,None,[P,U],[self.random,seed&0xffffffffffffffff])
        result=bool(self.call(0x672c40,I,[P,P,P,P],[self.feature,self.world,self.random,pos])&255)
        return {'seed':seed,'position':position,'profile':'spring','above':above,'below':below,'current':current,'sides':sides,'target':target,'protected':protected,'reject_rule':reject_rule,'tick_draws':tick_draws,
                'result':result,**self.world_result(),'lifecycle':self.lifecycle,'fluid_queries':self.fluid_queries,'light_queries':self.light_queries,'random_after':C.c_int64(self.call(0x5970e0,U,[P],[self.random])).value}
