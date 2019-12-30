package magpiebridge.converter.minimaltestsuite.java6;

import static org.junit.Assert.assertTrue;

import magpiebridge.converter.minimaltestsuite.MinimalTestSuiteBase;
import org.junit.Ignore;
import org.junit.Test;
import soot.Modifier;
import soot.SootClass;

public class AnnotationLibraryTest extends MinimalTestSuiteBase {

  @Test
  public void defaultTest() {}

  @Ignore
  public void testAnnotation() {
    System.out.println(getDeclaredClassSignature());
    SootClass sootClass = loadClass(getDeclaredClassSignature());
    assertTrue(Modifier.isAnnotation(sootClass.getModifiers()));
  }
}
