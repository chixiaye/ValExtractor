package org.jfree.chart.axis;
import java.io.ObjectStreamException;
import java.io.Serializable;

final public class DateTickMarkPosition implements Serializable  {
  final private static long serialVersionUID = 2540750672764537240L;
  final public static DateTickMarkPosition START = new DateTickMarkPosition("DateTickMarkPosition.START");
  final public static DateTickMarkPosition MIDDLE = new DateTickMarkPosition("DateTickMarkPosition.MIDDLE");
  final public static DateTickMarkPosition END = new DateTickMarkPosition("DateTickMarkPosition.END");
  private String name;
  private DateTickMarkPosition(String name) {
    super();
    this.name = name;
  }
  private Object readResolve() throws ObjectStreamException {
    if(this.equals(DateTickMarkPosition.START)) {
      return DateTickMarkPosition.START;
    }
    else 
      if(this.equals(DateTickMarkPosition.MIDDLE)) {
        return DateTickMarkPosition.MIDDLE;
      }
      else {
        DateTickMarkPosition var_605 = DateTickMarkPosition.END;
        if(this.equals(var_605)) {
          return DateTickMarkPosition.END;
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
    if(!(obj instanceof DateTickMarkPosition)) {
      return false;
    }
    DateTickMarkPosition position = (DateTickMarkPosition)obj;
    if(!this.name.equals(position.toString())) {
      return false;
    }
    return true;
  }
}