"""Original huge-mushroom and melon geometry with controlled voxel bindings."""
from native_flora import FloraOracle,C,P,U,I


class MushroomOracle(FloraOracle):
    # Each immediate is read from its original mov [rbx+8], imm32. Aliases
    # are assigned by the following original initializer stores at 0x3d86e0.
    ENUMS=[(0x1417488,0x3d9251),(0x1417480,0x3d92e4),(0x1417478,0x3d9377),
           (0x1417470,0x3d940a),(0x14174a0,0x3d949d),(0x1417468,0x3d9530),
           (0x1417460,0x3d95c3),(0x1417458,0x3d9656),(0x1417450,0x3d96e9),
           (0x14174a8,0x3d977c),(0x14174b0,0x3d980f),(0x1417448,0x3d98a2),(0x14174b8,0x3d9936)]

    def __init__(self,executable,library):
        super().__init__(executable,library)
        self.properties['huge']=self.alloc(16)
        self.game.w(0x14173d0,'Q',self.properties['huge'])
        self.huge_boxes={}
        self.huge_enums={}
        self.enum_inputs=[]
        boxvt=self.alloc(64)
        self.w(boxvt+0x18,'Q',self.callback(I,[P,P],lambda box,type:1))
        for address,pc in self.ENUMS:
            if C.string_at(self.game.base+pc,3)!=b'\xc7\x43\x08':raise ValueError('Unexpected native enum initializer')
            id=self.game.read(pc+3,'I')
            enum=self.alloc(64)
            self.w(enum+8,'I',id)
            self.huge_enums[id]=enum
            self.game.w(address,'Q',enum)
            box=self.alloc(32)
            self.w(box,'Q',boxvt)
            self.w(box+16,'Q',enum)
            self.huge_boxes[id]=box
            self.enum_inputs.append({'alias':hex(address),'initializer':hex(pc),'id':id})
        for id,enum in self.huge_enums.items():self.game.w(0x14173e0+id*8,'Q',enum)
        self.w(self.blocks[99],'Q',self.game.base+0x126a378)
        self.w(self.blocks[100],'Q',self.game.base+0x126a378)
        self.w(self.blocks[103],'Q',self.game.base+0x1280058)
        self.game.w(0x140b4d0,'Q',self.blocks[99])
        self.game.w(0x140b4d8,'Q',self.blocks[100])
        self.game.w(0x140b4f0,'Q',self.blocks[103])

    def property(self,state,prop):
        if prop==self.properties.get('huge'):return self.huge_boxes[self.read(state+8,'I')&15]
        return super().property(state,prop)

    def set_property(self,state,prop,boxed):
        if prop==self.properties.get('huge'):
            tile=self.read(state+8,'I')>>4
            enum=self.read(boxed+16,'Q')
            return self.state((tile<<4)|self.read(enum+8,'I'))
        return super().set_property(state,prop,boxed)

    def initial(self,x,y,z):
        ox,oy,oz=self.position
        if self.profile=='blocked' and (x,y,z)==(ox+1,oy+4,oz+1):return 17
        if self.profile=='trunk_obstacle' and (x,y,z)==(ox,oy+1,oz):return 17
        if self.profile=='glass' and (x,y,z)==(ox,oy+1,oz):return 20
        if self.profile=='leaves' and y>=oy and y<oy+16 and abs(x-ox)<=3 and abs(z-oz)<=3:return 18
        if self.profile=='replaceable' and y==oy and (x+z)%3==0:return 31|(2<<16)
        return super().initial(x,y,z)

    def geometry(self,kind,seed,position,profile,forced=0,updates=False):
        self.reset_world(profile,256,position)
        self.fluid_queries=[]
        self.w(self.feature,'Q',self.game.base+0x126a880)
        self.w(self.feature+8,'B',int(updates))
        self.w(self.feature+0x10,'Q',self.blocks[forced] if forced else 0)
        pos=self.alloc(12)
        for i,value in enumerate(position):self.w(pos+i*4,'i',value)
        self.call(0x596c90,None,[P,U],[self.random,seed&0xffffffffffffffff])
        result=self.call(0x370120 if kind=='huge' else 0x4642c0,I,[P,P,P,P],[self.feature,self.world,self.random,pos])&255
        after=self.call(0x5970e0,U,[P],[self.random])
        return {'kind':kind,'seed':seed,'position':position,'profile':profile,'forced':forced,'updates':updates,
                'result':bool(result),**self.world_result(),'fluid_queries':self.fluid_queries,'light_queries':self.light_queries,'random_after':C.c_int64(after).value}
