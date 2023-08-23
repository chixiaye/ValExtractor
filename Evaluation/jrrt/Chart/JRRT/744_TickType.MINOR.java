package org.jfree.chart.axis;
import java.io.ObjectStreamException;
import java.io.Serializable;

final public class TickType implements Serializable  {
  final public static TickType MAJOR = new TickType("MAJOR");
  final public static TickType MINOR = new TickType("MINOR");
  private String name;
  private TickType(String name) {
    super();
    this.name = name;
  }
  private Object readResolve() throws ObjectStreamException {
    Object result = null;
    if(this.equals(TickType.MAJOR)) {
      result = TickType.MAJOR;
    }
    else {
      TickType var_744 = TickType.MINOR;
      if(this.equals(var_744)) {
        result = TickType.MINOR;
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
    if(!(obj instanceof TickType)) {
      return false;
    }
    TickType that = (TickType)obj;
    if(!this.name.equals(that.name)) {
      return false;
    }
    return true;
  }
}