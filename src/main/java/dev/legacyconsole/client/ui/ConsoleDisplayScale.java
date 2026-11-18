package dev.legacyconsole.client.ui;
/** LCE's 640x360 wide and 640x480 standard scene profiles, at integer GUI scales. */
public final class ConsoleDisplayScale {
 private ConsoleDisplayScale(){}public static int automaticFactor(int width,int height){int designHeight=LceViewport.standard(width,height)?480:360;return Math.max(1,Math.min(width/640,height/designHeight));}
}
