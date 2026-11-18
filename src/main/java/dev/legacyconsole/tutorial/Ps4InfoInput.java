package dev.legacyconsole.tutorial;

import java.util.HashSet;
import java.util.Set;

/** The completedMappings list in PS4 InfoTask, independent of its display timer. */
public final class Ps4InfoInput {
 private Ps4TutorialScript.Task active;
 private final Set<Integer> completed=new HashSet<>();
 public void clear(){active=null;completed.clear();}
 public boolean press(Ps4TutorialScript.Task task,int action){
  if(active!=task){clear();active=task;}
  for(int mapping:task.actions)if(mapping==action){completed.add(action);return true;}
  return false;
 }
 public boolean completed(Ps4TutorialScript.Task task,int elapsed){
  if(active!=task||elapsed<Ps4TutorialScript.MINIMUM_TICKS)return false;
  for(int mapping:task.actions)if(!completed.contains(mapping))return false;
  return true;
 }
}
