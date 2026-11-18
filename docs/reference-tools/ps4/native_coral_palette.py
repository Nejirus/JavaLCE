"""Original clock-seeded Mth Random and old dead_coral palette conversion.

The clock return value and canonical property storage are explicit inputs.
Original Random constructor, shared draws, CoralBlock parser and getData execute.
The full NBT/name/property registry loader is outside this comparison.
"""
from native_corals import CoralOracle,C,P,U,I


class CoralPaletteOracle(CoralOracle):
    def __init__(self,executable,library,**kwargs):
        super().__init__(executable,library,**kwargs)
        # The tree harness supplies seed zero to default-constructed feature
        # RNGs. This comparison specifically needs the ORIGINAL constructor.
        segment=next(s for s in self.game.ph if s[0]==1 and s[3]<=0x596c00<s[3]+s[5])
        offset=segment[2]+0x596c00-segment[3]
        C.memmove(self.game.base+0x596c00,bytes(self.game.file[offset:offset+12]),12)
        self.clock_tick=0
        self.game.bind('4J2sUJmuHZQ',P(self.callback(U,[],lambda:self.clock_tick&0xffffffffffffffff)))
        self.dead_coral_text=self.alloc(40);self.w(self.dead_coral_text+32,'Q',7)
        buffer=C.create_string_buffer('dead_coral'.encode('utf-16le'));self.keep.append(buffer)
        self.call(0x67840,None,[P,P,U],[self.dead_coral_text,C.addressof(buffer),10])

    def initialize_math_random(self,tick):
        self.clock_tick=tick
        self.call(0x596c00,None,[P],[self.game.base+0x1418180])

    def dead_coral(self):
        block=self.blocks[259]
        state=self.call(0x1c73d0,P,[P,P,P,P],[block,self.state(259<<4),self.coral_properties['block'],self.dead_coral_text])
        meta=self.call(self.read(self.read(block,'Q')+0x50,'Q')-self.game.base,I,[P,P],[block,state])
        if meta!=(self.unpacked(state)>>16):raise ValueError('Original coral metadata disagrees with canonical state storage')
        return 259|(meta<<16)

    def palette(self,tick,prefix,count):
        self.initialize_math_random(tick)
        for kind in prefix:
            if kind=='double':self.call(0x3b0930,C.c_double,[],[])
            else:self.call(0x3c0e70,I,[I],[kind])
        states=[self.dead_coral() for _ in range(count)]
        return {'clock_tick':tick,'prefix':prefix,'states':states,'random_after':C.c_int64(self.call(0x5970e0,U,[P],[self.game.base+0x1418180])).value}
