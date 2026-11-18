#!/usr/bin/env python3
"""Run isolated PS4 biome selection and finite spawn-walk instructions.

Requires unicorn. Inputs are controlled biome rectangles and ground predicates,
not claims about final PS4 terrain. Random::nextInt runs from the executable too.
Usage: python export_spawn.py --eboot /path/eboot.bin --output vectors.json
"""
import argparse
import hashlib
import json
import struct
from pathlib import Path
from unicorn import Uc,UC_ARCH_X86,UC_MODE_64,UC_HOOK_CODE
from unicorn import x86_const as r
from export_maps import SELF_SHA256

def main():
 parser=argparse.ArgumentParser(description=__doc__);parser.add_argument('--eboot',type=Path,required=True);parser.add_argument('--output',type=Path,required=True);args=parser.parse_args()
 data=args.eboot.read_bytes()
 if hashlib.sha256(data).hexdigest()!=SELF_SHA256:raise ValueError('Unexpected executable')
 elf=data.index(b'\x7fELF',0,512);h=struct.unpack_from('<16sHHIQQQIHHHHHH',data,elf)
 ph=[struct.unpack_from('<IIQQQQQQ',data,elf+h[5]+i*h[9]) for i in range(h[10])]
 uc=Uc(UC_ARCH_X86,UC_MODE_64);uc.mem_map(0,0x1500000);uc.mem_map(0x50000000,0x400000)
 for i in range((elf-32)//32):
  flags,offset,size,_=struct.unpack_from('<QQQQ',data,32+i*32)
  if flags&0x800:
   seg=ph[flags>>20]
   if seg[0] in (1,0x61000010):uc.mem_write(seg[3],data[offset:offset+size])
 frame=0x50300000;stop=0x503ff000;source=0x50002000;layer=0x50003000;vt=0x50004000;vec=0x50005000;registry=0x50006000;array=0x50010000;random=0x50007000;level=0x50008000;dim=0x50009000;dvt=0x5000a000
 state={'heap':0x50100000,'acceptAfter':0,'visited':[]}
 def write(a,fmt,v):uc.mem_write(a,struct.pack(fmt,v))
 def ret(value=0):
  uc.reg_write(r.UC_X86_REG_RAX,value&0xffffffffffffffff);sp=uc.reg_read(r.UC_X86_REG_RSP);pc=struct.unpack('<Q',uc.mem_read(sp,8))[0];uc.reg_write(r.UC_X86_REG_RSP,sp+8);uc.reg_write(r.UC_X86_REG_RIP,pc)
 def signed(value):value&=0xffffffff;return value-0x100000000 if value&0x80000000 else value
 def hook(machine,address,size,unused):
  if address in (0x3796f0,0xaff530,0xe6cfb0):ret()
  elif address==0xe6cfd0:
   amount=machine.reg_read(r.UC_X86_REG_RDI);p=state['heap'];state['heap']+=((amount+15)//16)*16;ret(p)
  elif address==0x50001000:ret(array)
  elif address==0x50001010:
   point=[signed(machine.reg_read(r.UC_X86_REG_RSI)),signed(machine.reg_read(r.UC_X86_REG_RDX))]
   valid=len(state['visited'])>=state['acceptAfter'];state['visited'].append(point);ret(int(valid))
 uc.hook_add(UC_HOOK_CODE,hook)
 write(source+8,'<Q',layer);write(layer,'<Q',vt);write(vt+0x20,'<Q',0x50001000)
 write(0x134bbf0,'<Q',registry)
 for i in range(257):write(registry+i*8,'<Q',0x500b0000+i*16)
 allowed=[4,5,1,19,18,21,22,40,41];values=0x5000c000
 for i,value in enumerate(allowed):write(values+i*8,'<Q',0x500b0000+value*16)
 write(vec+8,'<Q',values);write(vec+16,'<Q',values+8*len(allowed))
 write(level+0x1c8,'<Q',dim);write(dim,'<Q',dvt);write(dvt+0x40,'<Q',0x50001010)
 def execute(pc,end=stop):uc.emu_start(pc,end,count=5000000)
 def prepare_return():uc.reg_write(r.UC_X86_REG_RSP,frame-0x1000);write(frame-0x1000,'<Q',stop)
 output=[]
 for index,biome in enumerate(allowed+[0,-1,-2]):
  for variant in range(2):
   seed=[0,1,-1,123456789,-987654321,82958734][(index+variant)%6];width=[864,1024,3072,5120][(index+variant)%4]
   accept=[0,7,43,1001][(index+variant)%4];state.update(heap=0x50100000,acceptAfter=accept,visited=[])
   ids=[biome]*(129*129)
   if biome==-1:ids=[1 if (i%129+3*(i//129))%7==0 else 0 for i in range(129*129)]
   if biome==-2:ids=[0]*(129*129);ids[-1]=4
   uc.mem_write(array,struct.pack('<'+str(len(ids))+'i',*ids));write(random,'<Q',(seed^0x5deece66d)&0xffffffffffff)
   prepare_return();uc.reg_write(r.UC_X86_REG_RDI,source);uc.reg_write(r.UC_X86_REG_RSI,0);uc.reg_write(r.UC_X86_REG_RDX,0);uc.reg_write(r.UC_X86_REG_RCX,256);uc.reg_write(r.UC_X86_REG_R8,vec);uc.reg_write(r.UC_X86_REG_R9,random)
   execute(0x8e130)
   ptr=uc.reg_read(r.UC_X86_REG_RAX);x,z=(struct.unpack('<iii',uc.mem_read(ptr,12))[::2] if ptr else (8,8));rng=bytes(uc.mem_read(random,8))
   uc.reg_write(r.UC_X86_REG_RBP,frame);uc.reg_write(r.UC_X86_REG_RSP,frame-0x1000)
   write(frame-0x7c,'<i',width//16);write(frame-0x88,'<Q',level);uc.mem_write(frame-0x48,rng)
   uc.reg_write(r.UC_X86_REG_R13,level);uc.reg_write(r.UC_X86_REG_R12,x&0xffffffff);uc.reg_write(r.UC_X86_REG_R15,z&0xffffffff);uc.reg_write(r.UC_X86_REG_RBX,width//16)
   execute(0xcf9cba,0xcf9db1)
   sx=signed(uc.reg_read(r.UC_X86_REG_R12));sz=signed(uc.reg_read(r.UC_X86_REG_RBX))
   prepare_return();uc.reg_write(r.UC_X86_REG_RDI,frame-0x48);uc.reg_write(r.UC_X86_REG_RSI,1000003);execute(0x593950)
   output.append({'seed':seed,'width':width,'biomePattern':biome,'acceptAfter':accept,'x':sx,'z':sz,'nextInt':uc.reg_read(r.UC_X86_REG_RAX),'visited':state['visited']})
 args.output.parent.mkdir(parents=True,exist_ok=True);args.output.write_text(json.dumps({'executableSha256':SELF_SHA256,'cases':output},indent=2)+'\n');print('Captured',len(output),'PS4 spawn cases')

if __name__=='__main__':main()
