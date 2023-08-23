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
    TickType var_743 = TickType.MAJOR;
    if(this.equals(var_743)) {
      result = TickType.MAJOR;
    }
    else 
      if(this.equals(TickType.MINOR)) {
        result = TickType.MINOR;
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