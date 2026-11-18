package dev.legacyconsole.worldgen.ps4;

/** Defined generation traits in the original Material hierarchy. Class
 * overrides and flag mutations follow 0x44cc10..0x44cd10. Slot 0x80's
 * unwritten trailing byte and allocator padding are outside this port. */
public final class Ps4Material {
 public enum Type {BASE,GAS,LIQUID,DECORATION,PORTAL,WEB}
 /** The original color object's numeric words; no RGB interpretation. */
 public record Color(int code,int ordinal){}
 private final Type type;
 private final Color color;
 private boolean flammable,replaceable,translucent,toolNotRequired;
 private final int pushReaction;
 public Ps4Material(Type type,Color color,int flags,int pushReaction){this.type=type;this.color=color;flammable=(flags&1)!=0;replaceable=(flags&2)!=0;translucent=(flags&4)!=0;toolNotRequired=(flags&8)!=0;this.pushReaction=pushReaction;}
 public Type type(){return type;}
 public Color color(){return color;}
 public boolean liquid(){return type==Type.LIQUID;}
 public boolean solid(){return type==Type.BASE;}
 public boolean nonSolid(){return !liquid()&&!solid();}
 public boolean blocksLight(){return type==Type.BASE||type==Type.LIQUID||type==Type.WEB;}
 public boolean blocksMotion(){return type==Type.BASE;}
 public boolean flammable(){return flammable;}
 public boolean replaceable(){return replaceable;}
 public boolean translucent(){return translucent;}
 public boolean solidBlocking(){return !translucent&&blocksMotion();}
 public boolean toolNotRequired(){return toolNotRequired;}
 public int pushReaction(){return pushReaction;}
 public Ps4Material setTranslucent(){translucent=true;return this;}
 public Ps4Material requireTool(){toolNotRequired=false;return this;}
 public Ps4Material setFlammable(){flammable=true;return this;}
 public Ps4Material setReplaceable(){replaceable=true;return this;}
}
