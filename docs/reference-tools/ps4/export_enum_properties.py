#!/usr/bin/env python3
"""Capture original opaque pointer sets and actual enum canonical pipelines."""
import argparse,gzip,hashlib,itertools,json,platform,random
from pathlib import Path
from native_enum_properties import EnumPropertiesOracle
parser=argparse.ArgumentParser(description=__doc__);parser.add_argument('--eboot',type=Path,required=True);parser.add_argument('--libc',type=Path,required=True);parser.add_argument('--output-dir',type=Path,required=True);a=parser.parse_args()
if platform.system()!='Linux' or platform.machine() not in ['x86_64','AMD64']:parser.error('Linux x86-64 is required')
expected={a.eboot:'c6089834fe1372854eed8f9acf2cb307f7009aaeb32ee303ce5bf6a1160786c8',a.libc:'1936f5700cc590877d9bf493c6a6bb7714e84003dfd6d74a7d77b1869f71d816'}
for path,digest in expected.items():
    if hashlib.sha256(path.read_bytes()).hexdigest()!=digest:parser.error(f'Unexpected mcps4 input: {path}')
n=EnumPropertiesOracle(a.eboot,a.libc);sets=[];rng=random.Random(0x4d07b0)
for length in [1,2,6,7,8,9,16,17,63,64,65,127,511,512,513,1023,1024,1025]:
    patterns=[list(range(length)),[0x8000000000000000+(i<<32)+i*64 for i in range(length)],[rng.getrandbits(64) for i in range(length)],[((i%19)<<48)|((i%7)<<8) for i in range(length)]]
    for values in patterns:sets.append(n.pointer_case(values))
sets.append(n.pointer_case([0,0,0xffffffffffffffff,0xffffffffffffffff,0,1,0x100000001,0x100000001]))
queries=[{'enum':i} for i in range(7)]+[True,False,-1,0,1,7]
properties=[]
for mask in range(64):
    identities=[i for i in range(6) if mask&(1<<i)]
    properties.append(n.enum_case('variant',identities,queries))
    if len(identities)>1:properties.append(n.enum_case('property_😀',list(reversed(identities)),queries))
for order in itertools.permutations(range(3)):properties.append(n.enum_case('数値',list(order),queries))
properties.append(n.enum_case('same_id',[0,6],queries))
def e(name,identities):return {'type':'enum','name':name,'identities':identities}
def b(name):return {'type':'boolean','name':name}
def i(name,minimum,maximum):return {'type':'integer','name':name,'minimum':minimum,'maximum':maximum}
schemas=[[e('variant',list(range(6)))],[e('variant',[2,0,1])],[e('variant',[0,6])],[b('decayable'),e('variant',[0,1,2,3]),b('check_decay')],[e('variant',[4,5]),i('age',0,2)],[i('layers',1,3),e('variant',[3,1,5]),b('open')],[e('same',[0,1]),e('same',[1,2,0])],[e('first',[5,3,1]),e('second',[0,2,4])]]
definitions=[n.enum_definition_case(schema,queries) for schema in schemas]
data={'repository':'https://github.com/Nejirus/mcps4','commit':'37e258085773f11599cf0ffe29119926769554b7','eboot_sha256':expected[a.eboot],'libc_sha256':expected[a.libc],'entrypoints':['0x4b9c40','0x4d03a0','0x4d07b0','0x4d0a10','0x4d0cc0','0x53d910','0x4d0ea0','0x4d1630','0x4d1640','0x4d1670','0xcf850','0xd04f0','0xd0550'],'input_kind':'Opaque uint64 pointer-set keys execute original constructor/insertion/duplicate/rehash/container cleanup bodies; the downstream enum factory is intercepted only to inspect the live set and append supplied keys through original insertion. First factory array repeats the first key six times to initialize the real set with one unique element. Actual enum objects/IDs/UTF16 names execute the isolated wood initializer with its property factory intercepted; a same-ID/name distinct object is an explicit clone. Original enum-property constructors execute over supplied unique ordered linked sets of those genuine enum objects. No supplied enum/property/box/hash/equality methods. Real boolean/integer/enum boxes and full canonical getter/mutation pipeline execute. Allocator and pure string imports/readonly bool/int RTTI linked. Native type/address dependent hash numbers and host-allocated enum factory order are excluded from numeric comparison. This does not establish actual per-block schema tables, platform heap layout or whole-world integration.','enum_values':n.enum_descriptions,'pointer_sets':sets,'properties':properties,'definitions':definitions}
a.output_dir.mkdir(parents=True,exist_ok=True);p=a.output_dir/'ps4-enum-properties.json.gz';p.write_bytes(gzip.compress(json.dumps(data,separators=(',',':')).encode(),mtime=0));print(p.name,'sets',len(sets),'snapshots',sum(len(c['snapshots']) for c in sets),'properties',len(properties),'definitions',len(definitions),'getters',sum(len(c['getters']) for c in definitions),'setters',sum(len(c['setters']) for c in definitions),'sha256',hashlib.sha256(p.read_bytes()).hexdigest(),flush=True)
