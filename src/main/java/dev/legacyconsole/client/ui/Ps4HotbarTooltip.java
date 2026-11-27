package dev.legacyconsole.client.ui;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;

/** Translates 0x3bb630's attribute selection and 0x3cda0's number formatting. */
public final class Ps4HotbarTooltip {
  private Ps4HotbarTooltip() {}
  private static final Map<String,Integer> ATTRIBUTES = Map.ofEntries(
      Map.entry("attribute.name.generic.max_health",0),
      Map.entry("attribute.name.generic.follow_range",1),
      Map.entry("attribute.name.generic.knockback_resistance",2),
      Map.entry("attribute.name.generic.movement_speed",3),
      Map.entry("attribute.name.generic.attack_damage",4),
      Map.entry("attribute.name.horse.jump_strength",5),
      Map.entry("attribute.name.zombie.spawn_reinforcements",6),
      Map.entry("attribute.name.generic.attack_speed",7),
      Map.entry("attribute.name.generic.armor",8),
      Map.entry("attribute.name.generic.armor_toughness",9),
      Map.entry("attribute.name.generic.luck",10));

  private static TranslatableComponent translation(Component component, String prefix) {
    if (component instanceof TranslatableComponent text && text.getKey().startsWith(prefix))
      return text;
    for (Component sibling : component.getSiblings()) {
      var found = translation(sibling, prefix);
      if (found != null) return found;
    }
    return null;
  }

  public static List<Component> lines(List<Component> tooltip) {
    var lines = new ArrayList<Component>();
    for (Component line : tooltip) {
      if (translation(line, "item.modifiers.") != null) continue;
      var modifier = translation(line, "attribute.modifier.");
      if (modifier != null) line = attribute(modifier);
      if (line != null && !line.getString().isBlank()) lines.add(line);
    }
    // 0xc78a90 replaces entry five with an ellipsis when five or more remain.
    if (lines.size() >= Ps4InformationLayout.MAX_SELECTED_LINES) {
      lines.subList(4, lines.size()).clear();
      lines.add(new TextComponent("..."));
    }
    return List.copyOf(lines);
  }

  private static Component attribute(TranslatableComponent text) {
    Object[] args = text.getArgs();
    if (args.length < 2 || !(args[1] instanceof TranslatableComponent name)) return null;
    Integer id = ATTRIBUTES.get(name.getKey());
    // The original selects IDs below seven, plus armor (eight).
    if (id == null || !(id < 7 || id == 8)) return null;
    double value;
    try {
      value = Double.parseDouble(String.valueOf(args[0]));
    } catch (NumberFormatException invalid) {
      return null;
    }
    if (!Double.isFinite(value)) return null;
    if (text.getKey().startsWith("attribute.modifier.take.")) value = -Math.abs(value);
    boolean percentage = !text.getKey().endsWith(".0");
    String sign = value > 0 ? "+" : value < 0 ? "-" : "";
    String number = percentage ? (int)Math.abs(value) + "%"
        : new BigDecimal(Math.abs(value)).setScale(1, RoundingMode.HALF_EVEN).toPlainString();
    return new TextComponent(sign + number + " ").append(name.copy())
        .withStyle(value > 0 ? ChatFormatting.BLUE : value < 0 ? ChatFormatting.RED : ChatFormatting.WHITE);
  }
}
