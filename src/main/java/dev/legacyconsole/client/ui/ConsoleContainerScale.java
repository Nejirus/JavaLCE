package dev.legacyconsole.client.ui;

/** Uniform menu transform using the shared LCE/PS4 numbered size stops.
 * Slot coordinates stay in their original menu space; rendering and input
 * use this same transform. The footer and HUD remain in screen space. */
public record ConsoleContainerScale(float scale,float offsetX,float offsetY) {
 public static final ConsoleContainerScale IDENTITY=new ConsoleContainerScale(1,0,0);
 public static ConsoleContainerScale fit(int width,int height,float left,float top,float panelWidth,float panelHeight,float footerTop,int size){
  if(width<=0||height<=0||panelWidth<=0||panelHeight<=0)return IDENTITY;
  float safeX=width*.05f,safeY=height*.05f,bottom=Math.min(height-safeY,footerTop);
  float fit=Math.min((width-2*safeX)/panelWidth,(bottom-safeY)/panelHeight);
  float scale=Math.min(Ps4UiScale.factor(size),Math.max(.01f,fit));
  float cx=left+panelWidth/2,cy=top+panelHeight/2;
  float x=Math.max(safeX+panelWidth*scale/2,Math.min(width-safeX-panelWidth*scale/2,cx));
  float y=Math.max(safeY+panelHeight*scale/2,Math.min(bottom-panelHeight*scale/2,cy));
  return new ConsoleContainerScale(scale,x-cx*scale,y-cy*scale);
 }
 public float screenX(float x){return offsetX+x*scale;}
 public float screenY(float y){return offsetY+y*scale;}
 public double menuX(double x){return (x-offsetX)/scale;}
 public double menuY(double y){return (y-offsetY)/scale;}
 public float[] rectangle(float x,float y,float w,float h){return new float[]{screenX(x),screenY(y),w*scale,h*scale};}
}
