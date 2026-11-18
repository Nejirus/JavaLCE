import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;
import net.minecraft.nbt.*;
import net.minecraft.util.datafix.*;
import net.minecraft.util.datafix.fixes.References;
import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.zip.*;

/** Use Minecraft's own data fixes, without generating any replacement terrain. */
public final class ConvertPs4Tutorial {
 public static void main(String[] args) throws Exception {
  SharedConstants.tryDetectVersion();Bootstrap.bootStrap();dev.legacyconsole.worldgen.LegacyWorldgen.register();
  Path input=Path.of(args[0]),output=Path.of(args[1]);
  try(var files=Files.walk(input)) {
   for(Path source:files.filter(Files::isRegularFile).sorted().toList()) {
    Path target=output.resolve(input.relativize(source));Files.createDirectories(target.getParent());
    if(source.toString().endsWith(".mca")){if(args.length<3||!args[2].equals("--metadata-only"))convertRegion(source,target);}
    else if(source.getFileName().toString().equals("level.dat")) {
     CompoundTag root=NbtIo.readCompressed(source.toFile());
     normalizeConsoleUUIDs(root);
     // The console save names this dimension "The End". Java 1.17 stores its
     // fight record directly in level data; let vanilla upgrade its UUID.
     CompoundTag savedEnd=root.getCompound("Data").getCompound("DimensionData").getCompound("The End");
     if(savedEnd.contains("DragonFight",10))root.getCompound("Data").put("DragonFight",savedEnd.getCompound("DragonFight").copy());
     CompoundTag fixed=NbtUtils.update(DataFixers.getDataFixer(),DataFixTypes.LEVEL,root.getCompound("Data"),0,2730);
     fixed.putInt("DataVersion",2730);fixed.putInt("version",19133);
     fixed.putString("LevelName","PS4 Tutorial");
     var registries=net.minecraft.core.RegistryAccess.builtin();var properties=new Properties();properties.setProperty("level-seed",Long.toString(fixed.getLong("RandomSeed")));
     var base=net.minecraft.world.level.levelgen.WorldGenSettings.create(registries,properties);
     var generator=dev.legacyconsole.worldgen.LegacyWorldgen.create(registries,base,dev.legacyconsole.worldgen.Ps4WorldSize.CLASSIC,false,false);
     fixed.put("WorldGenSettings",net.minecraft.world.level.levelgen.WorldGenSettings.CODEC.encodeStart(net.minecraft.resources.RegistryWriteOps.create(NbtOps.INSTANCE,registries),generator).getOrThrow(false,System.err::println));
     // Preserve the original tutorial save's HellScale=3. All other dimension
     // settings come from 1.17.1's actual Nether definition.
     com.google.gson.JsonObject nether=net.minecraft.world.level.dimension.DimensionType.DIRECT_CODEC.encodeStart(com.mojang.serialization.JsonOps.INSTANCE,base.dimensions().get(net.minecraft.world.level.dimension.LevelStem.NETHER).type()).getOrThrow(false,System.err::println).getAsJsonObject();
     nether.addProperty("coordinate_scale",fixed.getInt("HellScale"));
     Path pack=output.resolve("datapacks/ps4_tutorial");Files.createDirectories(pack.resolve("data/legacyconsole/dimension_type"));
     Files.writeString(pack.resolve("pack.mcmeta"),"{\"pack\":{\"pack_format\":7,\"description\":\"Original PS4 tutorial dimension scale\"}}\n");
     Files.writeString(pack.resolve("data/legacyconsole/dimension_type/tutorial_nether.json"),new com.google.gson.GsonBuilder().setPrettyPrinting().create().toJson(nether)+"\n");
     fixed.getCompound("WorldGenSettings").getCompound("dimensions").getCompound("minecraft:the_nether").putString("type","legacyconsole:tutorial_nether");
     CompoundTag packs=new CompoundTag();ListTag enabled=new ListTag();enabled.add(StringTag.valueOf("vanilla"));enabled.add(StringTag.valueOf("file/ps4_tutorial"));packs.put("Enabled",enabled);packs.put("Disabled",new ListTag());fixed.put("DataPacks",packs);
     CompoundTag version=new CompoundTag();version.putInt("Id",2730);version.putString("Name","1.17.1");version.putBoolean("Snapshot",false);fixed.put("Version",version);
     root.put("Data",fixed);NbtIo.writeCompressed(root,target.toFile());
    } else Files.copy(source,target,StandardCopyOption.REPLACE_EXISTING);
   }
  }
  System.out.println("Finished original PS4 tutorial schema conversion");
  System.exit(0);
 }
 private static void convertRegion(Path source,Path target) throws Exception {
  try(RandomAccessFile in=new RandomAccessFile(source.toFile(),"r");RandomAccessFile out=new RandomAccessFile(target.toFile(),"rw")) {
   out.setLength(0);out.write(new byte[8192]);int[] locations=new int[1024];for(int i=0;i<1024;i++)locations[i]=in.readInt();int count=0;
   for(int i=0;i<1024;i++) {
    if(locations[i]==0)continue;
    in.seek((locations[i]>>>8)*4096L);int length=in.readInt();int compression=in.readUnsignedByte();if(compression!=2)throw new IOException("Unexpected compression");byte[] compressed=new byte[length-1];in.readFully(compressed);
    CompoundTag original=NbtIo.read(new DataInputStream(new InflaterInputStream(new ByteArrayInputStream(compressed))));
    normalizeConsoleUUIDs(original);
    CompoundTag oldLevel=original.getCompound("Level");
    normalizeLegacyIds(oldLevel.getList("Entities",10),References.ENTITY);
    normalizeLegacyIds(oldLevel.getList("TileEntities",10),References.BLOCK_ENTITY);
    ListTag entities=updateRecords(oldLevel.getList("Entities",10),References.ENTITY);
    ListTag blockEntities=updateRecords(oldLevel.getList("TileEntities",10),References.BLOCK_ENTITY);
    // This save contains both old class IDs and namespaced 1.12 IDs. Upgrade
    // the old class records first, then use the 1.12 chunk/item schema for all
    // records. Starting the whole mixed chunk at version zero skips namespaced
    // block-entity inventories because they are unknown to the earliest schema.
    CompoundTag fixed=NbtUtils.update(DataFixers.getDataFixer(),DataFixTypes.CHUNK,original,1343,2730);
    // Console records contain fields unknown to the Java chunk schema. Run
    // each known record through its own reference so its inventory and entity
    // payload is upgraded as well. Keep block-dependent records removed by the
    // chunk fix (notably legacy flower pots) removed.
    fixed.getCompound("Level").put("Entities",entities);
    ListTag fixedTiles=fixed.getCompound("Level").getList("TileEntities",10);
    for(int tile=0;tile<fixedTiles.size();tile++)for(Tag value:blockEntities){CompoundTag candidate=(CompoundTag)value;CompoundTag current=fixedTiles.getCompound(tile);if(candidate.getInt("x")==current.getInt("x")&&candidate.getInt("y")==current.getInt("y")&&candidate.getInt("z")==current.getInt("z")){fixedTiles.set(tile,candidate);break;}}
    fixed.getCompound("Level").putString("Status","full");
    fixed.getCompound("Level").putBoolean("isLightOn",false);
    fixItemStacks(fixed);
    fixed.putInt("DataVersion",2730);
    ByteArrayOutputStream bytes=new ByteArrayOutputStream();try(DataOutputStream data=new DataOutputStream(new DeflaterOutputStream(bytes))) {NbtIo.write(fixed,data);}
    byte[] payload=bytes.toByteArray();int sectors=(payload.length+5+4095)/4096;long offset=out.length();out.seek(i*4L);out.writeInt(((int)(offset/4096)<<8)|sectors);out.seek(offset);out.writeInt(payload.length+1);out.writeByte(2);out.write(payload);out.write(new byte[sectors*4096-payload.length-5]);count++;
   }
   System.out.println(source.getFileName()+" "+count+" original chunks converted");
  }
 }
 private static void fixItemStacks(Tag node){
  if(node instanceof CompoundTag tag){
   for(String key:new ArrayList<>(tag.getAllKeys()))fixItemStacks(tag.get(key));
   if(tag.contains("id")&&tag.contains("Count",99)&&tag.contains("Damage",99)){
    int version=tag.contains("id",8)?1343:0;
    CompoundTag fixed=(CompoundTag)DataFixers.getDataFixer().update(References.ITEM_STACK,new com.mojang.serialization.Dynamic<Tag>(NbtOps.INSTANCE,tag.copy()),version,2730).getValue();
    for(String key:new ArrayList<>(tag.getAllKeys()))tag.remove(key);tag.merge(fixed);
   }
  }else if(node instanceof ListTag list)for(Tag child:list)fixItemStacks(child);
 }
 private static ListTag updateRecords(ListTag input,com.mojang.datafixers.DSL.TypeReference type){ListTag result=new ListTag();for(Tag item:input){Tag fixed=DataFixers.getDataFixer().update(type,new com.mojang.serialization.Dynamic<Tag>(NbtOps.INSTANCE,item.copy()),1343,2730).getValue();result.add(fixed);}return result;}
 private static void normalizeLegacyIds(ListTag list,com.mojang.datafixers.DSL.TypeReference type) {
  for(int index=0;index<list.size();index++) {
   CompoundTag tag=list.getCompound(index);
   if(!tag.getString("id").contains(":")) {
    Tag fixed=DataFixers.getDataFixer().update(type,new com.mojang.serialization.Dynamic<Tag>(NbtOps.INSTANCE,tag),0,1343).getValue();
    list.set(index,fixed);
   }
  }
 }
 private static void normalizeConsoleUUIDs(Tag node) {
  if(node instanceof CompoundTag tag) {
   if(tag.contains("UUID",8)) {
    String id=tag.getString("UUID");
    if(id.matches("ent[0-9a-fA-F]{32}")) {
     String hex=id.substring(3);
     tag.putString("UUID",hex.substring(0,8)+"-"+hex.substring(8,12)+"-"+hex.substring(12,16)+"-"+hex.substring(16,20)+"-"+hex.substring(20));
    }
   }
   for(String name:tag.getAllKeys())normalizeConsoleUUIDs(tag.get(name));
  } else if(node instanceof ListTag list)for(Tag child:list)normalizeConsoleUUIDs(child);
 }
}
