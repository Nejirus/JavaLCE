package dev.legacyconsole.worldgen.ps4;

import java.util.function.IntConsumer;

/** Original compressed-section emptiness routine (0x35a090), operating on the
 * native numeric header/payload layout. Its locking wrapper is separate. */
public final class Ps4CompressedSections {
 public interface Access {
  boolean allocated(int sectionIndex);
  int sectionOffset(int sectionIndex);
  int unsignedByte(int byteOffset);
  default int unsignedShort(int byteOffset){return unsignedByte(byteOffset)|(unsignedByte(byteOffset+1)<<8);}
 }
 private static final int[] OFFSETS_0={0,3,6,10,16,0,0,32},OFFSETS_1000={0,5,10,16,24,0,0,64};
 private Ps4CompressedSections(){}
 /** Original raw packed lookup (0x35bc60). Its optional byte output is 1
  * for an unallocated section or constant header and 0 for other modes.
  * The separate 0x224540 wrapper masks the returned value with 0x7fff. */
 public static int value(Access storage,int x,int y,int z,IntConsumer constant){
  int section=y>>>4;
  if(!storage.allocated(section)){if(constant!=null)constant.accept(1);return 0;}
  int base=(storage.sectionOffset(section)&65535)<<8,index=((x&12)<<2)|(z&12)|((y>>>2)&3),header=storage.unsignedShort(base+index*2),mode=header>>>13;
  if(mode==0){if(constant!=null)constant.accept(1);return header<0x2000?header&4095:0;}
  int payload=base+128+payloadOffset(storage,base,index,header)*4,cell=((x<<4)&48)|(y&3)|((z&3)<<2),result;
  if(mode==7)result=storage.unsignedShort(payload+cell*2);
  else{
   int mask=128>>>(cell&7),byteIndex=cell>>>3,planes=payload+(1<<mode)*2,selected=0;
   for(int plane=0;plane<mode;plane++)if((storage.unsignedByte(planes+((plane<<3)|byteIndex))&mask)!=0)selected|=1<<plane;
   result=storage.unsignedShort(payload+selected*2);
  }
  if(constant!=null)constant.accept(0);return result;
 }
 /** Pure stored secondary lookup (0x35bf10), before the wrapper's primary
  * high-bit shortcut. Both layers share the same palette. */
 public static int storedSecondary(Access storage,int x,int y,int z){
  int section=y>>>4;if(!storage.allocated(section))return 0;
  int base=(storage.sectionOffset(section)&65535)<<8,index=((x&12)<<2)|(z&12)|((y>>>2)&3),header=storage.unsignedShort(base+index*2),mode=header>>>13;
  if((header&0x1000)==0||mode==0)return 0;
  int payload=base+128+payloadOffset(storage,base,index,header)*4,cell=((x<<4)&48)|(y&3)|((z&3)<<2);
  if(mode==7)return storage.unsignedShort(payload+128+cell*2);
  int mask=128>>>(cell&7),byteIndex=cell>>>3,planes=payload+(1<<mode)*2+mode*8,selected=0;
  for(int plane=0;plane<mode;plane++)if((storage.unsignedByte(planes+((plane<<3)|byteIndex))&mask)!=0)selected|=1<<plane;
  return storage.unsignedShort(payload+selected*2);
 }
 /** Original 0x224630 value selection with the platform lock boundary removed. */
 public static int secondary(Access storage,int x,int y,int z){return (value(storage,x,y,z,null)&0x8000)!=0?0x90:storedSecondary(storage,x,y,z);}
 /** Original 0x224540 return mask, after the pure primary lookup. */
 public static int primary(Access storage,int x,int y,int z){return value(storage,x,y,z,null)&0x7fff;}
 /** Empty sections leave the optional output untouched. A nonempty result
  * reports the original four-block slice's upper Y, rather than a voxel Y. */
 public static boolean empty(Access storage,int sectionY,IntConsumer nonEmptyTop){
  int section=sectionY/16;if(!storage.allocated(section))return true;
  int base=(storage.sectionOffset(section)&65535)<<8;
  for(int slice=3;slice>=0;slice--)for(int column=0;column<16;column++){
   int index=(column<<2)|slice,header=storage.unsignedShort(base+index*2),mode=header>>>13;
   boolean nonempty=false;
   if(mode==0)nonempty=(header&4095)!=0;
   else{
    int payload=base+128+payloadOffset(storage,base,index,header)*4;
    if(mode==7){for(int i=0;i<64;i++)if(storage.unsignedShort(payload+i*2)!=0){nonempty=true;break;}}
    else{
     int paletteSize=1<<mode,planes=payload+paletteSize*2,selected=0;
     for(int plane=0;plane<mode;plane++){
      int any=0,all=255;
      for(int i=0;i<8;i++){int value=storage.unsignedByte(planes+plane*8+i);any|=value;all&=value;}
      if(any!=0&&all!=255){nonempty=true;break;}
     }
     if(!nonempty){
      for(int plane=0;plane<mode;plane++)if(storage.unsignedByte(planes+plane*8)==255)selected|=1<<plane;
      nonempty=storage.unsignedShort(payload+selected*2)!=0;
     }
    }
   }
   if(nonempty){if(nonEmptyTop!=null)nonEmptyTop.accept(sectionY+4+slice*4);return false;}
  }
  return true;
 }
 private static int payloadOffset(Access storage,int base,int index,int header){
  if(index==0)return 0;
  if(header>=0x2000)return header&4095;
  // Preserve the original generic decoder's preceding-header paths. The
  // current caller reaches this helper only for nonzero modes (>=0x2000).
  int previous=index-1;
  while(previous>0){int value=storage.unsignedShort(base+previous*2);if((value>>>13)!=0)return ((value&4095)+offsetAfter(value))&65535;previous--;}
  return offsetAfter(storage.unsignedShort(base));
 }
 private static int offsetAfter(int header){return ((header&0x1000)==0?OFFSETS_0:OFFSETS_1000)[header>>>13];}
}
