#!/usr/bin/env python3
"""Capture pure map/bounds arithmetic from the supplied mcps4 eboot.

Requires Python 3 and unicorn. No executable or media is bundled.
Run: python export_maps.py --eboot /path/to/eboot.bin --output vectors.json
The only substituted math import is the verified libc round(double) import.
No game entry point, platform APIs, save files or renderer are executed.
"""
import argparse
import hashlib
import json
import math
import struct
from pathlib import Path
from unicorn import Uc, UC_ARCH_X86, UC_MODE_64, UC_HOOK_CODE
from unicorn.x86_const import (
 UC_X86_REG_RAX, UC_X86_REG_RBP, UC_X86_REG_RSP, UC_X86_REG_RIP,
 UC_X86_REG_XMM0, UC_X86_REG_RDI, UC_X86_REG_RSI, UC_X86_REG_RDX,
 UC_X86_REG_RCX,
)

SELF_SHA256="c6089834fe1372854eed8f9acf2cb307f7009aaeb32ee303ce5bf6a1160786c8"

def main():
 parser=argparse.ArgumentParser(description=__doc__)
 parser.add_argument('--eboot',type=Path,required=True)
 parser.add_argument('--output',type=Path,required=True)
 args=parser.parse_args()
 data=args.eboot.read_bytes()
 if hashlib.sha256(data).hexdigest()!=SELF_SHA256:raise ValueError('Unexpected PS4 executable revision')
 elf=data.index(b'\x7fELF',0,512)
 header=struct.unpack_from('<16sHHIQQQIHHHHHH',data,elf)
 ph=[struct.unpack_from('<IIQQQQQQ',data,elf+header[5]+i*header[9]) for i in range(header[10])]
 uc=Uc(UC_ARCH_X86,UC_MODE_64)
 uc.mem_map(0,0x1500000)
 for i in range((elf-32)//32):
  flags,offset,stored,_=struct.unpack_from('<QQQQ',data,32+i*32)
  if flags&0x800:
   segment=ph[flags>>20]
   if segment[0] in (1,0x61000010):uc.mem_write(segment[3],data[offset:offset+stored])
 uc.mem_map(0x50000000,0x100000)
 frame=0x50080000;target=0x50002000
 def write(address,fmt,value):uc.mem_write(address,struct.pack(fmt,value))
 def imported_round(machine,address,size,unused):
  if address!=0xe6d3b0:return
  value=struct.unpack('<d',machine.reg_read(UC_X86_REG_XMM0).to_bytes(16,'little')[:8])[0]
  rounded=math.copysign(math.floor(abs(value)+.5),value)
  machine.reg_write(UC_X86_REG_XMM0,int.from_bytes(struct.pack('<d',rounded),'little'))
  sp=machine.reg_read(UC_X86_REG_RSP)
  ret=struct.unpack('<Q',machine.mem_read(sp,8))[0]
  machine.reg_write(UC_X86_REG_RSP,sp+8);machine.reg_write(UC_X86_REG_RIP,ret)
 uc.hook_add(UC_HOOK_CODE,imported_round)
 vectors=[]
 coordinates=[-2560.,-2048.,-1536.,-1024.1,-1024.,-1023.9,-768.,-512.1,-512.,-511.9,-432.,-256.,-128.,-64.,-.1,0.,.1,63.9,64.,127.9,128.,255.9,256.,431.,511.9,512.,512.1,768.,1024.,1536.,2048.,2559.]
 for scale in range(5):
  for x in coordinates:
   z=-x
   uc.reg_write(UC_X86_REG_RBP,frame);uc.reg_write(UC_X86_REG_RSP,frame-0x1000)
   for offset,fmt,value in [(-0x108,'<d',x),(-0xf0,'<d',z),(-0xd8,'<d',float(128<<scale)),(-0xdc,'<i',scale),(-0xf4,'<i',128<<scale),(-0xb0,'<Q',target)]:write(frame+offset,fmt,value)
   # Exact center calculation and writes in MapItem::create; stop before dimension lookup.
   uc.emu_start(0x443427,0x4434cd,count=200)
   cx,cz=struct.unpack('<ii',uc.mem_read(target+0x44,8))
   vectors.append({'scale':scale,'x':x,'z':z,'centerX':cx,'centerZ':cz})
 first=[]
 for exists in [False,True]:
  uc.reg_write(UC_X86_REG_RAX,int(exists))
  uc.emu_start(0x226005,0x22600d,count=10)
  first.append({'hasMaps':exists,'scale':uc.reg_read(UC_X86_REG_RCX)})
 bounds=[];dimension=0x50004000;vtable=0x50005000;stop=0x500ff000
 write(dimension,'<Q',vtable);write(vtable+0xc8,'<Q',0x1fe120)
 for width in [864,1024,3072,5120]:
  write(dimension+0x4c,'<i',width//16)
  half=width//32
  for x in [-half-1,-half,-half+1,-1,0,half-1,half,half+1]:
   for z in [-half-1,-half,0,half-1,half]:
    sp=frame-0x1000;write(sp,'<Q',stop)
    uc.reg_write(UC_X86_REG_RSP,sp);uc.reg_write(UC_X86_REG_RDI,dimension)
    uc.reg_write(UC_X86_REG_RSI,x&0xffffffff);uc.reg_write(UC_X86_REG_RDX,z&0xffffffff)
    uc.emu_start(0x1fe210,stop,count=100)
    bounds.append({'width':width,'chunkX':x,'chunkZ':z,'inside':bool(uc.reg_read(UC_X86_REG_RAX)&255)})
 output={'executableSha256':SELF_SHA256,'mapCenters':vectors,'initialScales':first,'chunkBounds':bounds}
 args.output.parent.mkdir(parents=True,exist_ok=True)
 args.output.write_text(json.dumps(output,indent=2)+'\n')
 print(f'Captured {len(vectors)} map centers, {len(first)} initial scales, {len(bounds)} bounds cases')

if __name__=='__main__':main()
