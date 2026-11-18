"""Emit marker font descriptors supported by Minecraft 1.17.1.

The Java tutorial splitter supplies glyph advances directly from glyphs.json;
1.17.1 does not support the newer 'space' provider. No replacement art is made.
"""
import argparse,json
from pathlib import Path
p=argparse.ArgumentParser();p.add_argument('--project-root',type=Path,required=True);a=p.parse_args()
root=a.project_root/'src/main/resources/assets/legacyconsole'
for name in json.loads((root/'glyphs.json').read_text()):
 (root/'font'/('tutorial_'+name+'.json')).write_text(json.dumps({'providers':[]},indent=2)+'\n')
