package dev.legacyconsole.crafting;

import com.google.gson.Gson;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import net.minecraft.core.Registry;
import net.minecraft.nbt.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.*;

/** BannerPattern::StaticCtor at PS4 0x46fd0: original rows, groups and alternatives. */
public final class Ps4BannerPatterns {
  private Ps4BannerPatterns() {}
  public record Layer(String recipe, int color, String dye) {
    public Layer(String recipe,int color){this(recipe,color,"");}
    public Item material(){return Ps4Dyes.material(color,dye);}
  }
  public static final class Pattern {
    public String hash, source_hash, special;
    public int group, ps4_id, ps4_ordinal;
    public String[] shape;
    public boolean reusable,loom;
    public Item material() {
      if (special == null) return Items.AIR;
      ResourceLocation key = new ResourceLocation(special);
      return Registry.ITEM.containsKey(key) ? Registry.ITEM.get(key) : Items.AIR;
    }
    public boolean supported() { return special == null || material() != Items.AIR; }
    public boolean fits(boolean table) { return !loom && !reusable && supported() && (table || special != null); }
    public boolean fits(boolean table,boolean atLoom) { return atLoom?(loom||reusable)&&supported():fits(table); }
    public int dyes() {
      if (loom || special != null) return 1;
      return Arrays.stream(shape).mapToInt(s -> (int)s.chars().filter(c -> c == '#').count()).sum();
    }
    public List<ItemStack> grid(ItemStack banner, int color, boolean table) {
      return grid(banner,new ItemStack(DyeItem.byColor(DyeColor.byId(color))),table);
    }
    public List<ItemStack> grid(ItemStack banner, ItemStack dye, boolean table) {
      return grid(banner,dye,table,false);
    }
    public List<ItemStack> grid(ItemStack banner, ItemStack dye, boolean table,boolean loom) {
      int size = table ? 3 : 2;
      List<ItemStack> cells = new ArrayList<>(Collections.nCopies(size * size, ItemStack.EMPTY));
      if (!fits(table,loom)) return cells;
      if (this.loom || special != null) {
        cells.set(0, unit(banner)); cells.set(1, dye);
        if(special!=null)cells.set(size, new ItemStack(material()));
      } else {
        for (int y=0;y<3;y++) for (int x=0;x<3;x++) {
          char cell=shape[y].charAt(x);
          if (cell=='#') cells.set(y*3+x,dye.copy());
          else if (cell=='*') cells.set(y*3+x,unit(banner));
        }
      }
      return cells;
    }
  }
  private static final List<Pattern> ALL;
  static {
    try (var in = new InputStreamReader(Ps4BannerPatterns.class.getResourceAsStream(
        "/assets/legacyconsole/ps4_banner_patterns.json"),StandardCharsets.UTF_8)) {
      List<Pattern> patterns=new ArrayList<>(List.of(new Gson().fromJson(in,Pattern[].class)));
      // Java 1.17's additional template items. They are target adapters, not recovered PS4 rows.
      for(String hash:List.of("glb","pig")) {
        Pattern p=new Pattern();p.hash=hash;p.source_hash="java117:"+hash;p.group=5;
        p.special=hash.equals("glb")?"globe_banner_pattern":"piglin_banner_pattern";
        p.reusable=true;p.ps4_id=p.ps4_ordinal=-1;p.shape=new String[]{"   ","   ","   "};patterns.add(p);
      }
      ALL=List.copyOf(patterns);
    } catch(Exception error) { throw new ExceptionInInitializerError(error); }
  }
  public static List<Pattern> all() { return ALL; }
  public static Pattern get(String recipe) {
    return ALL.stream().filter(p -> p.source_hash.equals(recipe) && p.supported()).findFirst().orElse(null);
  }
  public static List<Pattern> group(int group, boolean table) {
    return ALL.stream().filter(p -> p.group==group && p.supported() && p.fits(table)).toList();
  }
  public static List<Pattern> group(int group,boolean table,boolean loom) {
    return ALL.stream().filter(p->p.group==group&&p.fits(table,loom)).toList();
  }
  public static int count(ItemStack banner) {
    return banner.hasTag() ? banner.getTag().getCompound("BlockEntityTag").getList("Patterns",10).size() : 0;
  }
  public static ItemStack unit(ItemStack item) {
    ItemStack result=item.copy(); if(!result.isEmpty())result.setCount(1); return result;
  }
  public static ItemStack apply(ItemStack banner, Pattern pattern, int color) {
    if (!(banner.getItem() instanceof BannerItem) || pattern==null || color<0 || color>15 || count(banner)>=6)
      return ItemStack.EMPTY;
    ItemStack output=unit(banner);
    CompoundTag block=output.getOrCreateTagElement("BlockEntityTag");
    ListTag list=block.getList("Patterns",10).copy();
    CompoundTag layer=new CompoundTag();layer.putString("Pattern",pattern.hash);layer.putInt("Color",color);
    list.add(layer);block.put("Patterns",list);return output;
  }
}
