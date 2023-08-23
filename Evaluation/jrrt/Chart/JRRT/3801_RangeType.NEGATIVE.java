package org.jfree.data;
import java.io.ObjectStreamException;
import java.io.Serializable;

final public class RangeType implements Serializable  {
  final private static long serialVersionUID = -9073319010650549239L;
  final public static RangeType FULL = new RangeType("RangeType.FULL");
  final public static RangeType POSITIVE = new RangeType("RangeType.POSITIVE");
  final public static RangeType NEGATIVE = new RangeType("RangeType.NEGATIVE");
  private String name;
  private RangeType(String name) {
    super();
    this.name = name;
  }
  private Object readResolve() throws ObjectStreamException {
    if(this.equals(RangeType.FULL)) {
      return RangeType.FULL;
    }
    else 
      if(this.equals(RangeType.POSITIVE)) {
        return RangeType.POSITIVE;
      }
      else {
        RangeType var_3801 = RangeType.NEGATIVE;
        if(this.equals(var_3801)) {
          return RangeType.NEGATIVE;
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
    if(!(obj instanceof RangeType)) {
      return false;
    }
    RangeType that = (RangeType)obj;
    if(!this.name.equals(that.toString())) {
      return false;
    }
    return true;
  }
  public int hashCode() {
    return this.name.hashCode();
  }
}