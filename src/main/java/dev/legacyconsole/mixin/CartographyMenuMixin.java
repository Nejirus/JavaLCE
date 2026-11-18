package dev.legacyconsole.mixin;
import org.spongepowered.asm.mixin.*;import org.spongepowered.asm.mixin.injection.*;import org.spongepowered.asm.mixin.injection.callback.*;
import net.minecraft.world.inventory.*;import net.minecraft.world.Container;import net.minecraft.world.item.*;import net.minecraft.network.chat.TextComponent;import net.minecraft.SharedConstants;
/** PS4 cartography naming adapted to Java's result container and map NBT operations. */
@Mixin(CartographyTableMenu.class)
public abstract class CartographyMenuMixin implements dev.legacyconsole.port.RenameMapMenu {
 @Shadow @Final private ResultContainer resultContainer;
 @Shadow @Final private ContainerLevelAccess access;
 @Unique private String legacy$mapName;
 @Unique private boolean legacy$updating;
 public void legacy$setMapName(String value){String clean=value==null?null:SharedConstants.filterText(value);legacy$mapName=clean==null||clean.isBlank()||clean.length()>dev.legacyconsole.client.ui.Ps4MenuLogic.MAP_NAME_LIMIT?null:clean;((CartographyTableMenu)(Object)this).slotsChanged(resultContainer);}
 @Inject(method="slotsChanged",at=@At("HEAD"),cancellable=true)private void legacy$guard(Container c,CallbackInfo ci){if(legacy$updating)ci.cancel();}
 @Inject(method="slotsChanged",at=@At("RETURN"))private void legacy$rename(Container c,CallbackInfo ci){
  if(legacy$updating)return;var menu=(CartographyTableMenu)(Object)this;
  access.execute((level,pos)->{ItemStack input=menu.getSlot(0).getItem(),material=menu.getSlot(1).getItem();var data=MapItem.getSavedData(input,level);if(!input.is(Items.MAP)&&data==null)return;ItemStack old=resultContainer.getItem(2),result=old.copy();
   if(old.isEmpty()&&(input.is(Items.FILLED_MAP)||input.is(Items.MAP))&&material.isEmpty()){result=input.copy();result.setCount(1);}
   if(result.isEmpty())return;if(legacy$mapName!=null)result.setHoverName(new TextComponent(legacy$mapName));else if(result.hasCustomHoverName())result.resetHoverName();
   if(!ItemStack.matches(old,result)){legacy$updating=true;try{resultContainer.setItem(2,result);menu.broadcastChanges();}finally{legacy$updating=false;}}
  });
 }
}
