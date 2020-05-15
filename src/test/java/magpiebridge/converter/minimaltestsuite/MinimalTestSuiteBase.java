package magpiebridge.converter.minimaltestsuite;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import magpiebridge.converter.IdentifierFactory;
import magpiebridge.converter.Utils;
import magpiebridge.converter.WalaToSootIRConverter;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
import soot.Body;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;

public abstract class MinimalTestSuiteBase {

  static final String baseDir = "src/test/resources/minimaltestsuite/";
  protected IdentifierFactory identifierFactory = IdentifierFactory.getInstance();

  @ClassRule public static CustomTestWatcher customTestWatcher = new CustomTestWatcher();

  public static class CustomTestWatcher extends TestWatcher {
    private String classPath;
    private boolean firstTime = true;

    /** Load WalaJavaClassProvider once */
    @Override
    protected void starting(Description description) {
      this.classPath = description.getClassName();
      if (firstTime) {
        Set<String> sourcePathSet =
            new HashSet<String>(
                Arrays.asList(baseDir + "java6", baseDir + "java7", baseDir + "java8"));
        WalaToSootIRConverter converter = new WalaToSootIRConverter(sourcePathSet);
        converter.convert();
        firstTime = false;
      }
    }
  }

  @Test
  public void defaultTest() {
    SootMethod method = loadMethod(getMethodSignature());
    assertJimpleStmts(method, expectedBodyStmts());
  }

  public void assertJimpleStmts(SootMethod method, List<String> expectedStmts) {
    Body body = method.getActiveBody();
    Utils.print(body, false);
    assertNotNull(body);
    List<String> actualStmts = Utils.bodyStmtsAsStrings(body);
    assertEquals(expectedStmts, actualStmts);
  }

  public SootClass loadClass(String className) {
    return Scene.v().getSootClass(className);
  }

  public SootMethod loadMethod(String methodSigature) {
    return Scene.v().getMethod(methodSigature);
  }

  /**
   * @returns the name of the class - assuming the testname unit has "Test" appended to the
   *     respective name of the class
   */
  public String getClassName(String classPath) {
    String[] classPathArray = classPath.split("\\.");
    String className =
        classPathArray[classPathArray.length - 1].substring(
            0, classPathArray[classPathArray.length - 1].length() - 4);
    return className;
  }

  public String getDeclaredClassSignature() {
    return getClassName(customTestWatcher.classPath);
  }

  public String getMethodSignature() {
    fail("getMethodSignature() is used but not overridden");
    return null;
  }

  public List<String> expectedBodyStmts() {
    fail("expectedBodyStmts() is used but not overridden");
    return null;
  }

  public List<String> expectedBodyStmts(String... jimpleLines) {
    return Stream.of(jimpleLines).collect(Collectors.toList());
  }
}
