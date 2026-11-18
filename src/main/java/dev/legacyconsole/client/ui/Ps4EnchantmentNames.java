package dev.legacyconsole.client.ui;

import com.google.gson.Gson;
import dev.legacyconsole.worldgen.ps4.Ps4Random;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

/** 0x9e1df0's original three/four-word generator and 0x9e1f90's literal word pool. */
public final class Ps4EnchantmentNames {
  private static final class Data {String[] words;}
  private static final String[] WORDS;
  static {
    try(var reader=new InputStreamReader(Ps4EnchantmentNames.class.getResourceAsStream(
        "/assets/legacyconsole/ps4_enchantment_names.json"),StandardCharsets.UTF_8)) {
      WORDS=new Gson().fromJson(reader,Data.class).words;
    }catch(Exception e){throw new ExceptionInInitializerError(e);}
  }
  // The source constructs a shared clock-seeded Random once; offer names are not
  // reset to the world's enchanting seed on every render. The host supplies the clock.
  static final Ps4EnchantmentNames INSTANCE=new Ps4EnchantmentNames(Ps4Random.fromClockTick(System.nanoTime()));
  private final Ps4Random random;
  public Ps4EnchantmentNames(long seed){this(new Ps4Random(seed));}
  private Ps4EnchantmentNames(Ps4Random random){this.random=random;}
  public synchronized String next() {
    int extra=random.nextInt(2);
    StringBuilder text=new StringBuilder();
    for(int i=-1;i<extra+2;i++) {
      if(i!=-1)text.append(' ');
      text.append(WORDS[random.nextInt(WORDS.length)]);
    }
    return text.toString();
  }
}
