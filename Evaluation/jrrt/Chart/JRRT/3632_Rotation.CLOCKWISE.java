package org.jfree.chart.util;
import java.io.ObjectStreamException;
import java.io.Serializable;

final public class Rotation implements Serializable  {
  final private static long serialVersionUID = -4662815260201591676L;
  final public static Rotation CLOCKWISE = new Rotation("Rotation.CLOCKWISE", -1.0D);
  final public static Rotation ANTICLOCKWISE = new Rotation("Rotation.ANTICLOCKWISE", 1.0D);
  private String name;
  private double factor;
  private Rotation(final String name, final double factor) {
    super();
    this.name = name;
    this.factor = factor;
  }
  private Object readResolve() throws ObjectStreamException {
    Rotation var_3632 = Rotation.CLOCKWISE;
    if(this.equals(var_3632)) {
      return Rotation.CLOCKWISE;
    }
    else 
      if(this.equals(Rotation.ANTICLOCKWISE)) {
        return Rotation.ANTICLOCKWISE;
      }
    return null;
  }
  public String toString() {
    return this.name;
  }
  public boolean equals(final Object o) {
    if(this == o) {
      return true;
    }
    if(!(o instanceof Rotation)) {
      return false;
    }
    final Rotation rotation = (Rotation)o;
    if(this.factor != rotation.factor) {
      return false;
    }
    return true;
  }
  public double getFactor() {
    return this.factor;
  }
  public int hashCode() {
    final long temp = Double.doubleToLongBits(this.factor);
    return (int)(temp ^ (temp >>> 32));
  }
}