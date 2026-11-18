package dev.legacyconsole.client.ui;

import dev.legacyconsole.LegacyConsole;
import dev.legacyconsole.client.ConsoleAudio;
import dev.legacyconsole.client.input.*;
import dev.legacyconsole.crafting.*;
import dev.legacyconsole.network.*;
import dev.legacyconsole.port.*;
import java.io.IOException;
import java.util.*;
import java.util.function.Predicate;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.*;
import static dev.legacyconsole.crafting.Ps4BannerPatterns.*;

/** PS4 IUIScene_CraftingMenu's banner, firework and dye state machine and FUI scene. */
public final class ConsoleSpecialCrafting extends ConsoleContainerScreen implements ControllerScreen {
  private final ConsoleContainer container;
  private final int type;
  private int mode,selected,offset,ticks,seenRevision,inventoryHash;
  public int tutorialType(){return type;}
  private final List<Selector> selectors=new ArrayList<>();
  private final List<ItemStack> added=new ArrayList<>();
  private List<ItemStack> fixedGrid=List.of();
  private final List<Layer> layers=new ArrayList<>();
  private ItemStack banner=ItemStack.EMPTY;
  private RecipeCatalog.Entry recipe;
  private Ps4BannerCrafting.Plan bannerPlan;
  private boolean blankBannerAvailable;
  private List<ItemStack> grid=List.of();
  private Ps4CraftingArt art;
  private final CraftRepeater repeater=new CraftRepeater();
  private int repeatSource;

  public ConsoleSpecialCrafting(ConsoleContainer container,int type) {
    super(container);this.container=container;this.type=type;seenRevision=container.revision;
    if((type<1||type>3)&&type!=5)throw new IllegalArgumentException("Unsupported PS4 crafting type");
    if((type==5)!=(container.kind==ConsoleContainer.LOOM))throw new IllegalArgumentException("Incorrect loom context");
  }
  private static String text(String id){return Ps4CraftingArt.label(id);}
  private boolean same(ItemStack a,ItemStack b){return !a.isEmpty() && !b.isEmpty() && a.sameItem(b) && ItemStack.tagMatches(a,b);}
  private List<ItemStack> inventory(){return mc.player.getInventory().items;}
  private int count(ItemStack item) {
    int count=0;for(ItemStack s:inventory())if(same(s,item))count+=s.getCount();return count;
  }
  private final class Selector {
    final String title;
    final List<ItemStack> fixed;
    final Predicate<ItemStack> filter;
    final Pattern pattern;
    List<ItemStack> choices=List.of();
    ItemStack current=ItemStack.EMPTY;
    int index;
    Selector(String title,List<ItemStack> fixed,Predicate<ItemStack> filter,Pattern pattern) {
      this.title=title;this.fixed=fixed;this.filter=filter;this.pattern=pattern;refresh();
    }
    void refresh() {
      if(fixed!=null)choices=fixed;
      else {
        List<ItemStack> found=new ArrayList<>();
        for(ItemStack item:inventory())if(!item.isEmpty() && filter.test(item) && found.stream().noneMatch(s -> same(s,item)))found.add(unit(item));
        choices=found;
      }
      int old=-1;for(int i=0;i<choices.size();i++)if(same(current,choices.get(i))){old=i;break;}
      index=old<0?Math.min(index,Math.max(0,choices.size()-1)):old;
      current=choices.isEmpty()?ItemStack.EMPTY:choices.get(index);
    }
    void cycle(int delta) {
      refresh();if(choices.isEmpty())return;
      index=Math.floorMod(index+delta,choices.size());current=choices.get(index);
    }
  }
  private void fixed(String title,ItemStack... items){selectors.add(new Selector(title,List.of(items),null,null));}
  private void owned(String title,Predicate<ItemStack> filter){selectors.add(new Selector(title,null,filter,null));}
  private ItemStack[] colors(){return Ps4Dyes.choices();}
  private ItemStack value(int column){return selectors.get(column).current;}
  private int color(Selector s){return Ps4Dyes.color(s.current)==null?0:Ps4Dyes.color(s.current).getId();}
  private boolean banners(){return type==1||type==5;}
  private boolean craftingTable(){return container.kind==ConsoleContainer.TABLE;}
  private int groups(){return banners()?(craftingTable()||type==5?6:3):type==2?3:2;}
  private int patternGroup(){return craftingTable()||type==5?mode:mode==1?4:5;}
  private boolean editingBanner(){return banners() && mode>0;}
  private int selectBannerColumn(){return craftingTable()?1:0;}
  private int copyBannerColumn(){return selectBannerColumn()+1;}
  private boolean selectingBanner(){return banners() && mode==0 && selected==selectBannerColumn();}

  private void resetSelectors() {
    selectors.clear();selected=offset=0;repeater.cancel();
    if(banners()) {
      if(mode==0) {
        if(craftingTable()) {
          ItemStack[] banners=Arrays.stream(DyeColor.values()).map(c -> new ItemStack(Registry.ITEM.get(new ResourceLocation(c.getName()+"_banner")))).toArray(ItemStack[]::new);
          fixed(text("7f57b5e0"),banners);
        }
        owned(text("93d2882c"),s -> s.getItem() instanceof BannerItem && Ps4BannerPatterns.count(s)<6);
        owned(text("f6bfb839"),s -> s.getItem() instanceof BannerItem && Ps4BannerPatterns.count(s)>0);
      } else {
        for(Pattern p:Ps4BannerPatterns.group(patternGroup(),container.table,type==5))selectors.add(new Selector("",List.of(Ps4Dyes.bannerChoices()),null,p));
      }
    } else if(type==2) {
      if(mode==0) {
        fixed(text("d0d94a48"),colors());
        fixed(text("dcca69ba"),new ItemStack(Items.FIRE_CHARGE),new ItemStack(Items.GOLD_NUGGET),
            new ItemStack(Items.SKELETON_SKULL),new ItemStack(Items.WITHER_SKELETON_SKULL),new ItemStack(Items.ZOMBIE_HEAD),
            new ItemStack(Items.PLAYER_HEAD),new ItemStack(Items.CREEPER_HEAD),new ItemStack(Items.DRAGON_HEAD),new ItemStack(Items.FEATHER));
        fixed(text("7a3f69fa"),new ItemStack(Items.GLOWSTONE_DUST),new ItemStack(Items.DIAMOND));
      } else if(mode==1) {
        owned(text("80ac0ce1"),s -> s.is(Items.FIREWORK_STAR));fixed(text("529a620a"),colors());
      } else {
        fixed(text("3a0766fb"),new ItemStack(Items.GUNPOWDER));owned(text("80ac0ce1"),s -> s.is(Items.FIREWORK_STAR));
      }
    } else {
      owned(mode==0?text("6ca19582"):text("47c65cd1"),s -> mode==0?s.getItem() instanceof DyeableLeatherItem:
          s.getItem() instanceof BedItem || s.getItem() instanceof BlockItem b &&
              (b.getBlock() instanceof ShulkerBoxBlock || Ps4Dyes.woolColor(s)!=null));
      fixed(text("d0d94a48"),colors());
    }
    compose();rebuildButtons();
  }
  @Override public void initGui() {
    art=new Ps4CraftingArt(width,height);xSize=Math.round(1034*art.scale());ySize=Math.round(739*art.scale());
    super.initGui();guiLeft=(int)Math.floor(art.x());guiTop=(int)Math.floor(art.y());mc.player.containerMenu=container;
    for(int i=0;i<36;i++) {
      Slot slot=container.getSlot(i);Ps4CraftingArt.Rect r=inventoryRect(i);
      slot.x=Math.round(r.x()-guiLeft);slot.y=Math.round(r.y()-guiTop);
    }
    if(selectors.isEmpty())resetSelectors();else rebuildButtons();
  }
  private Ps4CraftingArt.Rect inventoryRect(int index) {
    return art.rect(556+index%9*48,index<27?466+index/9*48:634,48,48);
  }
  private ItemStack displayBanner(){return banner.isEmpty()?new ItemStack(Items.WHITE_BANNER):banner;}
  private ItemStack previewBanner() {
    ItemStack preview=displayBanner();
    for(Layer layer:layers)preview=apply(preview,get(layer.recipe()),layer.color());
    return preview;
  }
  private List<ItemStack> emptyGrid(){return new ArrayList<>(Collections.nCopies(container.table?9:4,ItemStack.EMPTY));}
  private List<ItemStack> blankBannerGrid(ItemStack result) {
    List<ItemStack> cells=emptyGrid();
    if(craftingTable() && result.getItem() instanceof BannerItem item) {
      Item wool=Registry.ITEM.get(new ResourceLocation(item.getColor().getName()+"_wool"));
      for(int i=0;i<6;i++)cells.set(i,new ItemStack(wool));
      cells.set(7,new ItemStack(Items.STICK));
    }
    return cells;
  }
  private void compose() {
    for(Selector selector:selectors)selector.refresh();
    recipe=null;bannerPlan=null;grid=emptyGrid();
    int size=container.table?3:2;
    if(editingBanner()) {
      if(!selectors.isEmpty()) {
        Selector selector=selectors.get(selected);grid=selector.pattern.grid(banner,unit(selector.current),container.table,type==5);
      }
      bannerPlan=Ps4BannerCrafting.plan(banner,layers,container.table,type==5);
    } else if(banners()) {
      if(craftingTable() && selected==0) {
        grid=blankBannerGrid(value(0));
      } else if(selected==copyBannerColumn() && !value(selected).isEmpty()) {
        grid.set(0,unit(value(selected)));grid.set(1,new ItemStack(value(selected).getItem()));
      } else if(selectingBanner())grid.set(0,unit(value(selected)));
      if(!selectingBanner())recipe=DynamicRecipes.entry(grid,mc.level,container.table);
    } else {
      if(type==2 && mode==0){grid.set(1,unit(value(0)));grid.set(size+1,new ItemStack(Items.GUNPOWDER));}
      else if(type==2 && mode==1){grid.set(1,unit(value(1)));grid.set(size+1,unit(value(0)));}
      else if(type==2){grid.set(1,new ItemStack(Items.GUNPOWDER));grid.set(size+1,new ItemStack(Items.PAPER));}
      else {grid.set(1,unit(value(1)));grid.set(size+1,unit(value(0)));}
      if(!added.isEmpty())grid=new ArrayList<>(fixedGrid);
      for(ItemStack extra:added) {
        int slot=-1;for(int i=0;i<grid.size();i++)if(grid.get(i).isEmpty()){slot=i;break;}
        if(slot<0)break;grid.set(slot,unit(extra));
      }
      boolean missing=(type==3 || type==2 && mode==1) && value(0).isEmpty();
      if(!missing)recipe=DynamicRecipes.entry(grid,mc.level,container.table);
    }
    blankBannerAvailable=false;
    if(banners() && mode==0 && craftingTable() && !selectors.isEmpty()) {
      var creation=selected==0?recipe:DynamicRecipes.entry(blankBannerGrid(value(0)),mc.level,true);
      blankBannerAvailable=creation!=null && creation.allocate(inventory())!=null;
    }
    inventoryHash=inventoryHash();
  }
  private int inventoryHash(){int hash=1;for(ItemStack s:inventory())hash=31*hash+s.serializeNBT().hashCode();return hash;}
  private boolean canCraft() {
    return editingBanner()?bannerPlan!=null && bannerPlan.allocate(inventory())!=null:
        recipe!=null && recipe.allocate(inventory())!=null;
  }
  private long recipeKey(){return editingBanner()?layers.hashCode()*31L+banner.serializeNBT().hashCode():recipe==null?0:recipe.key;}
  private boolean canAdd() {
    if(selectors.isEmpty() || !container.getCarried().isEmpty())return false;
    if(editingBanner()) {
      List<Layer> trial=new ArrayList<>(layers);Selector s=selectors.get(selected);trial.add(new Layer(s.pattern.source_hash,color(s),Registry.ITEM.getKey(s.current.getItem()).toString()));
      var plan=Ps4BannerCrafting.plan(banner,trial,container.table,type==5);return plan!=null && plan.allocate(inventory())!=null;
    }
    if(banners())return (selectingBanner() || selected==copyBannerColumn()) && !value(selected).isEmpty() && Ps4BannerPatterns.count(value(selected))<6;
    if(type==2 && mode==1 && selected==0 || type==3 && selected==0)return false;
    if(type==3 && mode==1)return false;
    ItemStack current=value(selected);if(current.isEmpty() || grid.stream().noneMatch(ItemStack::isEmpty))return false;
    if(type==2 && mode==0) {
      if(selected==1 && added.stream().anyMatch(s -> Ps4Dyes.color(s)==null && !s.is(Items.GLOWSTONE_DUST) && !s.is(Items.DIAMOND)))return false;
      if(selected==2 && added.stream().anyMatch(s -> s.is(current.getItem())))return false;
    }
    if(type==2 && mode==2 && selected==0 && added.stream().filter(s -> s.is(Items.GUNPOWDER)).count()>=2)return false;
    return count(current)>grid.stream().filter(s -> same(s,current)).count();
  }
  private void addIngredient() {
    if(!canAdd()){ConsoleAudio.play("fail");return;}
    if(banners() && mode==0) {
      banner=unit(value(selected));layers.clear();changeMode(1);return;
    }
    if(editingBanner()) {
      Selector selector=selectors.get(selected);layers.add(new Layer(selector.pattern.source_hash,color(selector),Registry.ITEM.getKey(selector.current.getItem()).toString()));
    } else {if(added.isEmpty())fixedGrid=grid.stream().map(ItemStack::copy).toList();added.add(unit(value(selected)));}
    repeater.cancel();compose();ConsoleAudio.play("focus");
  }
  private void removeIngredient() {
    if(editingBanner() && !layers.isEmpty())layers.remove(layers.size()-1);
    else if(!added.isEmpty()){added.remove(added.size()-1);if(added.isEmpty())fixedGrid=List.of();}
    repeater.cancel();compose();ConsoleAudio.play("focus");
  }
  private void changeMode(int next) {
    if(banners()) {
      if(banner.isEmpty()){if(next!=0)return;mode=0;}
      else mode=1+Math.floorMod(next-1,groups()-1);
    } else {mode=Math.floorMod(next,groups());added.clear();fixedGrid=List.of();}
    resetSelectors();ConsoleAudio.play("tab");
  }
  private void changeType(int next) {
    if(type==5)return;
    if(!container.getCarried().isEmpty()){ConsoleAudio.play("fail");return;}
    next=Math.floorMod(next,4);mc.setScreen(next==0?new ConsoleCraftingScreen(container):new ConsoleSpecialCrafting(container,next));
  }
  private void edit(int delta){if(selectors.isEmpty())return;selectors.get(selected).cycle(delta);repeater.cancel();compose();ConsoleAudio.play("focus");}
  private void selectColumn(int index) {
    if(selectors.isEmpty())return;
    selected=Math.floorMod(index,selectors.size());if(selected<offset)offset=selected;if(selected>=offset+12)offset=selected-11;
    repeater.cancel();compose();ConsoleAudio.play("focus");
  }
  private void craft(int batches) {
    if(selectingBanner()){addIngredient();return;}
    if(!canCraft() || !container.getCarried().isEmpty()){ConsoleAudio.play("fail");return;}
    sendCraft(batches);
    if(batches==1){repeater.press(recipeKey(),ticks);repeatSource=ControllerInput.INSTANCE.state.held(PadState.CROSS)?1:Mouse.isButtonDown(0)?2:3;}
  }
  private void sendCraft(int batches) {
    if(editingBanner())LegacyConsole.NETWORK.sendToServer(new Ps4BannerCraftRequest(container.containerId,banner,layers,batches));
    else LegacyConsole.NETWORK.sendToServer(new DynamicCraftRequest(container.containerId,grid,batches));
  }
  private ItemStack output(){return editingBanner()?previewBanner():recipe==null?ItemStack.EMPTY:recipe.output;}
  private Ps4CraftingArt.Rect selectorRect(int column) {
    if(editingBanner())return art.rect(30+(column-offset)*82,215,82,82);
    String prefix=selectors.size()==3?"Slot3_":"Slot2_";return art.control(prefix+(column+1));
  }
  private GuiButton hit(int id,Ps4CraftingArt.Rect r) {
    return new GuiButton(id,(int)r.x(),(int)r.y(),(int)Math.ceil(r.w()),(int)Math.ceil(r.h()),"") {
      @Override public void drawButton(net.minecraft.client.Minecraft mc,int x,int y){}
    };
  }
  private void rebuildButtons() {
    if(art==null)return;buttonList.clear();
    if(type!=5)for(int i=0;i<4;i++)buttonList.add(hit(900+i,art.rect(-113,119+i*120,135,120)));
    for(int i=0;i<groups();i++)buttonList.add(hit(200+i,art.rect(6+i*147,0,147,104)));
    for(int i=0;i<selectors.size();i++)if(i>=offset && i<offset+12)buttonList.add(hit(100+i,selectorRect(i)));
    buttonList.add(hit(0,art.control("CraftingOutput")));
  }
  @Override protected void actionPerformed(GuiButton b) {
    if(b.id>=900)changeType(b.id-900);
    else if(b.id>=200)changeMode(b.id-200);
    else if(b.id>=100){if(selected==b.id-100)craft(1);else selectColumn(b.id-100);}
    else craft(1);
  }
  @Override protected void mouseClicked(int x,int y,int button)throws IOException {
    for(GuiButton b:buttonList)if(x>=b.xPosition && y>=b.yPosition && x<b.xPosition+b.width && y<b.yPosition+b.height) {
      if(b.id==0 && button==1)craft(64);else if(button==0)actionPerformed(b);return;
    }
    if(!selectors.isEmpty()) {
      var r=selectorRect(selected);float half=r.h();
      if(x>=r.x() && x<r.x()+r.w() && y>=r.y()-half && y<r.y()+half*2)edit(y<r.y()? -1:1);
    }
  }
  @Override public void handleMouseInput()throws IOException {super.handleMouseInput();int wheel=Mouse.getEventDWheel();if(wheel!=0)edit(wheel>0?-1:1);}
  private ItemStack icon(int column,int delta) {
    Selector s=selectors.get(column);if(s.choices.isEmpty())return ItemStack.EMPTY;
    ItemStack value=s.choices.get(Math.floorMod(s.index+delta,s.choices.size()));
    if(s.pattern!=null)return apply(displayBanner(),s.pattern,Ps4Dyes.color(value).getId());
    return value;
  }
  private ItemStack tabIcon(int group) {
    if(banners()) {
      ItemStack result=new ItemStack(Items.WHITE_BANNER);
      if(group>0) {
        int id=craftingTable()||type==5?group:group==1?4:5;
        List<Pattern> patterns=Ps4BannerPatterns.group(id,container.table,type==5);
        if(!patterns.isEmpty())result=apply(result,get(Ps4CraftingArt.DATA.tab_patterns.get(id-1)),DyeColor.BLACK.getId());
      }
      return result;
    }
    if(type==2) {
      ItemStack result=new ItemStack(group==2?Items.FIREWORK_ROCKET:Items.FIREWORK_STAR);
      if(group==1)result.getOrCreateTagElement("Explosion").putIntArray("Colors",new int[]{DyeColor.RED.getFireworkColor()});return result;
    }
    if(group==1)return new ItemStack(Items.RED_DYE);
    ItemStack result=new ItemStack(Items.LEATHER_CHESTPLATE);((DyeableLeatherItem)result.getItem()).setColor(result,DyeColor.GREEN.getFireworkColor());return result;
  }
  private ItemStack typeIcon(int index) {
    return switch(index){case 0->new ItemStack(Items.CRAFTING_TABLE);case 1->new ItemStack(Items.WHITE_BANNER);case 2->new ItemStack(Items.FIREWORK_ROCKET);default->tabIconForDye();};
  }
  private ItemStack tabIconForDye(){ItemStack stack=new ItemStack(Items.LEATHER_CHESTPLATE);((DyeableLeatherItem)stack.getItem()).setColor(stack,DyeColor.GREEN.getFireworkColor());return stack;}
  private String heading() {
    if(editingBanner())return text(new String[]{"","133e5cd4","b35ee68b","dd9417ba","f8e4117f","8078384b"}[patternGroup()]);
    return selectors.isEmpty()?"":selectors.get(selected).title;
  }
  private void tabs(boolean active) {
    for(int i=0;i<groups();i++)if((i==mode)==active) {
      if(active)art.sprite(i==0?"LargeTabLeft":i==groups()-1?"LargeTabRight":"LargeTabMiddle",6+i*147,0);
      else art.sprite("LargeOffTab",6+i*147,10);
      art.item(tabIcon(i),art.rect(46+i*147,active?18:28,72,72),72);
    }
    if(type!=5)for(int i=0;i<4;i++)if((i==type)==active) {
      art.verticalTab(i,active,typeIcon(i));
    }
  }
  private void drawSelectors() {
    int end=Math.min(selectors.size(),offset+12);
    for(int i=offset;i<end;i++)if(i!=selected)drawSelector(i);
    // The wider focused selector overlaps its neighbours. Paint it above the
    // entire row so later squares cannot cover its right border or highlight.
    if(selected>=offset && selected<end)drawSelector(selected);
  }
  private void drawSelector(int i) {
      var r=selectorRect(i);Selector s=selectors.get(i);
      if(editingBanner() && i==selected && s.choices.size()>1) {
        boolean three=s.choices.size()>2;
        float sourceX=(r.x()-art.x())/art.scale()-19;
        float sourceY=(r.y()-art.y())/art.scale()-(three?153:71);
        art.sprite(three?"Crafting_3SlotLargeV":"Crafting_2SlotLargeV",sourceX,sourceY);
        if(three)art.slot(icon(i,-1),new Ps4CraftingArt.Rect(r.x(),r.y()-r.h(),r.w(),r.h()),false,false,false);
        art.slot(icon(i,1),new Ps4CraftingArt.Rect(r.x(),r.y()+r.h(),r.w(),r.h()),false,false,false);
      }
      boolean missing=s.pattern==null && !value(i).isEmpty() && count(value(i))==0;
      if(banners() && mode==0 && craftingTable() && i==0)missing=!blankBannerAvailable;
      art.slot(icon(i,0),r,i==selected,missing,false);
      if(!editingBanner() && i==selected && s.choices.size()>1)art.selectorArrows(r);
  }
  private void drawGrid() {
    int size=container.table?3:2;var r=art.control(container.table?"IngredientsLayout":"IngredientsLayout_Small");
    boolean[] missing=RecipeCatalog.missingGridItems(grid,inventory());
    for(int i=0;i<grid.size();i++) {
      var cell=new Ps4CraftingArt.Rect(r.x()+i%size*r.w()/size,r.y()+i/size*r.h()/size,r.w()/size,r.h()/size);
      art.slot(grid.get(i),cell,false,missing[i]);
    }
    art.sprite("Arrow_Small_Off",290,564);
    art.slot(output(),art.control("CraftingOutput"),false,!output().isEmpty() && !canCraft());
    String name=output().isEmpty()?text("cb14bd42"):output().getHoverName().getString();
    if(name==null)name="Ingredients";
    art.label(name,31,426,460,true,ConsoleTheme.DARK);
  }
  @Override protected void drawGuiContainerBackgroundLayer(float partial,int mx,int my) {
    tabs(false);art.panels();tabs(true);
    if(!editingBanner())art.descriptionPanel();
    if(editingBanner())art.label(heading(),23,143,970,true,ConsoleTheme.DARK);
    else art.label(heading(),31,143,460,true,ConsoleTheme.DARK);
    drawSelectors();drawGrid();
    if(editingBanner()) {
      art.label(text("d047a3ce"),535,412,465,true,ConsoleTheme.DARK);
      art.item(previewBanner(),art.control("PreviewSlot"),233);
    } else {
      art.label(text("a89bcc3a"),535,412,465,true,ConsoleTheme.DARK);
      for(int i=0;i<36;i++)art.slot(container.getSlot(i).getItem(),inventoryRect(i),false,false);
    }
    if(!selectors.isEmpty() && !editingBanner()) {
      ItemStack info=editingBanner()?previewBanner():icon(selected,0);
      if(!info.isEmpty()) {
        art.description(info,true);
      }
    }
    rebuildButtons();
  }
  @Override protected void drawSlot(Slot slot) {} // PS4 crafting inventory is a read-only display.
  @Override protected boolean isPointInRegion(int x,int y,int w,int h,int mx,int my){return false;}
  @Override protected void drawGuiContainerForegroundLayer(int x,int y){}
  @Override public void drawScreen(int mx,int my,float partial) {
    drawDefaultBackground();super.drawScreen(mx,my,partial);
    List<String> prompts=new ArrayList<>();
    if(canCraft())Collections.addAll(prompts,"cross",text(banners() && mode==0 && selected==copyBannerColumn()?"fe4f3a12":"5f116e91"));
    if(banners() && mode==0 && canAdd())Collections.addAll(prompts,"triangle",text("8d6323ed"));
    else if(canAdd())Collections.addAll(prompts,"triangle",text("91d18454"));
    if(!added.isEmpty() || !layers.isEmpty())Collections.addAll(prompts,"square",text("9219698b"));
    Collections.addAll(prompts,"circle",text("ded2ef45"));
    if(type!=5)Collections.addAll(prompts,"left_trigger","","right_trigger",text("f73b4f2d"));
    if(!banners() || !banner.isEmpty())Collections.addAll(prompts,"left_bumper","","right_bumper",text("06974812"));
    ConsoleTheme.footer(width,height,prompts.toArray(String[]::new));
  }
  @Override public void updateScreen() {
    super.updateScreen();ticks++;
    if(seenRevision!=container.revision) {
      seenRevision=container.revision;ConsoleAudio.play(container.status==NativeCrafting.OK?"craft":"fail");
      if(editingBanner() && container.status==NativeCrafting.OK && bannerPlan!=null) {
        banner=ItemStack.EMPTY;layers.clear();mode=0;resetSelectors();repeater.cancel();
      }
      compose();if(container.status!=NativeCrafting.OK)repeater.cancel();
    } else if(inventoryHash()!=inventoryHash)compose();
    boolean held=repeatSource==1?ControllerInput.INSTANCE.state.held(PadState.CROSS):repeatSource==2?Mouse.isButtonDown(0):Keyboard.isKeyDown(Keyboard.KEY_RETURN);
    if(!canCraft() || !Display.isActive())repeater.cancel();
    else if(repeater.repeat(held,recipeKey(),ticks))sendCraft(1);
  }
  @Override protected void keyTyped(char c,int key)throws IOException {
    if(key==Keyboard.KEY_RETURN)craft(1);
    else if(key==Keyboard.KEY_O)addIngredient();
    else if(key==Keyboard.KEY_X)removeIngredient();
    else if(key==Keyboard.KEY_LEFT || key==Keyboard.KEY_RIGHT)selectColumn(selected+(key==Keyboard.KEY_LEFT?-1:1));
    else if(key==Keyboard.KEY_UP || key==Keyboard.KEY_DOWN)edit(key==Keyboard.KEY_UP?-1:1);
    else if(key==Keyboard.KEY_TAB)changeMode(mode+(isShiftKeyDown()?-1:1));
    else if(key==Keyboard.KEY_LBRACKET || key==Keyboard.KEY_RBRACKET) {
      int delta=key==Keyboard.KEY_LBRACKET?-1:1;if(isShiftKeyDown())changeType(type+delta);else changeMode(mode+delta);
    } else super.keyTyped(c,key);
  }
  @Override public boolean controllerPointer(){return false;}
  @Override public boolean controllerDirection(int direction) {
    if(direction==PadState.LEFT || direction==PadState.RIGHT)selectColumn(selected+(direction==PadState.LEFT?-1:1));
    else edit(direction==PadState.UP?-1:1);return true;
  }
  @Override public boolean controllerButton(int button) {
    if(button==PadState.L2 || button==PadState.R2){changeType(type+(button==PadState.L2?-1:1));return true;}
    if(button==PadState.L1 || button==PadState.R1){changeMode(mode+(button==PadState.L1?-1:1));return true;}
    if(button==PadState.CROSS){craft(1);return true;}
    if(button==PadState.TRIANGLE){addIngredient();return true;}
    if(button==PadState.SQUARE){removeIngredient();return true;}
    return false;
  }
  @Override public void onGuiClosed(){repeater.cancel();super.onGuiClosed();}
}
