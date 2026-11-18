package dev.legacyconsole.client;
import java.util.Set;
/** Regular-menu progress stored alongside the existing local one-time hint IDs. */
public final class Ps4MenuHintHistory {
 private final Set<String> records;
 public Ps4MenuHintHistory(Set<String> records){this.records=records;}
 public boolean completed(int context){return records.contains("menu:"+context);}
 public boolean complete(int context){return records.add("menu:"+context);}
 public int progress(int context){
  String prefix="menu-progress:"+context+":";int result=0;
  for(String line:records)if(line.startsWith(prefix))try{result=Math.max(result,Integer.parseInt(line.substring(prefix.length())));}catch(NumberFormatException ignored){}
  return result;
 }
 public boolean remember(int context,int index){
  index=Math.max(0,index);if(progress(context)==index)return false;
  String prefix="menu-progress:"+context+":";records.removeIf(line->line.startsWith(prefix));records.add(prefix+index);return true;
 }
}
