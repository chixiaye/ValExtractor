package org.jfree.chart.axis;
import java.io.Serializable;
import java.text.DateFormat;
import java.text.FieldPosition;
import java.text.NumberFormat;
import java.text.ParsePosition;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

public class QuarterDateFormat extends DateFormat implements Cloneable, Serializable  {
  final private static long serialVersionUID = -6738465248529797176L;
  final public static String[] REGULAR_QUARTERS = new String[]{ "1", "2", "3", "4" } ;
  final public static String[] ROMAN_QUARTERS = new String[]{ "I", "II", "III", "IV" } ;
  final public static String[] GREEK_QUARTERS = new String[]{ "\u0391", "\u0392", "\u0393", "\u0394" } ;
  private String[] quarters = REGULAR_QUARTERS;
  private boolean quarterFirst;
  public QuarterDateFormat() {
    this(TimeZone.getDefault());
  }
  public QuarterDateFormat(TimeZone zone) {
    this(zone, REGULAR_QUARTERS);
  }
  public QuarterDateFormat(TimeZone zone, String[] quarterSymbols) {
    this(zone, quarterSymbols, false);
  }
  public QuarterDateFormat(TimeZone zone, String[] quarterSymbols, boolean quarterFirst) {
    super();
    if(zone == null) {
      throw new IllegalArgumentException("Null \'zone\' argument.");
    }
    this.calendar = new GregorianCalendar(zone);
    this.quarters = quarterSymbols;
    this.quarterFirst = quarterFirst;
    this.numberFormat = NumberFormat.getNumberInstance();
  }
  public Date parse(String source, ParsePosition pos) {
    return null;
  }
  public StringBuffer format(Date date, StringBuffer toAppendTo, FieldPosition fieldPosition) {
    this.calendar.setTime(date);
    int year = this.calendar.get(Calendar.YEAR);
    int month = this.calendar.get(Calendar.MONTH);
    int quarter = month / 3;
    if(this.quarterFirst) {
      String[] var_722 = this.quarters;
      toAppendTo.append(var_722[quarter]);
      toAppendTo.append(" ");
      toAppendTo.append(year);
    }
    else {
      toAppendTo.append(year);
      toAppendTo.append(" ");
      toAppendTo.append(this.quarters[quarter]);
    }
    return toAppendTo;
  }
  public boolean equals(Object obj) {
    if(obj == this) {
      return true;
    }
    if(!(obj instanceof QuarterDateFormat)) {
      return false;
    }
    QuarterDateFormat that = (QuarterDateFormat)obj;
    if(!Arrays.equals(this.quarters, that.quarters)) {
      return false;
    }
    if(this.quarterFirst != that.quarterFirst) {
      return false;
    }
    return super.equals(obj);
  }
}