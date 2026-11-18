#!/usr/bin/env python3
"""Read registered biome virtual bindings and their used variant fields.

Uses the same explicit initialization boundaries as export_biome_settings.py.
No world generation, SDK startup, or feature geometry runs in this capture.
"""
import argparse
import hashlib
import json
from pathlib import Path
from native_biome_settings import BiomeSettingsOracle
from native_tree_selection import TreeSelectionOracle
from native_flora_selection import FloraSelectionOracle
from native_biome_wrappers import BiomeWrapperOracle
from native_ocean_wrappers import OceanWrapperOracle

p=argparse.ArgumentParser(description=__doc__)
p.add_argument('--eboot',type=Path,required=True)
p.add_argument('--libc',type=Path,required=True)
p.add_argument('--output',type=Path,required=True)
a=p.parse_args()
for path,sha in [(a.eboot,'c6089834fe1372854eed8f9acf2cb307f7009aaeb32ee303ce5bf6a1160786c8'),(a.libc,'1936f5700cc590877d9bf493c6a6bb7714e84003dfd6d74a7d77b1869f71d816')]:
    if hashlib.sha256(path.read_bytes()).hexdigest()!=sha:p.error(f'Unexpected original input: {path}')
n=BiomeSettingsOracle(a.eboot,a.libc)
methods={
 'tree':(0x10,{**TreeSelectionOracle.SELECTORS,**TreeSelectionOracle.EXTRA_SELECTORS}),
 'grass':(0x18,FloraSelectionOracle.GRASS),
 'flowers':(0x20,FloraSelectionOracle.FLOWERS),
 'decoration':(0x58,{'DEFAULT':0x85430,**BiomeWrapperOracle.WRAPPERS,**OceanWrapperOracle.WRAPPERS})}
profiles=[]
for b in n.biome_settings():
    obj=n.game.read(0x1409fc0+8*b['id'],'Q');vt=n.read(obj,'Q')
    profile={'id':b['id'],'class':b['class'],'maximumSnowDepth':b['maximum_snow_depth'],'top':b['top']}
    for key,(slot,choices) in methods.items():
        pc=n.read(vt+slot,'Q')-n.game.base
        profile[key]=next((name for name,value in choices.items() if value==pc),None)
        if profile[key] is None:raise ValueError(f'Untranslated registered binding {b["id"]} {b["class"]} {key} {pc:x}')
    # Only fields read by the translated selectors/wrappers are captured.
    if b['class'] in ['11ForestBiome','18MutatedForestBiome','10TaigaBiome']:
        profile['variant']=n.read(obj+0x1d0,'i')
    elif b['class'] in ['11PlainsBiome','11JungleBiome','8IceBiome']:
        profile['variant']=n.read(obj+0x1d0,'B')
    else:profile['variant']=0
    profiles.append(profile)
result={'repository':'https://github.com/Nejirus/mcps4','commit':'37e258085773f11599cf0ffe29119926769554b7','entrypoint':'0x7f900','virtualSlots':{k:hex(v[0]) for k,v in methods.items()},'variantScope':'Only fields read by these translated bindings are captured. Unused variant arguments are zero; BambooJungle has no variant read.','profiles':profiles}
selectors=TreeSelectionOracle(a.eboot,a.libc)
result['additionalTreeSelections']=[selectors.selection(kind,0,seed) for kind in ['ICE','MESA'] for seed in [0,1,-1,123456789,-987654321,0x7fffffffffffffff]]
a.output.parent.mkdir(parents=True,exist_ok=True)
a.output.write_text(json.dumps(result,indent=2)+'\n')
print(f'{len(profiles)} original biome profiles -> {a.output}')
