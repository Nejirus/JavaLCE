package dev.legacyconsole.worldgen.ps4;

import java.util.ArrayList;
import java.util.List;

/** Pointer-key set ordering from 0x4d07b0/0x4d0a10/0x4d0cc0.
 * Keys are explicit native pointer tokens; Java identity hashes are not used. */
public final class Ps4PointerSet {
 private static final class Node {final long key;Node previous,next;Node(long key){this.key=key;}}
 private final Node head=new Node(0);
 private Node[] first=new Node[8],last=new Node[8];
 private int size;
 public Ps4PointerSet(){head.previous=head;head.next=head;}
 public static long hash(long pointer){long hash=0xcbf29ce484222325L;for(int i=0;i<8;i++){hash^=(pointer>>>(i*8))&255;hash*=0x100000001b3L;}return hash^(hash>>>32);}
 private static void remove(Node node){node.previous.next=node.next;node.next.previous=node.previous;}
 private static void before(Node position,Node node){node.previous=position.previous;node.next=position;position.previous.next=node;position.previous=node;}
 public boolean add(long pointer){Node node=new Node(pointer);before(head.next,node);size++;return reinsert(node);}
 private boolean reinsert(Node node){
  int bucket=(int)(hash(node.key)&(first.length-1));Node position=head;
  if(first[bucket]!=null){Node existing=last[bucket];while(true){if(node.key==existing.key){remove(node);size--;return false;}if(existing==first[bucket])break;existing=existing.previous;}position=first[bucket];}
  if(node.next!=position){remove(node);before(position,node);}if(first[bucket]==null)last[bucket]=node;first[bucket]=node;
  if((float)size/(float)first.length>1f){Node stop=head.previous;int capacity=first.length*(first.length<512?8:2);first=new Node[capacity];last=new Node[capacity];while(true){Node current=head.next;reinsert(current);if(current==stop)break;}}
  return true;
 }
 public int size(){return size;}
 public int bucketCount(){return first.length;}
 public List<Long> values(){List<Long> result=new ArrayList<>(size);for(Node node=head.next;node!=head;node=node.next)result.add(node.key);return result;}
}
