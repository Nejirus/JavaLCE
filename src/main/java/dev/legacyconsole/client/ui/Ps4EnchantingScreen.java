package dev.legacyconsole.client.ui;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.legacyconsole.port.GlStateManager;
import dev.legacyconsole.port.GuiButton;
import dev.legacyconsole.port.PortGraphics;
import dev.legacyconsole.port.RenderHelper;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.EnchantmentScreen;
import net.minecraft.client.model.BookModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.inventory.EnchantmentMenu;

/** Original EnchantingMenu1080 scene and numbered PS4 offer widgets. */
public final class Ps4EnchantingScreen extends Ps4ContainerScreen {
  private final EnchantmentMenu enchant;
  private final EnchantmentScreen animation;
  private BookModel book;
  private final FormattedText[] runes=new FormattedText[3];
  private final int[] lastCosts=new int[3];
  private static final ResourceLocation BOOK=new ResourceLocation("textures/entity/enchanting_table_book.png");

  public Ps4EnchantingScreen(AbstractContainerScreen<?> original) {
    super(original,"EnchantingMenu");
    enchant=(EnchantmentMenu)inventorySlots;animation=(EnchantmentScreen)original;
  }
  @Override protected void layoutControls() {
    place(0,"ingredient");place(1,"lapis");
    animation.init(mc,width,height);
    book=new BookModel(mc.getEntityModels().bakeLayer(ModelLayers.BOOK));
    for(int i=0;i<3;i++)buttonList.add(new Offer(i,art.control("Button"+(i+1),360,63)));
    navigationSlot(0x23,0);navigationSlot(0x24,1);
    for(int i=0;i<3;i++)navigationButton(0x25+i,art.control("Button"+(i+1),360,63));
  }
  @Override public void updateScreen(){animation.tickBook();}
  private boolean available(int i) {
    return enchant.costs[i]>0&&Registry.ENCHANTMENT.byId(enchant.enchantClue[i])!=null&&(mc.player.getAbilities().instabuild||
        mc.player.experienceLevel>=enchant.costs[i]&&enchant.getGoldCount()>=i+1);
  }
  @Override protected void actionPerformed(GuiButton button) {
    // 0x00a3c100: call the menu first, then send the accepted button index to the server.
    if(enchant.clickMenuButton(mc.player,button.id))mc.gameMode.handleInventoryButtonClick(enchant.containerId,button.id);
  }
  @Override protected void drawGuiContainerBackgroundLayer(float partial,int mx,int my) {
    art.backgrounds();
    art.label(I18n.get("container.enchant"),"enchantLabel");
    art.label(I18n.get("container.inventory"),"inventoryLabel");
    drawHolders(mx,my);
    if(!inventorySlots.getSlot(1).hasItem())art.emptyIcon("LapisIcon");
    if(!inventorySlots.getSlot(0).hasItem()) {
      float[] m=art.matrix("ingredient");
      art.image("Enchant_Slot",art.rect(m[4]+3,m[5]+3,57,57));
    }
    for(int i=0;i<3;i++) {
      if(lastCosts[i]!=enchant.costs[i]) {
        lastCosts[i]=enchant.costs[i];
        runes[i]=FormattedText.of(Ps4EnchantmentNames.INSTANCE.next(),
            net.minecraft.network.chat.Style.EMPTY.withFont(new ResourceLocation("minecraft","alt")));
      }
      if(runes[i]==null)runes[i]=FormattedText.EMPTY;
    }
    drawBook(partial);
  }
  private void drawBook(float partial) {
    // EnchantingMenu1080.fui local:2 / shape:0 is 65x59, rather than a 50x50 slot placeholder.
    var r=art.control("iggy_EnchantmentBook",65,59);
    // UIControl_EnchantmentBook::render, 0x009e0b10, constants 0x00ee28e4..0x00ee2924.
    // The original renderer's combined X/Y scale is 57, verified at four viewport ratios.
    // Java supplies the 1.17 book mesh and synchronized animation state.
    GlStateManager.pushMatrix();
    GlStateManager.translate(r.x()+r.w()*.5f,r.y()+r.h()*.5f,50);
    GlStateManager.scale(-57*art.scale,57*art.scale,360*art.scale);
    GlStateManager.enableDepth();GlStateManager.enableRescaleNormal();GlStateManager.enableColorMaterial();
    GlStateManager.color(1,1,1,1);GlStateManager.rotate(135,0,1,0);
    RenderHelper.enableStandardItemLighting();GlStateManager.rotate(-135,0,1,0);
    GlStateManager.rotate(20,1,0,0);
    float open=Mth.lerp(partial,animation.oOpen,animation.open),closed=1-open;
    GlStateManager.translate(closed*.2f,closed*.1f,closed*.25f);
    GlStateManager.rotate(-90*closed-90,0,1,0);GlStateManager.rotate(180,1,0,0);
    float flip=Mth.lerp(partial,animation.oFlip,animation.flip);
    book.setupAnim(0,page(flip+.25f),page(flip+.75f),open);
    GlStateManager.enableCull();
    var buffers=mc.renderBuffers().bufferSource();
    book.renderToBuffer(new PoseStack(),buffers.getBuffer(RenderType.entitySolid(BOOK)),
        0xF000F0,OverlayTexture.NO_OVERLAY,1,1,1,1);buffers.endBatch();
    GlStateManager.disableCull();RenderHelper.disableStandardItemLighting();
    GlStateManager.disableRescaleNormal();GlStateManager.disableDepth();GlStateManager.popMatrix();
  }
  private static float page(float f){return Mth.clamp((f-(float)Math.floor(f))*1.6f-.3f,0,1);}
  @Override public void drawScreen(int mx,int my,float partial) {
    super.drawScreen(mx,my,partial);
    for(GuiButton button:buttonList)if(((Offer)button).bounds.contains(mx,my)) {
      int i=button.id;var clue=Registry.ENCHANTMENT.byId(enchant.enchantClue[i]);
      if(clue==null||enchant.costs[i]<1||enchant.levelClue[i]<0)continue;
      // 0x00a3c2b0: clue, then XP requirement or lapis/level costs; creative omits costs.
      List<String> lines=new ArrayList<>();
      lines.add(ChatFormatting.WHITE+I18n.get("container.enchant.clue",clue.getFullname(enchant.levelClue[i]).getString()));
      if(!mc.player.getAbilities().instabuild) {
        lines.add("");
        if(mc.player.experienceLevel<enchant.costs[i])lines.add(ChatFormatting.RED+
            I18n.get("container.enchant.level.requirement",enchant.costs[i]));
        else {
          int amount=i+1;
          lines.add((enchant.getGoldCount()>=amount?ChatFormatting.GRAY:ChatFormatting.RED)+
              I18n.get(amount==1?"container.enchant.lapis.one":"container.enchant.lapis.many",amount));
          lines.add(ChatFormatting.GRAY+I18n.get(amount==1?"container.enchant.level.one":
              "container.enchant.level.many",amount));
        }
      }
      drawHoveringText(lines,mx,my);
    }
  }
  private final class Offer extends GuiButton {
    final Ps4UiArt.Rect bounds;
    Offer(int id,Ps4UiArt.Rect r){super(id,(int)r.x(),(int)r.y(),(int)Math.ceil(r.w()),(int)Math.ceil(r.h()),"");bounds=r;}
    @Override public void drawButton(net.minecraft.client.Minecraft game,int mx,int my) {
      enabled=available(id);hovered=bounds.contains(mx,my);
      art.image(!enabled?"EnchantmentButtonEmpty":hovered?"EnchantmentButtonSelected":"EnchantmentButtonActive",bounds);
      art.image("Level"+(id+1)+(enabled?"_On":"_Off"),new Ps4UiArt.Rect(bounds.x()+5*art.scale,
          bounds.y()+9.5f*art.scale,52*art.scale,44*art.scale));
      if(enchant.costs[id]>0) {
        // Original local:57 rune placeholder: (59,8), 297x48 after its FUI transforms.
        // Minecraft's bitmap font is the host font adapter; retain the authored viewport.
        double framebufferScale=mc.getWindow().getGuiScale();
        int left=(int)Math.floor((bounds.x()+59*art.scale)*framebufferScale);
        int right=(int)Math.ceil((bounds.x()+356*art.scale)*framebufferScale);
        int top=(int)Math.floor((bounds.y()+8*art.scale)*framebufferScale);
        int bottom=(int)Math.ceil((bounds.y()+56*art.scale)*framebufferScale);
        com.mojang.blaze3d.systems.RenderSystem.enableScissor(left,mc.getWindow().getHeight()-bottom,right-left,bottom-top);
        GlStateManager.pushMatrix();GlStateManager.translate(bounds.x()+59*art.scale,bounds.y()+8*art.scale,0);
        GlStateManager.scale(art.scale*3,art.scale*3,1);
        // Native 0x9e1b90 requests texture-pack colors 0x1c2..0x1c4. Java's book-font
        // palette is the host adapter for that unavailable texture-pack color table.
        int color=enabled?(hovered?0xffff80:0x685e4a):(0x685e4a&0xfefefe)>>1;
        int y=0;for(var line:mc.font.split(runes[id],99)){mc.font.draw(PortGraphics.pose,line,0,y,color);y+=9;}
        GlStateManager.popMatrix();
        com.mojang.blaze3d.systems.RenderSystem.disableScissor();
        art.styledText(String.valueOf(enchant.costs[id]),enabled?"FJ_Label_Enchanting":"FJ_Label_Enchanting_Inactive",
            (bounds.x()-art.x)/art.scale+280,(bounds.y()-art.y)/art.scale+26);
      }
    }
  }
}
