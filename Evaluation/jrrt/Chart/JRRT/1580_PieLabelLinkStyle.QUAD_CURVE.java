package org.jfree.chart.plot;
import java.io.ObjectStreamException;
import java.io.Serializable;

final public class PieLabelLinkStyle implements Serializable  {
  final public static PieLabelLinkStyle STANDARD = new PieLabelLinkStyle("PieLabelLinkStyle.STANDARD");
  final public static PieLabelLinkStyle QUAD_CURVE = new PieLabelLinkStyle("PieLabelLinkStyle.QUAD_CURVE");
  final public static PieLabelLinkStyle CUBIC_CURVE = new PieLabelLinkStyle("PieLabelLinkStyle.CUBIC_CURVE");
  private String name;
  private PieLabelLinkStyle(String name) {
    super();
    this.name = name;
  }
  private Object readResolve() throws ObjectStreamException {
    Object result = null;
    if(this.equals(PieLabelLinkStyle.STANDARD)) {
      result = PieLabelLinkStyle.STANDARD;
    }
    else {
      PieLabelLinkStyle var_1580 = PieLabelLinkStyle.QUAD_CURVE;
      if(this.equals(var_1580)) {
        result = PieLabelLinkStyle.QUAD_CURVE;
      }
      else 
        if(this.equals(PieLabelLinkStyle.CUBIC_CURVE)) {
          result = PieLabelLinkStyle.CUBIC_CURVE;
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
    if(!(obj instanceof PieLabelLinkStyle)) {
      return false;
    }
    PieLabelLinkStyle style = (PieLabelLinkStyle)obj;
    if(!this.name.equals(style.toString())) {
      return false;
    }
    return true;
  }
  public int hashCode() {
    return this.name.hashCode();
  }
}