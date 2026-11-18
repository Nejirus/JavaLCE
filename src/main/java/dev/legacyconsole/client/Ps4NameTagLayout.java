package dev.legacyconsole.client;
/** c0a083/c0a18d: the original background rectangle and five-point border strip. */
public final class Ps4NameTagLayout {
 private Ps4NameTagLayout(){}
 public record Bounds(float left,float top,float right,float bottom){}
 public static Bounds bounds(int width,int baseline){return new Bounds(-width/2-1,baseline-1,width/2+1,baseline+9);}
}
