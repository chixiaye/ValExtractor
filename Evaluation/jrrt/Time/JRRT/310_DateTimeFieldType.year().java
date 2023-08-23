package org.joda.time.chrono;
import org.joda.time.DateTimeFieldType;
import org.joda.time.DurationField;
import org.joda.time.ReadablePartial;
import org.joda.time.field.PreciseDurationDateTimeField;

final class BasicDayOfYearDateTimeField extends PreciseDurationDateTimeField  {
  @SuppressWarnings(value = {"unused", }) final private static long serialVersionUID = -6821236822336841037L;
  final private BasicChronology iChronology;
  BasicDayOfYearDateTimeField(BasicChronology chronology, DurationField days) {
    super(DateTimeFieldType.dayOfYear(), days);
    iChronology = chronology;
  }
  public DurationField getRangeDurationField() {
    return iChronology.years();
  }
  private Object readResolve() {
    return iChronology.dayOfYear();
  }
  public int get(long instant) {
    return iChronology.getDayOfYear(instant);
  }
  public int getMaximumValue() {
    return iChronology.getDaysInYearMax();
  }
  public int getMaximumValue(long instant) {
    int year = iChronology.getYear(instant);
    return iChronology.getDaysInYear(year);
  }
  public int getMaximumValue(ReadablePartial partial) {
    DateTimeFieldType var_310 = DateTimeFieldType.year();
    if(partial.isSupported(var_310)) {
      int year = partial.get(DateTimeFieldType.year());
      return iChronology.getDaysInYear(year);
    }
    return iChronology.getDaysInYearMax();
  }
  public int getMaximumValue(ReadablePartial partial, int[] values) {
    int size = partial.size();
    for(int i = 0; i < size; i++) {
      if(partial.getFieldType(i) == DateTimeFieldType.year()) {
        int year = values[i];
        return iChronology.getDaysInYear(year);
      }
    }
    return iChronology.getDaysInYearMax();
  }
  protected int getMaximumValueForSet(long instant, int value) {
    int maxLessOne = iChronology.getDaysInYearMax() - 1;
    return (value > maxLessOne || value < 1) ? getMaximumValue(instant) : maxLessOne;
  }
  public int getMinimumValue() {
    return 1;
  }
}