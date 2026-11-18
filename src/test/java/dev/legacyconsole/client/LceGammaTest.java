package dev.legacyconsole.client;

import org.junit.Test;
import static org.junit.Assert.*;

public class LceGammaTest {
  @Test public void sourceUnsignedShortQuantizationAndEndpoints() {
    assertEquals(0,LceGamma.packed(0));
    assertEquals(16384,LceGamma.packed(50));
    assertEquals(32768,LceGamma.packed(100));
    assertEquals(2,LceGamma.exponent(0),0);
    assertEquals(.8f,LceGamma.exponent(50),0);
    assertEquals(.5f,LceGamma.exponent(100),0);
  }

  @Test public void everySliderValueMatchesTheInspectedRendererOperations() {
    float previous=Float.POSITIVE_INFINITY;
    for(int percent=0;percent<=100;percent++) {
      int packed=(int)(percent*327.68f);
      float expected=1f/(packed/32768f*1.5f+.5f);
      assertEquals(expected,LceGamma.exponent(percent),0);
      assertTrue(LceGamma.exponent(percent)<previous);
      previous=LceGamma.exponent(percent);
    }
  }

  @Test public void outOfRangeValuesClampToTheSourceSliderRange() {
    assertEquals(LceGamma.exponent(0),LceGamma.exponent(-100),0);
    assertEquals(LceGamma.exponent(100),LceGamma.exponent(1000),0);
  }
}
