package org.joda.time.chrono;
import java.io.Serializable;
import org.joda.time.Chronology;
import org.joda.time.DateTimeField;
import org.joda.time.DateTimeFieldType;
import org.joda.time.DateTimeZone;
import org.joda.time.DurationField;
import org.joda.time.DurationFieldType;
import org.joda.time.IllegalFieldValueException;
import org.joda.time.ReadablePartial;
import org.joda.time.ReadablePeriod;
import org.joda.time.field.FieldUtils;
import org.joda.time.field.UnsupportedDateTimeField;
import org.joda.time.field.UnsupportedDurationField;

abstract public class BaseChronology extends Chronology implements Serializable  {
  final private static long serialVersionUID = -7310865996721419676L;
  protected BaseChronology() {
    super();
  }
  abstract public Chronology withUTC();
  abstract public Chronology withZone(DateTimeZone zone);
  public DateTimeField centuryOfEra() {
    return UnsupportedDateTimeField.getInstance(DateTimeFieldType.centuryOfEra(), centuries());
  }
  public DateTimeField clockhourOfDay() {
    return UnsupportedDateTimeField.getInstance(DateTimeFieldType.clockhourOfDay(), hours());
  }
  public DateTimeField clockhourOfHalfday() {
    return UnsupportedDateTimeField.getInstance(DateTimeFieldType.clockhourOfHalfday(), hours());
  }
  public DateTimeField dayOfMonth() {
    return UnsupportedDateTimeField.getInstance(DateTimeFieldType.dayOfMonth(), days());
  }
  public DateTimeField dayOfWeek() {
    return UnsupportedDateTimeField.getInstance(DateTimeFieldType.dayOfWeek(), days());
  }
  public DateTimeField dayOfYear() {
    return UnsupportedDateTimeField.getInstance(DateTimeFieldType.dayOfYear(), days());
  }
  public DateTimeField era() {
    return UnsupportedDateTimeField.getInstance(DateTimeFieldType.era(), eras());
  }
  public DateTimeField halfdayOfDay() {
    return UnsupportedDateTimeField.getInstance(DateTimeFieldType.halfdayOfDay(), halfdays());
  }
  public DateTimeField hourOfDay() {
    return UnsupportedDateTimeField.getInstance(DateTimeFieldType.hourOfDay(), hours());
  }
  public DateTimeField hourOfHalfday() {
    return UnsupportedDateTimeField.getInstance(DateTimeFieldType.hourOfHalfday(), hours());
  }
  public DateTimeField millisOfDay() {
    return UnsupportedDateTimeField.getInstance(DateTimeFieldType.millisOfDay(), millis());
  }
  public DateTimeField millisOfSecond() {
    return UnsupportedDateTimeField.getInstance(DateTimeFieldType.millisOfSecond(), millis());
  }
  public DateTimeField minuteOfDay() {
    return UnsupportedDateTimeField.getInstance(DateTimeFieldType.minuteOfDay(), minutes());
  }
  public DateTimeField minuteOfHour() {
    return UnsupportedDateTimeField.getInstance(DateTimeFieldType.minuteOfHour(), minutes());
  }
  public DateTimeField monthOfYear() {
    return UnsupportedDateTimeField.getInstance(DateTimeFieldType.monthOfYear(), months());
  }
  public DateTimeField secondOfDay() {
    return UnsupportedDateTimeField.getInstance(DateTimeFieldType.secondOfDay(), seconds());
  }
  public DateTimeField secondOfMinute() {
    return UnsupportedDateTimeField.getInstance(DateTimeFieldType.secondOfMinute(), seconds());
  }
  public DateTimeField weekOfWeekyear() {
    return UnsupportedDateTimeField.getInstance(DateTimeFieldType.weekOfWeekyear(), weeks());
  }
  public DateTimeField weekyear() {
    return UnsupportedDateTimeField.getInstance(DateTimeFieldType.weekyear(), weekyears());
  }
  public DateTimeField weekyearOfCentury() {
    return UnsupportedDateTimeField.getInstance(DateTimeFieldType.weekyearOfCentury(), weekyears());
  }
  public DateTimeField year() {
    return UnsupportedDateTimeField.getInstance(DateTimeFieldType.year(), years());
  }
  public DateTimeField yearOfCentury() {
    return UnsupportedDateTimeField.getInstance(DateTimeFieldType.yearOfCentury(), years());
  }
  public DateTimeField yearOfEra() {
    return UnsupportedDateTimeField.getInstance(DateTimeFieldType.yearOfEra(), years());
  }
  abstract public DateTimeZone getZone();
  public DurationField centuries() {
    return UnsupportedDurationField.getInstance(DurationFieldType.centuries());
  }
  public DurationField days() {
    return UnsupportedDurationField.getInstance(DurationFieldType.days());
  }
  public DurationField eras() {
    return UnsupportedDurationField.getInstance(DurationFieldType.eras());
  }
  public DurationField halfdays() {
    return UnsupportedDurationField.getInstance(DurationFieldType.halfdays());
  }
  public DurationField hours() {
    return UnsupportedDurationField.getInstance(DurationFieldType.hours());
  }
  public DurationField millis() {
    return UnsupportedDurationField.getInstance(DurationFieldType.millis());
  }
  public DurationField minutes() {
    return UnsupportedDurationField.getInstance(DurationFieldType.minutes());
  }
  public DurationField months() {
    return UnsupportedDurationField.getInstance(DurationFieldType.months());
  }
  public DurationField seconds() {
    return UnsupportedDurationField.getInstance(DurationFieldType.seconds());
  }
  public DurationField weeks() {
    return UnsupportedDurationField.getInstance(DurationFieldType.weeks());
  }
  public DurationField weekyears() {
    return UnsupportedDurationField.getInstance(DurationFieldType.weekyears());
  }
  public DurationField years() {
    return UnsupportedDurationField.getInstance(DurationFieldType.years());
  }
  abstract public String toString();
  public int[] get(ReadablePartial partial, long instant) {
    int size = partial.size();
    int[] values = new int[size];
    for(int i = 0; i < size; i++) {
      values[i] = partial.getFieldType(i).getField(this).get(instant);
    }
    return values;
  }
  public int[] get(ReadablePeriod period, long duration) {
    int size = period.size();
    int[] values = new int[size];
    if(duration != 0) {
      long current = 0;
      for(int i = 0; i < size; i++) {
        DurationField field = period.getFieldType(i).getField(this);
        if(field.isPrecise()) {
          int value = field.getDifference(duration, current);
          current = field.add(current, value);
          values[i] = value;
        }
      }
    }
    return values;
  }
  public int[] get(ReadablePeriod period, long startInstant, long endInstant) {
    int size = period.size();
    int[] values = new int[size];
    if(startInstant != endInstant) {
      for(int i = 0; i < size; i++) {
        DurationField field = period.getFieldType(i).getField(this);
        int value = field.getDifference(endInstant, startInstant);
        startInstant = field.add(startInstant, value);
        values[i] = value;
      }
    }
    return values;
  }
  public long add(long instant, long duration, int scalar) {
    if(duration == 0 || scalar == 0) {
      return instant;
    }
    long add = FieldUtils.safeMultiply(duration, scalar);
    return FieldUtils.safeAdd(instant, add);
  }
  public long add(ReadablePeriod period, long instant, int scalar) {
    if(scalar != 0 && period != null) {
      for(int i = 0, isize = period.size(); i < isize; i++) {
        long value = period.getValue(i);
        if(value != 0) {
          instant = period.getFieldType(i).getField(this).add(instant, value * scalar);
        }
      }
    }
    return instant;
  }
  public long getDateTimeMillis(int year, int monthOfYear, int dayOfMonth, int millisOfDay) throws IllegalArgumentException {
    long instant = year().set(0, year);
    instant = monthOfYear().set(instant, monthOfYear);
    instant = dayOfMonth().set(instant, dayOfMonth);
    return millisOfDay().set(instant, millisOfDay);
  }
  public long getDateTimeMillis(int year, int monthOfYear, int dayOfMonth, int hourOfDay, int minuteOfHour, int secondOfMinute, int millisOfSecond) throws IllegalArgumentException {
    long instant = year().set(0, year);
    instant = monthOfYear().set(instant, monthOfYear);
    instant = dayOfMonth().set(instant, dayOfMonth);
    instant = hourOfDay().set(instant, hourOfDay);
    instant = minuteOfHour().set(instant, minuteOfHour);
    instant = secondOfMinute().set(instant, secondOfMinute);
    return millisOfSecond().set(instant, millisOfSecond);
  }
  public long getDateTimeMillis(long instant, int hourOfDay, int minuteOfHour, int secondOfMinute, int millisOfSecond) throws IllegalArgumentException {
    instant = hourOfDay().set(instant, hourOfDay);
    instant = minuteOfHour().set(instant, minuteOfHour);
    instant = secondOfMinute().set(instant, secondOfMinute);
    return millisOfSecond().set(instant, millisOfSecond);
  }
  public long set(ReadablePartial partial, long instant) {
    for(int i = 0, isize = partial.size(); i < isize; i++) {
      instant = partial.getFieldType(i).getField(this).set(instant, partial.getValue(i));
    }
    return instant;
  }
  public void validate(ReadablePartial partial, int[] values) {
    int size = partial.size();
    for(int i = 0; i < size; i++) {
      int var_348 = values[i];
      int value = var_348;
      DateTimeField field = partial.getField(i);
      if(value < field.getMinimumValue()) {
        throw new IllegalFieldValueException(field.getType(), Integer.valueOf(value), Integer.valueOf(field.getMinimumValue()), null);
      }
      if(value > field.getMaximumValue()) {
        throw new IllegalFieldValueException(field.getType(), Integer.valueOf(value), null, Integer.valueOf(field.getMaximumValue()));
      }
    }
    for(int i = 0; i < size; i++) {
      int value = values[i];
      DateTimeField field = partial.getField(i);
      if(value < field.getMinimumValue(partial, values)) {
        throw new IllegalFieldValueException(field.getType(), Integer.valueOf(value), Integer.valueOf(field.getMinimumValue(partial, values)), null);
      }
      if(value > field.getMaximumValue(partial, values)) {
        throw new IllegalFieldValueException(field.getType(), Integer.valueOf(value), null, Integer.valueOf(field.getMaximumValue(partial, values)));
      }
    }
  }
}