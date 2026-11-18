package dev.legacyconsole.worldgen.ps4;

import java.util.List;

/** BooleanProperty constructor/vector methods (0xda5a0, 0xdaba0,
 * 0xdb0b0/0c0/100). The original vector stores true before false. */
public final class Ps4BooleanProperty implements Ps4Property<Boolean> {
 private static final List<Boolean> VALUES=List.of(true,false);
 private final String name;
 public Ps4BooleanProperty(String name){this.name=name;}
 public String name(){return name;}
 public int count(){return 2;}
 public List<Boolean> possibleValues(){return VALUES;}
 public Boolean valueByIndex(int index){return VALUES.get(index);}
 /** The original typed-box search returns zero when no value matches. */
 public int indexOf(Object value){for(int i=0;i<VALUES.size();i++)if(VALUES.get(i).equals(value))return i;return 0;}
}
