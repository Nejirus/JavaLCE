#!/usr/bin/env python3
"""Capture original registered biome climate and decoration constructor data."""
import argparse,gzip,hashlib,json,platform
from pathlib import Path
from native_biome_settings import BiomeSettingsOracle,I

parser=argparse.ArgumentParser(description=__doc__);parser.add_argument('--eboot',type=Path,required=True);parser.add_argument('--libc',type=Path,required=True);parser.add_argument('--output-dir',type=Path,required=True);args=parser.parse_args()
if platform.system()!='Linux' or platform.machine() not in ('x86_64','AMD64'):parser.error('Linux x86-64 is required')
expected={args.eboot:'c6089834fe1372854eed8f9acf2cb307f7009aaeb32ee303ce5bf6a1160786c8',args.libc:'1936f5700cc590877d9bf493c6a6bb7714e84003dfd6d74a7d77b1869f71d816'}
for path,digest in expected.items():
    if hashlib.sha256(path.read_bytes()).hexdigest()!=digest:parser.error(f'Unexpected mcps4 input: {path}')
oracle=BiomeSettingsOracle(args.eboot,args.libc);biomes=oracle.biome_settings()
equality=[{'first':a['id'],'second':b['id'],'result':bool(oracle.call(0x3e4530,I,[I,I],[a['id'],b['id']])&255)} for a in biomes for b in biomes]
data={'repository':'https://github.com/Nejirus/mcps4','commit':'37e258085773f11599cf0ffe29119926769554b7','eboot_sha256':expected[args.eboot],'libc_sha256':expected[args.libc],
      'entrypoints':['0x7f900','0x83e60','0x86c00','0x3e4530','0x85990','0x85300','0x85320','0x852c0'],'preset_store_span':['0x15110d','0x151280'],
      'input_kind':'Original complete biome initializer, original biome constructors/base constructor and decorator constructor count stores. Only audited numeric preset stores run from a grouped initializer, returning before its next allocation. Name-map nodes, registry lookup/default/canonical property storage, infested stone property binding and nine feature constructor/deletion bindings are controlled; feature geometry does not run. Original climate arithmetic/flags/category/class identities and decoration counts are outputs, not supplied climate/count inputs. No full original registry startup or whole-world capture.',
      'feature_constructor_bindings':[hex(pc) for pc in oracle.feature_constructor_bindings],'infested_stone_enum_id':oracle.infested_enum_id,'presets':oracle.presets,'biomes':biomes,'equality':equality}
args.output_dir.mkdir(parents=True,exist_ok=True);path=args.output_dir/'ps4-biome-settings.json.gz';path.write_bytes(gzip.compress(json.dumps(data,separators=(',',':')).encode(),mtime=0));print(path.name,len(biomes),hashlib.sha256(path.read_bytes()).hexdigest(),flush=True)
