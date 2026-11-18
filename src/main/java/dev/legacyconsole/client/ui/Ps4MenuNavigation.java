package dev.legacyconsole.client.ui;

/** PS4 section routing. Direction constants are the native ETapState values 1..4. */
public final class Ps4MenuNavigation {
  private Ps4MenuNavigation() {}
  public record Route(int section,int xOffset,int yOffset) {}
  private static Route r(int section){return new Route(section,0,0);}
  private static Route r(int section,int dx){return new Route(section,dx,0);}

  /** UIScene_FurnaceMenu::handleSection, 0xa40e10; tables 0xee64e0/0xee64f0. */
  public static Route furnace(int section,int d,int x,boolean split){
    return switch(section){
      case 4->d==2?r(x>5?8:6):d==1?(split?r(x>5?8:7):r(5)):r(section);
      case 5->d==2?r(4):d==1?r(x>5?8:7):r(section);
      case 6->table(d,new int[]{4,7,8,8},new int[]{-3,0,0,0},section);
      case 7->d==1?r(6):d==2?r(split?4:5,-3):d==3||d==4?r(8):r(section);
      case 8->d==1?r(4,-7):d==2?r(split?4:5,-7):d==3||d==4?r(6):r(section);
      default->r(section);
    };
  }
  /** UIScene_AnvilMenu::handleSection, 0xa0dbf0. */
  public static Route anvil(int section,int d,int x,boolean split){
    return switch(section){
      case 0x31->d==2?r(x>3?0x36:0x33):d==1?(split?r(x<4?0x33:x==4?0x34:0x35):r(0x32)):r(section);
      case 0x32->d==2?r(0x31):d==1?r(x<4?0x33:x==4?0x34:0x35):r(section);
      case 0x33->d==1?r(0x36):d==2?r(split?0x31:0x32,3):d==3?r(0x35):d==4?r(0x34):r(section);
      case 0x34->d==1?r(0x36):d==2?r(split?0x31:0x32,-4):d==3?r(0x33):d==4?r(0x35):r(section);
      case 0x35->d==1?r(0x36):d==2?r(split?0x31:0x32,-5):d==3?r(0x34):d==4?r(0x33):r(section);
      case 0x36->d==1?r(0x31,-4):d==2?r(0x34):r(section);
      default->r(section);
    };
  }

  /** 0x00a12f70; lookup constants at 0x00ee63f0..0x00ee641f. */
  public static Route brewing(int section,int d,int x,boolean split) {
    return switch(section) {
      case 0x29 -> d==2?r(x<2?0x2f:0x2e):d==1?
          (split?r(x<2?0x2f:x<4?0x2b:x==4?0x2c:0x2d):r(0x2a)):r(section);
      case 0x2a -> d==2?r(0x29):d==1?r(x<2?0x2f:x<4?0x2b:x==4?0x2c:0x2d):r(section);
      case 0x2b -> d==1?r(0x2e):d==2?r(split?0x29:0x2a,-3):d==3?r(0x2d):d==4?r(0x2c):r(section);
      case 0x2c -> d==1?r(0x2e):d==2?r(split?0x29:0x2a,-4):d==3?r(0x2b):d==4?r(0x2d):r(section);
      case 0x2d -> d==1?r(0x2e):d==2?r(split?0x29:0x2a,-5):d==3?r(0x2c):d==4?r(0x2b):r(section);
      case 0x2e -> table(d,new int[]{0x29,0x2c,0x2f,0x2f},new int[]{5,0,0,0},section);
      case 0x2f -> table(d,new int[]{0x29,0x2a,0x2e,0x2e},null,section);
      default -> r(section);
    };
  }
  /** 0x00a3bed0; constants at 0x00ee64a0..0x00ee64cf. */
  public static Route enchanting(int section,int d,int x,boolean split) {
    return switch(section) {
      case 0x21 -> d==2?r(x<3?(x==2?0x24:0x23):0x25):d==1?
          (split?r(x<3?(x==2?0x24:0x23):0x27):r(0x22)):r(section);
      case 0x22 -> d==2?r(0x21):d==1?r(x<3?(x==2?0x24:0x23):0x27):r(section);
      case 0x23 -> d==1?r(0x21):d==2?r(split?0x21:0x22):d==3?r(0x25):d==4?r(0x24):r(section);
      case 0x24 -> d==1?r(0x21,-2):d==2?r(split?0x21:0x22,-2):d==3?r(0x23):d==4?r(0x25):r(section);
      case 0x25 -> table(d,new int[]{0x21,0x26,0x24,0x23},new int[]{-7,0,0,0},section);
      case 0x26 -> table(d,new int[]{0x25,0x27,0x24,0x23},null,section);
      case 0x27 -> d==1?r(0x26):d==2?r(split?0x21:0x22,-7):d==3?r(0x24):d==4?r(0x23):r(section);
      default -> r(section);
    };
  }
  /** 0x00a146b0; constants at 0x00ee6420, 0x00ee6430 and 0x00ee6510. */
  public static Route cartography(int section,int d,int x,boolean split) {
    return switch(section) {
      case 0x59 -> d==2?r(x<4?0x5e:0x5d):d==1?
          (split?r(x<4?0x5c:0x5d):r(0x5a)):r(section);
      case 0x5a -> d==2?r(0x59):d==1?r(x<4?0x5c:0x5d):r(section);
      case 0x5b -> table(d,new int[]{0x5e,0x5c,0x5d,0x5d},null,section);
      case 0x5c -> d==1?r(0x5b):d==2?r(split?0x59:0x5a,-3):d==3||d==4?r(0x5d):r(section);
      case 0x5d -> d==1?r(0x59,4):d==2?r(split?0x59:0x5a,-4):d==3||d==4?r(0x5b):r(section);
      case 0x5e -> table(d,new int[]{0x59,0x5b,0x5b,0x5d},new int[]{3,0,0,0},section);
      default -> r(section);
    };
  }
  /** 0x00a0f5d0; the secondary visibility query is 0x009df1f0. */
  public static Route beacon(int section,int d,int x,boolean split,boolean secondaryVisible) {
    return switch(section) {
      case 0x38 -> d==2?r(x<2?0x3b:x<5?0x3c:x>8&&secondaryVisible?0x41:0x40):d==1?
          (split?r(x<4?0x3f:x<7?0x3a:0x42):r(0x39)):r(section);
      case 0x39 -> d==2?r(0x38):d==1?r(x<4?0x3f:x<7?0x3a:0x42):r(section);
      case 0x3a -> d==1?r(0x40):d==2?r(split?0x38:0x39,-5):d==3||d==4?r(0x42):r(section);
      case 0x3b -> table(d,new int[]{0x38,0x3d,0x3c,0x3c},new int[]{-1,0,0,0},section);
      case 0x3c -> table(d,new int[]{0x38,0x3e,0x3b,0x3b},new int[]{-3,0,0,0},section);
      case 0x3d -> d==1?r(0x3b):d==2?r(0x3f):d==3?r(secondaryVisible?0x41:0x40):d==4?r(0x3e):r(section);
      case 0x3e -> table(d,new int[]{0x3c,0x3f,0x3d,0x40},null,section);
      case 0x3f -> d==1?r(0x3d):d==2?r(split?0x38:0x39,-3):r(section);
      case 0x40 -> d==1?r(0x38,-7):d==2?r(0x3a):d==3?r(0x3e):d==4?r(secondaryVisible?0x41:0x3d):r(section);
      case 0x41 -> table(d,new int[]{0x38,0x3a,0x40,0x3d},new int[]{-8,0,0,0},section);
      case 0x42 -> d==1?r(0x40):d==2?r(split?0x38:0x39,-8):d==3||d==4?r(0x3a):r(section);
      default -> r(section);
    };
  }
  private static Route table(int direction,int[] sections,int[] offsets,int fallback) {
    return direction<1||direction>4?r(fallback):r(sections[direction-1],offsets==null?0:offsets[direction-1]);
  }
  /** IUIScene_AbstractContainerMenu::adjustTarget, 0x009a2610. */
  public static int[] position(int old,Route route,int direction,int x,int y,int columns,int rows) {
    int xx=x-route.xOffset(),yy=y;
    if(route.section()!=old) {
      yy=direction==1?rows-1-route.yOffset():direction==2?-route.yOffset():y-route.yOffset();
      yy=yy<0?0:yy<rows?yy:rows-1;
      xx=xx<0?0:xx<columns?xx:columns-1;
    } else xx=xx<0?columns-1:xx<columns?xx:0;
    return new int[]{xx,yy};
  }
}
