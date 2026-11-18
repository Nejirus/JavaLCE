package dev.legacyconsole.network;
import dev.legacyconsole.crafting.*;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fmllegacy.network.NetworkEvent;
import java.util.*;import java.util.function.Supplier;
public final class DynamicCraftRequest {
 private final int window,batches;private final List<ItemStack> inputs;
 public DynamicCraftRequest(int window,List<ItemStack> inputs,int batches){this.window=window;this.inputs=inputs.stream().map(ItemStack::copy).toList();this.batches=batches;}
 public DynamicCraftRequest(FriendlyByteBuf b){window=b.readInt();batches=b.readUnsignedByte();int size=b.readUnsignedByte();if(size!=4&&size!=9)throw new IllegalArgumentException("Invalid custom recipe size");inputs=new ArrayList<>();for(int i=0;i<size;i++)inputs.add(b.readItem());}
 public void encode(FriendlyByteBuf b){b.writeInt(window);b.writeByte(batches);b.writeByte(inputs.size());for(ItemStack s:inputs)b.writeItem(s);}
 public static void handle(DynamicCraftRequest m,Supplier<NetworkEvent.Context> ctx){var c=ctx.get();c.enqueueWork(()->{var p=c.getSender();if(p==null||!(p.containerMenu instanceof ConsoleContainer menu)||m.batches<1||m.batches>64)return;long tick=p.level.getGameTime();if(menu.containerId!=m.window||menu.lastCraftTick==tick||!menu.stillValid(p))return;menu.lastCraftTick=tick;NativeCrafting.craft(p,menu,DynamicRecipes.entry(m.inputs,p.level,menu.table),m.batches);});c.setPacketHandled(true);}
}
