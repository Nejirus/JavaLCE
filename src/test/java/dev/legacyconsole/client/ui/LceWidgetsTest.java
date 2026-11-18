package dev.legacyconsole.client.ui;

import static org.junit.Assert.*;

import org.junit.Test;

public class LceWidgetsTest {
  @Test public void binaryPresentationKeepsCaptionStateAndLockedStatus() {
    LceWidgets.Toggle enabled = LceWidgets.toggle("\u00a7eHost Privileges: On (Locked)");
    assertEquals("Host Privileges (Locked)", enabled.label);
    assertTrue(enabled.checked);
    LceWidgets.Toggle disabled = LceWidgets.toggle("Fire Spreads: false");
    assertEquals("Fire Spreads", disabled.label);
    assertFalse(disabled.checked);
    assertNull(LceWidgets.toggle("Old Lighting: Default"));
    assertNull(LceWidgets.toggle("Difficulty: Normal"));
    assertNull(LceWidgets.toggle("Graphics: Fancy"));
  }

  @Test public void arbitraryButtonNamesDoNotOptIntoBooleanRendering() {
    ConsoleButton world = new ConsoleButton(1, 0, 0, 220, 20, "My World: On");
    assertFalse(world.allowCheckbox);
  }
}
