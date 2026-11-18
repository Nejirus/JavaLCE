package dev.legacyconsole.client.ui;
import static org.junit.Assert.*;
import org.junit.Test;

public class HostOptionsLayoutTest {
 private static final int[] HEIGHTS={36,36,36,36,36,36,60,60,60,60,60,60,60,60,36,36,36,60,60};
 private static final boolean[] BOXES={true,true,true,true,true,true,false,false,false,false,false,false,false,false,true,true,true,false,false};
 @Test public void mixedRowsUseSettingsSpacing(){
  var p=new HostOptionsLayout(HEIGHTS,BOXES,610,0,-1);
  assertEquals(45,p.y(1)-p.y(0));
  assertEquals(54,p.y(6)-p.y(5));
  assertEquals(75,p.y(7)-p.y(6));
  assertTrue(p.y(p.end-1)+HEIGHTS[p.end-1]<=610);
 }
 @Test public void scrollingStopsAtFilledLastPage(){
  var p=new HostOptionsLayout(HEIGHTS,BOXES,610,1000,-1);
  assertEquals(HEIGHTS.length,p.end);assertEquals(p.lastFirst,p.first);
  assertTrue(p.first<HEIGHTS.length-1);
  assertTrue(p.y(p.end-1)+HEIGHTS[p.end-1]<=610);
 }
 @Test public void everyRowCanBeRevealedWithoutLeavingViewport(){
  for(float available:new float[]{490,555,610})for(int start:new int[]{0,5,100})for(int selected=0;selected<HEIGHTS.length;selected++){
   var p=new HostOptionsLayout(HEIGHTS,BOXES,available,start,selected);
   assertTrue(selected>=p.first&&selected<p.end);
   assertTrue(p.y(selected)>=0&&p.y(selected)+HEIGHTS[selected]<=available);
  }
 }
 @Test public void stateRefreshKeepsScrolledPage(){
  var p=new HostOptionsLayout(HEIGHTS,BOXES,610,5,-1);
  var refresh=new HostOptionsLayout(HEIGHTS,BOXES,610,p.first,-1);
  assertEquals(5,refresh.first);assertEquals(p.end,refresh.end);
 }
 @Test public void emptyAndShortListsDoNotScrollToBlankPages(){
  var empty=new HostOptionsLayout(new int[0],new boolean[0],490,10,-1);
  assertEquals(0,empty.first);assertEquals(0,empty.end);
  var shortList=new HostOptionsLayout(new int[]{90,90},new boolean[2],490,10,-1);
  assertEquals(0,shortList.first);assertEquals(2,shortList.end);assertEquals(105,shortList.y(1));
 }
 @Test public void backwardsScrollingReturnsToFirstRow(){
  var p=new HostOptionsLayout(HEIGHTS,BOXES,610,-100,-1);
  assertEquals(0,p.first);assertTrue(p.end>1);
 }
 @Test public void navigationWrapsInBothDirectionsAndSkipsDisabledRows(){
  boolean[] enabled={true,false,true,true,false};
  assertEquals(3,HostOptionsLayout.nextEnabled(enabled,0,-1));
  assertEquals(0,HostOptionsLayout.nextEnabled(enabled,3,1));
  assertEquals(2,HostOptionsLayout.nextEnabled(enabled,0,1));
  assertEquals(0,HostOptionsLayout.nextEnabled(enabled,2,-1));
 }
 @Test public void disabledHeaderAndEmptyMenusHaveNoInvalidTargets(){
  assertEquals(2,HostOptionsLayout.nextEnabled(new boolean[]{false,true,true},1,-1));
  assertEquals(1,HostOptionsLayout.nextEnabled(new boolean[]{false,true,true},2,1));
  assertEquals(-1,HostOptionsLayout.nextEnabled(new boolean[0],0,1));
  assertEquals(-1,HostOptionsLayout.nextEnabled(new boolean[]{false,false},0,-1));
 }
}
