package dev.legacyconsole.client.ui;

import dev.legacyconsole.port.GuiButton;
import java.util.List;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.protocol.game.ServerboundSetBeaconPacket;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.inventory.BeaconMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/** UIScene_BeaconMenu: native tiers, selection transitions and FUI button states. */
public final class Ps4BeaconScreen extends Ps4ContainerScreen {
  private final BeaconMenu beacon;
  // 0x0005a320 populates 0x01409c90 from the registered effect objects, in this order.
  private static final MobEffect[] PRIMARY={MobEffects.MOVEMENT_SPEED,MobEffects.DIG_SPEED,
      MobEffects.DAMAGE_RESISTANCE,MobEffects.JUMP,MobEffects.DAMAGE_BOOST};
  private static final String[] ICONS={"Speed","Haste","Resistance","Jump_Boost","Strength","Regeneration"};
  // UIScene_BeaconMenu::_customDrawSlotControl, 0x00a0ef50, cases 0..3.
  // Java 1.17 extension requested by the user; the four original PS4 icons retain their order.
  private static final Item[] PAYMENT={Items.NETHERITE_INGOT,Items.EMERALD,Items.DIAMOND,Items.GOLD_INGOT,Items.IRON_INGOT};
  private MobEffect primary,secondary;
  private int initialPrimary=-1,initialSecondary=-1;
  private boolean initialized;
  private boolean edited;

  public Ps4BeaconScreen(AbstractContainerScreen<?> original) {
    super(original,"BeaconMenu");beacon=(BeaconMenu)inventorySlots;
  }
  @Override protected void layoutControls() {
    place(0,"ActivatorSlot");
    for(int i=0;i<5;i++)buttonList.add(new EffectButton(i,"Primary_Slot_0"+(i+1)));
    buttonList.add(new EffectButton(5,"Secondary_Slot_01"));
    buttonList.add(new EffectButton(6,"Secondary_Slot_02"));
    buttonList.add(new EffectButton(7,"ConfirmButton"));
    navigationSlot(0x3a,0);
    for(var button:buttonList)navigationButton(0x3b+button.id,((EffectButton)button).bounds);
  }
  @Override protected boolean secondaryVisible(){return primary!=null;}
  private static int id(MobEffect effect){return effect==null?0:MobEffect.getId(effect);}
  @Override public void updateScreen() {
    int serverPrimary=beacon.getPrimaryEffect()==null?-1:id(beacon.getPrimaryEffect());
    int serverSecondary=beacon.getSecondaryEffect()==null?-1:id(beacon.getSecondaryEffect());
    if(!initialized||!edited&&(initialPrimary!=serverPrimary||initialSecondary!=serverSecondary)) {
      initialized=true;primary=beacon.getPrimaryEffect();secondary=beacon.getSecondaryEffect();
      initialPrimary=primary==null?-1:id(primary);initialSecondary=secondary==null?-1:id(secondary);
    }
    for(GuiButton b:buttonList) {
      if(b.id<5)b.enabled=beacon.getLevels()>(b.id<2?0:b.id<4?1:2);
      else if(b.id<7){b.enabled=beacon.getLevels()>3;b.visible=b.id!=6||primary!=null;}
      else b.enabled=beacon.hasPayment()&&(initialPrimary!=(primary==null?-1:id(primary))||
          initialSecondary!=(secondary==null?-1:id(secondary)));
    }
  }
  @Override protected void actionPerformed(GuiButton button) {
    updateScreen();if(!button.enabled)return;
    edited=true;
    if(button.id<5) {
      // 0x00a0f9d0: an active level-II secondary follows a newly selected primary.
      boolean follows=secondary==primary&&primary!=null;
      primary=PRIMARY[button.id];if(follows)secondary=primary;
    } else if(button.id==5)secondary=MobEffects.REGENERATION;
    else if(button.id==6)secondary=primary;
    else {
      mc.player.connection.send(new ServerboundSetBeaconPacket(id(primary),id(secondary)));
      mc.player.closeContainer();
    }
  }
  @Override protected void drawGuiContainerBackgroundLayer(float partial,int mx,int my) {
    updateScreen();art.backgrounds();
    art.insetLabel(Ps4UiArt.text("b3c82373"),"PrimaryPowerLabel","FJ_SquareRecessedPanel");
    art.insetLabel(Ps4UiArt.text("7b8c1d5d"),"SecondaryPowerLabel","FJ_SquareRecessedPanel");
    for(int i=0;i<PAYMENT.length;i++)art.item(new ItemStack(PAYMENT[i]),art.cell("ActivatorList",i,0,PAYMENT.length,1));
    drawHolders(mx,my);
  }
  @Override public void drawScreen(int mx,int my,float partial) {
    super.drawScreen(mx,my,partial);
    for(GuiButton b:buttonList)if(b.visible&&((EffectButton)b).bounds.contains(mx,my)) {
      MobEffect effect=b.id<5?PRIMARY[b.id]:b.id==5?MobEffects.REGENERATION:primary;
      String text=b.id==7?I18n.get("gui.done"):effect==null?"":I18n.get(effect.getDescriptionId())+
          (b.id==6?" II":"");
      if(!text.isEmpty())drawHoveringText(List.of(text),mx,my);
    }
  }
  private final class EffectButton extends GuiButton {
    final Ps4UiArt.Rect bounds;
    EffectButton(int id,String control){this(id,art.control(control,66,66));}
    private EffectButton(int id,Ps4UiArt.Rect r){super(id,(int)r.x(),(int)r.y(),(int)Math.ceil(r.w()),
        (int)Math.ceil(r.h()),"");bounds=r;}
    @Override public void drawButton(net.minecraft.client.Minecraft game,int mx,int my){
      if(!visible)return;
      hovered=bounds.contains(mx,my);
      boolean selected=id<5?primary==PRIMARY[id]:id==5?secondary==MobEffects.REGENERATION:
          id==6&&primary!=null&&secondary==primary;
      int frame=Ps4MenuLogic.beaconButtonFrame(enabled,selected,hovered);
      String[] holders={"IconHolderInactive_1x1border","IconHolder_1x1border_Off",
          "IconHolderHighlight_1x1border","IconHolder_1x1border"};
      art.image(holders[frame],bounds);
      if(id==7)art.image("Beacon_Button_Tick.png",new Ps4UiArt.Rect(bounds.x()+5*art.scale,
          bounds.y()+5*art.scale,56*art.scale,56*art.scale));
      else {
        int icon=id<6?id:0;
        if(id==6)for(int i=0;i<5;i++)if(primary==PRIMARY[i])icon=i;
        art.image("Potion_Effect_Icon_"+ICONS[icon]+"_Small",new Ps4UiArt.Rect(
            bounds.x()+6*art.scale,bounds.y()+6*art.scale,54*art.scale,54*art.scale));
      }
    }
  }
}
