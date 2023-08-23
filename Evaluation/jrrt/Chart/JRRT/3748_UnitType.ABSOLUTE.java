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
    UnitType var_3748 = UnitType.ABSOLUTE;
    if(this.equals(var_3748)) {
      return UnitType.ABSOLUTE;
    }
    else 
      if(this.equals(UnitType.RELATIVE)) {
        return UnitType.RELATIVE;
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