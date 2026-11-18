package dev.legacyconsole.client.ui;
import java.util.*;import java.io.IOException;
import com.mojang.blaze3d.vertex.PoseStack;import com.mojang.blaze3d.platform.Lighting;
import net.minecraft.client.gui.screens.inventory.*;import net.minecraft.client.model.geom.*;
import net.minecraft.client.renderer.*;import net.minecraft.client.renderer.blockentity.BannerRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.world.inventory.*;import net.minecraft.world.item.*;import net.minecraft.world.level.block.entity.*;
import net.minecraft.network.chat.*;import net.minecraft.core.Registry;
import dev.legacyconsole.client.*;import dev.legacyconsole.client.input.*;import dev.legacyconsole.port.*;
/** Legacy4J's classic workstation layouts, adapted to the native 1.17 menu protocols. */
public final class ConsoleWorkstationScreen extends ConsoleContainerScreen implements ControllerScreen {
 private enum Kind {LOOM,CARTOGRAPHY,STONECUTTER,GRINDSTONE,SMITHING}
 private final Kind kind;private final String caption;private final int[][] oldPositions;private final Map<Integer,float[]> frames=new HashMap<>();
 private String mapName="";private net.minecraft.world.entity.decoration.ArmorStand preview;
 private int row,hash;private boolean scrolling;private ModelPart flag;
 public ConsoleWorkstationScreen(AbstractContainerScreen<?> source){super(source.getMenu());caption=source.getTitle().getString();kind=inventorySlots instanceof LoomMenu?Kind.LOOM:inventorySlots instanceof CartographyTableMenu?Kind.CARTOGRAPHY:inventorySlots instanceof StonecutterMenu?Kind.STONECUTTER:inventorySlots instanceof GrindstoneMenu?Kind.GRINDSTONE:Kind.SMITHING;oldPositions=new int[inventorySlots.slots.size()][2];for(var slot:inventorySlots.slots){oldPositions[slot.index][0]=slot.x;oldPositions[slot.index][1]=slot.y;}}
 private int inputs(){return kind==Kind.LOOM?4:kind==Kind.STONECUTTER?2:3;}
 @Override protected boolean consoleUiScaling(){return kind==Kind.SMITHING||kind==Kind.GRINDSTONE;}
 @Override public void initGui(){
  xSize=kind==Kind.LOOM||kind==Kind.STONECUTTER?215:207;ySize=kind==Kind.CARTOGRAPHY?254:kind==Kind.LOOM?217:kind==Kind.STONECUTTER?208:215;super.initGui();guiTop=Math.max(1,(height-ySize)/2-6);frames.clear();
  for(var slot:inventorySlots.slots)if(slot.index>=inputs()){int n=slot.getContainerSlot();int x=kind==Kind.LOOM||kind==Kind.STONECUTTER?14:10;int top=kind==Kind.CARTOGRAPHY?156:kind==Kind.LOOM?115:kind==Kind.STONECUTTER?108:kind==Kind.GRINDSTONE?116:117;int hotbar=kind==Kind.CARTOGRAPHY?225:kind==Kind.STONECUTTER?178:185;position(slot.index,x+n%9*21,n<9?hotbar:top+(n-9)/9*21,21);}
  switch(kind){case LOOM->{position(0,19,41,23);position(1,45,41,23);position(2,32,66,23);position(3,166,75,32);flag=mc.getEntityModels().bakeLayer(ModelLayers.BANNER).getChild("flag");}case CARTOGRAPHY->{position(0,10,62,23);position(1,10,105,23);position(2,166,82,27);}case STONECUTTER->{position(0,31,45,23);position(1,166,41,32);}case GRINDSTONE->{position(0,41,30,30);position(1,41,65,30);position(2,138,46,30);}case SMITHING->{position(0,21,60,23);position(1,52,60,23);position(2,127,56,30);}}
  if(kind==Kind.SMITHING){preview=new net.minecraft.world.entity.decoration.ArmorStand(mc.level,0,0,0);var standTag=new net.minecraft.nbt.CompoundTag();standTag.putBoolean("NoBasePlate",true);standTag.putBoolean("ShowArms",true);preview.readAdditionalSaveData(standTag);preview.setYRot(180);}
  rebuild();
 }
 private void position(int id,float x,float y,int size){Slot s=inventorySlots.getSlot(id);s.x=Math.round(x);s.y=Math.round(y);frames.put(id,new float[]{x-size/20f,y-size/20f,size,size});}
 @Override public void onGuiClosed(){for(var slot:inventorySlots.slots){slot.x=oldPositions[slot.index][0];slot.y=oldPositions[slot.index][1];}super.onGuiClosed();}
 private List<Integer> patterns(){if(!(inventorySlots instanceof LoomMenu loom)||!loom.getBannerSlot().hasItem()||!loom.getDyeSlot().hasItem())return List.of();ItemStack banner=loom.getBannerSlot().getItem();if(BannerBlockEntity.getPatternCount(banner)>=6)return List.of();ItemStack special=loom.getPatternSlot().getItem();if(special.getItem() instanceof BannerPatternItem p)return List.of(p.getBannerPattern().ordinal());List<Integer> result=new ArrayList<>();for(int i=1;i<BannerPattern.AVAILABLE_PATTERNS;i++)result.add(i);return result;}
 private int count(){return kind==Kind.LOOM?patterns().size():kind==Kind.STONECUTTER?((StonecutterMenu)inventorySlots).getNumRecipes():0;}
 private int maxRow(){return Math.max(0,(count()+3)/4-4);}
 private void rebuild(){buttonList.clear();row=Math.max(0,Math.min(maxRow(),row));if(kind==Kind.CARTOGRAPHY){ConsoleTextInput name=new ConsoleTextInput(9000,guiLeft+10,guiTop+38,120,18,mapName);name.enabled=inventorySlots.getSlot(0).hasItem();buttonList.add(name);return;}if(kind!=Kind.LOOM&&kind!=Kind.STONECUTTER)return;int count=count();for(int i=row*4;i<Math.min(count,row*4+16);i++)buttonList.add(new Choice(i,guiLeft+73+(i%4)*18,guiTop+19+(i-row*4)/4*18));}
 @Override public void updateScreen(){int next=1;for(int i=0;i<inputs();i++)next=31*next+inventorySlots.getSlot(i).getItem().save(new net.minecraft.nbt.CompoundTag()).hashCode();if(next!=hash){hash=next;if(kind==Kind.CARTOGRAPHY){ItemStack input=inventorySlots.getSlot(0).getItem();mapName=input.isEmpty()?"":input.getHoverName().getString();}rebuild();}}
 @Override protected int[] inventoryHighlightBounds(Slot slot){float[] r=frames.get(slot.index);return r==null?null:new int[]{Math.round(r[0]),Math.round(r[1]),Math.round(r[2]),Math.round(r[3])};}
 @Override protected float[] inventoryHighlightFace(Slot slot){float[] r=frames.get(slot.index);return new float[]{r[0]+r[2]/20,r[1]+r[3]/20,r[2]*.9f,r[3]*.9f};}
 @Override protected float[] itemDisplayBounds(Slot slot){float[] r=frames.get(slot.index);return new float[]{r[0]+r[2]/20,r[1]+r[3]/20,r[2]*.9f,r[3]*.9f};}
 @Override public float[] controllerSlotBoundsExact(Slot slot){return frames.get(slot.index).clone();}
 @Override protected void drawGuiContainerForegroundLayer(int x,int y){}
 private void sprite(String name,float x,float y,float w,float h){ConsoleTheme.spriteAt("gui/sprites/container/"+name,guiLeft+x,guiTop+y,w,h);}
 @Override protected void drawGuiContainerBackgroundLayer(float partial,int mx,int my){
  if(kind==Kind.LOOM)ConsoleTheme.sprite("gui/sprites/tiles/small_panel",guiLeft,guiTop,xSize,ySize);else ConsoleTheme.panel(guiLeft,guiTop,xSize,ySize);int titleX=kind==Kind.CARTOGRAPHY?(xSize-fontRendererObj.getStringWidth(caption))/2:kind==Kind.SMITHING?56:kind==Kind.LOOM||kind==Kind.STONECUTTER?14:10;ConsoleTheme.text(caption,guiLeft+titleX,guiTop+(kind==Kind.SMITHING?25:kind==Kind.GRINDSTONE?11:10),ConsoleTheme.DARK);
  int invY=kind==Kind.CARTOGRAPHY?144:kind==Kind.LOOM?104:kind==Kind.STONECUTTER?95:105;ConsoleTheme.text("Inventory",guiLeft+(kind==Kind.LOOM||kind==Kind.STONECUTTER?14:10),guiTop+invY,ConsoleTheme.DARK);
  for(var entry:frames.entrySet()){float[] f=entry.getValue();ConsoleTheme.spriteAt("gui/sprites/container/icon_holder",guiLeft+f[0],guiTop+f[1],f[2],f[3]);}
  if(kind==Kind.LOOM||kind==Kind.STONECUTTER){LceWidgets.panel("square_recess",guiLeft+(kind==Kind.STONECUTTER?70:72),guiTop+18,75,75);LceWidgets.panel("square_recess",guiLeft+(kind==Kind.STONECUTTER?148.5f:149.5f),guiTop+18,13,75);LceWidgets.panel("panel16",guiLeft+147.5f,guiTop+17+(maxRow()==0?0:61.5f*row/maxRow()),16,16);if(row>0)ConsoleTheme.sprite("gui/sprites/widget/scroll_up",guiLeft+152,guiTop+7,7,5);if(row<maxRow())ConsoleTheme.sprite("gui/sprites/widget/scroll_down",guiLeft+152,guiTop+97,7,5);}
  if(kind==Kind.LOOM){
   String[] emptyIcons={"banner_slot","dye_slot","pattern_slot"};
   for(int i=0;i<3;i++)if(!inventorySlots.getSlot(i).hasItem()){
    float[] r=frames.get(i);ConsoleTheme.spriteAt("gui/sprites/container/loom/"+emptyIcons[i],guiLeft+r[0]+r[2]/20,guiTop+r[1]+r[3]/20,r[2]*.9f,r[3]*.9f);
   }
   if(BannerBlockEntity.getPatternCount(inventorySlots.getSlot(0).getItem())>=6)sprite("error_cross",168,28,24,24);
   LceWidgets.panel("square_recess",guiLeft+164.5f,guiTop+7,32,64);ItemStack result=inventorySlots.getSlot(3).getItem();if(result.getItem() instanceof BannerItem banner)banner(BannerBlockEntity.createPatterns(banner.getColor(),BannerBlockEntity.getItemPatterns(result)),guiLeft+168.5f,guiTop+69,24);}
  if(kind==Kind.GRINDSTONE){sprite("arrow",85,50,33,22.5f);if((inventorySlots.getSlot(0).hasItem()||inventorySlots.getSlot(1).hasItem())&&!inventorySlots.getSlot(2).hasItem())sprite("error_cross",88,50,22.5f,22.5f);}
  if(kind==Kind.SMITHING){if(preview!=null){for(var equipment:net.minecraft.world.entity.EquipmentSlot.values())preview.setItemSlot(equipment,ItemStack.EMPTY);ItemStack result=inventorySlots.getSlot(2).getItem();if(!result.isEmpty())preview.setItemSlot(net.minecraft.world.entity.LivingEntity.getEquipmentSlotForItem(result),result);InventoryScreen.renderEntityInInventory(guiLeft+182,guiTop+95,35,0,0,preview);}sprite("smithing_hammer",13.5f,9.5f,37.5f,37.5f);sprite("arrow",82,59,33,22.5f);if(inventorySlots.getSlot(0).hasItem()&&inventorySlots.getSlot(1).hasItem()&&!inventorySlots.getSlot(2).hasItem())sprite("error_cross",88,59,22.5f,22.5f);}
  if(kind==Kind.CARTOGRAPHY){ConsoleTheme.text("Map Name",guiLeft+10,guiTop+27,ConsoleTheme.DARK);sprite("combiner_plus",14,88,13,13);sprite("arrow",36,87,22,15);sprite("arrow",139,87,22,15);ItemStack map=inventorySlots.getSlot(0).getItem(),material=inventorySlots.getSlot(1).getItem();String mode=material.is(Items.MAP)?"copy":material.is(Items.PAPER)?"zoom":material.is(Items.GLASS_PANE)?"glass":"map";sprite("cartography_table_"+(map.is(Items.FILLED_MAP)?mode:"empty"),70,61,66,66);if(map.is(Items.FILLED_MAP)){var data=MapItem.getSavedData(map,mc.level);Integer id=MapItem.getMapId(map);if(data!=null&&id!=null){PoseStack pose=new PoseStack();pose.translate(guiLeft+78,guiTop+69,1);pose.scale(.39f,.39f,1);var buffers=mc.renderBuffers().bufferSource();mc.gameRenderer.getMapRenderer().render(pose,buffers,id,data,true,0xF000F0);buffers.endBatch();}if(data!=null&&(data.locked&&(material.is(Items.PAPER)||material.is(Items.GLASS_PANE))||data.scale>=4&&material.is(Items.PAPER)))sprite("error_cross",40,87,15,15);}}
  drawInventoryHighlight(mx,my);
 }
 private void banner(List<com.mojang.datafixers.util.Pair<BannerPattern,DyeColor>> patterns,float x,float y,float size){
  // Legacy4J model size/centering with the native 1.17 banner draw pose.
  Lighting.setupForFlatItems();
  PoseStack pose=new PoseStack();pose.pushPose();
  try{pose.translate(x,y,100);pose.scale(size,-size,1);
   if(size==6)pose.translate(.5,.5,0); // native pattern-thumbnail centering
   pose.translate(.5,.5,.5);pose.scale(1,-1,-1);flag.xRot=0;flag.y=-32;
   var buffers=mc.renderBuffers().bufferSource();
   // The native GUI text pipeline is lit by the supplied full-bright lightmap,
   // without world/entity diffuse lighting. This also keeps OptiFine from
   // darkening the flat banner previews when a shaderpack is enabled.
   flag.render(pose,ModelBakery.BANNER_BASE.sprite().wrap(buffers.getBuffer(RenderType.text(ModelBakery.BANNER_BASE.atlasLocation()))),0xF000F0,OverlayTexture.NO_OVERLAY);
   for(int i=0;i<Math.min(17,patterns.size());i++){
    var layer=patterns.get(i);float[] rgb=layer.getSecond().getTextureDiffuseColors();
    var material=Sheets.getBannerMaterial(layer.getFirst());
    flag.render(pose,material.sprite().wrap(buffers.getBuffer(RenderType.text(material.atlasLocation()))),0xF000F0,OverlayTexture.NO_OVERLAY,rgb[0],rgb[1],rgb[2],1);
   }
   buffers.endBatch();
  }finally{pose.popPose();Lighting.setupFor3DItems();}
 }
 private void editMapName(){if(!inventorySlots.getSlot(0).hasItem())return;mc.setScreen(new ConsoleKeyboard(this,"Map Name",mapName,value->{mapName=value;String name=!inventorySlots.getSlot(0).getItem().hasCustomHoverName()&&value.equals(inventorySlots.getSlot(0).getItem().getHoverName().getString())?"":value;((RenameMapMenu)inventorySlots).legacy$setMapName(name);mc.player.connection.send(new net.minecraft.network.protocol.game.ServerboundRenameItemPacket(name));},50,value->true));}
 private final class Choice extends GuiButton {
  Choice(int id,int x,int y){super(id,x,y,18,18,"");}
  @Override public void drawButton(net.minecraft.client.Minecraft game,int mx,int my){hovered=ConsoleTheme.buttonHover(this,mx,my);if(kind==Kind.LOOM&&id>=patterns().size())return;int actual=kind==Kind.LOOM?patterns().get(id):id;int selected=kind==Kind.LOOM?((LoomMenu)inventorySlots).getSelectedBannerPatternIndex():((StonecutterMenu)inventorySlots).getSelectedRecipeIndex();String path=actual==selected?"button_slot_selected":hovered?"button_slot_highlighted":"button_slot";ConsoleTheme.spriteAt("gui/sprites/container/"+path,xPosition+(kind==Kind.LOOM?.5f:0),yPosition+(kind==Kind.LOOM?.5f:0),18,18);if(kind==Kind.LOOM)banner(List.of(com.mojang.datafixers.util.Pair.of(BannerPattern.BASE,DyeColor.GRAY),com.mojang.datafixers.util.Pair.of(BannerPattern.values()[actual],DyeColor.WHITE)),xPosition+4,yPosition+19.5f,6);else ConsoleTheme.item(((StonecutterMenu)inventorySlots).getRecipes().get(id).getResultItem(),xPosition+1,yPosition+1);}
 }
 @Override protected void actionPerformed(GuiButton b){if(b.id==9000){editMapName();return;}int id=kind==Kind.LOOM?patterns().get(b.id):b.id;if(inventorySlots.clickMenuButton(mc.player,id)){mc.gameMode.handleInventoryButtonClick(inventorySlots.containerId,id);ConsoleAudio.play("focus");}}
 private void scroll(int d){int old=row;row=Math.max(0,Math.min(maxRow(),row+d));if(old!=row){rebuild();ConsoleAudio.play("scroll");}}
 @Override public void handleMouseInput()throws IOException{super.handleMouseInput();int wheel=Mouse.getEventDWheel();if(wheel!=0)scroll(wheel>0?-1:1);}
 @Override protected void mouseClicked(int x,int y,int button)throws IOException{if(button==0&&(kind==Kind.LOOM||kind==Kind.STONECUTTER)&&x>=guiLeft+148&&x<guiLeft+163&&y>=guiTop+18&&y<guiTop+93){scrolling=true;row=Math.max(0,Math.min(maxRow(),Math.round(maxRow()*(y-guiTop-18)/75f)));rebuild();return;}super.mouseClicked(x,y,button);}
 @Override public boolean mouseDragged(double x,double y,int b,double dx,double dy){if(scrolling){row=Math.max(0,Math.min(maxRow(),Math.round(maxRow()*((float)y-guiTop-18)/75)));rebuild();return true;}return super.mouseDragged(x,y,b,dx,dy);}
 @Override protected void mouseReleased(int x,int y,int b){scrolling=false;super.mouseReleased(x,y,b);}
 @Override public void drawScreen(int mx,int my,float partial){drawDefaultBackground();super.drawScreen(mx,my,partial);ConsoleTheme.footer(width,height,"cross","Take","square",inventorySlots.getCarried().isEmpty()?"Split":"Place One","triangle","Quick Move","circle","Back");}
 @Override public boolean controllerDirection(int d){return false;}
 @Override public boolean controllerButton(int b){if((kind==Kind.LOOM||kind==Kind.STONECUTTER)&&(b==PadState.L2||b==PadState.R2)){scroll(b==PadState.L2?-1:1);return true;}return false;}
 @Override public boolean controllerPointer(){return true;}
}
