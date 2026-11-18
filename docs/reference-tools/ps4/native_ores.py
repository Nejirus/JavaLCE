"""Original mcps4 OreFeature kernel with controlled block/predicate callbacks."""
from native_carvers import Oracle,C,P,U,I,D
import hashlib
import struct


def initial_block(profile,x,y,z):
    if y<0 or y>=256:
        return 0
    if profile=='air':
        return 0
    if profile=='mixed':
        choices=[1,1|(1<<16),1|(2<<16),1|(3<<16),1|(4<<16),1|(5<<16),1|(6<<16),2,3,13,0,49,7]
        return choices[(x*17+y*11+z*31)%len(choices)]
    return 1


class OreOracle(Oracle):
    def __init__(self,executable,library):
        super().__init__(executable,library)
        self.patch(0xaf4a20,P,[P],lambda app:self.rules if self.profile=='rules' else 0)
        self.patch(0x936740,I,[P,P,P],self.intersects)
        self.patch(0x3f20c0,I,[P,I,I],self.height)
        self.patch(0x3efa80,I,[P,I,I,I,I,I,I],self.set_block)
        self.w(self.vt+0x78,'Q',self.callback(I,[P,I,I,I],self.get_block))
        # Execute StoneType's own initializer. Only its enum-property factory
        # is supplied; original IDs and ore-replacement flags execute unchanged.
        self.game.bind('fL3O02ypZFE',P(self.lib.base+self.lib.exports['fL3O02ypZFE']))
        self.patch(0x67fae0,P,[P,P,P,P],lambda *args:self.alloc(64))
        self.call(0x67f1f0,None,[],[])
        # Original predicate's initialized cached stone-ID field (registry binding).
        self.game.w(0x142dca8,'B',1)
        self.game.w(0x142dca0,'I',1)
        self.predicate=self.game.base+0x1401258
        self.rules=self.alloc(16)
        self.random=self.alloc(24)
        self.ore=self.alloc(32)
        self.w(self.ore+0x18,'Q',self.predicate)

    def predicate_cases(self):
        cases=[]
        pair=self.alloc(8)
        for tile in [0,1,2,3,13,56]:
            for meta in range(7):
                self.w(pair,'I',tile)
                self.w(pair+4,'I',meta)
                result=self.call(0x504a30,I,[P,P],[self.predicate,pair])&255
                cases.append({'state':tile|(meta<<16),'result':bool(result)})
        return cases

    def get_block(self,world,x,y,z):
        state=self.changed.get((x,y,z),initial_block(self.profile,x,y,z))
        self.read_hash.update(struct.pack('<4i',x,y,z,state))
        self.read_count+=1
        return ((state&65535)<<4)|((state>>16)&15)

    def set_block(self,world,x,y,z,tile,meta,flags):
        state=tile|(meta<<16)
        self.writes.append([x,y,z,state,flags])
        self.changed[x,y,z]=state
        return 1

    def height(self,world,x,z):
        self.height_queries.append([x,z])
        return self.height_value

    def intersects(self,rules,minimum,maximum):
        box=[self.read(minimum+i*4,'i') for i in range(3)]+[self.read(maximum+i*4,'i') for i in range(3)]
        self.rule_queries.append(box)
        return box[0]<=self.position[0]+8<=box[3]

    def place(self,seed,size,position,profile='stone',height=128,state=56):
        self.reset_world(profile,height,position)
        self.w(self.ore+0xc,'I',state&65535)
        self.w(self.ore+0x10,'I',state>>16)
        self.w(self.ore+0x14,'I',size)
        self.call(0x596c90,None,[P,U],[self.random,seed&0xffffffffffffffff])
        pos=self.alloc(12)
        for i,v in enumerate(position):
            self.w(pos+4*i,'i',v)
        result=self.call(0x503f60,I,[P,P,P,P],[self.ore,self.world,self.random,pos])&255
        after=self.call(0x58fd50,D,[P],[self.random])
        return {'seed':seed,'size':size,'position':position,'profile':profile,'height':height,
                'state':state,'result':bool(result),**self.world_result(),'random_after':after}

    def reset_world(self,profile,height,position):
        self.profile=profile
        self.position=position
        self.height_value=height
        self.changed={}
        self.writes=[]
        self.height_queries=[]
        self.rule_queries=[]
        self.read_hash=hashlib.sha256()
        self.read_count=0

    def world_result(self):
        return {'writes':self.writes,'read_count':self.read_count,
                'read_sha256':self.read_hash.hexdigest(),'height_queries':self.height_queries,
                'rule_queries':self.rule_queries}

    # Configuration fields, feature pointers and legacy states supplied to the
    # original dispatcher. Counts/ranges/sizes are read from its own settings.
    ORES=[('dirt',0x74,0x38,3),('gravel',0x84,0x40,13),
          ('diorite',0xa4,0x50,1|(3<<16)),('granite',0x94,0x48,1|(1<<16)),
          ('andesite',0xb4,0x58,1|(5<<16)),('coal',0xc4,0x60,16),
          ('iron',0xd4,0x68,15),('gold',0xe4,0x70,14),
          ('redstone',0xf4,0x78,73),('diamond',0x104,0x80,56),('lapis',0x114,0x88,21)]

    def settings(self,cfg):
        return [dict(name=name,state=state,size=self.read(cfg+off,'i'),attempts=self.read(cfg+off+4,'i'),
                     minimum=self.read(cfg+off+8,'i'),maximum=self.read(cfg+off+12,'i'),triangular=name=='lapis')
                for name,off,feature,state in self.ORES]

    def decorate(self,seed,origin,mode='default',profile='stone',height=128,mesa=False):
        self.reset_world(profile,height,[origin[0],0,origin[1]])
        cfg=self.alloc(0x124)
        self.call(0x1f1940,None,[P],[cfg])
        for i,(name,off,feature,state) in enumerate(self.ORES):
            if mode=='disabled':
                self.w(cfg+off+4,'i',0)
            elif mode=='ranges':
                self.w(cfg+off+4,'i',2)
                lo,hi=[(32,8),(64,64),(255,255),(0,0),(80,16),(64,96),(0,64),(37,37),(16,0),(255,255),(40,8)][i]
                self.w(cfg+off+8,'i',lo)
                self.w(cfg+off+12,'i',hi)
        decorator=self.alloc(0x160)
        self.w(decorator+0xc,'i',origin[0])
        self.w(decorator+0x14,'i',origin[1])
        self.w(decorator+0x18,'Q',cfg)
        self.feature_calls=[]
        def place(feature,world,random,pos):
            self.feature_calls.append([self.read(feature+0xc,'I')|(self.read(feature+0x10,'I')<<16),self.read(feature+0x14,'i')]+[self.read(pos+i*4,'i') for i in range(3)])
            return self.call(0x503f60,I,[P,P,P,P],[feature,world,random,pos])
        fvt=self.alloc(32)
        self.w(fvt+0x10,'Q',self.callback(I,[P,P,P,P],place))
        for name,off,field,state in self.ORES:
            feature=self.alloc(32)
            self.w(feature,'Q',fvt)
            self.w(feature+0xc,'I',state&65535)
            self.w(feature+0x10,'I',state>>16)
            self.w(feature+0x14,'i',self.read(cfg+off,'i'))
            self.w(feature+0x18,'Q',self.predicate)
            self.w(decorator+field,'Q',feature)
        # Level's global block-update mode is outside the controlled block bridge.
        self.patch(0x3edc80,None,[I],lambda flag:None)
        self.call(0x596c90,None,[P,U],[self.random,seed&0xffffffffffffffff])
        self.call(0x46ba10 if mesa else 0x88970,None,[P,P,P],[decorator,self.world,self.random])
        after=self.call(0x58fd50,D,[P],[self.random])
        return {'seed':seed,'origin':origin,'mode':mode,'profile':profile,'height':height,
                'settings':self.settings(cfg),'feature_calls':self.feature_calls,
                **self.world_result(),'random_after':after}
