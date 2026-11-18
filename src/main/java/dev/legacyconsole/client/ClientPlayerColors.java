package dev.legacyconsole.client;
import java.util.*;
import net.minecraft.nbt.CompoundTag;
public final class ClientPlayerColors {
 private static Map<UUID,Integer> players=Map.of();
 private ClientPlayerColors(){}
 public static void clear(){players=Map.of();}
 public static int index(UUID id){return players.getOrDefault(id,-1);}
 public static void receive(CompoundTag state){
  Map<UUID,Integer> next=new HashMap<>();
  if(state!=null)for(var value:state.getList("players",10)){
   CompoundTag p=(CompoundTag)value;int index=p.getInt("index");
   if(p.hasUUID("uuid")&&index>=0&&index<dev.legacyconsole.world.Ps4PlayerColors.SLOTS)next.put(p.getUUID("uuid"),index);
  }
  players=Map.copyOf(next);
 }
}
