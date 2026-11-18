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
  /** UIScene's player draw 0x9f6f30: original 1920/1080 aspect and 8/9 foot anchor. */
  public static float playerScale(float width){return width/(1920f/1080f);}
  public static float playerFoot(float height){return height+height/-9f;}
}
