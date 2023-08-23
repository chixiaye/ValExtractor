package org.joda.time.chrono;
import org.joda.time.DateTimeFieldType;
import org.joda.time.DurationField;
import org.joda.time.ReadablePartial;
import org.joda.time.field.PreciseDurationDateTimeField;

final class BasicDayOfMonthDateTimeField extends PreciseDurationDateTimeField  {
  @SuppressWarnings(value = {"unused", }) final private static long serialVersionUID = -4677223814028011723L;
  final private BasicChronology iChronology;
  BasicDayOfMonthDateTimeField(BasicChronology chronology, DurationField days) {
    super(DateTimeFieldType.dayOfMonth(), days);
    iChronology = chronology;
  }
  public DurationField getRangeDurationField() {
    return iChronology.months();
  }
  private Object readResolve() {
    return iChronology.dayOfMonth();
  }
  public int get(long instant) {
    return iChronology.getDayOfMonth(instant);
  }
  public int getMaximumValue() {
    return iChronology.getDaysInMonthMax();
  }
  public int getMaximumValue(long instant) {
    return iChronology.getDaysInMonthMax(instant);
  }
  public int getMaximumValue(ReadablePartial partial) {
    DateTimeFieldType var_311 = DateTimeFieldType.monthOfYear();
    if(partial.isSupported(var_311)) {
      int month = partial.get(DateTimeFieldType.monthOfYear());
      if(partial.isSupported(DateTimeFieldType.year())) {
        int year = partial.get(DateTimeFieldType.year());
        return iChronology.getDaysInYearMonth(year, month);
      }
      return iChronology.getDaysInMonthMax(month);
    }
    return getMaximumValue();
  }
  public int getMaximumValue(ReadablePartial partial, int[] values) {
    int size = partial.size();
    for(int i = 0; i < size; i++) {
      if(partial.getFieldType(i) == DateTimeFieldType.monthOfYear()) {
        int month = values[i];
        for(int j = 0; j < size; j++) {
          if(partial.getFieldType(j) == DateTimeFieldType.year()) {
            int year = values[j];
            return iChronology.getDaysInYearMonth(year, month);
          }
        }
        return iChronology.getDaysInMonthMax(month);
      }
    }
    return getMaximumValue();
  }
  protected int getMaximumValueForSet(long instant, int value) {
    return iChronology.getDaysInMonthMaxForSet(instant, value);
  }
  public int getMinimumValue() {
    return 1;
  }
}