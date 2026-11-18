"""Original BiomeDecorator constructor/main with controlled feature virtuals.
This captures dispatcher sequencing, not the supplied features' geometry.
"""
from native_flora import FloraOracle,C,P,U,I
from native_carvers import F
import mmap


class DecorationOracle(FloraOracle):
    FIELDS=[('waterlilies',0x118),('trees',0x11c),('extraTreeChance',0x120),('flowers',0x124),('grass',0x128),
            ('deadBushes',0x12c),('mushrooms',0x130),('reeds',0x134),('cacti',0x138),('gravel',0x13c),
            ('sand',0x140),('clay',0x144),('hugeMushrooms',0x148),('kelp',0x14c),('seagrass',0x154),('springs',0x158)]
    FEATURES={0x20:('clay',82),0x28:('sand',12),0x30:('gravel',13),0x90:('flower',37),
              0x98:('brown_mushroom',39),0xa0:('red_mushroom',40),0xa8:('huge_mushroom',0),
              0xb0:('reeds',83),0xb8:('cactus',81),0xc0:('waterlily',111),0xc8:('pumpkin',257),
              0xd0:('kelp',0),0xf8:('seagrass',0)}

    def __init__(self,executable,library):
        super().__init__(executable,library)
        # The constructor's count/flag stores execute unchanged. The feature
        # constructors are supplied as object bindings for this dispatch test.
        for pc in [0x1813c0,0x5c3e30,0x2ea330,0x271a20,0x3700f0,0x3c6400,0x5f6700,0x372190,0x1722d0]:
            self.patch(pc,P,[P,U,U,U,U,U],lambda obj,*args:obj)
        self.decorator=self.alloc(0x160)
        self.call(0x86c00,None,[P],[self.decorator])
        self.defaults=self.settings()
        self.feature_info={}
        self.fvt=self.alloc(64)
        self.w(self.fvt+8,'Q',self.callback(None,[P],lambda obj:None))
        self.w(self.fvt+0x10,'Q',self.callback(I,[P,P,P,P],self.place))
        self.w(self.fvt+0x18,'Q',self.callback(None,[P],lambda obj:self.events.append(['prepare',self.feature_info[obj][0]])))
        self.w(self.fvt+0x28,'Q',self.callback(None,[P,P,P,P],self.post_place))
        self.dvt=self.alloc(64)
        self.w(self.decorator,'Q',self.dvt)
        self.w(self.dvt+0x20,'Q',self.callback(None,[P,P,P],self.ores))
        self.bvt=self.alloc(64)
        self.w(self.biome,'Q',self.bvt)
        self.w(self.bvt+0x10,'Q',self.callback(P,[P,P],self.choose_tree))
        self.w(self.bvt+0x18,'Q',self.callback(P,[P,P],self.choose_grass))
        self.w(self.bvt+0x20,'Q',self.callback(P,[P,P,P],self.choose_flower))
        for off,(kind,state) in self.FEATURES.items():self.w(self.decorator+off,'Q',self.make_feature(kind,state))
        self.patch(0x1f72a0,P,[P,P],lambda obj,block:self.bind_feature(obj,'dead_bush',32))
        self.patch(0x672c10,P,[P,P],lambda obj,block:self.bind_feature(obj,'water_spring' if self.read(block+8,'I')==8 else 'lava_spring',self.read(block+8,'I')))
        # BlockPos return values use RAX/RDX. Python callbacks cannot return
        # that aggregate ABI, so use a tiny original-ABI adapter per helper.
        self.position_adapter(0x3f1f40,'height')
        self.position_adapter(0x3f7ca0,'top')
        self.w(self.vt+0xa8,'Q',self.callback(I,[P,P],self.empty))
        self.patch(0x2e9570,P,[P],lambda choice:choice)
        self.patch(0x2e9c30,P,[P],lambda choice:self.blocks[self.choice_states[choice]&65535])
        self.patch(0x2ea400,None,[P,P,P],self.set_flower)

    def bind_feature(self,obj,kind,state):
        self.w(obj,'Q',self.fvt)
        self.feature_info[obj]=(kind,state)
        return obj

    def make_feature(self,kind,state):return self.bind_feature(self.alloc(32),kind,state)

    def settings(self):
        return {key:bool(self.read(self.decorator+off,'B')) if key=='springs' else self.read(self.decorator+off,'f' if key=='extraTreeChance' else 'i') for key,off in self.FIELDS}

    def next_long(self,random):return C.c_int64(self.call(0x5970e0,U,[P],[random])).value

    def next_int(self,random,n):return self.call(0x593950,I,[P,I],[random,n])

    def place(self,feature,world,random,pos):
        kind,state=self.feature_info[feature]
        value=self.next_long(random)
        self.events.append(['place',kind,*self.xyz(pos),state,value])
        return value&1

    def post_place(self,feature,world,random,pos):
        self.events.append(['post',self.feature_info[feature][0],*self.xyz(pos),self.next_long(random)])

    def ores(self,decorator,world,random):self.events.append(['ores',self.next_long(random)])

    def choose_tree(self,biome,random):
        selection=self.next_int(random,3)
        self.events.append(['tree',selection])
        return self.make_feature('tree'+str(selection),0)

    def choose_grass(self,biome,random):
        selection=self.next_int(random,2)
        self.events.append(['grass',selection])
        return self.make_feature('grass'+str(selection),31|((selection+1)<<16))

    def choose_flower(self,biome,random,pos):
        selection=self.next_int(random,4)
        state=[0,37,38,38|(8<<16)][selection]
        self.events.append(['flower',*self.xyz(pos),state])
        choice=self.alloc(32)
        self.choice_states[choice]=state
        return choice

    def set_flower(self,feature,block,choice):
        self.feature_info[feature]=('flower',self.choice_states[choice])

    def height_value_at(self,x,z):
        return {'zero':0,'negative':-32,'one':1,'flat':64}.get(self.height_profile,32+((3*x+z)&31))

    def height_y(self,x,y,z):
        h=self.height_value_at(x,z)
        self.events.append(['height',x,y,z,h])
        return h

    def top_y(self,x,y,z):
        h=62+((x^z)&3)
        self.events.append(['top',x,y,z,h])
        return h

    def position_adapter(self,pc,kind):
        # SysV: input (world RDI,pos RSI), output packed XY RAX,Z EDX.
        # Save XYZ, call a scalar-Y callback, then rebuild the exact aggregate.
        callback=self.callback(I,[I,I,I],self.height_y if kind=='height' else self.top_y)
        code=bytes.fromhex('554889e54883ec10488b06488945f08b46088945f88b3e8b56088b75f448b8')+callback.to_bytes(8,'little')+bytes.fromhex('ffd08945f48b55f8488b45f0c9c3')
        memory=mmap.mmap(-1,len(code),prot=mmap.PROT_READ|mmap.PROT_WRITE|mmap.PROT_EXEC)
        memory.write(code)
        self.keep.append(memory)
        adapter=C.addressof(C.c_char.from_buffer(memory))
        self.game.patch(pc,P(adapter))

    def empty(self,world,pos):
        x,y,z=self.xyz(pos)
        result=y>=62+((x^z)&3)+1
        self.events.append(['empty',x,y,z,bool(result)])
        return result

    def decorate(self,seed,origin,mode,height_profile):
        for key,off in self.FIELDS:
            self.w(self.decorator+off,'B' if key=='springs' else 'f' if key=='extraTreeChance' else 'i',self.defaults[key])
        if mode=='all':
            for key,off in self.FIELDS:
                self.w(self.decorator+off,'B' if key=='springs' else 'f' if key=='extraTreeChance' else 'i',True if key=='springs' else .75 if key=='extraTreeChance' else 3)
        if mode in ['disabled','negative']:
            for key,off in self.FIELDS:self.w(self.decorator+off,'B' if key=='springs' else 'f' if key=='extraTreeChance' else 'i',False if key=='springs' else 0. if key=='extraTreeChance' else -2 if mode=='negative' else 0)
        for i,value in enumerate(origin):self.w(self.decorator+0xc+i*4,'i',value)
        self.events=[]
        self.choice_states={}
        self.height_profile=height_profile
        self.call(0x596c90,None,[P,U],[self.random,seed&0xffffffffffffffff])
        self.call(0x87760,None,[P,P,P,P],[self.decorator,self.biome,self.world,self.random])
        return {'seed':seed,'origin':origin,'mode':mode,'height_profile':height_profile,'settings':self.settings(),'events':self.events,'random_after':self.next_long(self.random)}
