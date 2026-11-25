package dev.legacyconsole.crafting;
import java.util.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MerchantMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.*;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.protocol.game.ClientboundMerchantOffersPacket;
import net.minecraft.stats.Stats;
import net.minecraft.world.entity.ExperienceOrb;
public final class NativeTrading {
 private NativeTrading(){}
 public static Merchant merchant(MerchantMenu menu){return net.minecraftforge.fml.util.ObfuscationReflectionHelper.getPrivateValue(MerchantMenu.class,menu,"f_40027_");}
 // PS4 0xad5200 tests the two costs; output space does not disable an offer.
 public static boolean canTrade(Player p,MerchantOffer offer){if(offer.isOutOfStock())return false;List<ItemStack> inv=NativeCrafting.copies(p.getInventory().items);return consume(inv,offer.getCostA())&&consume(inv,offer.getCostB());}
 public static boolean[] costWarnings(Player p,MerchantOffer offer){List<ItemStack> inv=NativeCrafting.copies(p.getInventory().items);return new boolean[]{!consume(inv,offer.getCostA()),!consume(inv,offer.getCostB())};}
 public static boolean trade(Player p,MerchantMenu menu,int index){
  if(!menu.stillValid(p)||!p.isAlive()||p.isSpectator()||!menu.getCarried().isEmpty())return false;
  Merchant trader=merchant(menu);MerchantOffers offers=trader.getOffers();if(index<0||index>=offers.size())return false;
  MerchantOffer offer=offers.get(index);if(offer.isOutOfStock())return false;
  List<ItemStack> inv=NativeCrafting.copies(p.getInventory().items);
  for(int i=0;i<2;i++)if(!NativeCrafting.insert(inv,menu.getSlot(i).getItem().copy()))return false;
  if(!consume(inv,offer.getCostA())||!consume(inv,offer.getCostB()))return false;
  // PS4 0xad3ce0 inserts what fits and drops the remainder, retaining result NBT.
  ItemStack remainder=offer.assemble();NativeCrafting.insert(inv,remainder);
  for(int i=0;i<inv.size();i++)p.getInventory().items.set(i,inv.get(i));
  menu.getSlot(0).set(ItemStack.EMPTY);menu.getSlot(1).set(ItemStack.EMPTY);
  if(!remainder.isEmpty())p.drop(remainder,false);
  trader.notifyTrade(offer);trader.overrideXp(trader.getVillagerXp()+offer.getXp());
  p.awardStat(Stats.TRADED_WITH_VILLAGER);p.getInventory().setChanged();menu.broadcastChanges();
  // MerchantMenu's level is populated on the client by the offers packet. The
  // server menu retains its default, so read the actual villager for updates.
  int level=trader instanceof net.minecraft.world.entity.npc.Villager villager
      ?villager.getVillagerData().getLevel():menu.getTraderLevel();
  if(p instanceof ServerPlayer server)server.connection.send(new ClientboundMerchantOffersPacket(menu.containerId,trader.getOffers(),level,trader.getVillagerXp(),trader.showProgressBar(),trader.canRestock()));
  return true;
 }
 private static boolean consume(List<ItemStack> inv,ItemStack required){if(required.isEmpty())return true;int left=required.getCount();for(ItemStack s:inv)if(ItemStack.isSame(s,required)&&(!required.hasTag()||NbtUtils.compareNbt(required.getTag(),s.getTag(),false))){int take=Math.min(left,s.getCount());s.shrink(take);left-=take;if(left==0)return true;}return false;}
}
