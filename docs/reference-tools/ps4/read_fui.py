from pathlib import Path
import struct,json,zlib,sys
from PIL import Image,ImageDraw
from io import BytesIO

class Fui:
 def __init__(self,path):
  self.path=Path(path);self.b=b=self.path.read_bytes()
  assert b[:4]==b'\x01IUF'
  self.counts=struct.unpack_from('<15I',b,0x4c)
  nd,nn,np,n4,n5,n6,nf,ng,ni,n10,ne,nb,ndata,n14,nl=self.counts
  self.dataStart=len(b)-ndata
  self.bitmapStart=self.dataStart-nb*32
  self.libraryStart=self.bitmapStart-nl*64
  self.exportStart=self.libraryStart-ne*72
  # Mixed libraries store imports before text fields/font descriptors.
  self.importStart=self.exportStart-ni*72-n10*312-n14*260
  self.nameStart=self.importStart-nn*64
  self.geometryStart=self.nameStart-ng*72
  self.frameStart=self.geometryStart-nf*72
  self.names=[b[self.nameStart+i*64:self.nameStart+(i+1)*64].split(b'\0')[0].decode() for i in range(nn)]
  textStart=self.importStart+ni*72
  self.textfields=[]
  for i in range(n10):
   a=textStart+i*312
   self.textfields.append({'bounds':struct.unpack_from('<4f',b,a+4),'font_size':struct.unpack_from('<f',b,a+24)[0],
    'color':struct.unpack_from('<I',b,a+28)[0],'alignment':b[a+32],
    'markup':b[a+56:a+312].split(b'\0')[0].decode()})
  self.imports=[b[self.importStart+i*72+4:self.importStart+i*72+68].split(b'\0')[0].decode() for i in range(ni)]
  self.exports={b[self.exportStart+i*72:self.exportStart+i*72+64].split(b'\0')[0].decode():struct.unpack_from('<2I',b,self.exportStart+i*72+64) for i in range(ne)}
  self.defs=[struct.unpack_from('<I4H4I',b,0x98+i*28) for i in range(nd)]
  self.frames=[]
  for i in range(nf):
   a=self.frameStart+i*72;self.frames.append({'start':struct.unpack_from('<I',b,a+64)[0],'count':struct.unpack_from('<I',b,a+68)[0],'label':b[a:a+64].split(b'\0')[0].decode()})
  self.geometry=[]
  for i in range(ng):
   a=self.geometryStart+i*72;h=struct.unpack_from('<6H',b,a);m=struct.unpack_from('<6f',b,a+12);self.geometry.append({'index':i,'name':self.names[h[5]] if h[5]<nn else '', 'header':h,'matrix':m})
  self.images=[]
  for i in range(nb):
   record=struct.unpack_from('<8I',b,self.bitmapStart+i*32)
   start=self.dataStart+record[4];end=start+record[5]
   assert self.dataStart<=start<end<=len(b)
   data=b[start:end]
   image=Image.open(BytesIO(data));assert image.size==record[2:4]
   self.images.append({'index':i,'bytes':data,'size':image.size,'record':record})
  assert not nb or max(self.dataStart+x['record'][4]+x['record'][5] for x in self.images)==len(b)
 def dump(self):
  return {'counts':self.counts,'names':self.names,'imports':self.imports,'exports':self.exports,'defs':self.defs,'frames':self.frames,'geometry':self.geometry,'images':[{k:v for k,v in x.items() if k!='bytes'} for x in self.images]}

