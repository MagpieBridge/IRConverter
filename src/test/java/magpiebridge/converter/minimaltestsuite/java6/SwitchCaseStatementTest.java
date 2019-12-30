/** @author: Hasitha Rajapakse */
package magpiebridge.converter.minimaltestsuite.java6;

import java.util.Collections;
import magpiebridge.converter.categories.Java8Test;
import magpiebridge.converter.minimaltestsuite.MinimalTestSuiteBase;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import soot.SootMethod;

@Category(Java8Test.class)
public class SwitchCaseStatementTest extends MinimalTestSuiteBase {

  @Test
  public void defaultTest() {
    // add test after fixing jimple
  }

  @Ignore
  public void defaultTest2() {
    SootMethod method = loadMethod(getMethodSignature("switchCaseStatementEnum"));
    assertJimpleStmts(
        method,
        expectedBodyStmts(
            "r0 := @this: SwitchCaseStatement",
            "$r1 = \"RED\"",
            "$r2 = \"\"",
            "$r3 = staticinvoke <SwitchCaseStatement$Color: SwitchCaseStatement$Color valueOf(java.lang.String)>($r1)",
            "$r4 = <SwitchCaseStatement$Color: SwitchCaseStatement$Color RED>",
            "if $r3 == $r4 goto label1",
            "$r5 = <SwitchCaseStatement$Color: SwitchCaseStatement$Color GREEN>",
            "if $r3 == $r5 goto label2",
            "goto label3",
            "label1:",
            "$r6 = <SwitchCaseStatement$Color: SwitchCaseStatement$Color RED>",
            "$r2 = \"color red detected\"",
            "goto label4",
            "label2:",
            "$r7 = <SwitchCaseStatement$Color: SwitchCaseStatement$Color GREEN>",
            "$r2 = \"color green detected\"",
            "goto label4",
            "label3:",
            "$r2 = \"invalid color\"",
            "goto label4",
            "label4:",
            "return"));
    method = loadMethod(getMethodSignature("switchCaseStatementInt"));
    assertJimpleStmts(
        method,
        expectedBodyStmts(
            "r0 := @this: SwitchCaseStatement",
            "$i0 = 2",
            "$r1 = null",
            "lookupswitch($i0)",
            "case 1: goto label2",
            "case 2: goto [?= null]",
            "case 3: goto [?= null]",
            "default: goto label1",
            "label1:",
            "$r1 = \"number 1 detected\"",
            "goto label3",
            "$r1 = \"number 2 detected\"",
            "label2:",
            "goto label3",
            "$r1 = \"number 3 detected\"",
            "goto label3",
            "label3:",
            "return"));
  }

  public String getMethodSignature(String methodName) {
    return identifierFactory.getMethodSignature(
        methodName, getDeclaredClassSignature(), "void", Collections.emptyList());
  }
}
