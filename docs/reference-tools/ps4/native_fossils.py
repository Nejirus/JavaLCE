"""Original fossil dispatcher and chunk RNG; template placement is controlled.

The original template size/position helpers and settings constructor run.
Rotation enum storage/order and height/geometry results are explicit inputs.
"""
from native_tree_selection import TreeSelectionOracle,C,P,U,I
from nbt_input import read_nbt
from pathlib import Path
import hashlib,json


class FossilOracle(TreeSelectionOracle):
    def __init__(self,executable,library,assets,controlled_geometry=True):
        super().__init__(executable,library)
        self.assets=assets;manifest=json.loads((assets/'templates.json').read_text());self.templates={};self.template_ids={};self.template_inputs=[]
        for entry in manifest:
            if not entry['path'].startswith('fossils/'):continue
            path=assets/'templates'/(entry['path']+'.nbt')
            if hashlib.sha256(path.read_bytes()).hexdigest()!=entry['gzip_sha256']:raise ValueError('Modified original fossil template: '+str(path))
            data=read_nbt(path);template=self.alloc(0x100);id=entry['index'];self.templates[id]=template;self.template_ids[template]=id
            for i,value in enumerate(data['size']):self.w(template+0x88+4*i,'i',value)
            self.template_inputs.append({'id':id,'path':entry['path'],'size':data['size'],'sha256':entry['gzip_sha256']})
        self.rotations=[]
        for i in range(4):
            rotation=self.alloc(64);self.w(rotation+0x28,'i',i);self.rotations.append(rotation);self.game.w(0x142f858+i*8,'Q',rotation)
        for address in [0x14194d0,0x14194d8,0x14194e0]:self.game.w(address,'Q',self.alloc(64))
        info=self.alloc(64);ivt=self.alloc(64);self.w(self.world+0x218,'Q',info);self.w(info,'Q',ivt)
        self.w(ivt+0x28,'Q',self.callback(U,[P],lambda obj:self.world_seed&0xffffffffffffffff))
        self.chunk=self.alloc(0x300);chunk_vt=self.alloc(0x220);self.w(self.chunk,'Q',chunk_vt);self.w(self.chunk+0x1b0,'Q',self.world)
        self.w(chunk_vt+0x200,'Q',self.callback(P,[P,I],self.chunk_random))
        provider=self.alloc(64);provider_vt=self.alloc(0x60);self.w(self.world+0x1f8,'Q',provider);self.w(provider,'Q',provider_vt)
        self.w(provider_vt+0x58,'Q',self.callback(P,[P,I,I],self.chunk_at))
        self.patch(0xc34710,P,[],lambda:self.world)
        self.patch(0xc80600,P,[P],lambda game:self.world)
        self.patch(0x6bd1e0,P,[P,I],self.template)
        self.patch(0x3f2020,I,[P,I,I],self.height_xz)
        if controlled_geometry:self.patch(0x6c92f0,None,[P,P,P,P,I],self.place_template)

    def chunk_at(self,provider,x,z):
        self.w(self.chunk+0x1f0,'i',x);self.w(self.chunk+0x1f4,'i',z);self.events.append(['chunk',x,z]);return self.chunk

    def chunk_random(self,chunk,salt):
        self.events.append(['chunk_rng',salt]);self.last_random=self.call(0x40fd80,P,[P,U],[chunk,salt&0xffffffffffffffff]);return self.last_random

    def template(self,manager,id):self.events.append(['template',id]);return self.templates[id]

    def height_xz(self,world,x,z):
        value={'flat':64,'zero':0,'high':300}.get(self.height_profile)
        if value is None:value=32+((x*3+z)&31)
        self.events.append(['height',x,z,value]);return value

    def place_template(self,template,world,pos,settings,flags):
        bbox=self.read(settings+0x30,'Q');random=self.read(settings+0x40,'Q')
        draws=[C.c_int64(self.call(0x5970e0,U,[P],[random])).value for _ in range(self.geometry_draws)]
        self.events.append(['place',self.template_ids[template],*self.xyz(pos),self.read(self.read(settings+0x10,'Q')+0x28,'i'),
                            *[self.read(bbox+4*i,'i') for i in range(6)],self.read(settings+0x3c,'f'),flags,draws])

    def fossil(self,world_seed,population_seed,position,height_profile,geometry_draws,rotation_order):
        self.events=[];self.world_seed=world_seed;self.height_profile=height_profile;self.geometry_draws=geometry_draws
        for i,ordinal in enumerate(rotation_order):self.game.w(0x142f880+i*8,'Q',self.rotations[ordinal])
        pos=self.alloc(12)
        for i,value in enumerate(position):self.w(pos+4*i,'i',value)
        self.call(0x596c90,None,[P,U],[self.random,population_seed&0xffffffffffffffff])
        result=bool(self.call(0x2f6ef0,I,[P,P,P,P],[self.feature,self.world,self.random,pos])&255)
        after=C.c_int64(self.call(0x5970e0,U,[P],[self.last_random])).value;self.host.free(self.last_random)
        return {'world_seed':world_seed,'population_seed':population_seed,'position':position,'height_profile':height_profile,'geometry_draws':geometry_draws,'rotation_order':rotation_order,
                'result':result,'events':self.events,'chunk_random_after':after,'population_random_after':C.c_int64(self.call(0x5970e0,U,[P],[self.random])).value}

    def random_case(self,world_seed,x,z,salt):
        self.world_seed=world_seed;self.w(self.chunk+0x1f0,'i',x);self.w(self.chunk+0x1f4,'i',z)
        random=self.call(0x40fd80,P,[P,U],[self.chunk,salt&0xffffffffffffffff]);values=[C.c_int64(self.call(0x5970e0,U,[P],[random])).value for _ in range(4)];self.host.free(random)
        return {'world_seed':world_seed,'chunk_x':x,'chunk_z':z,'salt':salt,'values':values}
