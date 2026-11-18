package dev.legacyconsole.crafting;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

/** Persists PS4 EmptyMapItem's first-map scale choice per player and world. */
public final class LegacyMapHistory extends SavedData {
 private static final String ID="legacyconsole_map_history";
 private final Set<String> players=new HashSet<>();
 public static LegacyMapHistory get(ServerLevel level){return level.getServer().overworld().getDataStorage().computeIfAbsent(LegacyMapHistory::load,LegacyMapHistory::new,ID);}
 public byte nextScale(UUID player){return (byte)(players.contains(player.toString())?0:3);}
 public void opened(UUID player){if(players.add(player.toString()))setDirty();}
 public static LegacyMapHistory load(CompoundTag tag){LegacyMapHistory data=new LegacyMapHistory();ListTag list=tag.getList("Players",8);for(int i=0;i<list.size();i++)data.players.add(list.getString(i));return data;}
 @Override public CompoundTag save(CompoundTag tag){ListTag list=new ListTag();players.stream().sorted().forEach(player->list.add(StringTag.valueOf(player)));tag.put("Players",list);return tag;}
}
