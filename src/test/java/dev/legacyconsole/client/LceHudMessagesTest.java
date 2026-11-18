package dev.legacyconsole.client;

import static org.junit.Assert.*;
import org.junit.Test;
import net.minecraft.network.chat.*;

public class LceHudMessagesTest {
  @Test public void failedSleepAndOtherBedProblemsUseTheLceMessageHistoryPath() {
    for(String key:new String[]{"no_sleep","not_safe","occupied","too_far_away","obstructed"})
      assertTrue(key,LceHudMessages.isBedNotice(new TranslatableComponent("block.minecraft.bed."+key)));
  }
  @Test public void unrelatedActionBarAndNowPlayingMessagesStayOnTheirNativePath() {
    assertFalse(LceHudMessages.isBedNotice(new TranslatableComponent("record.nowPlaying",new TextComponent("Cat"))));
    assertFalse(LceHudMessages.isBedNotice(new TranslatableComponent("mount.onboard","Shift")));
    assertFalse(LceHudMessages.isBedNotice(new TextComponent("block.minecraft.bed.not_safe")));
    assertFalse(LceHudMessages.isBedNotice(TextComponent.EMPTY));
  }
  @Test public void vanillaMultiplayerSleepStatusAlsoUsesVisibleChatWhenTheServerDoesNotHaveTheMod() {
    assertTrue(LceHudMessages.isBedNotice(new TranslatableComponent("sleep.skipping_night")));
    assertTrue(LceHudMessages.isBedNotice(new TranslatableComponent("sleep.players_sleeping",1,2)));
  }
  @Test public void normalChatCannotBeMisidentifiedFromAnArgumentOrLiteralContent() {
    assertFalse(LceHudMessages.isBedNotice(new TranslatableComponent("chat.type.text",
        new TextComponent("Player"),new TranslatableComponent("block.minecraft.bed.no_sleep"))));
    assertFalse(LceHudMessages.isBedNotice(new TextComponent("You can only sleep at night")));
  }
}
