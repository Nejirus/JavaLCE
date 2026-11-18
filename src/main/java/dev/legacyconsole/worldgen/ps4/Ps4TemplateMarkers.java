package dev.legacyconsole.worldgen.ps4;

import java.util.ArrayList;
import java.util.List;

/** StructureTemplate marker-map ordering, mcps4 0x6feac0/0x6febd0/0x6fedc0.
 * The native linked list groups hash buckets and is rebuilt on rehash. */
public final class Ps4TemplateMarkers {
 public record Marker(int x,int y,int z,String metadata){}
 private static final class Node {
  final Marker marker;Node previous,next;
  Node(Marker marker){this.marker=marker;}
 }
 private final Node head=new Node(null);
 private Node[] first=new Node[8],last=new Node[8];
 private int size;
 private Ps4TemplateMarkers(){head.previous=head;head.next=head;}
 private static long hash(Marker marker){return (long)marker.x+31L*marker.y+961L*marker.z;}
 private static boolean samePosition(Marker first,Marker second){return first.x==second.x&&first.y==second.y&&first.z==second.z;}
 private static void remove(Node node){node.previous.next=node.next;node.next.previous=node.previous;}
 private static void before(Node position,Node node){node.previous=position.previous;node.next=position;position.previous.next=node;position.previous=node;}
 private void insert(Marker marker){Node node=new Node(marker);before(head.next,node);size++;reinsert(node);}

 private void reinsert(Node node){
  int bucket=(int)(hash(node.marker)&(first.length-1));Node position=head;
  if(first[bucket]!=null){
   Node existing=last[bucket];
   while(true){
    if(samePosition(node.marker,existing.marker)){remove(node);size--;return;}
    if(existing==first[bucket])break;existing=existing.previous;
   }
   position=first[bucket];
  }
  if(node.next!=position){remove(node);before(position,node);}
  if(first[bucket]==null)last[bucket]=node;first[bucket]=node;
  if((float)size/(float)first.length>1f){
   Node stop=head.previous;int capacity=first.length*(first.length<512?8:2);
   first=new Node[capacity];last=new Node[capacity];
   while(true){Node current=head.next;reinsert(current);if(current==stop)break;}
  }
 }

 /** Entries already have their original transformed world positions. The first
  * value for a duplicate position is retained, as in the native insertion. */
 public static List<Marker> order(List<Marker> markers){
  Ps4TemplateMarkers map=new Ps4TemplateMarkers();for(Marker marker:markers)map.insert(marker);
  List<Marker> result=new ArrayList<>(map.size);for(Node node=map.head.next;node!=map.head;node=node.next)result.add(node.marker);return result;
 }
}
