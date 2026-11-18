#!/usr/bin/env python3
"""Capture original fossils with controlled template geometry and chunk RNG."""
import argparse,gzip,hashlib,json,platform
from pathlib import Path
from native_fossils import FossilOracle

parser=argparse.ArgumentParser(description=__doc__);parser.add_argument('--eboot',type=Path,required=True);parser.add_argument('--libc',type=Path,required=True);parser.add_argument('--assets',type=Path,required=True);parser.add_argument('--output-dir',type=Path,required=True);args=parser.parse_args()
if platform.system()!='Linux' or platform.machine() not in ('x86_64','AMD64'):parser.error('Linux x86-64 is required')
expected={args.eboot:'c6089834fe1372854eed8f9acf2cb307f7009aaeb32ee303ce5bf6a1160786c8',args.libc:'1936f5700cc590877d9bf493c6a6bb7714e84003dfd6d74a7d77b1869f71d816'}
for path,digest in expected.items():
    if hashlib.sha256(path.read_bytes()).hexdigest()!=digest:parser.error(f'Unexpected mcps4 input: {path}')
oracle=FossilOracle(args.eboot,args.libc,args.assets);cases=[];random_cases=[]
seeds=[0,1,9,82958734,-1,-987654321,-9223372036854775808,9223372036854775807]
coords=[[0,0],[1,0],[0,1],[-1,1],[1,-1],[156,-157],[-157,156],[32767,-32768],[65535,-65536],[1875000,-1875000],[46340,46341],[-2147483648,2147483647],[2147483647,-2147483648]]
positions=[[0,0,0],[16,0,32],[-16,0,-32],[-1,0,1],[-2493,0,2501],[1536,64,-1537],[30000000,0,-30000000],[65535,1,-65536],[741455,0,-741455]]
for seed in seeds:
    for x,z in coords:
        for salt in [0,1,987234911,-1,-9223372036854775808,9223372036854775807]:random_cases.append(oracle.random_case(seed,x,z,salt))
    for position in positions:
        for profile in ['flat','zero','high','steps']:
            for draws in [0,1,3]:
                for order in [[0,1,2,3],[3,2,1,0]]:cases.append(oracle.fossil(seed,-seed-1,position,profile,draws,order))
    print('Original fossil seed',seed,'cases',len(cases),'chunk RNG',len(random_cases),flush=True)
data={'repository':'https://github.com/Nejirus/mcps4','commit':'37e258085773f11599cf0ffe29119926769554b7','eboot_sha256':expected[args.eboot],'libc_sha256':expected[args.libc],
      'entrypoints':['0x2f6ef0','0x40fd80','0x3eea80','0x17fe00','0x6c1d60','0x6c2ec0','0x6c2ef0','0x6c2f40','0x6c2f50','0x6c97e0','0x6c9850'],
      'input_kind':'Original fossil dispatcher, chunk RNG constructor, settings construction and template size/position helpers. Unmodified original template dimensions are supplied; template geometry callbacks consume the specified number of original nextLong values. Rotation registry storage/order, world heights and template-manager lookup are controlled. No template placed-block or whole-world parity claim.',
      'template_inputs':oracle.template_inputs,'random_cases':random_cases,'cases':cases}
args.output_dir.mkdir(parents=True,exist_ok=True);path=args.output_dir/'ps4-fossils.json.gz';path.write_bytes(gzip.compress(json.dumps(data,separators=(',',':')).encode(),mtime=0));print(path.name,len(cases),len(random_cases),hashlib.sha256(path.read_bytes()).hexdigest(),flush=True)
