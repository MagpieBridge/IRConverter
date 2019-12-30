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
public class WhileLoopTest extends MinimalTestSuiteBase {

  @Override
  public String getMethodSignature() {
    return identifierFactory.getMethodSignature(
        "whileLoop", getDeclaredClassSignature(), "void", Collections.emptyList());
  }

  @Override
  public List<String> expectedBodyStmts() {
    return Stream.of(
            "r0 := @this: WhileLoop",
            "$i0 = 10",
            "$i1 = 0",
            "label1:",
            "$z0 = $i0 > $i1",
            "if $z0 == 0 goto label2",
            "$i2 = $i0",
            "$i3 = $i0 - 1",
            "$i0 = $i3",
            "goto label1",
            "label2:",
            "return")
        .collect(Collectors.toList());
  }
}
