package magpiebridge.converter.minimaltestsuite.java6;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import magpiebridge.converter.minimaltestsuite.MinimalTestSuiteBase;

/** @author Kaustubh Kelkar */
public class MethodOverridingTest extends MinimalTestSuiteBase {

  public String getMethodSignature() {

    return identifierFactory.getMethodSignature(
        "calculateArea", "MethodOverridingSubclass", "void", Collections.emptyList());
  }

  @Override
  public List<String> expectedBodyStmts() {
    return Stream.of(
            "r0 := @this: MethodOverridingSubclass",
            "$r1 = <java.lang.System: java.io.PrintStream out>",
            "virtualinvoke $r1.<java.io.PrintStream: void println(java.lang.String)>(\"Inside MethodOverridingSubclass-calculateArea()\")",
            "return")
        .collect(Collectors.toCollection(ArrayList::new));
  }
}
