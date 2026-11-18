#!/usr/bin/env python3
"""Capture original lava ignition alone and inside the original liquid tick."""
import argparse,gzip,hashlib,json,platform
from pathlib import Path
from native_lava_fire import LavaFireOracle

parser=argparse.ArgumentParser(description=__doc__);parser.add_argument('--eboot',type=Path,required=True);parser.add_argument('--libc',type=Path,required=True);parser.add_argument('--output-dir',type=Path,required=True);args=parser.parse_args()
if platform.system()!='Linux' or platform.machine() not in ('x86_64','AMD64'):parser.error('Linux x86-64 is required')
expected={args.eboot:'c6089834fe1372854eed8f9acf2cb307f7009aaeb32ee303ce5bf6a1160786c8',args.libc:'1936f5700cc590877d9bf493c6a6bb7714e84003dfd6d74a7d77b1869f71d816'}
for path,digest in expected.items():
    if hashlib.sha256(path.read_bytes()).hexdigest()!=digest:parser.error(f'Unexpected mcps4 input: {path}')
oracle=LavaFireOracle(args.eboot,args.libc);cases=[]
positions=[[0,64,0],[-2493,1,2501],[-1,0,1],[16,255,-17],[511,256,-512],[5,257,-9],[3,2,7],[-9,127,11]]
for seed in [0,1,9,82958734,-1,-987654321,-9223372036854775808,9223372036854775807]:
    for position in positions:
        for profile in ['air','wood_plane','metadata_wood','blocked_above','leaf_shell','mixed']:
            for enabled in [False,True]:
                for loaded in ['all','none','alternating']:
                    for integrated in [False,True]:
                        r=oracle.fire(seed,position,profile,enabled,loaded,integrated)
                        for key,value in {'kind':'lava','level':0,'primary':[],'secondary':[],'chunk_count':64,'hot_step':False,'ultra_warm':False,'closed_faces':False,'drop_plants':False,'immediate':False,'replay':False}.items():r.setdefault(key,value)
                        events=r.pop('events');r['event_count']=len(events);r['event_sha256']=hashlib.sha256(json.dumps(events,separators=(',',':')).encode()).hexdigest();r['effects']=[e for e in events if e[0] not in ['primary','secondary','pair','face','chunk']];r['event_prefix']=events[:12];cases.append(r)
    print('Original lava ignition seed',seed,'cases',len(cases),flush=True)
data={'repository':'https://github.com/Nejirus/mcps4','commit':'37e258085773f11599cf0ffe29119926769554b7','eboot_sha256':expected[args.eboot],'libc_sha256':expected[args.libc],
      'entrypoints':['0x41c980','0x221e90','0x2215c0'],
      'input_kind':'Original lava ignition helper alone and inside the original dynamic-liquid tick. Game rule, block defaults, flammable material identities, loaded-chunk results, six direction-vector storage and voxel contexts are supplied. Original offset draws, unsigned Y check, probe order, first-air-neighbor writes and following RNG execute. Flow classifications, notification/scheduling/fizz/drop effects remain controlled, feature15=false and writes succeed. Not whole-world fire simulation or original full block-registry initialization.',
      'default_inputs':oracle.default_inputs,'cases':cases}
args.output_dir.mkdir(parents=True,exist_ok=True);path=args.output_dir/'ps4-lava-fire.json.gz';path.write_bytes(gzip.compress(json.dumps(data,separators=(',',':')).encode(),mtime=0));print(path.name,len(cases),hashlib.sha256(path.read_bytes()).hexdigest(),flush=True)
