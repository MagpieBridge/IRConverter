package magpiebridge.converter.minimaltestsuite.java6;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import magpiebridge.converter.minimaltestsuite.MinimalTestSuiteBase;
import org.junit.Test;
import soot.SootClass;
import soot.SootMethod;

/** @author Kaustubh Kelkar */
public class SubClassTest extends MinimalTestSuiteBase {
  public String getMethodSignature() {
    return identifierFactory.getMethodSignature(
        "subclassMethod", getDeclaredClassSignature(), "void", Collections.emptyList());
  }

  /** @returns the method signature needed for second method in testCase */
  public String getMethodSignature1() {
    return identifierFactory.getMethodSignature(
        "superclassMethod", getDeclaredClassSignature(), "void", Collections.emptyList());
  }

  @Test
  public void testSuperClassStmts() {
    SootMethod m = loadMethod(getMethodSignature1());
    assertJimpleStmts(m, expectedBodyStmts1());
    SootClass sootClass = loadClass(getDeclaredClassSignature());
    assertTrue(sootClass.getSuperclass().getName().equals("SuperClass"));
  }

  @Override
  public List<String> expectedBodyStmts() {
    return Stream.of(
            "r0 := @this: SubClass",
            "r0.<SubClass: int aa> = 10",
            "r0.<SubClass: int bb> = 20",
            "r0.<SubClass: int cc> = 30",
            "r0.<SubClass: int dd> = 40",
            "return")
        .collect(Collectors.toCollection(ArrayList::new));
  }

  public List<String> expectedBodyStmts1() {
    return Stream.of(
            "r0 := @this: SubClass",
            "specialinvoke r0.<SuperClass: void superclassMethod()>()",
            "r0.<SuperClass: int a> = 100",
            "r0.<SuperClass: int b> = 200",
            "r0.<SuperClass: int c> = 300",
            "return")
        .collect(Collectors.toCollection(ArrayList::new));
  }
}
