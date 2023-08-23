package org.joda.time.chrono;
import java.util.HashMap;
import java.util.Map;
import org.joda.time.Chronology;
import org.joda.time.DateTimeConstants;
import org.joda.time.DateTimeZone;

final public class GregorianChronology extends BasicGJChronology  {
  final private static long serialVersionUID = -861407383323710522L;
  final private static long MILLIS_PER_YEAR = (long)(365.2425D * DateTimeConstants.MILLIS_PER_DAY);
  final private static long MILLIS_PER_MONTH = (long)(365.2425D * DateTimeConstants.MILLIS_PER_DAY / 12);
  final private static int DAYS_0000_TO_1970 = 719527;
  final private static int MIN_YEAR = -292275054;
  final private static int MAX_YEAR = 292278993;
  final private static GregorianChronology INSTANCE_UTC;
  final private static Map<DateTimeZone, GregorianChronology[]> cCache = new HashMap<DateTimeZone, GregorianChronology[]>();
  static {
    INSTANCE_UTC = getInstance(DateTimeZone.UTC);
  }
  private GregorianChronology(Chronology base, Object param, int minDaysInFirstWeek) {
    super(base, param, minDaysInFirstWeek);
  }
  public Chronology withUTC() {
    return INSTANCE_UTC;
  }
  public Chronology withZone(DateTimeZone zone) {
    if(zone == null) {
      zone = DateTimeZone.getDefault();
    }
    if(zone == getZone()) {
      return this;
    }
    return getInstance(zone);
  }
  public static GregorianChronology getInstance() {
    return getInstance(DateTimeZone.getDefault(), 4);
  }
  public static GregorianChronology getInstance(DateTimeZone zone) {
    return getInstance(zone, 4);
  }
  public static GregorianChronology getInstance(DateTimeZone zone, int minDaysInFirstWeek) {
    if(zone == null) {
      zone = DateTimeZone.getDefault();
    }
    GregorianChronology chrono;
    synchronized(cCache) {
      GregorianChronology[] chronos = cCache.get(zone);
      if(chronos == null) {
        chronos = new GregorianChronology[7];
        cCache.put(zone, chronos);
      }
      try {
        GregorianChronology var_261 = chronos[minDaysInFirstWeek - 1];
        chrono = var_261;
      }
      catch (ArrayIndexOutOfBoundsException e) {
        throw new IllegalArgumentException("Invalid min days in first week: " + minDaysInFirstWeek);
      }
      if(chrono == null) {
        if(zone == DateTimeZone.UTC) {
          chrono = new GregorianChronology(null, null, minDaysInFirstWeek);
        }
        else {
          chrono = getInstance(DateTimeZone.UTC, minDaysInFirstWeek);
          chrono = new GregorianChronology(ZonedChronology.getInstance(chrono, zone), null, minDaysInFirstWeek);
        }
        chronos[minDaysInFirstWeek - 1] = chrono;
      }
    }
    return chrono;
  }
  public static GregorianChronology getInstanceUTC() {
    return INSTANCE_UTC;
  }
  private Object readResolve() {
    Chronology base = getBase();
    int minDays = getMinimumDaysInFirstWeek();
    minDays = (minDays == 0 ? 4 : minDays);
    return base == null ? getInstance(DateTimeZone.UTC, minDays) : getInstance(base.getZone(), minDays);
  }
  boolean isLeapYear(int year) {
    return ((year & 3) == 0) && ((year % 100) != 0 || (year % 400) == 0);
  }
  int getMaxYear() {
    return MAX_YEAR;
  }
  int getMinYear() {
    return MIN_YEAR;
  }
  long calculateFirstDayOfYearMillis(int year) {
    int leapYears = year / 100;
    if(year < 0) {
      leapYears = ((year + 3) >> 2) - leapYears + ((leapYears + 3) >> 2) - 1;
    }
    else {
      leapYears = (year >> 2) - leapYears + (leapYears >> 2);
      if(isLeapYear(year)) {
        leapYears--;
      }
    }
    return (year * 365L + (leapYears - DAYS_0000_TO_1970)) * DateTimeConstants.MILLIS_PER_DAY;
  }
  long getApproxMillisAtEpochDividedByTwo() {
    return (1970L * MILLIS_PER_YEAR) / 2;
  }
  long getAverageMillisPerMonth() {
    return MILLIS_PER_MONTH;
  }
  long getAverageMillisPerYear() {
    return MILLIS_PER_YEAR;
  }
  long getAverageMillisPerYearDividedByTwo() {
    return MILLIS_PER_YEAR / 2;
  }
  protected void assemble(Fields fields) {
    if(getBase() == null) {
      super.assemble(fields);
    }
  }
}