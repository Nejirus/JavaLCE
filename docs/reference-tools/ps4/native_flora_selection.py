"""Original flower/grass selectors with original enums and native biome noise."""
from native_tree_selection import TreeSelectionOracle,C,P,U,I
from native_carvers import D


class FloraSelectionOracle(TreeSelectionOracle):
    FLOWERS={'DEFAULT':0x84fe0,'FOREST':0x2f6a20,'PLAINS':0x53d410,'SWAMP':0x6ca1d0}
    GRASS={'DEFAULT':0x84fb0,'TAIGA':0x6d1850,'JUNGLE':0x3c4fc0,'BAMBOO_JUNGLE':0x41830}

    def __init__(self,executable,library):
        super().__init__(executable,library)
        # Original UTF-16 memory copy and compare implementations.
        for nid in ['Noj9PsJrsa8','QJ5xVfKkni0']:self.game.bind(nid,P(self.lib.base+self.lib.exports[nid]))
        self.call(0x34cbc0,None,[],[])
        self.game.w(0x140ab48,'Q',self.blocks[37]);self.game.w(0x140b2d0,'Q',self.blocks[38])
        self.flowers=[self.game.read(0x1416700+8*i,'Q') for i in range(12)]
        self.flower_by_state={self.flower_state(f):f for f in self.flowers}
        self.flower_boxes={}
        for state,flower in self.flower_by_state.items():
            box=self.alloc(32);self.w(box,'Q',self.read(self.type_boxes[0],'Q'));self.w(box+16,'Q',flower);self.flower_boxes[state]=box
        self.flower_property=self.alloc(16)
        self.flower_inputs=[{'index':i,'state':self.flower_state(f),'meta':self.read(f+0x10,'i'),'ordinal':self.read(f+0x78,'i'),'name':self.text(f+0x18)} for i,f in enumerate(self.flowers)]
        for tile in [37,38]:
            vt=self.alloc(0x500);C.memmove(vt,self.game.base+0x1258458,0x500)
            self.w(vt+0x4e8,'Q',self.callback(P,[P],lambda block:self.flower_property));self.w(self.blocks[tile],'Q',vt)
        self.properties['grass_variant']=self.alloc(16);self.game.w(0x14333a8,'Q',self.properties['grass_variant'])
        self.grass_inputs=[]
        for address,pc in [(0x1433458,0x707c04),(0x14333a0,0x707c97),(0x1433398,0x707d2b)]:
            if C.string_at(self.game.base+pc,3)!=b'\xc7\x43\x08':raise ValueError('Unexpected grass enum initializer')
            id=self.game.read(pc+3,'I');self.game.w(address,'Q',self.types[id]);self.grass_inputs.append({'alias':hex(address),'initializer':hex(pc),'id':id})
        self.game.w(0x1430180,'Q',0x3fd76cf5d0b09954);self.game.w(0x1430188,'Q',0x3fcb0cb174df99c8)
        random=self.alloc(24);self.noise=self.alloc(24)
        self.call(0x596c90,None,[P,U],[random,2345]);self.call(0x52a300,None,[P,P,I],[self.noise,random,1]);self.game.w(0x140ab08,'Q',self.noise)
        self.original_noise=self.read(self.read(self.noise,'Q')+0x10,'Q')-self.game.base
        nvt=self.alloc(32);self.w(nvt+0x10,'Q',self.callback(D,[P,D,D],self.noise_value));self.w(self.noise,'Q',nvt)

    def text(self,obj):
        length=self.read(obj+24,'Q');data=self.read(obj+8,'Q') if self.read(obj+32,'Q')>=8 else obj+8
        return C.string_at(data,length*2).decode('utf-16le')

    def flower_state(self,flower):
        category=self.call(0x2e9570,P,[P],[flower]);block=self.call(0x2e9c30,P,[P],[category])
        return self.read(block+8,'I')|(self.read(flower+0x10,'i')<<16)

    def property(self,state,prop):
        packed=self.read(state+8,'I')
        if prop==getattr(self,'flower_property',None):return self.flower_boxes[(packed>>4)|((packed&15)<<16)]
        if prop==self.properties.get('grass_variant'):return self.type_boxes[packed&15]
        return super().property(state,prop)

    def set_property(self,state,prop,boxed):
        packed=self.read(state+8,'I')
        if prop==getattr(self,'flower_property',None):return self.state((packed&~15)|self.read(self.read(boxed+16,'Q')+0x10,'I'))
        if prop==self.properties.get('grass_variant'):return self.state((packed&~15)|self.read(self.read(boxed+16,'Q')+8,'I'))
        return super().set_property(state,prop,boxed)

    def noise_value(self,noise,x,z):
        value=self.call(self.original_noise,D,[P,D,D],[noise,x,z]) if self.override is None else self.override
        self.noise_queries.append([x,z,value]);return value

    def flower(self,kind,variant,seed,position,override=None):
        self.w(self.biome+0x1d0,'i',variant);self.noise_queries=[];self.override=override
        self.call(0x596c90,None,[P,U],[self.random,seed&0xffffffffffffffff])
        pos=self.alloc(12)
        for i,value in enumerate(position):self.w(pos+4*i,'i',value)
        flower=self.call(self.FLOWERS[kind],P,[P,P,P],[self.biome,self.random,pos]);state=self.flower_state(flower)
        block=self.blocks[state&65535]
        self.call(0x2ea400,None,[P,P,P],[self.feature,block,flower])
        configured=self.read(self.feature+0x18,'Q');native_data=self.call(0x2e9b60,I,[P,P],[block,configured])
        return {'kind':kind,'variant':variant,'seed':seed,'position':position,'noise_override':override,'noise_queries':self.noise_queries,
                'state':state,'configured_state':self.unpacked(configured),'native_data':native_data,'random_after':C.c_int64(self.call(0x5970e0,U,[P],[self.random])).value}

    def grass(self,kind,seed):
        self.call(0x596c90,None,[P,U],[self.random,seed&0xffffffffffffffff])
        feature=self.call(self.GRASS[kind],P,[P,P],[self.biome,self.random]);state=self.read(feature+0x10,'Q');unpacked=self.unpacked(state)
        native_data=self.call(0x6d3750,I,[P,P],[self.blocks[31],state]);self.host.free(feature)
        return {'kind':kind,'seed':seed,'state':unpacked,'native_data':native_data,'random_after':C.c_int64(self.call(0x5970e0,U,[P],[self.random])).value}
