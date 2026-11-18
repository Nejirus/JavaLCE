package dev.legacyconsole.client;
import static org.junit.Assert.*;
import org.junit.*;
import org.junit.rules.TemporaryFolder;
import java.nio.file.*;
public class ShieldDefaultTest {
 @Rule public TemporaryFolder temporary=new TemporaryFolder();
 @Test public void oldProfilesGetTheRequestedDefaultOnceAndLaterOptOutPersists() throws Exception {
  var directory=temporary.newFolder();var config=directory.toPath().resolve("legacyconsole-client.cfg");
  Files.writeString(config,"controller.crouchShield=false\n");ClientConfig.load(directory);assertTrue(ClientConfig.crouchShield);
  ClientConfig.crouchShield=false;ClientConfig.save();ClientConfig.load(directory);assertFalse(ClientConfig.crouchShield);
  ClientConfig.crouchShield=true;
 }
}
