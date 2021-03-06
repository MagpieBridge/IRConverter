/** @author: Hasitha Rajapakse */
package magpiebridge.converter.minimaltestsuite.java6;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import magpiebridge.converter.categories.Java8Test;
import magpiebridge.converter.minimaltestsuite.MinimalTestSuiteBase;
import org.junit.experimental.categories.Category;

@Category(Java8Test.class)
public class StringConcatenationTest extends MinimalTestSuiteBase {

  @Override
  public String getMethodSignature() {
    return identifierFactory.getMethodSignature(
        "stringConcatenation", getDeclaredClassSignature(), "void", Collections.emptyList());
  }

  @Override
  public List<String> expectedBodyStmts() {
    return Stream.of(
            "r0 := @this: StringConcatenation",
            "$r2 = new java.lang.StringBuilder",
            "specialinvoke $r2.<java.lang.StringBuilder: void <init>(java.lang.String)>(\"the\")",
            "$r3 = virtualinvoke $r2.<java.lang.StringBuilder: java.lang.StringBuilder append(java.lang.String)>(\"string\")",
            "$r1 = virtualinvoke $r3.<java.lang.StringBuilder: java.lang.StringBuilder toString()>()",
            "return")
        .collect(Collectors.toList());
  }
}
