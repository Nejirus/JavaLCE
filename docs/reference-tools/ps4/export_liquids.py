#!/usr/bin/env python3
"""Capture original two-layer fluid flow, side probes, RNG and bounded tick queues."""
import argparse,gzip,hashlib,json,platform
from pathlib import Path
from native_liquids import LiquidOracle

parser=argparse.ArgumentParser(description=__doc__);parser.add_argument('--eboot',type=Path,required=True);parser.add_argument('--libc',type=Path,required=True);parser.add_argument('--output-dir',type=Path,required=True);args=parser.parse_args()
if platform.system()!='Linux' or platform.machine() not in ('x86_64','AMD64'):parser.error('Linux x86-64 is required')
expected={args.eboot:'c6089834fe1372854eed8f9acf2cb307f7009aaeb32ee303ce5bf6a1160786c8',args.libc:'1936f5700cc590877d9bf493c6a6bb7714e84003dfd6d74a7d77b1869f71d816'}
for path,digest in expected.items():
    if hashlib.sha256(path.read_bytes()).hexdigest()!=digest:parser.error(f'Unexpected mcps4 input: {path}')
oracle=LiquidOracle(args.eboot,args.libc);cases=[]
seeds=[0,1,9,82958734,-1,-987654321,-9223372036854775808,9223372036854775807]
positions=[[0,64,0],[-513,32,511],[-512,0,-512],[511,127,511],[512,254,-513],[431,1,-432]]
def capture(**kwargs):
    result=oracle.tick(**kwargs);events=result.pop('events');result['event_count']=len(events);result['event_sha256']=hashlib.sha256(json.dumps(events,separators=(',',':')).encode()).hexdigest()
    result['effects']=[e for e in events if e[0] not in ['primary','secondary','pair','face']];result['event_prefix']=events[:12];cases.append(result)
for seed in seeds:
    for kind in ['water','lava']:
        for pos in positions:
            for profile in ['empty','floor']:
                for level in [0,1,7,8,15]:
                    for closed in [False,True]:
                        for hot,warm in ([(False,False)] if kind=='water' else [(False,False),(False,True),(True,False),(True,True)]):
                            capture(kind=kind,seed=seed,position=pos,level=level,profile=profile,closed_faces=closed,hot_step=hot,ultra_warm=warm,chunk_count=54 if pos==positions[-1] else 64)
        tile=8 if kind=='water' else 10;x,y,z=0,64,0
        contexts=[
            ('one_source',1,[[0,64,-1,tile]],[]),
            ('two_sources',3,[[0,64,-1,tile],[1,64,0,tile]],[]),
            ('above_source',3,[[0,64,-1,tile],[0,65,0,tile]],[]),
            ('above_fall',2,[[0,65,0,tile|(8<<16)]],[]),
            ('slowdown',1,[[0,64,-1,tile|(4<<16)]],[]),
            ('water_below',0,[[0,63,0,9]],[]),
            ('flow_below',0,[[0,63,0,8|(6<<16)]],[]),
            ('lava_below',0,[[0,63,0,11]],[]),
            ('ice_below',0,[[0,63,0,79]],[]),
            ('flower_below',0,[[0,63,0,37]],[]),
            ('stale_air',2,[[0,64,0,0]],[]),
            ('waterlogged',0,[[0,64,0,20]],[[0,64,0,tile]]),
            ('waterlogged_decay',3,[[0,64,0,20]],[[0,64,0,tile|(3<<16)]]),
            ('side_hole',0,[[-2,63,0,0]],[]),
            ('side_plant',0,[[-1,64,0,31|(1<<16)]],[]),
            ('below_secondary',0,[[0,63,0,20]],[[0,63,0,9]])]
        for name,level,primary,secondary in contexts:
            for profile in ['empty','floor']:
                for drops in [False,True]:
                    capture(kind=kind,seed=seed,position=[x,y,z],level=level,profile=profile,primary=primary,secondary=secondary,drop_plants=drops)
                    cases[-1]['context']=name
        for level in [0,1]:capture(kind=kind,seed=seed,position=[x,y,z],level=level,profile='empty',immediate=True,replay=True)
    print('Original liquid seed',seed,'cases',len(cases),flush=True)
data={'repository':'https://github.com/Nejirus/mcps4','commit':'37e258085773f11599cf0ffe29119926769554b7','eboot_sha256':expected[args.eboot],'libc_sha256':expected[args.libc],
      'entrypoints':['0x221e90','0x2213a0','0x2215c0','0x221300','0x222050','0x222290','0x2223e0','0x222600','0x41a0d0','0x41ae70','0x41c4b0','0x41c550','0x41c650','0x41c930','0x3fc910'],
      'input_kind':'Original fluid tick, primary/secondary reads and writes, slope search, state conversion, metadata, RNG and immediate-update queue. Canonical state/property/registry/deque-proxy storage, material/face/drop classifiers, dimension flags and successful voxel APIs are supplied. Fire game rule and feature15 are false; world drop/notification/scheduling/fizz effects are observers. Replay cases request original reentrant ticks from the schedule callback, exercising the original 100-update limit. Native state-code inputs use IDs <=255. Not full block registry, fluid-world integration or whole-world captures.',
      'cases':cases}
args.output_dir.mkdir(parents=True,exist_ok=True);path=args.output_dir/'ps4-liquids.json.gz';path.write_bytes(gzip.compress(json.dumps(data,separators=(',',':')).encode(),mtime=0));print(path.name,len(cases),hashlib.sha256(path.read_bytes()).hexdigest(),flush=True)
