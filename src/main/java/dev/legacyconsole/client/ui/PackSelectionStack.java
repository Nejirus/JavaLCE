package dev.legacyconsole.client.ui;

import java.util.*;

/** Native repository order: earlier selections below later selections. */
final class PackSelectionStack {
 private final List<String> ids=new ArrayList<>();
 PackSelectionStack(Collection<String> selected){for(String id:selected)if(!ids.contains(id))ids.add(id);}
 boolean contains(String id){return ids.contains(id);}
 int priority(String id){return ids.indexOf(id)+1;}
 List<String> ids(){return List.copyOf(ids);}
 void toggle(String id){if(!ids.remove(id))ids.add(id);}
 void clear(){ids.clear();}
}
