package magpiebridge.converter.minimaltestsuite.java8;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import magpiebridge.converter.minimaltestsuite.MinimalTestSuiteBase;
import org.junit.Ignore;
import org.junit.Test;
import soot.Modifier;
import soot.SootClass;

/** @author Kaustubh Kelkar */
public class RepeatingAnnotationsTest extends MinimalTestSuiteBase {

  @Override
  public String getMethodSignature() {
    return identifierFactory.getMethodSignature(
        "annotaionMethod", getDeclaredClassSignature(), "void", Collections.emptyList());
  }

  @Test
  public void defaultTest() {}

  @Ignore
  public void annotationTest() {
    super.defaultTest();
    SootClass sootClass = loadClass(getDeclaredClassSignature());
    assertTrue(Modifier.isAnnotation(sootClass.getModifiers()));
  }

  @Override
  public List<String> expectedBodyStmts() {
    return Stream.of("r0 := @this: RepeatingAnnotations", "$r1 = \"\"", "$r2 = \"\"", "return")
        .collect(Collectors.toCollection(ArrayList::new));
  }
}
