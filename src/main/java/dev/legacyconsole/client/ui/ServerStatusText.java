package dev.legacyconsole.client.ui;

import net.minecraft.network.chat.Component;

/** Server-provided text is a Component; Object.toString() is its diagnostic representation. */
public final class ServerStatusText {
  private ServerStatusText() {}
  public static String label(String label,Component value) {
    String text=value==null?"":value.getString();
    return label+": "+(text.isBlank()?"...":text);
  }
}
