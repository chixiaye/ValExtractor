package org.joda.time;
import org.joda.convert.FromString;
import org.joda.convert.ToString;
import org.joda.time.base.BaseSingleFieldPeriod;
import org.joda.time.field.FieldUtils;
import org.joda.time.format.ISOPeriodFormat;
import org.joda.time.format.PeriodFormatter;

final public class Seconds extends BaseSingleFieldPeriod  {
  final public static Seconds ZERO = new Seconds(0);
  final public static Seconds ONE = new Seconds(1);
  final public static Seconds TWO = new Seconds(2);
  final public static Seconds THREE = new Seconds(3);
  final public static Seconds MAX_VALUE = new Seconds(Integer.MAX_VALUE);
  final public static Seconds MIN_VALUE = new Seconds(Integer.MIN_VALUE);
  final private static PeriodFormatter PARSER = ISOPeriodFormat.standard().withParseType(PeriodType.seconds());
  final private static long serialVersionUID = 87525275727380862L;
  private Seconds(int seconds) {
    super(seconds);
  }
  public Days toStandardDays() {
    return Days.days(getValue() / DateTimeConstants.SECONDS_PER_DAY);
  }
  public Duration toStandardDuration() {
    long seconds = getValue();
    return new Duration(seconds * DateTimeConstants.MILLIS_PER_SECOND);
  }
  public DurationFieldType getFieldType() {
    return DurationFieldType.seconds();
  }
  public Hours toStandardHours() {
    return Hours.hours(getValue() / DateTimeConstants.SECONDS_PER_HOUR);
  }
  public Minutes toStandardMinutes() {
    return Minutes.minutes(getValue() / DateTimeConstants.SECONDS_PER_MINUTE);
  }
  private Object readResolve() {
    return Seconds.seconds(getValue());
  }
  public PeriodType getPeriodType() {
    return PeriodType.seconds();
  }
  public Seconds dividedBy(int divisor) {
    if(divisor == 1) {
      return this;
    }
    return Seconds.seconds(getValue() / divisor);
  }
  public Seconds minus(int seconds) {
    return plus(FieldUtils.safeNegate(seconds));
  }
  public Seconds minus(Seconds seconds) {
    if(seconds == null) {
      return this;
    }
    return minus(seconds.getValue());
  }
  public Seconds multipliedBy(int scalar) {
    return Seconds.seconds(FieldUtils.safeMultiply(getValue(), scalar));
  }
  public Seconds negated() {
    return Seconds.seconds(FieldUtils.safeNegate(getValue()));
  }
  @FromString() public static Seconds parseSeconds(String periodStr) {
    if(periodStr == null) {
      return Seconds.ZERO;
    }
    Period p = PARSER.parsePeriod(periodStr);
    return Seconds.seconds(p.getSeconds());
  }
  public Seconds plus(int seconds) {
    if(seconds == 0) {
      return this;
    }
    return Seconds.seconds(FieldUtils.safeAdd(getValue(), seconds));
  }
  public Seconds plus(Seconds seconds) {
    if(seconds == null) {
      return this;
    }
    return plus(seconds.getValue());
  }
  public static Seconds seconds(int seconds) {
    switch (seconds){
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
      return new Seconds(seconds);
    }
  }
  public static Seconds secondsBetween(ReadableInstant start, ReadableInstant end) {
    int amount = BaseSingleFieldPeriod.between(start, end, DurationFieldType.seconds());
    return Seconds.seconds(amount);
  }
  public static Seconds secondsBetween(ReadablePartial start, ReadablePartial end) {
    if(start instanceof LocalTime && end instanceof LocalTime) {
      Chronology chrono = DateTimeUtils.getChronology(start.getChronology());
      int seconds = chrono.seconds().getDifference(((LocalTime)end).getLocalMillis(), ((LocalTime)start).getLocalMillis());
      return Seconds.seconds(seconds);
    }
    int amount = BaseSingleFieldPeriod.between(start, end, ZERO);
    return Seconds.seconds(amount);
  }
  public static Seconds secondsIn(ReadableInterval interval) {
    if(interval == null) {
      return Seconds.ZERO;
    }
    int amount = BaseSingleFieldPeriod.between(interval.getStart(), interval.getEnd(), DurationFieldType.seconds());
    return Seconds.seconds(amount);
  }
  public static Seconds standardSecondsIn(ReadablePeriod period) {
    int amount = BaseSingleFieldPeriod.standardPeriodIn(period, DateTimeConstants.MILLIS_PER_SECOND);
    return Seconds.seconds(amount);
  }
  @ToString() public String toString() {
    return "PT" + String.valueOf(getValue()) + "S";
  }
  public Weeks toStandardWeeks() {
    return Weeks.weeks(getValue() / DateTimeConstants.SECONDS_PER_WEEK);
  }
  public boolean isGreaterThan(Seconds other) {
    if(other == null) {
      return getValue() > 0;
    }
    return getValue() > other.getValue();
  }
  public boolean isLessThan(Seconds other) {
    if(other == null) {
      int var_127 = getValue();
      return var_127 < 0;
    }
    return getValue() < other.getValue();
  }
  public int getSeconds() {
    return getValue();
  }
}