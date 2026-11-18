"""Convert the original mcps4 Tutorial.mcs container to a Java Anvil intermediate.

The PS4 block IDs/data are the original pre-flattening IDs. Java's own DataFixer
performs the subsequent schema conversion; this stage only transposes storage.
"""
from pathlib import Path
import os,struct,zlib,json,gzip,hashlib
import numpy as np
from nbt_tools import Nbt,Tag,encode,plain,unrle
from scan_world import chunks

ROOT=Path(os.environ['PS4_TUTORIAL_WORK'])
OUT=ROOT/'anvil-intermediate-v2'
OUT.mkdir(exist_ok=True)
rules=json.loads((ROOT/'Tutorial.pck.grf.json').read_text())
levelrules=rules[1]['children']
populate={}
for n in levelrules:
 if n['type']=='OnInitialiseWorld':
  for c in n['children']:
   a=c['attributes'];populate[tuple(int(a[d]) for d in ['x','y','z'])]=c

def item(n):
 a=n['attributes'];v={
 'id':Tag(2,int(a['itemId'])),'Count':Tag(1,int(a.get('quantity','1'))),
 'Damage':Tag(2,int(a.get('auxValue','0'))),'Slot':Tag(1,int(a.get('slot','0')))}
 tag={}
 if int(a.get('dataTag','0')):tag['4jdata']=Tag(3,int(a['dataTag']))
 enchant=[]
 for c in n['children']:
  if c['type']!='AddEnchantment':raise ValueError(c)
  enchant.append(Tag(10,{'id':Tag(2,int(c['attributes']['enchantmentId'])),'lvl':Tag(2,int(c['attributes']['enchantmentLevel']))}))
 if enchant:tag['ench']=Tag(9,(10,enchant))
 if tag:v['tag']=Tag(10,tag)
 return Tag(10,v)

def convert(root):
 level=root.value['Level'].value
 original=level['Blocks'].value
 # PS4 x/z/y -> Java section y/z/x.
 # The original stores two 128-high XZY slabs: low half, then high half.
 def transpose(b):return np.frombuffer(b,dtype=np.uint8).reshape(2,16,16,128).transpose(0,3,2,1).reshape(16,4096)
 def nibbles(b):
  a=np.frombuffer(b,dtype=np.uint8);v=np.empty(65536,dtype=np.uint8);v[::2]=a&15;v[1::2]=a>>4
  v=v.reshape(2,16,16,128).transpose(0,3,2,1).reshape(16,4096)
  return v[:,::2]|(v[:,1::2]<<4)
 blocks=transpose(original);arrays={name:nibbles(level[name].value) for name in ['Data','BlockLight','SkyLight']}
 sections=[]
 for sy in range(16):
  if not np.any(blocks[sy]):continue
  s={'Y':Tag(1,sy),'Blocks':Tag(7,blocks[sy].tobytes())}
  for name,v in arrays.items():s[name]=Tag(7,v[sy].tobytes())
  sections.append(Tag(10,s))
 level['Sections']=Tag(9,(10,sections))
 level['TerrainPopulated']=Tag(1,1);level['LightPopulated']=Tag(1,0)
 level['HeightMap']=Tag(11,list(level['HeightMap'].value))
 level['InhabitedTime']=Tag(4,0)
 for key in ['Blocks','Data','BlockLight','SkyLight','version','TerrainPopulatedFlags']:level.pop(key,None)
 for tile in level['TileEntities'].value[1]:
  v=tile.value;pos=tuple(v[k].value for k in ['x','y','z'])
  if pos in populate:
   old={i.value['Slot'].value:i for i in v.get('Items',Tag(9,(10,[]))).value[1]}
   for n in populate.pop(pos)['children']:
    it=item(n);old[it.value['Slot'].value]=it
   v['Items']=Tag(9,(10,list(old.values())))
 root.value['DataVersion']=Tag(3,0)
 return root

report={'regions':[],'chunks':0,'source_mcs_sha256':hashlib.sha256((ROOT/'reference/Tutorial.mcs').read_bytes()).hexdigest()}
regions={};region_counts={}
for path in sorted((ROOT/'raw-world').rglob('*.mcr')):
 relative=str(path.relative_to(ROOT/'raw-world'))
 if relative.startswith('DIM-1'):parent=OUT/'DIM-1/region'
 elif relative.startswith('DIM1/'):parent=OUT/'DIM1/region'
 else:parent=OUT/'region'
 parent.mkdir(parents=True,exist_ok=True)
 count=0
 for index,root in chunks(path):
  root=convert(root)
  level=root.value['Level'].value;cx=level['xPos'].value;cz=level['zPos'].value
  # Original console region files here cover 16x16 chunks. Java Anvil files
  # cover 32x32; re-bucket by the chunk's saved coordinates, never by filename.
  dest=parent/f'r.{cx>>5}.{cz>>5}.mca';index=(cx&31)+(cz&31)*32
  data=regions.setdefault(dest,bytearray(8192));region_counts[dest]=region_counts.get(dest,0)+1
  if struct.unpack_from('>I',data,index*4)[0]:raise ValueError(('Duplicate source chunk',cx,cz))
  b=zlib.compress(encode(root));sectors=(len(b)+5+4095)//4096;offset=len(data)//4096
  struct.pack_into('>I',data,index*4,(offset<<8)|sectors)
  payload=struct.pack('>IB',len(b)+1,2)+b;data.extend(payload);data.extend(b'\0'*(sectors*4096-len(payload)));count+=1
  report['chunks']+=1
 print(relative,count,flush=True)
for dest,data in regions.items():
 dest.write_bytes(data);report['regions'].append({'path':str(dest.relative_to(OUT)),'chunks':region_counts[dest]})
if populate:raise ValueError(('Unmatched source initialisation containers',populate.keys()))
name,root=Nbt((ROOT/'raw-world/level.dat').read_bytes()).root();data=root.value['Data'].value
data['version']=Tag(3,19133);data['DataVersion']=Tag(3,0)
data['LevelName']=Tag(8,'PS4 Tutorial');data['GameType']=Tag(3,0);data['allowCommands']=Tag(1,0)
data['hardcore']=Tag(1,0);data['initialized']=Tag(1,1);data['Difficulty']=Tag(1,2);data['DifficultyLocked']=Tag(1,1)
data['BorderCenterX']=Tag(6,0.0);data['BorderCenterZ']=Tag(6,0.0);data['BorderSize']=Tag(6,864.0)
data['BorderSizeLerpTarget']=Tag(6,864.0);data['BorderSizeLerpTime']=Tag(4,0)
data['LastPlayed']=Tag(4,0);data.pop('Player',None)
data['GameRules']=Tag(10,{'doDaylightCycle':Tag(8,'false')})
with gzip.open(OUT/'level.dat','wb') as f:f.write(encode(root))
# The marker is world-specific. Client preference switches cannot enable the
# starting-area restrictions in unrelated worlds.
marker=Tag(10,{'data':Tag(10,{'Version':Tag(3,1),'PS4Tutorial':Tag(1,1)})})
(OUT/'data').mkdir(exist_ok=True)
with gzip.open(OUT/'data/legacyconsole_ps4_tutorial.dat','wb') as f:f.write(encode(marker))
for source in (ROOT/'raw-world/data').glob('*.dat'):
 original=source.read_bytes();Nbt(original).root()
 (OUT/'data'/source.name).write_bytes(gzip.compress(original,mtime=0))
(ROOT/'convert-world-report.json').write_text(json.dumps(report,indent=2)+'\n')
print('total',report['chunks'])
