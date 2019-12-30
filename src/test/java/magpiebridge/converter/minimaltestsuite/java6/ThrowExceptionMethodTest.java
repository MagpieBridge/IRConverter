package magpiebridge.converter.minimaltestsuite.java6;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import magpiebridge.converter.minimaltestsuite.MinimalTestSuiteBase;
import org.junit.Ignore;
import soot.SootMethod;

/** @author Kaustubh Kelkar */
public class ThrowExceptionMethodTest extends MinimalTestSuiteBase {

  public String getMethodSignature() {
    return identifierFactory.getMethodSignature(
        "divideByZero", getDeclaredClassSignature(), "void", Collections.emptyList());
  }

  public String getThrowCustomExceptionSignature() {
    return identifierFactory.getMethodSignature(
        "throwCustomException", getDeclaredClassSignature(), "void", Collections.emptyList());
  }

  @Override
  public void defaultTest() {
    super.defaultTest();
    SootMethod method = loadMethod(getMethodSignature());
    assertJimpleStmts(method, expectedBodyStmts());
    assertTrue(
        method.getExceptions().stream()
            .anyMatch(classType -> classType.getShortName().equals("ArithmeticException")));
    /** TODO can not detect the custom exception a */
  }

  @Ignore
  public void ignoreTest() {
    SootMethod method = loadMethod(getThrowCustomExceptionSignature());
    assertJimpleStmts(method, expectedBodyStmts1());
    assertTrue(
        method.getExceptions().stream()
            .anyMatch(classType -> classType.getShortName().equals("CustomException")));
  }

  @Override
  public List<String> expectedBodyStmts() {
    return Stream.of("r0 := @this: ThrowExceptionMethod", "$i0 = 8 / 0", "return")
        .collect(Collectors.toCollection(ArrayList::new));
  }

  public String getMethodSignature1() {
    return identifierFactory.getMethodSignature(
        "throwCustomException", getDeclaredClassSignature(), "void", Collections.emptyList());
  }

  public List<String> expectedBodyStmts1() {
    return Stream.of(
            "r0 := @this: ThrowExceptionMethod",
            "$r1 = new CustomException",
            "specialinvoke $r1.<CustomException: void <init>()>()",
            "throw $r1",
            "return")
        .collect(Collectors.toCollection(ArrayList::new));
  }
}
