#!/usr/bin/env python3
"""Capture original template marker filtering, transforms and native map order."""
import argparse,gzip,hashlib,json,platform
from pathlib import Path
from native_template_markers import TemplateMarkerOracle
from nbt_input import read_nbt
parser=argparse.ArgumentParser(description=__doc__);parser.add_argument('--eboot',type=Path,required=True);parser.add_argument('--libc',type=Path,required=True);parser.add_argument('--assets',type=Path,required=True);parser.add_argument('--output-dir',type=Path,required=True);a=parser.parse_args()
if platform.system()!='Linux' or platform.machine() not in ('x86_64','AMD64'):parser.error('Linux x86-64 is required')
expected={a.eboot:'c6089834fe1372854eed8f9acf2cb307f7009aaeb32ee303ce5bf6a1160786c8',a.libc:'1936f5700cc590877d9bf493c6a6bb7714e84003dfd6d74a7d77b1869f71d816'}
for p,d in expected.items():
    if hashlib.sha256(p.read_bytes()).hexdigest()!=d:parser.error(f'Unexpected mcps4 input: {p}')
n=TemplateMarkerOracle(a.eboot,a.libc);templates=[];cases=[]
ids={r['name']:r['id'] for r in json.loads(Path(__file__).with_name('block-registry-native.json').read_text())['registrations']}
for entry in json.loads((a.assets/'templates.json').read_text()):
    if not entry['path'].startswith('coralcrust/'):continue
    path=a.assets/'templates'/(entry['path']+'.nbt')
    if hashlib.sha256(path.read_bytes()).hexdigest()!=entry['gzip_sha256']:parser.error('Modified original template '+str(path))
    tag=read_nbt(path);blocks=[]
    for block in tag['blocks']:
        name=tag['palette'][block['state']]['Name'].removeprefix('minecraft:');blocks.append([*block['pos'],ids[name],block.get('nbt',{}).get('metadata')])
    templates.append({'id':entry['index'],'path':entry['path'],'sha256':entry['gzip_sha256'],'blocks':blocks})
for size in [0,1,7,8,9,63,64,65,511,512,513,600]:templates.append({'id':1000+size,'path':'synthetic/size_'+str(size),'blocks':[[17*i,i%7,-11*i,255,'marker_'+str(i)] for i in range(size)]})
templates.append({'id':2000,'path':'synthetic/colliding_hashes','blocks':[[512*i,0,0,255,'collision_'+str(i)] for i in range(600)]})
templates.append({'id':2001,'path':'synthetic/duplicate_keys','blocks':[[1,2,3,255,'first'],[4,5,6,255,'other'],[1,2,3,255,'second'],[-9,8,-7,255,'珊瑚'],[4,5,6,255,'replaced']]})
templates.append({'id':2002,'path':'synthetic/identity_and_nbt_filter','blocks':[[0,0,0,1,'non-marker'],[1,0,0,255,None],[2,0,0,259,'non-marker'],[3,0,0,255,''],[4,0,0,255,'blockMarker']]})
for template in templates:
    n.template(template['id'],template['blocks'])
    for origin in [[0,49,0],[-2493,51,2501],[17,-1,-23],[2147483000,254,-2147483001]]:
        for rotation in range(4):cases.append(n.collect(template['id'],origin,rotation))
    print('Original marker template',template['id'],'cases',len(cases),flush=True)
data={'repository':'https://github.com/Nejirus/mcps4','commit':'37e258085773f11599cf0ffe29119926769554b7','eboot_sha256':expected[a.eboot],'libc_sha256':expected[a.libc],
      'entrypoints':['0x6c50e0','0x6c8410','0x6feac0','0x6febd0','0x6fedc0','0x6fe6b0','0xc0900','0xc0430'],
      'input_kind':'Original marker collector, position transforms, identity/NBT-presence filter, native list/hash-map insertion, duplicates and rehash. Raw positions/order/metadata strings from eleven hash-checked original assets plus explicit synthetic container contexts are supplied. Canonical block IDs are inputs; block-state metadata and NBT parsing are outside this comparison. Rotation/mirror storage supplied with no mirror/clip. No Java ordering algorithm runs in the exporter. No template block geometry, world effects or whole-world capture.','templates':templates,'cases':cases}
a.output_dir.mkdir(parents=True,exist_ok=True);p=a.output_dir/'ps4-template-markers.json.gz';p.write_bytes(gzip.compress(json.dumps(data,separators=(',',':')).encode(),mtime=0));print(p.name,len(cases),sum(len(c['markers']) for c in cases),hashlib.sha256(p.read_bytes()).hexdigest(),flush=True)
