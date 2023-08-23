package org.jfree.chart.util;
import java.io.ObjectStreamException;
import java.io.Serializable;

final public class UnitType implements Serializable  {
  final private static long serialVersionUID = 6531925392288519884L;
  final public static UnitType ABSOLUTE = new UnitType("UnitType.ABSOLUTE");
  final public static UnitType RELATIVE = new UnitType("UnitType.RELATIVE");
  private String name;
  private UnitType(String name) {
    super();
    this.name = name;
  }
  private Object readResolve() throws ObjectStreamException {
    if(this.equals(UnitType.ABSOLUTE)) {
      return UnitType.ABSOLUTE;
    }
    else {
      UnitType var_3749 = UnitType.RELATIVE;
      if(this.equals(var_3749)) {
        return UnitType.RELATIVE;
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
    if(!(obj instanceof UnitType)) {
      return false;
    }
    UnitType that = (UnitType)obj;
    if(!this.name.equals(that.name)) {
      return false;
    }
    return true;
  }
  public int hashCode() {
    return this.name.hashCode();
  }
}