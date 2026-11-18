"""Original crust dispatch/value-copy rotations with controlled template bodies.

The original feature, offsets, settings constructor, copied-Random rotation and
template random constructor execute. Template placement and postprocessing are
explicit callbacks; they do not claim original crust geometry here.
"""
from native_corals import CoralOracle,C,P,U,I
import hashlib,json


class Pos(C.Structure):_fields_=[('x',C.c_int),('y',C.c_int),('z',C.c_int)]


class CoralCrustOracle(CoralOracle):
    def __init__(self,executable,library,assets,controlled_templates=True,**kwargs):
        super().__init__(executable,library,**kwargs)
        self.rotations=[]
        for i in range(4):
            r=self.alloc(64);self.w(r+0x28,'i',i);self.rotations.append(r);self.game.w(0x142f858+8*i,'Q',r)
        for i,address in enumerate([0x14194d0,0x14194d8,0x14194e0]):
            m=self.alloc(64);self.w(m+0x28,'i',i);self.game.w(address,'Q',m)
        self.offset_inputs=[]
        for kind,count,pc in [('crust',5,0x1c84b0),('outcropping',6,0x1c8650)]:
            for index in range(count):
                for rotation in range(4):
                    p=self.call(pc,Pos,[P,P,I],[self.feature,self.rotations[rotation],index])
                    self.offset_inputs.append({'kind':kind,'index':index,'rotation':rotation,'offset':[p.x,p.y,p.z]})
        manifest=json.loads((assets/'templates.json').read_text());self.templates={};self.template_ids={};self.template_inputs=[]
        self.crust_ids=[self.game.read(0xe82260+4*i,'I') for i in range(5)]
        self.outcrop_ids=[self.game.read(0xe82280+4*i,'I') for i in range(6)]
        for entry in manifest:
            if entry['index'] not in self.crust_ids+self.outcrop_ids:continue
            path=assets/'templates'/(entry['path']+'.nbt')
            if hashlib.sha256(path.read_bytes()).hexdigest()!=entry['gzip_sha256']:raise ValueError('Modified crust asset')
            t=self.alloc(0x100);self.templates[entry['index']]=t;self.template_ids[t]=entry['index'];self.template_inputs.append({'id':entry['index'],'path':entry['path'],'sha256':entry['gzip_sha256']})
        server=self.alloc(64);vt=self.alloc(0x70);self.w(server,'Q',vt);manager=self.alloc(32)
        self.w(vt+0x60,'Q',self.callback(P,[P],lambda server:manager))
        self.patch(0x403ed0,P,[P],lambda world:server)
        self.patch(0x6bd0c0,P,[P,I],self.template)
        if controlled_templates:
            self.patch(0x6c8640,I,[P,P,P,P,P,I],self.place_template)
            self.patch(0x1c8940,None,[P,P,P,P,P,P],self.postprocess)

    def template(self,manager,id):self.template_events.append(['template',id]);return self.templates[id]

    def place_template(self,template,world,pos,processor,settings,flags):
        if self.read(settings+0x48,'q')!=-1 or self.read(settings+0x40,'Q')!=0:raise ValueError('Unexpected template random defaults')
        random=self.read(processor+0x10,'Q');owned=self.read(processor+0x18,'B')
        draws=[C.c_int64(self.call(0x5970e0,U,[P],[random])).value for _ in range(self.geometry_draws)]
        self.template_events.append(['place',self.template_ids[template],*self.xyz(pos),self.read(self.read(settings+0x10,'Q')+0x28,'i'),self.read(settings+0x3c,'f'),flags,draws])
        if owned:self.host.free(random)
        self.host.free(processor);return 1

    def postprocess(self,feature,world,pos,random,template,settings):
        draws=[C.c_int64(self.call(0x5970e0,U,[P],[random])).value for _ in range(self.post_draws)]
        self.template_events.append(['post',self.template_ids[template],*self.xyz(pos),self.read(self.read(settings+0x10,'Q')+0x28,'i'),draws])

    def initial(self,x,y,z):
        if not self.profile.startswith('crust_'):return super().initial(x,y,z)
        if self.profile=='crust_air' or y<0 or y>=256:return 0
        floor=52 if self.profile=='crust_shallow' else 30 if self.profile=='crust_deep' else 48
        if self.profile=='crust_steps':floor+=(3*x+7*z)%7-3
        if y<floor:return 1
        if y==floor:return 259 if self.profile=='crust_live' else 259|(8<<16) if self.profile=='crust_dead' else 1
        return 9 if y<64 else 0

    def crust(self,seed,position,profile,rotation_order=(0,1,2,3),geometry_draws=1,post_draws=1,client=False):
        pos=self.begin_coral(profile,position,'all','all',self.HORIZONTAL)
        self.geometry_draws=geometry_draws;self.post_draws=post_draws;self.template_events=[]
        self.w(self.world+0x248,'B',int(client));self.w(self.feature,'Q',self.treevt);self.w(self.feature+8,'B',0)
        for i,ordinal in enumerate(rotation_order):self.game.w(0x142f880+8*i,'Q',self.rotations[ordinal])
        self.call(0x596c90,None,[P,U],[self.random,seed&0xffffffffffffffff])
        result=bool(self.call(0x1c7650,I,[P,P,P,P],[self.feature,self.world,self.random,pos])&255)
        return {'seed':seed,'position':position,'profile':profile,'rotation_order':list(rotation_order),'geometry_draws':geometry_draws,'post_draws':post_draws,'client':client,'result':result,'events':self.template_events,**self.coral_result(),'random_after':C.c_int64(self.call(0x5970e0,U,[P],[self.random])).value}
