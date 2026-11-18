package dev.legacyconsole.world;
import static org.junit.Assert.*;
import net.minecraft.nbt.*;
import org.junit.Test;
public class HostOptionsTest {
 @Test public void disabledPrivilegesRejectEveryActionEvenForTheHost(){
  for(int action:new int[]{-1,0,1,15,16,17,18,100,105,107,108,109,110,112,200,201})
   assertFalse(HostOptions.mayRequest(false,true,true,true,action));
 }
 @Test public void personalGrantsNeverAuthorizeGlobalOptionsOrOtherPlayers(){
  for(int action:new int[]{0,1,15,16,17,18,100,105,110,111,112})assertFalse(HostOptions.mayRequest(true,false,true,true,action));
  for(int action:new int[]{200,201}){assertTrue(HostOptions.mayRequest(true,false,true,true,action));assertFalse(HostOptions.mayRequest(true,false,true,false,action));}
  for(int action:new int[]{107,108,109}){
   assertTrue(HostOptions.mayRequest(true,false,true,true,action));
   assertFalse(HostOptions.mayRequest(true,false,false,true,action));
   assertFalse(HostOptions.mayRequest(true,false,true,false,action));
  }
 }
 @Test public void optionHistoryRoundTripsWithoutGrantingMissingPermissions(){
  CompoundTag input=new CompoundTag(),players=new CompoundTag(),p=new CompoundTag();p.putBoolean("build",false);p.putBoolean("allowFly",true);players.put("test",p);input.putBoolean("TNTExplodes",false);input.put("Players",players);
  var output=HostOptions.load(input).save(new CompoundTag());assertFalse(output.getBoolean("TNTExplodes"));assertEquals(players,output.getCompound("Players"));assertFalse(output.getCompound("Players").getCompound("test").getBoolean("moderator"));
 }
}
