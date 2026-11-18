"""Read unmodified, big-endian NBT input assets for the native capture harness."""
import gzip,io,struct


def read_nbt(path):
    stream=io.BytesIO(gzip.decompress(path.read_bytes()))
    def scalar(fmt):return struct.unpack('>'+fmt,stream.read(struct.calcsize('>'+fmt)))[0]
    def string():return stream.read(scalar('H')).decode('utf-8')
    def payload(kind):
        if kind in range(1,7):return scalar(['b','h','i','q','f','d'][kind-1])
        if kind==7:return stream.read(scalar('i'))
        if kind==8:return string()
        if kind==9:
            element=scalar('B');return [payload(element) for _ in range(scalar('i'))]
        if kind==10:
            result={}
            while True:
                element=scalar('B')
                if element==0:return result
                name=string();result[name]=payload(element)
        if kind in [11,12]:return [scalar('i' if kind==11 else 'q') for _ in range(scalar('i'))]
        raise ValueError('Unsupported NBT input tag '+str(kind))
    kind=scalar('B');string();result=payload(kind)
    if stream.read(1):raise ValueError('Trailing NBT input bytes')
    return result
