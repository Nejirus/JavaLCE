package dev.legacyconsole.crafting;
import dev.legacyconsole.LegacyConsole;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.*;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
public final class ConsoleContainer extends AbstractContainerMenu {
 public static final int PERSONAL=0,TABLE=1,STONECUTTER=2,LOOM=3;
 public final int kind;
 public final boolean table;public final BlockPos origin;private final Player owner;
 public int status,crafted,revision;public long lastCraftTick=Long.MIN_VALUE;
 private final ContainerData data=new SimpleContainerData(3);
 public ConsoleContainer(int id,Inventory inv,boolean table,BlockPos origin){
  this(id,inv,table?TABLE:PERSONAL,origin);
 }
 public ConsoleContainer(int id,Inventory inv,int kind,BlockPos origin){
  super(LegacyConsole.CRAFTING.get(),id);if(kind<0||kind>LOOM)throw new IllegalArgumentException("Unknown crafting context");
  this.kind=kind;this.table=kind!=PERSONAL;this.origin=origin.immutable();owner=inv.player;
  for(int row=0;row<3;row++)for(int col=0;col<9;col++)addSlot(new Slot(inv,col+row*9+9,71+col*18,143+row*18));
  for(int col=0;col<9;col++)addSlot(new Slot(inv,col,71+col*18,201));
  addDataSlots(data);
 }
 public static ConsoleContainer client(int id,Inventory inv,FriendlyByteBuf buf){return new ConsoleContainer(id,inv,buf.readUnsignedByte(),buf.readBlockPos());}
 @Override public boolean stillValid(Player p){var block=switch(kind){case TABLE->Blocks.CRAFTING_TABLE;case STONECUTTER->Blocks.STONECUTTER;case LOOM->Blocks.LOOM;default->null;};return p==owner&&p.isAlive()&&!p.isSpectator()&&(block==null||p.level.getBlockState(origin).is(block)&&p.distanceToSqr(origin.getX()+.5,origin.getY()+.5,origin.getZ()+.5)<=64);}
 public boolean canInteractWith(Player p){return stillValid(p);}
 @Override public ItemStack quickMoveStack(Player p,int index){if(index<0||index>=slots.size())return ItemStack.EMPTY;Slot slot=slots.get(index);if(!slot.hasItem())return ItemStack.EMPTY;ItemStack stack=slot.getItem(),copy=stack.copy();if(!moveItemStackTo(stack,index<27?27:0,index<27?36:27,false))return ItemStack.EMPTY;if(stack.isEmpty())slot.set(ItemStack.EMPTY);else slot.setChanged();slot.onTake(p,stack);return copy;}
 public void result(int result,int count){status=result;crafted=count;revision=(revision+1)&32767;data.set(0,status);data.set(1,crafted);data.set(2,revision);broadcastChanges();}
 @Override public void setData(int id,int value){super.setData(id,value);status=data.get(0);crafted=data.get(1);revision=data.get(2);}
}
