#!/usr/bin/env python3
"""Capture original write-prefix precipitation-height invalidation."""
import argparse,gzip,hashlib,json,platform
from pathlib import Path
from native_height_invalidation import HeightInvalidationOracle
parser=argparse.ArgumentParser(description=__doc__);parser.add_argument('--eboot',type=Path,required=True);parser.add_argument('--libc',type=Path,required=True);parser.add_argument('--output-dir',type=Path,required=True);a=parser.parse_args()
if platform.system()!='Linux' or platform.machine() not in ['x86_64','AMD64']:parser.error('Linux x86-64 is required')
expected={a.eboot:'c6089834fe1372854eed8f9acf2cb307f7009aaeb32ee303ce5bf6a1160786c8',a.libc:'1936f5700cc590877d9bf493c6a6bb7714e84003dfd6d74a7d77b1869f71d816'}
for path,digest in expected.items():
    if hashlib.sha256(path.read_bytes()).hexdigest()!=digest:parser.error(f'Unexpected mcps4 input: {path}')
n=HeightInvalidationOracle(a.eboot,a.libc);cases=[]
for cached in range(256):
    for y in sorted({-2147483648,-256,-2,-1,0,1,15,63,64,127,128,253,254,255,256,2147483647,cached-2,cached-1,cached}):
        for x,z in [(0,0),(-1,-17),(15,16),(-2493,2501),(2147483647,-2147483648)]:
            for fast in [False,True]:cases.append(n.invalidate([x,y,z],cached,fast))
data={'repository':'https://github.com/Nejirus/mcps4','commit':'37e258085773f11599cf0ffe29119926769554b7','eboot_sha256':expected[a.eboot],'libc_sha256':expected[a.libc],'entrypoints':['0x40af70'],
 'input_kind':'Original complete LevelChunk.setTile entry executes cache invalidation before two controlled early-return paths. Fast writes use an explicit null compressed-storage pointer and return true; ordinary unchanged voxel writes read supplied stone/meta0 and a null block-entity reference and return false. Neither path executes compressed storage writes, platform TLS/locks, lighting or neighbor physics. All 256 initial cache bytes, write-height boundaries, signed coordinate masks and complete cache hashes are compared. Out-of-range Y cases are direct chunk helper inputs; the separate world wrapper rejects those heights. Not a full write or world integration capture.','cases':cases}
a.output_dir.mkdir(parents=True,exist_ok=True);p=a.output_dir/'ps4-height-invalidation.json.gz';p.write_bytes(gzip.compress(json.dumps(data,separators=(',',':')).encode(),mtime=0));print(p.name,len(cases),hashlib.sha256(p.read_bytes()).hexdigest(),flush=True)
