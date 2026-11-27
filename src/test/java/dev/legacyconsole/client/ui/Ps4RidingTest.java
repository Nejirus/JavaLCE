package dev.legacyconsole.client.ui;

import com.google.gson.*;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import org.junit.Test;
import static org.junit.Assert.*;

public class Ps4RidingTest {
  @Test public void matchesOriginalHorseNavigationInstructions()throws Exception{
    try(var reader=new InputStreamReader(getClass().getResourceAsStream("/ps4/riding-native.json"),StandardCharsets.UTF_8)){
      var rows=new Gson().fromJson(reader,JsonObject.class).getAsJsonArray("rows");int count=0;
      for(var value:rows){
        var row=value.getAsJsonArray();int[] a=new int[row.size()];for(int i=0;i<a.length;i++)a[i]=row.get(i).getAsInt();
        var r=Ps4ContainerLogic.horse(a[5],a[6],a[7],a[8],a[0]!=0,a[1]!=0,a[2]!=0,a[3]!=0,a[4]);
        assertEquals(row.toString(),a[9],r.section());
        assertEquals(row.toString(),a[10]-a[12],a[7]-r.xOffset());
        assertEquals(row.toString(),a[11]-a[13],a[8]-r.yOffset());count++;
      }
      assertEquals(3312,count);
    }
  }
}
