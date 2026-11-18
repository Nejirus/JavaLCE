"""Recover mcps4's numbered size stops, HUD transform ratios and slider PNGs.

Applying those ratios to workstation menus is the user's separate Java option;
the PS4 setting itself controls HUD frames. The original HUD option is retained.
"""
import argparse,hashlib,json,struct
from pathlib import Path
from io import BytesIO
from PIL import Image
from read_fui import Fui
from read_block_constructors import Source
p=argparse.ArgumentParser();p.add_argument('--graphics',type=Path,required=True);p.add_argument('--hud',type=Path,required=True);p.add_argument('--eboot',type=Path,required=True);p.add_argument('--project-root',type=Path,required=True);a=p.parse_args()
def sha(b):return hashlib.sha256(b).hexdigest()
s=Source(a.eboot);assert s.read(0xabc438,5)==bytes.fromhex('b901000000');assert s.read(0xabc43d,6)==bytes.fromhex('41b803000000')
f=Fui(a.hud);scales=[]
for frame,label in [('ScaleSmall','HudSmall'),('ScaleNormal','HudNormal'),('ScaleLarge','HudLarge')]:
 row=next(x for x in f.frames if x['label']==frame);node=next(g for g in f.geometry[row['start']:row['start']+row['count']] if g['name']==label);scales.append(node['matrix'][0])
assert scales==[1,1.5,2]
b=a.graphics.read_bytes();c=struct.unpack_from('<15I',b,0x4c);data=len(b)-c[12];bitmap=data-c[11]*32;export=bitmap-c[14]*64-c[10]*72
names={b[export+i*72:export+i*72+64].split(b'\0')[0].decode():struct.unpack_from('<2I',b,export+i*72+64) for i in range(c[10])}
roles={'slider':'FJ_Slider_Background','slider_handle':'Slider_Graphic','slider_outline':'MainMenuButton_Outline','slider_highlight':'SliderBackgroundHighlight','slider_tint_active':'Slider_Track_TintActive','slider_tint_inactive':'Slider_Track_TintInactive'}
assets=[];out=a.project_root/'src/main/resources/assets/legacyconsole/textures/ps4/ui';out.mkdir(parents=True,exist_ok=True)
for role,name in roles.items():
 kind,index=names[name];assert kind==3;record=struct.unpack_from('<8I',b,bitmap+index*32);raw=b[data+record[4]:data+record[4]+record[5]];im=Image.open(BytesIO(raw)).convert('RGBA');bb,g,r,alpha=im.split();buf=BytesIO();Image.merge('RGBA',(r,g,bb,alpha)).save(buf,format='PNG');p=out/(role+'.png');p.write_bytes(buf.getvalue());assets.append(dict(symbol=name,bitmap=index,source_sha256=sha(raw),sha256=sha(p.read_bytes()),destination=str(p.relative_to(a.project_root)),size=im.size))
result=dict(repository='Nejirus/mcps4',revision='37e258085773f11599cf0ffe29119926769554b7',eboot_sha256=sha(a.eboot.read_bytes()),constructor='0xaba940',minimum_instruction='0xabc438',maximum_instruction='0xabc43d',slider_init='0x9f7980',stops=[1,2,3],hud_source_scales=scales,factors=[x/scales[1] for x in scales],hud_sha256=sha(f.b),graphics_sha256=sha(b),assets=assets,adaptation='The separate Console UI Size option applies the original HUD frame ratios to PS4 workstation/crafting/trading menus and tutorial/description panels. Screen-fit limits avoid clipping; HUD Size is independent. The PS4 setting is not claimed to be a native global menu zoom.')
(a.project_root/'PS4-UI-SCALE-2312.json').write_text(json.dumps(result,indent=2)+'\n');print('Recovered stops',result['stops'],'ratios',result['factors'],'and',len(assets),'native slider images')
