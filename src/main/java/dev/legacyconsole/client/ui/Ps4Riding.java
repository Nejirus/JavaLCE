package dev.legacyconsole.client.ui;

import com.google.gson.Gson;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.animal.horse.AbstractHorse;

/** Original HorseInventoryMenu1080 and skinHDHud horse widgets. */
public final class Ps4Riding {
  private Ps4Riding() {}
  static final class Widget {float[] matrix;List<Ps4UiArt.Node> children,slots;}
  static final class Portrait {float aspect,foot_divisor,scale,mouse_y;}
  static final class Data {
    Ps4UiArt.Scene scene;
    Map<String,Ps4UiArt.Sprite> sprites;
    Map<String,Widget> hud;
    Portrait portrait;
  }
  static final Data DATA;
  static {
    try(var reader=new InputStreamReader(Ps4Riding.class.getResourceAsStream(
        "/assets/legacyconsole/ps4_riding.json"),StandardCharsets.UTF_8)){
      DATA=new Gson().fromJson(reader,Data.class);
    }catch(Exception e){throw new ExceptionInInitializerError(e);}
  }
  private static void image(String name,float x,float y,float w,float h){
    ConsoleTheme.spriteAt("ps4/riding/"+DATA.sprites.get(name).texture,x,y,w,h);
  }
  /** The existing HUD uses half the original 1080p FUI canvas units. */
  public static void health(AbstractHorse horse,float x,float y,boolean flash){
    var widget=DATA.hud.get("FJ_HorseHealthBar");
    int max=Mth.clamp(Mth.ceil(horse.getMaxHealth()/2),0,widget.slots.size());
    int health=Mth.ceil(horse.getHealth());
    for(var node:widget.slots){
      int i=Integer.parseInt(node.name.substring("FJ_Health_".length()));
      if(i>=max)continue;
      float px=x+(widget.matrix[4]+node.matrix[4])*.5f;
      float py=y+(widget.matrix[5]+node.matrix[5])*.5f;
      image("Health_Background"+(flash?"_Flash":""),px,py,13.5f,13.5f);
      String fill=health>=(i+1)*2?"Full":health==i*2+1?"Half":null;
      if(fill!=null)image("HorseHealth_"+fill+(flash?"_Flash":""),px,py,13.5f,13.5f);
    }
  }
  public static void jump(float amount,float x,float y){
    var widget=DATA.hud.get("HorseJumpBar");
    for(var node:widget.children){
      var sprite=DATA.sprites.get(node.symbol);var m=node.matrix;
      float px=x+(widget.matrix[4]+m[4])*.5f,py=y+(widget.matrix[5]+m[5])*.5f;
      float w=sprite.size[0]*m[0]*.5f,h=sprite.size[1]*m[3]*.5f;
      if(node.symbol.equals("HorseJump_bar_empty"))image(node.symbol,px,py,w,h);
      else ConsoleTheme.croppedSprite("ps4/riding/"+sprite.texture,px,py,w,h,0,0,Mth.clamp(amount,0,1),1);
    }
  }
}
