package org.joda.time;
import org.joda.convert.FromString;
import org.joda.convert.ToString;
import org.joda.time.base.BaseSingleFieldPeriod;
import org.joda.time.field.FieldUtils;
import org.joda.time.format.ISOPeriodFormat;
import org.joda.time.format.PeriodFormatter;

final public class Days extends BaseSingleFieldPeriod  {
  final public static Days ZERO = new Days(0);
  final public static Days ONE = new Days(1);
  final public static Days TWO = new Days(2);
  final public static Days THREE = new Days(3);
  final public static Days FOUR = new Days(4);
  final public static Days FIVE = new Days(5);
  final public static Days SIX = new Days(6);
  final public static Days SEVEN = new Days(7);
  final public static Days MAX_VALUE = new Days(Integer.MAX_VALUE);
  final public static Days MIN_VALUE = new Days(Integer.MIN_VALUE);
  final private static PeriodFormatter PARSER = ISOPeriodFormat.standard().withParseType(PeriodType.days());
  final private static long serialVersionUID = 87525275727380865L;
  private Days(int days) {
    super(days);
  }
  public static Days days(int days) {
    switch (days){
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
      case Integer.MAX_VALUE:
      return MAX_VALUE;
      case Integer.MIN_VALUE:
      return MIN_VALUE;
      default:
      return new Days(days);
    }
  }
  public static Days daysBetween(ReadableInstant start, ReadableInstant end) {
    int amount = BaseSingleFieldPeriod.between(start, end, DurationFieldType.days());
    return Days.days(amount);
  }
  public static Days daysBetween(ReadablePartial start, ReadablePartial end) {
    if(start instanceof LocalDate && end instanceof LocalDate) {
      Chronology chrono = DateTimeUtils.getChronology(start.getChronology());
      int days = chrono.days().getDifference(((LocalDate)end).getLocalMillis(), ((LocalDate)start).getLocalMillis());
      return Days.days(days);
    }
    int amount = BaseSingleFieldPeriod.between(start, end, ZERO);
    return Days.days(amount);
  }
  public static Days daysIn(ReadableInterval interval) {
    if(interval == null) {
      return Days.ZERO;
    }
    int amount = BaseSingleFieldPeriod.between(interval.getStart(), interval.getEnd(), DurationFieldType.days());
    return Days.days(amount);
  }
  public Days dividedBy(int divisor) {
    if(divisor == 1) {
      return this;
    }
    return Days.days(getValue() / divisor);
  }
  public Days minus(int days) {
    return plus(FieldUtils.safeNegate(days));
  }
  public Days minus(Days days) {
    if(days == null) {
      return this;
    }
    return minus(days.getValue());
  }
  public Days multipliedBy(int scalar) {
    return Days.days(FieldUtils.safeMultiply(getValue(), scalar));
  }
  public Days negated() {
    return Days.days(FieldUtils.safeNegate(getValue()));
  }
  @FromString() public static Days parseDays(String periodStr) {
    if(periodStr == null) {
      return Days.ZERO;
    }
    Period p = PARSER.parsePeriod(periodStr);
    return Days.days(p.getDays());
  }
  public Days plus(int days) {
    if(days == 0) {
      return this;
    }
    return Days.days(FieldUtils.safeAdd(getValue(), days));
  }
  public Days plus(Days days) {
    if(days == null) {
      return this;
    }
    return plus(days.getValue());
  }
  public static Days standardDaysIn(ReadablePeriod period) {
    int amount = BaseSingleFieldPeriod.standardPeriodIn(period, DateTimeConstants.MILLIS_PER_DAY);
    return Days.days(amount);
  }
  public Duration toStandardDuration() {
    long days = getValue();
    return new Duration(days * DateTimeConstants.MILLIS_PER_DAY);
  }
  public DurationFieldType getFieldType() {
    return DurationFieldType.days();
  }
  public Hours toStandardHours() {
    return Hours.hours(FieldUtils.safeMultiply(getValue(), DateTimeConstants.HOURS_PER_DAY));
  }
  public Minutes toStandardMinutes() {
    return Minutes.minutes(FieldUtils.safeMultiply(getValue(), DateTimeConstants.MINUTES_PER_DAY));
  }
  private Object readResolve() {
    return Days.days(getValue());
  }
  public PeriodType getPeriodType() {
    return PeriodType.days();
  }
  public Seconds toStandardSeconds() {
    return Seconds.seconds(FieldUtils.safeMultiply(getValue(), DateTimeConstants.SECONDS_PER_DAY));
  }
  @ToString() public String toString() {
    return "P" + String.valueOf(getValue()) + "D";
  }
  public Weeks toStandardWeeks() {
    return Weeks.weeks(getValue() / DateTimeConstants.DAYS_PER_WEEK);
  }
  public boolean isGreaterThan(Days other) {
    if(other == null) {
      return getValue() > 0;
    }
    return getValue() > other.getValue();
  }
  public boolean isLessThan(Days other) {
    if(other == null) {
      int var_122 = getValue();
      return var_122 < 0;
    }
    return getValue() < other.getValue();
  }
  public int getDays() {
    return getValue();
  }
}