package dev.legacyconsole.worldgen.ps4;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;

/** The fossil path through StructureTemplate::place/BlockRotProcessor
 * (0x6c92f0, 0x6c8640, 0x6fa880), with no mirror, entities or block NBT.
 * Original asset ordering and the separate integrity draws are retained. */
public final class Ps4FossilTemplate {
 public interface Access extends Ps4OreFeature.Access {
  /** Combined original loaded-column and non-placeholder-chunk check. */
  boolean columnAvailable(int x,int y,int z);
  void notifyNeighbors(int x,int y,int z,int originalTile,int flags);
 }
 public record Block(int x,int y,int z,int state){}
 private final Ps4FossilFeature.Size size;
 private final List<Block> blocks;
 private Ps4FossilTemplate(Ps4FossilFeature.Size size,List<Block> blocks){this.size=size;this.blocks=List.copyOf(Ps4TemplateBlockOrder.order(blocks,b->Ps4TemplateBlockOrder.key(b.x,b.z)));}
 public Ps4FossilFeature.Size size(){return size;}
 public List<Block> blocks(){return blocks;}

 /** Read unchanged PS4 fossil assets; this bridge accepts their two block types. */
 public static Ps4FossilTemplate read(InputStream input)throws IOException{
  CompoundTag tag=NbtIo.readCompressed(input);var dimensions=tag.getList("size",3);var palette=tag.getList("palette",10);int[] states=new int[palette.size()];
  for(int i=0;i<states.length;i++){
   CompoundTag entry=palette.getCompound(i);String name=entry.getString("Name");
   if(name.equals("minecraft:coal_ore"))states[i]=16;
   else if(name.equals("minecraft:bone_block")){
    int meta=switch(entry.getCompound("Properties").getString("axis")){case "y"->0;case "x"->4;case "z"->8;default->throw new IOException("Unknown original fossil bone axis");};states[i]=216|(meta<<16);
   }else throw new IOException("Unknown original fossil block: "+name);
  }
  if(!tag.getList("entities",10).isEmpty())throw new IOException("Unsupported original fossil entities");
  List<Block> blocks=new ArrayList<>();for(var value:tag.getList("blocks",10)){CompoundTag entry=(CompoundTag)value;if(entry.contains("nbt"))throw new IOException("Unsupported original fossil block NBT");var p=entry.getList("pos",3);blocks.add(new Block(p.getInt(0),p.getInt(1),p.getInt(2),states[entry.getInt("state")]));}
  return new Ps4FossilTemplate(new Ps4FossilFeature.Size(dimensions.getInt(0),dimensions.getInt(1),dimensions.getInt(2)),blocks);
 }

 public void place(Access world,Random random,int x,int y,int z,int rotation,Ps4FossilFeature.Clip clip,float integrity,int flags){
  int cachedX=Integer.MAX_VALUE,cachedZ=Integer.MAX_VALUE;
  for(Block block:blocks){
   int wx=x+rotatedX(block,rotation),wy=y+block.y,wz=z+rotatedZ(block,rotation);
   boolean accepted=integrity>=1.0f||random.nextFloat()<=integrity;
   // The original checks chunk availability even when the processor rejected it.
   if(wx!=cachedX||wz!=cachedZ){if(!world.columnAvailable(wx,wy,wz))continue;cachedX=wx;cachedZ=wz;}
   if(!accepted||!inside(clip,wx,wy,wz))continue;
   int state=block.state;
   if((rotation==1||rotation==3)&&(state&65535)==216){int meta=state>>>16;if(meta==4)state=216|(8<<16);else if(meta==8)state=216|(4<<16);}
   // In the original, this second draw gates replacement processing, not writing.
   // The fossil's replacement tables are empty, but the block probe still occurs.
   if(integrity>=1.0f||random.nextFloat()<=integrity)world.block(wx,wy,wz);
   world.setBlock(wx,wy,wz,state,flags);
  }
  // Notify every clipped original entry, including processor-rejected entries.
  for(Block block:blocks){int wx=x+rotatedX(block,rotation),wy=y+block.y,wz=z+rotatedZ(block,rotation);if(inside(clip,wx,wy,wz))world.notifyNeighbors(wx,wy,wz,block.state&65535,0);}
 }
 private static int rotatedX(Block p,int rotation){return switch(rotation){case 1->-p.z;case 2->-p.x;case 3->p.z;default->p.x;};}
 private static int rotatedZ(Block p,int rotation){return switch(rotation){case 1->p.x;case 2->-p.z;case 3->-p.x;default->p.z;};}
 private static boolean inside(Ps4FossilFeature.Clip b,int x,int y,int z){return x>=b.minX()&&x<=b.maxX()&&y>=b.minY()&&y<=b.maxY()&&z>=b.minZ()&&z<=b.maxZ();}
}
