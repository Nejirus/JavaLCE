"""Original biome constructors, temperature virtuals and their native noise.

The existing biome initializer harness supplies registry/feature-object storage.
Temperature fields and frozen temperature noise are constructor outputs. Shared
noise objects use the original constructor and original seed/count inputs.
"""
from native_biome_settings import BiomeSettingsOracle,C,P,U,I
from native_carvers import F
from native_surfaces import SurfaceOracle
import struct


class BiomeTemperatureOracle(BiomeSettingsOracle):
    def __init__(self,executable,library):
        super().__init__(executable,library)
        self.simplex_numeric_initializers=[]
        for alias,pc in [(0x1430180,0x6ac2fe),(0x1430188,0x6ac310)]:
            if C.string_at(self.game.base+pc,2)!=b'\x48\xb9':raise ValueError('Original Simplex numeric initializer changed')
            bits=self.game.read(pc+2,'Q');self.game.w(alias,'Q',bits)
            self.simplex_numeric_initializers.append({'alias':hex(alias),'immediate_pc':hex(pc),'bits':f'{bits:016x}'})
        self.shared_noise_inputs=[]
        for alias,seed_pc,count_pc in [(0x140ab00,0x151296,0x1512a3),(0x140ab08,0x1512d7,0x1512e4)]:
            if self.game.read(seed_pc,'B')!=0xbe or self.game.read(count_pc,'B')!=0xba:raise ValueError('Original biome noise constructor arguments changed')
            seed=self.game.read(seed_pc+1,'I');count=self.game.read(count_pc+1,'I');self.game.w(alias,'Q',SurfaceOracle.noise(self,seed,count))
            self.shared_noise_inputs.append({'alias':hex(alias),'seed_pc':hex(seed_pc),'count_pc':hex(count_pc),'seed':seed,'count':count})
        self.biome_inputs=[]
        for id in range(256):
            biome=self.game.read(0x1409fc0+8*id,'Q')
            if biome:self.biome_inputs.append({'id':id,'temperature_bits':f'{self.read(biome+0x18,"I"):08x}','virtual':hex(self.read(self.read(biome,'Q')+0x50,'Q')-self.game.base)})

    def temperature(self,id,position):
        biome=self.game.read(0x1409fc0+8*id,'Q');pc=self.read(self.read(biome,'Q')+0x50,'Q')-self.game.base;pos=self.alloc(12)
        for axis,value in enumerate(position):self.w(pos+4*axis,'i',value)
        value=self.call(pc,F,[P,P],[biome,pos])
        return {'biome':id,'position':position,'bits':struct.pack('<f',value)[::-1].hex()}
