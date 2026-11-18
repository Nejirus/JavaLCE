"""Pure secondary data lookup and bounded original wrapper value selection.

Only 0x2246a8..0x2246d0 / 0x2246de..0x2246ec of the wrapper execute, with
entry registers supplied and exits before TLS. Original 0x35bc60/0x35bf10
execute unchanged. The normal and primary-high-bit paths are native outputs.
"""
import hashlib,struct,subprocess,tempfile
from pathlib import Path
from native_compressed_sections import CompressedSectionsOracle,C,P,I

class SecondaryStorageOracle(CompressedSectionsOracle):
    def __init__(self,executable,library):
        super().__init__(executable,library)
        for pc,expected in [(0x2246a8,b'\x45\x31\xc0\x4c\x89\xf7'),(0x2246d0,b'\x48\x8d\x0d'),(0x2246ec,b'\x64\x48\x8b\x04\x25\0\0\0\0')]:
            if C.string_at(self.game.base+pc,len(expected))!=expected:raise ValueError('Secondary wrapper boundary bytes changed')
        self.temporary=tempfile.TemporaryDirectory(prefix='ps4-secondary-storage-');compiled=Path(self.temporary.name)/'capture.so'
        subprocess.run(['cc','-shared','-fPIC',str(Path(__file__).with_name('compressed_secondary_capture.S')),'-o',str(compiled)],check=True)
        self.capture=C.CDLL(str(compiled));self.capture.run_secondary_body.argtypes=[P,P,I,I,I];self.capture.run_secondary_body.restype=I
        # RET preserves the pure secondary helper's EAX. The high-bit exit
        # returns its original R15D selection before any FS/TLS instruction.
        C.memmove(self.game.base+0x2246d0,b'\xc3',1);C.memmove(self.game.base+0x2246ec,b'\x44\x89\xf8\xc3',4)
        self.primary_function=self.game.function(0x35bc60,I,[P,I,I,I,P]);self.secondary_function=self.game.function(0x35bf10,I,[P,I,I,I])
    def values(self,row):
        self.load(row['section_y'],row['allocated'],row['section_offset'],row['headers'],row['patches']);origin=row['read_origin'];digest=hashlib.sha256()
        if not 0<=origin[1]<=252:raise ValueError('Secondary body fixture requires Y 0..255')
        for x in range(4):
            for z in range(4):
                for y in range(4):
                    args=[self.storage,origin[0]+x,origin[1]+y,origin[2]+z]
                    primary=self.primary_function(*args,0)&65535;stored=self.secondary_function(*args)&65535;selected=self.capture.run_secondary_body(self.game.base+0x2246a8,*args)&65535
                    digest.update(struct.pack('<HHH',primary,stored,selected))
        return {k:row[k] for k in ['section_y','allocated','section_offset','headers','patches','read_origin']}|{'samples':64,'read_sha256':digest.hexdigest()}
