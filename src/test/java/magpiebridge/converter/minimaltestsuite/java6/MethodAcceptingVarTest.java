/** @author: Hasitha Rajapakse */
package magpiebridge.converter.minimaltestsuite.java6;

import java.util.Collections;
import magpiebridge.converter.categories.Java8Test;
import magpiebridge.converter.minimaltestsuite.MinimalTestSuiteBase;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import soot.SootMethod;

@Category(Java8Test.class)
public class MethodAcceptingVarTest extends MinimalTestSuiteBase {

  @Test
  public void defaultTest() {
    SootMethod method = loadMethod(getMethodSignature("short"));
    assertJimpleStmts(
        method,
        expectedBodyStmts(
            "r0 := @this: MethodAcceptingVar",
            "$s0 := @parameter0: short",
            "$s1 = $s0",
            "$s2 = $s0 + 1",
            "$s0 = $s2",
            "return"));

    method = loadMethod(getMethodSignature("byte"));
    assertJimpleStmts(
        method,
        expectedBodyStmts(
            "r0 := @this: MethodAcceptingVar",
            "$b0 := @parameter0: byte",
            "$b1 = $b0",
            "$b2 = $b0 + 1",
            "$b0 = $b2",
            "return"));

    method = loadMethod(getMethodSignature("char"));
    assertJimpleStmts(
        method,
        expectedBodyStmts(
            "r0 := @this: MethodAcceptingVar", "$c0 := @parameter0: char", "$c0 = 97", "return"));

    method = loadMethod(getMethodSignature("int"));
    assertJimpleStmts(
        method,
        expectedBodyStmts(
            "r0 := @this: MethodAcceptingVar",
            "$i0 := @parameter0: int",
            "$i1 = $i0",
            "$i2 = $i0 + 1",
            "$i0 = $i2",
            "return"));

    method = loadMethod(getMethodSignature("long"));
    assertJimpleStmts(
        method,
        expectedBodyStmts(
            "r0 := @this: MethodAcceptingVar",
            "$l0 := @parameter0: long",
            "$l0 = 123456777",
            "return"));

    method = loadMethod(getMethodSignature("float"));

    assertJimpleStmts(
        method,
        expectedBodyStmts(
            "r0 := @this: MethodAcceptingVar",
            "$f0 := @parameter0: float",
            "$f0 = 7.77F",
            "return"));

    method = loadMethod(getMethodSignature("double"));
    assertJimpleStmts(
        method,
        expectedBodyStmts(
            "r0 := @this: MethodAcceptingVar",
            "$d0 := @parameter0: double",
            "$d0 = 1.787777777",
            "return"));
  }

  public String getMethodSignature(String datatype) {
    return identifierFactory.getMethodSignature(
        datatype + "Variable",
        getDeclaredClassSignature(),
        "void",
        Collections.singletonList(datatype));
  }
}
