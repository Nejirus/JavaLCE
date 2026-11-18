#!/usr/bin/env python3
"""Capture actual class metadata decoders with all canonical defaults."""
import argparse,gzip,hashlib,json,platform
from pathlib import Path
from native_scalar_state_decoders import ScalarStateDecodersOracle
from read_scalar_properties import recover
parser=argparse.ArgumentParser(description=__doc__);parser.add_argument('--eboot',type=Path,required=True);parser.add_argument('--libc',type=Path,required=True);parser.add_argument('--output-dir',type=Path,required=True);a=parser.parse_args()
if platform.system()!='Linux' or platform.machine() not in ['x86_64','AMD64']:parser.error('Linux x86-64 is required')
expected={a.eboot:'c6089834fe1372854eed8f9acf2cb307f7009aaeb32ee303ce5bf6a1160786c8',a.libc:'1936f5700cc590877d9bf493c6a6bb7714e84003dfd6d74a7d77b1869f71d816'}
for path,digest in expected.items():
    if hashlib.sha256(path.read_bytes()).hexdigest()!=digest:parser.error(f'Unexpected mcps4 input: {path}')
inputs=json.loads(Path(__file__).with_name('scalar-block-vtables.json').read_text());n=ScalarStateDecodersOracle(a.eboot,a.libc,recover(a.eboot));definitions=[n.decoder_case(int(pc,16),list(range(16))) for pc in inputs['vtables']]
data={'repository':'https://github.com/Nejirus/mcps4','commit':'37e258085773f11599cf0ffe29119926769554b7','eboot_sha256':expected[a.eboot],'libc_sha256':expected[a.libc],'input_kind':'Original scalar initializers, real typed boxes/properties, complete concrete definitions and fromData virtuals execute without property/count/box/definition/conversion callbacks. All 1303 canonical states in 86 explicit concrete classes are supplied in turn as the default field; every uint4 metadata value is queried. Returned state canonical identity and unchanged supplied default field verified. Auxiliary owner fields zeroed; one-thread static guard protocol/pure imports supplied. Initializers read from original literal code, copied aliases retain identity. Actual post-constructor default choices, complete registry mapping/index, geometry/physics/world integration are not established; inputs outside metadata nibble contract are not queried.','vtables_input':inputs,'definitions':definitions}
a.output_dir.mkdir(parents=True,exist_ok=True);p=a.output_dir/'ps4-scalar-state-decoders.json.gz';p.write_bytes(gzip.compress(json.dumps(data,separators=(',',':')).encode(),mtime=0));print(p.name,'classes',len(definitions),'defaults',sum(len(d['outputs']) for d in definitions),'queries',sum(len(row[1]) for d in definitions for row in d['outputs']),'decoders',len({d['decoder'] for d in definitions}),'sha256',hashlib.sha256(p.read_bytes()).hexdigest(),flush=True)
