package dev.legacyconsole.worldgen.ps4;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/** CoralFeature's original shape helpers. Lists retain duplicates and native
 * append order; later fan placement consumes the same population random. */
final class Ps4CoralShapes {
 record Pos(int x,int y,int z){
  Pos offset(int dx,int dy,int dz){return new Pos(x+dx,y+dy,z+dz);}
  Pos up(int dy){return offset(0,dy,0);}
  Pos relative(int d){return offset(Ps4DirectionPlane.dx(d),d==0?-1:d==1?1:0,Ps4DirectionPlane.dz(d));}
 }
 record Side(Pos position,int direction){}
 final Ps4CoralFeatures.Access world;
 final Random random;
 final int state,color;
 final List<Pos> tops=new ArrayList<>();
 final List<Side> sides=new ArrayList<>();
 Ps4CoralShapes(Ps4CoralFeatures.Access world,Random random,int state){this.world=world;this.random=random;this.state=state;color=state>>>16&7;}
 private boolean sameColor(Pos p){int other=world.block(p.x,p.y,p.z);return (other&65535)!=259||(other>>>16&7)==color;}
 private boolean water(Pos p){return world.waterMaterial(world.block(p.x,p.y,p.z));}
 private boolean blocking(Pos p){return world.solidBlocking(world.block(p.x,p.y,p.z));}
 private boolean canPlace(Pos p){return world.canPlaceState(state,p.x,p.y,p.z);}
 private void write(Pos p){world.setBlock(p.x,p.y,p.z,state,2);}
 private int horizontal(){return random.nextInt(4)+2;}
 private static int clockwise(int d){return switch(d){case 2->5;case 5->3;case 3->4;case 4->2;default->throw new IllegalArgumentException("Horizontal direction required");};}
 private static int counterclockwise(int d){return clockwise(d)^1;}
 private int remainingDirection(int first,int second){
  int pair=(1<<first)|(1<<second);int[] options=switch(pair){case (1<<2)|(1<<5)->new int[]{3,4};case (1<<2)|(1<<3)->new int[]{5,4};case (1<<2)|(1<<4)->new int[]{3,5};case (1<<3)|(1<<5)->new int[]{2,4};case (1<<5)|(1<<4)->new int[]{3,2};default->new int[]{2,5};};return options[random.nextInt(2)];
 }
 private boolean supportedWrite(Pos p){if(!canPlace(p.up(1))||!sameColor(p))return false;write(p);return true;}
 private void sideCandidate(Pos p){int direction=horizontal();if(random.nextFloat()<.4f)sides.add(new Side(p,direction));}
 private boolean sideColumn(Pos base,int height){
  for(int i=0;i<height;i++){if(!supportedWrite(base.up(i)))return false;sideCandidate(base);}return true;
 }

 /** 0x1d04c0. Side candidates intentionally refer to each column's base. */
 void branch(Pos p,int direction,boolean shortened){
  int kind=shortened?1+random.nextInt(2):random.nextInt(3);
  if(kind==2){
   if(!sameColor(p))return;write(p);p=p.up(1).relative(direction);
   if(!sideColumn(p,2))return;
   int turn=random.nextBoolean()?clockwise(direction):counterclockwise(direction);
   p=p.relative(turn).up(1);if(!sideColumn(p,2))return;
   if(random.nextFloat()<.2f)tops.add(p.up(2));
  }else if(kind==1){
   if(!sideColumn(p,2))return;
   p=p.relative(direction).up(1);if(!sideColumn(p,3))return;
   if(random.nextFloat()<.2f)tops.add(p.up(3));
  }else{
   if(!supportedWrite(p))return;p=p.relative(direction);
   if(!canPlace(p.up(2))||!sameColor(p.up(1)))return;write(p.up(1));
   p=p.relative(direction).up(2);if(!supportedWrite(p))return;sideCandidate(p);
   p=p.up(1);if(!supportedWrite(p))return;sideCandidate(p);
   p=p.up(1).relative(direction^1);if(!supportedWrite(p))return;
   if(random.nextFloat()<.2f)tops.add(p.up(1));
  }
 }

 /** 0x1cc8e0. The foundation center remains at the original position while
  * the cross/diagonal helpers receive separate layer offsets. */
 void shape0(Pos origin){
  int choice=random.nextInt(2),height=choice+1,width=choice*2+3;
  for(int layer=0;layer<=choice+1;layer++){
   if(!canPlace(origin.up(layer+1))||!sameColor(origin))return;write(origin);
   if(width>1){
    float bias=layer*.15f;
    for(int i=1;i<=width/2;i++){
     float chance=(i*.1f+.5f)-bias;
     if(height==2&&layer==0){cross(origin,0,true,chance);diagonal(origin,i,true,.25f);}
     else cross(origin.up(layer),layer,true,chance);
    }
   }
   width-=2;
  }
  Pos p=origin.up(height);if(!canPlace(p.up(1)))return;
  int first=horizontal(),second=Ps4CoralFeatures.otherDirection(random,first),third=remainingDirection(first,second);
  int[] directions={first,second,third};if(!sameColor(p))return;write(p);
  branch(p.relative(first),first,false);
  int count=2+random.nextInt(2);
  for(int i=1;i<count;i++){Pos q=p.up(i);if(!sameColor(q))return;write(q);branch(q.relative(directions[i]),directions[i],i==count-1);}
 }

 /** 0x1ccf80. The skipped minimum-X/Z corner is checked separately. */
 void shape1(Pos origin){
  int radius=1+random.nextInt(2),width=radius*2+1;Pos corner=origin.offset(-radius,0,-radius);
  for(int dx=0;dx<width;dx++)for(int dz=0;dz<width;dz++){
   if((dx==0||dx==width-1)&&(dz==0||dz==width-1))continue;
   Pos q=corner.offset(dx,0,dz);if(random.nextInt(5)!=0&&blocking(q.up(-1))){if(!sameColor(q))return;write(q);}
  }
  if(sameColor(corner))write(corner);
  Pos p=origin.up(1);if(!supportedWrite(p))return;
  int[][] arms={{1,0,2},{-1,0,3},{0,1,4},{0,-1,3}};
  for(int[] a:arms)if(random.nextInt(a[2])!=0){Pos q=p.offset(a[0],0,a[1]);if(!sameColor(q))return;write(q);}
  p=p.up(1);cross(p,1,true,.33f);p=p.up(1);cross(p,1,true,.7f);p=p.up(1);
  int height=1+random.nextInt(2);
  for(int i=1;i<=height;i++){
   if(!supportedWrite(p.up(i-1)))return;sideCandidate(p);
   if(i==height&&random.nextFloat()<.4f)tops.add(p.up(height));
  }
 }

 /** 0x1cd950. Three forward-growing arms, with independent optional rises. */
 void rare(Pos origin){
  int direction=horizontal(),right=clockwise(direction),left=right^1;
  int width=3+random.nextInt(2),height=1+random.nextInt(3);
  if(!canPlace(origin.up(height+2)))return;
  for(int i=0;i<height;i++){Pos q=origin.up(i);if(!sameColor(q))return;write(q);}
  Pos center=origin.up(height),r=center.relative(right),l=center.relative(left);
  if(width!=3){
   if(random.nextBoolean()){if(!sameColor(r))return;write(r);r=r.relative(right);}
   else{if(!sameColor(l))return;write(l);l=l.relative(left);}
  }
  if(!sameColor(r))return;write(r);if(!sameColor(l))return;write(l);
  Pos[] arms={center.relative(direction),r.up(1).relative(direction),l.up(1).relative(direction)};
  for(int i=0;i<3;i++){
   int length=(i==0?2:4)+random.nextInt(2);Pos p=arms[i];
   for(int step=0;step<length;step++){
    if(!sameColor(p))return;write(p);
    if(random.nextFloat()<.1f)tops.add(p.up(1));
    if(random.nextFloat()<.6f)sides.add(new Side(p,right));
    if(random.nextFloat()<.6f)sides.add(new Side(p,left));
    if(random.nextFloat()<.25f&&canPlace(p.up(2)))p=p.up(1);
    p=p.relative(direction);
   }
  }
 }

 /** 0x1cc3d0: the original switch intentionally falls through. */
 void dispatch(Pos origin){
  int kind=random.nextInt(3);
  if(kind==0)shape0(origin);
  if(kind<=1)shape1(origin);
  if(random.nextInt(7)==0)rare(origin);else patch(origin);
 }

 /** 0x1cf6f0. A rejected selected arm returns from the entire helper. */
 void cross(Pos p,int radius,boolean increasing,float chance){
  if(!sameColor(p))return;write(p);
  int[][] offsets={{radius,0},{-radius,0},{0,radius},{0,-radius}};
  for(int[] d:offsets){
   float draw=random.nextFloat();if(increasing?draw<chance:draw>chance){
    Pos q=p.offset(d[0],0,d[1]);if(!sameColor(q)||!blocking(q.up(-1)))return;write(q);
   }
  }
 }

 /** 0x1cfe00. The second diagonal is deliberately repeated by mcps4. */
 void diagonal(Pos p,int radius,boolean increasing,float chance){
  int[][] offsets={{radius,radius},{-radius,radius},{-radius,radius},{radius,-radius}};
  for(int[] d:offsets){
   float draw=random.nextFloat();if(increasing?draw<chance:draw>chance){
    Pos q=p.offset(d[0],0,d[1]);if(!sameColor(q)||!blocking(q.up(-1)))return;write(q);
   }
  }
 }

 /** 0x1ce3d0. Foundation X then Z, followed by the upward cross. */
 void patch(Pos origin){
  for(int dx=-1;dx<=1;dx++)for(int dz=-1;dz<=1;dz++){
   Pos q=origin.offset(dx,0,dz);
   if(random.nextInt(3)!=0&&!water(q.up(-1))){if(!sameColor(q))return;write(q);}
  }
  Pos p=origin.up(1);if(!canPlace(p.up(1))||!sameColor(p))return;write(p);
  int[][] offsets={{1,0},{-1,0},{0,1},{0,-1}};
  for(int[] d:offsets){
   Pos q=p.offset(d[0],0,d[1]);if(random.nextInt(4)!=0&&!water(q.up(-1))){
    if(!sameColor(q))return;write(q);if(random.nextFloat()<.2f)tops.add(q.up(1));
   }
  }
  if(canPlace(p.up(2))){Pos q=p.up(1);if(!sameColor(q))return;write(q);if(random.nextFloat()<.2f)tops.add(p.up(2));}
  if(random.nextFloat()<.25f&&canPlace(p.up(3))){Pos q=p.up(2);if(!sameColor(q))return;write(q);if(random.nextFloat()<.1f)tops.add(p.up(3));}
 }
}
