package dev.legacyconsole.client.ui;

import com.google.gson.Gson;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/** Display-list data exported from the pinned mcps4 media archive. */
final class Ps4Information {
  static final Ps4Information DATA;
  static {
    try(var r=new InputStreamReader(Ps4Information.class.getResourceAsStream(
        "/assets/legacyconsole/ps4_information.json"),StandardCharsets.UTF_8)){
      DATA=new Gson().fromJson(r,Ps4Information.class);
    }catch(Exception e){throw new ExceptionInInitializerError(e);}
  }
  Map<String,String> effect_icons;
  Map<String,Integer> effect_order;
  Map<String,float[]> normal,small,tile,inventory_anchors,cursor,cursor_small;
  float[] inventory_offset,selected_fade;
  Map<String,Ps4UiArt.Label> effect_labels;
  Ps4UiArt.Label selected_label;
}
