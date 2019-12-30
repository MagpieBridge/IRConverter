/** @author: Hasitha Rajapakse */
package magpiebridge.converter.minimaltestsuite.java6;

import static org.junit.Assert.assertTrue;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import magpiebridge.converter.categories.Java8Test;
import magpiebridge.converter.minimaltestsuite.MinimalTestSuiteBase;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import soot.SootClass;

@Category(Java8Test.class)
public class AbstractClassTest extends MinimalTestSuiteBase {

  @Test
  public void defaultTest() {
    SootClass clazz = loadClass(getDeclaredClassSignature());

    // The SuperClass is the abstract one
    SootClass superClazz = loadClass(clazz.getSuperclass().getName());
    assertTrue(superClazz.isAbstract());
    super.defaultTest();
  }

  public String getMethodSignature() {
    String methodSignature =
        identifierFactory.getMethodSignature(
            "abstractClass", getDeclaredClassSignature(), "void", Collections.emptyList());
    return methodSignature;
  }

  @Override
  public List<String> expectedBodyStmts() {
    return Stream.of(
            "r0 := @this: AbstractClass",
            "$r1 = new AbstractClass",
            "specialinvoke $r1.<AbstractClass: void <init>()>()",
            "virtualinvoke $r1.<A: void a()>()",
            "return")
        .collect(Collectors.toList());
  }
}
