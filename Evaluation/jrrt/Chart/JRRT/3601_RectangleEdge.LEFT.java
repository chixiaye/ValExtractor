package org.jfree.chart.util;
import java.awt.geom.Rectangle2D;
import java.io.ObjectStreamException;
import java.io.Serializable;

final public class RectangleEdge implements Serializable  {
  final private static long serialVersionUID = -7400988293691093548L;
  final public static RectangleEdge TOP = new RectangleEdge("RectangleEdge.TOP");
  final public static RectangleEdge BOTTOM = new RectangleEdge("RectangleEdge.BOTTOM");
  final public static RectangleEdge LEFT = new RectangleEdge("RectangleEdge.LEFT");
  final public static RectangleEdge RIGHT = new RectangleEdge("RectangleEdge.RIGHT");
  private String name;
  private RectangleEdge(String name) {
    super();
    this.name = name;
  }
  private Object readResolve() throws ObjectStreamException {
    RectangleEdge result = null;
    if(this.equals(RectangleEdge.TOP)) {
      result = RectangleEdge.TOP;
    }
    else 
      if(this.equals(RectangleEdge.BOTTOM)) {
        result = RectangleEdge.BOTTOM;
      }
      else 
        if(this.equals(RectangleEdge.LEFT)) {
          result = RectangleEdge.LEFT;
        }
        else 
          if(this.equals(RectangleEdge.RIGHT)) {
            result = RectangleEdge.RIGHT;
          }
    return result;
  }
  public static RectangleEdge opposite(RectangleEdge edge) {
    RectangleEdge result = null;
    if(edge == RectangleEdge.TOP) {
      result = RectangleEdge.BOTTOM;
    }
    else 
      if(edge == RectangleEdge.BOTTOM) {
        result = RectangleEdge.TOP;
      }
      else {
        RectangleEdge var_3601 = RectangleEdge.LEFT;
        if(edge == var_3601) {
          result = RectangleEdge.RIGHT;
        }
        else 
          if(edge == RectangleEdge.RIGHT) {
            result = RectangleEdge.LEFT;
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
    if(!(obj instanceof RectangleEdge)) {
      return false;
    }
    RectangleEdge order = (RectangleEdge)obj;
    if(!this.name.equals(order.name)) {
      return false;
    }
    return true;
  }
  public static boolean isLeftOrRight(RectangleEdge edge) {
    return (edge == RectangleEdge.LEFT || edge == RectangleEdge.RIGHT);
  }
  public static boolean isTopOrBottom(RectangleEdge edge) {
    return (edge == RectangleEdge.TOP || edge == RectangleEdge.BOTTOM);
  }
  public static double coordinate(Rectangle2D rectangle, RectangleEdge edge) {
    double result = 0.0D;
    if(edge == RectangleEdge.TOP) {
      result = rectangle.getMinY();
    }
    else 
      if(edge == RectangleEdge.BOTTOM) {
        result = rectangle.getMaxY();
      }
      else 
        if(edge == RectangleEdge.LEFT) {
          result = rectangle.getMinX();
        }
        else 
          if(edge == RectangleEdge.RIGHT) {
            result = rectangle.getMaxX();
          }
    return result;
  }
  public int hashCode() {
    return this.name.hashCode();
  }
}