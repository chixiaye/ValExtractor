package org.jfree.chart.util;
import java.io.ObjectStreamException;
import java.io.Serializable;

final public class XYCoordinateType implements Serializable  {
  final public static XYCoordinateType DATA = new XYCoordinateType("XYCoordinateType.DATA");
  final public static XYCoordinateType RELATIVE = new XYCoordinateType("XYCoordinateType.RELATIVE");
  final public static XYCoordinateType INDEX = new XYCoordinateType("XYCoordinateType.INDEX");
  private String name;
  private XYCoordinateType(String name) {
    super();
    this.name = name;
  }
  private Object readResolve() throws ObjectStreamException {
    XYCoordinateType var_3713 = XYCoordinateType.DATA;
    if(this.equals(var_3713)) {
      return XYCoordinateType.DATA;
    }
    else 
      if(this.equals(XYCoordinateType.RELATIVE)) {
        return XYCoordinateType.RELATIVE;
      }
      else 
        if(this.equals(XYCoordinateType.INDEX)) {
          return XYCoordinateType.INDEX;
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
    if(!(obj instanceof XYCoordinateType)) {
      return false;
    }
    XYCoordinateType order = (XYCoordinateType)obj;
    if(!this.name.equals(order.toString())) {
      return false;
    }
    return true;
  }
}