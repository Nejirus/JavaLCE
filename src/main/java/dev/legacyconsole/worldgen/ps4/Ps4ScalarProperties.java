package dev.legacyconsole.worldgen.ps4;

import java.util.*;

/** Literal scalar initializer arguments and copied global aliases recovered from
 * mcps4. Each context retains shared property identity across copied aliases. */
public final class Ps4ScalarProperties {
 private final Map<Long,Ps4Property<?>> aliases;
 public Ps4ScalarProperties(){
  Map<Long,Ps4Property<?>> values=new LinkedHashMap<>();
  install(values,new Ps4IntegerProperty("damage",0,2),0x1408f58L); // 0x76fb4
  install(values,new Ps4IntegerProperty("leaf_size",0,2),0x1409750L); // 0x780ba
  install(values,new Ps4IntegerProperty("bamboo_thickness",0,1),0x1409758L); // 0x78118
  install(values,new Ps4IntegerProperty("rotation",0,15),0x1409798L); // 0x78176
  install(values,new Ps4BooleanProperty("isOpen"),0x1409b90L); // 0x7848d
  install(values,new Ps4BooleanProperty("occupied"),0x1409d88L); // 0x78d3b
  install(values,new Ps4IntegerProperty("age",0,3),0x1409de8L); // 0x78e14
  install(values,new Ps4IntegerProperty("attachment_type",0,3),0x1409ec0L); // 0x150e01
  install(values,new Ps4BooleanProperty("has_bottle_0"),0x140bcc0L); // 0x151d6a
  install(values,new Ps4BooleanProperty("has_bottle_1"),0x140bcc8L); // 0x151db7
  install(values,new Ps4BooleanProperty("has_bottle_2"),0x140bcd0L); // 0x151dfe
  install(values,new Ps4BooleanProperty("drag"),0x140bd40L); // 0x151f2b
  install(values,new Ps4BooleanProperty("powered"),0x140f950L); // 0x1587a6
  install(values,new Ps4IntegerProperty("age",0,15),0x140fa70L); // 0x19b77d
  install(values,new Ps4IntegerProperty("bites",0,6),0x140fa98L); // 0x19b854
  install(values,new Ps4BooleanProperty("on_fire"),0x140fb70L); // 0x19bb95
  install(values,new Ps4IntegerProperty("level",0,6),0x140fee0L); // 0x19c11d
  install(values,new Ps4IntegerProperty("liquid_type",0,1),0x140fee8L); // 0x19c17b
  install(values,new Ps4IntegerProperty("age",0,5),0x140ffb8L); // 0x19c432
  install(values,new Ps4BooleanProperty("north"),0x1410000L); // 0x19c60f
  install(values,new Ps4BooleanProperty("east"),0x1410008L); // 0x19c666
  install(values,new Ps4BooleanProperty("south"),0x1410010L); // 0x19c6bd
  install(values,new Ps4BooleanProperty("west"),0x1410018L); // 0x19c714
  install(values,new Ps4BooleanProperty("up"),0x1410020L); // 0x19c76b
  install(values,new Ps4BooleanProperty("down"),0x1410028L); // 0x19c7c2
  install(values,new Ps4IntegerProperty("age",0,2),0x1410368L); // 0x1e7b93
  install(values,new Ps4BooleanProperty("conditional"),0x1410430L); // 0x1e7e8e
  install(values,new Ps4BooleanProperty("powered"),0x14105f8L); // 0x1e8048
  install(values,new Ps4IntegerProperty("level",0,8),0x14107b8L); // 0x1e83de
  install(values,new Ps4IntegerProperty("variant",0,4),0x14133a8L); // 0x1e8817
  install(values,new Ps4IntegerProperty("variant",0,5),0x1413560L); // 0x1e8aa0
  install(values,new Ps4BooleanProperty("dead_coral"),0x1413568L); // 0x1e8af7
  install(values,new Ps4IntegerProperty("variant",0,4),0x1413588L); // 0x1e8d79
  install(values,new Ps4IntegerProperty("direction",0,1),0x1413590L); // 0x1e8dd7
  install(values,new Ps4IntegerProperty("variantHang",0,1),0x1413598L); // 0x1e8e6c
  install(values,new Ps4IntegerProperty("directionHang",0,3),0x14135a0L); // 0x1e8eca
  install(values,new Ps4BooleanProperty("dead_coral"),0x1413768L); // 0x1e8f21
  install(values,new Ps4IntegerProperty("age",0,7),0x1413960L); // 0x245f67
  install(values,new Ps4IntegerProperty("power",0,15),0x1413bd0L); // 0x24695a
  install(values,new Ps4BooleanProperty("powered"),0x1413c30L); // 0x246a22
  install(values,new Ps4BooleanProperty("snowy"),0x1413f00L); // 0x2472aa
  install(values,new Ps4BooleanProperty("triggered"),0x1413f80L); // 0x247332
  install(values,new Ps4BooleanProperty("open"),0x1414018L); // 0x2473e4
  install(values,new Ps4BooleanProperty("powered"),0x1414028L); // 0x24744a
  install(values,new Ps4BooleanProperty("eye"),0x14158b0L); // 0x2d3bcf
  install(values,new Ps4IntegerProperty("fj_portal_number",0,15),0x1415928L); // 0x2d3b12
  install(values,new Ps4IntegerProperty("moisture",0,7),0x1416230L); // 0x2d4e31
  install(values,new Ps4BooleanProperty("north"),0x1416250L); // 0x2d4eb7
  install(values,new Ps4BooleanProperty("east"),0x1416258L); // 0x2d4f0e
  install(values,new Ps4BooleanProperty("south"),0x1416260L); // 0x2d4f65
  install(values,new Ps4BooleanProperty("west"),0x1416268L); // 0x2d4fbc
  install(values,new Ps4BooleanProperty("open"),0x1416348L); // 0x2d53d0
  install(values,new Ps4BooleanProperty("powered"),0x1416350L); // 0x2d5427
  install(values,new Ps4BooleanProperty("in_wall"),0x1416358L); // 0x2d547e
  install(values,new Ps4IntegerProperty("age",0,15),0x1416430L); // 0x2d56e5
  install(values,new Ps4BooleanProperty("north"),0x1416438L); // 0x2d573c
  install(values,new Ps4BooleanProperty("east"),0x1416440L); // 0x2d5793
  install(values,new Ps4BooleanProperty("south"),0x1416448L); // 0x2d57ea
  install(values,new Ps4BooleanProperty("west"),0x1416450L); // 0x2d5841
  install(values,new Ps4BooleanProperty("upper"),0x1416458L); // 0x2d5898
  install(values,new Ps4IntegerProperty("legacy_data",0,15),0x1416838L); // 0x34df23
  install(values,new Ps4IntegerProperty("age",0,3),0x1416a10L); // 0x34ef9a
  install(values,new Ps4BooleanProperty("snowy"),0x1416be8L); // 0x34f195
  install(values,new Ps4IntegerProperty("attachment_type",0,2),0x1416cc0L); // 0x34f33d
  install(values,new Ps4BooleanProperty("enabled"),0x1416e20L); // 0x3d899e
  install(values,new Ps4BooleanProperty("has_record"),0x14181a8L); // 0x3da12b
  install(values,new Ps4IntegerProperty("age",0,15),0x14182a8L); // 0x3da296
  install(values,new Ps4BooleanProperty("hanging"),0x14183a0L); // 0x461b53
  install(values,new Ps4BooleanProperty("decayable"),0x1418790L); // 0x462096
  install(values,new Ps4BooleanProperty("check_decay"),0x1418798L); // 0x4620ed
  install(values,new Ps4BooleanProperty("powered"),0x1418878L); // 0x462273
  install(values,new Ps4BooleanProperty("has_book"),0x1418880L); // 0x4622ca
  install(values,new Ps4IntegerProperty("level",0,15),0x14188e0L); // 0x462759
  install(values,new Ps4BooleanProperty("powered"),0x1418a28L); // 0x4623ff
  install(values,new Ps4BooleanProperty("snowy"),0x1419890L); // 0x4d45dc
  install(values,new Ps4IntegerProperty("age",0,3),0x1419938L); // 0x4d4804
  install(values,new Ps4BooleanProperty("powered"),0x142da58L); // 0x583ebc
  install(values,new Ps4BooleanProperty("extended"),0x142e2d8L); // 0x587f32
  install(values,new Ps4BooleanProperty("short"),0x142e4d0L); // 0x5886da
  install(values,new Ps4BooleanProperty("powered"),0x142ecb0L); // 0x5e4087
  install(values,new Ps4BooleanProperty("powered"),0x142ecd8L); // 0x5e40de
  install(values,new Ps4IntegerProperty("power",0,15),0x142f660L); // 0x5e5990
  install(values,new Ps4IntegerProperty("age",0,15),0x142f6d0L); // 0x5e5bb9
  install(values,new Ps4BooleanProperty("locked"),0x142f758L); // 0x5e5d31
  install(values,new Ps4IntegerProperty("delay",1,4),0x142f760L); // 0x5e5d92
  install(values,new Ps4IntegerProperty("stage",0,1),0x142fa18L); // 0x5e656e
  install(values,new Ps4IntegerProperty("stability",0,7),0x142fbc0L); // 0x5e67ee
  install(values,new Ps4BooleanProperty("stabilityCheck"),0x142fbc8L); // 0x5e6845
  install(values,new Ps4IntegerProperty("type",0,2),0x142fc68L); // 0x61d3a9
  install(values,new Ps4IntegerProperty("count",0,3),0x142fc88L); // 0x61d43a
  install(values,new Ps4IntegerProperty("flange",0,1),0x142fc90L); // 0x61d498
  install(values,new Ps4IntegerProperty("rotation",0,15),0x1430158L); // 0x6ad2a1
  install(values,new Ps4BooleanProperty("nodrop"),0x1430278L); // 0x6ac393
  install(values,new Ps4IntegerProperty("layers",1,8),0x1430430L); // 0x6ac528
  install(values,new Ps4BooleanProperty("wet"),0x1432090L); // 0x6acaa1
  install(values,new Ps4IntegerProperty("age",0,7),0x1432620L); // 0x6ad58a
  install(values,new Ps4BooleanProperty("seamless"),0x1432990L); // 0x6adbd8
  install(values,new Ps4BooleanProperty("seamless"),0x1432aa8L); // 0x6adc2f
  install(values,new Ps4IntegerProperty("growth",0,3),0x14332d0L); // 0x70794e
  install(values,new Ps4BooleanProperty("north"),0x14334e8L); // 0x707f10
  install(values,new Ps4BooleanProperty("east"),0x14334f0L); // 0x707f67
  install(values,new Ps4BooleanProperty("south"),0x14334f8L); // 0x707fbe
  install(values,new Ps4BooleanProperty("west"),0x1433500L); // 0x708015
  install(values,new Ps4BooleanProperty("explode"),0x1433610L); // 0x708382
  install(values,new Ps4BooleanProperty("open"),0x14338f0L); // 0x70879a
  install(values,new Ps4BooleanProperty("powered"),0x1433990L); // 0x7bd554
  install(values,new Ps4BooleanProperty("suspended"),0x1433998L); // 0x7bd5ab
  install(values,new Ps4BooleanProperty("attached"),0x14339a0L); // 0x7bd602
  install(values,new Ps4BooleanProperty("disarmed"),0x14339a8L); // 0x7bd659
  install(values,new Ps4BooleanProperty("north"),0x14339b0L); // 0x7bd6b0
  install(values,new Ps4BooleanProperty("east"),0x14339b8L); // 0x7bd707
  install(values,new Ps4BooleanProperty("south"),0x14339c0L); // 0x7bd75e
  install(values,new Ps4BooleanProperty("west"),0x14339c8L); // 0x7bd7b5
  install(values,new Ps4BooleanProperty("powered"),0x1433a48L); // 0x7bd86e
  install(values,new Ps4BooleanProperty("attached"),0x1433a50L); // 0x7bd8c5
  install(values,new Ps4BooleanProperty("suspended"),0x1433a58L); // 0x7bd91c
  install(values,new Ps4IntegerProperty("egg_count",0,3),0x1433e18L); // 0x7bdf6d
  install(values,new Ps4IntegerProperty("hatch_level",0,2),0x1433e20L); // 0x7bdfcb
  install(values,new Ps4BooleanProperty("up"),0x1434228L,0x1434250L); // 0x7be516
  install(values,new Ps4BooleanProperty("north"),0x1434230L,0x1434258L); // 0x7be56d
  install(values,new Ps4BooleanProperty("east"),0x1434238L,0x1434270L); // 0x7be5c4
  install(values,new Ps4BooleanProperty("south"),0x1434240L,0x1434260L); // 0x7be61c
  install(values,new Ps4BooleanProperty("west"),0x1434248L,0x1434268L); // 0x7be674
  install(values,new Ps4BooleanProperty("up"),0x14342d0L); // 0x7be7de
  install(values,new Ps4BooleanProperty("north"),0x14342d8L); // 0x7be835
  install(values,new Ps4BooleanProperty("east"),0x14342e0L); // 0x7be88c
  install(values,new Ps4BooleanProperty("south"),0x14342e8L); // 0x7be8e3
  install(values,new Ps4BooleanProperty("west"),0x14342f0L); // 0x7be93a
  install(values,new Ps4IntegerProperty("power",0,15),0x1434630L); // 0x7bf90c
  install(values,new Ps4IntegerProperty("woodType",0,5),0x1434710L); // 0x7bf9e6
  install(values,new Ps4BooleanProperty("stripped"),0x1434718L); // 0x7bfa3d
  aliases=Collections.unmodifiableMap(values);
 }
 private static void install(Map<Long,Ps4Property<?>> values,Ps4Property<?> property,long... aliases){for(long alias:aliases)values.put(alias,property);}
 public Ps4Property<?> get(long alias){Ps4Property<?> property=aliases.get(alias);if(property==null)throw new IllegalArgumentException("Untranslated scalar property alias: "+Long.toHexString(alias));return property;}
 public Map<Long,Ps4Property<?>> aliases(){return aliases;}
}
