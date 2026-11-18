import os,sys,json
from pathlib import Path
sys.path.insert(0,str(Path(__file__).resolve().parent.parent))
from read_block_constructors import Source,RootFlow
class EnumSource(Source):
 def instructions(self,pc,length=None):
  if pc==0x957a0:return super().instructions(0x67f1f0,0x67fcd0-0x67f1f0)
  return super().instructions(pc,length)
s=EnumSource(Path(os.environ['MCPS4_EBOOT']));f=RootFlow(s).run();en=json.loads(Path(os.environ['PS4_ENGLISH_JSON']).read_text());out=[]
for p,a in f.objects.items():
 variant=f.memory.get(('object',p,8));hashes=f.memory.get(('object',p,0x38));hashes=hashes&0xffffffffffffffff if isinstance(hashes,int) else hashes
 if isinstance(variant,int) and isinstance(hashes,int) and f'{hashes>>32:08x}' in en:
  out.append(dict(native_id=1,aux=variant,description_hash=f'{hashes>>32:08x}',name_hash=f'{hashes&0xffffffff:08x}',text=en[f'{hashes>>32:08x}'],allocation_pc=hex(p)))
(Path(os.environ['PS4_TUTORIAL_WORK'])/Path('native-stone-descriptions.json')).write_text(json.dumps(out,indent=2)+'\n');print(out)
