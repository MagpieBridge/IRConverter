package magpiebridge.converter.minimaltestsuite.java6;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import magpiebridge.converter.minimaltestsuite.MinimalTestSuiteBase;
import org.junit.Test;
import soot.SootClass;

public class DeclareInnerClassTest extends MinimalTestSuiteBase {

  public String getMethodSignature() {
    return identifierFactory.getMethodSignature(
        "methodDisplayOuter", getDeclaredClassSignature(), "void", Collections.emptyList());
  }

  @Test
  @Override
  public void defaultTest() {
    super.defaultTest();
    // loadMethod(expectedBodyStmts1(), getStaticMethodSignature());
    // SootMethod staticMethod = loadMethod(expectedBodyStmts1(),
    // getStaticMethodSignature());
    SootClass sootClass = loadClass(getDeclaredClassSignature());
    /** TODO check for inner class inside method body */
    // assertTrue(sootClass.getFields().stream().anyMatch(sootField -> {return
    // sootField.getModifiers().equals()}));
  }

  @Override
  public List<String> expectedBodyStmts() {
    return Stream.of(
            "r0 := @this: DeclareInnerClass",
            "$r1 = <java.lang.System: java.io.PrintStream out>",
            "virtualinvoke $r1.<java.io.PrintStream: void println(java.lang.String)>(\"methodDisplayOuter\")",
            "return")
        .collect(Collectors.toList());
  }
}
