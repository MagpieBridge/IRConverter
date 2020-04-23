package magpiebridge.converter.minimaltestsuite.java6;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import magpiebridge.converter.minimaltestsuite.MinimalTestSuiteBase;

public class SynchronizedBlockTest extends MinimalTestSuiteBase {

  public String getMethodSignature() {
    return identifierFactory.getMethodSignature(
        "run", getDeclaredClassSignature(), "void", Collections.emptyList());
  }

  @Override
  public List<String> expectedBodyStmts() {
    return Stream.of(
            "r0 := @this: SynchronizedBlock",
            "$r1 = r0.<SynchronizedBlock: Sender sender>",
            "entermonitor $r1",
            "$r2 = r0.<SynchronizedBlock: Sender sender>",
            "$r3 = r0.<SynchronizedBlock: java.lang.String msg>",
            "virtualinvoke $r2.<Sender: void send(java.lang.String)>($r3)",
            "goto label1",
            "$r4 := @caughtexception",
            "exitmonitor $r1",
            "throw $r4",
            "label1:",
            "exitmonitor $r1",
            "return")
        .collect(Collectors.toCollection(ArrayList::new));
  }
}
