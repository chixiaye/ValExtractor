package org.jfree.chart.block;
import java.io.ObjectStreamException;
import java.io.Serializable;

final public class LengthConstraintType implements Serializable  {
  final private static long serialVersionUID = -1156658804028142978L;
  final public static LengthConstraintType NONE = new LengthConstraintType("LengthConstraintType.NONE");
  final public static LengthConstraintType RANGE = new LengthConstraintType("RectangleConstraintType.RANGE");
  final public static LengthConstraintType FIXED = new LengthConstraintType("LengthConstraintType.FIXED");
  private String name;
  private LengthConstraintType(String name) {
    super();
    this.name = name;
  }
  private Object readResolve() throws ObjectStreamException {
    if(this.equals(LengthConstraintType.NONE)) {
      return LengthConstraintType.NONE;
    }
    else 
      if(this.equals(LengthConstraintType.RANGE)) {
        return LengthConstraintType.RANGE;
      }
      else {
        LengthConstraintType var_899 = LengthConstraintType.FIXED;
        if(this.equals(var_899)) {
          return LengthConstraintType.FIXED;
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
    if(!(obj instanceof LengthConstraintType)) {
      return false;
    }
    LengthConstraintType that = (LengthConstraintType)obj;
    if(!this.name.equals(that.toString())) {
      return false;
    }
    return true;
  }
  public int hashCode() {
    return this.name.hashCode();
  }
}