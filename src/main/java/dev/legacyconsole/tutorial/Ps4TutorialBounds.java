package dev.legacyconsole.tutorial;

import net.minecraft.world.phys.Vec3;

/** FullTutorial's AreaConstraint has contains=1, horizontal=1, vertical=0
 * at 0x9669ba..0x9669c6. Translate the axis correction in 0x961df0;
 * the area's authoring height is not a movement ceiling. */
public final class Ps4TutorialBounds {
 public static Vec3 constrain(Vec3 from,Vec3 target,double minX,double minZ,double maxX,double maxZ){
  return new Vec3(axis(from.x,target.x,minX,maxX),target.y,axis(from.z,target.z,minZ,maxZ));
 }
 private static double axis(double from,double target,double min,double max){
  if(target<=min&&target<from)return min;
  if(target>=max&&target>from)return max;
  return target;
 }
 private Ps4TutorialBounds(){}
}
