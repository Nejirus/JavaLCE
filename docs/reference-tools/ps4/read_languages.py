"""Read the original big-endian languages.loc v2 directory and UTF-8 string blocks."""
import struct


def read_languages(data):
    version, count = struct.unpack_from('>2I', data)
    assert version == 2 and data[8] == 1
    strings = struct.unpack_from('>I', data, 9)[0]
    ids = struct.unpack_from('>'+str(strings)+'I', data, 13)
    cursor = 13+strings*4
    directory=[]
    for _ in range(count):
        length=struct.unpack_from('>H',data,cursor)[0];cursor+=2
        name=data[cursor:cursor+length].decode();cursor+=length
        size=struct.unpack_from('>I',data,cursor)[0];cursor+=4
        directory.append((name,size))
    result={}
    for name,size in directory:
        begin=cursor
        assert struct.unpack_from('>I',data,cursor)[0] == 2 and data[cursor+4] == 1
        cursor+=5
        length=struct.unpack_from('>H',data,cursor)[0];cursor+=2
        assert data[cursor:cursor+length].decode()==name;cursor+=length
        assert struct.unpack_from('>I',data,cursor)[0]==strings;cursor+=4
        values=[]
        for _ in range(strings):
            length=struct.unpack_from('>H',data,cursor)[0];cursor+=2
            values.append(data[cursor:cursor+length].decode());cursor+=length
        assert cursor-begin==size
        result[name]={f'{key:08x}':value for key,value in zip(ids,values)}
    assert cursor==len(data)
    return result
