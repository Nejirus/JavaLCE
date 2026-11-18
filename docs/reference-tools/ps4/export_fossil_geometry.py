#!/usr/bin/env python3
"""Capture original fossil dispatcher AND original template geometry/processor."""
import argparse,gzip,hashlib,json,platform
from pathlib import Path
from native_fossil_geometry import FossilGeometryOracle

parser=argparse.ArgumentParser(description=__doc__);parser.add_argument('--eboot',type=Path,required=True);parser.add_argument('--libc',type=Path,required=True);parser.add_argument('--assets',type=Path,required=True);parser.add_argument('--output-dir',type=Path,required=True);args=parser.parse_args()
if platform.system()!='Linux' or platform.machine() not in ('x86_64','AMD64'):parser.error('Linux x86-64 is required')
expected={args.eboot:'c6089834fe1372854eed8f9acf2cb307f7009aaeb32ee303ce5bf6a1160786c8',args.libc:'1936f5700cc590877d9bf493c6a6bb7714e84003dfd6d74a7d77b1869f71d816'}
for path,digest in expected.items():
    if hashlib.sha256(path.read_bytes()).hexdigest()!=digest:parser.error(f'Unexpected mcps4 input: {path}')
oracle=FossilGeometryOracle(args.eboot,args.libc,args.assets);cases=[]
positions=[[0,0,0],[16,0,32],[-16,0,-32],[-1,0,1],[-2493,0,2501],[1536,64,-1537],[30000000,0,-30000000],[65535,1,-65536],[741455,0,-741455]]
for seed in [0,1,9,82958734,-1,-987654321,-9223372036854775808,9223372036854775807]:
    for position in positions:
        for heights in ['flat','zero','high','steps']:
            for order in [[0,1,2,3],[3,2,1,0]]:
                for voxels in ['stone','air','water','mixed']:cases.append(oracle.geometry(seed,-seed-1,position,heights,order,voxels))
    print('Original fossil geometry seed',seed,'cases',len(cases),flush=True)
data={'repository':'https://github.com/Nejirus/mcps4','commit':'37e258085773f11599cf0ffe29119926769554b7','eboot_sha256':expected[args.eboot],'libc_sha256':expected[args.libc],
      'entrypoints':['0x2f6ef0','0x40fd80','0x6c92f0','0x6c8640','0x6c2390','0x6c8410','0x6c9730','0x6fa880','0x6ff170','0x6ff5e0','0x6ff840','0x5c19c0','0x5c1b90'],
      'input_kind':'Original fossil dispatcher, original template block sort, geometry and integrity processor, bone rotation and metadata. Unmodified NBT block order, palette, dimensions and positions are supplied to original template storage, bypassing the original NBT loader; its original sort then executes before placement. Rotation enum storage/order, world height, canonical properties, block registry lookup, loaded-column status, successful voxel writes and neighbor-notification effects are controlled. No original save or complete world comparison.',
      'template_inputs':oracle.template_inputs,'palette_inputs':oracle.palette_inputs,'bone_axis_data':oracle.axis_data,'cases':cases}
args.output_dir.mkdir(parents=True,exist_ok=True);path=args.output_dir/'ps4-fossil-geometry.json.gz';path.write_bytes(gzip.compress(json.dumps(data,separators=(',',':')).encode(),mtime=0));print(path.name,len(cases),hashlib.sha256(path.read_bytes()).hexdigest(),flush=True)
