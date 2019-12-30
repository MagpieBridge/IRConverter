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
public class AssertStatementTest extends MinimalTestSuiteBase {
  @Override
  public String getMethodSignature() {
    return identifierFactory.getMethodSignature(
        "assertStatement", getDeclaredClassSignature(), "void", Collections.emptyList());
  }

  @Override
  public List<String> expectedBodyStmts() {
    return Stream.of(
            "r0 := @this: AssertStatement",
            "$z0 = \"\" != null",
            "$z1 = <AssertStatement: boolean $assertionsDisabled>",
            "if $z1 == 1 goto label1",
            "if $z0 == 1 goto label1",
            "$r1 = new java.lang.AssertionError",
            "specialinvoke $r1.<java.lang.AssertionError: void <init>()>()",
            "throw $r1",
            "label1:",
            "nop",
            "return")
        .collect(Collectors.toList());
  }
}
