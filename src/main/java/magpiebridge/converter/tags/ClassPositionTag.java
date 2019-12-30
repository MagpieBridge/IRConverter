package magpiebridge.converter.tags;

import com.ibm.wala.cast.tree.CAstSourcePositionMap.Position;
import soot.tagkit.AttributeValueException;
import soot.tagkit.Tag;

/**
 * Tag used for storing position of a java source code class.
 *
 * @author Linghui Luo
 */
public class ClassPositionTag implements Tag {

  private Position pos;

  @Override
  public String getName() {
    return "ClassPositionTag";
  }

  @Override
  public byte[] getValue() throws AttributeValueException {
    return pos.toString().getBytes();
  }

  public ClassPositionTag(Position pos) {
    this.pos = pos;
  }

  public Position getPosition() {
    return this.pos;
  }

  @Override
  public String toString() {
    if (pos != null) {
      return pos.toString();
    } else {
      return "No position";
    }
  }
}
