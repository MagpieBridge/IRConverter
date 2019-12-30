package magpiebridge.converter.minimaltestsuite.java6;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import magpiebridge.converter.minimaltestsuite.MinimalTestSuiteBase;

public class GenTypeParamTest extends MinimalTestSuiteBase {

  public String getMethodSignature() {
    return identifierFactory.getMethodSignature(
        "geneTypeParamDisplay", getDeclaredClassSignature(), "void", Collections.emptyList());
  }

  /** Jimple code will change when Generics are incorporated */
  @Override
  public List<String> expectedBodyStmts() {
    return Stream.of(
            "r0 := @this: GenTypeParam",
            "$r1 = new java.util.ArrayList",
            "specialinvoke $r1.<java.util.ArrayList: void <init>(int)>(3)",
            "$r2 = newarray (java.lang.Object[])[3]",
            "$r2[0] = 1",
            "$r2[1] = 2",
            "$r2[2] = 3",
            "$r3 = staticinvoke <java.util.Arrays: java.util.List asList(java.lang.Object[])>($r2)",
            "$r4 = new GenTypeParam",
            "specialinvoke $r4.<GenTypeParam: void <init>()>()",
            "virtualinvoke $r4.<GenTypeParam: void copy(java.util.List,java.util.List)>($r1, $r3)",
            "$r5 = <java.lang.System: java.io.PrintStream out>",
            "$r6 = virtualinvoke $r4.<GenTypeParam: java.lang.Number largestNum(java.lang.Number,java.lang.Number,java.lang.Number)>(2, 8, 3)",
            "$r7 = (java.lang.Integer) $r6",
            "virtualinvoke $r5.<java.io.PrintStream: void println(java.lang.Object)>($r7)",
            "return")
        .collect(Collectors.toCollection(ArrayList::new));
  }
}
