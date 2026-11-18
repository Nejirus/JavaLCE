"""Extract the canonical PS4 tutorial save and GRF without executing game code."""
import argparse,hashlib,json,struct,zlib
from pathlib import Path
from nbt_tools import unrle
from read_grf import Reader
p=argparse.ArgumentParser();p.add_argument('--mcs',type=Path,required=True);p.add_argument('--pck',type=Path,required=True);p.add_argument('--output',type=Path,required=True);a=p.parse_args()
a.output.mkdir(parents=True,exist_ok=True)
raw=a.mcs.read_bytes();size=struct.unpack_from('<I',raw,4)[0];b=zlib.decompress(raw[8:]);assert len(b)==size
table,count,major,minor=struct.unpack_from('<IIHH',b);assert (major,minor)==(7,7)
world=a.output/'raw-world';world.mkdir(exist_ok=True)
for i in range(count):
 row=table+i*144;name=b[row:row+128].decode('utf-16le').split('\0')[0];size,offset=struct.unpack_from('<II',b,row+128)
 target=world/name;assert target.resolve().is_relative_to(world.resolve());target.parent.mkdir(parents=True,exist_ok=True);target.write_bytes(b[offset:offset+size])
# PCK v3: count followed by (length,type,UTF16 name length/name,flags),
# then each payload with its four-byte wrapper prefix.
b=a.pck.read_bytes();version,flags,count=struct.unpack_from('<III',b);assert version==3
cursor=12;files=[]
for i in range(count):
 length,kind,n=struct.unpack_from('<III',b,cursor);cursor+=12
 name=b[cursor:cursor+n*2].decode('utf-16le');cursor+=n*2+4;files.append((name,length))
for name,length in files:
 payload=b[cursor:cursor+length+4];cursor+=length+4
 if name=='GameRules.grf':
  # GRF compressed wrapper: 23-byte header, zlib then console RLE.
  data=unrle(zlib.decompress(payload[23:]));reader=Reader(data)
  (a.output/'Tutorial.pck.grf.json').write_text(json.dumps(reader.read(),indent=2)+'\n')
assert cursor==len(b)
ref=a.output/'reference';ref.mkdir(exist_ok=True);(ref/'Tutorial.mcs').write_bytes(raw)
print('Extracted',count,'PCK entries and canonical PS4 tutorial save')
