import os
"""Normalize the observed PS4 task graph, preserving authored order and strings."""
import json,struct,re
from pathlib import Path
ROOT=Path(os.environ['PS4_TUTORIAL_WORK']);english=json.loads(Path(os.environ['PS4_ENGLISH_JSON']).read_text())

def export(name):
 snapshot=json.loads((ROOT/f'capture-{name}.json').read_text());alloc=snapshot['allocations'];decoded={}
 def memory(p,n):
  for address,a in alloc.items():
   base=int(address)
   if base<=p and p+n<=base+a['size']:return bytes.fromhex(a['bytes'])[p-base:p-base+n]
  raise ValueError(('Unresolved captured memory',hex(p),n))
 def values(p,n,fmt):return list(struct.unpack('<'+fmt*n,memory(p,n*struct.calcsize('<'+fmt)))) if n else []
 def registry(p):
  a=alloc.get(str(p),{})
  if 'registry_name' not in a:raise ValueError(('Not a registry adapter',hex(p),a))
  return a['registry_name']
 def task(p):
  if p in decoded:return decoded[p]
  a=alloc[str(p)];b=bytes.fromhex(a['bytes']);kind=re.sub(r'^\d+','',a['type']);
  def v(o,fmt='I'):return struct.unpack_from('<'+fmt,b,o)[0]
  def pointers(o,count):return values(v(o,'Q'),count,'Q')
  out={'kind':kind,'allocation_return_pc':a['return_pc'],'description_hash':f'{v(8):08x}','prompt_hash':f'{v(12):08x}'}
  out['text']=english.get(out['description_hash'],'');out['prompt']=english.get(out['prompt_hash'],'')
  out['precompletion']=bool(b[0x18]);out['minimum_time']=bool(b[0x1c]);decoded[p]=out
  if kind=='ProcedureCompoundTask':out['children']=[task(q) for q in values(v(0x50,'Q'),(v(0x58,'Q')-v(0x50,'Q'))//8,'Q')]
  elif kind=='ControllerTask':
   constraints=values(v(0x28,'Q'),(v(0x30,'Q')-v(0x28,'Q'))//8,'Q')
   out['actions']=[struct.unpack_from('<i',bytes.fromhex(alloc[str(c)]['bytes']),16)[0] for c in constraints]
   out['completion_masks']=values(v(0xf0,'Q'),v(0xf8),'I')
  elif kind=='CraftTask':
   count=v(0x58);out['items']=[{'name':registry(q),'aux':aux} for q,aux in zip(pointers(0x48,count),values(v(0x50,'Q'),count,'i'))];out['required']=v(0x5c)
  elif kind in ['PickupTask','UseItemTask','CompleteUsingItemTask']:
   count=v(0x50);out['items']=[{'name':registry(q),'aux':v(0x58,'i') if kind=='PickupTask' else -1} for q in pointers(0x48,count)]
   out['required']=v(0x54) if kind=='PickupTask' else 1
  elif kind=='UseBlockTask':out['block']=registry(v(0x50,'Q'))
  elif kind=='StateChangeTask':out['state']=v(0x44)
  elif kind=='ProgressFlagTask':out['flag']=v(0x50);out['operation']=v(0x54)
  elif kind=='FullTutorialActiveTask':out['completion_action']=v(0x44)
  elif kind=='UICraftingTask':
   out['selection_type']=v(0x44);out['group']=v(0x48)
   if out['selection_type']==1:out['selected_item']=registry(v(0x50,'Q'))
  elif kind in ['ChoiceTask','HorseChoiceTask']:out['no_action']=v(0x50);out['yes_action']=v(0x4c)
  elif kind=='AreaTask':
   constraints=values(v(0x28,'Q'),(v(0x30,'Q')-v(0x28,'Q'))//8,'Q');out['constraints']=[{'type':alloc[str(q)]['type'],'bytes':alloc[str(q)]['bytes']} for q in constraints]
  elif kind!='InfoTask':raise ValueError(('Untranslated source task type',kind))
  if kind=='InfoTask':
   head=v(0x50,'Q');node=values(head,1,'Q')[0];actions=[]
   while node!=head:
    actions.append(struct.unpack_from('<i',memory(node,24),16)[0]);node=values(node,1,'Q')[0]
   out['actions']=actions
  if kind=='HorseChoiceTask':out['variants']=[english[f'{v(i):08x}'] for i in range(96,112,4)]
  return out
 selfdata=bytes.fromhex(alloc[str(snapshot['self'])]['bytes']);states={}
 if name=='full':states['0']=[task(t['object']) for t in snapshot['tasks']]
 for state in range(1,44):
  ptr,end,cap=struct.unpack_from('<3Q',selfdata,0x98+state*32)
  if ptr:
   states[str(state)]=[task(q) for q in values(ptr,(end-ptr)//8,'Q')]
 return states

full=export('full')
snapshot=json.loads((ROOT/'capture-full.json').read_text());area_hints=[]
for a in snapshot['allocations'].values():
 if a.get('type')!='8AreaHint':continue
 b=bytes.fromhex(a['bytes']);hash=struct.unpack_from('<I',b,12)[0];p=struct.unpack_from('<Q',b,48)[0]
 bounds=struct.unpack('<6d',bytes.fromhex(snapshot['allocations'][str(p)]['bytes']))
 area_hints.append(dict(text=english[f'{hash:08x}'],description_hash=f'{hash:08x}',bounds=bounds,display_state=struct.unpack_from('<i',b,60)[0],complete_state=struct.unpack_from('<i',b,64)[0]))
out={'repository':'Nejirus/mcps4','revision':'37e258085773f11599cf0ffe29119926769554b7',
 'full_constructor':'0x964350','common_constructor':'0x999780',
 'gameplay':full.pop('0'),'guided_states':full,'states':export('tasks-guided'),'area_hints':area_hints}
(ROOT/'tutorial-tasks-raw.json').write_text(json.dumps(out,indent=2)+'\n')
print('gameplay',len(out['gameplay']),'states',len(out['states']))
for t in out['gameplay']:print(t['kind'],t.get('actions',t.get('items',t.get('state',t.get('flag','')))))
