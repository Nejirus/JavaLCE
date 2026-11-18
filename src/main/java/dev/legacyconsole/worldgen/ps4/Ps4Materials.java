package dev.legacyconsole.worldgen.ps4;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/** Original Material/MaterialColor initializer stores (0x44c270/0x44cd50).
 * Alias tokens retain native identity comparisons. Each context owns its
 * mutable Material instances; block-to-material assignment is a separate port. */
public final class Ps4Materials {
 private Ps4Materials(){}
 public static Map<Long,Ps4Material> initialize(){
  Map<Long,Ps4Material> values=new LinkedHashMap<>();
  add(values,0x1418310L,Ps4Material.Type.DECORATION,161,0,8,1);
  add(values,0x1418328L,Ps4Material.Type.LIQUID,165,4,10,1);
  add(values,0x1418330L,Ps4Material.Type.LIQUID,173,12,10,1);
  add(values,0x14183a8L,Ps4Material.Type.BASE,167,6,0,0);
  add(values,0x14187a0L,Ps4Material.Type.BASE,168,7,13,1);
  add(values,0x1418890L,Ps4Material.Type.BASE,174,13,9,0);
  add(values,0x14188d0L,Ps4Material.Type.GAS,161,0,10,0);
  add(values,0x14188d8L,Ps4Material.Type.PORTAL,161,0,8,2);
  add(values,0x14188e8L,Ps4Material.Type.BASE,166,5,12,0);
  add(values,0x14188f0L,Ps4Material.Type.BASE,171,10,8,0);
  add(values,0x1418ec0L,Ps4Material.Type.BASE,172,11,0,0);
  add(values,0x1418fe0L,Ps4Material.Type.BASE,162,1,8,0);
  add(values,0x1418fe8L,Ps4Material.Type.BASE,167,6,0,2);
  add(values,0x1418ff0L,Ps4Material.Type.LIQUID,173,12,10,1);
  add(values,0x1418ff8L,Ps4Material.Type.DECORATION,168,7,8,1);
  add(values,0x1419000L,Ps4Material.Type.DECORATION,168,7,11,1);
  add(values,0x1419008L,Ps4Material.Type.BASE,168,7,8,1);
  add(values,0x1419010L,Ps4Material.Type.BASE,164,3,8,0);
  add(values,0x1419018L,Ps4Material.Type.BASE,164,3,9,0);
  add(values,0x1419020L,Ps4Material.Type.GAS,161,0,10,1);
  add(values,0x1419028L,Ps4Material.Type.BASE,163,2,8,0);
  add(values,0x1419030L,Ps4Material.Type.DECORATION,164,3,9,0);
  add(values,0x1419038L,Ps4Material.Type.DECORATION,172,11,8,0);
  add(values,0x1419040L,Ps4Material.Type.BASE,161,0,12,0);
  add(values,0x1419048L,Ps4Material.Type.BASE,161,0,8,0);
  add(values,0x1419050L,Ps4Material.Type.BASE,165,4,13,0);
  add(values,0x1419058L,Ps4Material.Type.BASE,168,7,8,1);
  add(values,0x1419060L,Ps4Material.Type.BASE,166,5,8,0);
  add(values,0x1419068L,Ps4Material.Type.DECORATION,169,8,6,1);
  add(values,0x1419070L,Ps4Material.Type.BASE,169,8,0,0);
  add(values,0x1419078L,Ps4Material.Type.BASE,168,7,12,1);
  add(values,0x1419080L,Ps4Material.Type.BASE,170,9,8,0);
  add(values,0x1419088L,Ps4Material.Type.BASE,168,7,8,1);
  add(values,0x1419090L,Ps4Material.Type.BASE,168,7,8,1);
  add(values,0x1419098L,Ps4Material.Type.BASE,161,0,8,1);
  add(values,0x14190a0L,Ps4Material.Type.WEB,164,3,0,1);
  add(values,0x14190a8L,Ps4Material.Type.BASE,172,11,8,2);
  add(values,0x14190b0L,Ps4Material.Type.BASE,161,0,0,2);
  add(values,0x14190b8L,Ps4Material.Type.GAS,161,0,10,0);
  add(values,0x14190c0L,Ps4Material.Type.DECORATION,161,0,9,1);
  return Collections.unmodifiableMap(values);
 }
 private static void add(Map<Long,Ps4Material> values,long alias,Ps4Material.Type type,int colorCode,int colorOrdinal,int flags,int pushReaction){values.put(alias,new Ps4Material(type,new Ps4Material.Color(colorCode,colorOrdinal),flags,pushReaction));}
}
