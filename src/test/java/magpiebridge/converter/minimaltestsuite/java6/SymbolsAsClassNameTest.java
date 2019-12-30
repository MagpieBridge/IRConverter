package magpiebridge.converter.minimaltestsuite.java6;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import magpiebridge.converter.minimaltestsuite.MinimalTestSuiteBase;
import org.junit.Ignore;
import soot.SootClass;

/** @author Kaustubh Kelkar */
public class SymbolsAsClassNameTest extends MinimalTestSuiteBase {
  @Override
  public String getMethodSignature() {
    return identifierFactory.getMethodSignature(
        "αρετηAsClassName", getDeclaredClassSignature(), "void", Collections.emptyList());
  }

  @Override
  public void defaultTest() {
    /**
     * Exception in thread "main" java.nio.file.InvalidPathException: Illegal char <?> at index 1:
     * a?et?.java
     */
  }

  @Ignore
  public void ignoreTest() {
    super.defaultTest();
    SootClass sootClass = loadClass(getDeclaredClassSignature());
    System.out.println(sootClass.getName());
  }

  @Override
  public List<String> expectedBodyStmts() {
    return Stream.of(
            "r0 := @this: αρετη",
            "$r1 = <java.lang.System: java.io.PrintStream out>",
            "virtualinvoke $r1.<java.io.PrintStream: void println(java.lang.String)>(\"this is αρετη class\")",
            "return")
        .collect(Collectors.toList());
  }
}
