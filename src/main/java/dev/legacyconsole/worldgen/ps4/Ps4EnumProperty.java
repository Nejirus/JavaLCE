package dev.legacyconsole.worldgen.ps4;

import java.util.List;

/** EnumProperty's ordered-set constructor/vector methods, 0x4d0ea0,
 * 0x4d1630/640/670. Factory set construction is separate. */
public final class Ps4EnumProperty implements Ps4Property<Ps4EnumProperty.Value> {
 /** Native typed boxes compare enum pointers, including for equal IDs/names. */
 public static final class Value {
  private final int id;private final String name;
  public Value(int id,String name){this.id=id;this.name=name;}
  public int id(){return id;}
  public String name(){return name;}
 }
 private final String name;private final List<Value> values;
 /** The input follows the original set's iteration order. */
 public Ps4EnumProperty(String name,List<Value> orderedValues){this.name=name;this.values=List.copyOf(orderedValues);}
 public String name(){return name;}
 public int count(){return values.size();}
 public List<Value> possibleValues(){return values;}
 public Value valueByIndex(int index){return values.get(index);}
 public int indexOf(Object value){for(int i=0;i<values.size();i++)if(values.get(i)==value)return i;return 0;}
}
