package dev.legacyconsole.crafting;

import static org.junit.Assert.*;

import org.junit.Test;

public class IngredientAllocatorTest {
  @Test
  public void preservesRareIngredientForStrictRequirement() {
    int[] result =
        IngredientAllocator.allocate(
            new boolean[][] {{true, true}, {true, false}}, new int[] {1, 1});
    assertArrayEquals(new int[] {1, 0}, result);
  }

  @Test
  public void doesNotSpendOneItemTwice() {
    assertNull(IngredientAllocator.allocate(new boolean[][] {{true}, {true}}, new int[] {1}));
  }

  @Test
  public void sharesAStackWithinItsCapacity() {
    assertArrayEquals(
        new int[] {0, 0, 0},
        IngredientAllocator.allocate(new boolean[][] {{true}, {true}, {true}}, new int[] {3}));
  }

  @Test
  public void refusesMissingAlternative() {
    assertNull(IngredientAllocator.allocate(new boolean[][] {{false, false}}, new int[] {64, 64}));
  }

  @Test
  public void handlesEmptyRecipe() {
    assertArrayEquals(new int[0], IngredientAllocator.allocate(new boolean[0][0], new int[0]));
  }
}
