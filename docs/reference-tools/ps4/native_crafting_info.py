"""Bounded original PS4 display-mode counter and FUI visibility calls."""
import argparse,platform,sys,struct,ctypes as C,mmap,json
from pathlib import Path
sys.path.insert(0,str(Path(__file__).resolve().parent))
p=argparse.ArgumentParser();p.add_argument('--eboot',type=Path,required=True);p.add_argument('--output',type=Path,required=True);args=p.parse_args()
if platform.machine() not in ["x86_64","AMD64"]:p.error("Native comparisons require an x86-64 host")
from read_block_constructors import Source
from native_module import Module
s=Source(args.eboot);m=Module(args.eboot)
code=b'\x41\x56\x49\x89\xfe'+s.read(0x9ac698,0x20)+b'\x41\x5e\xc3'
b=mmap.mmap(-1,4096,prot=mmap.PROT_READ|mmap.PROT_WRITE|mmap.PROT_EXEC);b.write(code);fn=C.CFUNCTYPE(None,C.c_void_p)(C.addressof(C.c_char.from_buffer(b)))
obj=C.create_string_buffer(0x1000);transitions=[]
for value in [0,1,2]*4:
 C.memmove(C.addressof(obj)+0xe18,struct.pack('<i',value),4);fn(obj);transitions.append({'before':value,'after':struct.unpack('<i',obj.raw[0xe18:0xe1c])[0]})
keep=[];shown={};widgets={};counter=C.c_uint64(0);m.w(0x134bae8,'Q',C.addressof(counter))
def allocate(size):
 x=C.create_string_buffer(size);keep.append(x);return C.addressof(x)
@C.CFUNCTYPE(C.c_uint64,C.c_void_p,C.c_uint64,C.c_uint64)
def reserve(p,n,zero):
 q=allocate(n+1);C.memmove(p+8,struct.pack('<Q',q),8);C.memmove(p+32,struct.pack('<Q',max(n,16)),8);return 1
@C.CFUNCTYPE(None,C.c_void_p)
def free(p):pass
@C.CFUNCTYPE(None,C.c_void_p,C.c_uint64,C.c_uint64)
def show(p,v,unused):shown[widgets[p]]=bool(v)
vt=allocate(0x40);C.memmove(vt+0x30,struct.pack('<Q',C.cast(show,C.c_void_p).value),8)
@C.CFUNCTYPE(C.c_void_p,C.c_void_p,C.c_void_p)
def lookup(root,p):
 n=C.c_uint64.from_address(p+24).value;cap=C.c_uint64.from_address(p+32).value
 address=C.c_uint64.from_address(p+8).value if cap>15 else p+8
 name=C.string_at(address,n).decode();q=allocate(64);C.memmove(q,struct.pack('<Q',vt),8);widgets[q]=name;return q
m.patch(0x676a0,reserve);m.patch(0xe6cfb0,free);m.patch(0x7cff60,lookup)
for it in s.instructions(0x7f8d70):
 if it.mnemonic=='call' and it.op_str.startswith('0x') and int(it.op_str,16) not in [0x676a0,0xe6cfb0,0x7cff60,0xe6d030]:raise ValueError(('Unexpected call',hex(it.address),it.op_str))
visibility=[]
for mode in range(3):
 root=allocate(0x300);C.memmove(root+0xb4,b'\1'*5,5);C.memmove(root+0xa4,b'\xff'*4,4);shown.clear();m.function(0x7f8d70,None,[C.c_void_p,C.c_int])(root,mode);visibility.append({'mode':mode,'controls':dict(shown)})
out={'repository':'Nejirus/mcps4','revision':'37e258085773f11599cf0ffe29119926769554b7','counter_span':['0x9ac698','0x9ac6b8'],'visibility_function':'0x7f8d70','transitions':transitions,'visibility':visibility}
args.output.write_text(json.dumps(out,indent=2)+'\n');print(json.dumps(out,indent=2))
