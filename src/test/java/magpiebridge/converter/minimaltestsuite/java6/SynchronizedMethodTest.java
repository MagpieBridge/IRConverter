package magpiebridge.converter.minimaltestsuite.java6;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import magpiebridge.converter.minimaltestsuite.MinimalTestSuiteBase;

public class SynchronizedMethodTest extends MinimalTestSuiteBase {

  public String getMethodSignature() {
    return identifierFactory.getMethodSignature(
        "run", getDeclaredClassSignature(), "void", Collections.emptyList());
  }

  @Override
  public void defaultTest() {
    super.defaultTest();
    /** TODO assertTrue(method.isSynchronized()); */
  }

  @Override
  public List<String> expectedBodyStmts() {
    return Stream.of(
            "r0 := @this: SynchronizedMethod",
            "$r1 = r0.<SynchronizedMethod: SenderMethod sender>",
            "$r2 = r0.<SynchronizedMethod: java.lang.String msg>",
            "virtualinvoke $r1.<SenderMethod: void send(java.lang.String)>($r2)",
            "return")
        .collect(Collectors.toCollection(ArrayList::new));
  }
}
