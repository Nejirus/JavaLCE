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

import java.util.ArrayDeque;
import java.util.Arrays;




/** Capacity matching avoids consuming a flexible ingredient needed by a stricter one. */
public final class IngredientAllocator {
  private IngredientAllocator() {}

  public static int[] allocate(boolean[][] matches, int[] quantities) {
    int[] result=partial(matches,quantities);
    for(int slot:result)if(slot<0)return null;
    return result;
  }

  /** Maximum partial allocation; -1 marks each requirement still missing. */
  public static int[] partial(boolean[][] matches, int[] quantities) {
    int ingredients = matches.length, stacks = quantities.length;
    int source = ingredients + stacks, sink = source + 1, nodes = sink + 1;
    int[][] residual = new int[nodes][nodes];
    for (int i = 0; i < ingredients; i++) {
      if (matches[i].length != stacks)
        throw new IllegalArgumentException("Inconsistent match matrix");
      residual[source][i] = 1;
      for (int j = 0; j < stacks; j++) if (matches[i][j]) residual[i][ingredients + j] = 1;
    }
    for (int j = 0; j < stacks; j++) residual[ingredients + j][sink] = Math.max(0, quantities[j]);
    for (int flow = 0; flow < ingredients; flow++) {
      int[] parent = new int[nodes];
      Arrays.fill(parent, -1);
      parent[source] = source;
      ArrayDeque<Integer> queue = new ArrayDeque<>();
      queue.add(source);
      while (!queue.isEmpty() && parent[sink] == -1) {
        int from = queue.removeFirst();
        for (int to = 0; to < nodes; to++)
          if (residual[from][to] > 0 && parent[to] == -1) {
            parent[to] = from;
            queue.add(to);
          }
      }
      if (parent[sink] == -1) break;
      for (int to = sink; to != source; to = parent[to]) {
        residual[parent[to]][to]--;
        residual[to][parent[to]]++;
      }
    }
    int[] assignment = new int[ingredients];
    for (int i = 0; i < ingredients; i++) {
      assignment[i] = -1;
      for (int j = 0; j < stacks; j++)
        if (residual[ingredients + j][i] > 0) {
          assignment[i] = j;
          break;
        }
    }
    return assignment;
  }
}
