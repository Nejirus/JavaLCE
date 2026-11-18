package dev.legacyconsole.client;
import static org.junit.Assert.*;
import java.nio.file.Files;
import java.util.*;
import org.junit.Test;
public class Ps4MenuHintHistoryTest {
 @Test public void partialProgressAndCompletionSurviveAProfileReload()throws Exception{
  Set<String> records=new HashSet<>(Set.of("block.minecraft.stone","7138c68b"));var history=new Ps4MenuHintHistory(records);
  assertTrue(history.remember(1,2));assertFalse(history.remember(1,2));assertTrue(history.complete(4));
  var path=Files.createTempFile("legacy-hint-history-",".txt");try{Files.write(path,records);var loaded=new Ps4MenuHintHistory(new HashSet<>(Files.readAllLines(path)));assertEquals(2,loaded.progress(1));assertTrue(loaded.completed(4));assertFalse(loaded.completed(1));}finally{Files.delete(path);}
  assertTrue(records.contains("block.minecraft.stone"));assertTrue(records.contains("7138c68b"));
 }
 @Test public void progressUpdatesReplaceOnlyTheirOwnContext(){
  Set<String> records=new HashSet<>();var h=new Ps4MenuHintHistory(records);h.remember(1,2);h.remember(4,3);h.remember(1,4);
  assertEquals(4,h.progress(1));assertEquals(3,h.progress(4));assertEquals(2,records.size());assertFalse(records.contains("menu-progress:1:2"));
 }
 @Test public void malformedOlderRecordsDoNotHideNewHints(){
  var h=new Ps4MenuHintHistory(new HashSet<>(Set.of("menu-progress:1:bad","menu-progress:4:-2","generic_hint")));
  assertEquals(0,h.progress(1));assertEquals(0,h.progress(4));assertFalse(h.completed(1));assertFalse(h.remember(1,0));
 }
}
