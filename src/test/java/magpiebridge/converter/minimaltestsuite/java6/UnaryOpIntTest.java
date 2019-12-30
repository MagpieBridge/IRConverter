package magpiebridge.converter.minimaltestsuite.java6;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import magpiebridge.converter.minimaltestsuite.MinimalTestSuiteBase;
import org.junit.Test;

public class UnaryOpIntTest extends MinimalTestSuiteBase {

  public String getMethodSignature() {
    return identifierFactory.getMethodSignature(
        "methodUnaryOpInt", getDeclaredClassSignature(), "void", Collections.emptyList());
  }

  @Test
  @Override
  public void defaultTest() {
    super.defaultTest();
    /**
     * TODO Do we need to check the type of variable as int?
     * assertTrue(getFields().stream().anyMatch(sootField -> {return
     * sootField.getType().equals("int");}));
     */
  }

  @Override
  public List<String> expectedBodyStmts() {
    return Stream.of(
            "r0 := @this: UnaryOpInt",
            "$i0 = r0.<UnaryOpInt: int i>",
            "$i1 = r0.<UnaryOpInt: int j>",
            "$i2 = $i0 + $i1",
            "return")
        .collect(Collectors.toCollection(ArrayList::new));
  }
}
