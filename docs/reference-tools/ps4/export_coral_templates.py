#!/usr/bin/env python3
"""Capture complete native crust templates and dispatcher/marker decoration."""
import argparse,gzip,hashlib,json,platform
from pathlib import Path
from native_coral_templates import CoralTemplateOracle
parser=argparse.ArgumentParser(description=__doc__);parser.add_argument('--eboot',type=Path,required=True);parser.add_argument('--libc',type=Path,required=True);parser.add_argument('--assets',type=Path,required=True);parser.add_argument('--output-dir',type=Path,required=True);a=parser.parse_args()
if platform.system()!='Linux' or platform.machine() not in ('x86_64','AMD64'):parser.error('Linux x86-64 is required')
expected={a.eboot:'c6089834fe1372854eed8f9acf2cb307f7009aaeb32ee303ce5bf6a1160786c8',a.libc:'1936f5700cc590877d9bf493c6a6bb7714e84003dfd6d74a7d77b1869f71d816'}
for p,d in expected.items():
 if hashlib.sha256(p.read_bytes()).hexdigest()!=d:parser.error(f'Unexpected mcps4 input: {p}')
n=CoralTemplateOracle(a.eboot,a.libc,a.assets);contexts=[];cases=[]
for context,(tick,order) in enumerate([(0,list(range(177,188))),(123456789000000,list(range(187,176,-1)))]):
 contexts.append(n.load_templates(tick,order))
 for id in range(177,188):
  for rotation in range(4):
   for position in [[0,49,0],[-2493,50,2501],[15,254,-17],[2147483000,-1,-2147483001]]:
    for columns,empty in [('all','none'),('none','none'),('checker','checker')]:
     cases.append({'context':context,**n.geometry(id,position,rotation,'crust_floor',column_mode=columns,empty_mode=empty)})
 # Paired settings clocks: integrity one has no draws from either template RNG.
 for counter,frequency in [(0,1000000000),(1,1000000),(987654321234567,1000000000)]:
  for id in [177,182,187]:
   for writes in ['none','checker']:
    cases.append({'context':context,**n.geometry(id,[17,49,-23],3,'crust_steps',write_mode=writes,counter=counter,frequency=frequency)})
 for seed in list(range(24))+[-1,-9223372036854775808,9223372036854775807,82958734]:
  for profile in ['crust_floor','crust_steps','crust_dead','crust_live','crust_air','crust_shallow','crust_deep']:
   for writes in ['all','checker']:
    cases.append({'context':context,**n.complete_crust(seed,[-2493,45,2501],profile,[0,1,2,3] if context==0 else [3,2,1,0],write_mode=writes)})
 for seed in range(8):
  for columns,empty in [('none','none'),('checker','checker'),('all','all')]:
   cases.append({'context':context,**n.complete_crust(seed,[15,45,-17],'crust_floor',column_mode=columns,empty_mode=empty)})
 cases.append({'context':context,**n.complete_crust(1,[0,45,0],'crust_floor',client=True)})
 print('Original complete crust context',context,'cases',len(cases),flush=True)
data={'repository':'https://github.com/Nejirus/mcps4','commit':'37e258085773f11599cf0ffe29119926769554b7','eboot_sha256':expected[a.eboot],'libc_sha256':expected[a.libc],
 'entrypoints':['0x1c7650','0x6c5060','0x6c8640','0x6fa880','0x6c2390','0x6c8410','0x6c25d0','0x6c3080','0x6c50e0','0x6ff170','0x6ff5e0','0x6ff840','0x1c8940','0x1c8bd0','0x1c9430','0x1c95e0','0x1c73d0'],
 'input_kind':'Unmodified native crust dispatcher, template engine/integrity processor, marker collector/native hash map and coral marker decoration execute in a shared mutable voxel world. Eleven hash-checked unchanged assets supply NBT fields/order/dimensions. Canonical palette storage is supplied; legacy dead_coral conversion executes the original parser using the original clock-seeded Mth Random. Stone/sand mappings and ignored structure-block metadata zero are controlled inputs, not a full registry/NBT loader capture. Cache load order, rotation/mirror enum storage/order, OS counter/frequency returns, materials/properties, column/chunk availability, entity lookup absence, write outcomes and neighbor-notification effects are explicit inputs. The native default Random constructor is restored from original bytes after the inherited tree harness bindings. Original clocks execute only supplied callbacks. No complete game initialization, world physics or whole-world save comparison.','templates':n.template_inputs,'contexts':contexts,'cases':cases}
a.output_dir.mkdir(parents=True,exist_ok=True);p=a.output_dir/'ps4-coral-templates.json.gz';p.write_bytes(gzip.compress(json.dumps(data,separators=(',',':')).encode(),mtime=0));print(p.name,len(cases),sum(len(c['writes']) for c in cases),sum(c['read_count'] for c in cases),hashlib.sha256(p.read_bytes()).hexdigest(),flush=True)
