package magpiebridge.converter.sourceinfo;

import com.ibm.wala.cast.tree.CAstSourcePositionMap.Position;
import com.ibm.wala.classLoader.IMethod.SourcePosition;
import java.io.IOException;
import java.io.Reader;
import java.net.URL;

/**
 * This class represents the case when there is no position.
 *
 * @author Linghui Luo
 */
public class NoPositionInfo implements Position {

  private static final NoPositionInfo INSTANCE = new NoPositionInfo();

  private NoPositionInfo() {}

  public static NoPositionInfo getInstance() {
    return INSTANCE;
  }

  @Override
  public String toString() {
    return "No position info";
  }

  @Override
  public int getFirstLine() {
    return -1;
  }

  @Override
  public int getLastLine() {
    return -1;
  }

  @Override
  public int getFirstCol() {
    return -1;
  }

  @Override
  public int getLastCol() {
    return -1;
  }

  @Override
  public int getFirstOffset() {
    return -1;
  }

  @Override
  public int getLastOffset() {
    return -1;
  }

  @Override
  public int compareTo(SourcePosition o) {
    return -1;
  }

  @Override
  public URL getURL() {
    return null;
  }

  @Override
  public Reader getReader() throws IOException {
    return new Reader() {

      @Override
      public int read(char[] cbuf, int off, int len) throws IOException {
        return -1;
      }

      @Override
      public void close() throws IOException {}
    };
  }
}
