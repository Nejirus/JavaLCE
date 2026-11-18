from read_block_constructors import Source
from capstone.x86 import *
from pathlib import Path
import json
import argparse
parser=argparse.ArgumentParser(description='Recover all original BannerPattern constructor registrations, including the separate loom vector.')
parser.add_argument('--eboot',type=Path,required=True);parser.add_argument('--output',type=Path,required=True);options=parser.parse_args()
im=Source(options.eboot);regs={'rbp':('stack',0),'rsp':('stack',-0xf8)};mem={};strings={};rows=[]
def rn(r):return im.register(r)
def plus(v,d):return (v[0],v[1]+d) if isinstance(v,tuple) and v[0] in ['stack','data','object'] else v+d if isinstance(v,int) else None
def addr(it,o):
 m=o.mem
 return ('data',it.address+it.size+m.disp) if m.base==X86_REG_RIP else plus(regs.get(rn(m.base)),m.disp)
def val(it,o):
 if o.type==X86_OP_IMM:return o.imm
 if o.type==X86_OP_REG:return regs.get(rn(o.reg))
 if o.type==X86_OP_MEM:return mem.get(addr(it,o))
for it in im.instructions(0x46fd0):
 o=it.operands;m=it.mnemonic
 if m=='lea':regs[rn(o[0].reg)]=addr(it,o[1])
 elif m in ['mov','movabs','movzx']:
  if o[0].type==X86_OP_REG:regs[rn(o[0].reg)]=val(it,o[1])
  elif o[0].type==X86_OP_MEM:mem[addr(it,o[0])]=val(it,o[1])
 elif m=='xor' and len(o)==2 and o[0].type==o[1].type==X86_OP_REG and o[0].reg==o[1].reg:regs[rn(o[0].reg)]=0
 elif m in ['add','sub'] and o[0].type==X86_OP_REG:
  a,b=val(it,o[0]),val(it,o[1]);regs[rn(o[0].reg)]=plus(a,b if m=='add' else -b) if isinstance(b,int) else None
 elif m=='call':
  t=val(it,o[0]);a=[regs.get(r) for r in ['rdi','rsi','rdx','rcx','r8','r9']];ret=None
  if t==0xe6cfd0:ret=('object',it.address)
  elif t==0x67840 and isinstance(a[1],tuple) and a[1][0]=='data' and isinstance(a[2],int):strings[a[0]]=im.read(a[1][1],a[2]*2).decode('utf-16le')
  elif t==0x67a10:strings[a[0]]=strings.get(a[1])
  elif t==0x4d620:
   rows.append(dict(hash=strings.get(a[3]),id=a[1],ordinal=a[2],base_type=a[5],vector=mem.get(regs.get('rsp')),pc=hex(it.address)))
  for r in ['rax','rcx','rdx','rdi','rsi','r8','r9','r10','r11']:regs[r]=None
  regs['rax']=ret
assert len(rows)==77 and all(r['hash'] is not None for r in rows)
options.output.write_text(json.dumps(rows,indent=2)+'\n')
print('Recovered',len(rows),'banner pattern constructors')
