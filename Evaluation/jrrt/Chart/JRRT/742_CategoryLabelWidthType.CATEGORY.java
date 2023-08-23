package org.jfree.chart.axis;
import java.io.ObjectStreamException;
import java.io.Serializable;

final public class CategoryLabelWidthType implements Serializable  {
  final private static long serialVersionUID = -6976024792582949656L;
  final public static CategoryLabelWidthType CATEGORY = new CategoryLabelWidthType("CategoryLabelWidthType.CATEGORY");
  final public static CategoryLabelWidthType RANGE = new CategoryLabelWidthType("CategoryLabelWidthType.RANGE");
  private String name;
  private CategoryLabelWidthType(String name) {
    super();
    if(name == null) {
      throw new IllegalArgumentException("Null \'name\' argument.");
    }
    this.name = name;
  }
  private Object readResolve() throws ObjectStreamException {
    CategoryLabelWidthType var_742 = CategoryLabelWidthType.CATEGORY;
    if(this.equals(var_742)) {
      return CategoryLabelWidthType.CATEGORY;
    }
    else 
      if(this.equals(CategoryLabelWidthType.RANGE)) {
        return CategoryLabelWidthType.RANGE;
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
    if(!(obj instanceof CategoryLabelWidthType)) {
      return false;
    }
    CategoryLabelWidthType t = (CategoryLabelWidthType)obj;
    if(!this.name.equals(t.toString())) {
      return false;
    }
    return true;
  }
}