#!/usr/bin/env python3
"""Capture original boolean properties and real mixed-type state operations."""
import argparse,gzip,hashlib,json,platform
from pathlib import Path
from native_boolean_properties import BooleanPropertiesOracle
parser=argparse.ArgumentParser(description=__doc__);parser.add_argument('--eboot',type=Path,required=True);parser.add_argument('--libc',type=Path,required=True);parser.add_argument('--output-dir',type=Path,required=True);a=parser.parse_args()
if platform.system()!='Linux' or platform.machine() not in ['x86_64','AMD64']:parser.error('Linux x86-64 is required')
expected={a.eboot:'c6089834fe1372854eed8f9acf2cb307f7009aaeb32ee303ce5bf6a1160786c8',a.libc:'1936f5700cc590877d9bf493c6a6bb7714e84003dfd6d74a7d77b1869f71d816'}
for path,digest in expected.items():
    if hashlib.sha256(path.read_bytes()).hexdigest()!=digest:parser.error(f'Unexpected mcps4 input: {path}')
n=BooleanPropertiesOracle(a.eboot,a.libc);queries=[True,False,-2147483648,-1,0,1,2,3,7,8,15,16,2147483647]
properties=[n.boolean_case(name,queries) for name in ['decayable','check_decay','snowy','persistent','open','数値','😀flag','property_😀']]
def b(name):return {'type':'boolean','name':name}
def i(name,minimum,maximum):return {'type':'integer','name':name,'minimum':minimum,'maximum':maximum}
schemas=[[],[b('snowy')],[b('decayable'),b('check_decay')],[b('same'),b('same')],[b('persistent'),i('age',0,2)],[i('age',0,2),b('persistent')],[i('layers',1,8),b('snowy')],[b('open'),i('stage',-1,1),b('powered')],[i('power',0,15),b('powered'),i('stage',0,1)],[b('same'),i('same',0,2)],[b('property_😀'),i('数値',2,7)],[i('near_max',2147483644,2147483647),b('flag')],[b('flag'),i('near_min',-2147483648,-2147483645)]]
definitions=[n.mixed_definition_case(schema,queries) for schema in schemas]
data={'repository':'https://github.com/Nejirus/mcps4','commit':'37e258085773f11599cf0ffe29119926769554b7','eboot_sha256':expected[a.eboot],'libc_sha256':expected[a.libc],'entrypoints':['0xdabb0','0xda5a0','0xdaba0','0xdb0b0','0xdb0c0','0xdb100','0x37a180','0xb7a930','0xcf850','0xd04f0','0xd0550'],'input_kind':'Complete original boolean and integer constructors, typed-box hash/equality/type comparisons, property operations and canonical definitions execute without supplied property/count/enumeration/ordinal/hash/equality callbacks. Pure allocator/string imports and readonly original libc bool/int RTTI exports are linked, including original strcmp for unequal RTTI names. Boolean enumeration is true then false. Mixed queries exercise distinct boolean/integer types, identity-distinct equal names and zero-index nonmatch fallback. UTF16 small/heap/surrogate names included. Type-pointer-dependent cached property hash excluded from numeric comparison. Input schemas are explicit, not actual per-block initializer schemas. No platform startup or world integration.','properties':properties,'definitions':definitions}
a.output_dir.mkdir(parents=True,exist_ok=True);p=a.output_dir/'ps4-boolean-properties.json.gz';p.write_bytes(gzip.compress(json.dumps(data,separators=(',',':')).encode(),mtime=0));print(p.name,len(properties),len(definitions),sum(len(c['getters']) for c in definitions),sum(len(c['setters']) for c in definitions),hashlib.sha256(p.read_bytes()).hexdigest(),flush=True)
