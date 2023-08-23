package org.jfree.chart.util;
import java.io.ObjectStreamException;
import java.io.Serializable;

final public class VerticalAlignment implements Serializable  {
  final private static long serialVersionUID = 7272397034325429853L;
  final public static VerticalAlignment TOP = new VerticalAlignment("VerticalAlignment.TOP");
  final public static VerticalAlignment BOTTOM = new VerticalAlignment("VerticalAlignment.BOTTOM");
  final public static VerticalAlignment CENTER = new VerticalAlignment("VerticalAlignment.CENTER");
  private String name;
  private VerticalAlignment(String name) {
    super();
    this.name = name;
  }
  private Object readResolve() throws ObjectStreamException {
    if(this.equals(VerticalAlignment.TOP)) {
      return VerticalAlignment.TOP;
    }
    else {
      VerticalAlignment var_3614 = VerticalAlignment.BOTTOM;
      if(this.equals(var_3614)) {
        return VerticalAlignment.BOTTOM;
      }
      else 
        if(this.equals(VerticalAlignment.CENTER)) {
          return VerticalAlignment.CENTER;
        }
        else {
          return null;
        }
    }
  }
  public String toString() {
    return this.name;
  }
  public boolean equals(Object obj) {
    if(this == obj) {
      return true;
    }
    if(!(obj instanceof VerticalAlignment)) {
      return false;
    }
    VerticalAlignment alignment = (VerticalAlignment)obj;
    if(!this.name.equals(alignment.name)) {
      return false;
    }
    return true;
  }
  public int hashCode() {
    return this.name.hashCode();
  }
}