package org.joda.time.field;
import org.joda.time.DateTimeField;
import org.joda.time.DateTimeFieldType;
import org.joda.time.DurationField;

public class RemainderDateTimeField extends DecoratedDateTimeField  {
  @SuppressWarnings(value = {"unused", }) final private static long serialVersionUID = 5708241235177666790L;
  final int iDivisor;
  final DurationField iDurationField;
  final DurationField iRangeField;
  public RemainderDateTimeField(DateTimeField field, DateTimeFieldType type, int divisor) {
    super(field, type);
    if(divisor < 2) {
      throw new IllegalArgumentException("The divisor must be at least 2");
    }
    DurationField var_450 = field.getDurationField();
    DurationField rangeField = var_450;
    if(rangeField == null) {
      iRangeField = null;
    }
    else {
      iRangeField = new ScaledDurationField(rangeField, type.getRangeDurationType(), divisor);
    }
    iDurationField = field.getDurationField();
    iDivisor = divisor;
  }
  public RemainderDateTimeField(DateTimeField field, DurationField rangeField, DateTimeFieldType type, int divisor) {
    super(field, type);
    if(divisor < 2) {
      throw new IllegalArgumentException("The divisor must be at least 2");
    }
    iRangeField = rangeField;
    iDurationField = field.getDurationField();
    iDivisor = divisor;
  }
  public RemainderDateTimeField(DividedDateTimeField dividedField) {
    this(dividedField, dividedField.getType());
  }
  public RemainderDateTimeField(DividedDateTimeField dividedField, DateTimeFieldType type) {
    this(dividedField, dividedField.getWrappedField().getDurationField(), type);
  }
  public RemainderDateTimeField(DividedDateTimeField dividedField, DurationField durationField, DateTimeFieldType type) {
    super(dividedField.getWrappedField(), type);
    iDivisor = dividedField.iDivisor;
    iDurationField = durationField;
    iRangeField = dividedField.iDurationField;
  }
  @Override() public DurationField getDurationField() {
    return iDurationField;
  }
  public DurationField getRangeDurationField() {
    return iRangeField;
  }
  public int get(long instant) {
    int value = getWrappedField().get(instant);
    if(value >= 0) {
      return value % iDivisor;
    }
    else {
      return (iDivisor - 1) + ((value + 1) % iDivisor);
    }
  }
  private int getDivided(int value) {
    if(value >= 0) {
      return value / iDivisor;
    }
    else {
      return ((value + 1) / iDivisor) - 1;
    }
  }
  public int getDivisor() {
    return iDivisor;
  }
  public int getMaximumValue() {
    return iDivisor - 1;
  }
  public int getMinimumValue() {
    return 0;
  }
  public long addWrapField(long instant, int amount) {
    return set(instant, FieldUtils.getWrappedValue(get(instant), amount, 0, iDivisor - 1));
  }
  public long remainder(long instant) {
    return getWrappedField().remainder(instant);
  }
  public long roundCeiling(long instant) {
    return getWrappedField().roundCeiling(instant);
  }
  public long roundFloor(long instant) {
    return getWrappedField().roundFloor(instant);
  }
  public long roundHalfCeiling(long instant) {
    return getWrappedField().roundHalfCeiling(instant);
  }
  public long roundHalfEven(long instant) {
    return getWrappedField().roundHalfEven(instant);
  }
  public long roundHalfFloor(long instant) {
    return getWrappedField().roundHalfFloor(instant);
  }
  public long set(long instant, int value) {
    FieldUtils.verifyValueBounds(this, value, 0, iDivisor - 1);
    int divided = getDivided(getWrappedField().get(instant));
    return getWrappedField().set(instant, divided * iDivisor + value);
  }
}