#!/usr/bin/env python3
"""Capture both stored layers and the original primary-high-bit water shortcut."""
import argparse,gzip,hashlib,json,platform,struct
from pathlib import Path
from native_secondary_storage import SecondaryStorageOracle
parser=argparse.ArgumentParser(description=__doc__);parser.add_argument('--eboot',type=Path,required=True);parser.add_argument('--libc',type=Path,required=True);parser.add_argument('--output-dir',type=Path,required=True);parser.add_argument('--inputs',type=Path,default=Path(__file__).resolve().parents[3]/'src/test/resources/worldgen/ps4-compressed-sections.json.gz');a=parser.parse_args()
if platform.system()!='Linux' or platform.machine() not in ['x86_64','AMD64']:parser.error('Linux x86-64 and a C compiler are required')
expected={a.eboot:'c6089834fe1372854eed8f9acf2cb307f7009aaeb32ee303ce5bf6a1160786c8',a.libc:'1936f5700cc590877d9bf493c6a6bb7714e84003dfd6d74a7d77b1869f71d816',a.inputs:'f2df5d219243fc809c162b4f5052ca289cffbf08336d4421f7489905e3ee7eab'}
for path,digest in expected.items():
    if hashlib.sha256(path.read_bytes()).hexdigest()!=digest:parser.error(f'Unexpected capture input: {path}')
n=SecondaryStorageOracle(a.eboot,a.libc);cases=[]
# Reuse only explicit input bytes and sample coordinates. All previous output
# fields are discarded; both secondary results are newly executed CPU outputs.
for row in json.loads(gzip.decompress(a.inputs.read_bytes()))['cases']:cases.append(n.values(row))
for index in range(64):
    for mode in range(1,8):
        for pattern in [0,1]:
            for offset in [0,64]:
                for flag in ([0,0x1000] if mode==7 else [0x1000]):
                    headers=[0]*64;headers[index]=(mode<<13)|flag|offset
                    if mode==7:
                        primary=[([0,0x10,0x8000,0x8010,0xffff,0x90,0x7fff,0x70][(cell+pattern)%8]) for cell in range(64)]
                        secondary=[([0,0x90,0x70,0xffff][(cell+pattern)%4]) for cell in range(64)];payload=struct.pack('<128H',*(primary+secondary))
                    else:
                        size=1<<mode;payload=bytearray(size*2+mode*16)
                        palette=[((value*0x1f10)^(0x8000 if pattern else 0x70))&65535 for value in range(size)]
                        for i,value in enumerate(palette):struct.pack_into('<H',payload,i*2,value)
                        for cell in range(64):
                            for layer,selected in enumerate([cell&(size-1),(63-cell)&(size-1)]):
                                for plane in range(mode):
                                    if selected&(1<<plane):payload[size*2+layer*mode*8+plane*8+cell//8]|=0x80>>(cell&7)
                    section=(index%16);origin=[((index>>4)&3)*4-32,section*16+(index&3)*4,((index>>2)&3)*4+16]
                    cases.append(n.values({'section_y':section*16,'allocated':1,'section_offset':[0,1,31][index%3],'headers':headers,'patches':[[128+offset*4,payload.hex()]],'read_origin':origin}))
data={'repository':'https://github.com/Nejirus/mcps4','commit':'37e258085773f11599cf0ffe29119926769554b7','eboot_sha256':expected[a.eboot],'libc_sha256':expected[a.libc],'input_bytes_fixture_sha256':expected[a.inputs],'entrypoints':['0x35bc60','0x35bf10','0x2246a8..0x2246d0 / 0x2246de..0x2246ec'],
 'input_kind':'Original pure primary/secondary lookup bodies and bounded original secondary wrapper value-selection branch execute on supplied raw bytes. A host assembly ABI shim enters after TLS and returns at verified exits before TLS; no FS, locks, startup or full wrapper prologue/cleanup execute. All earlier explicit input bytes are reused without reading their expected outputs, plus new two-layer shared palettes/bitplanes/raw layouts and primary-high-bit cases. Each sample hash concatenates little-endian uint16 raw primary, stored secondary and wrapper-selected secondary in X/Z/Y order. Storage construction/mutation and world/state/physics integration remain outside this comparison. Input layouts include synthetic redundant/invalid states; no complete-game-chunk claim.','cases':cases}
a.output_dir.mkdir(parents=True,exist_ok=True);p=a.output_dir/'ps4-secondary-storage.json.gz';p.write_bytes(gzip.compress(json.dumps(data,separators=(',',':')).encode(),mtime=0));print(p.name,len(cases),sum(c['samples'] for c in cases),hashlib.sha256(p.read_bytes()).hexdigest(),flush=True)
