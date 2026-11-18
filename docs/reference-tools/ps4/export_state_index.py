#!/usr/bin/env python3
"""Capture original definition-order state-ID writes and alias collisions."""
import argparse,gzip,hashlib,json,platform,random
from pathlib import Path
from native_state_index import StateIndexOracle
parser=argparse.ArgumentParser(description=__doc__);parser.add_argument('--eboot',type=Path,required=True);parser.add_argument('--libc',type=Path,required=True);parser.add_argument('--output-dir',type=Path,required=True);a=parser.parse_args()
if platform.system()!='Linux' or platform.machine() not in ['x86_64','AMD64']:parser.error('Linux x86-64 and a C compiler are required')
expected={a.eboot:'c6089834fe1372854eed8f9acf2cb307f7009aaeb32ee303ce5bf6a1160786c8',a.libc:'1936f5700cc590877d9bf493c6a6bb7714e84003dfd6d74a7d77b1869f71d816'}
for path,digest in expected.items():
    if hashlib.sha256(path.read_bytes()).hexdigest()!=digest:parser.error(f'Unexpected mcps4 input: {path}')
n=StateIndexOracle(a.eboot,a.libc);cases=[]
cases.append(n.index('empty',[],[]));cases.append(n.index('empty definitions',[{'token':0,'data':0}],[{'id':0,'states':[]},{'id':365,'states':[None]}]))
values=[{'token':i,'data':i%16} for i in range(100)]
cases.append(n.index('same metadata and repeated state',values,[{'id':1,'states':[0,16,None,32,0]},{'id':365,'states':[99,15,31,47,None]},{'id':1,'states':[48,64,80,96]}]))
cases.append(n.index('shared state across definitions',values,[{'id':1,'states':[7,None,7]},{'id':2,'states':[7,8]},{'id':365,'states':[7]}]))
registry=json.loads(Path(__file__).with_name('block-registry-native.json').read_text());ids=[0]+[r['id'] for r in registry['registrations']]
values=[{'token':id*16+meta,'data':meta} for id in ids for meta in range(16)]
for order in [ids,list(reversed(ids)),sorted(ids)]:
    blocks=[{'id':id,'states':[id*16+meta for meta in range(16)]+([None] if id%11==0 else [])} for id in order];cases.append(n.index('all registered synthetic metadata vectors',values,blocks))
for seed in range(128):
    rng=random.Random(seed);values=[{'token':i,'data':rng.randrange(16)} for i in range(64)];blocks=[{'id':rng.choice([0,1,255,256,257,365]),'states':[None if rng.randrange(8)==0 else rng.randrange(64) for _ in range(rng.randrange(25))]} for _ in range(rng.randrange(1,17))];cases.append(n.index('supplied order '+str(seed),values,blocks))
data={'repository':'https://github.com/Nejirus/mcps4','commit':'37e258085773f11599cf0ffe29119926769554b7','eboot_sha256':expected[a.eboot],'libc_sha256':expected[a.libc],'entrypoints':['0xb7737..0xb77ea','0x95490'],
 'input_kind':'Original final registry state-ID enumeration loop executes with host-provided node sentinel/frame fields, definition vectors, registry ID and metadata callbacks. Original definition getter, node/vector loops, null-state skips, state +0x10 stores and packed-table stores execute; exit is before unrelated registration cleanup. Actual startup table capacity 5856 used. All state IDs, complete table pointer-token hash and callback-order hash are compared. Token inputs include duplicate metadata and reused states to expose last-write behavior; synthetic 16-metadata vectors for registered IDs are not claimed actual game definitions. Constructor/property/RTTI earlier phases and actual registry node/vector ordering remain outside this comparison. Table hash is signed-int32 token per slot, -1 for NULL. Callback events follow export_state_ids format.','cases':cases}
a.output_dir.mkdir(parents=True,exist_ok=True);p=a.output_dir/'ps4-state-index.json.gz';p.write_bytes(gzip.compress(json.dumps(data,separators=(',',':')).encode(),mtime=0));print(p.name,len(cases),sum(len(c['state_ids']) for c in cases),hashlib.sha256(p.read_bytes()).hexdigest(),flush=True)
