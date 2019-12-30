/** @author: Hasitha Rajapakse */
package magpiebridge.converter.minimaltestsuite.java6;

import static org.junit.Assert.assertTrue;

import java.util.Collections;
import magpiebridge.converter.categories.Java8Test;
import magpiebridge.converter.minimaltestsuite.MinimalTestSuiteBase;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import soot.SootClass;

/** @author: Hasitha Rajapakse */
@Category(Java8Test.class)
public class NoModifierClassTest extends MinimalTestSuiteBase {

  @Test
  public void defaultTest() {
    SootClass clazz = loadClass(getDeclaredClassSignature());
    assertTrue(
        clazz
            .getMethod(identifierFactory.getSubMethodSignature(getMethodSignature("private")))
            .isPrivate());
    assertTrue(
        clazz
            .getMethod(identifierFactory.getSubMethodSignature(getMethodSignature("protected")))
            .isProtected());
    assertTrue(
        clazz
            .getMethod(identifierFactory.getSubMethodSignature(getMethodSignature("public")))
            .isPublic());
    assertTrue(
        clazz
                .getMethod(
                    identifierFactory.getSubMethodSignature(getMethodSignature("noModifier")))
                .getModifiers()
            == 0);
  }

  public String getMethodSignature(String modifier) {
    return identifierFactory.getMethodSignature(
        modifier + "Method", getDeclaredClassSignature(), "void", Collections.emptyList());
  }
}
