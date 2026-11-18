package dev.legacyconsole.client;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;

/** BedTile -> displayClientMessage -> Gui::addMessage in the LCE source. */
public final class LceHudMessages {
  private LceHudMessages() {}
  public static boolean isBedNotice(Component message) {
    return message instanceof TranslatableComponent translated
        &&(translated.getKey().startsWith("block.minecraft.bed.")||translated.getKey().startsWith("sleep."));
  }
}
