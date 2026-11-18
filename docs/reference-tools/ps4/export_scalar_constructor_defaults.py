#!/usr/bin/env python3
"""Capture original scalar constructor defaults and requested property changes."""
import argparse,gzip,hashlib,json,platform
from pathlib import Path
from native_scalar_constructor_defaults import ScalarConstructorDefaultsOracle
from read_scalar_properties import recover as recover_properties
from read_block_constructors import recover as recover_constructors
parser=argparse.ArgumentParser(description=__doc__);parser.add_argument('--eboot',type=Path,required=True);parser.add_argument('--libc',type=Path,required=True);parser.add_argument('--output-dir',type=Path,required=True);a=parser.parse_args()
if platform.system()!='Linux' or platform.machine() not in ['x86_64','AMD64']:parser.error('Linux x86-64 is required')
expected={a.eboot:'c6089834fe1372854eed8f9acf2cb307f7009aaeb32ee303ce5bf6a1160786c8',a.libc:'1936f5700cc590877d9bf493c6a6bb7714e84003dfd6d74a7d77b1869f71d816'}
for path,digest in expected.items():
    if hashlib.sha256(path.read_bytes()).hexdigest()!=digest:parser.error(f'Unexpected mcps4 input: {path}')
inputs=json.loads(Path(__file__).with_name('scalar-constructor-inputs.json').read_text());flow=recover_constructors(a.eboot);rows={r['id']:r for r in flow['registrations']};n=ScalarConstructorDefaultsOracle(a.eboot,a.libc,recover_properties(a.eboot));cases=[n.constructor_case(rows[id]) for id in inputs['ids']]
data={'repository':'https://github.com/Nejirus/mcps4','commit':'37e258085773f11599cf0ffe29119926769554b7','eboot_sha256':expected[a.eboot],'libc_sha256':expected[a.libc],'input_kind':'Original isolated constructor bodies run with real scalar properties/definitions/typed boxes, original Material/MaterialColor initialization, original mutation and default-field storage. One unobserved and one observed invocation compare final class/default/metadata. Native C observers dispatch original mutation code and the verified original eight-byte position-independent setter body; they supply no mutation/default results. Live state codes/owner vtables avoid freed ancestor-state reuse. Only final-class definition operations are retained. Original constructor associations/known GP inputs are read from registration code; text arguments are explicit empty UTF16 strings and unknown/unused GP inputs zeroed. No physical-attribute/complete constructor-environment claim. One-thread static guard protocol/pure imports/readonly RTTI supplied. Inherited void boundaries trap if called. Registration-time changes, empty base-block initialization, final indexing, world/physics integration remain outside scope.','constructor_inputs':inputs,'cases':cases}
a.output_dir.mkdir(parents=True,exist_ok=True);p=a.output_dir/'ps4-scalar-constructor-defaults.json.gz';p.write_bytes(gzip.compress(json.dumps(data,separators=(',',':')).encode(),mtime=0));print(p.name,'constructors',len(cases),'classes',len({c['vtable'] for c in cases}),'operations',sum(len(c['operations']) for c in cases),'sha256',hashlib.sha256(p.read_bytes()).hexdigest(),flush=True)
