#!/usr/bin/env python3
"""Recover literal boolean/integer property initializer inputs from mcps4.

This reads code and UTF16 constants; it never executes module startup. Direct
constructors, factory calls and copied global aliases retain their origin PCs.
The two generic factory forwarders have dynamic inputs and are not initializers.
"""
import argparse,bisect,json,struct
from pathlib import Path
from capstone.x86 import X86_OP_REG,X86_OP_MEM,X86_OP_IMM,X86_REG_RIP
from read_block_constructors import Source,VOLATILE,ARGUMENTS,plus

FACTORIES={0xdabb0:'boolean',0x37a180:'integer',0xda5a0:'boolean',0x379b40:'integer'}

def call_sites(image):
    candidates={}
    for segment in image.segments:
        if segment[0] not in [1,0x61000010] or not segment[1]&1:continue
        data=image.read(segment[3],segment[5]);offset=data.find(b'\xe8')
        while offset>=0:
            if offset+5<=len(data):
                pc=segment[3]+offset;target=pc+5+struct.unpack_from('<i',data,offset+1)[0]
                if target in FACTORIES:candidates[pc]=target
            offset=data.find(b'\xe8',offset+1)
    functions=sorted({image.functions[bisect.bisect_right(image.functions,pc)-1] for pc in candidates});verified={}
    for function in functions:
        for instruction in image.instructions(function):
            if instruction.address in candidates and instruction.mnemonic=='call' and instruction.operands[0].type==X86_OP_IMM and instruction.operands[0].imm==candidates[instruction.address]:verified[instruction.address]=candidates[instruction.address]
    if set(verified)!=set(candidates):raise ValueError('A candidate property call is not an original decoded instruction')
    if len(verified)!=133:raise ValueError(('Property call coverage changed',len(verified)))
    return verified,functions

def recover(executable):
    image=Source(executable);sites,functions=call_sites(image);outputs={};calls={};allocations={}
    for function in functions:
        registers={'rbp':('stack',0),'rsp':('stack',-0x1000)};memory={};strings={}
        def address(instruction,operand):
            m=operand.mem
            if m.base==X86_REG_RIP:return ('data',instruction.address+instruction.size+m.disp)
            return None if m.index else plus(registers.get(image.register(m.base)),m.disp)
        def value(instruction,operand):
            if operand.type==X86_OP_IMM:return operand.imm
            if operand.type==X86_OP_REG:return registers.get(image.register(operand.reg))
            if operand.type==X86_OP_MEM:
                a=address(instruction,operand)
                return memory.get(a,('global',a[1]) if isinstance(a,tuple) and a[0]=='data' else None)
        def store(instruction,operand,v):
            if operand.type==X86_OP_REG:
                if isinstance(v,int) and operand.size<8:v&=(1<<(operand.size*8))-1
                registers[image.register(operand.reg)]=v
            elif operand.type==X86_OP_MEM:
                a=address(instruction,operand)
                if a is None:return
                memory[a]=v
                if isinstance(v,tuple) and v[0]=='allocation' and v[1] in allocations:v=('property',allocations[v[1]])
                if isinstance(a,tuple) and a[0]=='data' and isinstance(v,tuple) and v[0]=='property':outputs[a[1]]={'alias':hex(a[1]),'store_pc':hex(instruction.address),**calls[v[1]]}
        for instruction in image.instructions(function):
            op=instruction.operands;m=instruction.mnemonic
            if m in ['push','pop','leave','ret','nop'] or op and op[0].type==X86_OP_REG and image.register(op[0].reg) in ['rbp','rsp']:continue
            if m=='lea':store(instruction,op[0],address(instruction,op[1]));continue
            if m in ['mov','movabs','movzx','movsx','movsxd']:store(instruction,op[0],value(instruction,op[1]));continue
            if m=='xor' and op[0].type==op[1].type==X86_OP_REG and op[0].reg==op[1].reg:store(instruction,op[0],0);continue
            if m in ['add','sub']:
                a,b=value(instruction,op[0]),value(instruction,op[1]);store(instruction,op[0],plus(a,b if m=='add' else -b) if isinstance(b,int) else None);continue
            if m=='call':
                target=value(instruction,op[0]);args=[registers.get(r) for r in ARGUMENTS];dst,src,length=args[:3];result=None
                if target in [0xe6cfc0,0xe6cfd0]:result=('allocation',instruction.address)
                elif target==0x67840:
                    if dst is not None:strings[dst]=image.read(src[1],length*2).decode('utf-16le') if isinstance(src,tuple) and src[0]=='data' and isinstance(length,int) else None
                    result=dst
                elif target==0x67a10:
                    if dst is not None:strings[dst]=strings.get(src)
                    result=dst
                elif target in FACTORIES:
                    original_dst=dst
                    if target in [0xda5a0,0x379b40]:dst,src,length=args[1:4]
                    spec={'factory_pc':hex(instruction.address),'function':hex(function),'factory':hex(target),'type':FACTORIES[target],'name':strings.get(dst)}
                    if target in [0x37a180,0x379b40]:
                        for key,v in [('minimum',src),('maximum',length)]:spec[key]=((v+2147483648)&0xffffffff)-2147483648 if isinstance(v,int) else v
                    calls[instruction.address]=spec;result=('property',instruction.address)
                    if target in [0xda5a0,0x379b40] and isinstance(original_dst,tuple) and original_dst[0]=='allocation':allocations[original_dst[1]]=instruction.address
                for register in VOLATILE:registers[register]=None
                registers['rax']=result;continue
            if op and op[0].type==X86_OP_REG and m not in ['cmp','test'] and not m.startswith('j'):registers[image.register(op[0].reg)]=None
    if set(calls)!=set(sites):raise ValueError('Decoded property calls were not all traced')
    wrappers=[s for s in calls.values() if s['name']is None]
    if {s['factory_pc'] for s in wrappers}!={'0xdabcd','0x37a1ad'}:raise ValueError(('Unresolved literal property input',wrappers))
    properties=sorted(outputs.values(),key=lambda s:int(s['alias'],16))
    if len(properties)!=136 or any(s['name']is None or s['type']=='integer' and not isinstance(s['minimum'],int) for s in properties):raise ValueError('Incomplete scalar initializer constants')
    return {'properties':properties,'factory_calls':list(calls.values()),'generic_forwarders':wrappers}

if __name__=='__main__':
    parser=argparse.ArgumentParser(description=__doc__);parser.add_argument('--eboot',type=Path,required=True);parser.add_argument('--output',type=Path,required=True);a=parser.parse_args();data=recover(a.eboot);a.output.write_text(json.dumps(data,indent=2)+'\n');print(a.output.name,len(data['properties']),len(data['factory_calls']))
