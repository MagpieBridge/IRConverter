package magpiebridge.converter;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Collectors;
import soot.Body;
import soot.Printer;
import soot.util.EscapedWriter;

public class Utils {

  public static ArrayList<String> bodyStmtsAsStrings(Body body) {
    body.getLocals().clear();
    StringWriter writer = new StringWriter();
    try (PrintWriter writerOut = new PrintWriter(new EscapedWriter(writer))) {
      Printer.v().printTo(body, writerOut);
    }

    return Arrays.stream(writer.toString().split("\n"))
        .skip(1) // Remove method declaration
        .map(String::trim)
        .map(line -> line.endsWith(";") ? line.substring(0, line.length() - 1) : line)
        .filter(line -> !line.isEmpty() && !"{".equals(line) && !"}".equals(line))
        .collect(Collectors.toCollection(ArrayList::new));
  }

  public static void print(Body body, boolean print) {
    if (print) {
      PrintWriter r = new PrintWriter(System.out);
      Printer.v().printTo(body, r);
      r.flush();
    }
  }
}
