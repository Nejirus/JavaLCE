import os,sys,json
from pathlib import Path
sys.path.insert(0,str(Path(__file__).resolve().parent.parent))
from read_block_constructors import Source,RootFlow

class Descriptions(RootFlow):
 def value(self,it,op):
  v=super().value(it,op)
  if isinstance(v,tuple) and v[0]=='method' and v[2]==0x370:
   obj=v[1]
   if isinstance(obj,tuple) and obj[0]=='object':
    self.objects[obj[1]]['description']={'pc':hex(it.address),'name_hash':f'{self.regs.get("rsi",0):08x}','description_hash':f'{self.regs.get("rdx",0):08x}'}
  return v
source=Source(Path(os.environ['MCPS4_EBOOT']));root=Descriptions(source).run()
english=json.loads(Path(os.environ['PS4_ENGLISH_JSON']).read_text());out=[]
for r in root.rows:
 d=r['allocation'].get('description')
 if d and d['description_hash'] in english:out.append(dict(name=r['name'],native_id=r['id'],**d,text=english[d['description_hash']]))
(Path(os.environ['PS4_TUTORIAL_WORK'])/Path('native-block-descriptions.json')).write_text(json.dumps(out,indent=2)+'\n');print(len(out),'original PS4 block descriptions')
