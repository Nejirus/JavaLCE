package dev.legacyconsole.port;
import java.util.*;import java.io.*;import java.awt.image.BufferedImage;import javax.imageio.ImageIO;
import net.minecraft.client.Minecraft;import net.minecraft.resources.ResourceLocation;import net.minecraft.server.packs.repository.*;
import net.minecraft.client.renderer.texture.*;
public final class PackRepositoryBridge {
 private static final Map<String,Entry> CACHE=new HashMap<>();private final PackRepository repository;
 public final DefaultPack rprDefaultResourcePack=new DefaultPack();
 private PackRepositoryBridge(){repository=Minecraft.getInstance().getResourcePackRepository();}
 public static PackRepositoryBridge get(){return new PackRepositoryBridge();}
 public void updateRepositoryEntriesAll(){repository.reload();for(Entry e:CACHE.values())if(e.icon!=null&&e.icon.getNamespace().equals("minecraft")&&e.icon.getPath().startsWith("dynamic/legacy_pack_icon"))Minecraft.getInstance().getTextureManager().release(e.icon);CACHE.clear();}
 private Entry entry(Pack pack){return CACHE.computeIfAbsent(pack.getId(),id->new Entry(pack));}
 private boolean userPack(Pack pack){return !pack.isRequired()&&!pack.getId().equals("vanilla")&&!pack.getId().startsWith("mod:");}
 public List<Entry> getRepositoryEntriesAll(){return repository.getAvailablePacks().stream().filter(this::userPack).map(this::entry).toList();}
 public List<Entry> getRepositoryEntries(){return repository.getSelectedPacks().stream().filter(this::userPack).map(this::entry).toList();}
 public void setRepositories(List<Entry> entries){List<String> ids=new ArrayList<>();for(Pack p:repository.getSelectedPacks())if(!userPack(p))ids.add(p.getId());for(Entry e:entries)ids.add(e.pack.getId());repository.setSelected(ids);}
 public static final class Entry {
  final Pack pack;private ResourceLocation icon;
  Entry(Pack pack){this.pack=pack;}
  public String getResourcePackName(){return pack.getId();}public String getTexturePackDescription(){return pack.getDescription().getString();}
  public int getPackFormat(){return pack.getCompatibility().isCompatible()?7:0;}
  public void bindTexturePackIcon(TextureManager textures){if(icon==null)try(var resources=pack.open();InputStream stream=resources.getRootResource("pack.png")){icon=textures.register("legacy_pack_icon",new DynamicTexture(com.mojang.blaze3d.platform.NativeImage.read(stream)));}catch(IOException e){icon=new ResourceLocation("textures/misc/unknown_pack.png");}PortGraphics.bind(icon);}
 }
 public final class DefaultPack {public BufferedImage getPackImage()throws IOException{Pack vanilla=repository.getPack("vanilla");if(vanilla==null)throw new IOException("Default resource pack unavailable");try(var pack=vanilla.open();InputStream stream=pack.getRootResource("pack.png")){return ImageIO.read(stream);}}}
}
