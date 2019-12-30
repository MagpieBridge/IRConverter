package magpiebridge.converter.minimaltestsuite.java6;

import static org.junit.Assert.assertTrue;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import magpiebridge.converter.minimaltestsuite.MinimalTestSuiteBase;
import org.junit.Ignore;
import org.junit.Test;
import soot.SootMethod;

public class NativeMethodTest extends MinimalTestSuiteBase {
  @Override
  public String getMethodSignature() {
    return identifierFactory.getMethodSignature(
        "returnResult", getDeclaredClassSignature(), "int", Collections.singletonList("int"));
  }

  @Test
  public void defaultTest() {
    /**
     * Can not pass assertJimpleStmts() as body for native method is empty and current check does nt
     * allow that
     */
  }

  @Ignore
  public void ignoreTest() {

    SootMethod method = loadMethod(getMethodSignature());
    assertJimpleStmts(method, expectedBodyStmts());
    assertTrue(method.isNative());
  }

  @Override
  public List<String> expectedBodyStmts() {
    return Stream.of("r0 := @this: NativeMethod", "$r1 = null", "return")
        .collect(Collectors.toList());
  }
}
