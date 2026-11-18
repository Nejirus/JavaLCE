"""Isolated constructor recovery. Executes no console startup or services."""
import os,sys,struct,json,bisect
from pathlib import Path
sys.path.insert(0,str(Path(__file__).resolve().parent.parent))
from native_module import Module,C
from unicorn import *
from unicorn.x86_const import *
from read_block_constructors import Source
from capstone.x86 import X86_OP_MEM,X86_OP_REG,X86_OP_IMM,X86_REG_RIP,X86_REG_RSI,X86_REG_EDX,X86_REG_RBX
import unicorn.x86_const as xr

m=Module(Path(os.environ['MCPS4_EBOOT']));src=Source(Path(os.environ['MCPS4_EBOOT']))
u=Uc(UC_ARCH_X86,UC_MODE_64);u.mem_map(m.base,len(m.mem));u.mem_write(m.base,bytes(m.mem))
HEAP=0x20000000;STACK=0x30000000;STOP=0x40000000
u.mem_map(HEAP,0x4000000);u.mem_map(STACK,0x800000);u.mem_map(STOP,0x1000)
cursor=HEAP;allocations={};events=[];last=[]
def alloc(n):
 global cursor
 p=cursor;cursor+=(n+31)&~31;allocations[p]={'size':n};return p
def write(p,fmt,*v):u.mem_write(p,struct.pack('<'+fmt,*v))
def read(p,fmt):return struct.unpack('<'+fmt,u.mem_read(p,struct.calcsize('<'+fmt)))
def ret(v=0):
 sp=u.reg_read(UC_X86_REG_RSP);pc=read(sp,'Q')[0];u.reg_write(UC_X86_REG_RAX,v);u.reg_write(UC_X86_REG_RSP,sp+8);u.reg_write(UC_X86_REG_RIP,pc)
regs=[UC_X86_REG_RDI,UC_X86_REG_RSI,UC_X86_REG_RDX,UC_X86_REG_RCX,UC_X86_REG_R8,UC_X86_REG_R9]
guard=alloc(8);write(m.base+0x134bae8,'Q',guard)
registry=json.loads((Path(__file__).resolve().parent.parent/'block-registry-native.json').read_text())
aliases={int(r['alias'],16):r['name'] for r in registry['aliases']}
ids={r['name']:r['id'] for r in registry['registrations']};ids['air']=0
name=None;p=None;length=0;ins=list(src.instructions(0x3a5a80))
for idx,it in enumerate(ins):
 o=it.operands
 if it.mnemonic=='lea' and len(o)==2 and o[0].reg==X86_REG_RSI and o[1].type==X86_OP_MEM and o[1].mem.base==X86_REG_RIP:p=it.address+it.size+o[1].mem.disp
 if it.mnemonic=='mov' and len(o)==2 and o[0].reg==X86_REG_EDX and o[1].type==X86_OP_IMM:length=o[1].imm
 if it.mnemonic=='call' and it.op_str=='0x67840' and p:name=src.read(p,length*2).decode('utf-16le')
 if it.mnemonic=='lea' and len(o)==2 and o[1].type==X86_OP_MEM and o[1].mem.base==X86_REG_RIP:
  a=it.address+it.size+o[1].mem.disp
  if 0x1415000<=a<0x1419000:
   for z in ins[idx+1:idx+6]:
    q=z.operands
    if z.mnemonic=='mov' and len(q)==2 and q[0].type==X86_OP_MEM and q[0].mem.base==o[0].reg and q[1].type==X86_OP_REG and q[1].reg==X86_REG_RBX:aliases[a]=name
named={};fake_names={}
for alias,name in aliases.items():
 if name not in named:
  ptr=alloc(0x400);named[name]=ptr;allocations[ptr]['registry_name']=name
  for offset in range(8,0x30,4):write(ptr+offset,'I',ids.get(name,0x8000+len(named)))
 write(m.base+alias,'Q',named[name]);fake_names[named[name]]=name
rulepack=alloc(64);areas={}
for node in json.loads((Path(os.environ['PS4_TUTORIAL_WORK'])/Path('Tutorial.pck.grf.json')).read_text())[1]['children']:
 if node['type']=='NamedArea':
  a=node['attributes'];p=alloc(48);write(p,'6d',*(float(a[k]) for k in ['x0','y0','z0','x1','y1','z1']));areas[a['name']]=p;allocations[p]['area']=a
tasks=[];hints=[]
statevt=alloc(0x200);write(statevt+0x170,'Q',STOP+16)
states={}
for name,blockptr in named.items():
 state=alloc(32);states[blockptr]=state;write(state,'Q',statevt);write(state+8,'Q',blockptr);allocations[state]['block_state']=name
def block(uc,address,size,data):
 pc=address-m.base;args=[uc.reg_read(r) for r in regs]
 last.append(hex(pc));last[:]=last[-16:]
 if pc in [0xe6cfd0,0xe6cfc0]:
  p=alloc(args[0]);allocations[p]['return_pc']=hex(read(uc.reg_read(UC_X86_REG_RSP),'Q')[0]-m.base);ret(p);return
 if pc in [0xe6cfa0,0xe6cfb0]:ret();return
 if pc in [0xe6d0a0,0xe6cf00]:
  u.mem_write(args[0],bytes(u.mem_read(args[1],args[2])));ret(args[0]);return
 if pc==0xe6d110:
  u.mem_write(args[0],bytes(u.mem_read(args[1],args[2]*2)));ret(args[0]);return
 if pc==0xe6d0b0:
  u.mem_write(args[0],bytes([args[1]&255])*args[2]);ret(args[0]);return
 if pc==0xafc4d0:ret();return
 if pc==0xaf4a10:ret(rulepack);return
 if pc==0x93a340:
  n=read(args[1]+24,'Q')[0];cap=read(args[1]+32,'Q')[0];p=read(args[1]+8,'Q')[0] if cap>=8 else args[1]+8;name=bytes(u.mem_read(p,n*2)).decode('utf-16le');ret(areas.get(name,0));return
 if pc==0x36ef50:ret(args[0]);return
 if pc==0x35fab0:
  name=fake_names.get(args[0]);ret(ids.get(name,0x8000+list(named).index(name)) if name else 0);return
 if pc==0x7a4d0:ret(states[args[0]]);return
 if address==STOP+16:ret(read(args[0]+8,'Q')[0]);return
 if pc==0x89ccf0:
  events.append({'platform':'add_player_constraint','object':args[1],'flag':args[2]});ret();return
 if pc==0x96de60:
  tasks.append({'state':args[1],'object':args[2]});ret();return
 if pc==0x99ea60:
  hints.append({'state':args[1],'object':args[2]});ret();return
 if pc==0x986450 and sys.argv[-1] in ['full','tasks','tasks-guided']:ret();return
 if pc==0x999780 and sys.argv[-1]=='full':ret();return
 if pc in src.functions and 0x95e000<=pc<0x9f0000:
  events.append({'function':hex(pc),'args':args,'stack':list(read(uc.reg_read(UC_X86_REG_RSP)+8,'8Q'))})
 if address==STOP:uc.emu_stop()
def invalid(uc,access,address,size,value,data):
 print('INVALID MEMORY',access,hex(address),size,'last',last,flush=True);return False
u.hook_add(UC_HOOK_BLOCK,block);u.hook_add(UC_HOOK_MEM_INVALID,invalid)
self=alloc(0x2000);sp=STACK+0x700000;write(sp,'Q',STOP);u.reg_write(UC_X86_REG_RSP,sp)
for reg,value in zip(regs,[self,0,0,1 if sys.argv[-1]=='tasks-guided' else 0,0,0]):u.reg_write(reg,value)
entry=0x964350 if sys.argv[-1]=='full' else 0x96d5c0
address=m.base+entry
for attempt in range(1000):
 try:u.emu_start(address,STOP,count=10000000);break
 except UcError as e:
  address=u.reg_read(UC_X86_REG_RIP);pc=address-m.base
  it=next(src.cs.disasm(bytes(u.mem_read(address,20)),address))
  def addr(op):
   mm=op.mem
   return (address+it.size if mm.base==X86_REG_RIP else u.reg_read(getattr(xr,'UC_X86_REG_'+src.cs.reg_name(mm.base).upper())))+mm.disp+(u.reg_read(getattr(xr,'UC_X86_REG_'+src.cs.reg_name(mm.index).upper()))*mm.scale if mm.index else 0)
  def value(op):
   if op.type==X86_OP_MEM:return bytes(u.mem_read(addr(op),op.size))
   if op.type==X86_OP_REG:return u.reg_read(getattr(xr,'UC_X86_REG_'+src.cs.reg_name(op.reg).upper())).to_bytes(op.size,'little')
  if e.errno==UC_ERR_INSN_INVALID and it.mnemonic in ['vmovups','vmovdqu','vmovaps','vmovdqa']:
   dst,ss=it.operands;v=value(ss)
   if dst.type==X86_OP_MEM:u.mem_write(addr(dst),v)
   else:u.reg_write(getattr(xr,'UC_X86_REG_'+src.cs.reg_name(dst.reg).upper()),int.from_bytes(v,'little'))
   address+=it.size;continue
  print('STOP',e,hex(pc),it.mnemonic,it.op_str,last,flush=True);break
else:raise ValueError('Instruction adapter limit')
def typename(p):
 try:
  vt=read(p,'Q')[0];info=read(vt-8,'Q')[0];name=read(info+8,'Q')[0];raw=bytes(u.mem_read(name,128)).split(b'\0')[0];return raw.decode()
 except Exception:return None
(Path(os.environ['PS4_TUTORIAL_WORK'])/Path('capture-'+sys.argv[-1]+'.json')).write_text(json.dumps({'base':m.base,'self':self,'tasks':tasks,'hints':hints,'events':events,'allocations':{str(p):dict(a,type=typename(p),bytes=bytes(u.mem_read(p,a['size'])).hex()) for p,a in allocations.items()}},indent=2)+'\n')
print('Recovered',len(tasks),'task registrations',len(events),'constructor calls',len(allocations),'allocations',flush=True)
