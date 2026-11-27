package dev.legacyconsole.client.ui;

/** Native PS4 information rules, separated from the Minecraft rendering adapter. */
public final class Ps4InformationLayout {
  private Ps4InformationLayout() {}
  public static final int SELECTED_TICKS=60, FADE_TICKS=20, MAX_SELECTED_LINES=5;
  public record Hotbar(float x,float y,float scale,float width,float height) {}

  /** 0x81dfa0: stack upward from Effect1; compress after seven normal bars. */
  public static float effectY(int index,int count,float first,float second,float seventh){
    first=(int)first;second=(int)second;seventh=(int)seventh;
    float step=count>7?Math.round((first-seventh)/(count-1)):first-second;
    return first-index*step;
  }
  public static float effectAlpha(int ticks){
    if(ticks>=200)return 1;
    return net.minecraft.util.Mth.clamp(ticks/10f/5f*.5f,0,.5f)+
        net.minecraft.util.Mth.cos(ticks*(float)Math.PI/5f)*net.minecraft.util.Mth.clamp((10-ticks/20)/10f*.25f,0,.25f);
  }
  /** Java viewport adapter: keep the native text below the crosshair and above
   * the HUD, including large HUDs, short windows and tall enchantment lists. */
  public static float crosshairClearance(int displayHeight,int size){
    int index=Math.max(0,Math.min(2,size));boolean hd=displayHeight>=1080;
    return (hd?new float[]{44,68,102}:new float[]{30,45,68})[index]/(hd?6f:4f)+16*Ps4UiScale.factor(index);
  }
  public static Hotbar hotbar(int width,int height,float bottom,float desiredScale,int textWidth,int lines,float clearance){
    float safe=clearance+3*Math.min(width/1920f,height/1080f),top=height/2f+safe;
    if(bottom<=top){bottom=Math.min(bottom,height/2f-safe);top=height*.05f;}
    float textHeight=Math.max(1,lines)*10;
    float scale=Math.min(desiredScale,Math.min((bottom-top)/textHeight,(width*.9f)/Math.max(1,textWidth)));
    scale=Math.max(.01f,scale);
    float w=textWidth*scale,h=textHeight*scale;
    return new Hotbar((width-w)/2,bottom-h,scale,w,h);
  }
  public static String duration(int ticks){
    int seconds=Math.max(0,ticks/20);
    return Math.min(99,seconds/60)+":"+(seconds%60<10?"0":"")+seconds%60;
  }
}
