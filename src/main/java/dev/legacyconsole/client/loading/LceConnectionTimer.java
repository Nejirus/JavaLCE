package dev.legacyconsole.client.loading;

import com.google.gson.Gson;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/** skin.swf::fourj.FJ_Timer's authored 80-frame display list, at 30 fps. */
public final class LceConnectionTimer {
  private static final Data DATA=read();
  private static final class Data {
    int fps, squareSize;
    float x,y;
    float[][] positions;
    int[][] opacity;
  }
  public record Square(LceLoadingLayout.Bounds bounds,int opacity) {}
  private LceConnectionTimer() {}
  public static int frame(long elapsedMillis) {
    long time=Math.max(0,elapsedMillis);
    return (int)(((time/1000%DATA.opacity.length)*DATA.fps+time%1000*DATA.fps/1000)%DATA.opacity.length);
  }
  public static List<Square> squares(int width,int height,long elapsedMillis) {
    int[] opacity=DATA.opacity[frame(elapsedMillis)];
    List<Square> result=new ArrayList<>();
    for(int i=0;i<DATA.positions.length;i++)if(opacity[i]>0) {
      float[] position=DATA.positions[i];
      result.add(new Square(LceLoadingLayout.project(width,height,DATA.x+position[0],
          DATA.y+position[1],DATA.squareSize,DATA.squareSize),opacity[i]));
    }
    return result;
  }
  private static Data read() {
    try(var in=new InputStreamReader(LceConnectionTimer.class.getResourceAsStream(
        "/assets/legacyconsole/connecting_timer.json"),StandardCharsets.UTF_8)) {
      return new Gson().fromJson(in,Data.class);
    } catch(Exception e) { throw new ExceptionInInitializerError(e); }
  }
}
