package org.joda.time.chrono;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import org.joda.time.Chronology;
import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;
import org.joda.time.DateTimeField;
import org.joda.time.DateTimeZone;

final public class IslamicChronology extends BasicChronology  {
  final private static long serialVersionUID = -3663823829888L;
  final public static int AH = DateTimeConstants.CE;
  final private static DateTimeField ERA_FIELD = new BasicSingleEraDateTimeField("AH");
  final public static LeapYearPatternType LEAP_YEAR_15_BASED = new LeapYearPatternType(0, 623158436);
  final public static LeapYearPatternType LEAP_YEAR_16_BASED = new LeapYearPatternType(1, 623191204);
  final public static LeapYearPatternType LEAP_YEAR_INDIAN = new LeapYearPatternType(2, 690562340);
  final public static LeapYearPatternType LEAP_YEAR_HABASH_AL_HASIB = new LeapYearPatternType(3, 153692453);
  final private static int MIN_YEAR = -292269337;
  final private static int MAX_YEAR = 292271022;
  final private static int MONTH_PAIR_LENGTH = 59;
  final private static int LONG_MONTH_LENGTH = 30;
  final private static int SHORT_MONTH_LENGTH = 29;
  final private static long MILLIS_PER_MONTH_PAIR = 59L * DateTimeConstants.MILLIS_PER_DAY;
  final private static long MILLIS_PER_MONTH = (long)(29.53056D * DateTimeConstants.MILLIS_PER_DAY);
  final private static long MILLIS_PER_LONG_MONTH = 30L * DateTimeConstants.MILLIS_PER_DAY;
  final private static long MILLIS_PER_YEAR = (long)(354.36667D * DateTimeConstants.MILLIS_PER_DAY);
  final private static long MILLIS_PER_SHORT_YEAR = 354L * DateTimeConstants.MILLIS_PER_DAY;
  final private static long MILLIS_PER_LONG_YEAR = 355L * DateTimeConstants.MILLIS_PER_DAY;
  final private static long MILLIS_YEAR_1 = -42521587200000L;
  final private static int CYCLE = 30;
  final private static long MILLIS_PER_CYCLE = ((19L * 354L + 11L * 355L) * DateTimeConstants.MILLIS_PER_DAY);
  final private static Map<DateTimeZone, IslamicChronology[]> cCache = new HashMap<DateTimeZone, IslamicChronology[]>();
  final private static IslamicChronology INSTANCE_UTC;
  static {
    INSTANCE_UTC = getInstance(DateTimeZone.UTC);
  }
  final private LeapYearPatternType iLeapYears;
  IslamicChronology(Chronology base, Object param, LeapYearPatternType leapYears) {
    super(base, param, 4);
    this.iLeapYears = leapYears;
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
  public static IslamicChronology getInstance() {
    return getInstance(DateTimeZone.getDefault(), LEAP_YEAR_16_BASED);
  }
  public static IslamicChronology getInstance(DateTimeZone zone) {
    return getInstance(zone, LEAP_YEAR_16_BASED);
  }
  public static IslamicChronology getInstance(DateTimeZone zone, LeapYearPatternType leapYears) {
    if(zone == null) {
      zone = DateTimeZone.getDefault();
    }
    IslamicChronology chrono;
    synchronized(cCache) {
      IslamicChronology[] chronos = cCache.get(zone);
      if(chronos == null) {
        chronos = new IslamicChronology[4];
        cCache.put(zone, chronos);
      }
      byte var_264 = leapYears.index;
      chrono = chronos[var_264];
      if(chrono == null) {
        if(zone == DateTimeZone.UTC) {
          chrono = new IslamicChronology(null, null, leapYears);
          DateTime lowerLimit = new DateTime(1, 1, 1, 0, 0, 0, 0, chrono);
          chrono = new IslamicChronology(LimitChronology.getInstance(chrono, lowerLimit, null), null, leapYears);
        }
        else {
          chrono = getInstance(DateTimeZone.UTC, leapYears);
          chrono = new IslamicChronology(ZonedChronology.getInstance(chrono, zone), null, leapYears);
        }
        chronos[leapYears.index] = chrono;
      }
    }
    return chrono;
  }
  public static IslamicChronology getInstanceUTC() {
    return INSTANCE_UTC;
  }
  public LeapYearPatternType getLeapYearPatternType() {
    return iLeapYears;
  }
  private Object readResolve() {
    Chronology base = getBase();
    return base == null ? getInstanceUTC() : getInstance(base.getZone());
  }
  public boolean equals(Object obj) {
    if(this == obj) {
      return true;
    }
    if(obj instanceof IslamicChronology) {
      IslamicChronology chrono = (IslamicChronology)obj;
      return getLeapYearPatternType().index == chrono.getLeapYearPatternType().index && super.equals(obj);
    }
    return false;
  }
  boolean isLeapYear(int year) {
    return iLeapYears.isLeapYear(year);
  }
  int getDayOfMonth(long millis) {
    int doy = getDayOfYear(millis) - 1;
    if(doy == 354) {
      return 30;
    }
    return (doy % MONTH_PAIR_LENGTH) % LONG_MONTH_LENGTH + 1;
  }
  int getDaysInMonthMax() {
    return LONG_MONTH_LENGTH;
  }
  int getDaysInMonthMax(int month) {
    if(month == 12) {
      return LONG_MONTH_LENGTH;
    }
    return (--month % 2 == 0 ? LONG_MONTH_LENGTH : SHORT_MONTH_LENGTH);
  }
  int getDaysInYear(int year) {
    return isLeapYear(year) ? 355 : 354;
  }
  int getDaysInYearMax() {
    return 355;
  }
  int getDaysInYearMonth(int year, int month) {
    if(month == 12 && isLeapYear(year)) {
      return LONG_MONTH_LENGTH;
    }
    return (--month % 2 == 0 ? LONG_MONTH_LENGTH : SHORT_MONTH_LENGTH);
  }
  int getMaxYear() {
    return MAX_YEAR;
  }
  int getMinYear() {
    return 1;
  }
  int getMonthOfYear(long millis, int year) {
    int doyZeroBased = (int)((millis - getYearMillis(year)) / DateTimeConstants.MILLIS_PER_DAY);
    if(doyZeroBased == 354) {
      return 12;
    }
    return ((doyZeroBased * 2) / MONTH_PAIR_LENGTH) + 1;
  }
  int getYear(long instant) {
    long millisIslamic = instant - MILLIS_YEAR_1;
    long cycles = millisIslamic / MILLIS_PER_CYCLE;
    long cycleRemainder = millisIslamic % MILLIS_PER_CYCLE;
    int year = (int)((cycles * CYCLE) + 1L);
    long yearMillis = (isLeapYear(year) ? MILLIS_PER_LONG_YEAR : MILLIS_PER_SHORT_YEAR);
    while(cycleRemainder >= yearMillis){
      cycleRemainder -= yearMillis;
      yearMillis = (isLeapYear(++year) ? MILLIS_PER_LONG_YEAR : MILLIS_PER_SHORT_YEAR);
    }
    return year;
  }
  public int hashCode() {
    return super.hashCode() * 13 + getLeapYearPatternType().hashCode();
  }
  long calculateFirstDayOfYearMillis(int year) {
    if(year > MAX_YEAR) {
      throw new ArithmeticException("Year is too large: " + year + " > " + MAX_YEAR);
    }
    if(year < MIN_YEAR) {
      throw new ArithmeticException("Year is too small: " + year + " < " + MIN_YEAR);
    }
    year--;
    long cycle = year / CYCLE;
    long millis = MILLIS_YEAR_1 + cycle * MILLIS_PER_CYCLE;
    int cycleRemainder = (year % CYCLE) + 1;
    for(int i = 1; i < cycleRemainder; i++) {
      millis += (isLeapYear(i) ? MILLIS_PER_LONG_YEAR : MILLIS_PER_SHORT_YEAR);
    }
    return millis;
  }
  long getApproxMillisAtEpochDividedByTwo() {
    return (-MILLIS_YEAR_1) / 2;
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
  long getTotalMillisByYearMonth(int year, int month) {
    if(--month % 2 == 1) {
      month /= 2;
      return month * MILLIS_PER_MONTH_PAIR + MILLIS_PER_LONG_MONTH;
    }
    else {
      month /= 2;
      return month * MILLIS_PER_MONTH_PAIR;
    }
  }
  long getYearDifference(long minuendInstant, long subtrahendInstant) {
    int minuendYear = getYear(minuendInstant);
    int subtrahendYear = getYear(subtrahendInstant);
    long minuendRem = minuendInstant - getYearMillis(minuendYear);
    long subtrahendRem = subtrahendInstant - getYearMillis(subtrahendYear);
    int difference = minuendYear - subtrahendYear;
    if(minuendRem < subtrahendRem) {
      difference--;
    }
    return difference;
  }
  long setYear(long instant, int year) {
    int thisYear = getYear(instant);
    int dayOfYear = getDayOfYear(instant, thisYear);
    int millisOfDay = getMillisOfDay(instant);
    if(dayOfYear > 354) {
      if(!isLeapYear(year)) {
        dayOfYear--;
      }
    }
    instant = getYearMonthDayMillis(year, 1, dayOfYear);
    instant += millisOfDay;
    return instant;
  }
  protected void assemble(Fields fields) {
    if(getBase() == null) {
      super.assemble(fields);
      fields.era = ERA_FIELD;
      fields.monthOfYear = new BasicMonthOfYearDateTimeField(this, 12);
      fields.months = fields.monthOfYear.getDurationField();
    }
  }
  
  public static class LeapYearPatternType implements Serializable  {
    final private static long serialVersionUID = 26581275372698L;
    final byte index;
    final int pattern;
    LeapYearPatternType(int index, int pattern) {
      super();
      this.index = (byte)index;
      this.pattern = pattern;
    }
    private Object readResolve() {
      switch (index){
        case 0:
        return LEAP_YEAR_15_BASED;
        case 1:
        return LEAP_YEAR_16_BASED;
        case 2:
        return LEAP_YEAR_INDIAN;
        case 3:
        return LEAP_YEAR_HABASH_AL_HASIB;
        default:
        return this;
      }
    }
    @Override() public boolean equals(Object obj) {
      if(obj instanceof LeapYearPatternType) {
        return index == ((LeapYearPatternType)obj).index;
      }
      return false;
    }
    boolean isLeapYear(int year) {
      int key = 1 << (year % 30);
      return ((pattern & key) > 0);
    }
    @Override() public int hashCode() {
      return index;
    }
  }
}