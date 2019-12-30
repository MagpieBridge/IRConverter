package magpiebridge.converter;

import java.util.List;
import soot.Scene;

public class IdentifierFactory {

  private static IdentifierFactory instance = new IdentifierFactory();

  public static IdentifierFactory getInstance() {
    return instance;
  }

  public String getMethodSignature(
      String methodName, String declaredClassSignature, String returnType, List<String> params) {
    StringBuilder buffer = new StringBuilder();
    buffer.append("<");
    buffer.append(Scene.v().quotedNameOf(declaredClassSignature));
    buffer.append(": ");
    buffer.append(getSubMethodSignature(methodName, returnType, params));
    buffer.append(">");
    return buffer.toString();
  }

  public String getSubMethodSignature(String methodName, String returnType, List<String> params) {
    StringBuilder buffer = new StringBuilder();
    buffer.append(returnType);
    buffer.append(" ");
    buffer.append(Scene.v().quotedNameOf(methodName));
    buffer.append("(");
    if (params != null) {
      for (int i = 0; i < params.size(); i++) {
        buffer.append(params.get(i));
        if (i < params.size() - 1) {
          buffer.append(",");
        }
      }
    }
    buffer.append(")");
    return buffer.toString();
  }

  public String getSubMethodSignature(String fullSignature) {
    return fullSignature.split(": ")[1].replace(">", "");
  }
}
