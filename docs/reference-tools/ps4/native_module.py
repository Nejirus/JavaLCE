"""Isolated pure-routine analysis. Never invokes module startup or game entrypoints."""
import ctypes as C,mmap,struct,os,sys
from pathlib import Path
U=C.c_uint64;I=C.c_int;F=C.c_float;D=C.c_double;P=C.c_void_p
class Module:
 def __init__(self,path):
  raw=Path(path).read_bytes();elf=raw.index(b'\x7fELF',0,512);h=struct.unpack_from('<16sHHIQQQIHHHHHH',raw,elf);self.ph=[struct.unpack_from('<IIQQQQQQ',raw,elf+h[5]+i*h[9]) for i in range(h[10])]
  length=max(s[2]+s[5] for s in self.ph);self.file=bytearray(length);self.file[:64]=raw[elf:elf+64];self.file[h[5]:h[5]+h[9]*h[10]]=raw[elf+h[5]:elf+h[5]+h[9]*h[10]]
  for i in range((elf-32)//32):
   flags,off,size,_=struct.unpack_from('<QQQQ',raw,32+i*32)
   if flags&0x800:
    seg=self.ph[flags>>20];self.file[seg[2]:seg[2]+size]=raw[off:off+size]
  mapped=[s for s in self.ph if s[0] in (1,0x61000010)];size=max(s[3]+s[6] for s in mapped);size=(size+4095)&~4095
  self.mem=mmap.mmap(-1,size,prot=mmap.PROT_READ|mmap.PROT_WRITE|mmap.PROT_EXEC);self.base=C.addressof(C.c_char.from_buffer(self.mem));self.callbacks=[];self.exports={};self.imports={};self.import_addends={}
  for seg in mapped:C.memmove(self.base+seg[3],bytes(self.file[seg[2]:seg[2]+seg[5]]),seg[5])
  meta=next(p for p in self.ph if p[0]==0x61000000);dyn=next(p for p in self.ph if p[0]==2);tags=dict(struct.unpack_from('<QQ',self.file,dyn[2]+i) for i in range(0,dyn[5],16));self.tags=tags
  symbase=meta[2]+tags[0x61000039];strbase=meta[2]+tags[0x61000035];symbols=[]
  for i in range(tags[0x6100003f]//24):
   name,info,other,section,val,size=struct.unpack_from('<IBBHQQ',self.file,symbase+i*24);p=strbase+name;s=bytes(self.file[p:self.file.index(0,p)]).decode(errors='replace');symbols.append((s,val,section))
   if section and s:self.exports[s.split('#')[0]]=val
  self.symbols=symbols;unhandled=[]
  for start,size in [(0x61000029,0x6100002d),(0x6100002f,0x61000031)]:
   if not tags.get(size):continue
   for i in range(0,tags[size],24):
    target,info,addend=struct.unpack_from('<QQq',self.file,meta[2]+tags[start]+i);kind=info&0xffffffff;sym=info>>32
    if kind==8:self.w(target,'Q',self.base+addend)
    elif kind in [1,6,7]:
     name,value,section=symbols[sym]
     if section:self.w(target,'Q',self.base+value+addend)
     else:self.imports.setdefault(name.split('#')[0],[]).append(target);self.import_addends[target]=addend;self.w(target,'Q',0)
    elif kind not in [0]:unhandled.append(kind)
  if unhandled:print('Unmapped relocation kinds:',sorted(set(unhandled)),flush=True)
 def w(self,p,fmt,value):C.memmove(self.base+p,struct.pack('<'+fmt,value),struct.calcsize('<'+fmt))
 def read(self,p,fmt):return struct.unpack('<'+fmt,C.string_at(self.base+p,struct.calcsize('<'+fmt)))[0]
 def bind(self,nid,fn):
  for p in self.imports.get(nid,[]):self.w(p,'Q',C.cast(fn,P).value+self.import_addends.get(p,0))
 def trap_unbound(self):
  for nid,targets in self.imports.items():
   if all(self.read(p,'Q') for p in targets):continue
   def unknown(a=0,b=0,c=0,d=0,e=0,f=0,nid=nid):print('UNBOUND IMPORT',nid,flush=True);os._exit(91)
   cb=C.CFUNCTYPE(U,U,U,U,U,U,U)(unknown);self.callbacks.append(cb);self.bind(nid,cb)
 def patch(self,addr,fn):
  self.callbacks.append(fn);C.memmove(self.base+addr,b'\x48\xb8'+struct.pack('<Q',C.cast(fn,P).value)+b'\xff\xe0',12)
 def function(self,addr,ret,args):return C.CFUNCTYPE(ret,*args)(self.base+addr)
