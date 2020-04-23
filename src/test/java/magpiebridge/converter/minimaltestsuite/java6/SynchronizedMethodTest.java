package magpiebridge.converter.minimaltestsuite.java6;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import magpiebridge.converter.minimaltestsuite.MinimalTestSuiteBase;
import org.junit.Test;
import soot.SootMethod;

public class SynchronizedMethodTest extends MinimalTestSuiteBase {

  public String getMethodSignature() {
    return identifierFactory.getMethodSignature(
        "run", getDeclaredClassSignature(), "void", Collections.emptyList());
  }

  @Override
  @Test
  public void defaultTest() {
    SootMethod method = loadMethod(getMethodSignature());
    assertJimpleStmts(method, expectedBodyStmts());
    assertTrue(method.isSynchronized());
  }

  @Override
  public List<String> expectedBodyStmts() {
    return Stream.of(
            "r0 := @this: SynchronizedMethod",
            "$r1 = <java.lang.System: java.io.PrintStream out>",
            "virtualinvoke $r1.<java.io.PrintStream: void println(java.lang.String)>(\"test\")",
            "return")
        .collect(Collectors.toCollection(ArrayList::new));
  }
}
