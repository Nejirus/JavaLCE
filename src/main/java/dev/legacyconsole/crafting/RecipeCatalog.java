package dev.legacyconsole.crafting;
import net.minecraft.world.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.*;
import net.minecraft.world.entity.animal.*;
import net.minecraft.world.entity.npc.*;
import net.minecraft.world.entity.vehicle.*;
import net.minecraft.world.entity.decoration.*;
import net.minecraft.world.entity.monster.*;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.level.material.*;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.storage.*;
import net.minecraft.world.level.saveddata.maps.*;
import net.minecraft.world.level.biome.*;
import net.minecraft.world.level.levelgen.*;
import net.minecraft.world.level.levelgen.flat.*;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.item.enchantment.*;
import net.minecraft.world.item.alchemy.*;
import net.minecraft.world.item.trading.*;
import net.minecraft.world.effect.*;
import net.minecraft.world.phys.*;
import net.minecraft.resources.*;
import net.minecraft.core.*;
import net.minecraft.nbt.*;
import net.minecraft.network.*;
import net.minecraft.network.chat.*;
import net.minecraft.network.protocol.game.*;
import net.minecraft.sounds.*;
import net.minecraft.util.*;
import net.minecraft.server.*;
import net.minecraft.server.level.*;
import net.minecraft.ChatFormatting;
import net.minecraft.SharedConstants;
import dev.legacyconsole.port.*;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.event.TickEvent;
import dev.legacyconsole.port.ReflectionHelper;

import java.util.*;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;




public final class RecipeCatalog {
  public enum Category {
    ALL("All"),
    BLOCKS("Blocks"),
    TOOLS("Tools"),
    GEAR("Gear"),
    LOGIC("Logic"),
    FOOD("Food"),
    OTHER("Other");
    public final String label;

    Category(String label) {
      this.label = label;
    }
  }

  public static final class Ingredient {
    public final List<ItemStack> alternatives;
    private final boolean exactNbt;

    Ingredient(Object raw) {
      this(raw, false);
    }

    Ingredient(Object raw, boolean exactNbt) {
      this.exactNbt = exactNbt;
      List<ItemStack> items = new ArrayList<>();
      if (raw instanceof ItemStack && !((ItemStack) raw).isEmpty())
        items.add(((ItemStack) raw).copy());
      if (raw instanceof Iterable)
        for (Object item : (Iterable<?>) raw)
          if (item instanceof ItemStack && !((ItemStack) item).isEmpty())
            items.add(((ItemStack) item).copy());
      alternatives = Collections.unmodifiableList(items);
    }

    public boolean matches(ItemStack stack) {
      if (stack.isEmpty()) return false;
      for (ItemStack choice : alternatives)
        if (ItemStack.isSame(choice, stack)
            && (!exactNbt || ItemStack.tagMatches(choice, stack))) return true;
      return false;
    }

    String signature() {
      List<String> names = new ArrayList<>();
      for (ItemStack item : alternatives) names.add(stackSignature(item));
      Collections.sort(names);
      return names.toString();
    }
  }

  public static final class Entry {
    public final long key;
    public final CraftingRecipe recipe;
    public final ItemStack output;
    public final int width, height;
    public final boolean shapeless;
    public final List<Ingredient> cells;
    public final Category category;

    Entry(CraftingRecipe recipe, int width, int height, boolean shapeless, List<Ingredient> cells) {
      this(recipe, recipe.getResultItem(), width, height, shapeless, cells);
    }

    Entry(
        CraftingRecipe recipe,
        ItemStack output,
        int width,
        int height,
        boolean shapeless,
        List<Ingredient> cells) {
      this.recipe = recipe;
      this.output = output.copy();
      this.width = width;
      this.height = height;
      this.shapeless = shapeless;
      this.cells = Collections.unmodifiableList(cells);
      this.category = category(output);
      StringBuilder signature =
          new StringBuilder(stackSignature(output))
              .append(':')
              .append(output.getCount())
              .append(':')
              .append(width)
              .append('x')
              .append(height)
              .append(':')
              .append(shapeless);
      for (Ingredient cell : cells) signature.append('|').append(cell.signature());
      try {
        key =
            ByteBuffer.wrap(
                    MessageDigest.getInstance("SHA-256")
                        .digest(signature.toString().getBytes(StandardCharsets.UTF_8)))
                .getLong();
      } catch (Exception e) {
        throw new IllegalStateException(e);
      }
    }

    public boolean fits(boolean table) {
      return table || (shapeless ? ingredientCount() <= 4 : width <= 2 && height <= 2);
    }

    public int ingredientCount() {
      int count = 0;
      for (Ingredient cell : cells) if (!cell.alternatives.isEmpty()) count++;
      return count;
    }

    public int[] allocate(List<ItemStack> inventory) {
      List<Ingredient> ingredients = new ArrayList<>();
      for (Ingredient cell : cells) if (!cell.alternatives.isEmpty()) ingredients.add(cell);
      boolean[][] matches = new boolean[ingredients.size()][inventory.size()];
      int[] counts = new int[inventory.size()];
      for (int j = 0; j < inventory.size(); j++) {
        counts[j] = inventory.get(j).getCount();
        for (int i = 0; i < ingredients.size(); i++)
          matches[i][j] = ingredients.get(i).matches(inventory.get(j));
      }
      return IngredientAllocator.allocate(matches, counts);
    }

    public boolean[] missingCells(List<ItemStack> inventory) {
      return missingIngredients(cells,inventory);
    }
  }

  /** LCE's missing-grid bitmask marks the first matching squares red. */
  static boolean[] missingIngredients(List<Ingredient> cells,List<ItemStack> inventory){
    List<Integer> indices=new ArrayList<>();
    for(int i=cells.size()-1;i>=0;i--)if(!cells.get(i).alternatives.isEmpty())indices.add(i);
    boolean[][] matches=new boolean[indices.size()][inventory.size()];int[] counts=new int[inventory.size()];
    for(int j=0;j<inventory.size();j++){
      counts[j]=inventory.get(j).getCount();
      for(int i=0;i<indices.size();i++)matches[i][j]=cells.get(indices.get(i)).matches(inventory.get(j));
    }
    int[] assigned=IngredientAllocator.partial(matches,counts);boolean[] missing=new boolean[cells.size()];
    for(int i=0;i<assigned.length;i++)missing[indices.get(i)]=assigned[i]<0;
    return missing;
  }

  public static boolean[] missingGridItems(List<ItemStack> grid,List<ItemStack> inventory){
    List<Ingredient> cells=new ArrayList<>();for(ItemStack item:grid)cells.add(new Ingredient(item,true));
    return missingIngredients(cells,inventory);
  }

  private static final Map<RecipeManager,Map<Long,Entry>> CATALOG=new WeakHashMap<>();
  private RecipeCatalog() {}
  public static synchronized void invalidate(){CATALOG.clear();}
  public static Collection<Entry> entries(Level level){return level==null?Collections.emptyList():index(level).values();}
  private static synchronized Map<Long,Entry> index(Level level){
    RecipeManager manager=level.getRecipeManager();
    return CATALOG.computeIfAbsent(manager,m->{Map<Long,Entry> result=new LinkedHashMap<>();for(CraftingRecipe recipe:m.getAllRecipesFor(RecipeType.CRAFTING)){Entry e=read(recipe);if(e!=null&&e.ingredientCount()>0)result.put(e.key,e);}return Collections.unmodifiableMap(result);});
  }
  public static Entry get(long key,Level level){return index(level).get(key);}
  public static Entry get(long key,Level level,ConsoleContainer menu){return menu.kind==ConsoleContainer.STONECUTTER?StonecuttingRecipes.get(key,level):get(key,level);}
  private static Entry read(CraftingRecipe recipe){
    if(recipe.isSpecial()||recipe.getResultItem().isEmpty()||recipe instanceof MapExtendingRecipe)return null;
    int width,height;boolean shapeless;
    if(recipe instanceof ShapedRecipe shaped){width=shaped.getWidth();height=shaped.getHeight();shapeless=false;}
    else if(recipe instanceof ShapelessRecipe){width=Math.min(3,recipe.getIngredients().size());height=(recipe.getIngredients().size()+2)/3;shapeless=true;}
    else return null;
    if(width<1||height<1||width>3||height>3)return null;
    List<Ingredient> cells=new ArrayList<>();
    for(net.minecraft.world.item.crafting.Ingredient ingredient:recipe.getIngredients())cells.add(new Ingredient(Arrays.asList(ingredient.getItems())));
    return new Entry(recipe,width,height,shapeless,cells);
  }
  private static String stackSignature(ItemStack stack){return stack.getItem().getRegistryName()+":"+stack.getDamageValue()+":"+stack.getTag();}
  private static Category category(ItemStack stack){
    Item item=stack.getItem();String name=item.getRegistryName().getPath();
    if(item instanceof MinecartItem||item instanceof BoatItem||name.matches(".*(rail|redstone|piston|repeater|comparator|observer|hopper|dropper|dispenser|lever|button|pressure_plate|daylight_detector|tripwire|lightning_rod|target).*"))return Category.LOGIC;
    if(item.isEdible()||name.equals("cake"))return Category.FOOD;
    if(item instanceof ArmorItem||item instanceof SwordItem||item instanceof BowItem||item instanceof CrossbowItem||item instanceof ShieldItem||item instanceof ArrowItem)return Category.GEAR;
    if(item instanceof DiggerItem||item instanceof HoeItem||item instanceof ShearsItem||item instanceof FishingRodItem||item instanceof FlintAndSteelItem||item instanceof BucketItem||name.matches("compass|clock|lead|spyglass"))return Category.TOOLS;
    return item instanceof BlockItem?Category.BLOCKS:Category.OTHER;
  }
}
