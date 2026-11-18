package dev.legacyconsole.tutorial;

import com.google.gson.*;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import org.junit.Test;
import static org.junit.Assert.*;

public class Ps4InfoInputTest {
 @Test public void infoMappingsMatchOriginalPs4Handler(){
  var data=new JsonParser().parse(new InputStreamReader(getClass().getResourceAsStream("/ps4/tutorial-map-239-native.json"),StandardCharsets.UTF_8)).getAsJsonObject();
  for(var row:data.getAsJsonArray("input_cases")){
   var v=row.getAsJsonObject();var task=new Ps4TutorialScript.Task();task.actions=new int[]{v.get("required").getAsInt()};var input=new Ps4InfoInput();
   if(v.get("active").getAsBoolean())input.press(task,v.get("pressed").getAsInt());
   assertEquals(v.toString(),v.get("completed").getAsBoolean(),input.completed(task,40));
  }
 }
 @Test public void squareIsRememberedBeforeMinimumTimeAndCannotSkipTheNextPanel(){
  var input=new Ps4InfoInput();int count=0;
  for(var task:Ps4TutorialScript.flatten(Ps4TutorialScript.SOURCE.states.get(2))){
   if(!task.kind.equals("InfoTask")||task.actions.length!=1||task.actions[0]!=2)continue;
   assertFalse(input.completed(task,100));assertFalse(input.press(task,0));assertFalse(input.completed(task,100));
   assertTrue(input.press(task,2));assertFalse(input.completed(task,39));assertTrue(input.completed(task,40));count++;
  }
  assertEquals(3,count);input.clear();
 }
}
