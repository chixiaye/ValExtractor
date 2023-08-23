package org.jfree.chart.plot;
import java.io.ObjectStreamException;
import java.io.Serializable;

final public class DialShape implements Serializable  {
  final private static long serialVersionUID = -3471933055190251131L;
  final public static DialShape CIRCLE = new DialShape("DialShape.CIRCLE");
  final public static DialShape CHORD = new DialShape("DialShape.CHORD");
  final public static DialShape PIE = new DialShape("DialShape.PIE");
  private String name;
  private DialShape(String name) {
    super();
    this.name = name;
  }
  private Object readResolve() throws ObjectStreamException {
    if(this.equals(DialShape.CIRCLE)) {
      return DialShape.CIRCLE;
    }
    else {
      DialShape var_1554 = DialShape.CHORD;
      if(this.equals(var_1554)) {
        return DialShape.CHORD;
      }
      else 
        if(this.equals(DialShape.PIE)) {
          return DialShape.PIE;
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
    if(!(obj instanceof DialShape)) {
      return false;
    }
    DialShape shape = (DialShape)obj;
    if(!this.name.equals(shape.toString())) {
      return false;
    }
    return true;
  }
  public int hashCode() {
    return this.name.hashCode();
  }
}