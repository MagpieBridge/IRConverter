package magpiebridge.converter.minimaltestsuite.java8;

import static org.junit.Assert.assertTrue;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import magpiebridge.converter.minimaltestsuite.MinimalTestSuiteBase;
import org.junit.Test;
import soot.SootClass;
import soot.SootMethod;

public class StaticMethodInterfaceImplTest extends MinimalTestSuiteBase {

  public String getMethodSignature() {
    return identifierFactory.getMethodSignature(
        "methodStaticMethodInterfaceImpl",
        getDeclaredClassSignature(),
        "void",
        Collections.emptyList());
  }

  private String getStaticMethodSignature() {
    return identifierFactory.getMethodSignature(
        "initStatic", getDeclaredClassSignature(), "void", Collections.emptyList());
  }

  @Test
  @Override
  public void defaultTest() {
    SootMethod method = loadMethod(getStaticMethodSignature());
    assertJimpleStmts(method, expectedBodyStmts1());
    SootMethod staticMethod = loadMethod(getStaticMethodSignature());
    assertJimpleStmts(staticMethod, expectedBodyStmts1());
    assertTrue(staticMethod.isStatic() && staticMethod.getName().equals("initStatic"));
    SootClass sootClass = loadClass(getDeclaredClassSignature());
    assertTrue(
        sootClass.getInterfaces().stream()
            .anyMatch(
                javaClassType -> {
                  return javaClassType.getShortName().equals("StaticMethodInterface");
                }));
  }

  @Override
  public List<String> expectedBodyStmts() {
    return Stream.of(
            "r0 := @this: StaticMethodInterfaceImpl",
            "$r1 = <java.lang.System: java.io.PrintStream out>",
            "virtualinvoke $r1.<java.io.PrintStream: void println(java.lang.String)>(\"Inside display - StaticmethodInterfaceImpl\")",
            "return")
        .collect(Collectors.toList());
  }

  public List<String> expectedBodyStmts1() {
    return Stream.of(
            "$r0 = <java.lang.System: java.io.PrintStream out>",
            "virtualinvoke $r0.<java.io.PrintStream: void println(java.lang.String)>(\"Inside initStatic - StaticmethodInterfaceImpl\")",
            "return")
        .collect(Collectors.toList());
  }
}
