package dev.legacyconsole.client.ui;

/** Rows inside panel_recess's original eight-unit nine-slice frame. */
final class JoinPlayerLayout {
 static final int FRAME=8,INSET=1;
 // LoadOrJoinMenu720 and LoadOrJoinMenu1080 both use 30-unit list rows.
 static final int ROW_HEIGHT=30,PITCH=30;
 final int x,y,width,height,rows;
 JoinPlayerLayout(int panelX,int panelY,int panelWidth,int panelHeight){
  x=panelX+7+FRAME+INSET;y=panelY+9+FRAME+INSET;
  width=Math.max(1,panelWidth-14-2*(FRAME+INSET));
  height=Math.max(0,panelHeight-42-2*(FRAME+INSET));
  rows=Math.max(0,(height+PITCH-ROW_HEIGHT)/PITCH);
 }
 int rowY(int row){return y+row*PITCH;}
 int offset(int requested,int count){return Math.max(0,Math.min(requested,Math.max(0,count-rows)));}
}
