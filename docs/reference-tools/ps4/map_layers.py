"""Capture mcps4's map/decorations layer calls; requires Python 3 and Unicorn."""
from pathlib import Path
import argparse, hashlib, json, struct
from unicorn import Uc, UC_ARCH_X86, UC_MODE_64, UC_HOOK_CODE
from unicorn import x86_const as r
p=argparse.ArgumentParser();p.add_argument('elf',type=Path);p.add_argument('--original-executable',type=Path,required=True);p.add_argument('--output',type=Path,required=True);a=p.parse_args()
original_sha=hashlib.sha256(a.original_executable.read_bytes()).hexdigest()
assert original_sha=='c6089834fe1372854eed8f9acf2cb307f7009aaeb32ee303ce5bf6a1160786c8'
b=a.elf.read_bytes();assert b[:4]==b'\x7fELF';ph=struct.unpack_from('<Q',b,32)[0];count=struct.unpack_from('<H',b,56)[0]
u=Uc(UC_ARCH_X86,UC_MODE_64);u.mem_map(0,0x1600000);u.mem_map(0x50000000,0x200000)
for i in range(count):
 s=struct.unpack_from('<IIQQQQQQ',b,ph+i*56)
 if s[0]==1:u.mem_write(s[3],b[s[2]:s[2]+s[5]])
base=0x50100000;u.reg_write(r.UC_X86_REG_RBP,base);u.reg_write(r.UC_X86_REG_RSP,base-0x1000)
vertices=[];transforms=[]
def f(reg):return struct.unpack('<f',u.reg_read(reg).to_bytes(16,'little')[:4])[0]
def ret():
 sp=u.reg_read(r.UC_X86_REG_RSP);pc=struct.unpack('<Q',u.mem_read(sp,8))[0];u.reg_write(r.UC_X86_REG_RSP,sp+8);u.reg_write(r.UC_X86_REG_RIP,pc)
def hook(m,pc,size,data):
 if pc==0xb27580:vertices.append([f(r.UC_X86_REG_XMM0+i) for i in range(5)]);ret()
 elif pc in (0xbe6fb0,0xbe6ff0):transforms.append({'operation':'translate' if pc==0xbe6fb0 else 'scale','values':[f(r.UC_X86_REG_XMM0+i) for i in range(3)]});ret()
u.hook_add(UC_HOOK_CODE,hook)
u.emu_start(0xc6fc48,0xc6fce7,count=200);image=list(vertices)
u.emu_start(0xc6fdf1,0xc6fdf9,count=10);u.emu_start(0xc6fe1a,0xc6fe22,count=10)
transforms.clear();u.emu_start(0xc700a7,0xc700c0,count=30)
scale=transforms[0]['values'];index_step=struct.unpack('<f',u.mem_read(0xf0698c,4))[0]
icons=[]
for i in range(64):
 transforms.clear()
 # Capture just the Z argument: the preceding x/y calculation is independent.
 u.emu_start(0xc70058,0xc70060,count=10)
 u.emu_start(0xc70068,0xc70075,count=30)
 layer=transforms[0]['values'][2]
 u.emu_start(0xc70243,0xc70269,count=30)
 next_layer=struct.unpack('<f',u.mem_read(base-0x104,4))[0]
 icons.append({'index':i,'translation_z':layer,'next_layer':next_layer})
out={'source':'Nejirus/mcps4','commit':'37e258085773f11599cf0ffe29119926769554b7','executable_sha256':original_sha,'function':'0xc6fba0','image_range':['0xc6fc48','0xc6fce7'],'icon_ranges':[['0xc70058','0xc70060'],['0xc70068','0xc70075'],['0xc700a7','0xc700c0'],['0xc70243','0xc70269']],'image':image,'icon_scale':scale,'index_vertex_step':index_step,'icons':icons}
a.output.write_text(json.dumps(out,indent=2)+'\n');print(json.dumps({'icons':len(icons),'image_depth':image[0][2],'first_icon':icons[0],'second_icon_layer':icons[1]['translation_z']},indent=2))
