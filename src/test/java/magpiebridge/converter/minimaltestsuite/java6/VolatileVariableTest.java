package magpiebridge.converter.minimaltestsuite.java6;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import magpiebridge.converter.minimaltestsuite.MinimalTestSuiteBase;
import org.junit.Test;
import soot.Modifier;
import soot.SootClass;

/** @author Kaustubh Kelkar */
public class VolatileVariableTest extends MinimalTestSuiteBase {
  public String getMethodSignature() {
    return identifierFactory.getMethodSignature(
        "increaseCounter", getDeclaredClassSignature(), "int", Collections.emptyList());
  }

  @Test
  @Override
  public void defaultTest() {
    super.defaultTest();
    SootClass clazz = loadClass(getDeclaredClassSignature());
    assertTrue(
        clazz.getFields().stream()
            .anyMatch(
                sootField -> {
                  return sootField.getName().equals("counter")
                      && Modifier.isVolatile(sootField.getModifiers());
                }));
  }

  @Override
  public List<String> expectedBodyStmts() {
    return Stream.of(
            "r0 := @this: VolatileVariable",
            "$i0 = r0.<VolatileVariable: int counter>",
            "$i1 = $i0 + 1",
            "r0.<VolatileVariable: int counter> = $i1",
            "return $i0")
        .collect(Collectors.toCollection(ArrayList::new));
  }
}
