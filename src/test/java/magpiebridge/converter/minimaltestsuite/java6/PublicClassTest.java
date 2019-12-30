package magpiebridge.converter.minimaltestsuite.java6;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import magpiebridge.converter.categories.Java8Test;
import magpiebridge.converter.minimaltestsuite.MinimalTestSuiteBase;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import soot.Modifier;
import soot.SootClass;
import soot.SootMethod;

/** @author: Hasitha Rajapakse */
@Category(Java8Test.class)
public class PublicClassTest extends MinimalTestSuiteBase {

  @Test
  public void defaultTest() {
    SootClass clazz = loadClass(getDeclaredClassSignature());
    assertEquals(Modifier.PUBLIC, clazz.getModifiers());

    SootMethod method;
    method =
        clazz.getMethod(identifierFactory.getSubMethodSignature(getMethodSignature("private")));
    assertTrue(method.isPrivate());
    assertJimpleStmts(method, expectedBodyStmts());

    method =
        clazz.getMethod(identifierFactory.getSubMethodSignature(getMethodSignature("protected")));
    assertTrue(method.isProtected());
    assertJimpleStmts(method, expectedBodyStmts());

    method = clazz.getMethod(identifierFactory.getSubMethodSignature(getMethodSignature("public")));
    assertTrue(method.isPublic());
    assertJimpleStmts(method, expectedBodyStmts());

    method =
        clazz.getMethod(identifierFactory.getSubMethodSignature(getMethodSignature("noModifier")));
    assertTrue(method.getModifiers() == 0);
    assertJimpleStmts(method, expectedBodyStmts());
  }

  public String getMethodSignature(String modifier) {
    return identifierFactory.getMethodSignature(
        modifier + "Method", getDeclaredClassSignature(), "void", Collections.emptyList());
  }

  @Override
  public List<String> expectedBodyStmts() {
    return Stream.of("r0 := @this: PublicClass", "return").collect(Collectors.toList());
  }
}
