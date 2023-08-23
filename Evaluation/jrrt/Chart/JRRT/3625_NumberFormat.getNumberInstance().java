package org.jfree.chart.util;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.FieldPosition;
import java.text.NumberFormat;
import java.text.ParsePosition;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class RelativeDateFormat extends DateFormat  {
  private long baseMillis;
  private boolean showZeroDays;
  private boolean showZeroHours;
  private NumberFormat dayFormatter;
  private String positivePrefix;
  private String daySuffix;
  private NumberFormat hourFormatter;
  private String hourSuffix;
  private NumberFormat minuteFormatter;
  private String minuteSuffix;
  private NumberFormat secondFormatter;
  private String secondSuffix;
  private static long MILLISECONDS_IN_ONE_HOUR = 60 * 60 * 1000L;
  private static long MILLISECONDS_IN_ONE_DAY = 24 * MILLISECONDS_IN_ONE_HOUR;
  public RelativeDateFormat() {
    this(0L);
  }
  public RelativeDateFormat(Date time) {
    this(time.getTime());
  }
  public RelativeDateFormat(long baseMillis) {
    super();
    this.baseMillis = baseMillis;
    this.showZeroDays = false;
    this.showZeroHours = true;
    this.positivePrefix = "";
    this.dayFormatter = NumberFormat.getNumberInstance();
    this.daySuffix = "d";
    this.hourFormatter = NumberFormat.getNumberInstance();
    this.hourSuffix = "h";
    NumberFormat var_3625 = NumberFormat.getNumberInstance();
    this.minuteFormatter = var_3625;
    this.minuteSuffix = "m";
    this.secondFormatter = NumberFormat.getNumberInstance();
    this.secondFormatter.setMaximumFractionDigits(3);
    this.secondFormatter.setMinimumFractionDigits(3);
    this.secondSuffix = "s";
    this.calendar = new GregorianCalendar();
    this.numberFormat = new DecimalFormat("0");
  }
  public Date parse(String source, ParsePosition pos) {
    return null;
  }
  public Object clone() {
    RelativeDateFormat clone = (RelativeDateFormat)super.clone();
    clone.dayFormatter = (NumberFormat)this.dayFormatter.clone();
    clone.secondFormatter = (NumberFormat)this.secondFormatter.clone();
    return clone;
  }
  public String getDaySuffix() {
    return this.daySuffix;
  }
  public String getHourSuffix() {
    return this.hourSuffix;
  }
  public String getMinuteSuffix() {
    return this.minuteSuffix;
  }
  public String getPositivePrefix() {
    return this.positivePrefix;
  }
  public String getSecondSuffix() {
    return this.secondSuffix;
  }
  public StringBuffer format(Date date, StringBuffer toAppendTo, FieldPosition fieldPosition) {
    long currentMillis = date.getTime();
    long elapsed = currentMillis - this.baseMillis;
    String signPrefix;
    if(elapsed < 0) {
      elapsed *= -1L;
      signPrefix = "-";
    }
    else {
      signPrefix = this.positivePrefix;
    }
    long days = elapsed / MILLISECONDS_IN_ONE_DAY;
    elapsed = elapsed - (days * MILLISECONDS_IN_ONE_DAY);
    long hours = elapsed / MILLISECONDS_IN_ONE_HOUR;
    elapsed = elapsed - (hours * MILLISECONDS_IN_ONE_HOUR);
    long minutes = elapsed / 60000L;
    elapsed = elapsed - (minutes * 60000L);
    double seconds = elapsed / 1000.0D;
    toAppendTo.append(signPrefix);
    if(days != 0 || this.showZeroDays) {
      toAppendTo.append(this.dayFormatter.format(days) + getDaySuffix());
    }
    if(hours != 0 || this.showZeroHours) {
      toAppendTo.append(this.hourFormatter.format(hours) + getHourSuffix());
    }
    toAppendTo.append(this.minuteFormatter.format(minutes) + getMinuteSuffix());
    toAppendTo.append(this.secondFormatter.format(seconds) + getSecondSuffix());
    return toAppendTo;
  }
  public boolean equals(Object obj) {
    if(obj == this) {
      return true;
    }
    if(!(obj instanceof RelativeDateFormat)) {
      return false;
    }
    if(!super.equals(obj)) {
      return false;
    }
    RelativeDateFormat that = (RelativeDateFormat)obj;
    if(this.baseMillis != that.baseMillis) {
      return false;
    }
    if(this.showZeroDays != that.showZeroDays) {
      return false;
    }
    if(this.showZeroHours != that.showZeroHours) {
      return false;
    }
    if(!this.positivePrefix.equals(that.positivePrefix)) {
      return false;
    }
    if(!this.daySuffix.equals(that.daySuffix)) {
      return false;
    }
    if(!this.hourSuffix.equals(that.hourSuffix)) {
      return false;
    }
    if(!this.minuteSuffix.equals(that.minuteSuffix)) {
      return false;
    }
    if(!this.secondSuffix.equals(that.secondSuffix)) {
      return false;
    }
    if(!this.dayFormatter.equals(that.dayFormatter)) {
      return false;
    }
    if(!this.hourFormatter.equals(that.hourFormatter)) {
      return false;
    }
    if(!this.minuteFormatter.equals(that.minuteFormatter)) {
      return false;
    }
    if(!this.secondFormatter.equals(that.secondFormatter)) {
      return false;
    }
    return true;
  }
  public boolean getShowZeroDays() {
    return this.showZeroDays;
  }
  public boolean getShowZeroHours() {
    return this.showZeroHours;
  }
  public int hashCode() {
    int result = 193;
    result = 37 * result + (int)(this.baseMillis ^ (this.baseMillis >>> 32));
    result = 37 * result + this.positivePrefix.hashCode();
    result = 37 * result + this.daySuffix.hashCode();
    result = 37 * result + this.hourSuffix.hashCode();
    result = 37 * result + this.minuteSuffix.hashCode();
    result = 37 * result + this.secondSuffix.hashCode();
    result = 37 * result + this.secondFormatter.hashCode();
    return result;
  }
  public long getBaseMillis() {
    return this.baseMillis;
  }
  public static void main(String[] args) {
    GregorianCalendar c0 = new GregorianCalendar(2006, 10, 1, 0, 0, 0);
    GregorianCalendar c1 = new GregorianCalendar(2006, 10, 1, 11, 37, 43);
    c1.set(Calendar.MILLISECOND, 123);
    System.out.println("Default: ");
    RelativeDateFormat rdf = new RelativeDateFormat(c0.getTimeInMillis());
    System.out.println(rdf.format(c1.getTime()));
    System.out.println();
    System.out.println("Hide milliseconds: ");
    rdf.setSecondFormatter(new DecimalFormat("0"));
    System.out.println(rdf.format(c1.getTime()));
    System.out.println();
    System.out.println("Show zero day output: ");
    rdf.setShowZeroDays(true);
    System.out.println(rdf.format(c1.getTime()));
    System.out.println();
    System.out.println("Alternative suffixes: ");
    rdf.setShowZeroDays(false);
    rdf.setDaySuffix(":");
    rdf.setHourSuffix(":");
    rdf.setMinuteSuffix(":");
    rdf.setSecondSuffix("");
    System.out.println(rdf.format(c1.getTime()));
    System.out.println();
  }
  public void setBaseMillis(long baseMillis) {
    this.baseMillis = baseMillis;
  }
  public void setDayFormatter(NumberFormat formatter) {
    if(formatter == null) {
      throw new IllegalArgumentException("Null \'formatter\' argument.");
    }
    this.dayFormatter = formatter;
  }
  public void setDaySuffix(String suffix) {
    if(suffix == null) {
      throw new IllegalArgumentException("Null \'suffix\' argument.");
    }
    this.daySuffix = suffix;
  }
  public void setHourFormatter(NumberFormat formatter) {
    if(formatter == null) {
      throw new IllegalArgumentException("Null \'formatter\' argument.");
    }
    this.hourFormatter = formatter;
  }
  public void setHourSuffix(String suffix) {
    if(suffix == null) {
      throw new IllegalArgumentException("Null \'suffix\' argument.");
    }
    this.hourSuffix = suffix;
  }
  public void setMinuteFormatter(NumberFormat formatter) {
    if(formatter == null) {
      throw new IllegalArgumentException("Null \'formatter\' argument.");
    }
    this.minuteFormatter = formatter;
  }
  public void setMinuteSuffix(String suffix) {
    if(suffix == null) {
      throw new IllegalArgumentException("Null \'suffix\' argument.");
    }
    this.minuteSuffix = suffix;
  }
  public void setPositivePrefix(String prefix) {
    if(prefix == null) {
      throw new IllegalArgumentException("Null \'prefix\' argument.");
    }
    this.positivePrefix = prefix;
  }
  public void setSecondFormatter(NumberFormat formatter) {
    if(formatter == null) {
      throw new IllegalArgumentException("Null \'formatter\' argument.");
    }
    this.secondFormatter = formatter;
  }
  public void setSecondSuffix(String suffix) {
    if(suffix == null) {
      throw new IllegalArgumentException("Null \'suffix\' argument.");
    }
    this.secondSuffix = suffix;
  }
  public void setShowZeroDays(boolean show) {
    this.showZeroDays = show;
  }
  public void setShowZeroHours(boolean show) {
    this.showZeroHours = show;
  }
}