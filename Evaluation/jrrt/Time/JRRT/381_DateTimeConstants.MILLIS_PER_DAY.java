package org.joda.time.chrono;
import java.util.Locale;
import org.joda.time.Chronology;
import org.joda.time.DateTimeConstants;
import org.joda.time.DateTimeField;
import org.joda.time.DateTimeFieldType;
import org.joda.time.DateTimeZone;
import org.joda.time.DurationField;
import org.joda.time.DurationFieldType;
import org.joda.time.field.DividedDateTimeField;
import org.joda.time.field.FieldUtils;
import org.joda.time.field.MillisDurationField;
import org.joda.time.field.OffsetDateTimeField;
import org.joda.time.field.PreciseDateTimeField;
import org.joda.time.field.PreciseDurationField;
import org.joda.time.field.RemainderDateTimeField;
import org.joda.time.field.ZeroIsMaxDateTimeField;

abstract class BasicChronology extends AssembledChronology  {
  final private static long serialVersionUID = 8283225332206808863L;
  final private static DurationField cMillisField;
  final private static DurationField cSecondsField;
  final private static DurationField cMinutesField;
  final private static DurationField cHoursField;
  final private static DurationField cHalfdaysField;
  final private static DurationField cDaysField;
  final private static DurationField cWeeksField;
  final private static DateTimeField cMillisOfSecondField;
  final private static DateTimeField cMillisOfDayField;
  final private static DateTimeField cSecondOfMinuteField;
  final private static DateTimeField cSecondOfDayField;
  final private static DateTimeField cMinuteOfHourField;
  final private static DateTimeField cMinuteOfDayField;
  final private static DateTimeField cHourOfDayField;
  final private static DateTimeField cHourOfHalfdayField;
  final private static DateTimeField cClockhourOfDayField;
  final private static DateTimeField cClockhourOfHalfdayField;
  final private static DateTimeField cHalfdayOfDayField;
  static {
    cMillisField = MillisDurationField.INSTANCE;
    cSecondsField = new PreciseDurationField(DurationFieldType.seconds(), DateTimeConstants.MILLIS_PER_SECOND);
    cMinutesField = new PreciseDurationField(DurationFieldType.minutes(), DateTimeConstants.MILLIS_PER_MINUTE);
    cHoursField = new PreciseDurationField(DurationFieldType.hours(), DateTimeConstants.MILLIS_PER_HOUR);
    cHalfdaysField = new PreciseDurationField(DurationFieldType.halfdays(), DateTimeConstants.MILLIS_PER_DAY / 2);
    cDaysField = new PreciseDurationField(DurationFieldType.days(), DateTimeConstants.MILLIS_PER_DAY);
    cWeeksField = new PreciseDurationField(DurationFieldType.weeks(), DateTimeConstants.MILLIS_PER_WEEK);
    cMillisOfSecondField = new PreciseDateTimeField(DateTimeFieldType.millisOfSecond(), cMillisField, cSecondsField);
    cMillisOfDayField = new PreciseDateTimeField(DateTimeFieldType.millisOfDay(), cMillisField, cDaysField);
    cSecondOfMinuteField = new PreciseDateTimeField(DateTimeFieldType.secondOfMinute(), cSecondsField, cMinutesField);
    cSecondOfDayField = new PreciseDateTimeField(DateTimeFieldType.secondOfDay(), cSecondsField, cDaysField);
    cMinuteOfHourField = new PreciseDateTimeField(DateTimeFieldType.minuteOfHour(), cMinutesField, cHoursField);
    cMinuteOfDayField = new PreciseDateTimeField(DateTimeFieldType.minuteOfDay(), cMinutesField, cDaysField);
    cHourOfDayField = new PreciseDateTimeField(DateTimeFieldType.hourOfDay(), cHoursField, cDaysField);
    cHourOfHalfdayField = new PreciseDateTimeField(DateTimeFieldType.hourOfHalfday(), cHoursField, cHalfdaysField);
    cClockhourOfDayField = new ZeroIsMaxDateTimeField(cHourOfDayField, DateTimeFieldType.clockhourOfDay());
    cClockhourOfHalfdayField = new ZeroIsMaxDateTimeField(cHourOfHalfdayField, DateTimeFieldType.clockhourOfHalfday());
    cHalfdayOfDayField = new HalfdayField();
  }
  final private static int CACHE_SIZE = 1 << 10;
  final private static int CACHE_MASK = CACHE_SIZE - 1;
  final private transient YearInfo[] iYearInfoCache = new YearInfo[CACHE_SIZE];
  final private int iMinDaysInFirstWeek;
  BasicChronology(Chronology base, Object param, int minDaysInFirstWeek) {
    super(base, param);
    if(minDaysInFirstWeek < 1 || minDaysInFirstWeek > 7) {
      throw new IllegalArgumentException("Invalid min days in first week: " + minDaysInFirstWeek);
    }
    iMinDaysInFirstWeek = minDaysInFirstWeek;
  }
  public DateTimeZone getZone() {
    Chronology base;
    if((base = getBase()) != null) {
      return base.getZone();
    }
    return DateTimeZone.UTC;
  }
  public String toString() {
    StringBuilder sb = new StringBuilder(60);
    String name = getClass().getName();
    int index = name.lastIndexOf('.');
    if(index >= 0) {
      name = name.substring(index + 1);
    }
    sb.append(name);
    sb.append('[');
    DateTimeZone zone = getZone();
    if(zone != null) {
      sb.append(zone.getID());
    }
    if(getMinimumDaysInFirstWeek() != 4) {
      sb.append(",mdfw=");
      sb.append(getMinimumDaysInFirstWeek());
    }
    sb.append(']');
    return sb.toString();
  }
  private YearInfo getYearInfo(int year) {
    YearInfo info = iYearInfoCache[year & CACHE_MASK];
    if(info == null || info.iYear != year) {
      info = new YearInfo(year, calculateFirstDayOfYearMillis(year));
      iYearInfoCache[year & CACHE_MASK] = info;
    }
    return info;
  }
  public boolean equals(Object obj) {
    if(this == obj) {
      return true;
    }
    if(obj != null && getClass() == obj.getClass()) {
      BasicChronology chrono = (BasicChronology)obj;
      return getMinimumDaysInFirstWeek() == chrono.getMinimumDaysInFirstWeek() && getZone().equals(chrono.getZone());
    }
    return false;
  }
  abstract boolean isLeapYear(int year);
  int getDayOfMonth(long millis) {
    int year = getYear(millis);
    int month = getMonthOfYear(millis, year);
    return getDayOfMonth(millis, year, month);
  }
  int getDayOfMonth(long millis, int year) {
    int month = getMonthOfYear(millis, year);
    return getDayOfMonth(millis, year, month);
  }
  int getDayOfMonth(long millis, int year, int month) {
    long dateMillis = getYearMillis(year);
    dateMillis += getTotalMillisByYearMonth(year, month);
    return (int)((millis - dateMillis) / DateTimeConstants.MILLIS_PER_DAY) + 1;
  }
  int getDayOfWeek(long instant) {
    long daysSince19700101;
    if(instant >= 0) {
      daysSince19700101 = instant / DateTimeConstants.MILLIS_PER_DAY;
    }
    else {
      daysSince19700101 = (instant - (DateTimeConstants.MILLIS_PER_DAY - 1)) / DateTimeConstants.MILLIS_PER_DAY;
      if(daysSince19700101 < -3) {
        return 7 + (int)((daysSince19700101 + 4) % 7);
      }
    }
    return 1 + (int)((daysSince19700101 + 3) % 7);
  }
  int getDayOfYear(long instant) {
    return getDayOfYear(instant, getYear(instant));
  }
  int getDayOfYear(long instant, int year) {
    long yearStart = getYearMillis(year);
    return (int)((instant - yearStart) / DateTimeConstants.MILLIS_PER_DAY) + 1;
  }
  int getDaysInMonthMax() {
    return 31;
  }
  abstract int getDaysInMonthMax(int month);
  int getDaysInMonthMax(long instant) {
    int thisYear = getYear(instant);
    int thisMonth = getMonthOfYear(instant, thisYear);
    return getDaysInYearMonth(thisYear, thisMonth);
  }
  int getDaysInMonthMaxForSet(long instant, int value) {
    return getDaysInMonthMax(instant);
  }
  int getDaysInYear(int year) {
    return isLeapYear(year) ? 366 : 365;
  }
  int getDaysInYearMax() {
    return 366;
  }
  abstract int getDaysInYearMonth(int year, int month);
  int getMaxMonth() {
    return 12;
  }
  int getMaxMonth(int year) {
    return getMaxMonth();
  }
  abstract int getMaxYear();
  int getMillisOfDay(long instant) {
    if(instant >= 0) {
      return (int)(instant % DateTimeConstants.MILLIS_PER_DAY);
    }
    else {
      return (DateTimeConstants.MILLIS_PER_DAY - 1) + (int)((instant + 1) % DateTimeConstants.MILLIS_PER_DAY);
    }
  }
  abstract int getMinYear();
  public int getMinimumDaysInFirstWeek() {
    return iMinDaysInFirstWeek;
  }
  int getMonthOfYear(long millis) {
    return getMonthOfYear(millis, getYear(millis));
  }
  abstract int getMonthOfYear(long millis, int year);
  int getWeekOfWeekyear(long instant) {
    return getWeekOfWeekyear(instant, getYear(instant));
  }
  int getWeekOfWeekyear(long instant, int year) {
    long firstWeekMillis1 = getFirstWeekOfYearMillis(year);
    if(instant < firstWeekMillis1) {
      return getWeeksInYear(year - 1);
    }
    long firstWeekMillis2 = getFirstWeekOfYearMillis(year + 1);
    if(instant >= firstWeekMillis2) {
      return 1;
    }
    return (int)((instant - firstWeekMillis1) / DateTimeConstants.MILLIS_PER_WEEK) + 1;
  }
  int getWeeksInYear(int year) {
    long firstWeekMillis1 = getFirstWeekOfYearMillis(year);
    long firstWeekMillis2 = getFirstWeekOfYearMillis(year + 1);
    return (int)((firstWeekMillis2 - firstWeekMillis1) / DateTimeConstants.MILLIS_PER_WEEK);
  }
  int getWeekyear(long instant) {
    int year = getYear(instant);
    int week = getWeekOfWeekyear(instant, year);
    if(week == 1) {
      return getYear(instant + DateTimeConstants.MILLIS_PER_WEEK);
    }
    else 
      if(week > 51) {
        return getYear(instant - (2 * DateTimeConstants.MILLIS_PER_WEEK));
      }
      else {
        return year;
      }
  }
  int getYear(long instant) {
    long unitMillis = getAverageMillisPerYearDividedByTwo();
    long i2 = (instant >> 1) + getApproxMillisAtEpochDividedByTwo();
    if(i2 < 0) {
      i2 = i2 - unitMillis + 1;
    }
    int year = (int)(i2 / unitMillis);
    long yearStart = getYearMillis(year);
    long diff = instant - yearStart;
    if(diff < 0) {
      year--;
    }
    else 
      if(diff >= DateTimeConstants.MILLIS_PER_DAY * 365L) {
        long oneYear;
        if(isLeapYear(year)) {
          int var_381 = DateTimeConstants.MILLIS_PER_DAY;
          oneYear = var_381 * 366L;
        }
        else {
          oneYear = DateTimeConstants.MILLIS_PER_DAY * 365L;
        }
        yearStart += oneYear;
        if(yearStart <= instant) {
          year++;
        }
      }
    return year;
  }
  public int hashCode() {
    return getClass().getName().hashCode() * 11 + getZone().hashCode() + getMinimumDaysInFirstWeek();
  }
  abstract long calculateFirstDayOfYearMillis(int year);
  abstract long getApproxMillisAtEpochDividedByTwo();
  abstract long getAverageMillisPerMonth();
  abstract long getAverageMillisPerYear();
  abstract long getAverageMillisPerYearDividedByTwo();
  long getDateMidnightMillis(int year, int monthOfYear, int dayOfMonth) {
    FieldUtils.verifyValueBounds(DateTimeFieldType.year(), year, getMinYear(), getMaxYear());
    FieldUtils.verifyValueBounds(DateTimeFieldType.monthOfYear(), monthOfYear, 1, getMaxMonth(year));
    FieldUtils.verifyValueBounds(DateTimeFieldType.dayOfMonth(), dayOfMonth, 1, getDaysInYearMonth(year, monthOfYear));
    return getYearMonthDayMillis(year, monthOfYear, dayOfMonth);
  }
  public long getDateTimeMillis(int year, int monthOfYear, int dayOfMonth, int millisOfDay) throws IllegalArgumentException {
    Chronology base;
    if((base = getBase()) != null) {
      return base.getDateTimeMillis(year, monthOfYear, dayOfMonth, millisOfDay);
    }
    FieldUtils.verifyValueBounds(DateTimeFieldType.millisOfDay(), millisOfDay, 0, DateTimeConstants.MILLIS_PER_DAY - 1);
    return getDateMidnightMillis(year, monthOfYear, dayOfMonth) + millisOfDay;
  }
  public long getDateTimeMillis(int year, int monthOfYear, int dayOfMonth, int hourOfDay, int minuteOfHour, int secondOfMinute, int millisOfSecond) throws IllegalArgumentException {
    Chronology base;
    if((base = getBase()) != null) {
      return base.getDateTimeMillis(year, monthOfYear, dayOfMonth, hourOfDay, minuteOfHour, secondOfMinute, millisOfSecond);
    }
    FieldUtils.verifyValueBounds(DateTimeFieldType.hourOfDay(), hourOfDay, 0, 23);
    FieldUtils.verifyValueBounds(DateTimeFieldType.minuteOfHour(), minuteOfHour, 0, 59);
    FieldUtils.verifyValueBounds(DateTimeFieldType.secondOfMinute(), secondOfMinute, 0, 59);
    FieldUtils.verifyValueBounds(DateTimeFieldType.millisOfSecond(), millisOfSecond, 0, 999);
    return getDateMidnightMillis(year, monthOfYear, dayOfMonth) + hourOfDay * DateTimeConstants.MILLIS_PER_HOUR + minuteOfHour * DateTimeConstants.MILLIS_PER_MINUTE + secondOfMinute * DateTimeConstants.MILLIS_PER_SECOND + millisOfSecond;
  }
  long getFirstWeekOfYearMillis(int year) {
    long jan1millis = getYearMillis(year);
    int jan1dayOfWeek = getDayOfWeek(jan1millis);
    if(jan1dayOfWeek > (8 - iMinDaysInFirstWeek)) {
      return jan1millis + (8 - jan1dayOfWeek) * (long)DateTimeConstants.MILLIS_PER_DAY;
    }
    else {
      return jan1millis - (jan1dayOfWeek - 1) * (long)DateTimeConstants.MILLIS_PER_DAY;
    }
  }
  abstract long getTotalMillisByYearMonth(int year, int month);
  abstract long getYearDifference(long minuendInstant, long subtrahendInstant);
  long getYearMillis(int year) {
    return getYearInfo(year).iFirstDayMillis;
  }
  long getYearMonthDayMillis(int year, int month, int dayOfMonth) {
    long millis = getYearMillis(year);
    millis += getTotalMillisByYearMonth(year, month);
    return millis + (dayOfMonth - 1) * (long)DateTimeConstants.MILLIS_PER_DAY;
  }
  long getYearMonthMillis(int year, int month) {
    long millis = getYearMillis(year);
    millis += getTotalMillisByYearMonth(year, month);
    return millis;
  }
  abstract long setYear(long instant, int year);
  protected void assemble(Fields fields) {
    fields.millis = cMillisField;
    fields.seconds = cSecondsField;
    fields.minutes = cMinutesField;
    fields.hours = cHoursField;
    fields.halfdays = cHalfdaysField;
    fields.days = cDaysField;
    fields.weeks = cWeeksField;
    fields.millisOfSecond = cMillisOfSecondField;
    fields.millisOfDay = cMillisOfDayField;
    fields.secondOfMinute = cSecondOfMinuteField;
    fields.secondOfDay = cSecondOfDayField;
    fields.minuteOfHour = cMinuteOfHourField;
    fields.minuteOfDay = cMinuteOfDayField;
    fields.hourOfDay = cHourOfDayField;
    fields.hourOfHalfday = cHourOfHalfdayField;
    fields.clockhourOfDay = cClockhourOfDayField;
    fields.clockhourOfHalfday = cClockhourOfHalfdayField;
    fields.halfdayOfDay = cHalfdayOfDayField;
    fields.year = new BasicYearDateTimeField(this);
    fields.yearOfEra = new GJYearOfEraDateTimeField(fields.year, this);
    DateTimeField field = new OffsetDateTimeField(fields.yearOfEra, 99);
    fields.centuryOfEra = new DividedDateTimeField(field, DateTimeFieldType.centuryOfEra(), 100);
    fields.centuries = fields.centuryOfEra.getDurationField();
    field = new RemainderDateTimeField((DividedDateTimeField)fields.centuryOfEra);
    fields.yearOfCentury = new OffsetDateTimeField(field, DateTimeFieldType.yearOfCentury(), 1);
    fields.era = new GJEraDateTimeField(this);
    fields.dayOfWeek = new GJDayOfWeekDateTimeField(this, fields.days);
    fields.dayOfMonth = new BasicDayOfMonthDateTimeField(this, fields.days);
    fields.dayOfYear = new BasicDayOfYearDateTimeField(this, fields.days);
    fields.monthOfYear = new GJMonthOfYearDateTimeField(this);
    fields.weekyear = new BasicWeekyearDateTimeField(this);
    fields.weekOfWeekyear = new BasicWeekOfWeekyearDateTimeField(this, fields.weeks);
    field = new RemainderDateTimeField(fields.weekyear, fields.centuries, DateTimeFieldType.weekyearOfCentury(), 100);
    fields.weekyearOfCentury = new OffsetDateTimeField(field, DateTimeFieldType.weekyearOfCentury(), 1);
    fields.years = fields.year.getDurationField();
    fields.months = fields.monthOfYear.getDurationField();
    fields.weekyears = fields.weekyear.getDurationField();
  }
  
  private static class HalfdayField extends PreciseDateTimeField  {
    @SuppressWarnings(value = {"unused", }) final private static long serialVersionUID = 581601443656929254L;
    HalfdayField() {
      super(DateTimeFieldType.halfdayOfDay(), cHalfdaysField, cDaysField);
    }
    public String getAsText(int fieldValue, Locale locale) {
      return GJLocaleSymbols.forLocale(locale).halfdayValueToText(fieldValue);
    }
    public int getMaximumTextLength(Locale locale) {
      return GJLocaleSymbols.forLocale(locale).getHalfdayMaxTextLength();
    }
    public long set(long millis, String text, Locale locale) {
      return set(millis, GJLocaleSymbols.forLocale(locale).halfdayTextToValue(text));
    }
  }
  
  private static class YearInfo  {
    final public int iYear;
    final public long iFirstDayMillis;
    YearInfo(int year, long firstDayMillis) {
      super();
      iYear = year;
      iFirstDayMillis = firstDayMillis;
    }
  }
}