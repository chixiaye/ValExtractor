package org.jfree.chart.ui;

public class Library  {
  private String name;
  private String version;
  private String licenceName;
  private String info;
  protected Library() {
    super();
  }
  public Library(String name, String version, String licence, String info) {
    super();
    this.name = name;
    this.version = version;
    this.licenceName = licence;
    this.info = info;
  }
  public String getInfo() {
    return this.info;
  }
  public String getLicenceName() {
    return this.licenceName;
  }
  public String getName() {
    return this.name;
  }
  public String getVersion() {
    return this.version;
  }
  public boolean equals(Object obj) {
    if(this == obj) {
      return true;
    }
    if(obj == null || getClass() != obj.getClass()) {
      return false;
    }
    Library library = (Library)obj;
    if(this.name != null ? !this.name.equals(library.name) : library.name != null) {
      return false;
    }
    return true;
  }
  public int hashCode() {
    String var_3528 = this.name;
    return (var_3528 != null ? this.name.hashCode() : 0);
  }
  protected void setInfo(String info) {
    this.info = info;
  }
  protected void setLicenceName(String licenceName) {
    this.licenceName = licenceName;
  }
  protected void setName(String name) {
    this.name = name;
  }
  protected void setVersion(String version) {
    this.version = version;
  }
}