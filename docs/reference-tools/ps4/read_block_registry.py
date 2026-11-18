#!/usr/bin/env python3
"""Read block registration IDs/names and named aliases from pinned instructions.
Requires capstone 5. No original executable routine or module startup is run.
"""
import argparse,hashlib,json
from pathlib import Path
from native_module import Module,C
from capstone import Cs,CS_ARCH_X86,CS_MODE_64
from capstone.x86 import X86_OP_IMM,X86_OP_REG,X86_OP_MEM,X86_REG_RIP,X86_REG_RBP,X86_REG_RSI,X86_REG_EDX,X86_REG_RBX

EXPECTED='c6089834fe1372854eed8f9acf2cb307f7009aaeb32ee303ce5bf6a1160786c8'

def extract(path):
    if hashlib.sha256(path.read_bytes()).hexdigest()!=EXPECTED:raise ValueError('Unexpected mcps4 executable')
    module=Module(path);cs=Cs(CS_ARCH_X86,CS_MODE_64);cs.detail=True
    def instructions(start,end):return list(cs.disasm(C.string_at(module.base+start,end-start),start))
    def utf16(address,length):return C.string_at(module.base+address,length*2).decode('utf-16le')
    regs={};strings={};registrations=[]
    def register(r):
        n=cs.reg_name(r)
        return {'eax':'rax','ebx':'rbx','ecx':'rcx','edx':'rdx','edi':'rdi','esi':'rsi',**{f'r{i}d':f'r{i}' for i in range(8,16)}}.get(n,n)
    def value(op):
        if op.type==X86_OP_IMM:return op.imm
        if op.type==X86_OP_REG:return regs.get(register(op.reg))
        return None
    for ins in instructions(0x957a0,0xb7dc0):
        op=ins.operands
        if ins.mnemonic=='lea' and len(op)==2:
            result=None
            if op[1].type==X86_OP_MEM:
                if op[1].mem.base==X86_REG_RIP:result=('data',ins.address+ins.size+op[1].mem.disp)
                elif op[1].mem.base==X86_REG_RBP:result=('stack',op[1].mem.disp)
            regs[register(op[0].reg)]=result
        elif ins.mnemonic in ['mov','movabs'] and len(op)==2 and op[0].type==X86_OP_REG:regs[register(op[0].reg)]=value(op[1])
        elif ins.mnemonic=='call':
            target=value(op[0]);dst=regs.get('rdi');src=regs.get('rsi');length=regs.get('rdx')
            if target==0x67840 and isinstance(src,tuple) and src[0]=='data' and isinstance(length,int):strings[dst]=utf16(src[1],length)
            elif target in [0xcda2f0,0xcda0b0]:strings[dst]=strings.get(src)
            elif op[0].type==X86_OP_REG and isinstance(src,int) and 0<=src<4096 and strings.get(length):registrations.append({'id':src,'name':strings[length],'register_pc':hex(ins.address)})
            for r in ['rax','rcx','rdx','rdi','rsi','r8','r9','r10','r11']:regs[r]=None
    aliases=[];name=None;length=0;current=None;ins=instructions(0xc1520,0xcf3e0)
    for i,it in enumerate(ins):
        op=it.operands
        if it.mnemonic=='lea' and len(op)==2 and op[0].reg==X86_REG_RSI and op[1].type==X86_OP_MEM and op[1].mem.base==X86_REG_RIP:name=(it.address+it.size+op[1].mem.disp,it.address)
        if it.mnemonic=='mov' and len(op)==2 and op[0].type==X86_OP_REG and op[0].reg==X86_REG_EDX and op[1].type==X86_OP_IMM:length=op[1].imm
        if it.mnemonic=='call' and it.op_str=='0x67840' and name:current=utf16(name[0],length)
        if it.mnemonic=='lea' and len(op)==2 and op[1].type==X86_OP_MEM and op[1].mem.base==X86_REG_RIP:
            address=it.address+it.size+op[1].mem.disp
            if 0x1409000<=address<0x140c000:
                for following in ins[i+1:i+5]:
                    f=following.operands
                    if following.mnemonic=='mov' and len(f)==2 and f[0].type==X86_OP_MEM and f[0].mem.base==op[0].reg and f[1].type==X86_OP_REG and f[1].reg==X86_REG_RBX:aliases.append({'alias':hex(address),'name':current,'string_pc':hex(name[1]),'store_pc':hex(following.address)})
    names={r['name'] for r in registrations};alias_names={a['name'] for a in aliases}
    if len(registrations)!=344 or len({r['id'] for r in registrations})!=344 or len(names)!=344 or len(aliases)!=345 or alias_names-names!={'air'} or names-alias_names:raise ValueError('Registration/alias instruction streams do not agree')
    return {'repository':'https://github.com/Nejirus/mcps4','commit':'37e258085773f11599cf0ffe29119926769554b7','eboot_sha256':EXPECTED,
            'input_kind':'Static instruction/dataflow recovery of 344 registrations at 0x957a0 and 345 named lookups at 0xc1520. Air is looked up here but is not registered by this routine. No block constructors, full registry or world generation executed.',
            'registrations':registrations,'aliases':aliases}

if __name__=='__main__':
    parser=argparse.ArgumentParser(description=__doc__);parser.add_argument('--eboot',type=Path,required=True);parser.add_argument('--output',type=Path,required=True);args=parser.parse_args()
    data=extract(args.eboot);args.output.write_text(json.dumps(data,indent=2)+'\n');print(args.output.name,len(data['registrations']),len(data['aliases']),hashlib.sha256(args.output.read_bytes()).hexdigest())
