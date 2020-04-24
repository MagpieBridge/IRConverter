package magpiebridge.converter;

import com.ibm.wala.classLoader.Module;
import com.ibm.wala.classLoader.ModuleEntry;
import com.ibm.wala.types.ClassLoaderReference;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import org.junit.Assert;
import org.junit.Test;

public class WalaToSootIRConverterTest {

  /**
   * Tests whether WalaToSootIRConverter is able to load libraries from both .class files and .jar
   * files.
   */
  @Test
  public void test_loadClassAndJar() {
    final Path testPath = Paths.get("src/test/resources/libPathLoading");
    final Path srcPath = testPath.resolve("src");
    final Path libPath = testPath.resolve("lib");
    final Set<String> srcSet = Collections.singleton(srcPath.toAbsolutePath().toString());
    final Set<String> libSet = Collections.singleton(libPath.toAbsolutePath().toString());
    WalaToSootIRConverter irConverter = new WalaToSootIRConverter(srcSet, libSet);

    // Collect the names for the loaded class files
    Set<String> names = new HashSet<>();
    for (Module m : irConverter.scope.getModules(ClassLoaderReference.Primordial)) {
      Iterator<? extends ModuleEntry> entryIt = m.getEntries();
      while (entryIt.hasNext()) {
        names.add(entryIt.next().getName());
      }
    }

    // src/test/resources/libPathLoading/lib contains the bytecode for classes B, C, D, E
    // either on a .class file or inside a .jar . Consequently, we expect that all of them
    // were successfully loaded into the analysis scope.
    Set<String> expected = new HashSet<>();
    expected.add("B.class");
    expected.add("C.class");
    expected.add("D.class");
    expected.add("E.class");

    Assert.assertTrue(names.containsAll(expected));
  }
}
