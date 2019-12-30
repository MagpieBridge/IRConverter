/** @author: Hasitha Rajapakse */
package magpiebridge.converter.minimaltestsuite.java6;

import java.util.Collections;
import magpiebridge.converter.categories.Java8Test;
import magpiebridge.converter.minimaltestsuite.MinimalTestSuiteBase;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import soot.SootMethod;

@Category(Java8Test.class)
public class VariableDeclarationTest extends MinimalTestSuiteBase {

  @Test
  public void defaultTest() {

    SootMethod method = loadMethod(getMethodSignature("shortVariable"));

    assertJimpleStmts(
        method, expectedBodyStmts("r0 := @this: VariableDeclaration", "$i0 = 10", "return"));

    method = loadMethod(getMethodSignature("byteVariable"));

    assertJimpleStmts(
        method, expectedBodyStmts("r0 := @this: VariableDeclaration", "$i0 = 0", "return"));

    method = loadMethod(getMethodSignature("charVariable"));

    assertJimpleStmts(
        method, expectedBodyStmts("r0 := @this: VariableDeclaration", "$i0 = 97", "return"));

    method = loadMethod(getMethodSignature("intVariable"));
    assertJimpleStmts(
        method, expectedBodyStmts("r0 := @this: VariableDeclaration", "$i0 = 512", "return"));

    method = loadMethod(getMethodSignature("longVariable"));
    assertJimpleStmts(
        method, expectedBodyStmts("r0 := @this: VariableDeclaration", "$i0 = 123456789", "return"));

    method = loadMethod(getMethodSignature("floatVariable"));
    assertJimpleStmts(
        method, expectedBodyStmts("r0 := @this: VariableDeclaration", "$f0 = 3.14F", "return"));

    method = loadMethod(getMethodSignature("doubleVariable"));
    assertJimpleStmts(
        method,
        expectedBodyStmts("r0 := @this: VariableDeclaration", "$d0 = 1.96969654", "return"));
  }

  public String getMethodSignature(String methodName) {
    return identifierFactory.getMethodSignature(
        methodName, getDeclaredClassSignature(), "void", Collections.emptyList());
  }
}
