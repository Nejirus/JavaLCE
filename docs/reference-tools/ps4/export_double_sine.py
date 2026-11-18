#!/usr/bin/env python3
"""Capture original libc double sine bits for iceberg argument ranges."""
import argparse,gzip,hashlib,json,platform,random,struct
from pathlib import Path
from native_carvers import Oracle,D

parser=argparse.ArgumentParser(description=__doc__);parser.add_argument('--eboot',type=Path,required=True);parser.add_argument('--libc',type=Path,required=True);parser.add_argument('--output-dir',type=Path,required=True);args=parser.parse_args()
if platform.system()!='Linux' or platform.machine() not in ('x86_64','AMD64'):parser.error('Linux x86-64 is required')
expected={args.eboot:'c6089834fe1372854eed8f9acf2cb307f7009aaeb32ee303ce5bf6a1160786c8',args.libc:'1936f5700cc590877d9bf493c6a6bb7714e84003dfd6d74a7d77b1869f71d816'}
for path,digest in expected.items():
    if hashlib.sha256(path.read_bytes()).hexdigest()!=digest:parser.error(f'Unexpected mcps4 input: {path}')
oracle=Oracle(args.eboot,args.libc);sine=oracle.lib.function(0xc6410,D,[D]);samples=[0.0,-0.0,1e-300,-1e-300,1e-20,-1e-20]
for high in [0x3e4fffff,0x3e500000,0x3fe921fb,0x3fe921fc,0x4002d97c,0x4002d97d,0x400f6a7a,0x400f6a7b,0x4012d97c,0x4015fdbc,0x4015fdbd,0x401921fb,0x401c463b,0x401c463c]:
    for offset in [-2,-1,0,1,2]:
        value=struct.unpack('<d',struct.pack('<Q',(high<<32)+offset))[0];samples.extend([value,-value])
rng=random.Random(0x641017)
for i in range(100000):samples.append(rng.uniform(-.8,.8) if i%3==0 else rng.uniform(-8,8) if i%3==1 else rng.uniform(-8192,8192))
raw=bytearray()
for value in samples:raw.extend(struct.pack('<dd',value,sine(value)))
args.output_dir.mkdir(parents=True,exist_ok=True);path=args.output_dir/'ps4-double-sine.f64.gz';path.write_bytes(gzip.compress(raw,mtime=0));print(path.name,len(samples),hashlib.sha256(path.read_bytes()).hexdigest(),flush=True)
