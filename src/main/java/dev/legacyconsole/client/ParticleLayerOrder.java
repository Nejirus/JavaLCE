package dev.legacyconsole.client;

import java.util.*;

/** Keep the native sheets in order and draw entity-based pickup effects last. */
public final class ParticleLayerOrder {
 private ParticleLayerOrder(){}
 public static <T> Set<T> ordered(Set<T> layers,List<T> nativeOrder,T pickup){
  List<T> ordered=new ArrayList<>(layers);
  ordered.sort(Comparator.comparingInt(layer->rank(layer,nativeOrder,pickup)));
  List<T> result=Collections.unmodifiableList(ordered);
  // The engine's map uses identity keys. Preserve even custom types that have
  // equal values, and preserve their relative order within the custom group.
  return new AbstractSet<T>(){
   public Iterator<T> iterator(){return result.iterator();}
   public int size(){return result.size();}
   public boolean contains(Object value){for(T layer:result)if(layer==value)return true;return false;}
  };
 }
 private static <T> int rank(T layer,List<T> nativeOrder,T pickup){
  if(layer==pickup)return nativeOrder.size()+1;
  for(int i=0;i<nativeOrder.size();i++)if(nativeOrder.get(i)==layer)return i;
  return nativeOrder.size();
 }
}
