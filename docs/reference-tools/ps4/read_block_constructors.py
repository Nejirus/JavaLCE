#!/usr/bin/env python3
"""Recover original block constructor/material arguments without executing code.

The root stream's registration names/IDs/PCs must agree with the separate
read_block_registry extractor. Unknown material origins or constructor-prefix
branches fail instead of assigning a Java-style material.
"""
import argparse,bisect,hashlib,json,struct
from pathlib import Path
from capstone import Cs,CS_ARCH_X86,CS_MODE_64
from capstone.x86 import X86_OP_REG,X86_OP_MEM,X86_OP_IMM,X86_REG_RIP
from native_module import Module
from read_block_registry import extract,EXPECTED

ARGUMENTS=['rdi','rsi','rdx','rcx','r8','r9']
VOLATILE=['rax','rcx','rdx','rdi','rsi','r8','r9','r10','r11']
SETTERS={0x90,0x98,0xa0,0xa8,0xb0,0x108,0x110,0x128,0x130,0x138,0x158,0x370,0x3b0,0x418,0x420}

class Source:
    def __init__(self,path):
        if hashlib.sha256(path.read_bytes()).hexdigest()!=EXPECTED:raise ValueError('Unexpected mcps4 executable')
        module=Module(path);self.file=module.file;self.segments=module.ph
        header=next(p for p in module.ph if p[0]==0x6474e550)
        count=struct.unpack_from('<I',self.file,header[2]+8)[0]
        self.functions=[header[3]+struct.unpack_from('<i',self.file,header[2]+12+i*8)[0] for i in range(count)]
        self.cs=Cs(CS_ARCH_X86,CS_MODE_64);self.cs.detail=True
    def read(self,address,length):
        for p in self.segments:
            if p[0] in [1,0x61000010] and p[3]<=address<p[3]+p[5]:
                offset=p[2]+address-p[3];return bytes(self.file[offset:offset+length])
        raise ValueError(('Unmapped source bytes',hex(address)))
    def instructions(self,pc,length=None):
        if length is None:length=self.functions[bisect.bisect_right(self.functions,pc)]-pc
        return self.cs.disasm(self.read(pc,length),pc)
    def register(self,number):
        name=self.cs.reg_name(number)
        return {'eax':'rax','ax':'rax','al':'rax','ebx':'rbx','bx':'rbx','bl':'rbx','ecx':'rcx','cx':'rcx','cl':'rcx','edx':'rdx','dx':'rdx','dl':'rdx','edi':'rdi','dil':'rdi','esi':'rsi','sil':'rsi','ebp':'rbp','esp':'rsp',**{f'r{i}{suffix}':f'r{i}' for i in range(8,16) for suffix in ['d','w','b']}}.get(name,name)

def plus(base,offset):
    if base is None:return None
    if isinstance(base,int):return base+offset
    if base[0] in ['stack','local','data']:return (base[0],base[1]+offset)
    if base[0]=='object':return ('object',base[1],base[2]+offset)
    return ('add',base,offset)

class RootFlow:
    def __init__(self,image):
        self.image=image;self.regs={'rbp':('stack',0),'rsp':('stack',-0x360)};self.memory={};self.strings={};self.objects={};self.rows=[]
    def address(self,it,op):
        m=op.mem
        if m.base==X86_REG_RIP:return ('data',it.address+it.size+m.disp)
        index=self.regs.get(self.image.register(m.index)) if m.index else 0
        return plus(self.regs.get(self.image.register(m.base)),m.disp+index*m.scale) if isinstance(index,int) else None
    def load(self,address):
        if address in self.memory:return self.memory[address]
        if not isinstance(address,tuple):return None
        if address[0]=='data':return ('global',address[1])
        if address[0]=='object':
            if address[2]==0:return ('vtable',address)
            if address[2]==0x190:return ('state',('object',address[1],0))
        if address[0]=='add':
            value,offset=address[1:]
            if value and value[0]=='state' and offset==0:return ('vtable',value)
            if value and value[0]=='vtable':return ('method',value[1],offset)
        return None
    def value(self,it,op):
        if op.type==X86_OP_IMM:return op.imm
        if op.type==X86_OP_REG:return self.regs.get(self.image.register(op.reg))
        if op.type==X86_OP_MEM:return self.load(self.address(it,op))
    def store(self,it,op,value):
        if op.type==X86_OP_REG:
            if isinstance(value,int) and op.size<8:value&=(1<<(op.size*8))-1
            self.regs[self.image.register(op.reg)]=value
        elif op.type==X86_OP_MEM:
            address=self.address(it,op)
            if address is not None:self.memory[address]=value
    def run(self):
        for it in self.image.instructions(0x957a0,0xb7dc0-0x957a0):
            op=it.operands;m=it.mnemonic
            if m in ['push','pop','leave','ret','nop'] or (op and op[0].type==X86_OP_REG and self.image.register(op[0].reg) in ['rbp','rsp']):continue
            if m=='lea':self.store(it,op[0],self.address(it,op[1]));continue
            if m in ['mov','movabs','movzx','movsx','movsxd']:self.store(it,op[0],self.value(it,op[1]));continue
            if m=='xor' and op[0].type==op[1].type==X86_OP_REG and op[0].reg==op[1].reg:self.store(it,op[0],0);continue
            if m in ['add','sub']:
                a,b=self.value(it,op[0]),self.value(it,op[1]);self.store(it,op[0],plus(a,b if m=='add' else -b) if isinstance(b,int) else None);continue
            if m=='call':
                target=self.value(it,op[0]);args=[self.regs.get(r) for r in ARGUMENTS];dst,src,length=args[:3];result=None
                if target in [0xe6cfd0,0xe6cfc0]:
                    result=('object',it.address,0);self.objects[it.address]={'allocation_pc':hex(it.address),'size':dst,'calls':[]}
                elif target==0x67840 and isinstance(src,tuple) and src[0]=='data' and isinstance(length,int):
                    self.strings[dst]=self.image.read(src[1],length*2).decode('utf-16le');result=dst
                elif target in [0xcda2f0,0xcda0b0]:self.strings[dst]=self.strings.get(src);result=dst
                elif op[0].type!=X86_OP_IMM:
                    if isinstance(src,int) and 0<=src<4096 and self.strings.get(length):self.rows.append({'id':src,'name':self.strings[length],'register_pc':hex(it.address),'object':args[3]})
                    if it.address==0x95a7b:
                        if src!=0:raise ValueError('Air registration ID changed')
                        self.rows.append({'id':0,'name':'air','register_pc':hex(it.address),'object':args[3]})
                    if isinstance(target,tuple) and target[0]=='method' and target[2] not in SETTERS:raise ValueError(('Unexpected root virtual',hex(it.address),target))
                    result=dst
                elif isinstance(dst,tuple) and dst[0]=='object' and dst[2]==0 and target not in [0xe6cfb0,0xe6cfa0,0xe6d0b0,0xcdab00]:
                    self.objects[dst[1]]['calls'].append({'pc':hex(it.address),'target':hex(target),'args':args});result=dst
                for register in VOLATILE:self.regs[register]=None
                self.regs['rax']=result;continue
            if op and op[0].type==X86_OP_REG and m not in ['cmp','test'] and not m.startswith('j'):self.regs[self.image.register(op[0].reg)]=None
        for row in self.rows:
            obj=row['object']
            if not isinstance(obj,tuple) or obj[0]!='object' or not self.objects[obj[1]]['calls']:raise ValueError(('Unresolved registration object',row))
            row['allocation']=self.objects[obj[1]]
        return self

class MaterialFlow:
    def __init__(self,root):self.root=root;self.image=root.image;self.cache={};self.functions=set();self.helpers=set()
    def owner(self,obj):
        if obj[0]=='state':return obj[1]
        if obj[0]=='object' and self.root.objects[obj[1]]['size']==56:return self.root.memory[('object',obj[1],0x18)]
        raise ValueError(('Unresolved state owner',obj))
    def material(self,obj):
        if obj[1] not in self.cache:
            call=self.root.objects[obj[1]]['calls'][0];result,path=self.prefix(int(call['target'],16),call['args'],[])
            if not isinstance(result,tuple) or result[0]!='global':raise ValueError(('Unresolved material origin',obj,call,result,path))
            self.cache[obj[1]]=(result,path)
        return self.cache[obj[1]]
    def prefix(self,pc,args,path):
        self.functions.add(pc);path=path+[hex(pc)]
        if pc in [0x91fb0,0x92150]:return args[1],path
        regs=dict(zip(ARGUMENTS,args));regs.update(rbp=('local',0),rsp=('local',-0x1000));memory={};instructions={it.address:it for it in self.image.instructions(pc)};cursor=pc;comparison=None
        def address(it,op):
            m=op.mem
            if m.base==X86_REG_RIP:return ('data',it.address+it.size+m.disp)
            return None if m.index else plus(regs.get(self.image.register(m.base)),m.disp)
        def load(a):
            if a in memory:return memory[a]
            if a in self.root.memory:return self.root.memory[a]
            if not isinstance(a,tuple):return None
            if a[0]=='data':return ('global',a[1])
            if a[0]=='object':
                if a[2]==0:return ('vtable',('object',a[1],0))
                if a[2]==0x170:return self.material(('object',a[1],0))[0]
            if a[0]=='add':
                value,offset=a[1:]
                if value and value[0]=='vtable':return ('method',value[1],offset)
                if value and value[0]=='state' and offset==0:return ('vtable',value)
                if value and value[0]=='global' and offset==0x38:return ('color',value)
            return None
        def value(it,op):
            if op.type==X86_OP_IMM:return op.imm
            if op.type==X86_OP_REG:return regs.get(self.image.register(op.reg))
            if op.type==X86_OP_MEM:return load(address(it,op))
        def store(it,op,v):
            if op.type==X86_OP_REG:
                if isinstance(v,int):v&=(1<<(op.size*8))-1
                regs[self.image.register(op.reg)]=v
            elif op.type==X86_OP_MEM:
                a=address(it,op)
                if a is not None:memory[a]=v
        for step in range(300):
            it=instructions[cursor];op=it.operands;m=it.mnemonic;cursor+=it.size
            if m in ['push','pop','nop'] or (op and op[0].type==X86_OP_REG and self.image.register(op[0].reg) in ['rbp','rsp']):continue
            if m=='lea':store(it,op[0],address(it,op[1]));continue
            if m in ['mov','movabs','movzx','movsx','movsxd']:store(it,op[0],value(it,op[1]));continue
            if m=='xor' and op[0].type==op[1].type==X86_OP_REG and op[0].reg==op[1].reg:store(it,op[0],0);continue
            if m in ['add','sub']:
                b=value(it,op[1]);store(it,op[0],plus(value(it,op[0]),b if m=='add' else -b) if isinstance(b,int) else None);continue
            if m=='cmp':comparison=(value(it,op[0]),value(it,op[1]));continue
            if m=='test':
                a,b=value(it,op[0]),value(it,op[1]);tested=a&b if isinstance(a,int) and isinstance(b,int) else 1 if a==b and isinstance(a,tuple) and a[0]=='global' and 0x1418fb8<=a[1]<0x1419248 else None;comparison=(tested,0);continue
            if m=='jmp' and op[0].type==X86_OP_IMM:
                if op[0].imm not in instructions:return self.prefix(op[0].imm,[regs.get(r) for r in ARGUMENTS],path)
                cursor=op[0].imm;continue
            if m.startswith('j'):
                a,b=comparison
                if not isinstance(a,int) or not isinstance(b,int):raise ValueError(('Unknown prefix branch',path,hex(it.address),comparison))
                take={'je':a==b,'jz':a==b,'jne':a!=b,'jnz':a!=b,'jb':a<b,'jae':a>=b,'ja':a>b,'jbe':a<=b,'jl':a<b,'jge':a>=b,'jg':a>b,'jle':a<=b}[m]
                if take:cursor=op[0].imm
                continue
            if m=='call':
                target=value(it,op[0]);call_args=[regs.get(r) for r in ARGUMENTS];dst=call_args[0];result=None
                if target in [0x91fb0,0x92150]:self.functions.add(target);return call_args[1],path+[hex(target)]
                if isinstance(target,int) and dst==args[0]:return self.prefix(target,call_args,path)
                if isinstance(target,tuple) and target[0]=='method' and target[2]==0x170:result=self.owner(dst)
                elif target in [0x67840,0x44d830,0x44cd30,0x503520]:self.helpers.add(target);result=dst if target==0x67840 else ('color',dst)
                else:raise ValueError(('Unknown prefix call',path,hex(it.address),target,call_args))
                for register in VOLATILE:regs[register]=None
                regs['rax']=result;continue
            if m=='ret':raise ValueError(('Return before material constructor',path))
            if op and op[0].type==X86_OP_REG:regs[self.image.register(op[0].reg)]=None
        raise ValueError(('Constructor prefix limit',path))

def audit_setters(image,functions):
    """Check return-this methods used to track registration object identity.

    All primary vtables installed by these constructor bodies dispatch these
    slots to the same audited Block implementations, which do not store +170.
    State's property setter (158) is outside this Block slot set.
    """
    tables=set()
    for function in functions:
        regs={'rdi':('self',0)};memory={}
        def address(it,op):
            m=op.mem
            if m.base==X86_REG_RIP:return ('data',it.address+it.size+m.disp)
            base=regs.get(image.register(m.base))
            if isinstance(base,tuple) and not m.index:return (base[0],base[1]+m.disp)
        def value(it,op):
            if op.type==X86_OP_IMM:return op.imm
            if op.type==X86_OP_REG:return regs.get(image.register(op.reg))
            if op.type==X86_OP_MEM:return memory.get(address(it,op))
        for it in image.instructions(function):
            op=it.operands;m=it.mnemonic
            if m in ['mov','movabs']:
                v=value(it,op[1])
                if op[0].type==X86_OP_REG:regs[image.register(op[0].reg)]=v
                elif op[0].type==X86_OP_MEM:
                    a=address(it,op[0]);memory[a]=v
                    if a==('self',0) and isinstance(v,tuple) and v[0]=='data':tables.add(v[1])
            elif m=='lea':regs[image.register(op[0].reg)]=address(it,op[1])
            elif m in ['add','sub'] and op[0].type==X86_OP_REG:
                a,b=value(it,op[0]),value(it,op[1]);regs[image.register(op[0].reg)]=(a[0],a[1]+(b if m=='add' else -b)) if isinstance(a,tuple) and isinstance(b,int) else None
            elif m=='call':
                for register in VOLATILE:regs[register]=None
    targets={0x90:0x92520,0x98:0x92540,0xa0:0x92550,0xa8:0x92560,0xb0:0x92580,0x108:0x92a00,0x110:0x92a30,0x128:0x92a90,0x130:0x92b20,0x138:0x92b30,0x370:0x94640,0x3b0:0x946d0,0x418:0x94890,0x420:0x948c0}
    if len(tables)!=200:raise ValueError(('Constructor vtable coverage changed',len(tables)))
    for slot,target in targets.items():
        if {struct.unpack('<Q',image.read(table+slot,8))[0] for table in tables}!={target}:raise ValueError(('Unexpected initialization setter override',hex(slot)))
    return {'primary_vtables':[hex(table) for table in sorted(tables)],'setter_targets':{hex(slot):hex(pc) for slot,pc in targets.items()}}

def recover(path):
    root=RootFlow(Source(path)).run();known=extract(path)['registrations']
    if [{k:r[k] for k in ['id','name','register_pc']} for r in root.rows if r['id']!=0]!=known:raise ValueError('Independent registration streams disagree')
    if len(root.rows)!=345 or len({r['id'] for r in root.rows})!=345:raise ValueError('Incomplete block constructor associations')
    flow=MaterialFlow(root);rows=[]
    for row in root.rows:
        alias,chain=flow.material(row['object']);rows.append({**row,'material_alias':hex(alias[1]),'constructor_chain':chain})
    writes=[]
    for pc in sorted(flow.functions):
        for it in root.image.instructions(pc):
            if it.mnemonic.startswith('mov') and it.operands[0].type==X86_OP_MEM and it.operands[0].mem.disp==0x170:writes.append(hex(it.address))
    if writes!=['0x9209d','0x92231']:raise ValueError(('Additional constructor material stores require review',writes))
    owners={key[1]:v[1] for key,v in root.memory.items() if isinstance(key,tuple) and key[0]=='object' and key[2]==0x18 and root.objects[key[1]]['size']==56 and isinstance(v,tuple) and v[0]=='object'}
    return {'registrations':rows,'objects':root.objects,'state_owners':owners,'constructor_functions':[hex(pc) for pc in sorted(flow.functions)],'nonself_helpers':[hex(pc) for pc in sorted(flow.helpers)],'material_stores':writes,**audit_setters(root.image,flow.functions)}

if __name__=='__main__':
    parser=argparse.ArgumentParser(description=__doc__);parser.add_argument('--eboot',type=Path,required=True);parser.add_argument('--output',type=Path,required=True);a=parser.parse_args();data=recover(a.eboot);a.output.write_text(json.dumps(data,indent=2)+'\n');print(a.output.name,len(data['registrations']),len(data['constructor_functions']),hashlib.sha256(a.output.read_bytes()).hexdigest())
