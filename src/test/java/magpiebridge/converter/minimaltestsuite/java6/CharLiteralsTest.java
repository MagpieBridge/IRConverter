/** @author: Hasitha Rajapakse */
package magpiebridge.converter.minimaltestsuite.java6;

import java.util.Collections;
import magpiebridge.converter.categories.Java8Test;
import magpiebridge.converter.minimaltestsuite.MinimalTestSuiteBase;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import soot.SootMethod;

@Category(Java8Test.class)
public class CharLiteralsTest extends MinimalTestSuiteBase {

  @Test
  public void defaultTest() {
    SootMethod method = loadMethod(getMethodSignature("charCharacter"));
    assertJimpleStmts(method, expectedBodyStmts("r0 := @this: CharLiterals", "$i0 = 97", "return"));

    method = loadMethod(getMethodSignature("charSymbol"));
    assertJimpleStmts(method, expectedBodyStmts("r0 := @this: CharLiterals", "$i0 = 37", "return"));

    method = loadMethod(getMethodSignature("charBackslashT"));
    assertJimpleStmts(method, expectedBodyStmts("r0 := @this: CharLiterals", "$i0 = 9", "return"));

    method = loadMethod(getMethodSignature("charBackslash"));
    assertJimpleStmts(method, expectedBodyStmts("r0 := @this: CharLiterals", "$i0 = 92", "return"));

    method = loadMethod(getMethodSignature("charSingleQuote"));
    assertJimpleStmts(method, expectedBodyStmts("r0 := @this: CharLiterals", "$i0 = 39", "return"));

    method = loadMethod(getMethodSignature("charUnicode"));
    assertJimpleStmts(
        method, expectedBodyStmts("r0 := @this: CharLiterals", "$i0 = 937", "return"));

    method = loadMethod(getMethodSignature("specialChar"));
    assertJimpleStmts(
        method, expectedBodyStmts("r0 := @this: CharLiterals", "$i0 = 8482", "return"));
  }

  public String getMethodSignature(String methodName) {
    return identifierFactory.getMethodSignature(
        methodName, getDeclaredClassSignature(), "void", Collections.emptyList());
  }
}
