package magpiebridge.converter.minimaltestsuite.java6;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import magpiebridge.converter.minimaltestsuite.MinimalTestSuiteBase;
import soot.Modifier;
import soot.SootClass;
import soot.SootMethod;

/** @author Kaustubh Kelkar */
public class DeclareFieldTest extends MinimalTestSuiteBase {

  @Override
  public String getMethodSignature() {
    return identifierFactory.getMethodSignature(
        "display", getDeclaredClassSignature(), "void", Collections.emptyList());
  }

  public String getStaticMethodSignature() {
    return identifierFactory.getMethodSignature(
        "staticDisplay", getDeclaredClassSignature(), "void", Collections.emptyList());
  }

  @Override
  public void defaultTest() {
    super.defaultTest();
    SootMethod method = loadMethod(getMethodSignature());
    assertJimpleStmts(method, expectedBodyStmts());
    method = loadMethod(getStaticMethodSignature());
    assertJimpleStmts(method, expectedBodyStmts1());
    SootClass clazz = loadClass(getDeclaredClassSignature());
    assertTrue(
        clazz.getFields().stream()
            .anyMatch(
                sootField -> {
                  return Modifier.isPrivate(sootField.getModifiers())
                      && Modifier.isStatic(sootField.getModifiers())
                      && sootField.getName().equals("i");
                }));
    assertTrue(
        clazz.getFields().stream()
            .anyMatch(
                sootField -> {
                  return Modifier.isPublic(sootField.getModifiers())
                      && Modifier.isFinal(sootField.getModifiers())
                      && sootField.getName().equals("s");
                }));
  }

  @Override
  public List<String> expectedBodyStmts() {
    return Stream.of(
            "r0 := @this: DeclareField",
            "$r1 = <java.lang.System: java.io.PrintStream out>",
            "$r2 = r0.<DeclareField: java.lang.String s>",
            "virtualinvoke $r1.<java.io.PrintStream: void println(java.lang.String)>(\"Java\")",
            "return")
        .collect(Collectors.toCollection(ArrayList::new));
  }

  public List<String> expectedBodyStmts1() {
    return Stream.of(
            "r0 := @this: DeclareField",
            "$r1 = <java.lang.System: java.io.PrintStream out>",
            "$i0 = <DeclareField: int i>",
            "virtualinvoke $r1.<java.io.PrintStream: void println(int)>($i0)",
            "return")
        .collect(Collectors.toCollection(ArrayList::new));
  }
}
