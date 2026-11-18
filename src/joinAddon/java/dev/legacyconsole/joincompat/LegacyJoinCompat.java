package dev.legacyconsole.joincompat;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.*;
import net.minecraftforge.fmllegacy.network.FMLNetworkConstants;
@Mod("legacyjoincompat")
public final class LegacyJoinCompat {
 public LegacyJoinCompat(){ModLoadingContext.get().registerExtensionPoint(IExtensionPoint.DisplayTest.class,()->new IExtensionPoint.DisplayTest(()->FMLNetworkConstants.IGNORESERVERONLY,(version,isServer)->true));}
}
