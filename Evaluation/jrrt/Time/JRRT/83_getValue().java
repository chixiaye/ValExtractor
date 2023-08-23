package org.joda.time;
import org.joda.convert.FromString;
import org.joda.convert.ToString;
import org.joda.time.base.BaseSingleFieldPeriod;
import org.joda.time.field.FieldUtils;
import org.joda.time.format.ISOPeriodFormat;
import org.joda.time.format.PeriodFormatter;

final public class Years extends BaseSingleFieldPeriod  {
  final public static Years ZERO = new Years(0);
  final public static Years ONE = new Years(1);
  final public static Years TWO = new Years(2);
  final public static Years THREE = new Years(3);
  final public static Years MAX_VALUE = new Years(Integer.MAX_VALUE);
  final public static Years MIN_VALUE = new Years(Integer.MIN_VALUE);
  final private static PeriodFormatter PARSER = ISOPeriodFormat.standard().withParseType(PeriodType.years());
  final private static long serialVersionUID = 87525275727380868L;
  private Years(int years) {
    super(years);
  }
  public DurationFieldType getFieldType() {
    return DurationFieldType.years();
  }
  private Object readResolve() {
    return Years.years(getValue());
  }
  public PeriodType getPeriodType() {
    return PeriodType.years();
  }
  @ToString() public String toString() {
    return "P" + String.valueOf(getValue()) + "Y";
  }
  public Years dividedBy(int divisor) {
    if(divisor == 1) {
      return this;
    }
    return Years.years(getValue() / divisor);
  }
  public Years minus(int years) {
    return plus(FieldUtils.safeNegate(years));
  }
  public Years minus(Years years) {
    if(years == null) {
      return this;
    }
    return minus(years.getValue());
  }
  public Years multipliedBy(int scalar) {
    return Years.years(FieldUtils.safeMultiply(getValue(), scalar));
  }
  public Years negated() {
    return Years.years(FieldUtils.safeNegate(getValue()));
  }
  @FromString() public static Years parseYears(String periodStr) {
    if(periodStr == null) {
      return Years.ZERO;
    }
    Period p = PARSER.parsePeriod(periodStr);
    return Years.years(p.getYears());
  }
  public Years plus(int years) {
    if(years == 0) {
      return this;
    }
    return Years.years(FieldUtils.safeAdd(getValue(), years));
  }
  public Years plus(Years years) {
    if(years == null) {
      return this;
    }
    return plus(years.getValue());
  }
  public static Years years(int years) {
    switch (years){
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
      return new Years(years);
    }
  }
  public static Years yearsBetween(ReadableInstant start, ReadableInstant end) {
    int amount = BaseSingleFieldPeriod.between(start, end, DurationFieldType.years());
    return Years.years(amount);
  }
  public static Years yearsBetween(ReadablePartial start, ReadablePartial end) {
    if(start instanceof LocalDate && end instanceof LocalDate) {
      Chronology chrono = DateTimeUtils.getChronology(start.getChronology());
      int years = chrono.years().getDifference(((LocalDate)end).getLocalMillis(), ((LocalDate)start).getLocalMillis());
      return Years.years(years);
    }
    int amount = BaseSingleFieldPeriod.between(start, end, ZERO);
    return Years.years(amount);
  }
  public static Years yearsIn(ReadableInterval interval) {
    if(interval == null) {
      return Years.ZERO;
    }
    int amount = BaseSingleFieldPeriod.between(interval.getStart(), interval.getEnd(), DurationFieldType.years());
    return Years.years(amount);
  }
  public boolean isGreaterThan(Years other) {
    if(other == null) {
      int var_83 = getValue();
      return var_83 > 0;
    }
    return getValue() > other.getValue();
  }
  public boolean isLessThan(Years other) {
    if(other == null) {
      return getValue() < 0;
    }
    return getValue() < other.getValue();
  }
  public int getYears() {
    return getValue();
  }
}