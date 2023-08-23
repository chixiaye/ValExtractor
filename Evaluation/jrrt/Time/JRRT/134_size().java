package org.joda.time.base;
import org.joda.convert.ToString;
import org.joda.time.DurationFieldType;
import org.joda.time.MutablePeriod;
import org.joda.time.Period;
import org.joda.time.ReadablePeriod;
import org.joda.time.format.ISOPeriodFormat;
import org.joda.time.format.PeriodFormatter;

abstract public class AbstractPeriod implements ReadablePeriod  {
  protected AbstractPeriod() {
    super();
  }
  public DurationFieldType getFieldType(int index) {
    return getPeriodType().getFieldType(index);
  }
  public DurationFieldType[] getFieldTypes() {
    DurationFieldType[] result = new DurationFieldType[size()];
    for(int i = 0; i < result.length; i++) {
      result[i] = getFieldType(i);
    }
    return result;
  }
  public MutablePeriod toMutablePeriod() {
    return new MutablePeriod(this);
  }
  public Period toPeriod() {
    return new Period(this);
  }
  @ToString() public String toString() {
    return ISOPeriodFormat.standard().print(this);
  }
  public String toString(PeriodFormatter formatter) {
    if(formatter == null) {
      return toString();
    }
    return formatter.print(this);
  }
  public boolean equals(Object period) {
    if(this == period) {
      return true;
    }
    if(period instanceof ReadablePeriod == false) {
      return false;
    }
    ReadablePeriod other = (ReadablePeriod)period;
    int var_134 = size();
    if(var_134 != other.size()) {
      return false;
    }
    for(int i = 0, isize = size(); i < isize; i++) {
      if(getValue(i) != other.getValue(i) || getFieldType(i) != other.getFieldType(i)) {
        return false;
      }
    }
    return true;
  }
  public boolean isSupported(DurationFieldType type) {
    return getPeriodType().isSupported(type);
  }
  public int get(DurationFieldType type) {
    int index = indexOf(type);
    if(index == -1) {
      return 0;
    }
    return getValue(index);
  }
  public int hashCode() {
    int total = 17;
    for(int i = 0, isize = size(); i < isize; i++) {
      total = 27 * total + getValue(i);
      total = 27 * total + getFieldType(i).hashCode();
    }
    return total;
  }
  public int indexOf(DurationFieldType type) {
    return getPeriodType().indexOf(type);
  }
  public int size() {
    return getPeriodType().size();
  }
  public int[] getValues() {
    int[] result = new int[size()];
    for(int i = 0; i < result.length; i++) {
      result[i] = getValue(i);
    }
    return result;
  }
}