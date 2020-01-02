package magpiebridge.converter.sourceinfo;

import com.ibm.wala.cast.tree.CAstSourcePositionMap.Position;
import java.util.Arrays;
import javax.annotation.Nonnull;

/**
 * This class stores position information stored for a statement.
 *
 * @author Linghui Luo
 */
public final class StmtPositionInfo {
  private final Position stmtPosition;
  private final Position[] operandPositions;

  /**
   * Create an instance with no position information.
   *
   * @return an instance with no position information.
   */
  public static StmtPositionInfo createNoStmtPositionInfo() {
    return new StmtPositionInfo(NoPositionInfo.getInstance(), null);
  }

  /**
   * Create an instance from given statement position and operand positions.
   *
   * @param stmtPosition the position of the statement
   * @param operandPositions the operand positions
   */
  public StmtPositionInfo(Position stmtPosition, Position[] operandPositions) {
    this.stmtPosition = stmtPosition;
    if (operandPositions != null)
      this.operandPositions =
          Arrays.stream(operandPositions)
              .map(
                  op -> {
                    return op == null ? NoPositionInfo.getInstance() : op;
                  })
              .toArray(Position[]::new);
    else this.operandPositions = null;
  }

  /**
   * Return the position of the statement.
   *
   * @return the position of the statement
   */
  public Position getStmtPosition() {
    if (this.stmtPosition != null) {
      return this.stmtPosition;
    } else {
      return NoPositionInfo.getInstance();
    }
  }

  /**
   * Return the precise position of the given operand in the statement.
   *
   * @param index the operand index
   * @return the position of the given operand
   */
  public Position getOperandPosition(int index) {
    if (this.operandPositions != null && index >= 0 && index < this.operandPositions.length) {
      return this.operandPositions[index];
    } else {
      return NoPositionInfo.getInstance();
    }
  }

  @Override
  public String toString() {
    StringBuilder s = new StringBuilder();
    s.append("stmtPosition: ").append(getStmtPosition().toString()).append("\n");
    s.append("operandPositions: ");
    if (operandPositions != null) {
      s.append("\n");
      for (int i = 0; i < operandPositions.length; i++) {
        s.append(i).append(": ").append(operandPositions[i]).append(" ");
      }
    } else {
      s.append("No position info");
    }
    return s.toString();
  }

  @Nonnull
  public StmtPositionInfo withStmtPosition(Position stmtPosition) {
    return new StmtPositionInfo(stmtPosition, operandPositions);
  }

  @Nonnull
  public StmtPositionInfo withOperandPositions(Position[] operandPositions) {
    return new StmtPositionInfo(stmtPosition, operandPositions);
  }
}
