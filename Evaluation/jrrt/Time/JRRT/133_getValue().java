package org.joda.time;
import org.joda.convert.FromString;
import org.joda.convert.ToString;
import org.joda.time.base.BaseSingleFieldPeriod;
import org.joda.time.field.FieldUtils;
import org.joda.time.format.ISOPeriodFormat;
import org.joda.time.format.PeriodFormatter;

final public class Months extends BaseSingleFieldPeriod  {
  final public static Months ZERO = new Months(0);
  final public static Months ONE = new Months(1);
  final public static Months TWO = new Months(2);
  final public static Months THREE = new Months(3);
  final public static Months FOUR = new Months(4);
  final public static Months FIVE = new Months(5);
  final public static Months SIX = new Months(6);
  final public static Months SEVEN = new Months(7);
  final public static Months EIGHT = new Months(8);
  final public static Months NINE = new Months(9);
  final public static Months TEN = new Months(10);
  final public static Months ELEVEN = new Months(11);
  final public static Months TWELVE = new Months(12);
  final public static Months MAX_VALUE = new Months(Integer.MAX_VALUE);
  final public static Months MIN_VALUE = new Months(Integer.MIN_VALUE);
  final private static PeriodFormatter PARSER = ISOPeriodFormat.standard().withParseType(PeriodType.months());
  final private static long serialVersionUID = 87525275727380867L;
  private Months(int months) {
    super(months);
  }
  public DurationFieldType getFieldType() {
    return DurationFieldType.months();
  }
  public Months dividedBy(int divisor) {
    if(divisor == 1) {
      return this;
    }
    return Months.months(getValue() / divisor);
  }
  public Months minus(int months) {
    return plus(FieldUtils.safeNegate(months));
  }
  public Months minus(Months months) {
    if(months == null) {
      return this;
    }
    return minus(months.getValue());
  }
  public static Months months(int months) {
    switch (months){
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
      case 9:
      return NINE;
      case 10:
      return TEN;
      case 11:
      return ELEVEN;
      case 12:
      return TWELVE;
      case Integer.MAX_VALUE:
      return MAX_VALUE;
      case Integer.MIN_VALUE:
      return MIN_VALUE;
      default:
      return new Months(months);
    }
  }
  public static Months monthsBetween(ReadableInstant start, ReadableInstant end) {
    int amount = BaseSingleFieldPeriod.between(start, end, DurationFieldType.months());
    return Months.months(amount);
  }
  public static Months monthsBetween(ReadablePartial start, ReadablePartial end) {
    if(start instanceof LocalDate && end instanceof LocalDate) {
      Chronology chrono = DateTimeUtils.getChronology(start.getChronology());
      int months = chrono.months().getDifference(((LocalDate)end).getLocalMillis(), ((LocalDate)start).getLocalMillis());
      return Months.months(months);
    }
    int amount = BaseSingleFieldPeriod.between(start, end, ZERO);
    return Months.months(amount);
  }
  public static Months monthsIn(ReadableInterval interval) {
    if(interval == null) {
      return Months.ZERO;
    }
    int amount = BaseSingleFieldPeriod.between(interval.getStart(), interval.getEnd(), DurationFieldType.months());
    return Months.months(amount);
  }
  public Months multipliedBy(int scalar) {
    return Months.months(FieldUtils.safeMultiply(getValue(), scalar));
  }
  public Months negated() {
    return Months.months(FieldUtils.safeNegate(getValue()));
  }
  @FromString() public static Months parseMonths(String periodStr) {
    if(periodStr == null) {
      return Months.ZERO;
    }
    Period p = PARSER.parsePeriod(periodStr);
    return Months.months(p.getMonths());
  }
  public Months plus(int months) {
    if(months == 0) {
      return this;
    }
    return Months.months(FieldUtils.safeAdd(getValue(), months));
  }
  public Months plus(Months months) {
    if(months == null) {
      return this;
    }
    return plus(months.getValue());
  }
  private Object readResolve() {
    return Months.months(getValue());
  }
  public PeriodType getPeriodType() {
    return PeriodType.months();
  }
  @ToString() public String toString() {
    return "P" + String.valueOf(getValue()) + "M";
  }
  public boolean isGreaterThan(Months other) {
    if(other == null) {
      return getValue() > 0;
    }
    return getValue() > other.getValue();
  }
  public boolean isLessThan(Months other) {
    if(other == null) {
      int var_133 = getValue();
      return var_133 < 0;
    }
    return getValue() < other.getValue();
  }
  public int getMonths() {
    return getValue();
  }
}