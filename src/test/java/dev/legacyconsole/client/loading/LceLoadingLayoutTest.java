package dev.legacyconsole.client.loading;

import static org.junit.Assert.*;
import org.junit.Test;
import javax.imageio.ImageIO;
import java.security.MessageDigest;


public class LceLoadingLayoutTest {
  private static String hex(byte[] bytes) { StringBuilder s=new StringBuilder();for(byte b:bytes)s.append(String.format("%02x",b&255));return s.toString(); }
  @Test public void fullscreenProgress720MatchesAuthoredGeometry() {
    assertEquals(new LceLoadingLayout.Bounds(320,390,640,20),LceLoadingLayout.bar(1280,720));
    assertEquals(new LceLoadingLayout.Bounds(322,392,636,16),LceLoadingLayout.fill(1280,720,1));
    assertEquals(new LceLoadingLayout.Bounds(240,510,800,110),LceLoadingLayout.tips(1280,720));
  }
  @Test public void barScalesUniformlyAtSupportedResolutionsAndCentersOnWideDisplays() {
    for(int height:new int[]{720,1080,1440,2160}) {
      float scale=height/720f;int width=height*16/9;
      var bar=LceLoadingLayout.bar(width,height);
      assertEquals(width/2f,bar.x()+bar.width()/2,0.001f);
      assertEquals(20*scale,bar.height(),0.001f);
      assertEquals(2*scale,LceLoadingLayout.fill(width,height,1).x()-bar.x(),0.001f);
    }
    assertEquals(1280f,LceLoadingLayout.bar(2560,1080).x()+LceLoadingLayout.bar(2560,1080).width()/2,0.001f);
  }
  @Test public void progressRevealsFillerAtFixedTextureScaleAndClampsNativeValues() {
    assertEquals(318,LceLoadingLayout.fill(1280,720,.5f).width(),0);
    assertEquals(322,LceLoadingLayout.fill(1280,720,.5f).x(),0);
    assertEquals(0,LceLoadingLayout.progress(-1),0);assertEquals(1,LceLoadingLayout.progress(2),0);
    assertEquals(0,LceLoadingLayout.progress(Float.NaN),0);assertEquals(0,LceLoadingLayout.progress(Float.POSITIVE_INFINITY),0);
  }
  @Test public void progressAndTipImagesAreUnmodifiedLceBitmaps() throws Exception {
    String[] names={"progress_background","progress_fill","tip_panel"};
    int[][] sizes={{640,20},{636,16},{24,24}};
    String[] hashes={"4b702f14ea9352be448367ee407771b0c9771e8cac220cfe4e5b911ccbdd8a70","6567fcaf2e2566083239e1a601f043162d27f005f4d186d7a39269276024ec69","c61433850d50353411ed12e30dd6f550fb6da108b082fd8bbeb3c8508c3d8c73"};
    for(int i=0;i<names.length;i++) {
      String path="/assets/legacyconsole/textures/startup/"+names[i]+".png";
      try(var in=getClass().getResourceAsStream(path)) {
        assertEquals(hashes[i],hex(MessageDigest.getInstance("SHA-256").digest(in.readAllBytes())));
      }
      var image=ImageIO.read(getClass().getResourceAsStream(path));assertEquals(sizes[i][0],image.getWidth());assertEquals(sizes[i][1],image.getHeight());
    }
  }
}
