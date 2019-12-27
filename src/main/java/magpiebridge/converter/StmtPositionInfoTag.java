package magpiebridge.converter;

import soot.tagkit.AttributeValueException;
import soot.tagkit.Tag;

/** @author Linghui Luo */
public class StmtPositionInfoTag implements Tag {

  private StmtPositionInfo stmtPos;

  @Override
  public String getName() {
    return "PositionInfoTag";
  }

  @Override
  public byte[] getValue() throws AttributeValueException {
    return stmtPos.toString().getBytes();
  }

  public StmtPositionInfoTag(StmtPositionInfo stmtPos) {
    this.stmtPos = stmtPos;
  }

  public StmtPositionInfo getPositionInfo() {
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
