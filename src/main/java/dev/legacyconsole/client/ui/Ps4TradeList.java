package dev.legacyconsole.client.ui;

/** UIScene_TradingMenu 0xad24a0 / 0xad2560 / 0xad3ce0, Java offer-list adapter.
 * PS4 retains exhausted offers and has ten columns; it permits selecting empty cells.
 * Java sends only unlocked offers, so no future-tier offers are synthesized here.
 */
final class Ps4TradeList {
  static final int COLUMNS=10;
  private int count,column,offset;
  boolean update(int count) {
    int before=serverIndex();
    this.count=Math.max(0,count);
    // 0xad29cf: retain the selector column when the offer vector changes.
    offset=Math.max(0,Math.min(offset,this.count-COLUMNS));
    return before!=serverIndex();
  }
  boolean choose(int index) {
    int before=selected();
    index=Math.max(0,Math.min(index,Math.max(COLUMNS,count)-1));
    if(index<offset)offset=index;
    if(index>=offset+COLUMNS)offset=index-COLUMNS+1;
    column=index-offset;
    return before!=selected();
  }
  int selected(){return offset+column;}
  int column(){return column;}
  int offset(){return offset;}
  int size(){return count;}
  int serverIndex(){return serverIndex(selected());}
  int serverIndex(int index){return index>=0&&index<count?index:-1;}
  boolean left(){return offset>0;}
  boolean right(){return offset+COLUMNS<count;}

  static String levelKey(int zeroBasedLevel) {
    return switch(zeroBasedLevel) {
      case 0->"07960f18";case 1->"1536049b";case 2->"d279b4e2";
      case 3->"e293078e";case 4->"5a64e9bc";default->null;
    };
  }
  /** 0xad5730: current tier progress plus the selected trade's XP / tier range. */
  static float preview(int zeroBasedLevel,float current,int tradeXp,int tierRange) {
    if(zeroBasedLevel==4)return 1;
    return Math.min(1,current+(float)tradeXp/tierRange);
  }
}
