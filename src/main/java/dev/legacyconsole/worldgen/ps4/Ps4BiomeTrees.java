package dev.legacyconsole.worldgen.ps4;

import java.util.Random;

/** Original biome tree selectors and feature constructor configurations.
 * State constants follow the original block defaults and cached initializers;
 * notably giant spruce retains CHECK_DECAY, unlike the other spruce features. */
public final class Ps4BiomeTrees {
 public enum Biome {DEFAULT,HILLS,FOREST,MUTATED_FOREST,JUNGLE,BAMBOO_JUNGLE,SAVANNA,PLAINS,SWAMP,TAIGA,ICE,MESA}
 public enum Kind {BASIC,BIG_OAK,BIRCH,PINE,SPRUCE,GROUND_BUSH,SWAMP,SAVANNA,ROOF,MEGA_PINE,MEGA_JUNGLE}
 public record Selection(Kind kind,int baseHeight,int extraRange,int log,int leaves,boolean jungle,boolean tall,boolean updates){
  /** Bind the original feature lifecycle to the population voxel bridge. */
  public Ps4BiomeDecorator.Tree bind(Ps4TreeTrunk.Access blocks){
   Ps4BigTreeFeature big=kind==Kind.BIG_OAK?new Ps4BigTreeFeature(updates,log,leaves):null;
   return new Ps4BiomeDecorator.Tree(){
    public void prepare(){if(big!=null)big.prepare();}
    public boolean place(Ps4BiomeDecorator.Access world,Random random,int x,int y,int z){
     return switch(kind){
      case BASIC->Ps4TreeFeature.place(blocks,random,x,y,z,baseHeight,log,leaves,jungle,updates);
      case BIG_OAK->big.place(blocks,random,x,y,z);
      case BIRCH->Ps4BirchFeature.place(blocks,random,x,y,z,baseHeight,log,leaves,tall,updates);
      case PINE->Ps4PineFeature.place(blocks,random,x,y,z,baseHeight,log,leaves,updates);
      case SPRUCE->Ps4SpruceFeature.place(blocks,random,x,y,z,baseHeight,log,leaves,updates);
      case GROUND_BUSH->Ps4GroundBushFeature.place(blocks,random,x,y,z,log,leaves,updates);
      case SWAMP->Ps4SwampTreeFeature.place(blocks,random,x,y,z,baseHeight,log,leaves,updates);
      case SAVANNA->Ps4SavannaTreeFeature.place(blocks,random,x,y,z,baseHeight,log,leaves,updates);
      case ROOF->Ps4RoofTreeFeature.place(blocks,random,x,y,z,baseHeight,log,leaves,updates);
      case MEGA_PINE->Ps4MegaPineFeature.place(blocks,random,x,y,z,baseHeight,extraRange,log,leaves,tall,updates);
      case MEGA_JUNGLE->Ps4MegaJungleFeature.place(blocks,random,x,y,z,baseHeight,extraRange,log,leaves,updates);
     };
    }
    public void postPlace(Ps4BiomeDecorator.Access world,Random random,int x,int y,int z){if(kind==Kind.MEGA_PINE)Ps4MegaPineFeature.postPlace(blocks,random,x,y,z,updates);}
   };
  }
 }
 private Ps4BiomeTrees(){}
 public static Selection select(Biome biome,int variant,Random random){
  return switch(biome){
   case DEFAULT->ordinary(random);
   case ICE->spruce();
   case MESA->basic();
   case HILLS->random.nextInt(3)>0?spruce():ordinary(random);
   case FOREST->{
    if(variant==3&&random.nextInt(3)>0)yield simple(Kind.ROOF,6,162|(1<<16),161|(1<<16));
    if(variant==2||random.nextInt(5)==0)yield birch(false);
    yield ordinary(random);
   }
   case MUTATED_FOREST->birch(random.nextBoolean());
   case JUNGLE->{
    if(random.nextInt(10)==0)yield big();
    if(random.nextInt(2)==0)yield bush();
    if((variant&255)==0&&random.nextInt(3)==0)yield megaJungle();
    yield jungle(random);
   }
   case BAMBOO_JUNGLE->{
    if(random.nextInt(20)==0)yield big();
    if(random.nextInt(20)<3)yield bush();
    if(random.nextInt(10)<7)yield megaJungle();
    yield jungle(random);
   }
   case SAVANNA->random.nextInt(5)==0?basic():simple(Kind.SAVANNA,5,162,161);
   case PLAINS->random.nextInt(3)==0?big():basic();
   case SWAMP->simple(Kind.SWAMP,5,17,18);
   case TAIGA->{
    if((variant==1||variant==2)&&random.nextInt(3)==0){boolean tall=variant==2||random.nextInt(13)==0;yield new Selection(Kind.MEGA_PINE,13,15,17|(1<<16),18|(9<<16),false,tall,false);}
    yield random.nextInt(3)==0?simple(Kind.PINE,7,17|(1<<16),18|(1<<16)):spruce();
   }
  };
 }
 private static Selection ordinary(Random random){return random.nextInt(10)==0?big():basic();}
 private static Selection simple(Kind kind,int height,int log,int leaves){return new Selection(kind,height,0,log,leaves,false,false,false);}
 private static Selection basic(){return simple(Kind.BASIC,4,17,18);}
 private static Selection big(){return simple(Kind.BIG_OAK,5,17,18|(8<<16));}
 private static Selection spruce(){return simple(Kind.SPRUCE,6,17|(1<<16),18|(1<<16));}
 private static Selection birch(boolean tall){return new Selection(Kind.BIRCH,5,0,17|(2<<16),18|(2<<16),false,tall,false);}
 private static Selection bush(){return simple(Kind.GROUND_BUSH,4,17|(3<<16),18);}
 private static Selection jungle(Random random){return new Selection(Kind.BASIC,4+random.nextInt(7),0,17|(3<<16),18|(3<<16),true,false,false);}
 private static Selection megaJungle(){return new Selection(Kind.MEGA_JUNGLE,10,20,17|(3<<16),18|(3<<16),false,false,false);}
}
