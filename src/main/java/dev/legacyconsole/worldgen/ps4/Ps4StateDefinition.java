package dev.legacyconsole.worldgen.ps4;

import java.util.*;

/** Canonical bit-field state tables from 0xcf850 and property operations from
 * 0xd04f0/0xd0550. Property identity, count, enumeration and value ordinals are
 * supplied by the translated property types, separately from this table core. */
public final class Ps4StateDefinition<B,P,V> {
 public interface Properties<P,V> {
  /** Original property contract: positive count and nonempty enumeration. */
  int count(P property);
  Iterable<V> possibleValues(P property);
  V valueByIndex(P property,int index);
  int indexOf(P property,V value);
  boolean same(V first,V second);
 }
 public static final class State<B,P,V> {
  private final Ps4StateDefinition<B,P,V> definition;private final int bits;
  private State(Ps4StateDefinition<B,P,V> definition,int bits){this.definition=definition;this.bits=bits;}
  public B owner(){return definition.owner;}
  public int bits(){return bits;}
  public V value(P property){return definition.value(this,property);}
  public State<B,P,V> with(P property,V value){return definition.with(this,property,value);}
 }
 private static final int[] DEBRUIJN={0,1,28,2,29,14,24,3,30,22,20,15,25,17,4,8,31,27,13,23,21,19,16,7,26,12,18,6,11,5,10,9};
 private final B owner;private final List<P> properties;private final Properties<P,V> values;private final int[] shifts,masks;private final State<B,P,V>[] states;
 @SuppressWarnings("unchecked") public Ps4StateDefinition(B owner,List<P> properties,Properties<P,V> values){
  this.owner=owner;this.properties=List.copyOf(properties);this.values=values;shifts=new int[properties.size()];masks=new int[properties.size()];int shift=0,capacity=1;
  for(int i=0;i<properties.size();i++){
   int rounded=roundPower(values.count(properties.get(i)));shifts[i]=shift&255;masks[i]=(rounded-1)&65535;shift=(shift+powerLog(rounded))&255;capacity*=rounded;
  }
  states=(State<B,P,V>[])new State[capacity];List<List<V>> possible=new ArrayList<>();for(P property:properties){List<V> list=new ArrayList<>();for(V value:values.possibleValues(property))list.add(value);possible.add(list);}enumerate(possible,new ArrayList<>(),0);
 }
 private static int roundPower(int value){value--;value|=value>>1;value|=value>>2;value|=value>>4;value|=value>>8;value|=value>>16;return value+1;}
 private static int powerLog(int value){int lowest=value&-value;return DEBRUIJN[(lowest*0x77cb531)>>>27];}
 private void enumerate(List<List<V>> possible,List<V> tuple,int depth){
  if(depth==properties.size()){
   int bits=0;for(int i=0;i<properties.size();i++)bits|=values.indexOf(properties.get(i),tuple.get(i))<<shifts[i];bits&=65535;states[bits]=new State<>(this,bits);return;
  }
  for(V value:possible.get(depth)){tuple.add(value);enumerate(possible,tuple,depth+1);tuple.remove(tuple.size()-1);}
 }
 public int capacity(){return states.length;}
 public int shift(int propertyIndex){return shifts[propertyIndex];}
 public int mask(int propertyIndex){return masks[propertyIndex];}
 public State<B,P,V> state(int index){return states[index];}
 public State<B,P,V> defaultState(){return states[0];}
 public List<State<B,P,V>> states(){return Collections.unmodifiableList(Arrays.asList(states));}
 private int find(P property){for(int i=0;i<properties.size();i++)if(properties.get(i)==property)return i;return -1;}
 private V value(State<B,P,V> state,P property){int index=find(property);if(index<0)return null;return values.valueByIndex(property,(state.bits>>>shifts[index])&masks[index]);}
 private State<B,P,V> with(State<B,P,V> state,P property,V value){
  // The source's first search assumes the property is present. Retain that
  // contract; there is no invented unchanged-state/default fallback.
  int index=0;while(properties.get(index)!=property)index++;
  V current=values.valueByIndex(property,(state.bits>>>shifts[index])&masks[index]);if(values.same(current,value))return state;
  int found=0;while(found<properties.size()&&properties.get(found)!=property)found++;int ordinal=found<properties.size()?values.indexOf(property,value)&65535:0;
  int mask=masks[found],shift=shifts[found],bits=(state.bits&~(mask<<shift))|((ordinal&mask)<<shift);return states[bits];
 }
}
