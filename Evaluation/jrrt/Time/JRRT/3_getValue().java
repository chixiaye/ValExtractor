package org.joda.time;
import org.joda.convert.FromString;
import org.joda.convert.ToString;
import org.joda.time.base.BaseSingleFieldPeriod;
import org.joda.time.field.FieldUtils;
import org.joda.time.format.ISOPeriodFormat;
import org.joda.time.format.PeriodFormatter;

final public class Weeks extends BaseSingleFieldPeriod  {
  final public static Weeks ZERO = new Weeks(0);
  final public static Weeks ONE = new Weeks(1);
  final public static Weeks TWO = new Weeks(2);
  final public static Weeks THREE = new Weeks(3);
  final public static Weeks MAX_VALUE = new Weeks(Integer.MAX_VALUE);
  final public static Weeks MIN_VALUE = new Weeks(Integer.MIN_VALUE);
  final private static PeriodFormatter PARSER = ISOPeriodFormat.standard().withParseType(PeriodType.weeks());
  final private static long serialVersionUID = 87525275727380866L;
  private Weeks(int weeks) {
    super(weeks);
  }
  public Days toStandardDays() {
    return Days.days(FieldUtils.safeMultiply(getValue(), DateTimeConstants.DAYS_PER_WEEK));
  }
  public Duration toStandardDuration() {
    long weeks = getValue();
    return new Duration(weeks * DateTimeConstants.MILLIS_PER_WEEK);
  }
  public DurationFieldType getFieldType() {
    return DurationFieldType.weeks();
  }
  public Hours toStandardHours() {
    return Hours.hours(FieldUtils.safeMultiply(getValue(), DateTimeConstants.HOURS_PER_WEEK));
  }
  public Minutes toStandardMinutes() {
    return Minutes.minutes(FieldUtils.safeMultiply(getValue(), DateTimeConstants.MINUTES_PER_WEEK));
  }
  private Object readResolve() {
    return Weeks.weeks(getValue());
  }
  public PeriodType getPeriodType() {
    return PeriodType.weeks();
  }
  public Seconds toStandardSeconds() {
    return Seconds.seconds(FieldUtils.safeMultiply(getValue(), DateTimeConstants.SECONDS_PER_WEEK));
  }
  @ToString() public String toString() {
    return "P" + String.valueOf(getValue()) + "W";
  }
  public Weeks dividedBy(int divisor) {
    if(divisor == 1) {
      return this;
    }
    return Weeks.weeks(getValue() / divisor);
  }
  public Weeks minus(int weeks) {
    return plus(FieldUtils.safeNegate(weeks));
  }
  public Weeks minus(Weeks weeks) {
    if(weeks == null) {
      return this;
    }
    return minus(weeks.getValue());
  }
  public Weeks multipliedBy(int scalar) {
    return Weeks.weeks(FieldUtils.safeMultiply(getValue(), scalar));
  }
  public Weeks negated() {
    return Weeks.weeks(FieldUtils.safeNegate(getValue()));
  }
  @FromString() public static Weeks parseWeeks(String periodStr) {
    if(periodStr == null) {
      return Weeks.ZERO;
    }
    Period p = PARSER.parsePeriod(periodStr);
    return Weeks.weeks(p.getWeeks());
  }
  public Weeks plus(int weeks) {
    if(weeks == 0) {
      return this;
    }
    return Weeks.weeks(FieldUtils.safeAdd(getValue(), weeks));
  }
  public Weeks plus(Weeks weeks) {
    if(weeks == null) {
      return this;
    }
    return plus(weeks.getValue());
  }
  public static Weeks standardWeeksIn(ReadablePeriod period) {
    int amount = BaseSingleFieldPeriod.standardPeriodIn(period, DateTimeConstants.MILLIS_PER_WEEK);
    return Weeks.weeks(amount);
  }
  public static Weeks weeks(int weeks) {
    switch (weeks){
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
      return new Weeks(weeks);
    }
  }
  public static Weeks weeksBetween(ReadableInstant start, ReadableInstant end) {
    int amount = BaseSingleFieldPeriod.between(start, end, DurationFieldType.weeks());
    return Weeks.weeks(amount);
  }
  public static Weeks weeksBetween(ReadablePartial start, ReadablePartial end) {
    if(start instanceof LocalDate && end instanceof LocalDate) {
      Chronology chrono = DateTimeUtils.getChronology(start.getChronology());
      int weeks = chrono.weeks().getDifference(((LocalDate)end).getLocalMillis(), ((LocalDate)start).getLocalMillis());
      return Weeks.weeks(weeks);
    }
    int amount = BaseSingleFieldPeriod.between(start, end, ZERO);
    return Weeks.weeks(amount);
  }
  public static Weeks weeksIn(ReadableInterval interval) {
    if(interval == null) {
      return Weeks.ZERO;
    }
    int amount = BaseSingleFieldPeriod.between(interval.getStart(), interval.getEnd(), DurationFieldType.weeks());
    return Weeks.weeks(amount);
  }
  public boolean isGreaterThan(Weeks other) {
    if(other == null) {
      return getValue() > 0;
    }
    return getValue() > other.getValue();
  }
  public boolean isLessThan(Weeks other) {
    if(other == null) {
      int var_3 = getValue();
      return var_3 < 0;
    }
    return getValue() < other.getValue();
  }
  public int getWeeks() {
    return getValue();
  }
}