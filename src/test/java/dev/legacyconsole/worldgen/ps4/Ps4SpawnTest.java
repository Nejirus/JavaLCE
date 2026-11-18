package dev.legacyconsole.worldgen.ps4;

import com.google.gson.*;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import org.junit.Test;
import static org.junit.Assert.*;

public class Ps4SpawnTest {
 @Test public void selectionWalkClampsAndFollowingRandomMatchPs4Instructions(){
  JsonObject data=new JsonParser().parse(new InputStreamReader(getClass().getResourceAsStream("/ps4-spawn-vectors.json"),StandardCharsets.UTF_8)).getAsJsonObject();
  for(JsonElement entry:data.getAsJsonArray("cases")){
   JsonObject v=entry.getAsJsonObject();int pattern=v.get("biomePattern").getAsInt(),accept=v.get("acceptAfter").getAsInt();
   int[] ids=new int[129*129];Arrays.fill(ids,pattern);
   if(pattern==-1)for(int i=0;i<ids.length;i++)ids[i]=(i%129+3*(i/129))%7==0?1:0;
   if(pattern==-2){Arrays.fill(ids,0);ids[ids.length-1]=4;}
   Random random=new Random(v.get("seed").getAsLong());List<int[]> visited=new ArrayList<>();
   int[] pos=Ps4Spawn.select(random,v.get("width").getAsInt(),ids,(x,z)->{boolean valid=visited.size()>=accept;visited.add(new int[]{x,z});return valid;});
   String label="seed="+v.get("seed")+" width="+v.get("width")+" pattern="+pattern+" accept="+accept;
   assertEquals(label,v.get("x").getAsInt(),pos[0]);assertEquals(label,v.get("z").getAsInt(),pos[1]);
   JsonArray expected=v.getAsJsonArray("visited");assertEquals(label,expected.size(),visited.size());
   for(int i=0;i<visited.size();i++){JsonArray p=expected.get(i).getAsJsonArray();assertArrayEquals(label+" visit="+i,new int[]{p.get(0).getAsInt(),p.get(1).getAsInt()},visited.get(i));}
   assertEquals(label,v.get("nextInt").getAsInt(),random.nextInt(1000003));
  }
 }
 @Test public void searchHasThePs4FallbackAndDoesNotReplaceItWithAnInventedCenter(){
  int[] pos=Ps4Spawn.select(new Random(0),864,new int[129*129],(x,z)->true);assertArrayEquals(new int[]{8,8},pos);
 }
}
