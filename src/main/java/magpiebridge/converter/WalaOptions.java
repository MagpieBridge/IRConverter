package magpiebridge.converter;

import java.io.File;

/**
 * Selected WALA options
 *
 * @author Linghui Luo
 */
public class WalaOptions {
  private boolean allowPhantomClass = false;
  private boolean useCustomizedWalaProperties = false;
  private String pathOfWalaProperties;

  public WalaOptions() {
    this.pathOfWalaProperties =
        System.getProperty("java.io.tmpdir") + File.separator + "wala.properties";
  }

  public boolean allowPhantomClass() {
    return allowPhantomClass;
  }

  public boolean useCustomizedWalaProperties() {
    return useCustomizedWalaProperties;
  }

  public WalaOptions setAllowPhantomClass(boolean allowPhantomClass) {
    this.allowPhantomClass = allowPhantomClass;
    return this;
  }

  public WalaOptions setCustomizedWalaProperties(String pathOfWalaProperties) {
    this.useCustomizedWalaProperties = true;
    this.pathOfWalaProperties = pathOfWalaProperties;
    return this;
  }

  /**
   * Gets the path of wala.properties file. Default is "wala.properties";
   *
   * @return the path of wala.properties file.
   */
  public String getPathOfWalaProperties() {
    return this.pathOfWalaProperties;
  }
}
