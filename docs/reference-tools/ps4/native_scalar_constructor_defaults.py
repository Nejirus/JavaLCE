"""Original constructor-stage scalar defaults with native operation observers.

Observers dispatch the complete original mutation routine and the original
eight-byte position-independent default-field store. Live state bits and owner
vtables are captured before ancestor definitions can be freed/reused. A second,
unobserved invocation verifies final default values are unchanged by observation.
"""
import mmap,struct,subprocess,tempfile
from pathlib import Path
from native_scalar_block_states import ScalarBlockStatesOracle,C,P,I
from native_module import U

class Event(C.Structure):
    _fields_=[(name,U) for name in ['kind','caller','owner','owner_vtable','definition','before','before_code','property','type_info','value','after','after_code']]

class ScalarConstructorDefaultsOracle(ScalarBlockStatesOracle):
    def __init__(self,executable,library,recovered):
        super().__init__(executable,library,recovered)
        self.call(0x44cd50,None,[],[]);self.call(0x44c270,None,[],[])
        def unexpected(*unused):raise ValueError('An inherited void boundary was reached during constructor capture')
        for pc in [0xaff530,0x3796f0]:self.patch(pc,None,[U,U,U,U,U,U],unexpected)
        self.temporary=tempfile.TemporaryDirectory(prefix='ps4-scalar-defaults-');compiled=Path(self.temporary.name)/'observer.so';subprocess.run(['cc','-shared','-fPIC','-O2',str(Path(__file__).with_name('scalar_default_observer.c')),'-o',str(compiled)],check=True)
        self.observer=C.CDLL(str(compiled));self.observer.configure.argtypes=[P,P];self.observer.run.argtypes=[P,C.POINTER(U)];self.observer.run.restype=U;self.observer.get_events.restype=C.POINTER(Event)
        self.original_with=self.game.read(0x1218f90+0x158,'Q');self.original_default=self.source.read(0x7a040,12);body=self.original_default[:8]
        if body!=bytes.fromhex('4889b790010000c3'):raise ValueError('Expected position-independent original default-field store/return')
        self.trampoline=mmap.mmap(-1,4096,prot=mmap.PROT_READ|mmap.PROT_WRITE|mmap.PROT_EXEC);self.trampoline.write(body);pointer=C.addressof(C.c_char.from_buffer(self.trampoline));self.observer.configure(P(self.game.base+0xd0550),P(pointer))
    def observing(self,enabled):
        self.game.w(0x1218f90+0x158,'Q',C.cast(self.observer.observe_with,P).value if enabled else self.original_with)
        if enabled:self.game.patch(0x7a040,self.observer.observe_default)
        else:C.memmove(self.game.base+0x7a040,self.original_default,len(self.original_default))
    def arguments(self,row):
        owner=self.alloc(max(4096,row['allocation']['size']));args=[owner]
        for arg in row['allocation']['calls'][0]['args'][1:]:
            if arg is None:args.append(0)
            elif isinstance(arg,int):args.append(arg&0xffffffffffffffff)
            elif arg[0]=='global':args.append(self.game.read(arg[1],'Q'))
            elif arg[0]=='data':args.append(self.game.base+arg[1])
            elif arg[0]=='stack':args.append(self.string(''))
            else:raise ValueError(('Unsupported constructor boundary',row['id'],arg))
        return owner,args
    def construct(self,row,observe):
        self.observing(observe);owner,args=self.arguments(row);target=int(row['allocation']['calls'][0]['target'],16)
        if observe:length=self.observer.run(P(self.game.base+target),(U*6)(*args))
        else:self.call(target,None,[P,U,U,U,U,U],args);length=0
        vtable=self.read(owner,'Q')-self.game.base;definition=self.read(owner+0x188,'Q');default=self.read(owner+0x190,'Q')
        if not definition or not default:raise ValueError(('Selected constructor has no initialized definition/default',row['id']))
        initial=self.read(self.read(definition+0x28,'Q'),'Q');operations=[]
        for event in self.observer.get_events()[:length]:
            if event.owner!=owner or event.owner_vtable!=self.game.base+vtable or event.definition!=definition:continue
            before=None if not event.before else event.before_code;after=None if not event.after else event.after_code
            if event.kind==0:
                if event.type_info==self.bool_type_info:value=bool(event.value&255)
                elif event.type_info==self.type_info:value=struct.unpack('<i',struct.pack('<Q',event.value)[:4])[0]
                else:raise ValueError('Non-scalar constructor mutation')
                operations.append({'kind':'with','return_pc':hex(event.caller-self.game.base),'before':before,'alias':hex(self.aliases[event.property]),'value':value,'after':after})
            else:operations.append({'kind':'default','return_pc':hex(event.caller-self.game.base),'before':before,'after':after})
        result={'id':row['id'],'name':row['name'],'constructor':hex(target),'vtable':hex(vtable),'initial':self.read(initial+0x28,'H'),'default':self.read(default+0x28,'H'),'metadata':self.call(self.game.read(vtable+0x50,'Q')-self.game.base,I,[P,P],[owner,default]),'operations':operations}
        return result
    def constructor_case(self,row):
        baseline=self.construct(row,False);observed=self.construct(row,True)
        if any(observed[key]!=baseline[key] for key in ['vtable','initial','default','metadata']):raise ValueError('Native observers changed constructor default results')
        current=stored=observed['initial']
        for operation in observed['operations']:
            if operation['kind']=='with':
                if operation['before']!=current:raise ValueError('Nonlinear final-class default initialization requires additional context')
                current=operation['after']
            else:
                if operation['before']!=stored or operation['after']!=current:raise ValueError('Unexpected final-class default-field store')
                stored=operation['after']
        if stored!=observed['default']:raise ValueError('Final-class default assignment was not captured')
        observed['observer_matches_unobserved']=True;observed['input_arguments']=row['allocation']['calls'][0]['args'][1:];return observed
