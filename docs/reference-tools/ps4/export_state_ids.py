#!/usr/bin/env python3
"""Capture original canonical-table fallback and item codecs with stub states."""
import argparse,gzip,hashlib,json,platform
from pathlib import Path
from native_state_ids import StateIdsOracle
parser=argparse.ArgumentParser(description=__doc__);parser.add_argument('--eboot',type=Path,required=True);parser.add_argument('--libc',type=Path,required=True);parser.add_argument('--output-dir',type=Path,required=True);a=parser.parse_args()
if platform.system()!='Linux' or platform.machine() not in ['x86_64','AMD64']:parser.error('Linux x86-64 is required')
expected={a.eboot:'c6089834fe1372854eed8f9acf2cb307f7009aaeb32ee303ce5bf6a1160786c8',a.libc:'1936f5700cc590877d9bf493c6a6bb7714e84003dfd6d74a7d77b1869f71d816'}
for path,digest in expected.items():
    if hashlib.sha256(path.read_bytes()).hexdigest()!=digest:parser.error(f'Unexpected mcps4 input: {path}')
n=StateIdsOracle(a.eboot,a.libc);contexts=[]
for entries in [[],[{'id':value,'slot':(value^0xa55a)&4095,'data':(value^15)&15} for value in [0,1,15,16,255,256,4095,5855,0x400f,0x4010,65535]]]:
    n.configure_cache(entries);contexts.append({'cache':entries,'ranges':n.ranges()})
encodings=[n.encode(id,data) for id in [0,1,255,256,257,365,1024,1025,1133,4095,-1,-2147483648,2147483647] for data in [0,1,15,16,31,255,-1,-2147483648,2147483647]]
data={'repository':'https://github.com/Nejirus/mcps4','commit':'37e258085773f11599cf0ffe29119926769554b7','eboot_sha256':expected[a.eboot],'libc_sha256':expected[a.libc],'entrypoints':['0x91c30','0xb84f0','0x91bc0','0x91c50','0x91ba0'],
 'input_kind':'Original table-first state-ID map lookup, fallback selection, item decode/encode and block-base encoding execute. Registry IDs, state ownership, metadata converters and cache records are supplied tagged stubs; actual canonical property construction and registry startup are not executed. Two uint16 lookup contexts cover every value for both layouts, with output/event SHA256 over little-endian signed-int32 triples. Outputs=(recordKind,blockSlot,data), recordKind1=cached and 0=converter. Events=(0,slot,data) converter, (1,slot,registryID) ID lookup, (2,slot,data) metadata getter. Input cache is deliberately enlarged to 65536 slots and the block table to 4096 dummy slots for safe selector coverage; original startup allocates 5856 state slots and 366 block slots. Unchecked source table indexing outside its real allocation is not a full-world safety/parity claim. Encodings include explicit signed/overflow stub fields, not claimed valid registry entries. No Java selector algorithm supplies native outputs.','contexts':contexts,'encodings':encodings}
a.output_dir.mkdir(parents=True,exist_ok=True);p=a.output_dir/'ps4-state-ids.json.gz';p.write_bytes(gzip.compress(json.dumps(data,separators=(',',':')).encode(),mtime=0));print(p.name,len(contexts)*65536*2,len(encodings),hashlib.sha256(p.read_bytes()).hexdigest(),flush=True)
