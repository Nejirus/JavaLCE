#!/usr/bin/env python3
"""Capture real integer properties and their native canonical-state pipeline."""
import argparse,gzip,hashlib,json,platform
from pathlib import Path
from native_integer_properties import IntegerPropertiesOracle
parser=argparse.ArgumentParser(description=__doc__);parser.add_argument('--eboot',type=Path,required=True);parser.add_argument('--libc',type=Path,required=True);parser.add_argument('--output-dir',type=Path,required=True);a=parser.parse_args()
if platform.system()!='Linux' or platform.machine() not in ['x86_64','AMD64']:parser.error('Linux x86-64 is required')
expected={a.eboot:'c6089834fe1372854eed8f9acf2cb307f7009aaeb32ee303ce5bf6a1160786c8',a.libc:'1936f5700cc590877d9bf493c6a6bb7714e84003dfd6d74a7d77b1869f71d816'}
for path,digest in expected.items():
    if hashlib.sha256(path.read_bytes()).hexdigest()!=digest:parser.error(f'Unexpected mcps4 input: {path}')
def i32(value):return ((value+2147483648)&0xffffffff)-2147483648
n=IntegerPropertiesOracle(a.eboot,a.libc);properties=[];ranges=[(-5,-1),(-2,3),(0,0),(0,1),(0,2),(0,3),(0,7),(0,15),(1,8),(2,7),(3,3),(4,1),(-2147483648,-2147483645),(2147483644,2147483647),(2147483647,-2147483648)]
for name in ['age','layers','level','persistent','snow_layers','数値','😀layer','property_😀']:
    for minimum,maximum in ranges:
        queries=sorted({-2147483648,-1,0,1,minimum,maximum,i32(minimum-1),i32(maximum+1),2147483647});properties.append(n.property_case(name,minimum,maximum,queries))
specifications=[[],[('age',0,2)],[('layers',1,8)],[('stage',-1,1)],[('level',3,7)],[('age',0,3),('layers',1,3)],[('power',0,15),('stage',0,1),('rank',1,2)],[('near_max',2147483644,2147483647)],[('near_min',-2147483648,-2147483645)]]
queries=[-2147483648,-5,-1,0,1,2,3,7,8,15,16,2147483647]
definitions=[n.definition_case([{'name':name,'minimum':minimum,'maximum':maximum} for name,minimum,maximum in specs],queries) for specs in specifications]
data={'repository':'https://github.com/Nejirus/mcps4','commit':'37e258085773f11599cf0ffe29119926769554b7','eboot_sha256':expected[a.eboot],'libc_sha256':expected[a.libc],'entrypoints':['0x37a180','0x379b40','0x379e50','0x37a260','0x37a270','0x37a2a0','0xb7a930','0xcf850','0xd04f0','0xd0550'],
 'input_kind':'Original integer-property factory/constructor, actual typed integer boxes, vector/count/value/index methods and complete canonical-definition/property-read/mutation pipeline execute. No supplied property/count/enumeration/ordinal/hash/equality callbacks. Explicit pure string/allocator bindings and original readonly non-executable libc int RTTI data export St4apgcBNfo at 0x118420 are linked; no platform startup. Input names include UTF16 small/heap/surrogate cases; bounded ranges include empty and signed limit intervals. Definitions use nonempty properties only. Integer property cached hash includes native type-pointer bits and is deliberately outside numeric output comparison. Inputs are property schemas, not claimed actual per-block initializer tables. Block geometry/world/complete registry remains outside scope.','properties':properties,'definitions':definitions}
a.output_dir.mkdir(parents=True,exist_ok=True);p=a.output_dir/'ps4-integer-properties.json.gz';p.write_bytes(gzip.compress(json.dumps(data,separators=(',',':')).encode(),mtime=0));print(p.name,len(properties),len(definitions),sum(len(c['getters']) for c in definitions),sum(len(c['setters']) for c in definitions),hashlib.sha256(p.read_bytes()).hexdigest(),flush=True)
