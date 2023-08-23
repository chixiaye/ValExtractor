package org.joda.time.field;
import org.joda.time.DateTimeFieldType;
import org.joda.time.DurationField;

public class PreciseDateTimeField extends PreciseDurationDateTimeField  {
  @SuppressWarnings(value = {"unused", }) final private static long serialVersionUID = -5586801265774496376L;
  final private int iRange;
  final private DurationField iRangeField;
  public PreciseDateTimeField(DateTimeFieldType type, DurationField unit, DurationField range) {
    super(type, unit);
    if(!range.isPrecise()) {
      throw new IllegalArgumentException("Range duration field must be precise");
    }
    long rangeMillis = range.getUnitMillis();
    iRange = (int)(rangeMillis / getUnitMillis());
    if(iRange < 2) {
      throw new IllegalArgumentException("The effective range must be at least 2");
    }
    iRangeField = range;
  }
  public DurationField getRangeDurationField() {
    return iRangeField;
  }
  public int get(long instant) {
    if(instant >= 0) {
      long var_444 = getUnitMillis();
      return (int)((instant / var_444) % iRange);
    }
    else {
      return iRange - 1 + (int)(((instant + 1) / getUnitMillis()) % iRange);
    }
  }
  public int getMaximumValue() {
    return iRange - 1;
  }
  public int getRange() {
    return iRange;
  }
  public long addWrapField(long instant, int amount) {
    int thisValue = get(instant);
    int wrappedValue = FieldUtils.getWrappedValue(thisValue, amount, getMinimumValue(), getMaximumValue());
    return instant + (wrappedValue - thisValue) * getUnitMillis();
  }
  public long set(long instant, int value) {
    FieldUtils.verifyValueBounds(this, value, getMinimumValue(), getMaximumValue());
    return instant + (value - get(instant)) * iUnitMillis;
  }
}