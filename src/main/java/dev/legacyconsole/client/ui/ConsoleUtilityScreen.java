package dev.legacyconsole.client.ui;
import net.minecraft.client.*;
import net.minecraft.client.gui.screens.*;
import net.minecraft.client.gui.screens.inventory.*;
import net.minecraft.client.gui.components.*;
import net.minecraft.client.player.*;
import net.minecraft.client.multiplayer.*;
import net.minecraft.client.server.*;
import net.minecraft.client.resources.language.*;
import net.minecraft.client.sounds.*;
import net.minecraft.client.resources.sounds.*;
import net.minecraft.client.renderer.texture.*;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.client.renderer.*;
import net.minecraft.client.model.*;
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
import com.mojang.blaze3d.vertex.*;
import net.minecraft.ChatFormatting;
import net.minecraft.SharedConstants;
import net.minecraft.client.server.IntegratedServer;
import dev.legacyconsole.port.*;
import dev.legacyconsole.port.Gui;
import dev.legacyconsole.port.GlStateManager;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.event.TickEvent;
import dev.legacyconsole.port.ReflectionHelper;

import dev.legacyconsole.client.input.*;
import io.netty.buffer.Unpooled;
import java.io.IOException;
import java.util.*;



/** LCE anvil and enchanting book behavior with Legacy4J enchantment controls. */
public final class ConsoleUtilityScreen extends ConsoleContainerScreen implements ControllerScreen {
  private final int kind; // 0 anvil, 1 enchanting
  private String rename = "", lastInput = "";
  private final EnchantmentScreen enchanting;
  private final FormattedText[] enchantNames = new FormattedText[3];
  private BookModel BOOK;

  public ConsoleUtilityScreen(AbstractContainerScreen<?> original) {
    super(original.getMenu());
    kind = original instanceof AnvilScreen ? 0 : 1;
    enchanting = kind == 1 ? (EnchantmentScreen) original : null;
  }

  @Override
  public void initGui() {
    xSize = kind == 0 ? 208 : 215;
    ySize = Math.min(kind == 1 ? 217 : 215, height - 20);
    super.initGui();
    if(kind==1){enchanting.init(mc,width,height);BOOK=new BookModel(mc.getEntityModels().bakeLayer(net.minecraft.client.model.geom.ModelLayers.BOOK));}
    guiTop = Math.max(2, (height - ySize) / 2 - 5);
    mc.player.containerMenu = inventorySlots;
    int first = kind == 0 ? 3 : 2;
    int inventoryY = kind == 0 ? 117 : 115;
    int hotbarY = kind == 0 ? 186 : 185;
    for (int i = first; i < inventorySlots.slots.size(); i++) {
      Slot s = inventorySlots.getSlot(i);
      int n = s.getContainerSlot();
      s.x = (kind == 0 ? 11 : 14) + n % 9 * 21;
      s.y = n < 9 ? hotbarY : inventoryY + (n - 9) / 9 * 21;
    }
    if (kind == 0) {
      place(0, 21, 62);
      place(1, 90, 62);
      place(2, 173, 62);
      buttonList.add(new ConsoleButton(10, guiLeft + 73, guiTop + 26, 122, 20, rename));
    } else if (kind == 1) {
      place(0, 19, 66);
      place(1, 50, 66);
      for (int i = 0; i < 3; i++)
        buttonList.add(new EnchantButton(i, guiLeft + 81, guiTop + 24 + i * 21));
    }
  }

  private void place(int i, int x, int y) {
    Slot s = inventorySlots.getSlot(i);
    s.x = x;
    s.y = y;
  }

  @Override
  public void updateScreen() {
    super.updateScreen();
    if (enchanting != null) enchanting.tickBook();
    if (kind == 0) {
      ItemStack s = inventorySlots.getSlot(0).getItem();
      String signature = s.serializeNBT().toString();
      if (!signature.equals(lastInput)) {
        lastInput = signature;
        rename = s.isEmpty() ? "" : s.getHoverName().getString();
      }
      if (!buttonList.isEmpty()) buttonList.get(0).displayString = s.isEmpty() ? "Name" : rename;
      if (!buttonList.isEmpty()) buttonList.get(0).enabled = !s.isEmpty();
    }
  }

  @Override
  protected void actionPerformed(GuiButton b) {
    if (kind == 0
        && b.id == 10
        && inventorySlots.getSlot(0).hasItem()
        && mc.player.containerMenu.getCarried().isEmpty())
      mc.setScreen(
          new ConsoleKeyboard(
              this,
              "Repair & Name",
              rename,
              value -> {
                rename = value.substring(0, Math.min(31, value.length()));
                ItemStack input = inventorySlots.getSlot(0).getItem();
                String name =
                    !input.hasCustomHoverName() && rename.equals(input.getHoverName().getString()) ? "" : rename;
                ((AnvilMenu) inventorySlots).setItemName(name);
                mc.player.connection.send(new ServerboundRenameItemPacket(name));
              }));
    if (kind == 1 && b.id >= 0 && b.id < 3) {
      EnchantmentMenu c = (EnchantmentMenu) inventorySlots;
      if (c.clickMenuButton(mc.player,b.id)) mc.gameMode.handleInventoryButtonClick(c.containerId,b.id);
    }
  }

  @Override
  protected void mouseClicked(int x, int y, int button) throws IOException {
    for (GuiButton b : buttonList)
      if (button == 0 && b.mousePressed(mc, x, y)) {
        b.playPressSound(mc.getSoundManager());
        actionPerformed(b);
        return;
      }
    super.mouseClicked(x, y, button);
  }

  private void box(int x, int y, int size) {
    ConsoleTheme.slot(guiLeft + x, guiTop + y, size, false);
  }

  @Override
  protected int[] inventoryHighlightBounds(Slot slot) {
    if (slot.container == mc.player.getInventory() || kind == 1)
      return new int[] {slot.x - (kind == 1 ? 1 : 2), slot.y - (kind == 1 ? 1 : 2), 21, 21};
    return null;
  }

  @Override
  protected float[] inventoryHighlightFace(Slot slot) {
    if (kind == 1) return new float[] {slot.x, slot.y, 18.9f, 18.9f};
    return super.inventoryHighlightFace(slot);
  }

  @Override
  protected float[] itemDisplayBounds(Slot slot) {
    return kind == 1 ? inventoryHighlightFace(slot) : null;
  }

  @Override
  public int[] controllerSlotBounds(Slot slot) {
    int[] bounds = inventoryHighlightBounds(slot);
    return bounds == null ? ControllerScreen.super.controllerSlotBounds(slot) : bounds;
  }

  @Override
  protected void drawGuiContainerBackgroundLayer(float partial, int mx, int my) {
    ConsoleTheme.lcePanel(guiLeft, guiTop, xSize, ySize);
    int first = kind == 0 ? 3 : 2;
    for (int i = first; i < inventorySlots.slots.size(); i++) {
      Slot s = inventorySlots.getSlot(i);
      ConsoleTheme.spriteAt(
          "gui/sprites/container/icon_holder",
          guiLeft + s.x - (kind == 1 ? 1.05f : 2),
          guiTop + s.y - (kind == 1 ? 1.05f : 2),
          21,
          21);
    }
    int labelY = kind == 0 ? 102 : 104;
    ConsoleTheme.text(
        "Inventory", guiLeft + (kind == 0 ? 9 : 14), guiTop + labelY, ConsoleTheme.DARK);
    if (kind == 0) {
      ConsoleTheme.text("Repair & Name", guiLeft + 73, guiTop + 9, ConsoleTheme.DARK);
      ConsoleTheme.sprite("lce/anvil_hammer", guiLeft + 14, guiTop + 10, 20, 20);
      box(14, 55, 30);
      box(83, 55, 30);
      box(166, 55, 30);
      ConsoleTheme.sprite("lce/anvil_plus", guiLeft + 53, guiTop + 60, 10, 10);
      boolean invalid =
          (inventorySlots.getSlot(0).hasItem() || inventorySlots.getSlot(1).hasItem())
              && !inventorySlots.getSlot(2).hasItem();
      ConsoleTheme.sprite(
          invalid ? "lce/anvil_cross" : "lce/arrow_off",
          guiLeft + 121,
          guiTop + 58,
          invalid ? 24 : 36,
          24);
      AnvilMenu c = (AnvilMenu) inventorySlots;
      if (c.getCost() > 0) {
        String cost =
            c.getCost() >= 40 && !mc.player.getAbilities().instabuild
                ? "Too Expensive!"
                : "Enchantment Cost: " + c.getCost();
        ConsoleTheme.centered(
            cost,
            guiLeft + xSize / 2,
            guiTop + 88,
            inventorySlots.getSlot(2).mayPickup(mc.player) ? 0xff80c020 : 0xffff5555);
      }
    } else if (kind == 1) {
      ConsoleTheme.text(
          I18n.get("container.enchant"), guiLeft + 14, guiTop + 10, ConsoleTheme.DARK);
      for (int i = 0; i < 2; i++) {
        Slot slot = inventorySlots.getSlot(i);
        ConsoleTheme.spriteAt(
            "gui/sprites/container/icon_holder",
            guiLeft + slot.x - 1.05f,
            guiTop + slot.y - 1.05f,
            21,
            21);
      }
      if (!inventorySlots.getSlot(0).hasItem())
        ConsoleTheme.spriteAt(
            "gui/sprites/container/enchanting_slot", guiLeft + 20.45f, guiTop + 67.45f, 16, 16);
      ConsoleTheme.sprite(
          "gui/sprites/tiles/square_recessed_panel", guiLeft + 79, guiTop + 22, 123, 66);
      EnchantmentNames words =
          EnchantmentNames.getInstance();
      EnchantmentMenu c = (EnchantmentMenu) inventorySlots;
      words.initSeed(c.getEnchantmentSeed());
      for(int i=0;i<3;i++)enchantNames[i]=c.costs[i]==0?FormattedText.EMPTY:words.getRandomName(mc.font,86-FontCompat.get().getStringWidth(String.valueOf(c.costs[i])));
      book(guiLeft + 13, guiTop + 24, 61, 39, partial);
    }
    drawInventoryHighlight(mx, my);
  }

  private void book(int x, int y, int w, int h, float partial) {
    // XUI_Ctrl_EnchantmentBook::OnRender: orthographic, control-height * 1.5.
    // The scene's 122x78 control at 26/48 becomes 61x39 at 13/24 GUI units.
    ScaledResolution resolution = new ScaledResolution(mc);
    int factor = resolution.getScaleFactor();
    org.lwjgl.opengl.GL11.glEnable(org.lwjgl.opengl.GL11.GL_SCISSOR_TEST);
    org.lwjgl.opengl.GL11.glScissor(
        (x - 2) * factor,
        (resolution.getScaledHeight() - y - h - 2) * factor,
        (w + 4) * factor,
        (h + 4) * factor);
    GlStateManager.clear(org.lwjgl.opengl.GL11.GL_DEPTH_BUFFER_BIT);
    org.lwjgl.opengl.GL11.glDisable(org.lwjgl.opengl.GL11.GL_SCISSOR_TEST);
    GlStateManager.pushMatrix();
    GlStateManager.translate(x + w / 2f, y + h / 2f, 50);
    float size = h * 1.5f;
    GlStateManager.scale(-size, size, size);
    GlStateManager.enableDepth();
    GlStateManager.enableRescaleNormal();
    GlStateManager.enableColorMaterial();
    GlStateManager.color(1, 1, 1, 1);
    GlStateManager.rotate(135, 0, 1, 0);
    RenderHelper.enableStandardItemLighting();
    GlStateManager.rotate(-135, 0, 1, 0);
    PortGraphics.bind(new ResourceLocation("textures/entity/enchanting_table_book.png"));
    GlStateManager.rotate(20, 1, 0, 0);
    float open = enchanting.oOpen + (enchanting.open - enchanting.oOpen) * partial;
    GlStateManager.translate((1 - open) * .2f, (1 - open) * .1f, (1 - open) * .25f);
    GlStateManager.rotate(-(1 - open) * 90 - 90, 0, 1, 0);
    GlStateManager.rotate(180, 1, 0, 0);
    float flip = enchanting.oFlip + (enchanting.flip - enchanting.oFlip) * partial;
    GlStateManager.enableCull();
    BOOK.setupAnim(0,pageFlip(flip+.25f),pageFlip(flip+.75f),open);
    var buffers=mc.renderBuffers().bufferSource();var vertices=buffers.getBuffer(RenderType.entitySolid(new ResourceLocation("textures/entity/enchanting_table_book.png")));
    BOOK.renderToBuffer(new PoseStack(),vertices,0xF000F0,net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY,1,1,1,1);buffers.endBatch();
    GlStateManager.disableCull();
    RenderHelper.disableStandardItemLighting();
    GlStateManager.disableRescaleNormal();
    GlStateManager.disableDepth();
    GlStateManager.popMatrix();
    GlStateManager.color(1, 1, 1, 1);
  }

  private float pageFlip(float flip) {
    return Mth.clamp((flip - (float) Math.floor(flip)) * 1.6f - .3f, 0, 1);
  }

  @Override
  protected void drawGuiContainerForegroundLayer(int x, int y) {}

  @Override
  public void drawScreen(int x, int y, float partial) {
    drawDefaultBackground();
    super.drawScreen(x, y, partial);
    ConsoleTheme.footer(
        width,
        height,
        "cross",
        "Take / Select",
        "square",
        "Split",
        "triangle",
        "Quick Move",
        "circle",
        "Back");
    if (kind == 1)
      for (GuiButton b : buttonList)
        if (x >= b.xPosition
            && x < b.xPosition + b.width
            && y >= b.yPosition
            && y < b.yPosition + b.height) {
          EnchantmentMenu c = (EnchantmentMenu) inventorySlots;
          Enchantment enchant = net.minecraft.core.Registry.ENCHANTMENT.byId(c.enchantClue[b.id]);
          if (enchant != null && c.costs[b.id] > 0 && c.levelClue[b.id] >= 0) {
            List<String> lines = new ArrayList<>();
            lines.add(
                ChatFormatting.WHITE
                    + I18n.get(
                        "container.enchant.clue", enchant.getFullname(c.levelClue[b.id]).getString()));
            if (!mc.player.getAbilities().instabuild) {
              lines.add("");
              if (mc.player.experienceLevel < c.costs[b.id])
                lines.add(
                    ChatFormatting.RED
                        + I18n.get(
                            "container.enchant.level.requirement", c.costs[b.id]));
              else {
                int amount = b.id + 1;
                lines.add(
                    (c.getGoldCount() >= amount ? ChatFormatting.GRAY : ChatFormatting.RED)
                        + I18n.get(
                            amount == 1
                                ? "container.enchant.lapis.one"
                                : "container.enchant.lapis.many",
                            amount));
                lines.add(
                    ChatFormatting.GRAY
                        + I18n.get(
                            amount == 1
                                ? "container.enchant.level.one"
                                : "container.enchant.level.many",
                            amount));
              }
            }
            drawHoveringText(lines, x, y);
          }
        }
  }

  @Override
  public boolean controllerPointer() {
    return true;
  }

  @Override
  public boolean controllerDirection(int d) {
    return false;
  }

  @Override
  public boolean controllerButton(int b) {
    if (kind == 0 && b == PadState.SQUARE) {
      actionPerformed(buttonList.get(0));
      return true;
    }
    return false;
  }

  private void runes(FormattedText text,int x,int y,int width,int color){if(text==null)return;for(var line:mc.font.split(text,width)){mc.font.draw(new PoseStack(),line,x,y,color);y+=9;}}
  private final class EnchantButton extends GuiButton {
    EnchantButton(int id, int x, int y) {
      super(id, x, y, 120, 21, "");
    }

    @Override
    public void drawButton(net.minecraft.client.Minecraft minecraft, int mx, int my) {
      EnchantmentMenu c = (EnchantmentMenu) inventorySlots;
      int cost = c.costs[id];
      enabled =
          cost > 0
              && c.enchantClue[id] >= 0
              && (mc.player.getAbilities().instabuild
                  || mc.player.experienceLevel >= cost && c.getGoldCount() >= id + 1);
      hovered = contains(mx, my);
      // Legacy4J's 80.5/23.5 origin and the original numbered offer symbols.
      GlStateManager.pushMatrix();
      GlStateManager.translate(-.5f, -.5f, 0);
      ConsoleTheme.spriteAt(
          "gui/sprites/container/enchantment_button_"
              + (!enabled ? "empty" : hovered ? "selected" : "active"),
          xPosition,
          yPosition,
          width,
          height);
      ConsoleTheme.sprite(
          "gui/sprites/container/enchanting_level_" + (id + 1) + (!enabled ? "_disabled" : ""),
          xPosition - 1,
          yPosition - 1,
          24,
          24);
      if (cost > 0) {
        int runesColor = enabled ? hovered ? 0xffff80 : 6839882 : (6839882 & 0xfefefe) >> 1;
        runes(
            enchantNames[id],
            xPosition + 24,
            yPosition + 3,
            86 - FontCompat.get().getStringWidth(String.valueOf(cost)),
            runesColor);
        FontCompat.get().drawStringWithShadow(
            String.valueOf(cost),
            xPosition + width - FontCompat.get().getStringWidth(String.valueOf(cost)),
            yPosition + 12,
            enabled ? 8453920 : 4226832);
      }
      GlStateManager.popMatrix();
    }

    @Override
    public boolean mousePressed(Minecraft minecraft, int mx, int my) {
      return enabled && visible && contains(mx, my);
    }

    private boolean contains(int mx, int my) {
      return mx >= xPosition - .5f
          && mx < xPosition + width - .5f
          && my >= yPosition - .5f
          && my < yPosition + height - .5f;
    }
  }
}
