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

import java.util.ArrayList;
import java.util.List;
import net.minecraftforge.common.ForgeHooks;




/**
 * Server-only transaction: validate and simulate every change before touching the live inventory.
 */
public final class NativeCrafting {
  public static final int OK = 1, MISSING = 2, FULL = 3, INVALID = 4, CURSOR = 5;

  private NativeCrafting() {}

  public static void craft(
      Player player, ConsoleContainer container, long recipeKey, int batches) {
    RecipeCatalog.Entry entry = RecipeCatalog.get(recipeKey,player.level,container);
    craft(player, container, entry, batches);
  }

  public static void craft(
      Player player, ConsoleContainer container, RecipeCatalog.Entry entry, int batches) {
    if (batches < 1
        || batches > 64
        || entry == null
        || (container.kind==ConsoleContainer.STONECUTTER)!=StonecuttingRecipes.isStonecutting(entry)
        || container.kind==ConsoleContainer.LOOM&&!(entry.recipe instanceof BannerDuplicateRecipe)
        || !entry.fits(container.table)
        || !container.canInteractWith(player)) {
      container.result(INVALID, 0);
      return;
    }
    if (!container.getCarried().isEmpty()) {
      container.result(CURSOR, 0);
      return;
    }
    int total = 0, status = MISSING;
    for (int batch = 0; batch < Math.min(64, Math.max(1, batches)); batch++) {
      List<ItemStack> simulated = copies(player.getInventory().items);
      int[] assignment = entry.allocate(simulated);
      if (assignment == null) {
        status = MISSING;
        break;
      }
      int size = container.table ? 3 : 2;
      CraftingContainer matrix = matrix(size);
      int ingredient = 0;
      for (int cell = 0; cell < entry.cells.size(); cell++) {
        if (entry.cells.get(cell).alternatives.isEmpty()) continue;
        int slot = assignment[ingredient];
        ItemStack input = simulated.get(slot).copy();
        input.setCount(1);
        int matrixSlot =
            entry.shapeless ? ingredient : (cell / entry.width) * size + cell % entry.width;
        matrix.setItem(matrixSlot, input);
        simulated.get(slot).shrink(1);
        ingredient++;
      }
      if (!entry.recipe.matches(matrix, player.level)) {
        status = INVALID;
        break;
      }
      ItemStack output = entry.recipe.assemble(matrix);
      if (output.isEmpty() || output.getCount() > output.getMaxStackSize()) {
        status = INVALID;
        break;
      }
      NonNullList<ItemStack> remainders;
      ForgeHooks.setCraftingPlayer(player);
      try {
        remainders = entry.recipe.getRemainingItems(matrix);
      } finally {
        ForgeHooks.setCraftingPlayer(null);
      }
      boolean fits = true;
      for (ItemStack remainder : remainders)
        if (!remainder.isEmpty() && !insert(simulated, remainder.copy())) {
          fits = false;
          break;
        }
      // A scaled map receives a new ID in onCrafting. It cannot merge with an
      // existing stack, even if the temporary recipe output still has that ID.
      boolean identityChange = MapCrafting.changesIdentity(output);
      if (!fits
          || (identityChange
              ? simulated.stream().noneMatch(ItemStack::isEmpty)
              : !insert(copies(simulated), output.copy()))) {
        status = FULL;
        break;
      }
      // Legacy4J RecipeMenu.tryCraft calls onCraft before inserting result.copy().
      // Finalize this exact output, so map ID/NBT mutations reach the inventory.
      MapItemSavedData source = MapCrafting.sourceMap(output, player);
      new CraftingAwards(player, matrix).award(output);
      MapCrafting.finishUpgrade(output, player, source);
      if (!insert(simulated, output.copy()))
        throw new IllegalStateException("Crafted output exceeded reserved capacity");
      for (int i = 0; i < simulated.size(); i++)
        player.getInventory().items.set(i, simulated.get(i));
      player.getInventory().setChanged();
      if (output.getItem() == Items.FILLED_MAP) MapCrafting.syncOwned(player);
      total += output.getCount();
      status = OK;
    }
    container.result(total > 0 ? OK : status, total);
  }

  public static List<ItemStack> copies(List<ItemStack> inventory) {
    List<ItemStack> result = new ArrayList<>();
    for (ItemStack stack : inventory) result.add(stack.copy());
    return result;
  }

  /** Mutates the simulated inventory only. NBT and per-item stack limits are respected. */
  public static boolean insert(List<ItemStack> inventory, ItemStack incoming) {
    for (ItemStack stack : inventory)
      if (!stack.isEmpty()
          && ItemStack.isSame(stack, incoming)
          && ItemStack.tagMatches(stack, incoming)) {
        int moved =
            Math.min(incoming.getCount(), Math.min(64, stack.getMaxStackSize()) - stack.getCount());
        if (moved > 0) {
          stack.grow(moved);
          incoming.shrink(moved);
        }
        if (incoming.isEmpty()) return true;
      }
    for (int i = 0; i < inventory.size(); i++)
      if (inventory.get(i).isEmpty()) {
        ItemStack placed = incoming.copy();
        placed.setCount(Math.min(incoming.getCount(), Math.min(64, incoming.getMaxStackSize())));
        incoming.shrink(placed.getCount());
        inventory.set(i, placed);
        if (incoming.isEmpty()) return true;
      }
    return incoming.isEmpty();
  }

  private static CraftingContainer matrix(int size) {
    return new CraftingContainer(
        new AbstractContainerMenu(null,-1) {
          @Override
          public boolean stillValid(Player player) {
            return false;
          }
        },
        size,
        size);
  }

  static void awardCustom(Player player,ItemStack output,List<RecipeCatalog.Ingredient> ingredients) {
    CraftingContainer matrix=new CraftingContainer(new AbstractContainerMenu(null,-1) {
      @Override public boolean stillValid(Player owner){return false;}
    },ingredients.size(),1);
    for(int i=0;i<ingredients.size();i++)matrix.setItem(i,ingredients.get(i).alternatives.get(0).copy());
    new CraftingAwards(player,matrix).award(output.copy());
  }

  private static final class CraftingAwards extends ResultSlot {
    CraftingAwards(Player player, CraftingContainer matrix) {
      super(player, matrix, new ResultContainer(), 0, 0, 0);
    }

    void award(ItemStack output) {
      onQuickCraft(output, output.getCount());
      checkTakeAchievements(output);
    }
  }
}
