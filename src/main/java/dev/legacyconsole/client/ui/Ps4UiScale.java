package dev.legacyconsole.client.ui;

/** Java menu adapter for mcps4's 1/1.5/2 HUD frames and numbered 1..3 stops.
 * This setting is independent of HUD Size and the game's global GUI scale. */
public final class Ps4UiScale {
 private Ps4UiScale(){}
 public record Layout(float x,float y,float scale){}
 public static float factor(int size){return new float[]{2f/3,1,4f/3}[Math.max(0,Math.min(2,size))];}
 public static Layout scene(int width,int height,int size,float rootX,float rootY,float left,float top,float right,float bottom){
  float base=Math.min(width/1920f,height/1080f),scale=base*factor(size);
  float edgeX=Math.max(960-rootX-left,rootX+right-960),edgeY=Math.max(540-rootY-top,rootY+bottom-540);
  if(size>1&&edgeX>0&&edgeY>0)scale=Math.min(scale,Math.max(base,Math.min(width*.45f/edgeX,height*.45f/edgeY)));
  return new Layout((width-1920*scale)/2+rootX*scale,(height-1080*scale)/2+rootY*scale,scale);
 }
 public static Layout popup(int width,int height,int size,float contentHeight){
  float base=Math.min(width/1920f,height/1080f),scale=base*factor(size);
  float right=(width-1920*base)/2+1830*base,top=(height-1080*base)/2+75*base;
  scale=Math.min(scale,Math.min((width-180*base)/750,(height-top-75*base)/contentHeight));
  return new Layout(right-1830*scale,top-75*scale,scale);
 }
}
