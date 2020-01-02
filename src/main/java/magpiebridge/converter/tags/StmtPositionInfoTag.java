package magpiebridge.converter.tags;

import magpiebridge.converter.sourceinfo.StmtPositionInfo;
import soot.tagkit.AttributeValueException;
import soot.tagkit.Tag;

/**
 * Tag used for storing {@link StmtPositionInfo}
 *
 * @author Linghui Luo
 */
public class StmtPositionInfoTag implements Tag {

  private StmtPositionInfo stmtPos;

  @Override
  public String getName() {
    return "StmtPositionInfoTag";
  }

  @Override
  public byte[] getValue() throws AttributeValueException {
    return stmtPos.toString().getBytes();
  }

  public StmtPositionInfoTag(StmtPositionInfo stmtPos) {
    this.stmtPos = stmtPos;
  }

  public StmtPositionInfo getStmtPositionInfo() {
    return this.stmtPos;
  }

  @Override
  public String toString() {
    if (stmtPos != null) {
      return stmtPos.toString();
    } else {
      return "No position info";
    }
  }
}
