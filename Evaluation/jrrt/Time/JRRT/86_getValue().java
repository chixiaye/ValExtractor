package org.joda.time;
import org.joda.convert.FromString;
import org.joda.convert.ToString;
import org.joda.time.base.BaseSingleFieldPeriod;
import org.joda.time.field.FieldUtils;
import org.joda.time.format.ISOPeriodFormat;
import org.joda.time.format.PeriodFormatter;

final public class Hours extends BaseSingleFieldPeriod  {
  final public static Hours ZERO = new Hours(0);
  final public static Hours ONE = new Hours(1);
  final public static Hours TWO = new Hours(2);
  final public static Hours THREE = new Hours(3);
  final public static Hours FOUR = new Hours(4);
  final public static Hours FIVE = new Hours(5);
  final public static Hours SIX = new Hours(6);
  final public static Hours SEVEN = new Hours(7);
  final public static Hours EIGHT = new Hours(8);
  final public static Hours MAX_VALUE = new Hours(Integer.MAX_VALUE);
  final public static Hours MIN_VALUE = new Hours(Integer.MIN_VALUE);
  final private static PeriodFormatter PARSER = ISOPeriodFormat.standard().withParseType(PeriodType.hours());
  final private static long serialVersionUID = 87525275727380864L;
  private Hours(int hours) {
    super(hours);
  }
  public Days toStandardDays() {
    return Days.days(getValue() / DateTimeConstants.HOURS_PER_DAY);
  }
  public Duration toStandardDuration() {
    long hours = getValue();
    return new Duration(hours * DateTimeConstants.MILLIS_PER_HOUR);
  }
  public DurationFieldType getFieldType() {
    return DurationFieldType.hours();
  }
  public Hours dividedBy(int divisor) {
    if(divisor == 1) {
      return this;
    }
    return Hours.hours(getValue() / divisor);
  }
  public static Hours hours(int hours) {
    switch (hours){
      case 0:
      return ZERO;
      case 1:
      return ONE;
      case 2:
      return TWO;
      case 3:
      return THREE;
      case 4:
      return FOUR;
      case 5:
      return FIVE;
      case 6:
      return SIX;
      case 7:
      return SEVEN;
      case 8:
      return EIGHT;
      case Integer.MAX_VALUE:
      return MAX_VALUE;
      case Integer.MIN_VALUE:
      return MIN_VALUE;
      default:
      return new Hours(hours);
    }
  }
  public static Hours hoursBetween(ReadableInstant start, ReadableInstant end) {
    int amount = BaseSingleFieldPeriod.between(start, end, DurationFieldType.hours());
    return Hours.hours(amount);
  }
  public static Hours hoursBetween(ReadablePartial start, ReadablePartial end) {
    if(start instanceof LocalTime && end instanceof LocalTime) {
      Chronology chrono = DateTimeUtils.getChronology(start.getChronology());
      int hours = chrono.hours().getDifference(((LocalTime)end).getLocalMillis(), ((LocalTime)start).getLocalMillis());
      return Hours.hours(hours);
    }
    int amount = BaseSingleFieldPeriod.between(start, end, ZERO);
    return Hours.hours(amount);
  }
  public static Hours hoursIn(ReadableInterval interval) {
    if(interval == null) {
      return Hours.ZERO;
    }
    int amount = BaseSingleFieldPeriod.between(interval.getStart(), interval.getEnd(), DurationFieldType.hours());
    return Hours.hours(amount);
  }
  public Hours minus(int hours) {
    return plus(FieldUtils.safeNegate(hours));
  }
  public Hours minus(Hours hours) {
    if(hours == null) {
      return this;
    }
    return minus(hours.getValue());
  }
  public Hours multipliedBy(int scalar) {
    return Hours.hours(FieldUtils.safeMultiply(getValue(), scalar));
  }
  public Hours negated() {
    return Hours.hours(FieldUtils.safeNegate(getValue()));
  }
  @FromString() public static Hours parseHours(String periodStr) {
    if(periodStr == null) {
      return Hours.ZERO;
    }
    Period p = PARSER.parsePeriod(periodStr);
    return Hours.hours(p.getHours());
  }
  public Hours plus(int hours) {
    if(hours == 0) {
      return this;
    }
    return Hours.hours(FieldUtils.safeAdd(getValue(), hours));
  }
  public Hours plus(Hours hours) {
    if(hours == null) {
      return this;
    }
    return plus(hours.getValue());
  }
  public static Hours standardHoursIn(ReadablePeriod period) {
    int amount = BaseSingleFieldPeriod.standardPeriodIn(period, DateTimeConstants.MILLIS_PER_HOUR);
    return Hours.hours(amount);
  }
  public Minutes toStandardMinutes() {
    return Minutes.minutes(FieldUtils.safeMultiply(getValue(), DateTimeConstants.MINUTES_PER_HOUR));
  }
  private Object readResolve() {
    return Hours.hours(getValue());
  }
  public PeriodType getPeriodType() {
    return PeriodType.hours();
  }
  public Seconds toStandardSeconds() {
    return Seconds.seconds(FieldUtils.safeMultiply(getValue(), DateTimeConstants.SECONDS_PER_HOUR));
  }
  @ToString() public String toString() {
    return "PT" + String.valueOf(getValue()) + "H";
  }
  public Weeks toStandardWeeks() {
    return Weeks.weeks(getValue() / DateTimeConstants.HOURS_PER_WEEK);
  }
  public boolean isGreaterThan(Hours other) {
    if(other == null) {
      return getValue() > 0;
    }
    return getValue() > other.getValue();
  }
  public boolean isLessThan(Hours other) {
    if(other == null) {
      int var_86 = getValue();
      return var_86 < 0;
    }
    return getValue() < other.getValue();
  }
  public int getHours() {
    return getValue();
  }
}