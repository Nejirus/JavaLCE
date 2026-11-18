#!/usr/bin/env python3
"""Capture original biome-specific decoration call order and RNG flow."""
import argparse,gzip,hashlib,json,math,platform
from pathlib import Path
from native_biome_wrappers import BiomeWrapperOracle,P,U,I

parser=argparse.ArgumentParser(description=__doc__);parser.add_argument('--eboot',type=Path,required=True);parser.add_argument('--libc',type=Path,required=True);parser.add_argument('--output-dir',type=Path,required=True);args=parser.parse_args()
if platform.system()!='Linux' or platform.machine() not in ('x86_64','AMD64'):parser.error('Linux x86-64 is required')
expected={args.eboot:'c6089834fe1372854eed8f9acf2cb307f7009aaeb32ee303ce5bf6a1160786c8',args.libc:'1936f5700cc590877d9bf493c6a6bb7714e84003dfd6d74a7d77b1869f71d816'}
for path,digest in expected.items():
    if hashlib.sha256(path.read_bytes()).hexdigest()!=digest:parser.error(f'Unexpected mcps4 input: {path}')
oracle=BiomeWrapperOracle(args.eboot,args.libc)
modes=[*[('FOREST',v,.7) for v in range(4)],('SAVANNA',0,1.2),('MUTATED_SAVANNA',0,1.1),('PLAINS',0,.8),('PLAINS',1,.8),('SWAMP',0,.8),
       ('TAIGA',0,.25),('TAIGA',0,-.5),('TAIGA',1,.3),('TAIGA',2,.25),('JUNGLE',0,.95),('JUNGLE',1,.95),('BAMBOO_JUNGLE',0,.95),('DESERT',0,2.),('HILLS',0,.2)]
cases=[]
for kind,variant,temperature in modes:
    for seed in [0,1,9,82958734,-1,-987654321,-9223372036854775808,9223372036854775807]:
        for origin in [[0,0,0],[-2496,0,2496],[96,17,-112]]:
            for height in ['flat','steps','zero','one']:
                for outcome in ['never','mixed','always']:cases.append(oracle.decorate(kind,variant,temperature,seed,origin,height,outcome))
    print('Original biome wrapper',kind,variant,temperature,'cases',len(cases),flush=True)
for seed in [0,1,9,-1]:
    for outcome in ['never','mixed','always']:
        for noise in [math.nextafter(-.8,-math.inf),-.8,math.nextafter(-.8,math.inf)]:cases.append(oracle.decorate('PLAINS',1,.8,seed,[-80,17,79],'steps',outcome,noise))
        for noise in [-2.,-.3,0.,.1,.7,2.]:cases.append(oracle.decorate('BAMBOO_JUNGLE',0,.95,seed,[-81,17,79],'steps',outcome,noise))
# Inputs selected to cover the rare original branches after the controlled
# base callback draw. The original code below verifies each branch again.
rare={'DESERT_FOSSIL':2,'SWAMP_FOSSIL':38,'DESERT_WELL':3720,'DESERT_WELL_AND_FOSSIL':544488}
for name,seed in rare.items():
    for outcome in ['never','mixed','always']:
        c=oracle.decorate('SWAMP' if name.startswith('SWAMP') else 'DESERT',0,.8,seed,[96,17,-112],'flat',outcome)
        features={e[1] for e in c['events'] if e[0]=='feature'}
        required={'DESERT_WELL','FOSSIL'} if name.endswith('WELL_AND_FOSSIL') else {'DESERT_WELL'} if name.endswith('WELL') else {'FOSSIL'}
        if not required<=features:raise RuntimeError(f'Original rare branch missing: {name}')
        cases.append(c)
data={'repository':'https://github.com/Nejirus/mcps4','commit':'37e258085773f11599cf0ffe29119926769554b7','eboot_sha256':expected[args.eboot],'libc_sha256':expected[args.libc],
      'entrypoints':[hex(pc) for pc in oracle.WRAPPERS.values()]+['0x2f6b50','0x2f6ce0','0x85990'],
      'input_kind':'Original full biome-specific wrappers with original tree selectors/constructors and native biome noise. Controlled feature geometry/base callbacks consume one original nextLong per call; the RNG-free well consumes none. Heights, feature outcomes, initial flower/grass counts, temperature and variant fields are supplied. Not complete population, missing feature kernels or whole-world captures.',
      'double_plant_enum_inputs':oracle.double_inputs,'rare_seeds':rare,'cases':cases}
args.output_dir.mkdir(parents=True,exist_ok=True);path=args.output_dir/'ps4-biome-wrappers.json.gz';path.write_bytes(gzip.compress(json.dumps(data,separators=(',',':')).encode(),mtime=0));print(path.name,len(cases),sum(len(c['events']) for c in cases),hashlib.sha256(path.read_bytes()).hexdigest(),rare,flush=True)
