package dev.legacyconsole.worldgen.ps4;

import java.util.*;

/** IntegerProperty's inclusive constructor/vector methods (0x379b40,
 * 0x379e50, 0x37a260/270/2a0). Native type-address/name hashing is separate. */
public final class Ps4IntegerProperty implements Ps4Property<Integer> {
 public static final Ps4StateDefinition.Properties<Ps4IntegerProperty,Integer> DEFINITION_VALUES=new Ps4StateDefinition.Properties<>() {
  public int count(Ps4IntegerProperty property){return property.count();}
  public Iterable<Integer> possibleValues(Ps4IntegerProperty property){return property.possibleValues();}
  public Integer valueByIndex(Ps4IntegerProperty property,int index){return property.valueByIndex(index);}
  public int indexOf(Ps4IntegerProperty property,Integer value){return property.indexOf(value);}
  public boolean same(Integer first,Integer second){return first.intValue()==second.intValue();}
 };
 private final String name;private final List<Integer> values;
 public Ps4IntegerProperty(String name,int minimum,int maximum){
  this.name=name;List<Integer> values=new ArrayList<>();
  if(minimum<=maximum){int value=minimum;while(true){values.add(value);if(value==maximum)break;value++;}}
  this.values=Collections.unmodifiableList(values);
 }
 public String name(){return name;}
 public int count(){return values.size();}
 public List<Integer> possibleValues(){return values;}
 public Integer valueByIndex(int index){return values.get(index);}
 /** The original search returns index zero when no boxed value matches. */
 public int indexOf(Object value){for(int i=0;i<values.size();i++)if(values.get(i).equals(value))return i;return 0;}
}
