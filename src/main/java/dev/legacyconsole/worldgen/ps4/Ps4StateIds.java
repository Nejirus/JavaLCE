package dev.legacyconsole.worldgen.ps4;

/** Original state-ID table fallback and item encoding. Chunk table IDs and
 * item values use different layouts. State/property construction is supplied
 * through Access, rather than approximating canonical properties here. */
public final class Ps4StateIds {
 public interface Access<S,B> {
  S cachedState(int id);
  B blockAt(int registryIndex);
  S fromData(B block,int data);
  B blockOf(S state);
  int blockId(B block);
  int data(B block,S state);
 }
 public interface MutableAccess<S,B> extends Access<S,B> {
  Iterable<S> states(B block);
  void storeStateId(S state,int id);
  void cacheState(int id,S state);
 }
 private Ps4StateIds(){}
 /** State-ID map virtual 0x08 (0xb84f0), called by 0x91c30. The original
  * indexes its supplied table before fallback; it has no bounds/AIR fallback. */
 public static <S,B> S byId(Access<S,B> access,int id){
  S cached=access.cachedState(id);if(cached!=null)return cached;
  int block=id>>4,index=block,offset=block-0x300;
  if(id>0x16df)index=offset;
  if(Integer.compareUnsigned(block-0x101,0x2ff)>0)index=offset;
  if(id<=0x400f)index=block;
  return access.fromData(access.blockAt(index),id&15);
 }
 /** Item encoding (0x91bc0): metadata in bits 12.., registry ID below it. */
 public static <S,B> int item(Access<S,B> access,S state){B block=access.blockOf(state);int id=access.blockId(block);return (access.data(block,state)<<12)+id;}
 /** Item decoding (0x91c50), including the original extended-ID adjustment. */
 public static <S,B> S fromItem(Access<S,B> access,int value){
  int data=(value>>>12)&15,id=value&4095,index=id,offset=id-0x300;
  if(Integer.compareUnsigned(id,0x16d)>0)index=offset;
  if(Integer.compareUnsigned(id-0x101,0x2ff)>0)index=offset;
  if(Integer.compareUnsigned(id,0x400)<=0)index=id;
  return access.fromData(access.blockAt(index),data);
 }
 /** Original block-only packed base (0x91ba0). */
 public static <S,B> int blockBase(Access<S,B> access,B block){return access.blockId(block)<<4;}
 /** Registry initialization's final state enumeration (0xb7737..0xb77ea).
  * Definition order and repeated packed IDs retain the original last write. */
 public static <S,B> void index(MutableAccess<S,B> access,Iterable<B> blocks){
  for(B block:blocks)for(S state:access.states(block))if(state!=null){
   int id=access.blockId(block)<<4;id|=access.data(block,state);access.storeStateId(state,id);access.cacheState(id,state);
  }
 }
}
