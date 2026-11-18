package dev.legacyconsole.worldgen.ps4;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.zip.GZIPInputStream;

/** PS4 Mth::sin/cos (0x496b30/0x496c40). The table is the exact original
 * initialization output, including float argument rounding and libc _FSin.
 * Recomputing it with the host JVM's Math.sin changes its float bits. */
public final class Ps4Math {
 private Ps4Math(){}
 private static final float[] SIN=load();
 private static float[] load(){
  try(InputStream source=Ps4Math.class.getResourceAsStream("/assets/legacyconsole/worldgen/ps4-sine.f32.gz")){
   if(source==null)throw new IOException("Missing PS4 sine table");
   byte[] bytes=new GZIPInputStream(source).readAllBytes();
   if(bytes.length!=65536*4)throw new IOException("Invalid PS4 sine table length");
   ByteBuffer data=ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN);float[] values=new float[65536];
   for(int i=0;i<values.length;i++)values[i]=data.getFloat();return values;
  }catch(IOException e){throw new IllegalStateException("Cannot load original PS4 math table",e);}
 }
 public static float sin(float angle){return SIN[(int)(angle*10430.378f)&65535];}
 public static float cos(float angle){return SIN[(int)(angle*10430.378f+16384f)&65535];}

 // libc.prx _FSin -> sinf (0xc73b0), cosf (0xc1770). Underwater
 // carvers call these directly, instead of using Mth's lookup table.
 // Constants and evaluation order come from those routines, including
 // the double intermediate operations and the final conversion to float.
 private static final double HALF_PI=0x1.921fb54442d18p+0;
 private static final double PI=0x1.921fb54442d18p+1;
 private static final double THREE_HALF_PI=0x1.2d97c7f3321d2p+2;
 private static final double TWO_PI=0x1.921fb54442d18p+2;
 private static final double S1=-0x1.5555554cbac77p-3,S2=0x1.11110896efbb2p-7;
 private static final double S3=-0x1.a00f9e2cae774p-13,S4=0x1.6cd878c3b46a7p-19;
 private static final double C1=-0x1.ffffffd0c5e81p-2,C2=0x1.55553e1053a42p-5;
 private static final double C3=-0x1.6c087e80f1e27p-10,C4=0x1.99342e0ee5069p-16;
 private static float kernelSin(double x){
  double z=x*x,w=z*z,s=x*z,r=z*S4+S3;
  return (float)((x+s*(z*S2+S1))+(s*w)*r);
 }
 private static float kernelNegativeSin(double x){
  double z=x*x,w=z*z,s=x*z,r=z*S4+S3;
  return (float)(((z*S2+S1)*(-s)-x)-(w*s)*r);
 }
 private static float kernelCos(double x){
  double z=x*x,w=z*z;
  return (float)(((z*C1+1)+w*C2)+(z*w)*(z*C4+C3));
 }
 private static double reduce(float x){
  // This is libc's medium argument branch, covering every angle produced
  // by the carvers. Its large argument kernel is deliberately not replaced
  // with the host JVM's trigonometry.
  if((Float.floatToRawIntBits(x)&0x7fffffff)>=0x4dc90fdb)
   throw new IllegalArgumentException("PS4 carver angle exceeds recovered reduction range");
  return ((double)x*0x1.45f306dc9c883p-1+0x1.8p52)-0x1.8p52;
 }
 /** Original direct sinf, for the bounded angles used by PS4 carvers. */
 public static float sinDirect(float angle){
  int bits=Float.floatToRawIntBits(angle),a=bits&0x7fffffff;
  if(a<=0x3f490fda)return a<0x39800000?angle:kernelSin(angle);
  if(a<=0x407b53d1){
   if(a<=0x4016cbe3)return bits>0?kernelCos(angle-HALF_PI):-kernelCos(angle+HALF_PI);
   return kernelSin((bits>0?PI:-PI)-angle);
  }
  if(a<=0x40e231d5){
   if(a<=0x40afeddf)return bits>0?-kernelCos(angle-THREE_HALF_PI):kernelCos(angle+THREE_HALF_PI);
   return kernelSin(angle+(bits>0?-TWO_PI:TWO_PI));
  }
  if(a>=0x7f800000)return angle-angle;
  double n=reduce(angle),x=(angle+n*-0x1.921fb5p+0)+n*-0x1.110b4611a6263p-26;
  return switch((int)n&3){case 0->kernelSin(x);case 1->kernelCos(x);case 2->kernelNegativeSin(x);default->-kernelCos(x);};
 }
 /** Original direct cosf, for the bounded angles used by PS4 carvers. */
 public static float cosDirect(float angle){
  int bits=Float.floatToRawIntBits(angle),a=bits&0x7fffffff;
  if(a<=0x3f490fda)return a<0x39800000?1:kernelCos(angle);
  if(a<=0x407b53d1){
   if(a<=0x4016cbe3)return kernelSin(bits>0?HALF_PI-angle:angle+HALF_PI);
   return -kernelCos(angle+(bits>0?-PI:PI));
  }
  if(a<=0x40e231d5){
   if(a<=0x40afeddf)return kernelSin(bits>0?angle-THREE_HALF_PI:-THREE_HALF_PI-angle);
   return kernelCos(angle+(bits>0?-TWO_PI:TWO_PI));
  }
  if(a>=0x7f800000)return angle-angle;
  double n=reduce(angle),x=(angle+n*-0x1.921fb5p+0)+n*-0x1.110b4611a6263p-26;
  return switch((int)n&3){case 0->kernelCos(x);case 1->kernelNegativeSin(x);case 2->-kernelCos(x);default->kernelSin(x);};
 }

 // Original libc double cos (0xc13c0), including its two-part remainder.
 // Badlands uses this directly, with a double argument and float PI constant.
 private static double kernelCosDouble(double x,double tail){
  double z=x*x,w=z*z;
  double low=z*(z*0x1.a01a019cb159p-16-0x1.6c16c16c15177p-10)+0x1.555555555554cp-5;
  double high=z*(z*-0x1.8fae9be8838d4p-37+0x1.1ee9ebdb4b1c4p-29)-0x1.27e4f809c52adp-22;
  double r=z*low+(w*w)*high,hz=z*.5,base=1-hz;
  return base+(((1-base)-hz)+(z*r-x*tail));
 }
 private static double kernelSinDouble(double x,double tail){
  double z=x*x,w=z*z*z;
  double r=w*(z*0x1.5d93a5acfd57cp-33-0x1.ae5e68a2b9cebp-26)
   +(z*(z*0x1.71de357b1fe7dp-19-0x1.a01a019c161d5p-13)+0x1.111111110f8a6p-7);
  double v=z*x;
  return x-(v*0x1.5555555555549p-3+(z*(tail*.5-v*r)-tail));
 }
 /** Original double cos for the bounded/medium arguments used by badlands. */
 public static double cosDouble(double angle){
  long bits=Double.doubleToRawLongBits(angle);int hi=(int)(bits>>>32),a=hi&0x7fffffff;
  if(a<0x3fe921fc)return a<=0x3e46a09d?1:kernelCosDouble(angle,0);
  if(a>=0x7ff00000)return angle-angle;
  if(a>=0x413921fb)throw new IllegalArgumentException("PS4 surface angle exceeds recovered reduction range");
  int quadrant;double head,tail,y;
  boolean negative=hi<0;
  if(a<0x400f6a7b&&(hi&0xfffff)!=0x921fb){
   if(a<0x4002d97d){
    head=angle+(negative?0x1.921fb544p0:-0x1.921fb544p0);
    tail=negative?0x1.0b4611a626331p-34:-0x1.0b4611a626331p-34;quadrant=negative?-1:1;
   }else{
    head=angle+(negative?0x1.921fb544p1:-0x1.921fb544p1);
    tail=negative?0x1.0b4611a626331p-33:-0x1.0b4611a626331p-33;quadrant=negative?-2:2;
   }
   y=head+tail;tail=(head-y)+tail;
  }else if(a>=0x400f6a7b&&a<0x401c463c&&((a<0x4015fdbd&&a!=0x4012d97c)||(a>=0x4015fdbd&&a!=0x401921fb))){
   if(a<0x4015fdbd){
    head=angle+(negative?0x1.2d97c7f3p2:-0x1.2d97c7f3p2);
    tail=negative?0x1.90e91a79394cap-33:-0x1.90e91a79394cap-33;quadrant=negative?-3:3;
   }else{
    head=angle+(negative?0x1.921fb544p2:-0x1.921fb544p2);
    tail=negative?0x1.0b4611a626331p-32:-0x1.0b4611a626331p-32;quadrant=0;
   }
   y=head+tail;tail=(head-y)+tail;
  }else{
   double n=(angle*0x1.45f306dc9c883p-1+0x1.8p52)-0x1.8p52;quadrant=(int)n;
   head=angle+n*-0x1.921fb544p0;double w=n*0x1.0b4611a626331p-34;y=head-w;
   if((a>>>20)-((int)(Double.doubleToRawLongBits(y)>>>52)&0x7ff)>16){
    double t=n*0x1.0b4611a6p-34,previous=head;head=previous-t;
    w=n*0x1.3198a2e037073p-69-((previous-head)-t);y=head-w;
    if((a>>>20)-((int)(Double.doubleToRawLongBits(y)>>>52)&0x7ff)>49){
     t=n*0x1.3198a2ep-69;previous=head;head=previous-t;
     w=n*0x1.b839a252049c1p-104-((previous-head)-t);y=head-w;
    }
   }
   tail=(head-y)-w;
  }
  return switch(quadrant&3){case 0->kernelCosDouble(y,tail);case 1->-kernelSinDouble(y,tail);case 2->-kernelCosDouble(y,tail);default->kernelSinDouble(y,tail);};
 }

 private static double kernelSinWithoutTail(double x){
  double z=x*x,w=z*z*z;
  double r=w*(z*0x1.5d93a5acfd57cp-33-0x1.ae5e68a2b9cebp-26)
   +(z*(z*0x1.71de357b1fe7dp-19-0x1.a01a019c161d5p-13)+0x1.111111110f8a6p-7);
  return x+(z*x)*(z*r-0x1.5555555555549p-3);
 }
 /** Original libc double sine (0xc6410), for the bounded/medium arguments
  * used by iceberg geometry; no host trigonometry is substituted. */
 public static double sinDouble(double angle){
  long bits=Double.doubleToRawLongBits(angle);int hi=(int)(bits>>>32),a=hi&0x7fffffff;
  if(a<0x3fe921fc)return a<=0x3e4fffff?angle:kernelSinWithoutTail(angle);
  if(a>=0x7ff00000)return angle-angle;
  if(a>=0x413921fb)throw new IllegalArgumentException("PS4 sine angle exceeds recovered reduction range");
  int quadrant;double head,tail,y;
  boolean negative=hi<0;
  if(a<0x400f6a7b&&(hi&0xfffff)!=0x921fb){
   if(a<0x4002d97d){
    head=angle+(negative?0x1.921fb544p0:-0x1.921fb544p0);
    tail=negative?0x1.0b4611a626331p-34:-0x1.0b4611a626331p-34;quadrant=negative?-1:1;
   }else{
    head=angle+(negative?0x1.921fb544p1:-0x1.921fb544p1);
    tail=negative?0x1.0b4611a626331p-33:-0x1.0b4611a626331p-33;quadrant=negative?-2:2;
   }
   y=head+tail;tail=(head-y)+tail;
  }else if(a>=0x400f6a7b&&a<0x401c463c&&((a<0x4015fdbd&&a!=0x4012d97c)||(a>=0x4015fdbd&&a!=0x401921fb))){
   if(a<0x4015fdbd){
    head=angle+(negative?0x1.2d97c7f3p2:-0x1.2d97c7f3p2);
    tail=negative?0x1.90e91a79394cap-33:-0x1.90e91a79394cap-33;quadrant=negative?-3:3;
   }else{
    head=angle+(negative?0x1.921fb544p2:-0x1.921fb544p2);
    tail=negative?0x1.0b4611a626331p-32:-0x1.0b4611a626331p-32;quadrant=0;
   }
   y=head+tail;tail=(head-y)+tail;
  }else{
   double n=(angle*0x1.45f306dc9c883p-1+0x1.8p52)-0x1.8p52;quadrant=(int)n;
   head=angle+n*-0x1.921fb544p0;double w=n*0x1.0b4611a626331p-34;y=head-w;
   if((a>>>20)-((int)(Double.doubleToRawLongBits(y)>>>52)&0x7ff)>16){
    double t=n*0x1.0b4611a6p-34,previous=head;head=previous-t;
    w=n*0x1.3198a2e037073p-69-((previous-head)-t);y=head-w;
    if((a>>>20)-((int)(Double.doubleToRawLongBits(y)>>>52)&0x7ff)>49){
     t=n*0x1.3198a2ep-69;previous=head;head=previous-t;
     w=n*0x1.b839a252049c1p-104-((previous-head)-t);y=head-w;
    }
   }
   tail=(head-y)-w;
  }
  return switch(quadrant&3){case 0->kernelSinDouble(y,tail);case 1->kernelCosDouble(y,tail);case 2->-kernelSinDouble(y,tail);default->-kernelCosDouble(y,tail);};
 }
}
