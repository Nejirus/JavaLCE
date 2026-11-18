"""Read mcps4's explicit item name/use-description fields at +0x68/+0x6c."""
import argparse,json,struct
from pathlib import Path
from read_block_constructors import Source
from capstone.x86 import *
p=argparse.ArgumentParser();p.add_argument('--eboot',type=Path,required=True);p.add_argument('--english',type=Path,required=True);p.add_argument('--output',type=Path,required=True);a=p.parse_args()
s=Source(a.eboot);regs={};strings={};objects={};rows=[];english=json.loads(a.english.read_text())
def rn(r):return s.register(r)
def val(o):
 if o.type==X86_OP_IMM:return o.imm
 if o.type==X86_OP_REG:return regs.get(rn(o.reg))
 return None
for it in s.instructions(0x386070):
 op=it.operands;m=it.mnemonic
 if m=='lea' and len(op)==2:
  result=None
  if op[1].type==X86_OP_MEM:
   if op[1].mem.base==X86_REG_RIP:result=('data',it.address+it.size+op[1].mem.disp)
   elif op[1].mem.base==X86_REG_RBP:result=('stack',op[1].mem.disp)
   elif isinstance(regs.get(rn(op[1].mem.base)),tuple):result=('member',regs[rn(op[1].mem.base)],op[1].mem.disp)
  regs[rn(op[0].reg)]=result
 elif m in ['mov','movabs'] and len(op)==2:
  if op[0].type==X86_OP_REG:regs[rn(op[0].reg)]=val(op[1])
  elif op[0].type==X86_OP_MEM:
   obj=regs.get(rn(op[0].mem.base));v=val(op[1]);off=op[0].mem.disp
   if isinstance(obj,tuple) and obj[0]=='object' and isinstance(v,int):
    row=objects.setdefault(obj,{})
    if off==0x68 and op[0].size==8:row.update(name_hash=f'{v&0xffffffff:08x}',description_hash=f'{(v>>32)&0xffffffff:08x}',store_pc=hex(it.address))
    elif off==0x6c and op[0].size==4:row.update(description_hash=f'{v&0xffffffff:08x}',store_pc=hex(it.address))
 elif m=='xor' and len(op)==2 and op[0].type==op[1].type==X86_OP_REG and op[0].reg==op[1].reg:regs[rn(op[0].reg)]=0
 elif m=='call':
  target=val(op[0]);dst=regs.get('rdi');src=regs.get('rsi');length=regs.get('rdx');ret=None
  if target==0x67840 and isinstance(src,tuple) and src[0]=='data' and isinstance(length,int):strings[dst]=s.read(src[1],length*2).decode('utf-16le')
  elif target in [0xcda2f0,0xcda0b0]:strings[dst]=strings.get(src)
  elif target==0xe6cfd0:ret=('object',it.address)
  elif target==0x3af660:
   name=strings.get(length);obj=regs.get('rcx');r=objects.get(obj,{})
   if name:
    r=dict(name=name,native_id=src,registration_pc=hex(it.address),**r)
    if r.get('description_hash') in english:r['text']=english[r['description_hash']]
    rows.append(r)
  for r in ['rax','rcx','rdx','rdi','rsi','r8','r9','r10','r11']:regs[r]=None
  regs['rax']=ret
out={'repository':'Nejirus/mcps4','revision':'37e258085773f11599cf0ffe29119926769554b7','root':'0x386070','field':'Item+0x6c','items':rows}
a.output.write_text(json.dumps(out,indent=2)+'\n');print('Items',len(rows),'English descriptions',sum('text' in r for r in rows));print([r for r in rows if r['name'] in ['wooden_sword','stick','diamond_pickaxe']])
