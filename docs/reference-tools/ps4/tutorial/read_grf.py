import os
from pathlib import Path
from io import BytesIO
import struct,json
class Reader:
 def __init__(self,b):self.f=BytesIO(b)
 def i(self):return struct.unpack('>I',self.f.read(4))[0]
 def text(self):return self.f.read(struct.unpack('>H',self.f.read(2))[0]).decode()
 def read(self):
  self.names=[self.text() for _ in range(self.i())]
  self.files=[]
  for _ in range(self.i()):
   name=self.text();data=self.f.read(self.i());self.files.append((name,data))
  return [self.node() for _ in range(self.i())]
 def node(self):
  name=self.names[self.i()];attrs={}
  for _ in range(self.i()):
   key=self.names[self.i()];attrs[key]=self.text()
  return dict(type=name,attributes=attrs,children=[self.node() for _ in range(self.i())])
if __name__=='__main__':
 for n in ['Tutorial.pck.grf','WorldPack.pck.grf']:
  r=Reader((Path(os.environ['PS4_TUTORIAL_WORK'])/Path(''+n+'.unrle')).read_bytes());a=r.read();(Path(os.environ['PS4_TUTORIAL_WORK'])/Path(''+n+'.json')).write_text(json.dumps(a,indent=2)+'\n');print(n,'root',[(x['type'],x['attributes']) for x in a]);print('end',r.f.tell())
  from collections import Counter
  counts=Counter()
  def walk(n):
   counts[n['type']]+=1
   if n['type'] in ['NamedArea','CompleteAll']: print(n['type'],n['attributes'])
   for c in n['children']:walk(c)
  for n in a:walk(n)
  print(counts)
