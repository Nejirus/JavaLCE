package dev.legacyconsole.port;
import java.io.IOException;import java.util.*;
import net.minecraft.client.gui.screens.Screen;import net.minecraft.client.gui.screens.worldselection.CreateWorldScreen;
import net.minecraft.core.*;import net.minecraft.world.*;import net.minecraft.world.level.*;import net.minecraft.world.level.levelgen.*;
import net.minecraft.client.resources.language.I18n;import net.minecraft.FileUtil;
public class GuiCreateWorld extends LegacyScreen {
 private final Screen parent;
 private final List<GuiButton> nativeModels=new ArrayList<>();
 private GuiTextField worldNameField,worldSeedField;
 private String worldName="New World",worldSeed="",gameMode="survival",saveDirName="New World";
 private boolean inMoreWorldOptionsDisplay,generateStructures=true,bonusChest,allowCheats;
 private int selectedIndex;
 protected dev.legacyconsole.worldgen.Ps4WorldSize legacySize=dev.legacyconsole.worldgen.Ps4WorldSize.LARGE;
 protected dev.legacyconsole.worldgen.Ps4BiomeScale legacyScale=dev.legacyconsole.worldgen.Ps4BiomeScale.SMALL;
 public String chunkProviderSettingsJson="";
 protected void legacySizeChanged(){recreated=null;}
 protected void setWorldSeed(String value){worldSeed=value;worldSeedField.setText(value);recreated=null;}
 private WorldGenSettings recreated;
 protected RegistryAccess.RegistryHolder registries=RegistryAccess.builtin();
 public GuiCreateWorld(Screen parent){this.parent=parent;}
 @Override public void initGui(){
  worldNameField=new GuiTextField(0,FontCompat.get(),0,0,200,20);worldNameField.setText(worldName);worldNameField.setMaxStringLength(64);
  worldSeedField=new GuiTextField(1,FontCompat.get(),0,0,200,20);worldSeedField.setText(worldSeed);worldSeedField.setMaxStringLength(64);
  buttonList.clear();
  buttonList.add(new GuiButton(0,0,0,"Create New World"));buttonList.add(new GuiButton(1,0,0,"Cancel"));
  buttonList.add(new GuiButton(2,0,0,"Game Mode: "+I18n.get("gameMode."+gameMode)));buttonList.add(new GuiButton(3,0,0,"More World Options..."));
  buttonList.add(new GuiButton(4,0,0,"Generate Structures: "+(generateStructures?"On":"Off")));
  buttonList.add(new GuiButton(5,0,0,"World Type: "+I18n.get(WorldPreset.WORLD_TYPES[selectedIndex].getTranslateName())));
  GuiButton cheats=new GuiButton(6,0,0,"Host Privileges: "+(allowCheats&&!gameMode.equals("hardcore")?"On":"Off"));cheats.enabled=!gameMode.equals("hardcore");buttonList.add(cheats);
  GuiButton bonus=new GuiButton(7,0,0,"Bonus Chest: "+(bonusChest?"On":"Off"));bonus.enabled=!gameMode.equals("hardcore");buttonList.add(bonus);
  GuiButton customize=new GuiButton(8,0,0,"Customize");customize.visible=selectedIndex==1;buttonList.add(customize);nativeModels.clear();nativeModels.addAll(buttonList);
 }
 private void updateModels(){for(GuiButton b:nativeModels){if(b.id==2)b.displayString="Game Mode: "+I18n.get("gameMode."+gameMode);if(b.id==4)b.displayString="Generate Structures: "+(generateStructures?"On":"Off");if(b.id==5)b.displayString="World Type: "+I18n.get(WorldPreset.WORLD_TYPES[selectedIndex].getTranslateName());if(b.id==6){b.displayString="Host Privileges: "+(allowCheats&&!gameMode.equals("hardcore")?"On":"Off");b.enabled=!gameMode.equals("hardcore");}if(b.id==7){b.displayString="Bonus Chest: "+(bonusChest?"On":"Off");b.enabled=!gameMode.equals("hardcore");}if(b.id==8)b.visible=selectedIndex==1;}}
 @Override protected void actionPerformed(GuiButton b)throws IOException{
  if(b.id==1){mc.setScreen(parent);return;}
  if(b.id==2){gameMode=gameMode.equals("survival")?"hardcore":gameMode.equals("hardcore")?"creative":"survival";allowCheats=gameMode.equals("creative");}
  if(b.id==3)inMoreWorldOptionsDisplay=!inMoreWorldOptionsDisplay;
  if(b.id==4){generateStructures=!generateStructures;recreated=null;}if(b.id==5){selectedIndex=(selectedIndex+1)%WorldPreset.WORLD_TYPES.length;recreated=null;}
  if(b.id==6&&!gameMode.equals("hardcore"))allowCheats=!allowCheats;if(b.id==7&&!gameMode.equals("hardcore"))bonusChest=!bonusChest;
  updateModels();if(b.id==0)create();
 }
 private void calcSaveDirName(){try{saveDirName=FileUtil.findAvailableName(mc.getLevelSource().getBaseDir(),worldNameField.getText().trim().isEmpty()?"World":worldNameField.getText().trim(),"");}catch(IOException e){throw new IllegalStateException("Cannot choose save folder",e);}}
 public void recreateFromExistingWorld(WorldInfo info){worldName="Copy of "+info.getWorldName();worldSeed=Long.toString(info.getSeed());gameMode=info.isHardcoreModeEnabled()?"hardcore":info.getGameType()==GameType.CREATIVE?"creative":"survival";allowCheats=info.areCommandsAllowed();dev.legacyconsole.client.ui.ConsoleCreateWorld.selectedDifficulty=info.getDifficulty();var ops=net.minecraft.resources.RegistryReadOps.createAndLoad(net.minecraft.nbt.NbtOps.INSTANCE,mc.getResourceManager(),registries);recreated=WorldGenSettings.CODEC.parse(ops,info.generatorTag()).getOrThrow(false,message->{throw new IllegalStateException("Cannot recreate generator: "+message);});if(recreated.overworld() instanceof dev.legacyconsole.worldgen.LegacyChunkGenerator legacy){selectedIndex=WorldPreset.LEGACY.getWorldTypeID();legacySize=legacy.size();legacyScale=legacy.scale();generateStructures=legacy.structures();}else if(recreated.isFlatWorld())selectedIndex=1;}
 private void create(){
  calcSaveDirName();boolean hardcore=gameMode.equals("hardcore");GameType mode=gameMode.equals("creative")?GameType.CREATIVE:GameType.SURVIVAL;
  long seed=new Random().nextLong();String text=worldSeedField.getText();if(!text.isEmpty())try{long parsed=Long.parseLong(text);seed=parsed;}catch(NumberFormatException e){seed=text.hashCode();}
  java.util.Properties props=new java.util.Properties();props.setProperty("level-seed",Long.toString(seed));props.setProperty("generate-structures",Boolean.toString(generateStructures));props.setProperty("level-type",new String[]{"default","default","largebiomes","amplified","default"}[selectedIndex]);
  WorldGenSettings gen=WorldGenSettings.create(registries,props);
  if(selectedIndex==1){var biomes=registries.ownedRegistryOrThrow(Registry.BIOME_REGISTRY);var flat=dev.legacyconsole.client.ui.FlatWorldModel.nativeSettings(chunkProviderSettingsJson,biomes);gen=new WorldGenSettings(seed,generateStructures,bonusChest&&!hardcore,WorldGenSettings.withOverworld(registries.ownedRegistryOrThrow(Registry.DIMENSION_TYPE_REGISTRY),gen.dimensions(),new FlatLevelSource(flat)));}
  else if(selectedIndex==WorldPreset.LEGACY.getWorldTypeID())gen=dev.legacyconsole.worldgen.LegacyWorldgen.create(registries,gen,legacySize,generateStructures,bonusChest&&!hardcore,legacyScale);
  else if(bonusChest&&!hardcore)gen=gen.withBonusChest();
  gen=gen.withSeed(hardcore,java.util.OptionalLong.of(seed));
  if(recreated!=null)gen=recreated.withSeed(hardcore,java.util.OptionalLong.of(seed));
  GameRules rules=new GameRules();boolean fire=!(this instanceof dev.legacyconsole.client.ui.ConsoleCreateWorld c)||c.fireSpreads();rules.getRule(GameRules.RULE_DOFIRETICK).set(fire,null);
  Difficulty diff=hardcore?Difficulty.HARD:dev.legacyconsole.client.ui.ConsoleCreateWorld.selectedDifficulty;
  LevelSettings settings=new LevelSettings(worldNameField.getText().trim(),mode,hardcore,diff,!hardcore&&allowCheats,rules,DataPackConfig.DEFAULT);
  mc.createLevel(saveDirName,settings,registries,gen);
 }
}
