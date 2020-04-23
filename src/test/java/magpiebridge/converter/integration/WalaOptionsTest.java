package magpiebridge.converter.integration;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collections;
import magpiebridge.converter.WalaOptions;
import magpiebridge.converter.WalaToSootIRConverter;
import org.junit.Test;
import soot.Scene;

public class WalaOptionsTest {

  @Test
  public void testCustomizedWalaProperties() {
    String testSourcePath = "src/test/resources/integration/testcase1/src";
    String testLibPath = "src/test/resources/integration/testcase1/mylib-0.0.1.jar";
    WalaOptions walaOptions = new WalaOptions();
    walaOptions.setCustomizedWalaProperties("src/test/resources/wala.properties");
    File file = new File("src/test/resources/wala.properties");
    if (file.exists()) {
      try {
        BufferedReader reader = new BufferedReader(new FileReader(file));
        String line = reader.readLine();
        reader.close();
        if (!line.equals(
            "java_runtime_dir = "
                + new File(System.getProperty("java.home")).toString().replace("\\", "/"))) {
          file.delete();
          assertTrue(!file.exists());
        }
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }
    WalaToSootIRConverter converter =
        new WalaToSootIRConverter(
            Collections.singleton(testSourcePath),
            Collections.singleton(testLibPath),
            null,
            walaOptions);
    converter.convert();
    assertEquals(Scene.v().getApplicationClasses().size(), 2);
  }
}
