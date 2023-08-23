package org.joda.time.field;
import org.joda.time.DateTimeField;
import org.joda.time.DateTimeFieldType;
import org.joda.time.DurationField;

public class OffsetDateTimeField extends DecoratedDateTimeField  {
  @SuppressWarnings(value = {"unused", }) final private static long serialVersionUID = 3145790132623583142L;
  final private int iOffset;
  final private int iMin;
  final private int iMax;
  public OffsetDateTimeField(DateTimeField field, DateTimeFieldType type, int offset) {
    this(field, type, offset, Integer.MIN_VALUE, Integer.MAX_VALUE);
  }
  public OffsetDateTimeField(DateTimeField field, DateTimeFieldType type, int offset, int minValue, int maxValue) {
    super(field, type);
    if(offset == 0) {
      throw new IllegalArgumentException("The offset cannot be zero");
    }
    iOffset = offset;
    int var_446 = field.getMinimumValue();
    if(minValue < (var_446 + offset)) {
      iMin = field.getMinimumValue() + offset;
    }
    else {
      iMin = minValue;
    }
    if(maxValue > (field.getMaximumValue() + offset)) {
      iMax = field.getMaximumValue() + offset;
    }
    else {
      iMax = maxValue;
    }
  }
  public OffsetDateTimeField(DateTimeField field, int offset) {
    this(field, (field == null ? null : field.getType()), offset, Integer.MIN_VALUE, Integer.MAX_VALUE);
  }
  public DurationField getLeapDurationField() {
    return getWrappedField().getLeapDurationField();
  }
  public boolean isLeap(long instant) {
    return getWrappedField().isLeap(instant);
  }
  public int get(long instant) {
    return super.get(instant) + iOffset;
  }
  public int getLeapAmount(long instant) {
    return getWrappedField().getLeapAmount(instant);
  }
  public int getMaximumValue() {
    return iMax;
  }
  public int getMinimumValue() {
    return iMin;
  }
  public int getOffset() {
    return iOffset;
  }
  public long add(long instant, int amount) {
    instant = super.add(instant, amount);
    FieldUtils.verifyValueBounds(this, get(instant), iMin, iMax);
    return instant;
  }
  public long add(long instant, long amount) {
    instant = super.add(instant, amount);
    FieldUtils.verifyValueBounds(this, get(instant), iMin, iMax);
    return instant;
  }
  public long addWrapField(long instant, int amount) {
    return set(instant, FieldUtils.getWrappedValue(get(instant), amount, iMin, iMax));
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
    FieldUtils.verifyValueBounds(this, value, iMin, iMax);
    return super.set(instant, value - iOffset);
  }
}