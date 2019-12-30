package magpiebridge.converter.minimaltestsuite.java6;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import magpiebridge.converter.minimaltestsuite.MinimalTestSuiteBase;

/** @author Kaustubh Kelkar */
public class SuperClassTest extends MinimalTestSuiteBase {
  public String getMethodSignature() {
    return identifierFactory.getMethodSignature(
        "superclassMethod", getDeclaredClassSignature(), "void", Collections.emptyList());
  }

  @Override
  public List<String> expectedBodyStmts() {
    return Stream.of(
            "r0 := @this: SuperClass",
            "r0.<SuperClass: int a> = 10",
            "r0.<SuperClass: int b> = 20",
            "r0.<SuperClass: int c> = 30",
            "r0.<SuperClass: int d> = 40",
            "return")
        .collect(Collectors.toCollection(ArrayList::new));
  }
}
