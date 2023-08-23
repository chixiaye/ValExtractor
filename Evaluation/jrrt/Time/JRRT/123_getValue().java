package org.joda.time;
import org.joda.convert.FromString;
import org.joda.convert.ToString;
import org.joda.time.base.BaseSingleFieldPeriod;
import org.joda.time.field.FieldUtils;
import org.joda.time.format.ISOPeriodFormat;
import org.joda.time.format.PeriodFormatter;

final public class Minutes extends BaseSingleFieldPeriod  {
  final public static Minutes ZERO = new Minutes(0);
  final public static Minutes ONE = new Minutes(1);
  final public static Minutes TWO = new Minutes(2);
  final public static Minutes THREE = new Minutes(3);
  final public static Minutes MAX_VALUE = new Minutes(Integer.MAX_VALUE);
  final public static Minutes MIN_VALUE = new Minutes(Integer.MIN_VALUE);
  final private static PeriodFormatter PARSER = ISOPeriodFormat.standard().withParseType(PeriodType.minutes());
  final private static long serialVersionUID = 87525275727380863L;
  private Minutes(int minutes) {
    super(minutes);
  }
  public Days toStandardDays() {
    return Days.days(getValue() / DateTimeConstants.MINUTES_PER_DAY);
  }
  public Duration toStandardDuration() {
    long minutes = getValue();
    return new Duration(minutes * DateTimeConstants.MILLIS_PER_MINUTE);
  }
  public DurationFieldType getFieldType() {
    return DurationFieldType.minutes();
  }
  public Hours toStandardHours() {
    return Hours.hours(getValue() / DateTimeConstants.MINUTES_PER_HOUR);
  }
  public Minutes dividedBy(int divisor) {
    if(divisor == 1) {
      return this;
    }
    return Minutes.minutes(getValue() / divisor);
  }
  public Minutes minus(int minutes) {
    return plus(FieldUtils.safeNegate(minutes));
  }
  public Minutes minus(Minutes minutes) {
    if(minutes == null) {
      return this;
    }
    return minus(minutes.getValue());
  }
  public static Minutes minutes(int minutes) {
    switch (minutes){
      case 0:
      return ZERO;
      case 1:
      return ONE;
      case 2:
      return TWO;
      case 3:
      return THREE;
      case Integer.MAX_VALUE:
      return MAX_VALUE;
      case Integer.MIN_VALUE:
      return MIN_VALUE;
      default:
      return new Minutes(minutes);
    }
  }
  public static Minutes minutesBetween(ReadableInstant start, ReadableInstant end) {
    int amount = BaseSingleFieldPeriod.between(start, end, DurationFieldType.minutes());
    return Minutes.minutes(amount);
  }
  public static Minutes minutesBetween(ReadablePartial start, ReadablePartial end) {
    if(start instanceof LocalTime && end instanceof LocalTime) {
      Chronology chrono = DateTimeUtils.getChronology(start.getChronology());
      int minutes = chrono.minutes().getDifference(((LocalTime)end).getLocalMillis(), ((LocalTime)start).getLocalMillis());
      return Minutes.minutes(minutes);
    }
    int amount = BaseSingleFieldPeriod.between(start, end, ZERO);
    return Minutes.minutes(amount);
  }
  public static Minutes minutesIn(ReadableInterval interval) {
    if(interval == null) {
      return Minutes.ZERO;
    }
    int amount = BaseSingleFieldPeriod.between(interval.getStart(), interval.getEnd(), DurationFieldType.minutes());
    return Minutes.minutes(amount);
  }
  public Minutes multipliedBy(int scalar) {
    return Minutes.minutes(FieldUtils.safeMultiply(getValue(), scalar));
  }
  public Minutes negated() {
    return Minutes.minutes(FieldUtils.safeNegate(getValue()));
  }
  @FromString() public static Minutes parseMinutes(String periodStr) {
    if(periodStr == null) {
      return Minutes.ZERO;
    }
    Period p = PARSER.parsePeriod(periodStr);
    return Minutes.minutes(p.getMinutes());
  }
  public Minutes plus(int minutes) {
    if(minutes == 0) {
      return this;
    }
    return Minutes.minutes(FieldUtils.safeAdd(getValue(), minutes));
  }
  public Minutes plus(Minutes minutes) {
    if(minutes == null) {
      return this;
    }
    return plus(minutes.getValue());
  }
  public static Minutes standardMinutesIn(ReadablePeriod period) {
    int amount = BaseSingleFieldPeriod.standardPeriodIn(period, DateTimeConstants.MILLIS_PER_MINUTE);
    return Minutes.minutes(amount);
  }
  private Object readResolve() {
    return Minutes.minutes(getValue());
  }
  public PeriodType getPeriodType() {
    return PeriodType.minutes();
  }
  public Seconds toStandardSeconds() {
    return Seconds.seconds(FieldUtils.safeMultiply(getValue(), DateTimeConstants.SECONDS_PER_MINUTE));
  }
  @ToString() public String toString() {
    return "PT" + String.valueOf(getValue()) + "M";
  }
  public Weeks toStandardWeeks() {
    return Weeks.weeks(getValue() / DateTimeConstants.MINUTES_PER_WEEK);
  }
  public boolean isGreaterThan(Minutes other) {
    if(other == null) {
      int var_123 = getValue();
      return var_123 > 0;
    }
    return getValue() > other.getValue();
  }
  public boolean isLessThan(Minutes other) {
    if(other == null) {
      return getValue() < 0;
    }
    return getValue() < other.getValue();
  }
  public int getMinutes() {
    return getValue();
  }
}