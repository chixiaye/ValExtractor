package org.jfree.chart.util;
import java.io.ObjectStreamException;
import java.io.Serializable;

final public class LengthAdjustmentType implements Serializable  {
  final private static long serialVersionUID = -6097408511380545010L;
  final public static LengthAdjustmentType NO_CHANGE = new LengthAdjustmentType("NO_CHANGE");
  final public static LengthAdjustmentType EXPAND = new LengthAdjustmentType("EXPAND");
  final public static LengthAdjustmentType CONTRACT = new LengthAdjustmentType("CONTRACT");
  private String name;
  private LengthAdjustmentType(String name) {
    super();
    this.name = name;
  }
  private Object readResolve() throws ObjectStreamException {
    if(this.equals(LengthAdjustmentType.NO_CHANGE)) {
      return LengthAdjustmentType.NO_CHANGE;
    }
    else 
      if(this.equals(LengthAdjustmentType.EXPAND)) {
        return LengthAdjustmentType.EXPAND;
      }
      else {
        LengthAdjustmentType var_3725 = LengthAdjustmentType.CONTRACT;
        if(this.equals(var_3725)) {
          return LengthAdjustmentType.CONTRACT;
        }
      }
    return null;
  }
  public String toString() {
    return this.name;
  }
  public boolean equals(Object obj) {
    if(obj == this) {
      return true;
    }
    if(!(obj instanceof LengthAdjustmentType)) {
      return false;
    }
    final LengthAdjustmentType that = (LengthAdjustmentType)obj;
    if(!this.name.equals(that.name)) {
      return false;
    }
    return true;
  }
  public int hashCode() {
    return this.name.hashCode();
  }
}