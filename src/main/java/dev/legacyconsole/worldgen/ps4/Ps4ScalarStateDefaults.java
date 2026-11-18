package dev.legacyconsole.worldgen.ps4;

import java.util.*;

/** Final-class constructor property assignments from the original call sites.
 * Base material, shape and registration initialization are separate. */
public final class Ps4ScalarStateDefaults {
 private Ps4ScalarStateDefaults(){}
 public record Assignment(long alias,Object value,long returnPc){}
 private static final Map<Long,List<Assignment>> PROGRAMS;
 static {Map<Long,List<Assignment>> values=new LinkedHashMap<>();
  values.put(0x125fd60L,List.of(new Assignment(0x1416be8L,false,0x335d16L)));
  values.put(0x123fb80L,List.of(new Assignment(0x14188e0L,0,0x419fbcL)));
  values.put(0x12d35a0L,List.of(new Assignment(0x14188e0L,0,0x419fbcL)));
  values.put(0x12d0738L,List.of(new Assignment(0x1432090L,false,0x671c59L)));
  values.put(0x12e4f88L,List.of(new Assignment(0x1433610L,false,0x6ed929L)));
  values.put(0x1252470L,List.of(new Assignment(0x1416430L,0,0x2ae63fL),new Assignment(0x1416438L,false,0x2ae68cL),new Assignment(0x1416440L,false,0x2ae6d1L),new Assignment(0x1416448L,false,0x2ae713L),new Assignment(0x1416450L,false,0x2ae755L),new Assignment(0x1416458L,false,0x2ae797L)));
  values.put(0x1235c20L,List.of(new Assignment(0x1413960L,0,0x1eb070L)));
  values.put(0x1250a48L,List.of(new Assignment(0x1416230L,0,0x2a3b7cL)));
  values.put(0x12d3038L,List.of(new Assignment(0x1430158L,0,0x678c93L)));
  values.put(0x12aae88L,List.of(new Assignment(0x142ecd8L,false,0x58b171L)));
  values.put(0x12cb690L,List.of(new Assignment(0x1430430L,1,0x6388b6L)));
  values.put(0x1220a48L,List.of(new Assignment(0x140fa70L,0,0x1591ecL)));
  values.put(0x12b2e98L,List.of(new Assignment(0x142f6d0L,0,0x5b3c69L)));
  values.put(0x1270048L,List.of(new Assignment(0x14181a8L,false,0x3c2bd0L)));
  values.put(0x1250fe8L,List.of(new Assignment(0x1416250L,false,0x2a4bdcL),new Assignment(0x1416258L,false,0x2a4c21L),new Assignment(0x1416260L,false,0x2a4c63L),new Assignment(0x1416268L,false,0x2a4ca5L)));
  values.put(0x1220f30L,List.of(new Assignment(0x140fa98L,0,0x159cdcL)));
  values.put(0x12e09a0L,List.of(new Assignment(0x14334e8L,false,0x6df515L),new Assignment(0x14334f0L,false,0x6df558L),new Assignment(0x14334f8L,false,0x6df59aL),new Assignment(0x1433500L,false,0x6df5dcL)));
  values.put(0x12ef910L,List.of(new Assignment(0x1434228L,false,0x7723d8L),new Assignment(0x1434230L,false,0x77241dL),new Assignment(0x1434238L,false,0x77245fL),new Assignment(0x1434240L,false,0x7724a1L),new Assignment(0x1434248L,false,0x7724e3L)));
  values.put(0x128d040L,List.of(new Assignment(0x1419890L,false,0x4a24b3L)));
  values.put(0x128f8d8L,List.of(new Assignment(0x1419938L,0,0x4b9006L)));
  values.put(0x121b560L,List.of());
  values.put(0x1223938L,List.of(new Assignment(0x140fee0L,0,0x1683d3L),new Assignment(0x140fee8L,0,0x168418L)));
  values.put(0x12e80a0L,List.of(new Assignment(0x1433990L,false,0x708adfL),new Assignment(0x14339a0L,false,0x708b1dL),new Assignment(0x14339a8L,false,0x708b5bL),new Assignment(0x14339b0L,false,0x708b99L),new Assignment(0x14339b8L,false,0x708bd7L),new Assignment(0x14339c0L,false,0x708c15L),new Assignment(0x14339c8L,false,0x708c53L)));
  values.put(0x12a5d48L,List.of(new Assignment(0x1413960L,0,0x1eb181L)));
  values.put(0x12f3b78L,List.of(new Assignment(0x1434630L,0,0x7830d6L)));
  values.put(0x1236748L,List.of(new Assignment(0x1413bd0L,0,0x1f5f03L)));
  values.put(0x120cac8L,List.of(new Assignment(0x1409798L,0,0x43e60L)));
  values.put(0x1227298L,List.of(new Assignment(0x1410000L,false,0x17eb3fL),new Assignment(0x1410008L,false,0x17eb7dL),new Assignment(0x1410010L,false,0x17ebbbL),new Assignment(0x1410018L,false,0x17ebf9L),new Assignment(0x1410020L,false,0x17ec37L),new Assignment(0x1410028L,false,0x17ec75L)));
  values.put(0x1226bb0L,List.of(new Assignment(0x140ffb8L,0,0x17ce76L)));
  values.put(0x1211918L,List.of(new Assignment(0x1409de8L,0,0x1eb181L)));
  values.put(0x124adf8L,List.of());
  values.put(0x12597c0L,List.of(new Assignment(0x1416a10L,0,0x31ca57L)));
  values.put(0x12706e0L,List.of(new Assignment(0x14182a8L,0,0x3c5419L)));
  values.put(0x1230420L,List.of(new Assignment(0x1413560L,0,0x1c6d08L),new Assignment(0x1413568L,false,0x1c6d55L)));
  values.put(0x122ff30L,List.of());
  values.put(0x1230908L,List.of(new Assignment(0x1413588L,3,0x1c9c3eL),new Assignment(0x1413590L,1,0x1c9c83L)));
  values.put(0x1230fd0L,List.of(new Assignment(0x1413598L,0,0x1cb289L),new Assignment(0x14135a0L,2,0x1cb2ceL),new Assignment(0x1413768L,false,0x1cb31bL)));
  values.put(0x12b9c58L,List.of(new Assignment(0x142fc68L,0,0x5f52b5L)));
  values.put(0x12ba6a8L,List.of());
  values.put(0x121c250L,List.of(new Assignment(0x140bd40L,true,0xe9c81L)));
  values.put(0x12eaae8L,List.of(new Assignment(0x1433e18L,0,0x710731L),new Assignment(0x1433e20L,0,0x710776L)));
  values.put(0x12f8ac8L,List.of(new Assignment(0x1434710L,0,0x794628L),new Assignment(0x1434718L,false,0x794675L)));
  values.put(0x120b858L,List.of(new Assignment(0x1409750L,0,0x3e968L),new Assignment(0x1409758L,0,0x3e9adL)));
  values.put(0x12b70f0L,List.of(new Assignment(0x142fbc0L,0,0x5c9e6cL),new Assignment(0x142fbc8L,false,0x5c9eb9L)));
  values.put(0x1273cf0L,List.of(new Assignment(0x14183a0L,false,0x3dd399L)));
  values.put(0x122e600L,List.of(new Assignment(0x14107b8L,0,0x1b318cL)));
  values.put(0x12de098L,List.of(new Assignment(0x14332d0L,0,0x6cb20bL)));
  PROGRAMS=Collections.unmodifiableMap(values);
 }
 public static List<Assignment> assignments(Ps4ScalarStateSchemas.Schema schema){List<Assignment> program=PROGRAMS.get(schema.vtable());if(program!=null)return program;if(schema.properties().isEmpty())return List.of();throw new IllegalArgumentException("Untranslated scalar constructor defaults: "+Long.toHexString(schema.vtable()));}
 public static <B> Ps4StateDefinition.State<B,Ps4Property<?>,Object> initialize(Ps4ScalarStateSchemas.Schema schema,Ps4StateDefinition<B,Ps4Property<?>,Object> definition,Ps4ScalarProperties context){var state=definition.defaultState();for(Assignment assignment:assignments(schema))state=state.with(context.get(assignment.alias()),assignment.value());return state;}
}
