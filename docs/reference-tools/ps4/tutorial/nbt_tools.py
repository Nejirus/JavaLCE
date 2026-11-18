import os
from io import BytesIO
import struct,json
class Tag:
 def __init__(self,kind,value):self.kind,self.value=kind,value
 def __repr__(self):return repr(self.value)
class Nbt:
 def __init__(self,data):self.f=BytesIO(data)
 def read(self,fmt):return struct.unpack('>'+fmt,self.f.read(struct.calcsize('>'+fmt)))
 def string(self):return self.f.read(self.read('H')[0]).decode('utf-8',errors='replace')
 def tag(self,t):
  if t in [1,2,3,4,5,6]:return Tag(t,self.read({1:'b',2:'h',3:'i',4:'q',5:'f',6:'d'}[t])[0])
  if t==7:return Tag(t,self.f.read(self.read('i')[0]))
  if t==8:return Tag(t,self.string())
  if t==9:
   kind,n=self.read('bi');return Tag(t,(kind,[self.tag(kind) for _ in range(n)]))
  if t==10:
   d={}
   while True:
    kind=self.read('b')[0]
    if kind==0:break
    name=self.string();d[name]=self.tag(kind)
   return Tag(t,d)
  if t in [11,12]:
   n=self.read('i')[0];return Tag(t,list(self.read(('i' if t==11 else 'q')*n)))
  raise ValueError(('NBT tag',t,self.f.tell()))
 def root(self):
  kind=self.read('b')[0];name=self.string();tag=self.tag(kind);return name,tag

def plain(t,arrays=False):
 if t.kind==10:return {k:plain(v,arrays) for k,v in t.value.items()}
 if t.kind==9:return [plain(v,arrays) for v in t.value[1]]
 if t.kind in [7,11,12]:return list(t.value) if arrays else f'<array {len(t.value)}>'
 return t.value

def unrle(data):
 out=bytearray();i=0
 while i<len(data):
  x=data[i];i+=1
  if x==255:
   n=data[i];i+=1
   if n<3:out.extend(b'\xff'*(n+1))
   else:out.extend(bytes([data[i]])*(n+1));i+=1
  else:out.append(x)
 return bytes(out)

def encode(tag,name=''):
 out=BytesIO()
 def write(fmt,*v):out.write(struct.pack('>'+fmt,*v))
 def text(v):
  b=v.encode('utf-8');write('H',len(b));out.write(b)
 def payload(t):
  k,v=t.kind,t.value
  if k in [1,2,3,4,5,6]:write({1:'b',2:'h',3:'i',4:'q',5:'f',6:'d'}[k],v)
  elif k==7:write('i',len(v));out.write(v)
  elif k==8:text(v)
  elif k==9:
   et,items=v;write('bi',et,len(items))
   for item in items:payload(item)
  elif k==10:
   for name,item in v.items():write('b',item.kind);text(name);payload(item)
   write('b',0)
  elif k in [11,12]:
   write('i',len(v))
   if v:write(('i' if k==11 else 'q')*len(v),*v)
  else:raise ValueError(('NBT write',k))
 write('b',tag.kind);text(name);payload(tag);return out.getvalue()
