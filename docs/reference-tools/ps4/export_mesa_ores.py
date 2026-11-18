#!/usr/bin/env python3
"""Capture the native Mesa ore override together with original vein geometry."""
import argparse,gzip,hashlib,json,platform
from pathlib import Path
from native_ores import OreOracle
parser=argparse.ArgumentParser(description=__doc__);parser.add_argument('--eboot',type=Path,required=True);parser.add_argument('--libc',type=Path,required=True);parser.add_argument('--output-dir',type=Path,required=True);a=parser.parse_args()
if platform.system()!='Linux' or platform.machine() not in ('x86_64','AMD64'):parser.error('Linux x86-64 is required')
expected={a.eboot:'c6089834fe1372854eed8f9acf2cb307f7009aaeb32ee303ce5bf6a1160786c8',a.libc:'1936f5700cc590877d9bf493c6a6bb7714e84003dfd6d74a7d77b1869f71d816'}
for p,d in expected.items():
 if hashlib.sha256(p.read_bytes()).hexdigest()!=d:parser.error(f'Unexpected mcps4 input: {p}')
n=OreOracle(a.eboot,a.libc);cases=[]
# Verify the override's binding from MesaBiome's original decorator assignment.
pc=0x46a870
if bytes(n.game.file[pc:pc+3])!=b'\x48\x8d\x05':parser.error('Unexpected original Mesa decorator vtable LEA')
vt=pc+7+n.game.read(pc+3,'i')+16
if n.game.read(vt+0x20,'Q')!=n.game.base+0x46ba10:parser.error('Unexpected original Mesa ore override')
for seed in [0,1,-1,82958734,-9223372036854775808,9223372036854775807]:
 for mode in ['default','ranges','disabled']:
  for profile,height in [('stone',128),('mixed',64),('air',0),('rules',300)]:
   for origin in [[0,0],[-2496,2480],[16,-32]]:cases.append(n.decorate(seed,origin,mode,profile,height,mesa=True))
 print('Original Mesa ore seed',seed,'cases',len(cases),flush=True)
data={'repository':'https://github.com/Nejirus/mcps4','commit':'37e258085773f11599cf0ffe29119926769554b7','eboot_sha256':expected[a.eboot],'libc_sha256':expected[a.libc],
 'entrypoints':['0x46ba10','0x88970','0x89360','0x503f60','0x504a30','0x67f1f0','0x1f1940'],'decorator_vtable':hex(vt),'override_slot':'0x20','binding_pc':hex(pc),
 'input_kind':'Original Mesa decorator override, base ore dispatcher, height dispatcher, vein geometry and stone replacement predicate execute on one shared original RNG. Default configuration constructor executes; ranges/counts in alternate modes, canonical feature/block storage, heights/protection boxes, successful writes and global update-mode effects are controlled inputs. No Java geometry or dispatch runs in the exporter. Not complete original world saves.','cases':cases}
a.output_dir.mkdir(parents=True,exist_ok=True);p=a.output_dir/'ps4-mesa-ores.json.gz';p.write_bytes(gzip.compress(json.dumps(data,separators=(',',':')).encode(),mtime=0));print(p.name,len(cases),sum(len(c['feature_calls']) for c in cases),sum(len(c['writes']) for c in cases),hashlib.sha256(p.read_bytes()).hexdigest(),flush=True)
