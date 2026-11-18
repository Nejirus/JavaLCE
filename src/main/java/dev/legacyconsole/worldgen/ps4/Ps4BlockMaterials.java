package dev.legacyconsole.worldgen.ps4;

import java.util.Map;

/** Material assignments from the original registered block constructor chains.
 * The two base stores are 0x9209d and 0x92231; BlockState reads them at
 * 0xd06a0. Each context preserves shared material identity across blocks.
 * Property/default/shape initialization is a separate translation. */
public final class Ps4BlockMaterials {
 private final Map<Long,Ps4Material> materials=Ps4Materials.initialize();
 public Ps4Material forRegisteredBlock(int blockId){return byAlias(alias(blockId));}
 public Ps4Material byAlias(long alias){
  Ps4Material material=materials.get(alias);
  if(material==null)throw new IllegalArgumentException("Unknown PS4 material alias: "+Long.toHexString(alias));
  return material;
 }
 public static long alias(int blockId){return switch(blockId){
  case 0,217,255,365 -> 0x14188d0L;
  case 1,4,7,14,15,16,21,23,24,43,44,45,48,49,52,56,61,62,67,70,
       73,74,77,87,98,108,109,112,113,114,116,120,121,128,129,130,139,153,155,156,
       158,159,168,172,173,179,180,181,182,201,202,203,204,205,206,213,215,216,218,219,
       220,221,222,223,224,225,226,227,228,229,230,231,232,233,234,235,236,237,238,239,
       240,241,242,243,244,245,246,247,248,249,250,251,259,260,264,265,266,267,268,291,
       292,293,311,312,322,323,324,325,326,327,328,329,330,331,332,333,334,335,336,352,
       353,355,356,359,360 -> 0x1418ec0L;
  case 2,110,170,214 -> 0x1418fe0L;
  case 3,60,208 -> 0x14188f0L;
  case 5,17,25,47,53,54,58,63,64,68,72,84,85,96,99,100,107,125,126,134,
       135,136,143,146,151,162,163,164,176,177,178,183,184,185,186,187,188,189,190,191,
       192,193,194,195,196,197,274,275,276,277,278,295,296,297,298,299,300,301,302,303,
       304,305,306,307,308,309,310,313,337,338,339,340,341,342,343,344,345,346,348,350,
       351,357,358,362,364 -> 0x1418890L;
  case 6,37,38,39,40,59,83,104,105,111,115,127,141,142,199,200,207,258,319,321,363 -> 0x1418ff8L;
  case 8,9 -> 0x1418330L;
  case 10,11 -> 0x1418328L;
  case 12,13,88,252 -> 0x1419028L;
  case 18,161 -> 0x14187a0L;
  case 19 -> 0x1419010L;
  case 20,89,95,102,138,160,169 -> 0x1419040L;
  case 22,41,42,57,71,101,117,118,133,137,147,148,152,154,167,210,211,347,349,354 -> 0x14183a8L;
  case 26,35 -> 0x1419018L;
  case 27,28,50,55,65,66,69,75,76,93,94,131,132,140,144,149,150,157,198,361 -> 0x1418310L;
  case 29,33,34,36 -> 0x14190a8L;
  case 30 -> 0x14190a0L;
  case 31,32,106,175,263,270,271 -> 0x1419000L;
  case 46 -> 0x1419050L;
  case 51 -> 0x1419020L;
  case 78 -> 0x1419068L;
  case 79,212 -> 0x14188e8L;
  case 80 -> 0x1419070L;
  case 81 -> 0x1419078L;
  case 82,97,165 -> 0x1419080L;
  case 86,91,103,257 -> 0x1419088L;
  case 90,119,209 -> 0x14188d8L;
  case 92 -> 0x1419098L;
  case 122,279 -> 0x1419090L;
  case 123,124 -> 0x1419048L;
  case 145 -> 0x1418fe8L;
  case 166 -> 0x14190b0L;
  case 171 -> 0x1419030L;
  case 174,273 -> 0x1419060L;
  case 256 -> 0x1419038L;
  case 269 -> 0x1419008L;
  case 272 -> 0x1418ff0L;
  case 320 -> 0x14190c0L;
  default -> throw new IllegalArgumentException("Unregistered PS4 block ID: "+blockId);
 };}
}
