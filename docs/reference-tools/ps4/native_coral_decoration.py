"""Original crust marker decoration over supplied marker order and voxel APIs."""
from native_coral_shapes import CoralShapeOracle,C,P,U,I


class CoralDecorationOracle(CoralShapeOracle):
    def __init__(self,executable,library,ordered_marker_input=True):
        super().__init__(executable,library)
        # Direction::getFacing (0x200620) reads byte +0x58, not ordinal +8.
        # Original constructor 0x2004b0 copies its ninth argument to that byte;
        # these are the six PUSH imm8 argument stores in 0x1ff830.
        self.direction_facing_inputs=[]
        for direction,pc in zip(self.directions,[0x1ff8e5,0x1ff994,0x1ffa44,0x1ffaed,0x1ffb9e,0x1ffc47]):
            if self.game.read(pc,'B')!=0x6a:raise ValueError('Unexpected original Direction facing argument')
            value=self.game.read(pc+1,'B');self.w(direction+0x58,'B',value)
            self.direction_facing_inputs.append({'argument_pc':hex(pc),'ordinal':self.read(direction+8,'I'),'facing':value})
        if ordered_marker_input:self.patch(0x6c50e0,P,[P,P,P,P],self.marker_map)

    def marker_map(self,out,template,pos,settings):
        # Explicit ordered input list. Original marker extraction/hash-map
        # insertion/iteration is outside this fixture, not simulated here.
        C.memset(out,0,80);head=self.host.malloc(72);C.memset(head,0,72)
        nodes=[self.host.malloc(72) for _ in self.markers]
        for i,node in enumerate(nodes):
            C.memset(node,0,72);self.w(node,'Q',nodes[i+1] if i+1<len(nodes) else head);self.w(node+8,'Q',nodes[i-1] if i else head)
            for axis,value in enumerate(self.markers[i]):self.w(node+16+4*axis,'i',value)
            self.w(node+64,'Q',7)
        self.w(head,'Q',nodes[0] if nodes else head);self.w(head+8,'Q',nodes[-1] if nodes else head)
        self.w(out+8,'Q',head);self.w(out+16,'Q',len(nodes));return out

    def set_block(self,world,x,y,z,tile,meta,flags):
        accepted=self.write_mode=='all' or self.write_mode=='checker' and (x+y+z)%3!=0
        self.write_attempts.append([x,y,z,tile|(meta<<16),flags,bool(accepted)])
        return super().set_block(world,x,y,z,tile,meta,flags) if accepted else 0

    def decoration(self,kind,seed,position,profile,color=0,direction=2,placement='all',write_mode='all',markers=()):
        self.shape_state=259|(color<<16);pos=self.begin_coral(profile,position,placement,'all',self.HORIZONTAL)
        self.write_mode=write_mode;self.write_attempts=[];self.markers=[list(p) for p in markers]
        self.w(self.feature,'Q',self.treevt);self.w(self.feature+8,'B',0)
        self.call(0x596c90,None,[P,U],[self.random,seed&0xffffffffffffffff]);tops=self.alloc(32);sides=self.alloc(32)
        if kind=='scatter':self.call(0x1c8bd0,None,[P,P,P,P,P,P,I],[self.feature,self.world,pos,self.random,tops,sides,color])
        elif kind=='fan':self.call(0x1c9430,None,[P,P,P,P],[self.feature,self.world,pos,self.random])
        elif kind=='side_fan':self.call(0x1c95e0,None,[P,P,P,P,P],[self.feature,self.world,pos,self.random,self.directions[direction]])
        elif kind=='markers':self.call(0x1c8940,None,[P,P,P,P,P,P],[self.feature,self.world,pos,self.random,0,0])
        else:raise ValueError(kind)
        result={'kind':kind,'seed':seed,'position':position,'profile':profile,'color':color,'direction':direction,'placement':placement,'write_mode':write_mode,'markers':self.markers,
                'write_attempts':self.write_attempts,'top_positions':self.vector(tops,12),'side_positions':self.vector(sides,24),**self.coral_result(),'random_after':C.c_int64(self.call(0x5970e0,U,[P],[self.random])).value}
        for vector in [tops,sides]:
            begin=self.read(vector+8,'Q')
            if begin:self.host.free(begin)
        return result
