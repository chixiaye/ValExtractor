package org.jfree.chart.util;
import java.io.ObjectStreamException;
import java.io.Serializable;

final public class HorizontalAlignment implements Serializable  {
  final private static long serialVersionUID = -8249740987565309567L;
  final public static HorizontalAlignment LEFT = new HorizontalAlignment("HorizontalAlignment.LEFT");
  final public static HorizontalAlignment RIGHT = new HorizontalAlignment("HorizontalAlignment.RIGHT");
  final public static HorizontalAlignment CENTER = new HorizontalAlignment("HorizontalAlignment.CENTER");
  private String name;
  private HorizontalAlignment(String name) {
    super();
    this.name = name;
  }
  private Object readResolve() throws ObjectStreamException {
    HorizontalAlignment result = null;
    if(this.equals(HorizontalAlignment.LEFT)) {
      result = HorizontalAlignment.LEFT;
    }
    else 
      if(this.equals(HorizontalAlignment.RIGHT)) {
        result = HorizontalAlignment.RIGHT;
      }
      else {
        HorizontalAlignment var_3703 = HorizontalAlignment.CENTER;
        if(this.equals(var_3703)) {
          result = HorizontalAlignment.CENTER;
        }
      }
    return result;
  }
  public String toString() {
    return this.name;
  }
  public boolean equals(Object obj) {
    if(this == obj) {
      return true;
    }
    if(!(obj instanceof HorizontalAlignment)) {
      return false;
    }
    HorizontalAlignment that = (HorizontalAlignment)obj;
    if(!this.name.equals(that.name)) {
      return false;
    }
    return true;
  }
  public int hashCode() {
    return this.name.hashCode();
  }
}