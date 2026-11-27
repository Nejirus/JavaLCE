"""Copy LCE's Xbox 360 PNGs and read normal XUI glyph geometry. No image editing.

The Windows SWFs contain different controller artwork and are not Xbox 360 skins.
HD uses the same original PNGs with the existing 720-to-1080 geometry conversion.
"""
import argparse,copy,hashlib,json,shutil,struct,xml.etree.ElementTree as E
from pathlib import Path
p=argparse.ArgumentParser();p.add_argument('--reference-dir',type=Path,required=True);p.add_argument('--tree',type=Path,required=True);p.add_argument('--project-root',type=Path,required=True);a=p.parse_args()
revision='acff48c2c7e18406fd6000b56945746ca9a6eb69'
tree=json.loads(a.tree.read_text());assert tree['sha']==revision;blobs={n['path']:n for n in tree['tree']}
base='Minecraft.Client/Common/Media/';xml=base+'skin_Minecraft.xui';raw=(a.reference_dir/xml).read_bytes()
def sha(b):return hashlib.sha256(b).hexdigest()
def verify(path):
 b=(a.reference_dir/path).read_bytes();assert hashlib.sha1(b'blob '+str(len(b)).encode()+b'\0'+b).hexdigest()==blobs[path]['sha'],path;return b
verify(xml);visuals={n.findtext('Properties/Id'):n for n in E.fromstring(raw).iter('XuiVisual')}
assets=a.project_root/'src/main/resources/assets/legacyconsole';dest=assets/'textures/lce/prompts/xbox360';dest.mkdir(parents=True,exist_ok=True);records={}
def image(path):
 b=verify(path);name=Path(path).stem;out=dest/(name+'.png');out.write_bytes(b)
 records[path]={'source':path,'git_blob':blobs[path]['sha'],'sha256':sha(b),'destination':str(out.relative_to(a.project_root)),'size':struct.unpack('>II',b[16:24])}
 return 'lce/prompts/xbox360/'+name
roles={'cross':('A_Button',{'graphic_button_normal','A_graphic'}),'circle':('B_Button',{'graphic_button_normal','B_graphic'}),'square':('X_Button',{'graphic_button_normal','x_graphic'}),'triangle':('Y_Button',{'graphic_button_normal','y_graphic'}),'left_bumper':('LB_Button',None),'right_bumper':('RB_Button',None),'left_trigger':('LTrigger',None),'right_trigger':('RTrigger',None),'left_stick':('LStick_Nav',None)}
glyphs={}
for role,(symbol,ids) in roles.items():
 layers=[]
 for node in visuals[symbol].findall('XuiImage'):
  props={n.tag:n.text for n in node.find('Properties')}
  if ids is not None and props['Id'] not in ids:continue
  x,y,*_=map(float,props.get('Position','0,0,0').split(','));w,h=float(props['Width']),float(props['Height']);path=base+props['ImagePath'].replace('\\','/');layers.append(dict(path=image(path),x=x,y=y,width=w,height=h))
 assert layers
 glyphs[role]=dict(bounds=[min(n['x'] for n in layers),max(n['x']+n['width'] for n in layers),min(n['y'] for n in layers),max(n['y']+n['height'] for n in layers)],layers=layers,frame=symbol)
extra={'left_stick_button':'ButtonLS','right_stick_button':'ButtonRS','right_stick':'ButtonRightStick','start_button':'ButtonStart','back_button':'ButtonBack','dpad_up':'ButtonDpadU','dpad_down':'ButtonDpadD','dpad_left':'ButtonDpadL','dpad_right':'ButtonDpadR'}
for role,name in extra.items():
 path=base+'Graphics/X360ControllerIcons/'+name+'.png';tex=image(path);w,h=records[path]['size'];glyphs[role]=dict(bounds=[0,w,0,h],layers=[dict(path=tex,x=0,y=0,width=w,height=h)],symbol=name)
# The supplementary RS image is a controller-diagram bitmap. Fit its display
# to the authored A-button row while preserving the original bitmap's aspect.
rs = glyphs['right_stick_button']
height = glyphs['cross']['bounds'][3] - glyphs['cross']['bounds'][2]
factor = height / rs['layers'][0]['height']
rs['bounds'] = [value * factor for value in rs['bounds']]
for key in ['x', 'y', 'width', 'height']:
 rs['layers'][0][key] *= factor
p=assets/'glyphs.json';data=json.loads(p.read_text())
for name,mult in [('xbox360',1),('xbox360_hd',1.5)]:
 family=data[name];family['glyphs']=copy.deepcopy(glyphs)
 for g in family['glyphs'].values():
  g['bounds']=[x*mult for x in g['bounds']]
  for layer in g['layers']:
   for key in ['x','y','width','height']:layer[key]*=mult
p.write_text(json.dumps(data,indent=2)+'\n')
record=dict(repository='https://github.com/chrisGrando/MinecraftLCE-7thGen',revision=revision,source_xui={'path':xml,'git_blob':blobs[xml]['sha'],'sha256':sha(raw)},assets=list(records.values()),glyphs=glyphs,adaptations=['Normal Xbox 360 XUI glyph images and geometry replace Windows SWF glyphs. Existing common prompt text/spacing and bindings remain.','At 1080p and above the same unchanged PNGs use the existing 3/2 coordinate conversion; no Xbox 360 HD artwork is invented.','Supplementary glyphs use original Button PNGs. The RS display fits the authored A-button height and preserves its source aspect ratio.'])
(a.project_root/'LCE-XBOX360-2312.json').write_text(json.dumps(record,indent=2)+'\n');print('Verified/copied',len(records),'original PNGs;',len(glyphs),'glyphs per scale')
