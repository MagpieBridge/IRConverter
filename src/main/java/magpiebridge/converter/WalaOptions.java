package magpiebridge.converter;

public class WalaOptions {
  private boolean allowPhantomClass = false;
  private boolean useCustomizedWalaProperties = false;
  private String pathOfWalaProperties;

  public WalaOptions() {
    this.pathOfWalaProperties = "wala.properties";
  }

  public boolean allowPhantomClass() {
    return allowPhantomClass;
  }

  public boolean useCustomizedWalaProperties() {
    return useCustomizedWalaProperties;
  }

  public void setAllowPhantomClass(boolean allowPhantomClass) {
    this.allowPhantomClass = allowPhantomClass;
  }

  public void setCustomizedWalaProperties(String pathOfWalaProperties) {
    this.useCustomizedWalaProperties = true;
    this.pathOfWalaProperties = pathOfWalaProperties;
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
