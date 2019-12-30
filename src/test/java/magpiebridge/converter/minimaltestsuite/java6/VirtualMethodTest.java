package magpiebridge.converter.minimaltestsuite.java6;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import magpiebridge.converter.minimaltestsuite.MinimalTestSuiteBase;

/** @author Kaustubh Kelkar */
public class VirtualMethodTest extends MinimalTestSuiteBase {

  @Override
  public String getMethodSignature() {
    return identifierFactory.getMethodSignature(
        "virtualMethodDemo", getDeclaredClassSignature(), "void", Collections.emptyList());
  }

  @Override
  public List<String> expectedBodyStmts() {
    return Stream.of(
            "r0 := @this: VirtualMethod",
            "$r1 = new TempEmployee",
            "specialinvoke $r1.<TempEmployee: void <init>(int,int)>(1500, 150)",
            "$r2 = new RegEmployee",
            "specialinvoke $r2.<RegEmployee: void <init>(int,int)>(1500, 500)",
            "$r3 = <java.lang.System: java.io.PrintStream out>",
            "$i0 = virtualinvoke $r1.<Employee: int getSalary()>()",
            "virtualinvoke $r3.<java.io.PrintStream: void println(int)>($i0)",
            "$r4 = <java.lang.System: java.io.PrintStream out>",
            "$i1 = virtualinvoke $r2.<Employee: int getSalary()>()",
            "virtualinvoke $r4.<java.io.PrintStream: void println(int)>($i1)",
            "return")
        .collect(Collectors.toCollection(ArrayList::new));
  }
}
