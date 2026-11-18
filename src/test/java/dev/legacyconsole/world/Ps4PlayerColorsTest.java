package dev.legacyconsole.world;
import static org.junit.Assert.*;
import com.google.gson.*;
import dev.legacyconsole.client.ClientPlayerColors;
import dev.legacyconsole.client.Ps4NameTagLayout;
import dev.legacyconsole.network.PlayerColorState;
import io.netty.buffer.Unpooled;
import java.io.*;
import java.util.*;
import net.minecraft.nbt.*;
import net.minecraft.network.FriendlyByteBuf;
import org.junit.Test;

public class Ps4PlayerColorsTest {
 private JsonObject nativeData()throws Exception{try(var r=new InputStreamReader(getClass().getResourceAsStream("/ps4/player-colors-native.json"),java.nio.charset.StandardCharsets.UTF_8)){return new JsonParser().parse(r).getAsJsonObject();}}
 @Test public void allocatorMatchesTheOriginalExecutable()throws Exception{
  for(var row:nativeData().getAsJsonArray("allocation")){
   var r=row.getAsJsonObject();List<Integer> used=new ArrayList<>();for(var n:r.getAsJsonArray("occupied"))used.add(n.getAsInt());
   assertEquals(r.toString(),r.get("result").getAsInt(),Ps4PlayerColors.firstFree(used));
  }
 }
 @Test public void paletteMatchesNativeLookupIncludingOutOfRangeSlots()throws Exception{
  for(var row:nativeData().getAsJsonArray("colors")){var r=row.getAsJsonObject();assertEquals(r.toString(),(int)r.get("argb").getAsLong(),Ps4PlayerColors.nameTagColor(r.get("index").getAsInt()));}
 }
 @Test public void rectanglesMatchTheNativeVertexStream()throws Exception{
  for(var row:nativeData().getAsJsonArray("rectangles")){
   var r=row.getAsJsonObject();var b=Ps4NameTagLayout.bounds(r.get("width").getAsInt(),r.get("baseline").getAsInt());var v=r.getAsJsonArray("border");
   assertEquals(b.left(),v.get(0).getAsJsonArray().get(0).getAsFloat(),0);assertEquals(b.top(),v.get(0).getAsJsonArray().get(1).getAsFloat(),0);
   assertEquals(b.right(),v.get(2).getAsJsonArray().get(0).getAsFloat(),0);assertEquals(b.bottom(),v.get(2).getAsJsonArray().get(1).getAsFloat(),0);
   assertEquals(v.get(0),v.get(4));
  }
 }
 @Test public void leavingReusesOnlyTheFreedSlotAndRespawnKeepsTheExistingSlot(){
  var slots=new Ps4PlayerColors.Slots();UUID a=UUID.randomUUID(),b=UUID.randomUUID(),c=UUID.randomUUID(),d=UUID.randomUUID();
  assertEquals(0,slots.join(a));assertEquals(1,slots.join(b));assertEquals(2,slots.join(c));
  assertEquals(1,slots.join(b));slots.leave(b);assertEquals(1,slots.join(d));assertEquals(2,slots.index(c));
  assertEquals(-1,slots.index(b));assertEquals(-1,new Ps4PlayerColors.Slots().index(a));
 }
 @Test public void packetRoundTripReplacesClientStateAndLogoutClearsIt(){
  UUID a=UUID.randomUUID(),b=UUID.randomUUID();CompoundTag state=new CompoundTag();ListTag list=new ListTag();
  for(int i=0;i<2;i++){CompoundTag p=new CompoundTag();p.putUUID("uuid",i==0?a:b);p.putInt("index",i);list.add(p);}state.put("players",list);
  var buf=new FriendlyByteBuf(Unpooled.buffer());try{new PlayerColorState(state).encode(buf);ClientPlayerColors.receive(new PlayerColorState(buf).state());assertEquals(0,ClientPlayerColors.index(a));assertEquals(1,ClientPlayerColors.index(b));
   ClientPlayerColors.receive(new CompoundTag());assertEquals(-1,ClientPlayerColors.index(a));ClientPlayerColors.receive(state);ClientPlayerColors.clear();assertEquals(-1,ClientPlayerColors.index(b));
  }finally{buf.release();ClientPlayerColors.clear();}
 }
}
