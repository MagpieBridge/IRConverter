package magpiebridge.converter.integration;

import static org.junit.Assert.assertEquals;

import java.util.Collections;
import magpiebridge.converter.WalaOptions;
import magpiebridge.converter.WalaToSootIRConverter;
import org.junit.Test;
import soot.Scene;

public class WalaOptionsTest {

  @Test
  public void testAllowPhantomClass() {
    String testSourcePath = "src/test/resources/integration/testcase1/src";
    String testLibPath = "src/test/resources/integration/testcase1/mylib-0.0.1.jar";
    WalaToSootIRConverter converter =
        new WalaToSootIRConverter(
            Collections.singleton(testSourcePath), Collections.singleton(testLibPath));
    WalaOptions walaOptions = new WalaOptions();
    walaOptions.setAllowPhantomClass(true);
    converter.setWalaOptions(walaOptions);
    converter.convert();
    assertEquals(Scene.v().getApplicationClasses().size(), 2);
  }
}
