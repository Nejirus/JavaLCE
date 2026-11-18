package dev.legacyconsole.worldgen.ps4;

import static org.junit.Assert.*;
import com.google.gson.*;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Random;
import org.junit.Test;

/** Expected samples were executed from the pinned PS4 executable, independently of this port. */
public class Ps4NoiseOracleTest {
  private static JsonArray cases(String name) {
    return new JsonParser().parse(new InputStreamReader(Ps4NoiseOracleTest.class.getResourceAsStream(
        "/worldgen/" + name), StandardCharsets.UTF_8)).getAsJsonObject().getAsJsonArray("cases");
  }

  @Test public void randomSequenceMatchesThePs4Executable() {
    for (JsonElement element : cases("ps4-noise-vectors.json")) {
      JsonObject row = element.getAsJsonObject();
      Random random = new Random(row.get("seed").getAsLong());
      for (JsonElement value : row.getAsJsonArray("random_doubles"))
        assertEquals(value.getAsDouble(), random.nextDouble(), 0);
    }
  }

  @Test public void offsetsAndPermutationMatchThePs4Constructor() throws Exception {
    var field = Ps4ImprovedNoise.class.getDeclaredField("p"); field.setAccessible(true);
    for (JsonElement element : cases("ps4-noise-vectors.json")) {
      JsonObject row = element.getAsJsonObject();
      Ps4ImprovedNoise noise = new Ps4ImprovedNoise(new Random(row.get("seed").getAsLong()));
      JsonArray offsets = row.getAsJsonArray("offsets");
      assertEquals(offsets.get(0).getAsDouble(), noise.xo, 0);
      assertEquals(offsets.get(1).getAsDouble(), noise.yo, 0);
      assertEquals(offsets.get(2).getAsDouble(), noise.zo, 0);
      int[] permutation = (int[])field.get(noise);
      for (int i = 0; i < permutation.length; i++)
        assertEquals(row.getAsJsonArray("permutation").get(i).getAsInt(), permutation[i]);
    }
  }

  @Test public void pointSamplesMatchThePs4NoiseRoutine() {
    for (JsonElement element : cases("ps4-noise-vectors.json")) {
      JsonObject row = element.getAsJsonObject();
      Ps4ImprovedNoise noise = new Ps4ImprovedNoise(new Random(row.get("seed").getAsLong()));
      for (JsonElement sample : row.getAsJsonArray("values")) {
        JsonObject value = sample.getAsJsonObject(); JsonArray xyz = value.getAsJsonArray("xyz");
        assertEquals("seed=" + row.get("seed") + ", xyz=" + xyz,
            value.get("value").getAsDouble(), noise.value(xyz.get(0).getAsDouble(),
            xyz.get(1).getAsDouble(), xyz.get(2).getAsDouble()), 0);
      }
    }
  }

  @Test public void octaveRegionsMatchOriginalTwoAndThreeDimensionalRoutines() {
    for (JsonElement element : cases("ps4-perlin-vectors.json")) {
      JsonObject row = element.getAsJsonObject();
      Ps4PerlinNoise noise = new Ps4PerlinNoise(new Random(row.get("seed").getAsLong()), row.get("octaves").getAsInt());
      JsonArray p = row.getAsJsonArray("position"), n = row.getAsJsonArray("dimensions"), s = row.getAsJsonArray("scale");
      double[] values = noise.region(p.get(0).getAsInt(), p.get(1).getAsInt(), p.get(2).getAsInt(),
          n.get(0).getAsInt(), n.get(1).getAsInt(), n.get(2).getAsInt(),
          s.get(0).getAsDouble(), s.get(1).getAsDouble(), s.get(2).getAsDouble());
      JsonArray expected = row.getAsJsonArray("values"); assertEquals(expected.size(), values.length);
      for (int i = 0; i < values.length; i++)
        assertEquals("seed=" + row.get("seed") + ", octaves=" + row.get("octaves") + ", position=" + p + ", index=" + i,
            expected.get(i).getAsDouble(), values[i], 0);
    }
  }
}
