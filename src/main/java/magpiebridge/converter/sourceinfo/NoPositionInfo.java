package magpiebridge.converter.sourceinfo;

/**
 * This class represents the case when there is no position.
 *
 * @author Linghui Luo
 */
public class NoPositionInfo extends SourcePosition {

  private static final NoPositionInfo INSTANCE = new NoPositionInfo();

  private NoPositionInfo() {
    super(-1, -1, -1, -1);
  }

  public static NoPositionInfo getInstance() {
    return INSTANCE;
  }

  @Override
  public String toString() {
    return "No position info";
  }
}
