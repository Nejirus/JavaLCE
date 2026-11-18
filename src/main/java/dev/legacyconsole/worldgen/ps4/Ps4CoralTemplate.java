package dev.legacyconsole.worldgen.ps4;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;

/** The coral-crust path through StructureTemplate (0x6c5060/0x6c8640):
 * no mirror/entities/clip, integrity one, ignored structure blocks, empty
 * replacement tables and no terrain adjustment. Original block order remains. */
public final class Ps4CoralTemplate {
 public interface Access extends Ps4OreFeature.Access {
  /** Original loaded-column test followed by the non-placeholder-chunk test. */
  boolean columnAvailable(int x,int y,int z);
  void notifyNeighbors(int x,int y,int z,int originalTile,int flags);
  /** Original block-entity lookup and setChanged, if one exists at this position. */
  void templateBlockEntityChanged(int x,int y,int z);
 }
 public record Block(int x,int y,int z,int state,String markerMetadata){}
 private final int width,height,depth;
 private final List<Block> blocks;
 private Ps4CoralTemplate(int width,int height,int depth,List<Block> blocks){this.width=width;this.height=height;this.depth=depth;this.blocks=List.copyOf(Ps4TemplateBlockOrder.order(blocks,b->Ps4TemplateBlockOrder.key(b.x,b.z)));}
 public List<Block> blocks(){return blocks;}

 /** Reads the eleven unchanged crust assets. The supplied Mth stream must
  * be shared across cache loads in their actual load order. */
 public static Ps4CoralTemplate read(InputStream input,Random sharedMathRandom)throws IOException{
  CompoundTag tag=NbtIo.readCompressed(input);var dimensions=tag.getList("size",3);var palette=tag.getList("palette",10);int[] states=new int[palette.size()];
  for(int i=0;i<states.length;i++){
   CompoundTag entry=palette.getCompound(i);String name=entry.getString("Name"),variant=entry.getCompound("Properties").getString("variant");
   states[i]=switch(name){
    case "minecraft:coral_block"->{if(!variant.equals("dead_coral"))throw new IOException("Unknown original coral block variant: "+variant);yield Ps4CoralPalette.deadCoral(sharedMathRandom);}
    case "minecraft:stone"->1|(switch(variant){case "granite"->1;case "diorite"->3;case "andesite"->5;default->throw new IOException("Unknown original crust stone variant: "+variant);}<<16);
    case "minecraft:sand"->12;
    // Geometry ignores this state; marker collection tests only block identity
    // and NBT presence. No structure-block mode metadata conversion is claimed.
    case "minecraft:structure_block"->255;
    default->throw new IOException("Unknown original crust block: "+name);
   };
  }
  if(!tag.getList("entities",10).isEmpty())throw new IOException("Unsupported original crust entities");
  List<Block> blocks=new ArrayList<>();
  for(var value:tag.getList("blocks",10)){
   CompoundTag entry=(CompoundTag)value;var p=entry.getList("pos",3);int state=states[entry.getInt("state")];String metadata=null;
   if(entry.contains("nbt")){if((state&65535)!=255)throw new IOException("Unsupported original crust block NBT");metadata=entry.getCompound("nbt").getString("metadata");}
   blocks.add(new Block(p.getInt(0),p.getInt(1),p.getInt(2),state,metadata));
  }
  return new Ps4CoralTemplate(dimensions.getInt(0),dimensions.getInt(1),dimensions.getInt(2),blocks);
 }

 /** The native crust caller always uses integrity one. Both template RNGs
  * are constructed by the original, but neither is drawn in this path. */
 public void place(Access world,int x,int y,int z,int rotation,int flags){
  if(blocks.isEmpty()||width<1||height<1||depth<1)return;
  int cachedX=Integer.MAX_VALUE,cachedZ=Integer.MAX_VALUE;
  for(Block b:blocks){
   int wx=x+rotatedX(b,rotation),wy=y+b.y,wz=z+rotatedZ(b,rotation);
   if(wx!=cachedX||wz!=cachedZ){if(!world.columnAvailable(wx,wy,wz))continue;cachedX=wx;cachedZ=wz;}
   if((b.state&65535)==255)continue;
   // These crust palette states have identity mirror/rotation, no secondary
   // layer and no block NBT. Empty replacement processing still probes the world.
   world.block(wx,wy,wz);world.setBlock(wx,wy,wz,b.state,flags);
  }
  for(Block b:blocks){int wx=x+rotatedX(b,rotation),wy=y+b.y,wz=z+rotatedZ(b,rotation);world.notifyNeighbors(wx,wy,wz,b.state&65535,0);if(b.markerMetadata!=null)world.templateBlockEntityChanged(wx,wy,wz);}
 }

 /** 0x6c50e0, followed by the native marker-map iteration order. */
 public List<Ps4CoralCrustDecoration.Marker> markers(int x,int y,int z,int rotation){
  List<Ps4TemplateMarkers.Marker> input=new ArrayList<>();for(Block b:blocks)if((b.state&65535)==255&&b.markerMetadata!=null)input.add(new Ps4TemplateMarkers.Marker(x+rotatedX(b,rotation),y+b.y,z+rotatedZ(b,rotation),b.markerMetadata));
  List<Ps4CoralCrustDecoration.Marker> markers=new ArrayList<>();for(var marker:Ps4TemplateMarkers.order(input))markers.add(new Ps4CoralCrustDecoration.Marker(marker.x(),marker.y(),marker.z()));return markers;
 }
 private static int rotatedX(Block b,int rotation){return switch(rotation){case 1->-b.z;case 2->-b.x;case 3->b.z;default->b.x;};}
 private static int rotatedZ(Block b,int rotation){return switch(rotation){case 1->b.x;case 2->-b.z;case 3->-b.x;default->b.z;};}
}
