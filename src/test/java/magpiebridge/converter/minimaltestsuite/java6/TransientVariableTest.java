package magpiebridge.converter.minimaltestsuite.java6;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import magpiebridge.converter.minimaltestsuite.MinimalTestSuiteBase;
import org.junit.Ignore;
import soot.Modifier;
import soot.SootClass;

/** @author Kaustubh Kelkar */
public class TransientVariableTest extends MinimalTestSuiteBase {
  public String getMethodSignature() {
    return identifierFactory.getMethodSignature(
        "transientVariable", getDeclaredClassSignature(), "void", Collections.emptyList());
  }

  @Override
  public void defaultTest() {
    // TODO: once the transient bug is fixed replace with the body of ignoredTest -
    // can not @Ignore
    // the overriden @Test method
  }

  @Ignore
  public void ignoredTest() {
    super.defaultTest();
    SootClass clazz = loadClass(getDeclaredClassSignature());
    assertTrue(
        clazz.getFields().stream()
            .anyMatch(
                sootField ->
                    sootField.getName().equals("transientVar")
                        && Modifier.isTransient(sootField.getModifiers())));
  }

  @Override
  public List<String> expectedBodyStmts() {
    return Stream.of(
            "r0 := @this: TransientVariable",
            "$r1 = <java.lang.System: java.io.PrintStream out>",
            "$i0 = r0.<TransientVariable: int transientVar>",
            "virtualinvoke $r1.<java.io.PrintStream: void println(int)>($i0)",
            "return")
        .collect(Collectors.toCollection(ArrayList::new));
  }
}
