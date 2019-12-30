package magpiebridge.converter.tags;

import com.ibm.wala.cast.loader.AstMethod.DebuggingInformation;
import soot.tagkit.AttributeValueException;
import soot.tagkit.Tag;

/**
 * Tag used for storing {@link DebuggingInformation}
 *
 * @author Linghui Luo
 */
public class DebuggingInformationTag implements Tag {

  private DebuggingInformation debugInfo;

  public DebuggingInformationTag(DebuggingInformation debugInfo) {
    this.debugInfo = debugInfo;
  }

  @Override
  public String getName() {
    return "DebuggingInformationTag";
  }

  @Override
  public byte[] getValue() throws AttributeValueException {
    return this.debugInfo.toString().getBytes();
  }

  public DebuggingInformation getDebugInfo() {
    return this.debugInfo;
  }

  @Override
  public String toString() {
    return debugInfo.getCodeBodyPosition().toString();
  }
}
