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
public class ForLoopTest extends MinimalTestSuiteBase {

  @Override
  public String getMethodSignature() {
    return identifierFactory.getMethodSignature(
        "forLoop", getDeclaredClassSignature(), "void", Collections.emptyList());
  }

  @Override
  public List<String> expectedBodyStmts() {
    return Stream.of(
            "r0 := @this: ForLoop",
            "$i0 = 10",
            "$i1 = 0",
            "$i2 = 0",
            "label1:",
            "$z0 = $i2 < $i0",
            "if $z0 == 0 goto label2",
            "$i3 = $i1",
            "$i4 = $i1 + 1",
            "$i1 = $i4",
            "$i5 = $i2",
            "$i6 = $i2 + 1",
            "$i2 = $i6",
            "goto label1",
            "label2:",
            "return")
        .collect(Collectors.toList());
  }
}
