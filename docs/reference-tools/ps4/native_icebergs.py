"""Original complete iceberg generator, including smoothing and carving."""
from native_ice import IceOracle,C,P,U,I


class IcebergOracle(IceOracle):
    def __init__(self,executable,library):
        super().__init__(executable,library)
        for nid in ['2WE3BTYVwKM','H8ya2H00jbI']:self.game.bind(nid,P(self.lib.base+self.lib.exports[nid]))
        self.call(0x372190,None,[P],[self.feature])
        self.constructor_fields=self.fields()
    def fields(self):
        return {'snow':bool(self.read(self.feature+9,'B')),'ellipse':bool(self.read(self.feature+10,'B')),'angle_bits':f'{self.read(self.feature+0x10,"Q"):016x}',
                'major':self.read(self.feature+0x18,'i'),'minor':self.read(self.feature+0x1c,'i'),'inset':self.read(self.feature+0x20,'i'),
                'state':self.unpacked(self.read(self.feature+0x28,'Q')) if self.read(self.feature+0x28,'Q') else None}
    def initial(self,x,y,z):
        if self.profile=='empty':return 0
        if self.profile=='stone':return 1
        if self.profile=='mixed':return [0,1,3,2,8,9,79,78,80,174,273][(x*7+y*3+z*11)%11]
        if self.profile=='caps':
            if y==self.sea_level+1:return 78
            if y==self.sea_level:return 80 if (x+z)%3==0 else 174
        return 9 if y<=self.sea_level else 0
    def iceberg(self,seed,position,profile='ocean',sea_level=63,reuse=False):
        if not reuse:self.call(0x372190,None,[P],[self.feature])
        before=self.fields();self.sea_level=sea_level;self.kind='iceberg';self.reset_world(profile,256,position);self.fluid_queries=[];self.w(self.world+8,'i',sea_level)
        pos=self.alloc(12)
        for i,value in enumerate(position):self.w(pos+4*i,'i',value)
        self.call(0x596c90,None,[P,U],[self.random,seed&0xffffffffffffffff])
        result=bool(self.call(0x3721e0,I,[P,P,P,P],[self.feature,self.world,self.random,pos])&255)
        return {'seed':seed,'position':position,'profile':profile,'sea_level':sea_level,'reuse':reuse,'before':before,'after':self.fields(),'result':result,
                **self.world_result(),'fluid_queries':self.fluid_queries,'light_queries':self.light_queries,'random_after':C.c_int64(self.call(0x5970e0,U,[P],[self.random])).value}
