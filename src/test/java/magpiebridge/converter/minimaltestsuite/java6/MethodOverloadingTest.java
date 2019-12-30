package magpiebridge.converter.minimaltestsuite.java6;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import magpiebridge.converter.minimaltestsuite.MinimalTestSuiteBase;
import org.junit.Test;
import soot.SootClass;
import soot.SootMethod;

/** @author Kaustubh Kelkar */
public class MethodOverloadingTest extends MinimalTestSuiteBase {

  public String getMethodSignature() {
    return identifierFactory.getMethodSignature(
        "calculate", getDeclaredClassSignature(), "int", Arrays.asList("int", "int"));
  }

  /** @returns the method signature needed for second method in testCase */
  public String getMethodSignatureSingleParam() {
    return identifierFactory.getMethodSignature(
        "calculate", getDeclaredClassSignature(), "int", Collections.singletonList("int"));
  }

  @Test
  @Override
  public void defaultTest() {
    SootMethod method = loadMethod(getMethodSignature());
    assertJimpleStmts(method, expectedBodyStmts());

    method = loadMethod(getMethodSignatureSingleParam());
    assertJimpleStmts(method, expectedBodyStmts1());

    SootClass sootClass = loadClass(getDeclaredClassSignature());
    assertTrue(
        sootClass.getMethodUnsafe(identifierFactory.getSubMethodSignature(getMethodSignature()))
            != null);
    assertTrue(
        sootClass.getMethodUnsafe(
                identifierFactory.getSubMethodSignature(getMethodSignatureSingleParam()))
            != null);
    assertTrue(sootClass.getMethodByName("<init>") != null);
    assertEquals(3, sootClass.getMethods().size());
  }

  @Override
  public List<String> expectedBodyStmts() {
    return Stream.of(
            "r0 := @this: MethodOverloading",
            "$i0 := @parameter0: int",
            "$i1 := @parameter1: int",
            "$i2 = $i0 + $i1",
            "return $i2")
        .collect(Collectors.toCollection(ArrayList::new));
  }

  public List<String> expectedBodyStmts1() {
    return Stream.of(
            "r0 := @this: MethodOverloading",
            "$i0 := @parameter0: int",
            "$i1 = $i0 + $i0",
            "return $i1")
        .collect(Collectors.toCollection(ArrayList::new));
  }
}
