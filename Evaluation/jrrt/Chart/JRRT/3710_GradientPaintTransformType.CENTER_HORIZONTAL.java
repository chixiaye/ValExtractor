package org.jfree.chart.util;
import java.io.ObjectStreamException;
import java.io.Serializable;

final public class GradientPaintTransformType implements Serializable  {
  final private static long serialVersionUID = 8331561784933982450L;
  final public static GradientPaintTransformType VERTICAL = new GradientPaintTransformType("GradientPaintTransformType.VERTICAL");
  final public static GradientPaintTransformType HORIZONTAL = new GradientPaintTransformType("GradientPaintTransformType.HORIZONTAL");
  final public static GradientPaintTransformType CENTER_VERTICAL = new GradientPaintTransformType("GradientPaintTransformType.CENTER_VERTICAL");
  final public static GradientPaintTransformType CENTER_HORIZONTAL = new GradientPaintTransformType("GradientPaintTransformType.CENTER_HORIZONTAL");
  private String name;
  private GradientPaintTransformType(String name) {
    super();
    this.name = name;
  }
  private Object readResolve() throws ObjectStreamException {
    GradientPaintTransformType result = null;
    if(this.equals(GradientPaintTransformType.HORIZONTAL)) {
      result = GradientPaintTransformType.HORIZONTAL;
    }
    else 
      if(this.equals(GradientPaintTransformType.VERTICAL)) {
        result = GradientPaintTransformType.VERTICAL;
      }
      else {
        GradientPaintTransformType var_3710 = GradientPaintTransformType.CENTER_HORIZONTAL;
        if(this.equals(var_3710)) {
          result = GradientPaintTransformType.CENTER_HORIZONTAL;
        }
        else 
          if(this.equals(GradientPaintTransformType.CENTER_VERTICAL)) {
            result = GradientPaintTransformType.CENTER_VERTICAL;
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
    if(!(obj instanceof GradientPaintTransformType)) {
      return false;
    }
    GradientPaintTransformType t = (GradientPaintTransformType)obj;
    if(!this.name.equals(t.name)) {
      return false;
    }
    return true;
  }
  public int hashCode() {
    return this.name.hashCode();
  }
}