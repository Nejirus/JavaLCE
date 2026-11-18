#!/usr/bin/env python3
"""Capture original canonical tables, property reads and property mutations."""
import argparse,gzip,hashlib,json,platform
from pathlib import Path
from native_state_definitions import StateDefinitionsOracle
parser=argparse.ArgumentParser(description=__doc__);parser.add_argument('--eboot',type=Path,required=True);parser.add_argument('--libc',type=Path,required=True);parser.add_argument('--output-dir',type=Path,required=True);a=parser.parse_args()
if platform.system()!='Linux' or platform.machine() not in ['x86_64','AMD64']:parser.error('Linux x86-64 is required')
expected={a.eboot:'c6089834fe1372854eed8f9acf2cb307f7009aaeb32ee303ce5bf6a1160786c8',a.libc:'1936f5700cc590877d9bf493c6a6bb7714e84003dfd6d74a7d77b1869f71d816'}
for path,digest in expected.items():
    if hashlib.sha256(path.read_bytes()).hexdigest()!=digest:parser.error(f'Unexpected mcps4 input: {path}')
counts=[[]]+[[c] for c in [1,2,3,4,5,6,7,8,9,15,16,17]]+[[2,3],[3,2],[3,5],[5,3],[2,2],[1,3],[2,3,2],[3,3,3],[5,2,3],[1,1,3],[4,4,4],[2,2,2,2],[3,2,3,2],[5,3,2,2]]
contexts=[]
for sizes in counts:
    contexts.append([{'count':count,'indices':list(range(count))} for count in sizes])
    if sizes:contexts.append([{'count':count,'indices':list(reversed(range(count)))} for count in sizes])
contexts.extend([[{'count':6,'indices':[0,2,5]}],[{'count':4,'indices':[0,3]}],[{'count':8,'indices':[0,2,7]}],[{'count':5,'indices':[1,4]},{'count':3,'indices':[0,2]}]])
n=StateDefinitionsOracle(a.eboot,a.libc);cases=[n.definition(properties,mode) for properties in contexts for mode in ['zero','ordinal','salted']]
data={'repository':'https://github.com/Nejirus/mcps4','commit':'37e258085773f11599cf0ffe29119926769554b7','eboot_sha256':expected[a.eboot],'libc_sha256':expected[a.libc],'entrypoints':['0xcf850','0x49eba0','0x49ebd0','0xd04f0','0xd0550','0x7a050'],
 'input_kind':'Complete original canonical-definition constructor, actual Cartesian iterator/container/state allocation, default-state getter and property read/mutation routines execute unchanged. Explicit property count, nonempty possible-value pointer vectors, value-by-index/ordinal callbacks and boxed scalar hash/equality are supplied; these are not actual game property initializers. Three hash contexts verify the sparse table behavior independently of supplied temporary hash values. Counts, ordered enum indices, holes, masks/shifts, owner links, missing-property reads, same-value identity and low16-masked mutation results are native outputs. Mutation is called only for present properties; the original first search is unchecked for absent properties. Zero-property/filter contexts are synthetic inputs, not claimed valid game definitions; empty value enumerations are outside the original constructor iterator contract. Property query events are signed-int32 triples: (0,propertyIndex,valueIndex), (1,propertyIndex,ordinalOfRequested), (2,firstScalar,secondScalar) equality; constructor temporary hash/equality calls are outside the query event hash. No Java construction/property algorithm supplies outputs. Actual property schemas, block constructors/shape initialization and full registry/world integration remain outside this comparison.','cases':cases}
a.output_dir.mkdir(parents=True,exist_ok=True);p=a.output_dir/'ps4-state-definitions.json.gz';p.write_bytes(gzip.compress(json.dumps(data,separators=(',',':')).encode(),mtime=0));print(p.name,len(cases),sum(len(c['getters']) for c in cases),sum(len(c['setters']) for c in cases),hashlib.sha256(p.read_bytes()).hexdigest(),flush=True)
