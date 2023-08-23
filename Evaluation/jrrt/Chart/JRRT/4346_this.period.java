package org.jfree.data.time;
import java.io.Serializable;

public class TimePeriodValue implements Cloneable, Serializable  {
  final private static long serialVersionUID = 3390443360845711275L;
  private TimePeriod period;
  private Number value;
  public TimePeriodValue(TimePeriod period, Number value) {
    super();
    if(period == null) {
      throw new IllegalArgumentException("Null \'period\' argument.");
    }
    this.period = period;
    this.value = value;
  }
  public TimePeriodValue(TimePeriod period, double value) {
    this(period, new Double(value));
  }
  public Number getValue() {
    return this.value;
  }
  public Object clone() {
    Object clone = null;
    try {
      clone = super.clone();
    }
    catch (CloneNotSupportedException e) {
      e.printStackTrace();
    }
    return clone;
  }
  public String toString() {
    return "TimePeriodValue[" + getPeriod() + "," + getValue() + "]";
  }
  public TimePeriod getPeriod() {
    return this.period;
  }
  public boolean equals(Object obj) {
    if(this == obj) {
      return true;
    }
    if(!(obj instanceof TimePeriodValue)) {
      return false;
    }
    TimePeriodValue timePeriodValue = (TimePeriodValue)obj;
    TimePeriod var_4346 = this.period;
    if(var_4346 != null ? !this.period.equals(timePeriodValue.period) : timePeriodValue.period != null) {
      return false;
    }
    if(this.value != null ? !this.value.equals(timePeriodValue.value) : timePeriodValue.value != null) {
      return false;
    }
    return true;
  }
  public int hashCode() {
    int result;
    result = (this.period != null ? this.period.hashCode() : 0);
    result = 29 * result + (this.value != null ? this.value.hashCode() : 0);
    return result;
  }
  public void setValue(Number value) {
    this.value = value;
  }
}