# Requires Python 3 and unicorn. Executes only bounded, isolated instruction ranges.
from pathlib import Path
import sys,struct,json,hashlib,random
import argparse
parser=argparse.ArgumentParser();parser.add_argument("elf",type=Path);parser.add_argument("--original-executable",type=Path,required=True);parser.add_argument("--output",type=Path,required=True);args=parser.parse_args()
class Image:
 def __init__(self):
  self.data=args.elf.read_bytes();assert self.data[:4]==b"\x7fELF"
  off=struct.unpack_from("<Q",self.data,32)[0];count=struct.unpack_from("<H",self.data,56)[0]
  self.segments=[struct.unpack_from("<IIQQQQQQ",self.data,off+i*56) for i in range(count)]
 def read(self,address,size):
  for s in self.segments:
   if s[0]==1 and s[3]<=address and address+size<=s[3]+s[5]:return self.data[s[2]+address-s[3]:s[2]+address-s[3]+size]
  raise ValueError(hex(address))
from unicorn import Uc,UC_ARCH_X86,UC_MODE_64,UC_HOOK_CODE
from unicorn import x86_const as r
im=Image();u=Uc(UC_ARCH_X86,UC_MODE_64);u.mem_map(0,0x1600000);u.mem_map(0x50000000,0x400000)
for s in im.segments:
 if s[0]==1:u.mem_write(s[3],im.read(s[3],s[5]))
def w(p,fmt,v):u.mem_write(p,struct.pack('<'+fmt,v))
rbp=0x50301000;server=0x50001000;vector=0x50002000;handle=0x50003000;new=0x50004000
u.reg_write(r.UC_X86_REG_RBP,rbp);u.reg_write(r.UC_X86_REG_RSP,rbp-0x1000);u.reg_write(r.UC_X86_REG_R13,server);u.reg_write(r.UC_X86_REG_R15,handle);w(handle,'Q',new)
rng=random.Random(2317);inputs=[[],list(range(16)),[0,2,3],[1,2,3],[0]*3]+[rng.sample(range(16),rng.randrange(17)) for _ in range(1024)]
slots=[]
for occupied in inputs:
 u.mem_write(rbp-0x80,b'\0'*16);w(server+8,'Q',vector);w(server+16,'Q',vector+len(occupied)*24)
 for i,value in enumerate(occupied):
  player=0x50010000+i*0x1000;w(vector+i*24,'Q',player);w(player+0xab4,'i',value)
 u.reg_write(r.UC_X86_REG_RSI,0);u.emu_start(0xcc3a78,0xcc3b85,count=1000)
 slots.append({'occupied':occupied,'result':struct.unpack('<i',u.mem_read(new+0xab4,4))[0]})
colors=[]
for index in range(-4,21):
 u.reg_write(r.UC_X86_REG_RDI,index&0xffffffff);u.emu_start(0xcd1a00,0xcd1a17,count=20)
 colors.append({'index':index,'argb':u.reg_read(r.UC_X86_REG_RAX)&0xffffffff})
vertices=[];draws=[];active=[]
def f(reg):return struct.unpack('<f',u.reg_read(reg).to_bytes(16,'little')[:4])[0]
def ret():
 sp=u.reg_read(r.UC_X86_REG_RSP);pc=struct.unpack('<Q',u.mem_read(sp,8))[0];u.reg_write(r.UC_X86_REG_RSP,sp+8);u.reg_write(r.UC_X86_REG_RIP,pc)
def hook(m,pc,size,data):
 if pc==0xb79870:vertices.append([f(r.UC_X86_REG_XMM0),f(r.UC_X86_REG_XMM1),f(r.UC_X86_REG_XMM2)]);ret()
 elif pc in [0xb276d0,0xb7cb50]:ret()
u.hook_add(UC_HOOK_CODE,hook)
for width in [0,1,2,3,15,16,31,32,99,100,101,192]:
 for baseline in [0,-10]:
  w(rbp-0x88,'q',width//2);w(rbp-0x78,'q',baseline);w(rbp-0x5c,'I',0xff33cc33);w(rbp-0x94,'I',255)
  vertices.clear();u.emu_start(0xc0a083,0xc0a121,count=100);quad=list(vertices)
  vertices.clear();u.emu_start(0xc0a18d,0xc0a214,count=100)
  draws.append({'width':width,'baseline':baseline,'background':quad,'border':list(vertices)})
out={'source':'Nejirus/mcps4','commit':'37e258085773f11599cf0ffe29119926769554b7','executable_sha256':hashlib.sha256(args.original_executable.read_bytes()).hexdigest(),'allocation_range':['0xcc3a78','0xcc3b85'],'palette_lookup':'0xcd1a00','nameplate_rectangles':['0xc0a083','0xc0a18d'],'allocation':slots,'colors':colors,'rectangles':draws}
p=args.output;p.write_text(json.dumps(out,separators=(',',':'))+'\n')
print(json.dumps({'allocation_vectors':len(slots),'color_vectors':len(colors),'nameplate_vectors':len(draws),'sample_border':draws[9]},indent=2))
