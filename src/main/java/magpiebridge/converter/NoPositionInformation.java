package magpiebridge.converter;

/**
 * This class represents the case when there is no position.
 *
 * @author Linghui Luo
 */
public class NoPositionInformation extends SourcePosition {

  private static final NoPositionInformation INSTANCE = new NoPositionInformation();

  private NoPositionInformation() {
    super(-1, -1, -1, -1);
  }

  public static NoPositionInformation getInstance() {
    return INSTANCE;
  }

  @Override
  public String toString() {
    return "No position info";
  }
}
