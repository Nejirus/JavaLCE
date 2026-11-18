#!/usr/bin/env python3
"""Capture original aquatic feature kernels, state constructors and rules."""
import argparse,gzip,hashlib,json,platform
from pathlib import Path
from native_aquatics import AquaticOracle

parser=argparse.ArgumentParser(description=__doc__);parser.add_argument('--eboot',type=Path,required=True);parser.add_argument('--libc',type=Path,required=True);parser.add_argument('--output-dir',type=Path,required=True);args=parser.parse_args()
if platform.system()!='Linux' or platform.machine() not in ('x86_64','AMD64'):parser.error('Linux x86-64 is required')
expected={args.eboot:'c6089834fe1372854eed8f9acf2cb307f7009aaeb32ee303ce5bf6a1160786c8',args.libc:'1936f5700cc590877d9bf493c6a6bb7714e84003dfd6d74a7d77b1869f71d816'}
for path,digest in expected.items():
    if hashlib.sha256(path.read_bytes()).hexdigest()!=digest:parser.error(f'Unexpected mcps4 input: {path}')
oracle=AquaticOracle(args.eboot,args.libc);rules=[];cases=[]
ids=sorted({r['id'] for r in json.loads(Path(__file__).with_name('block-registry-native.json').read_text())['registrations']}|{0})
for tile in ids:
    for blocking in [False,True]:
        for meta in [0,8]:rules.append(oracle.rule('kelp',258,tile|(meta<<16),9,9,blocking,'all'))
        for flag in [False,True]:rules.append(oracle.rule('seagrass',270,tile,9,9,blocking,'all',flag=flag,skip=True))
        for meta in [0,8]:rules.append(oracle.rule('pickles',271,tile|(meta<<16),9,9,blocking,'all'))
for effective in [0,1,8,9]+[9|(m<<16) for m in [1,2,7,8,9,15]]:
    for placement in ['all','none']:
        for below in [0,1,258,44|(8<<16),213,88]:
            rules.append(oracle.rule('kelp',258,below,9,effective,True,placement))
            rules.append(oracle.rule('seagrass',270,below,9,effective,True,placement,skip=False))
for state in [270,270|(1<<16),270|(2<<16)]:
    for current in [0,9,270,270|(1<<16),270|(2<<16)]:
        for below in [0,1,270,270|(1<<16),270|(2<<16)]:
            for above in [0,9,270,270|(1<<16),270|(2<<16)]:
                for blocking in [False,True]:
                    for placement in ['all','none']:rules.append(oracle.rule('seagrass',state,below,current,9,blocking,placement,skip=True,above=above))
print('Original aquatic rules',len(rules),flush=True)
positions=[[0,64,0],[-2493,1,2501],[5,0,-7],[-9,49,11],[17,253,-23],[3,256,7]]
seeds=[0,1,9,82958734,-1,-987654321,-9223372036854775808,9223372036854775807]
for seed in seeds:
    for kind in ['kelp','seagrass','pickles']:
        for profile in ['deep','shallow','steps','air','column_kelp','column_pickle','ice_floor','magma','soul','glass','top_slabs','bottom_slabs','mixed','flowing','waterlogged']:
            for position in positions:cases.append(oracle.aquatic(kind,seed,position,profile))
        for profile in ['deep','mixed']:
            for placement in ['none','checker']:
                for position in positions[::2]:cases.append(oracle.aquatic(kind,seed,position,profile,placement))
        if kind!='seagrass':
            for position in positions:cases.append(oracle.aquatic(kind,seed,position,'deep',client=True))
    for busy in [False,True]:
        for reentry in [False,True]:
            for position in positions:cases.append(oracle.aquatic('seagrass',seed,position,'deep',busy=busy,reentry=reentry))
    print('Original aquatic seed',seed,'kernels',len(cases),flush=True)
for seed in range(64):cases.append(oracle.aquatic('seagrass',seed,[0,64,0],'deep'))
data={'repository':'https://github.com/Nejirus/mcps4','commit':'37e258085773f11599cf0ffe29119926769554b7','eboot_sha256':expected[args.eboot],'libc_sha256':expected[args.libc],
      'entrypoints':['0x3c64e0','0x3c6400','0x3c5370','0x3c59b0','0x3c5df0','0x5f5210','0x5f6730','0x5f58a0','0x5f5ae0','0x5f5710','0x5f8c60','0x5f6e60','0x5f7310','0x5f7400','0x352e90','0x5f7810','0x37a180','0x379b40'],
      'input_kind':'Original feature kernels and support rules, growth-state cache/default assignments, getData methods and integer-property constructors. Block base/StateDefinition/canonical property storage, material/solid-blocking/slab/byte10 inputs, Level effective-fluid boundaries, collision placement outcomes, client/reentry flags and successful writes are supplied. Sea-pickle default storage is supplied; its constructor does not overwrite it. No grouped startup, original full registry, notification/waterlogging effects or whole-world/save capture.',
      'property_counts':oracle.property_counts,'default_state_cases':oracle.default_cases,'data_cases':oracle.data_cases,'kelp_cached_states':oracle.kelp_cached,'rules':rules,'cases':cases}
args.output_dir.mkdir(parents=True,exist_ok=True);path=args.output_dir/'ps4-aquatics.json.gz';path.write_bytes(gzip.compress(json.dumps(data,separators=(',',':')).encode(),mtime=0));print(path.name,len(rules),len(cases),hashlib.sha256(path.read_bytes()).hexdigest(),flush=True)
