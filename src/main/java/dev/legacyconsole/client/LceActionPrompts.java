package dev.legacyconsole.client;
import com.google.gson.*;import java.io.*;import java.nio.charset.StandardCharsets;import java.util.*;
import net.minecraft.client.Minecraft;import net.minecraft.core.BlockPos;import net.minecraft.world.entity.*;import net.minecraft.world.entity.player.Player;import net.minecraft.world.entity.animal.*;import net.minecraft.world.entity.npc.*;
import net.minecraft.world.item.*;import net.minecraft.world.level.*;import net.minecraft.world.level.block.*;import net.minecraft.world.phys.*;import net.minecraft.tags.FluidTags;
import dev.legacyconsole.client.input.ConsoleVehicle;
/** Read-only translation of LCE GetActionTooltips, extended with the new native workstations. */
public final class LceActionPrompts {
 private static final Map<String,String> LABELS;
 static{try(Reader reader=new InputStreamReader(LceActionPrompts.class.getResourceAsStream("/assets/legacyconsole/action_prompts.json"),StandardCharsets.UTF_8)){LABELS=new Gson().fromJson(reader,new com.google.gson.reflect.TypeToken<Map<String,String>>(){}.getType());}catch(IOException e){throw new ExceptionInInitializerError(e);}}
 public static String source(String key){return LABELS.get(key);}public static String label(String name){String result=source("IDS_TOOLTIPS_"+name);if(result==null)throw new IllegalArgumentException("Missing LCE tooltip "+name);return result;}
 private static void add(List<String> out,String button,String name){out.add(button);out.add(label(name));}
 public static String[] actions(Minecraft mc){Player p=mc.player;if(p==null||mc.level==null)return new String[0];List<String> out=new ArrayList<>();if(p.isEyeInFluid(FluidTags.WATER))add(out,"cross","SWIMUP");out.add("square");out.add(mc.gameMode.hasInfiniteItems()?label("CREATIVE"):"Crafting");out.add("triangle");out.add("Inventory");String use=null,attack=null;
  if(!p.isSpectator()){
   use=heldUse(p,p.getMainHandItem());if(use==null)use=heldUse(p,p.getOffhandItem());
   if(mc.hitResult instanceof BlockHitResult hit){BlockPos pos=hit.getBlockPos();var state=mc.level.getBlockState(pos);Block block=state.getBlock();if(!state.isAir()&&state.getFluidState().isEmpty()&&state.getDestroySpeed(mc.level,pos)>=0&&(p.getAbilities().mayBuild||p.getMainHandItem().hasAdventureModeBreakTagForBlock(mc.level.getTagManager(),new net.minecraft.world.level.block.state.pattern.BlockInWorld(mc.level,pos,false)))&&!(p.getAbilities().instabuild&&p.getMainHandItem().getItem() instanceof SwordItem))attack=label("MINE");
    if(p.getMainHandItem().getItem() instanceof BlockItem||p.getOffhandItem().getItem() instanceof BlockItem)use=label("PLACE");
    String special=blockUse(block,p.getAbilities().instabuild,p.getFoodData().needsFood(),block instanceof JukeboxBlock&&state.getValue(JukeboxBlock.HAS_RECORD));if(special!=null)use=special;
    if(shovelUse(state,hit.getDirection(),p.getMainHandItem())||shovelUse(state,hit.getDirection(),p.getOffhandItem()))use=label("USE");
    if(block instanceof NoteBlock&&!p.getAbilities().instabuild)attack=label("PLAY");if(block instanceof CakeBlock&&!p.getAbilities().instabuild&&p.getFoodData().needsFood())attack=label("EAT");
   }else if(mc.hitResult instanceof EntityHitResult hit){Entity target=hit.getEntity();if(target.isAttackable())attack=label(ConsoleVehicle.isVehicle(target)?"MINE":"HIT");String special=entityUse(p,target,p.getMainHandItem());if(special==null)special=entityUse(p,target,p.getOffhandItem());if(special!=null)use=special;}
   if(ConsoleVehicle.isVehicle(p.getVehicle()))use=label("EXIT");
  }else if(mc.hitResult instanceof BlockHitResult hit){Block block=mc.level.getBlockState(hit.getBlockPos()).getBlock();if(block instanceof ChestBlock||block instanceof EnderChestBlock||block instanceof ShulkerBoxBlock||block instanceof BarrelBlock)use=label("OPEN");}
  if(use!=null){out.add("left_trigger");out.add(use);}if(attack!=null){out.add("right_trigger");out.add(attack);}return out.toArray(new String[0]);
 }
 public static String blockUse(Block block,boolean creative,boolean hungry,boolean record){
  if(block instanceof ChestBlock||block instanceof EnderChestBlock||block instanceof ShulkerBoxBlock||block instanceof BarrelBlock)return label("OPEN");
  if(block instanceof DoorBlock||block instanceof TrapDoorBlock||block instanceof FenceGateBlock)return label("OPEN");
  if(block instanceof BedBlock)return label("SLEEP");if(block instanceof CakeBlock)return !creative&&hungry?label("EAT"):null;
  if(block instanceof JukeboxBlock)return record?label("EJECT"):null;if(block instanceof NoteBlock)return label("CHANGEPITCH");
  if(block instanceof CraftingTableBlock||block instanceof AbstractFurnaceBlock||block instanceof EnchantmentTableBlock||block instanceof AnvilBlock||block instanceof BrewingStandBlock||block instanceof BeaconBlock||block instanceof HopperBlock||block instanceof DispenserBlock||block instanceof LoomBlock||block instanceof CartographyTableBlock||block instanceof StonecutterBlock||block instanceof GrindstoneBlock||block instanceof SmithingTableBlock||block instanceof LecternBlock||block instanceof LeverBlock||block instanceof ButtonBlock||block instanceof RepeaterBlock||block instanceof ComparatorBlock||block instanceof SignBlock)return label("USE");return null;
 }
 /** Shovel use-on can extinguish a lit campfire except through its bottom face. */
 public static boolean shovelUse(net.minecraft.world.level.block.state.BlockState state,net.minecraft.core.Direction face,ItemStack held){
  return face!=net.minecraft.core.Direction.DOWN&&held.getItem() instanceof ShovelItem&&state.getBlock() instanceof CampfireBlock&&state.getValue(CampfireBlock.LIT);
 }
 /** Read-only predicates used by 1.17.1 FlintAndSteelItem.useOn and TntBlock.use.
  * Never invoke useOn to probe availability: it changes blocks and damages tools. */
 private static boolean flintAndSteelUse(Player player,ItemStack held){
  var mc=Minecraft.getInstance();if(mc.level==null||!(mc.hitResult instanceof BlockHitResult hit)||hit.getType()!=HitResult.Type.BLOCK)return false;
  var pos=hit.getBlockPos();var state=mc.level.getBlockState(pos);
  if(state.getBlock() instanceof TntBlock&&!player.isSecondaryUseActive())return true;
  if(!player.getAbilities().mayBuild&&!held.hasAdventureModePlaceTagForBlock(mc.level.getTagManager(),new net.minecraft.world.level.block.state.pattern.BlockInWorld(mc.level,pos,false)))return false;
  return CampfireBlock.canLight(state)||CandleBlock.canLight(state)||CandleCakeBlock.canLight(state)||BaseFireBlock.canBePlacedAt(mc.level,pos.relative(hit.getDirection()),player.getDirection());
 }
 private static String heldUse(Player p,ItemStack s){if(s.isEmpty())return null;Item item=s.getItem();if(item instanceof BowItem||item instanceof CrossbowItem)return label(p.isUsingItem()?"RELEASE_BOW":"DRAW_BOW");if(item instanceof ShieldItem)return ClientConfig.crouchShield?null:label("BLOCK");if(item instanceof FishingRodItem)return label("USE");if(s.getUseAnimation()==UseAnim.DRINK)return label("DRINK");if(item.isEdible())return p.canEat(s.getItem().getFoodProperties().canAlwaysEat())?label("EAT"):null;if(item instanceof SnowballItem||item instanceof EggItem||item instanceof EnderpearlItem||item instanceof ThrowablePotionItem||item instanceof ExperienceBottleItem)return label("THROW");if(item instanceof BlockItem)return label("PLACE");if(item instanceof BucketItem)return LceBucketPrompts.use(Minecraft.getInstance(),p,s);if(item instanceof WrittenBookItem||item instanceof WritableBookItem)return label("READ");if(item instanceof FlintAndSteelItem)return flintAndSteelUse(p,s)?label("USE"):null;if(item instanceof FireworkRocketItem)return label("USE");return null;}
 private static String entityUse(Player player,Entity target,ItemStack held){if(target instanceof net.minecraft.world.entity.monster.Creeper&&held.getItem() instanceof FlintAndSteelItem)return label("USE");if(ConsoleVehicle.isVehicle(target))return ConsoleVehicle.use(target,player.getVehicle(),held);if(target instanceof AbstractVillager)return label("TRADE");if(target instanceof Cow cow&&held.is(Items.BUCKET)&&!cow.isBaby())return label("COLLECT");if(target instanceof Sheep sheep&&held.getItem() instanceof ShearsItem&&!sheep.isSheared()&&!sheep.isBaby())return label("SHEAR");if(target instanceof Animal animal&&animal.isFood(held))return label(animal.isBaby()?"GROW":"FEED");if(target instanceof TamableAnimal tame&&tame.isOwnedBy(player))return label(tame.isInSittingPose()?"FOLLOWME":"SIT");if(target instanceof net.minecraft.world.entity.animal.horse.AbstractHorse horse&&horse.isTamed())return label("RIDE");if(target instanceof net.minecraft.world.entity.decoration.ItemFrame)return held.isEmpty()?null:label("USE");return null;}
}
