package dev.legacyconsole.client.ui;
import static org.junit.Assert.*;
import org.junit.Test;

public class JoinPlayerLayoutTest {
 @Test public void everyVisibleRowStaysInsideTheRecessedFrame(){
  for(int width:new int[]{168,193,225})for(int height:new int[]{100,142,166,177}){
   var list=new JoinPlayerLayout(20,40,width,height);
   for(int i=0;i<list.rows;i++){
    assertTrue(list.x>=20+7+8);assertTrue(list.x+list.width<=20+width-7-8);
    assertTrue(list.rowY(i)>=40+9+8);
    assertTrue(list.rowY(i)+JoinPlayerLayout.ROW_HEIGHT<=40+height-33-8);
   }
  }
 }
 @Test public void lastPageAndShortPlayerSamplesStayInRange(){
  var list=new JoinPlayerLayout(0,0,225,177);
  assertEquals(3,list.rows);assertEquals(0,list.offset(100,2));
  assertEquals(13,list.offset(100,16));assertEquals(0,list.offset(-1,16));
 }
}
