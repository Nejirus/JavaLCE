package dev.legacyconsole.worldgen.ps4;

import java.util.ArrayList;
import java.util.List;
import java.util.function.ToIntFunction;

/** StructureTemplate's original block sort (0x6ff170/0x6ff5e0/0x6ff840).
 * Signed packed X/Z comparisons ignore Y. Equal columns retain the native
 * partition/heap order, which differs from Java's stable comparator sort. */
public final class Ps4TemplateBlockOrder {
 private Ps4TemplateBlockOrder(){}
 public static int key(int x,int z){return (x<<16)|(z&65535);}
 public static <T> List<T> order(List<T> input,ToIntFunction<T> key){return order(input,key,input.size());}
 static <T> List<T> order(List<T> input,ToIntFunction<T> key,int ideal){List<T> result=new ArrayList<>(input);sort(result,key,0,result.size(),ideal);return result;}
 private static <T> void swap(List<T> a,int x,int y){T value=a.get(x);a.set(x,a.get(y));a.set(y,value);}
 private static <T> boolean less(List<T> a,ToIntFunction<T> key,int x,int y){return key.applyAsInt(a.get(x))<key.applyAsInt(a.get(y));}
 private static <T> void median3(List<T> a,ToIntFunction<T> key,int first,int middle,int last){
  if(less(a,key,middle,first))swap(a,middle,first);
  if(less(a,key,last,middle)){swap(a,last,middle);if(less(a,key,middle,first))swap(a,middle,first);}
 }
 private static <T> void median(List<T> a,ToIntFunction<T> key,int first,int middle,int last){
  if(last-first>40){int step=(last-first+1)>>>3;median3(a,key,first,first+step,first+2*step);median3(a,key,middle-step,middle,middle+step);median3(a,key,last-2*step,last-step,last);median3(a,key,first+step,middle,last-step);}
  else median3(a,key,first,middle,last);
 }
 private static <T> int[] partition(List<T> a,ToIntFunction<T> key,int first,int last){
  int middle=first+((last-first)>>>1);median(a,key,first,middle,last-1);
  int pfirst=middle,plast=middle+1;
  while(first<pfirst&&key.applyAsInt(a.get(pfirst-1))==key.applyAsInt(a.get(pfirst)))pfirst--;
  while(plast<last&&key.applyAsInt(a.get(plast))==key.applyAsInt(a.get(pfirst)))plast++;
  int gfirst=plast,glast=pfirst;
  while(true){
   for(;gfirst<last;gfirst++){
    int value=key.applyAsInt(a.get(gfirst)),pivot=key.applyAsInt(a.get(pfirst));
    if(value<pivot)break;
    if(value==pivot)swap(a,plast++,gfirst);
   }
   for(;first<glast;glast--){
    int value=key.applyAsInt(a.get(glast-1)),pivot=key.applyAsInt(a.get(pfirst));
    if(pivot<value)break;
    if(value==pivot)swap(a,--pfirst,glast-1);
   }
   if(gfirst==last&&glast==first)return new int[]{pfirst,plast};
   if(glast==first){if(gfirst!=plast)swap(a,pfirst,plast);plast++;swap(a,pfirst++,gfirst++);}
   else if(gfirst==last){swap(a,--glast,--pfirst);swap(a,pfirst,--plast);}
   else swap(a,gfirst++,--glast);
  }
 }
 private static <T> void sort(List<T> a,ToIntFunction<T> key,int first,int last,int ideal){
  while(last-first>32&&ideal>0){
   int[] middle=partition(a,key,first,last);ideal=(ideal>>>1)+(ideal>>>2);
   if(middle[0]-first<last-middle[1]){sort(a,key,first,middle[0],ideal);first=middle[1];}
   else{sort(a,key,middle[1],last,ideal);last=middle[0];}
  }
  if(last-first>32){heap(a,key,first,last);return;}
  for(int next=first+1;next<last;next++){
   T value=a.get(next);int valueKey=key.applyAsInt(value),hole=next;
   while(hole>first&&valueKey<key.applyAsInt(a.get(hole-1))){a.set(hole,a.get(hole-1));hole--;}
   a.set(hole,value);
  }
 }
 private static <T> void adjustHeap(List<T> a,ToIntFunction<T> key,int first,int hole,int size,T value){
  int top=hole,child=2*hole+2;
  while(child<size){if(less(a,key,first+child,first+child-1))child--;a.set(first+hole,a.get(first+child));hole=child;child=2*hole+2;}
  if(child==size){a.set(first+hole,a.get(first+size-1));hole=size-1;}
  while(hole>top){int parent=(hole-1)/2;if(key.applyAsInt(value)<=key.applyAsInt(a.get(first+parent)))break;a.set(first+hole,a.get(first+parent));hole=parent;}
  a.set(first+hole,value);
 }
 private static <T> void heap(List<T> a,ToIntFunction<T> key,int first,int last){
  int size=last-first;for(int hole=size/2-1;hole>=0;hole--)adjustHeap(a,key,first,hole,size,a.get(first+hole));
  while(size>1){T value=a.get(first+size-1);a.set(first+size-1,a.get(first));size--;adjustHeap(a,key,first,0,size,value);}
 }
}
