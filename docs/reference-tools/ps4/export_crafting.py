from read_fui import Fui
from pathlib import Path
from PIL import Image
from io import BytesIO
import json,struct,hashlib

import argparse
parser=argparse.ArgumentParser(description='Extract PS4 crafting art and authored FUI transforms.')
parser.add_argument('--input-dir',type=Path,required=True,help='Folder with CraftingMenu1080.fui and skinHDGraphicsInGame.fui')
parser.add_argument('--project-root',type=Path,required=True)
args=parser.parse_args()
root=args.project_root
dest=root/'src/main/resources/assets/legacyconsole'
art=dest/'textures/lce/ps4/crafting';art.mkdir(parents=True,exist_ok=True)
reference=Path(__file__).parent
here=args.input_dir
scene=Fui(here/'CraftingMenu1080.fui');graphics=Fui(here/'skinHDGraphicsInGame.fui')
def sha(b):return hashlib.sha256(b).hexdigest()
def portable_png(data):
 # Orbis FUI payload channels are BGRA. Decode them into Java's RGBA textures.
 im=Image.open(BytesIO(data)).convert('RGBA');r,g,b,a=im.split();out=BytesIO()
 Image.merge('RGBA',(b,g,r,a)).save(out,format='PNG');return out.getvalue()
data={'repository':'Nejirus/mcps4','revision':'37e258085773f11599cf0ffe29119926769554b7','scene_sha256':sha(scene.b),'graphics_sha256':sha(graphics.b),'root':[496.5,170.5],'placeholder':50,'controls':{},'sprites':{},'panels':{}}
for g in scene.geometry:
 if g['name'] and g['name']!='MainPanel':data['controls'][g['name']]=g['matrix']
symbols=['scrollUp','scrollDown','Crafting_2SlotLargeV','Crafting_3SlotLargeV','Craft_Highlight_L_Small','LargeVertTabOff','LargeVertTabOn','LargeOffTab','LargeTabLeft','LargeTabMiddle','LargeTabRight','IconHolder_1x1border','IconHolderRed_1x1border','IconHolderInactive_1x1border','IconHolderHighlight_1x1border','SlotHighlight','Arrow_Small_Off','Craft_Scroll_up','Craft_Scroll_Down']
for name in symbols:
 typ,index=graphics.exports[name];assert typ==3;img=graphics.images[index];filename=name.lower();(art/(filename+'.png')).write_bytes(portable_png(img['bytes']));data['sprites'][name]={'texture':filename,'size':img['size'],'source_sha256':sha(img['bytes']),'sha256':sha(portable_png(img['bytes']))}
# Original 9-grid triangles and texture translations; retain each source PNG.
nd,nn,np,nsh,nb,nv,*_=graphics.counts
shape_start=0x98+nd*28+np*132;batch_start=shape_start+nsh*28;vertex_start=batch_start+nb*44
for name in ['FJ_Panel9Grid','FJ_SquareRecessedPanel']:
 typ,index=graphics.exports[name];assert typ==2;definition=graphics.defs[index];frame=graphics.frames[definition[1]];assert frame['count']==1;node=graphics.geometry[frame['start']];assert node['header'][1]&255==1
 shape=node['header'][3];_,begin,count=struct.unpack_from('<3I',graphics.b,shape_start+shape*28);assert count==9
 pieces=[]
 for i in range(count):
  a=batch_start+(begin+i)*44;flags,_,bitmap=struct.unpack_from('<3I',graphics.b,a);matrix=struct.unpack_from('<6f',graphics.b,a+12);off,num=struct.unpack_from('<2I',graphics.b,a+36);assert num==6
  points=[struct.unpack_from('<2f',graphics.b,vertex_start+(off+j)*8) for j in range(num)];xs=[p[0] for p in points];ys=[p[1] for p in points];image=graphics.images[bitmap];filename=name.lower()+'_'+str(i);(art/(filename+'.png')).write_bytes(portable_png(image['bytes']));pieces.append({'texture':filename,'rect':[min(xs),min(ys),max(xs)-min(xs),max(ys)-min(ys)],'matrix':matrix,'source_sha256':sha(image['bytes']),'sha256':sha(portable_png(image['bytes']))})
 grid=[struct.unpack('<f',struct.pack('<I',x))[0] for x in definition[5:9]]
 data['panels'][name]={'grid':grid,'pieces':pieces}
data['backgrounds']=[{'panel':'FJ_Panel9Grid' if g['index']==82 else 'FJ_SquareRecessedPanel','matrix':g['matrix']} for g in scene.geometry if g['index'] in [82,95,103]]
english=json.loads((reference/'english.json').read_text());keys=['133e5cd4','b35ee68b','dd9417ba','f8e4117f','8078384b','7f57b5e0','93d2882c','f6bfb839','d0d94a48','dcca69ba','7a3f69fa','529a620a','80ac0ce1','3a0766fb','d047a3ce','5f116e91','ded2ef45','9219698b','91d18454','f73b4f2d','06974812','8d6323ed','fe4f3a12','1a4e966d','cb14bd42','a89bcc3a','6ca19582','47c65cd1','ae01f627','63d85f8f','ccf37993','52c6df2e','a4fbd60e','6bb77ede','f36c79ed','d2631070','48008e1a','acc3dd75','68615d41','86f48c5a','2fccdffb']
data['palette']=json.loads((reference/'palette-native.json').read_text())
data['tab_patterns']=list(json.loads((reference/'tab-patterns-native.json').read_text()).values())
data['channel_conversion']='PS4 BGRA to Java RGBA; no other pixel changes'
# VertTab's two authored frames use different item-holder offsets. The off
# frame moves the holder left by eight units as the tab retracts behind the panel.
data['vertical_tab_icons']={}
for state,index in [('on',51),('off',53)]:
 node=scene.geometry[index]
 assert node['header'][1]&255==2 if state=='on' else node['header'][1]&255==1
 data['vertical_tab_icons'][state]=node['matrix']
data['description_panel']=list(scene.geometry[125]['matrix'])+[7.578369140625,4.171844482421875]
data['labels']={k:english[k] for k in keys}
(dest/'ps4_crafting.json').write_text(json.dumps(data,indent=2)+'\n')
patterns=json.loads((reference/'patterns-native.json').read_text())
special={'bri':'bricks','cre':'creeper_head','cbo':'vine','flo':'oxeye_daisy','moj':'enchanted_golden_apple','sku':'wither_skeleton_skull','brip':'field_masoned_banner_pattern','crep':'creeper_banner_pattern','cbop':'bordure_indented_banner_pattern','flop':'flower_banner_pattern','mojp':'mojang_banner_pattern','skup':'skull_banner_pattern'}
converted=[]
for row in patterns:
 if row['hash']=='b':continue
 key=row['hash'];converted.append({'hash':key[:-1] if key in ['brip','crep','cbop','flop','mojp','skup'] else key,'source_hash':key,'group':row['base_type']-48,'crafting_type':5 if key.endswith('p') and key in special else 1,'shape':row['shape'],'special':special.get(key),'reusable':key.endswith('p') and key in special,'ps4_id':row['id'],'ps4_ordinal':row['ordinal']})
constructors=json.loads((reference/'pattern-constructors-native.json').read_text())
# The second vector in BannerPattern::StaticCtor contains 32 loom patterns.
# BannerRecipes::addRecipes 0x4e1e0..0x4e299 registers these as type 5,
# each with one banner and one dye. They have no paper template or shape grid.
loom=[r for r in constructors if r['vector']==['data',0x14098d8]]
assert len(loom)==32
for row in loom:
 converted.append(dict(hash=row['hash'],source_hash='loom:'+row['hash'],group=row['base_type']-48,
  crafting_type=5,shape=['   ']*3,special=None,reusable=False,loom=True,
  ps4_id=row['id'],ps4_ordinal=row['ordinal'],registration_pc=row['pc']))
(dest/'ps4_banner_patterns.json').write_text(json.dumps(converted,indent=2)+'\n')
print('Exported',len(data['sprites']),'sprites,',len(data['panels']),'9-grid panels,',len(converted),'pattern recipes')
