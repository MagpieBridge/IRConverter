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
public class VariableShadowingTest extends MinimalTestSuiteBase {
  @Override
  public String getMethodSignature() {
    return identifierFactory.getMethodSignature(
        "variableShadowing", getDeclaredClassSignature(), "void", Collections.emptyList());
  }

  @Override
  public List<String> expectedBodyStmts() {
    return Stream.of(
            "r0 := @this: VariableShadowing",
            "$i0 = r0.<VariableShadowing: int num>",
            "$i1 = 10",
            "return")
        .collect(Collectors.toList());
  }
}
