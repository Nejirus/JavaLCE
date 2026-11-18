"""Recover literal PS4 recipe results/categories and crafting family metadata.

Unknown dynamic outputs are omitted, never filled with guessed PS4 values.
"""
import argparse,json,bisect,hashlib,struct
from pathlib import Path
from read_block_constructors import Source,RootFlow
parser=argparse.ArgumentParser(description=__doc__);parser.add_argument("--eboot",type=Path,required=True);parser.add_argument("--call-xrefs",type=Path,help="Optional cached direct-call index");parser.add_argument("--output",type=Path,required=True);options=parser.parse_args()
Image=Source
from capstone.x86 import *
im=Image(options.eboot);cs=im.cs
APIS={0x5a9f20:2,0x5aa0a0:3,0x5ab2c0:2}
if options.call_xrefs:
 refs=json.loads(options.call_xrefs.read_text())
else:
 # Find direct E8 calls, then verify instruction boundaries in the enclosing
 # source function. This avoids depending on a private analysis cache.
 candidates=set()
 for segment in im.segments:
  if segment[0]!=1 or not segment[1]&1:continue
  data=im.read(segment[3],segment[5]);offset=data.find(b'\xe8')
  while offset!=-1 and offset+5<=len(data):
   pc=segment[3]+offset;target=pc+5+struct.unpack_from('<i',data,offset+1)[0]
   if target in APIS:candidates.add(im.functions[bisect.bisect_right(im.functions,pc)-1])
   offset=data.find(b'\xe8',offset+1)
 refs={str(api):[] for api in APIS}
 for function in sorted(candidates):
  for it in im.instructions(function):
   if it.mnemonic=='call' and it.operands[0].type==X86_OP_IMM and it.operands[0].imm in APIS:
    refs[str(it.operands[0].imm)].append(it.address)
aliases={int(r['alias'],16):r['name'] for r in json.load(open(Path(__file__).with_name('block-registry-native.json')))['aliases']}
# The original named-item static initializer binds a lookup name to each global.
name=None;ins=list(im.instructions(0x3a5a80));p=None;length=0
for idx,it in enumerate(ins):
 o=it.operands
 if it.mnemonic=='lea' and len(o)==2 and o[0].reg==X86_REG_RSI and o[1].type==X86_OP_MEM and o[1].mem.base==X86_REG_RIP:p=it.address+it.size+o[1].mem.disp
 if it.mnemonic=='mov' and len(o)==2 and o[0].reg==X86_REG_EDX and o[1].type==X86_OP_IMM:length=o[1].imm
 if it.mnemonic=='call' and it.op_str=='0x67840' and p:name=im.read(p,length*2).decode('utf-16le')
 if it.mnemonic=='lea' and len(o)==2 and o[1].type==X86_OP_MEM and o[1].mem.base==X86_REG_RIP:
  a=it.address+it.size+o[1].mem.disp
  if 0x1415000<=a<0x1419000:
   for z in ins[idx+1:idx+6]:
    q=z.operands
    if z.mnemonic=='mov' and len(q)==2 and q[0].type==X86_OP_MEM and q[0].mem.base==o[0].reg and q[1].type==X86_OP_REG and q[1].reg==X86_REG_RBX:aliases[a]=name

def rn(r):
 n=cs.reg_name(r)
 return {'eax':'rax','ax':'rax','al':'rax','ebx':'rbx','ecx':'rcx','edx':'rdx','edi':'rdi','esi':'rsi',**{f'r{i}d':f'r{i}' for i in range(8,16)}}.get(n,n)
def plus(v,d):
 if isinstance(v,int):return v+d
 if isinstance(v,tuple) and v[0] in ['stack','data','object']:return (v[0],v[1]+d)
 return None
functions=sorted({im.functions[bisect.bisect_right(im.functions,p)-1] for api in APIS for p in refs.get(str(api),[])})
rows=[]
for func in functions:
 regs={'rsp':('stack',8)};memory={};objects={}
 def addr(ins,o):
  m=o.mem
  if m.base==X86_REG_RIP:return ('data',ins.address+ins.size+m.disp)
  return plus(regs.get(rn(m.base)),m.disp)
 def value(ins,o):
  if o.type==X86_OP_IMM:return o.imm
  if o.type==X86_OP_REG:return regs.get(rn(o.reg))
  if o.type==X86_OP_MEM:
   a=addr(ins,o)
   if a in memory:return memory[a]
   if isinstance(a,tuple) and a[0]=='data':return ('global',a[1])
  return None
 for it in im.instructions(func):
  o=it.operands;m=it.mnemonic
  if m=='lea':regs[rn(o[0].reg)]=addr(it,o[1])
  elif m in ['mov','movabs','movzx','movsx','movsxd'] and len(o)==2:
   v=value(it,o[1]);v=(v&((1<<(o[0].size*8))-1)) if isinstance(v,int) else v
   if o[0].type==X86_OP_REG:regs[rn(o[0].reg)]=v
   elif o[0].type==X86_OP_MEM:memory[addr(it,o[0])]=v
  elif m in ['add','sub'] and o[0].type==X86_OP_REG:
   v=value(it,o[1]);regs[rn(o[0].reg)]=plus(value(it,o[0]),v if m=='add' else -v) if isinstance(v,int) else None
  elif m=='xor' and len(o)==2 and o[0].type==o[1].type==X86_OP_REG and o[0].reg==o[1].reg:regs[rn(o[0].reg)]=0
  elif m=='push':
   regs['rsp']=plus(regs.get('rsp'),-8);memory[regs['rsp']]=value(it,o[0])
  elif m=='pop':
   regs[rn(o[0].reg)]=memory.get(regs['rsp']);regs['rsp']=plus(regs.get('rsp'),8)
  elif m=='call':
   t=value(it,o[0]);args=[regs.get(k) for k in ['rdi','rsi','rdx','rcx','r8','r9']];ret=None
   if t==0xe6cfd0:ret=('object',it.address)
   elif t in [0x3b7720,0x3b7680,0x3b76d0,0x3b75c0,0x3b7600,0x3b7700]:
    item=args[1];name=aliases.get(item[1]) if isinstance(item,tuple) and item[0]=='global' else None
    objects[args[0]]={'name':name,'count':args[2] if t in [0x3b7720,0x3b7680,0x3b7600,0x3b7700] else 1,'aux':args[3] if t in [0x3b7720,0x3b7680] else 0}
    ret=args[0]
   elif t in APIS:
    fmt=args[APIS[t]];out=objects.get(args[1]);cat=None;code=None
    if isinstance(fmt,tuple) and fmt[0]=='data':
     raw=im.read(fmt[1],80);code=raw.decode('utf-16le',errors='replace').split('\0')[0]
     vals=args[APIS[t]+1:]+[memory.get(plus(regs.get('rsp'),i*8)) for i in range(32)]
     if code and code.endswith('g') and len(vals)>=len(code):cat=vals[len(code)-1]
    if out and out['name'] and cat in [ord(c) for c in 'STFAMVD']:
     rows.append(dict(**out,tab='STFAMVD'.index(chr(cat)),group=chr(cat),pc=hex(it.address),function=hex(func),format=code))
   for r in ['rax','rcx','rdx','rdi','rsi','r8','r9','r10','r11']:regs[r]=None
   regs['rax']=ret
# Preserve the source's registration call order, including direct recipe-class calls.
by_function={f:[] for f in functions}
for row in rows:by_function[int(row['function'],16)].append(row)
ordered=[];visited=set()
def visit(function):
 if function in visited:return
 visited.add(function)
 by_pc={int(r['pc'],16):r for r in by_function.get(function,[])}
 for it in im.instructions(function):
  if it.address in by_pc:ordered.append(by_pc[it.address])
  if it.mnemonic=='call' and it.operands[0].type==X86_OP_IMM and it.operands[0].imm in by_function:visit(it.operands[0].imm)
visit(0x5a0740)
for f in functions:visit(f)
# Explicit field stores in the original block registration stream.
root=RootFlow(im).run();blocks=[]
for row in root.rows:
 value=root.memory.get(('object',row['object'][1],0x14c))
 if isinstance(value,int):blocks.append(dict(name=row['name'],id=row['id'],base_type=value>>32,material=value&0xffffffff,registration_pc=row['register_pc']))
# Explicit item fields; copies through constructors are not claimed here.
registers={};strings={};objects={};items=[]
def regvalue(op):
 if op.type==X86_OP_IMM:return op.imm
 if op.type==X86_OP_REG:return registers.get(rn(op.reg))
 return None
for it in im.instructions(0x386070):
 op=it.operands;m=it.mnemonic
 if m=='lea' and len(op)==2:
  result=None
  if op[1].type==X86_OP_MEM:
   if op[1].mem.base==X86_REG_RIP:result=('data',it.address+it.size+op[1].mem.disp)
   elif op[1].mem.base==X86_REG_RBP:result=('stack',op[1].mem.disp)
  registers[rn(op[0].reg)]=result
 elif m in ['mov','movabs'] and len(op)==2:
  if op[0].type==X86_OP_REG:registers[rn(op[0].reg)]=regvalue(op[1])
  elif op[0].type==X86_OP_MEM:
   obj=registers.get(rn(op[0].mem.base));v=regvalue(op[1])
   if isinstance(obj,tuple) and obj[0]=='object' and op[0].mem.disp==0x28 and isinstance(v,int):objects[obj]=(v&0xffffffff,v>>32 if op[0].size==8 else None,hex(it.address))
 elif m=='xor' and len(op)==2 and op[0].type==op[1].type==X86_OP_REG and op[0].reg==op[1].reg:registers[rn(op[0].reg)]=0
 elif m=='call':
  target=regvalue(op[0]);dst=registers.get('rdi');src=registers.get('rsi');length=registers.get('rdx');ret=None
  if target==0x67840 and isinstance(src,tuple) and src[0]=='data' and isinstance(length,int):strings[dst]=im.read(src[1],length*2).decode('utf-16le')
  elif target in [0xcda2f0,0xcda0b0]:strings[dst]=strings.get(src)
  elif target==0xe6cfd0:ret=('object',it.address)
  elif target==0x3af660:
   name=strings.get(length);attr=objects.get(registers.get('rcx'))
   if name and attr:items.append(dict(name=name,id=src,base_type=attr[0],material=attr[1],store_pc=attr[2],registration_pc=hex(it.address)))
  for r in ['rax','rcx','rdx','rdi','rsi','r8','r9','r10','r11']:registers[r]=None
  registers['rax']=ret
output=dict(repository='Nejirus/mcps4',revision='37e258085773f11599cf0ffe29119926769554b7',eboot_sha256=hashlib.sha256(options.eboot.read_bytes()).hexdigest(),input_kind='Literal recipe result/category calls, block registration field 0x14c, and item registration field 0x28. Unknown dynamic outputs/constructor-inherited metadata omitted.',recipes=ordered,blocks=blocks,items=items)
assert len(ordered)==419 and len(blocks)==195 and len(items)==126
options.output.write_text(json.dumps(output,indent=2)+'\n')
print('Recovered',len(ordered),'recipe registrations,',len(blocks),'block families,',len(items),'item families')
