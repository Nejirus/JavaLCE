package dev.legacyconsole.worldgen.ps4;

/** Literal scalar getData/fromData routines selected by original code address.
 * Property values retain their original integer/boolean type. */
public final class Ps4ScalarStateCodecs {
 private Ps4ScalarStateCodecs(){}
 public interface Access {
  Object value(long propertyAlias);
  /** CropBlock's property selector at virtual slot 0x4e0. */
  long virtualProperty();
 }
 public interface DecodeAccess<S> {
  S defaultState();
  S with(S state,long propertyAlias,Object value);
  long virtualProperty();
 }
 private static int integer(Access state,long alias){return (Integer)state.value(alias);}
 private static int bool(Access state,long alias){return (Boolean)state.value(alias)?1:0;}
 public static int encode(int method,Access state){
  switch(method){
   case 0x91ec0:case 0x17f9b0:case 0x2a5350:case 0x336930:case 0x4a2af0:case 0x6e0200:case 0x780880:return 0;
   case 0x40250:{int data=0;if(integer(state,0x1409750L)!=0){if(integer(state,0x1409750L)==1)data=1;else if(integer(state,0x1409750L)==2)data=2;}if(integer(state,0x1409758L)==1)data|=8;return data;}
   case 0x44110:return integer(state,0x1409798L);
   case 0xe4d20:{int first=bool(state,0x140bcc0L),second=bool(state,0x140bcc8L);return bool(state,0x140bcd0L)<<2|second*2|first;}
   case 0xeadb0:return bool(state,0x140bd40L);
   case 0x1597c0:return integer(state,0x140fa70L);
   case 0x15a2a0:return integer(state,0x140fa98L);
   case 0x16d380:{int level=integer(state,0x140fee0L);return integer(state,0x140fee8L)==1?level+8:level;}
   case 0x17dc90:return integer(state,0x140ffb8L);
   case 0x1b3f20:return integer(state,0x14107b8L);
   case 0x1c6920:return integer(state,0x14133a8L);
   case 0x1c7240:{int variant=integer(state,0x1413560L);return bool(state,0x1413568L)!=0?variant|8:variant;}
   case 0x1ca3e0:{int variant=integer(state,0x1413588L);return integer(state,0x1413590L)<<3|variant;}
   case 0x1cb930:{int variant=integer(state,0x1413598L),direction=integer(state,0x14135a0L);return bool(state,0x1413768L)|direction<<2|variant*2;}
   case 0x1ec1b0:return integer(state,state.virtualProperty());
   case 0x1f67c0:return integer(state,0x1413bd0L);
   case 0x271980:return integer(state,0x1415928L);
   case 0x2a46d0:return integer(state,0x1416230L);
   case 0x2b12d0:return integer(state,0x1416430L);
   case 0x31cab0:return integer(state,0x1416a10L);
   case 0x3c3bb0:return bool(state,0x14181a8L);
   case 0x3c5df0:return integer(state,0x14182a8L);
   case 0x3ddf90:return bool(state,0x14183a0L);
   case 0x41c550:return integer(state,0x14188e0L);
   case 0x4b9570:return integer(state,0x1419938L);
   case 0x58b8c0:return bool(state,0x142ecd8L);
   case 0x5b4380:return integer(state,0x142f6d0L);
   case 0x5cae00:{int stability=integer(state,0x142fbc0L);return bool(state,0x142fbc8L)<<3|stability;}
   case 0x5f5710:return integer(state,0x142fc68L);
   case 0x5f7810:{int count=integer(state,0x142fc88L);return integer(state,0x142fc90L)<<3|count;}
   case 0x6394a0:return integer(state,0x1430430L)-1;
   case 0x672640:return bool(state,0x1432090L);
   case 0x678e50:return integer(state,0x1430158L);
   case 0x6cc380:return integer(state,0x14332d0L);
   case 0x6eebb0:return bool(state,0x1433610L);
   case 0x70a7a0:{int data=bool(state,0x1433990L);if(bool(state,0x14339a0L)!=0)data|=4;if(bool(state,0x14339a8L)!=0)data|=8;return data;}
   case 0x711f40:{int count=integer(state,0x1433e18L);return integer(state,0x1433e20L)<<2|count;}
   case 0x774820:{int data=bool(state,0x1434240L);if(bool(state,0x1434248L)!=0)data|=2;if(bool(state,0x1434230L)!=0)data|=4;if(bool(state,0x1434238L)!=0)data|=8;return data;}
   case 0x7835b0:return integer(state,0x1434630L);
   case 0x794740:{int wood=integer(state,0x1434710L);return bool(state,0x1434718L)<<3|wood;}
   default:throw new IllegalArgumentException("Untranslated scalar state encoder: "+Integer.toHexString(method));
  }
 }
 public static <S> S decode(int method,int data,DecodeAccess<S> access){
  S state=access.defaultState();
  switch(method){
   case 0x91eb0:return state;
   case 0x40150:state=access.with(state,0x1409750L,data&3);return access.with(state,0x1409758L,(data>>>3)&1);
   case 0x44070:return access.with(state,0x1409798L,data);
   case 0xe4be0:state=access.with(state,0x140bcc0L,(data&1)!=0);state=access.with(state,0x140bcc8L,((data>>>1)&1)!=0);return access.with(state,0x140bcd0L,((data>>>2)&1)!=0);
   case 0xead10:return access.with(state,0x140bd40L,data!=0);
   case 0x159720:return access.with(state,0x140fa70L,data);
   case 0x15a200:return access.with(state,0x140fa98L,data);
   case 0x16d280:state=access.with(state,0x140fee0L,data%8);return access.with(state,0x140fee8L,data/8);
   case 0x17dbf0:return access.with(state,0x140ffb8L,data);
   case 0x1b3e60:return access.with(state,0x14107b8L,data%9);
   case 0x1c6970:return access.with(state,0x14133a8L,data);
   case 0x1c72d0:state=access.with(state,0x1413560L,data&7);return access.with(state,0x1413568L,((data>>>3)&1)!=0);
   case 0x1ca470:state=access.with(state,0x1413588L,data&7);return access.with(state,0x1413590L,(data>>>3)&1);
   case 0x1cba00:state=access.with(state,0x1413598L,(data>>>1)&1);state=access.with(state,0x14135a0L,(data>>>2)&3);return access.with(state,0x1413768L,(data&1)!=0);
   case 0x1ec100:return access.with(state,access.virtualProperty(),data);
   case 0x1f6720:return access.with(state,0x1413bd0L,data);
   case 0x2718e0:return access.with(state,0x1415928L,data);
   case 0x2a4630:return access.with(state,0x1416230L,data&7);
   case 0x2b1230:return access.with(state,0x1416430L,data);
   case 0x31cb00:{int value=data<=3?data:3;if(data<0)value=0;return access.with(state,0x1416a10L,value);}
   case 0x3c3b10:return access.with(state,0x14181a8L,data>0);
   case 0x3c5d50:return access.with(state,0x14182a8L,data);
   case 0x3ddef0:return access.with(state,0x14183a0L,data!=0);
   case 0x41c4b0:return access.with(state,0x14188e0L,data);
   case 0x4b94d0:return access.with(state,0x1419938L,data);
   case 0x58b820:return access.with(state,0x142ecd8L,data==1);
   case 0x5b42e0:return access.with(state,0x142f6d0L,data);
   case 0x5cae90:state=access.with(state,0x142fbc0L,data&3);return access.with(state,0x142fbc8L,((data>>>3)&1)!=0);
   case 0x5f5670:return access.with(state,0x142fc68L,data);
   case 0x5f7a00:state=access.with(state,0x142fc88L,data&7);return access.with(state,0x142fc90L,(data>>>3)&1);
   case 0x6393a0:return access.with(state,0x1430430L,(data&7)+1);
   case 0x6725a0:return access.with(state,0x1432090L,(data&1)!=0);
   case 0x678db0:return access.with(state,0x1430158L,data);
   case 0x6cc2e0:return access.with(state,0x14332d0L,data&3);
   case 0x6eeb10:return access.with(state,0x1433610L,(data&1)!=0);
   case 0x70a650:state=access.with(state,0x1433990L,(data&1)!=0);state=access.with(state,0x14339a0L,((data>>>2)&1)!=0);return access.with(state,0x14339a8L,((data>>>3)&1)!=0);
   case 0x711fd0:state=access.with(state,0x1433e18L,data&3);return access.with(state,0x1433e20L,(data>>>2)&3);
   case 0x774680:state=access.with(state,0x1434240L,(data&1)!=0);state=access.with(state,0x1434248L,((data>>>1)&1)!=0);state=access.with(state,0x1434230L,((data>>>2)&1)!=0);return access.with(state,0x1434238L,((data>>>3)&1)!=0);
   case 0x783510:return access.with(state,0x1434630L,data);
   case 0x7947e0:state=access.with(state,0x1434710L,data&7);return access.with(state,0x1434718L,((data>>>3)&1)!=0);
   default:throw new IllegalArgumentException("Untranslated scalar state decoder: "+Integer.toHexString(method));
  }
 }
}
