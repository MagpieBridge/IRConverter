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

/** @author Kaustubh Kelkar */
public class MultiInterfaceImplClassTest extends MinimalTestSuiteBase {
  public String getMethodSignature() {
    return identifierFactory.getMethodSignature(
        "interfaceMethod", getDeclaredClassSignature(), "void", Collections.emptyList());
  }

  @Test
  @Override
  public void defaultTest() {
    super.defaultTest();
    SootClass clazz = loadClass(getDeclaredClassSignature());
    assertTrue(
        clazz.getInterfaces().stream()
            .anyMatch(
                javaClassType -> {
                  return javaClassType.getName().equals("InterfaceImplDummy");
                }));
  }

  @Override
  public List<String> expectedBodyStmts() {
    return Stream.of(
            "r0 := @this: MultiInterfaceImplClass",
            "$r1 = <java.lang.System: java.io.PrintStream out>",
            "virtualinvoke $r1.<java.io.PrintStream: void print(java.lang.String)>(\"Method from InterfaceImpl is implemented\")",
            "$r2 = <java.lang.System: java.io.PrintStream out>",
            "$r4 = new java.lang.StringBuilder",
            "specialinvoke $r4.<java.lang.StringBuilder: void <init>(java.lang.String)>(\"Variable from InterfaceImpl is \")",
            "$r5 = virtualinvoke $r4.<java.lang.StringBuilder: java.lang.StringBuilder append(java.lang.String)>(10)",
            "$r3 = virtualinvoke $r5.<java.lang.StringBuilder: java.lang.StringBuilder toString()>()",
            "virtualinvoke $r2.<java.io.PrintStream: void println(java.lang.String)>($r3)",
            "return")
        .collect(Collectors.toCollection(ArrayList::new));
  }
}
