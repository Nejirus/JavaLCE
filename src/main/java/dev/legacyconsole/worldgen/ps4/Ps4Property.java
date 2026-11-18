package dev.legacyconsole.worldgen.ps4;

import java.util.List;
import java.util.Objects;

/** Typed property values used by the native canonical state definition. */
public interface Ps4Property<V> {
 Ps4StateDefinition.Properties<Ps4Property<?>,Object> DEFINITION_VALUES=new Ps4StateDefinition.Properties<>() {
  public int count(Ps4Property<?> property){return property.count();}
  public Iterable<Object> possibleValues(Ps4Property<?> property){return ()->property.possibleValues().stream().map(value->(Object)value).iterator();}
  public Object valueByIndex(Ps4Property<?> property,int index){return property.valueByIndex(index);}
  public int indexOf(Ps4Property<?> property,Object value){return property.indexOf(value);}
  public boolean same(Object first,Object second){return Objects.equals(first,second);}
 };
 String name();
 int count();
 List<V> possibleValues();
 V valueByIndex(int index);
 int indexOf(Object value);
}
