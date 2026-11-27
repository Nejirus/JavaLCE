package dev.legacyconsole.client.ui;

/** mcps4's console inventory layout and visible-section routing. */
public final class Ps4ContainerLogic {
  private Ps4ContainerLogic() {}
  /** FJ_InventoryMenu::setClassicCrafting, 0x81e150, disabled branch. */
  public static float equipmentOffset(float armorX,float armorWidth,float playerX,float playerWidth,float panelWidth) {
    int half=Math.round((playerWidth+(playerX-(armorWidth+armorX))+armorWidth)*.5f);
    return Math.round(panelWidth*.5f)-(half+(int)armorX);
  }
  private static Ps4MenuNavigation.Route r(int section,int dx,int dy){return new Ps4MenuNavigation.Route(section,dx,dy);}
  /** UIScene_InventoryMenu::handleSection, 0xa66550, console-crafting branch. */
  public static Ps4MenuNavigation.Route inventory(int section,int d,int x,int y,boolean split) {
    return switch(section) {
      case 10->d==1&&!split?r(11,0,0):d==2||d==1?r(x<5?12:13,0,0):r(10,0,0);
      case 11->d==2?r(10,0,0):d==1?r(x<5?12:13,0,0):r(11,0,0);
      case 12->d==1?r(10,0,0):d==2?r(split?10:11,0,0):d==3||d==4?r(13,0,0):r(12,0,0);
      case 13->d==1?r(10,-8,0):d==2?r(split?10:11,-8,0):d==3||d==4?r(12,0,3):r(13,0,0);
      default->r(section,0,0);
    };
  }
  /** UIScene_ContainerMenu::handleSection, 0xa17a60. */
  public static Ps4MenuNavigation.Route chest(int section,int d,boolean split) {
    return r(switch(section) {
      case 0->d==2?2:d==1?(split?2:1):0;
      case 1->d==2?0:d==1?2:1;
      case 2->d==1?0:d==2?(split?0:1):2;
      default->section;
    },0,0);
  }
  /** UIScene_HorseInventoryMenu::handleSection, 0xa47bd0. */
  public static Ps4MenuNavigation.Route horse(int section,int d,int x,int y,boolean split,
      boolean chest,boolean armor,boolean saddle,int columns){
    int to=section,dx=0,dy=0;
    switch(section){
      case 0x48:
        if(d==1){
          if(!split)to=0x49;
          else if(chest&&x>=4){to=0x4a;dx=4;}
          else to=armor?0x4b:saddle?0x4c:0x48;
        }else if(d==2){
          if(armor&&x>=4){to=0x4a;dx=4;}
          else to=saddle?0x4c:0x48;
        }
        break;
      case 0x49:
        if(d==2)to=0x48;
        else if(d==1){
          if(chest&&x>=4){to=0x4a;dx=4;}
          else to=armor?0x4b:saddle?0x4c:0x49;
        }
        break;
      case 0x4a:
        if(d==1||d==2){to=d==1||split?0x48:0x49;dx=-4;}
        else if(d==3&&x<0||d==4&&x>=columns){
          if(armor&&y==1)to=0x4b;
          else if(y==0)to=saddle?0x4c:0x4a;
        }
        break;
      case 0x4b:
        if(d==1)to=saddle?0x4c:0x4b;
        else if(d==2)to=chest?0x4a:split?0x48:0x49;
        else if((d==3||d==4)&&chest){to=0x4a;dx=x-(d==3?columns:0);dy=-1;}
        break;
      case 0x4c:
        if(d==1)to=0x48;
        else if(d==2)to=armor?0x4b:split?0x48:0x49;
        else if((d==3||d==4)&&chest){to=0x4a;dx=x-(d==3?columns:0);}
        break;
      default:break;
    }
    return r(to,dx,dy);
  }
  /** UIScene's player draw 0x9f6f30: original 1920/1080 aspect and 8/9 foot anchor. */
  public static float playerScale(float width){return width/(1920f/1080f);}
  public static float playerFoot(float height){return height+height/-9f;}
}
