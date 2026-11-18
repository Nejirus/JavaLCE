package dev.legacyconsole.client.ui;

/** Mixed host rows in the PS4 scene's source units, sharing the settings-menu gaps. */
final class HostOptionsLayout {
 static final int UNITS=3;
 final int first,end,lastFirst;
 private final int[] heights;
 private final boolean[] checkboxes;
 private final float available;

 HostOptionsLayout(int[] heights,boolean[] checkboxes,float available,int requestedFirst,int reveal){
  this.heights=heights;this.checkboxes=checkboxes;this.available=available;
  int last=Math.max(0,heights.length-1);
  if(heights.length>0){
   int used=heights[last];
   while(last>0&&used+gap(last-1)+heights[last-1]<=available+.01f){used+=gap(last-1)+heights[--last];}
  }
  lastFirst=last;
  int start=Math.max(0,Math.min(requestedFirst,lastFirst));
  if(reveal>=0&&reveal<heights.length){
   if(reveal<start)start=reveal;
   while(reveal>=end(start)&&start<lastFirst)start++;
  }
  first=start;end=end(start);
 }

 private int gap(int index){
  return UNITS*ConsoleOptionLayout.gap(checkboxes[index],checkboxes[index+1],ConsoleOptionLayout.CHECKBOX_PITCH);
 }
 private int end(int start){
  int i=start,used=0;
  while(i<heights.length){
   int next=heights[i]+(i>start?gap(i-1):0);
   if(i>start&&used+next>available+.01f)break;
   used+=next;i++;
  }
  return i;
 }
 int y(int index){
  int y=0;for(int i=first;i<index;i++)y+=heights[i]+gap(i);return y;
 }
 static int nextEnabled(boolean[] enabled,int current,int direction){
  if(enabled.length==0)return -1;
  int step=direction<0?-1:1;
  if(current<0||current>=enabled.length)current=step>0?-1:0;
  for(int i=0;i<enabled.length;i++){
   current=Math.floorMod(current+step,enabled.length);
   if(enabled[current])return current;
  }
  return -1;
 }
}
