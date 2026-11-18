import os
from pathlib import Path
import struct,zlib,json
from collections import Counter,defaultdict
from nbt_tools import Nbt,plain,unrle

def chunks(path):
 b=path.read_bytes()
 for index in range(1024):
  location=struct.unpack_from('<I',b,index*4)[0]
  if not location:continue
  offset=(location>>8)*4096
  length,rawsize=struct.unpack_from('<II',b,offset)
  data=zlib.decompress(b[offset+8:offset+8+(length&0x7fffffff)])
  if length&0x80000000:data=unrle(data)
  if len(data)!=rawsize:raise ValueError((path,index,len(data),rawsize))
  name,root=Nbt(data).root()
  yield index,root

if __name__=='__main__':
 blocks=Counter();variants=defaultdict(Counter);entities=Counter();tiles=Counter();items=Counter();examples={};count=0
 root=Path(os.environ['PS4_TUTORIAL_WORK'])/Path('raw-world')
 for path in sorted(root.rglob('*.mcr')):
  total=0
  for index,chunk in chunks(path):
   count+=1;total+=1;level=chunk.value['Level'].value
   a=level['Blocks'].value;d=level['Data'].value
   for i,id in enumerate(a):
    blocks[id]+=1
    variants[id][(d[i>>1]>>((i&1)*4))&15]+=1
   for entity in level.get('Entities').value[1]:
    v=plain(entity,True);id=v.get('id');entities[id]+=1;examples.setdefault('entity:'+str(id),v)
   for tile in level.get('TileEntities').value[1]:
    v=plain(tile,True);id=v.get('id');tiles[id]+=1;examples.setdefault('tile:'+str(id),v)
    for item in v.get('Items',[]):items[str(item.get('id'))]+=1
  print(path.relative_to(root),total,flush=True)
 result={'chunks':count,'blocks':dict(blocks),'variants':{str(k):dict(v) for k,v in variants.items()},'entities':dict(entities),'tiles':dict(tiles),'items':dict(items),'examples':examples}
 (Path(os.environ['PS4_TUTORIAL_WORK'])/Path('world-scan.json')).write_text(json.dumps(result,indent=2)+'\n')
 print('chunks',count,'blocks',len(blocks),'entities',dict(entities),'tiles',dict(tiles),'items',dict(items))
