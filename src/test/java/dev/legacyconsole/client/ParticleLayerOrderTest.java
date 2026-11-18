package dev.legacyconsole.client;

import java.util.*;
import org.junit.Test;
import static org.junit.Assert.*;

public class ParticleLayerOrderTest {
 @Test public void pickupCannotDisableLightingBeforeTheNativeSheets(){
  Object terrain=new Object(),opaque=new Object(),lit=new Object(),translucent=new Object(),pickup=new Object(),extra=new Object();
  Set<Object> input=new LinkedHashSet<>(Arrays.asList(pickup,translucent,extra,terrain,lit,opaque));
  assertEquals(Arrays.asList(terrain,opaque,lit,translucent,extra,pickup),new ArrayList<>(ParticleLayerOrder.ordered(input,Arrays.asList(terrain,opaque,lit,translucent,pickup),pickup)));
 }
 @Test public void distinctCustomLayersWithEqualValuesAreRetained(){
  Object pickup=new Object();Object a=new String("custom"),b=new String("custom");
  Set<Object> input=Collections.newSetFromMap(new IdentityHashMap<>());input.add(a);input.add(b);input.add(pickup);
  List<Object> expected=new ArrayList<>(input);expected.removeIf(x->x==pickup);expected.add(pickup);
  Set<Object> sorted=ParticleLayerOrder.ordered(input,Collections.singletonList(pickup),pickup);List<Object> actual=new ArrayList<>(sorted);
  assertEquals(3,actual.size());for(int i=0;i<3;i++)assertSame(expected.get(i),actual.get(i));
  assertFalse(sorted.contains(new String("custom")));
 }
}
