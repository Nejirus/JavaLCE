package dev.legacyconsole.client.ui;

import static org.junit.Assert.*;
import org.junit.Test;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.TextComponent;

public class ServerStatusTextTest {
  @Test public void versionRendersStyledSiblingsAsVisibleText() {
    var text=new TextComponent("1.17.1").withStyle(ChatFormatting.GREEN)
        .append(new TextComponent(" (Forge)").withStyle(ChatFormatting.GRAY));
    assertEquals("Version: 1.17.1 (Forge)",ServerStatusText.label("Version",text));
    assertTrue(text.getStyle().getColor()!=null);
  }
  @Test public void playerCountContainsNoComponentDiagnostics() {
    var text=new TextComponent("4").append(new TextComponent("/20"));
    assertEquals("Players: 4/20",ServerStatusText.label("Players",text));
  }
  @Test public void pendingAndEmptyStatusKeepThePlaceholder() {
    assertEquals("Version: ...",ServerStatusText.label("Version",null));
    assertEquals("Players: ...",ServerStatusText.label("Players",new TextComponent("")));
  }
}
