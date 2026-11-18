#!/usr/bin/env python3
"""Capture all numeric header modes in the native section-empty routine."""
import argparse,gzip,hashlib,json,platform,struct
from pathlib import Path
from native_compressed_sections import CompressedSectionsOracle
parser=argparse.ArgumentParser(description=__doc__);parser.add_argument('--eboot',type=Path,required=True);parser.add_argument('--libc',type=Path,required=True);parser.add_argument('--output-dir',type=Path,required=True);a=parser.parse_args()
if platform.system()!='Linux' or platform.machine() not in ['x86_64','AMD64']:parser.error('Linux x86-64 is required')
expected={a.eboot:'c6089834fe1372854eed8f9acf2cb307f7009aaeb32ee303ce5bf6a1160786c8',a.libc:'1936f5700cc590877d9bf493c6a6bb7714e84003dfd6d74a7d77b1869f71d816'}
for path,digest in expected.items():
    if hashlib.sha256(path.read_bytes()).hexdigest()!=digest:parser.error(f'Unexpected mcps4 input: {path}')
n=CompressedSectionsOracle(a.eboot,a.libc);cases=[]
def capture(index,headers,patches,has_output,allocated=1):
    y=[-15,-1,0,1,15,16,63,112,127,128,239,240,255][index%13];offset=[0,1,31][index%3]
    cases.append(n.empty(y,allocated,offset,headers,patches,has_output,index))
for index in range(64):
    for header in [0,1,15,4095,0x1000,0x1fff]:
        headers=[0]*64;headers[index]=header
        for output in [False,True]:capture(index,headers,[],output)
    for offset in [0,31,64,4095]:
        for word in [0,1,31,63]:
            headers=[0]*64;headers[index]=0xe000|offset
            payload=bytearray(128);struct.pack_into('<H',payload,word*2,[1,16,4095,65535][word%4])
            for output in [False,True]:capture(index,headers,[[128+offset*4,payload.hex()]],output)
    for mode in range(1,7):
        size=1<<mode;selected=sum(1<<plane for plane in range(mode) if plane%2==0)
        for offset in [0,1,64,4095]:
            for profile in ['zero','ones','selected','unused','mixed','near_uniform','alternating_zero']:
                headers=[0]*64;headers[index]=(mode<<13)|offset
                payload=bytearray(size*2+mode*8)
                for plane in range(mode):
                    value=255 if profile=='ones' or (profile in ['selected','unused','near_uniform','alternating_zero'] and selected&(1<<plane)) else 0
                    payload[size*2+plane*8:size*2+(plane+1)*8]=bytes([value])*8
                if profile=='selected':struct.pack_into('<H',payload,selected*2,16)
                if profile=='unused':struct.pack_into('<H',payload,((selected+1)%size)*2,16)
                if profile=='mixed':payload[size*2+7]=1
                if profile=='near_uniform':payload[size*2+7]=254
                for output in [False,True]:capture(index,headers,[[128+offset*4,payload.hex()]],output)
    # Allocation flag wins without reading even explicitly nonempty headers.
    capture(index,[1]*64,[],True,0)
for first in range(64):
    for second in [0,3,12,15,48,51,60,63]:
        headers=[0]*64;headers[first]=1;headers[second]=15;capture(first,headers,[],True)
for index in range(64):
    for mode in range(1,8):
        headers=[0]*64;headers[index]=(mode<<13)|0x1000|64
        if mode==7:
            payload=bytearray(128);struct.pack_into('<H',payload,126,0xffff)
        else:
            size=1<<mode;payload=bytearray(size*2+mode*8);struct.pack_into('<H',payload,2,0xffff);payload[size*2:size*2+8]=b'\xff'*8
        for output in [False,True]:capture(index,headers,[[128+64*4,payload.hex()]],output)
data={'repository':'https://github.com/Nejirus/mcps4','commit':'37e258085773f11599cf0ffe29119926769554b7','eboot_sha256':expected[a.eboot],'libc_sha256':expected[a.libc],'entrypoints':['0x35a090','0x35bc60'],
 'input_kind':'Complete original pure compressed-section emptiness and packed-value lookup routines execute with explicit allocation flags, section offsets, 64 numeric headers, payload bytes and optional output pointers. All eight header modes, each slice/column, raw-word positions, uniform/mixed bitplanes, selected/unused palette words, offset-zero/large offsets and section-base shifts are compared. Every case also reads all 64 cells of its selected subcube at signed/wrapped X/Z coordinates; read SHA256 concatenates little-endian uint16 returned value and uint16 output byte in X/Z/Y loop order, resetting the output to 0xa5 for each call. Source synthetic byte layouts include redundant palettes/mixed index patterns; they are inputs, not claimed game-produced compressed chunks. Empty/height/value/constant outputs are native outputs. Locking/TLS wrappers 0x223f10/0x224540, compression construction/mutation and full world storage are not executed. 0x223f10 third parameter is an optional output pointer, not a layer index. The separate 0x224540 return mask 0x7fff is outside this raw lookup comparison. No Java decoding algorithm supplies native outputs.','cases':cases}
a.output_dir.mkdir(parents=True,exist_ok=True);p=a.output_dir/'ps4-compressed-sections.json.gz';p.write_bytes(gzip.compress(json.dumps(data,separators=(',',':')).encode(),mtime=0));print(p.name,len(cases),sum(c['read_samples'] for c in cases),hashlib.sha256(p.read_bytes()).hexdigest(),flush=True)
