package org.jfree.chart.util;
import java.io.ObjectStreamException;
import java.io.Serializable;

final public class Layer implements Serializable  {
  final private static long serialVersionUID = -1470104570733183430L;
  final public static Layer FOREGROUND = new Layer("Layer.FOREGROUND");
  final public static Layer BACKGROUND = new Layer("Layer.BACKGROUND");
  private String name;
  private Layer(String name) {
    super();
    this.name = name;
  }
  private Object readResolve() throws ObjectStreamException {
    Layer result = null;
    if(this.equals(Layer.FOREGROUND)) {
      result = Layer.FOREGROUND;
    }
    else {
      Layer var_3617 = Layer.BACKGROUND;
      if(this.equals(var_3617)) {
        result = Layer.BACKGROUND;
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
    if(!(obj instanceof Layer)) {
      return false;
    }
    Layer layer = (Layer)obj;
    if(!this.name.equals(layer.name)) {
      return false;
    }
    return true;
  }
  public int hashCode() {
    return this.name.hashCode();
  }
}