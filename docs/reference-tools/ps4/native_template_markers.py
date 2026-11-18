"""Original template marker collector and native hash/list container operations.

Original unchanged NBT order/positions, canonical block identities and metadata
strings are supplied. Block-state metadata/NBT parsing is outside the collector
comparison: the kernel only tests structure-block identity and NBT presence.
"""
from native_coral_decoration import CoralDecorationOracle,C,P,U,I


class TemplateMarkerOracle(CoralDecorationOracle):
    def __init__(self,executable,library):
        super().__init__(executable,library,ordered_marker_input=False)
        self.rotations=[]
        for i in range(4):
            r=self.alloc(64);self.w(r+0x28,'i',i);self.rotations.append(r);self.game.w(0x142f858+8*i,'Q',r)
        for i,address in enumerate([0x14194d0,0x14194d8,0x14194e0]):
            mirror=self.alloc(64);self.w(mirror+0x28,'i',i);self.game.w(address,'Q',mirror)
        self.settings=self.alloc(176);self.call(0x6c1d60,None,[P],[self.settings])
        self.nbt_strings={};self.templates={}
        self.patch(0x1af2a0,P,[P,P,P],self.nbt_string)

    def text(self,obj):
        length=self.read(obj+24,'Q');pointer=self.read(obj+8,'Q') if self.read(obj+32,'Q')>=8 else obj+8
        return C.string_at(pointer,2*length).decode('utf-16le')

    def nbt_string(self,out,nbt,key):
        if self.text(key)!='metadata':raise ValueError('Unexpected marker NBT key')
        C.memset(out,0,40);self.w(out+32,'Q',7);value=self.nbt_strings[nbt];buffer=C.create_string_buffer(value.encode('utf-16le')+b'\0\0');self.keep.append(buffer)
        self.call(0x67840,None,[P,P,U],[out,C.addressof(buffer),len(value)]);return out

    def template(self,id,blocks):
        template=self.alloc(0x100);pointers=self.alloc(len(blocks)*8);self.templates[id]=template
        for i,row in enumerate(blocks):
            x,y,z,tile,metadata=row;info=self.alloc(40)
            for axis,value in enumerate([x,y,z]):self.w(info+4*axis,'i',value)
            self.w(info+0x10,'Q',self.state(tile<<4));self.w(info+0x18,'Q',self.state(0));self.w(pointers+8*i,'Q',info)
            if metadata is not None:
                nbt=self.alloc(8);self.nbt_strings[nbt]=metadata;self.w(info+0x20,'Q',nbt)
        self.w(template+0x50,'Q',pointers);self.w(template+0x58,'Q',pointers+len(blocks)*8)

    def collect(self,id,origin,rotation):
        out=self.alloc(80);pos=self.alloc(12)
        for i,value in enumerate(origin):self.w(pos+4*i,'i',value)
        self.call(0x6c2ec0,None,[P,P],[self.settings,self.rotations[rotation]])
        self.call(0x6c50e0,P,[P,P,P,P],[out,self.templates[id],pos,self.settings])
        head=self.read(out+8,'Q');node=self.read(head,'Q');markers=[]
        while node!=head:
            markers.append([*self.xyz(node+16),self.text(node+32)]);node=self.read(node,'Q')
        bucket_count=self.read(out+0x40,'Q');node=self.read(head,'Q')
        while node!=head:
            following=self.read(node,'Q')
            if self.read(node+64,'Q')>7:self.host.free(self.read(node+40,'Q'))
            self.host.free(node);node=following
        buckets=self.read(out+0x20,'Q')
        if buckets:self.host.free(buckets)
        self.host.free(head)
        return {'template':id,'origin':origin,'rotation':rotation,'bucket_count':bucket_count,'markers':markers}
