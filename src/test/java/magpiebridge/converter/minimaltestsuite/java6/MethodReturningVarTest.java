/** @author: Hasitha Rajapakse */
package magpiebridge.converter.minimaltestsuite.java6;

import java.util.Collections;
import magpiebridge.converter.categories.Java8Test;
import magpiebridge.converter.minimaltestsuite.MinimalTestSuiteBase;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import soot.SootMethod;

@Category(Java8Test.class)
public class MethodReturningVarTest extends MinimalTestSuiteBase {

  @Test
  public void defaultTest() {
    SootMethod method = loadMethod(getMethodSignature("short"));
    assertJimpleStmts(
        method, expectedBodyStmts("r0 := @this: MethodReturningVar", "$i0 = 10", "return $i0"));

    method = loadMethod(getMethodSignature("byte"));
    assertJimpleStmts(
        method, expectedBodyStmts("r0 := @this: MethodReturningVar", "$i0 = 0", "return $i0"));

    method = loadMethod(getMethodSignature("char"));
    assertJimpleStmts(
        method, expectedBodyStmts("r0 := @this: MethodReturningVar", "$i0 = 97", "return $i0"));

    method = loadMethod(getMethodSignature("int"));
    assertJimpleStmts(
        method, expectedBodyStmts("r0 := @this: MethodReturningVar", "$i0 = 512", "return $i0"));

    method = loadMethod(getMethodSignature("long"));
    assertJimpleStmts(
        method,
        expectedBodyStmts("r0 := @this: MethodReturningVar", "$i0 = 123456789", "return $i0"));

    method = loadMethod(getMethodSignature("float"));
    assertJimpleStmts(
        method, expectedBodyStmts("r0 := @this: MethodReturningVar", "$f0 = 3.14F", "return $f0"));

    method = loadMethod(getMethodSignature("double"));
    assertJimpleStmts(
        method,
        expectedBodyStmts("r0 := @this: MethodReturningVar", "$d0 = 1.96969654", "return $d0"));
  }

  public String getMethodSignature(String datatype) {
    return identifierFactory.getMethodSignature(
        datatype + "Variable", getDeclaredClassSignature(), datatype, Collections.emptyList());
  }
}
