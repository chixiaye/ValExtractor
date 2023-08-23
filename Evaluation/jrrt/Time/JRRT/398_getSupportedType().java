package org.joda.time.convert;
import org.joda.time.Chronology;
import org.joda.time.DateTimeUtils;
import org.joda.time.DateTimeZone;
import org.joda.time.PeriodType;
import org.joda.time.ReadablePartial;
import org.joda.time.chrono.ISOChronology;
import org.joda.time.format.DateTimeFormatter;

abstract public class AbstractConverter implements Converter  {
  protected AbstractConverter() {
    super();
  }
  public Chronology getChronology(Object object, Chronology chrono) {
    return DateTimeUtils.getChronology(chrono);
  }
  public Chronology getChronology(Object object, DateTimeZone zone) {
    return ISOChronology.getInstance(zone);
  }
  public PeriodType getPeriodType(Object object) {
    return PeriodType.standard();
  }
  public String toString() {
    Class<?> var_398 = getSupportedType();
    return "Converter[" + (var_398 == null ? "null" : getSupportedType().getName()) + "]";
  }
  public boolean isReadableInterval(Object object, Chronology chrono) {
    return false;
  }
  public int[] getPartialValues(ReadablePartial fieldSource, Object object, Chronology chrono) {
    long instant = getInstantMillis(object, chrono);
    return chrono.get(fieldSource, instant);
  }
  public int[] getPartialValues(ReadablePartial fieldSource, Object object, Chronology chrono, DateTimeFormatter parser) {
    return getPartialValues(fieldSource, object, chrono);
  }
  public long getInstantMillis(Object object, Chronology chrono) {
    return DateTimeUtils.currentTimeMillis();
  }
}