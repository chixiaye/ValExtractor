package org.joda.time.chrono;
import java.util.HashMap;
import java.util.Map;
import org.joda.time.Chronology;
import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;
import org.joda.time.DateTimeField;
import org.joda.time.DateTimeZone;
import org.joda.time.field.SkipDateTimeField;

final public class CopticChronology extends BasicFixedMonthChronology  {
  final private static long serialVersionUID = -5972804258688333942L;
  final public static int AM = DateTimeConstants.CE;
  final private static DateTimeField ERA_FIELD = new BasicSingleEraDateTimeField("AM");
  final private static int MIN_YEAR = -292269337;
  final private static int MAX_YEAR = 292272708;
  final private static Map<DateTimeZone, CopticChronology[]> cCache = new HashMap<DateTimeZone, CopticChronology[]>();
  final private static CopticChronology INSTANCE_UTC;
  static {
    INSTANCE_UTC = getInstance(DateTimeZone.UTC);
  }
  CopticChronology(Chronology base, Object param, int minDaysInFirstWeek) {
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
  public static CopticChronology getInstance() {
    return getInstance(DateTimeZone.getDefault(), 4);
  }
  public static CopticChronology getInstance(DateTimeZone zone) {
    return getInstance(zone, 4);
  }
  public static CopticChronology getInstance(DateTimeZone zone, int minDaysInFirstWeek) {
    if(zone == null) {
      zone = DateTimeZone.getDefault();
    }
    CopticChronology chrono;
    synchronized(cCache) {
      CopticChronology[] chronos = cCache.get(zone);
      if(chronos == null) {
        chronos = new CopticChronology[7];
        cCache.put(zone, chronos);
      }
      try {
        chrono = chronos[minDaysInFirstWeek - 1];
      }
      catch (ArrayIndexOutOfBoundsException e) {
        throw new IllegalArgumentException("Invalid min days in first week: " + minDaysInFirstWeek);
      }
      if(chrono == null) {
        DateTimeZone var_209 = DateTimeZone.UTC;
        if(zone == var_209) {
          chrono = new CopticChronology(null, null, minDaysInFirstWeek);
          DateTime lowerLimit = new DateTime(1, 1, 1, 0, 0, 0, 0, chrono);
          chrono = new CopticChronology(LimitChronology.getInstance(chrono, lowerLimit, null), null, minDaysInFirstWeek);
        }
        else {
          chrono = getInstance(DateTimeZone.UTC, minDaysInFirstWeek);
          chrono = new CopticChronology(ZonedChronology.getInstance(chrono, zone), null, minDaysInFirstWeek);
        }
        chronos[minDaysInFirstWeek - 1] = chrono;
      }
    }
    return chrono;
  }
  public static CopticChronology getInstanceUTC() {
    return INSTANCE_UTC;
  }
  private Object readResolve() {
    Chronology base = getBase();
    int minDays = getMinimumDaysInFirstWeek();
    minDays = (minDays == 0 ? 4 : minDays);
    return base == null ? getInstance(DateTimeZone.UTC, minDays) : getInstance(base.getZone(), minDays);
  }
  int getMaxYear() {
    return MAX_YEAR;
  }
  int getMinYear() {
    return MIN_YEAR;
  }
  long calculateFirstDayOfYearMillis(int year) {
    int relativeYear = year - 1687;
    int leapYears;
    if(relativeYear <= 0) {
      leapYears = (relativeYear + 3) >> 2;
    }
    else {
      leapYears = relativeYear >> 2;
      if(!isLeapYear(year)) {
        leapYears++;
      }
    }
    long millis = (relativeYear * 365L + leapYears) * (long)DateTimeConstants.MILLIS_PER_DAY;
    return millis + (365L - 112) * DateTimeConstants.MILLIS_PER_DAY;
  }
  long getApproxMillisAtEpochDividedByTwo() {
    return (1686L * MILLIS_PER_YEAR + 112L * DateTimeConstants.MILLIS_PER_DAY) / 2;
  }
  protected void assemble(Fields fields) {
    if(getBase() == null) {
      super.assemble(fields);
      fields.year = new SkipDateTimeField(this, fields.year);
      fields.weekyear = new SkipDateTimeField(this, fields.weekyear);
      fields.era = ERA_FIELD;
      fields.monthOfYear = new BasicMonthOfYearDateTimeField(this, 13);
      fields.months = fields.monthOfYear.getDurationField();
    }
  }
}