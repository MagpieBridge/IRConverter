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
public class IfElseStatementTest extends MinimalTestSuiteBase {

  @Override
  public String getMethodSignature() {
    return identifierFactory.getMethodSignature(
        "ifElseStatement", getDeclaredClassSignature(), "void", Collections.emptyList());
  }

  @Override
  public List<String> expectedBodyStmts() {
    return Stream.of(
            "r0 := @this: IfElseStatement",
            "$i0 = 10",
            "$i1 = 20",
            "$i2 = 30",
            "$i3 = 0",
            "$z0 = $i0 < $i1",
            "if $z0 == 0 goto label1",
            "$i3 = 1",
            "goto label3",
            "label1:",
            "$z1 = $i1 < $i2",
            "if $z1 == 0 goto label2",
            "$i3 = 2",
            "goto label3",
            "label2:",
            "$i3 = 3",
            "label3:",
            "return")
        .collect(Collectors.toList());
  }
}
