package org.jfree.chart.axis;
import java.io.ObjectStreamException;
import java.io.Serializable;

final public class CategoryAnchor implements Serializable  {
  final private static long serialVersionUID = -2604142742210173810L;
  final public static CategoryAnchor START = new CategoryAnchor("CategoryAnchor.START");
  final public static CategoryAnchor MIDDLE = new CategoryAnchor("CategoryAnchor.MIDDLE");
  final public static CategoryAnchor END = new CategoryAnchor("CategoryAnchor.END");
  private String name;
  private CategoryAnchor(String name) {
    super();
    this.name = name;
  }
  private Object readResolve() throws ObjectStreamException {
    if(this.equals(CategoryAnchor.START)) {
      return CategoryAnchor.START;
    }
    else {
      CategoryAnchor var_476 = CategoryAnchor.MIDDLE;
      if(this.equals(var_476)) {
        return CategoryAnchor.MIDDLE;
      }
      else 
        if(this.equals(CategoryAnchor.END)) {
          return CategoryAnchor.END;
        }
    }
    return null;
  }
  public String toString() {
    return this.name;
  }
  public boolean equals(Object obj) {
    if(this == obj) {
      return true;
    }
    if(!(obj instanceof CategoryAnchor)) {
      return false;
    }
    CategoryAnchor position = (CategoryAnchor)obj;
    if(!this.name.equals(position.toString())) {
      return false;
    }
    return true;
  }
}