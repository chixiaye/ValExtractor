package org.joda.time.field;
import java.util.Locale;
import org.joda.time.DateTimeField;
import org.joda.time.DateTimeFieldType;
import org.joda.time.DurationField;
import org.joda.time.IllegalFieldValueException;
import org.joda.time.ReadablePartial;

abstract public class BaseDateTimeField extends DateTimeField  {
  final private DateTimeFieldType iType;
  protected BaseDateTimeField(DateTimeFieldType type) {
    super();
    if(type == null) {
      throw new IllegalArgumentException("The type must not be null");
    }
    iType = type;
  }
  final public DateTimeFieldType getType() {
    return iType;
  }
  abstract public DurationField getDurationField();
  public DurationField getLeapDurationField() {
    return null;
  }
  abstract public DurationField getRangeDurationField();
  public String getAsShortText(int fieldValue, Locale locale) {
    return getAsText(fieldValue, locale);
  }
  final public String getAsShortText(long instant) {
    return getAsShortText(instant, null);
  }
  public String getAsShortText(long instant, Locale locale) {
    return getAsShortText(get(instant), locale);
  }
  public String getAsShortText(ReadablePartial partial, int fieldValue, Locale locale) {
    return getAsShortText(fieldValue, locale);
  }
  final public String getAsShortText(ReadablePartial partial, Locale locale) {
    return getAsShortText(partial, partial.get(getType()), locale);
  }
  public String getAsText(int fieldValue, Locale locale) {
    return Integer.toString(fieldValue);
  }
  final public String getAsText(long instant) {
    return getAsText(instant, null);
  }
  public String getAsText(long instant, Locale locale) {
    return getAsText(get(instant), locale);
  }
  public String getAsText(ReadablePartial partial, int fieldValue, Locale locale) {
    return getAsText(fieldValue, locale);
  }
  final public String getAsText(ReadablePartial partial, Locale locale) {
    return getAsText(partial, partial.get(getType()), locale);
  }
  final public String getName() {
    return iType.getName();
  }
  public String toString() {
    return "DateTimeField[" + getName() + ']';
  }
  public boolean isLeap(long instant) {
    return false;
  }
  final public boolean isSupported() {
    return true;
  }
  protected int convertText(String text, Locale locale) {
    try {
      return Integer.parseInt(text);
    }
    catch (NumberFormatException ex) {
      throw new IllegalFieldValueException(getType(), text);
    }
  }
  abstract public int get(long instant);
  public int getDifference(long minuendInstant, long subtrahendInstant) {
    return getDurationField().getDifference(minuendInstant, subtrahendInstant);
  }
  public int getLeapAmount(long instant) {
    return 0;
  }
  public int getMaximumShortTextLength(Locale locale) {
    return getMaximumTextLength(locale);
  }
  public int getMaximumTextLength(Locale locale) {
    int max = getMaximumValue();
    if(max >= 0) {
      if(max < 10) {
        return 1;
      }
      else 
        if(max < 100) {
          return 2;
        }
        else 
          if(max < 1000) {
            return 3;
          }
    }
    return Integer.toString(max).length();
  }
  abstract public int getMaximumValue();
  public int getMaximumValue(long instant) {
    return getMaximumValue();
  }
  public int getMaximumValue(ReadablePartial instant) {
    return getMaximumValue();
  }
  public int getMaximumValue(ReadablePartial instant, int[] values) {
    return getMaximumValue(instant);
  }
  abstract public int getMinimumValue();
  public int getMinimumValue(long instant) {
    return getMinimumValue();
  }
  public int getMinimumValue(ReadablePartial instant) {
    return getMinimumValue();
  }
  public int getMinimumValue(ReadablePartial instant, int[] values) {
    return getMinimumValue(instant);
  }
  public int[] add(ReadablePartial instant, int fieldIndex, int[] values, int valueToAdd) {
    if(valueToAdd == 0) {
      return values;
    }
    DateTimeField nextField = null;
    while(valueToAdd > 0){
      int max = getMaximumValue(instant, values);
      long proposed = values[fieldIndex] + valueToAdd;
      if(proposed <= max) {
        values[fieldIndex] = (int)proposed;
        break ;
      }
      if(nextField == null) {
        if(fieldIndex == 0) {
          throw new IllegalArgumentException("Maximum value exceeded for add");
        }
        nextField = instant.getField(fieldIndex - 1);
        if(getRangeDurationField().getType() != nextField.getDurationField().getType()) {
          throw new IllegalArgumentException("Fields invalid for add");
        }
      }
      valueToAdd -= (max + 1) - values[fieldIndex];
      values = nextField.add(instant, fieldIndex - 1, values, 1);
      values[fieldIndex] = getMinimumValue(instant, values);
    }
    while(valueToAdd < 0){
      int min = getMinimumValue(instant, values);
      long proposed = values[fieldIndex] + valueToAdd;
      if(proposed >= min) {
        values[fieldIndex] = (int)proposed;
        break ;
      }
      if(nextField == null) {
        if(fieldIndex == 0) {
          throw new IllegalArgumentException("Maximum value exceeded for add");
        }
        nextField = instant.getField(fieldIndex - 1);
        if(getRangeDurationField().getType() != nextField.getDurationField().getType()) {
          throw new IllegalArgumentException("Fields invalid for add");
        }
      }
      valueToAdd -= (min - 1) - values[fieldIndex];
      values = nextField.add(instant, fieldIndex - 1, values, -1);
      values[fieldIndex] = getMaximumValue(instant, values);
    }
    return set(instant, fieldIndex, values, values[fieldIndex]);
  }
  public int[] addWrapField(ReadablePartial instant, int fieldIndex, int[] values, int valueToAdd) {
    int current = values[fieldIndex];
    int wrapped = FieldUtils.getWrappedValue(current, valueToAdd, getMinimumValue(instant), getMaximumValue(instant));
    return set(instant, fieldIndex, values, wrapped);
  }
  public int[] addWrapPartial(ReadablePartial instant, int fieldIndex, int[] values, int valueToAdd) {
    if(valueToAdd == 0) {
      return values;
    }
    DateTimeField nextField = null;
    while(valueToAdd > 0){
      int var_433 = getMaximumValue(instant, values);
      int max = var_433;
      long proposed = values[fieldIndex] + valueToAdd;
      if(proposed <= max) {
        values[fieldIndex] = (int)proposed;
        break ;
      }
      if(nextField == null) {
        if(fieldIndex == 0) {
          valueToAdd -= (max + 1) - values[fieldIndex];
          values[fieldIndex] = getMinimumValue(instant, values);
          continue ;
        }
        nextField = instant.getField(fieldIndex - 1);
        if(getRangeDurationField().getType() != nextField.getDurationField().getType()) {
          throw new IllegalArgumentException("Fields invalid for add");
        }
      }
      valueToAdd -= (max + 1) - values[fieldIndex];
      values = nextField.addWrapPartial(instant, fieldIndex - 1, values, 1);
      values[fieldIndex] = getMinimumValue(instant, values);
    }
    while(valueToAdd < 0){
      int min = getMinimumValue(instant, values);
      long proposed = values[fieldIndex] + valueToAdd;
      if(proposed >= min) {
        values[fieldIndex] = (int)proposed;
        break ;
      }
      if(nextField == null) {
        if(fieldIndex == 0) {
          valueToAdd -= (min - 1) - values[fieldIndex];
          values[fieldIndex] = getMaximumValue(instant, values);
          continue ;
        }
        nextField = instant.getField(fieldIndex - 1);
        if(getRangeDurationField().getType() != nextField.getDurationField().getType()) {
          throw new IllegalArgumentException("Fields invalid for add");
        }
      }
      valueToAdd -= (min - 1) - values[fieldIndex];
      values = nextField.addWrapPartial(instant, fieldIndex - 1, values, -1);
      values[fieldIndex] = getMaximumValue(instant, values);
    }
    return set(instant, fieldIndex, values, values[fieldIndex]);
  }
  public int[] set(ReadablePartial partial, int fieldIndex, int[] values, int newValue) {
    FieldUtils.verifyValueBounds(this, newValue, getMinimumValue(partial, values), getMaximumValue(partial, values));
    values[fieldIndex] = newValue;
    for(int i = fieldIndex + 1; i < partial.size(); i++) {
      DateTimeField field = partial.getField(i);
      if(values[i] > field.getMaximumValue(partial, values)) {
        values[i] = field.getMaximumValue(partial, values);
      }
      if(values[i] < field.getMinimumValue(partial, values)) {
        values[i] = field.getMinimumValue(partial, values);
      }
    }
    return values;
  }
  public int[] set(ReadablePartial instant, int fieldIndex, int[] values, String text, Locale locale) {
    int value = convertText(text, locale);
    return set(instant, fieldIndex, values, value);
  }
  public long add(long instant, int value) {
    return getDurationField().add(instant, value);
  }
  public long add(long instant, long value) {
    return getDurationField().add(instant, value);
  }
  public long addWrapField(long instant, int value) {
    int current = get(instant);
    int wrapped = FieldUtils.getWrappedValue(current, value, getMinimumValue(instant), getMaximumValue(instant));
    return set(instant, wrapped);
  }
  public long getDifferenceAsLong(long minuendInstant, long subtrahendInstant) {
    return getDurationField().getDifferenceAsLong(minuendInstant, subtrahendInstant);
  }
  public long remainder(long instant) {
    return instant - roundFloor(instant);
  }
  public long roundCeiling(long instant) {
    long newInstant = roundFloor(instant);
    if(newInstant != instant) {
      instant = add(newInstant, 1);
    }
    return instant;
  }
  abstract public long roundFloor(long instant);
  public long roundHalfCeiling(long instant) {
    long floor = roundFloor(instant);
    long ceiling = roundCeiling(instant);
    long diffFromFloor = instant - floor;
    long diffToCeiling = ceiling - instant;
    if(diffToCeiling <= diffFromFloor) {
      return ceiling;
    }
    else {
      return floor;
    }
  }
  public long roundHalfEven(long instant) {
    long floor = roundFloor(instant);
    long ceiling = roundCeiling(instant);
    long diffFromFloor = instant - floor;
    long diffToCeiling = ceiling - instant;
    if(diffFromFloor < diffToCeiling) {
      return floor;
    }
    else 
      if(diffToCeiling < diffFromFloor) {
        return ceiling;
      }
      else {
        if((get(ceiling) & 1) == 0) {
          return ceiling;
        }
        return floor;
      }
  }
  public long roundHalfFloor(long instant) {
    long floor = roundFloor(instant);
    long ceiling = roundCeiling(instant);
    long diffFromFloor = instant - floor;
    long diffToCeiling = ceiling - instant;
    if(diffFromFloor <= diffToCeiling) {
      return floor;
    }
    else {
      return ceiling;
    }
  }
  abstract public long set(long instant, int value);
  final public long set(long instant, String text) {
    return set(instant, text, null);
  }
  public long set(long instant, String text, Locale locale) {
    int value = convertText(text, locale);
    return set(instant, value);
  }
}