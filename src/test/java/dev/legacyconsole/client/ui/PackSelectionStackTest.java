package dev.legacyconsole.client.ui;
import static org.junit.Assert.*;
import java.util.*;
import org.junit.Test;
public class PackSelectionStackTest {
 @Test public void laterSelectionHasHigherPriority(){var s=new PackSelectionStack(List.of());s.toggle("alpha");s.toggle("beta");assertEquals(List.of("alpha","beta"),s.ids());assertEquals(1,s.priority("alpha"));assertEquals(2,s.priority("beta"));}
 @Test public void removingOnePackKeepsTheOther(){var s=new PackSelectionStack(List.of("alpha","beta"));s.toggle("alpha");assertEquals(List.of("beta"),s.ids());}
 @Test public void reselectingAPackPlacesItAboveExistingPacks(){var s=new PackSelectionStack(List.of("alpha","beta"));s.toggle("alpha");s.toggle("alpha");assertEquals(List.of("beta","alpha"),s.ids());}
 @Test public void reopeningRetainsSelectionOrderWithoutDuplicates(){var s=new PackSelectionStack(List.of("beta","alpha","beta"));assertEquals(List.of("beta","alpha"),s.ids());}
 @Test public void defaultClearsOnlyTheUserSelectionModel(){var s=new PackSelectionStack(List.of("alpha","beta"));s.clear();assertTrue(s.ids().isEmpty());assertFalse(s.contains("alpha"));}
}
