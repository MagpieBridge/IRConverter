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
public class LabelStatementTest extends MinimalTestSuiteBase {

  @Override
  public String getMethodSignature() {
    return identifierFactory.getMethodSignature(
        "labelStatement", getDeclaredClassSignature(), "void", Collections.emptyList());
  }

  @Override
  public List<String> expectedBodyStmts() {
    return Stream.of(
            "r0 := @this: LabelStatement",
            "$i0 = 20",
            "$i1 = 1",
            "label1:",
            "$z0 = $i1 < $i0",
            "if $z0 == 0 goto label3",
            "$i2 = $i1 % 10",
            "$z1 = $i2 == 0",
            "if $z1 == 0 goto label2",
            "goto label3",
            "label2:",
            "$i3 = $i1",
            "$i4 = $i1 + 1",
            "$i1 = $i4",
            "goto label1",
            "label3:",
            "return")
        .collect(Collectors.toList());
  }
}
