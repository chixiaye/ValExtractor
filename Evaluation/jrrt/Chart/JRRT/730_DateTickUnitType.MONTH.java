package org.jfree.chart.axis;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.util.Calendar;

public class DateTickUnitType implements Serializable  {
  final public static DateTickUnitType YEAR = new DateTickUnitType("DateTickUnitType.YEAR", Calendar.YEAR);
  final public static DateTickUnitType MONTH = new DateTickUnitType("DateTickUnitType.MONTH", Calendar.MONTH);
  final public static DateTickUnitType DAY = new DateTickUnitType("DateTickUnitType.DAY", Calendar.DATE);
  final public static DateTickUnitType HOUR = new DateTickUnitType("DateTickUnitType.HOUR", Calendar.HOUR_OF_DAY);
  final public static DateTickUnitType MINUTE = new DateTickUnitType("DateTickUnitType.MINUTE", Calendar.MINUTE);
  final public static DateTickUnitType SECOND = new DateTickUnitType("DateTickUnitType.SECOND", Calendar.SECOND);
  final public static DateTickUnitType MILLISECOND = new DateTickUnitType("DateTickUnitType.MILLISECOND", Calendar.MILLISECOND);
  private String name;
  private int calendarField;
  private DateTickUnitType(String name, int calendarField) {
    super();
    this.name = name;
    this.calendarField = calendarField;
  }
  private Object readResolve() throws ObjectStreamException {
    if(this.equals(DateTickUnitType.YEAR)) {
      return DateTickUnitType.YEAR;
    }
    else {
      DateTickUnitType var_730 = DateTickUnitType.MONTH;
      if(this.equals(var_730)) {
        return DateTickUnitType.MONTH;
      }
      else 
        if(this.equals(DateTickUnitType.DAY)) {
          return DateTickUnitType.DAY;
        }
        else 
          if(this.equals(DateTickUnitType.HOUR)) {
            return DateTickUnitType.HOUR;
          }
          else 
            if(this.equals(DateTickUnitType.MINUTE)) {
              return DateTickUnitType.MINUTE;
            }
            else 
              if(this.equals(DateTickUnitType.SECOND)) {
                return DateTickUnitType.SECOND;
              }
              else 
                if(this.equals(DateTickUnitType.MILLISECOND)) {
                  return DateTickUnitType.MILLISECOND;
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
    if(!(obj instanceof DateTickUnitType)) {
      return false;
    }
    DateTickUnitType t = (DateTickUnitType)obj;
    if(!this.name.equals(t.toString())) {
      return false;
    }
    return true;
  }
  public int getCalendarField() {
    return this.calendarField;
  }
}