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
public class DoWhileLoopTest extends MinimalTestSuiteBase {
  // TODO extends MinimalTestSuiteBase
  @Override
  public String getMethodSignature() {
    return identifierFactory.getMethodSignature(
        "doWhileLoop", getDeclaredClassSignature(), "void", Collections.emptyList());
  }

  @Override
  public List<String> expectedBodyStmts() {
    return Stream.of(
            "r0 := @this: DoWhileLoop",
            "$i0 = 10",
            "$i1 = 0",
            "label1:",
            "$i2 = $i1",
            "$i3 = $i1 + 1",
            "$i1 = $i3",
            "$z0 = $i0 > $i1",
            "if $z0 != 0 goto label1",
            "return")
        .collect(Collectors.toList());
  }
}
