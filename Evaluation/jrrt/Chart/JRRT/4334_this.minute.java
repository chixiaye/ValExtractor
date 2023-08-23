package org.jfree.data.time;
import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class Minute extends RegularTimePeriod implements Serializable  {
  final private static long serialVersionUID = 2144572840034842871L;
  final public static int FIRST_MINUTE_IN_HOUR = 0;
  final public static int LAST_MINUTE_IN_HOUR = 59;
  private Day day;
  private byte hour;
  private byte minute;
  private long firstMillisecond;
  private long lastMillisecond;
  public Minute() {
    this(new Date());
  }
  public Minute(Date time) {
    this(time, TimeZone.getDefault());
  }
  public Minute(Date time, TimeZone zone) {
    super();
    if(time == null) {
      throw new IllegalArgumentException("Null \'time\' argument.");
    }
    if(zone == null) {
      throw new IllegalArgumentException("Null \'zone\' argument.");
    }
    Calendar calendar = Calendar.getInstance(zone);
    calendar.setTime(time);
    int min = calendar.get(Calendar.MINUTE);
    this.minute = (byte)min;
    this.hour = (byte)calendar.get(Calendar.HOUR_OF_DAY);
    this.day = new Day(time, zone);
    peg(calendar);
  }
  public Minute(int minute, Hour hour) {
    super();
    if(hour == null) {
      throw new IllegalArgumentException("Null \'hour\' argument.");
    }
    this.minute = (byte)minute;
    this.hour = (byte)hour.getHour();
    this.day = hour.getDay();
    peg(Calendar.getInstance());
  }
  public Minute(int minute, int hour, int day, int month, int year) {
    this(minute, new Hour(hour, new Day(day, month, year)));
  }
  public Day getDay() {
    return this.day;
  }
  public Hour getHour() {
    return new Hour(this.hour, this.day);
  }
  public static Minute parseMinute(String s) {
    Minute result = null;
    s = s.trim();
    String daystr = s.substring(0, Math.min(10, s.length()));
    Day day = Day.parseDay(daystr);
    if(day != null) {
      String hmstr = s.substring(Math.min(daystr.length() + 1, s.length()), s.length());
      hmstr = hmstr.trim();
      String hourstr = hmstr.substring(0, Math.min(2, hmstr.length()));
      int hour = Integer.parseInt(hourstr);
      if((hour >= 0) && (hour <= 23)) {
        String minstr = hmstr.substring(Math.min(hourstr.length() + 1, hmstr.length()), hmstr.length());
        int minute = Integer.parseInt(minstr);
        if((minute >= 0) && (minute <= 59)) {
          result = new Minute(minute, new Hour(hour, day));
        }
      }
    }
    return result;
  }
  public RegularTimePeriod next() {
    Minute result;
    if(this.minute != LAST_MINUTE_IN_HOUR) {
      result = new Minute(this.minute + 1, getHour());
    }
    else {
      Hour nextHour = (Hour)getHour().next();
      if(nextHour != null) {
        result = new Minute(FIRST_MINUTE_IN_HOUR, nextHour);
      }
      else {
        result = null;
      }
    }
    return result;
  }
  public RegularTimePeriod previous() {
    Minute result;
    byte var_4334 = this.minute;
    if(var_4334 != FIRST_MINUTE_IN_HOUR) {
      result = new Minute(this.minute - 1, getHour());
    }
    else {
      Hour h = (Hour)getHour().previous();
      if(h != null) {
        result = new Minute(LAST_MINUTE_IN_HOUR, h);
      }
      else {
        result = null;
      }
    }
    return result;
  }
  public boolean equals(Object obj) {
    if(obj == this) {
      return true;
    }
    if(!(obj instanceof Minute)) {
      return false;
    }
    Minute that = (Minute)obj;
    if(this.minute != that.minute) {
      return false;
    }
    if(this.hour != that.hour) {
      return false;
    }
    return true;
  }
  public int compareTo(Object o1) {
    int result;
    if(o1 instanceof Minute) {
      Minute m = (Minute)o1;
      result = getHour().compareTo(m.getHour());
      if(result == 0) {
        result = this.minute - m.getMinute();
      }
    }
    else 
      if(o1 instanceof RegularTimePeriod) {
        result = 0;
      }
      else {
        result = 1;
      }
    return result;
  }
  public int getHourValue() {
    return this.hour;
  }
  public int getMinute() {
    return this.minute;
  }
  public int hashCode() {
    int result = 17;
    result = 37 * result + this.minute;
    result = 37 * result + this.hour;
    result = 37 * result + this.day.hashCode();
    return result;
  }
  public long getFirstMillisecond() {
    return this.firstMillisecond;
  }
  public long getFirstMillisecond(Calendar calendar) {
    int year = this.day.getYear();
    int month = this.day.getMonth() - 1;
    int day = this.day.getDayOfMonth();
    calendar.clear();
    calendar.set(year, month, day, this.hour, this.minute, 0);
    calendar.set(Calendar.MILLISECOND, 0);
    return calendar.getTime().getTime();
  }
  public long getLastMillisecond() {
    return this.lastMillisecond;
  }
  public long getLastMillisecond(Calendar calendar) {
    int year = this.day.getYear();
    int month = this.day.getMonth() - 1;
    int day = this.day.getDayOfMonth();
    calendar.clear();
    calendar.set(year, month, day, this.hour, this.minute, 59);
    calendar.set(Calendar.MILLISECOND, 999);
    return calendar.getTime().getTime();
  }
  public long getSerialIndex() {
    long hourIndex = this.day.getSerialIndex() * 24L + this.hour;
    return hourIndex * 60L + this.minute;
  }
  public void peg(Calendar calendar) {
    this.firstMillisecond = getFirstMillisecond(calendar);
    this.lastMillisecond = getLastMillisecond(calendar);
  }
}