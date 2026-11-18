"""Original fromData over every supplied canonical default state and nibble.

Complete original class definitions/properties/boxes/conversions execute. The
default field is an explicit input; post-constructor assignments remain separate.
"""
from native_scalar_block_states import ScalarBlockStatesOracle,P,I

class ScalarStateDecodersOracle(ScalarBlockStatesOracle):
    def decoder_case(self,vtable,queries):
        owner=self.alloc(4096);self.w(owner,'Q',self.game.base+vtable);ctor=self.read(self.game.base+vtable+0x478,'Q')-self.game.base;decoder=self.read(self.game.base+vtable+0x48,'Q')-self.game.base;definition=self.call(ctor,P,[P],[owner]);self.w(owner+0x188,'Q',definition);table=self.read(definition+0x28,'Q');capacity=self.read(definition+0x30,'I');states=[self.read(table+i*8,'Q') for i in range(capacity)];canonical={state:i for i,state in enumerate(states) if state};outputs=[]
        for initial,before in canonical.items():
            self.w(owner+0x190,'Q',initial);after=[];same=[]
            for data in queries:
                state=self.call(decoder,P,[P,I],[owner,data])
                if state not in canonical:raise ValueError(('Non-canonical decoded state',hex(vtable),hex(decoder),before,data))
                if self.read(owner+0x190,'Q')!=initial:raise ValueError('Original decoder changed the supplied default field')
                after.append(canonical[state]);same.append(state==initial)
            outputs.append([before,after,same])
        return {'vtable':hex(vtable),'decoder':hex(decoder),'queries':queries,'outputs':outputs,'default_field_unchanged':True}
