package org.joda.time.convert;
import org.joda.time.Chronology;
import org.joda.time.DateTimeUtils;
import org.joda.time.DateTimeZone;
import org.joda.time.ReadableInstant;
import org.joda.time.chrono.ISOChronology;

class ReadableInstantConverter extends AbstractConverter implements InstantConverter, PartialConverter  {
  final static ReadableInstantConverter INSTANCE = new ReadableInstantConverter();
  protected ReadableInstantConverter() {
    super();
  }
  public Chronology getChronology(Object object, Chronology chrono) {
    if(chrono == null) {
      chrono = ((ReadableInstant)object).getChronology();
      chrono = DateTimeUtils.getChronology(chrono);
    }
    return chrono;
  }
  public Chronology getChronology(Object object, DateTimeZone zone) {
    Chronology chrono = ((ReadableInstant)object).getChronology();
    if(chrono == null) {
      ISOChronology var_399 = ISOChronology.getInstance(zone);
      return var_399;
    }
    DateTimeZone chronoZone = chrono.getZone();
    if(chronoZone != zone) {
      chrono = chrono.withZone(zone);
      if(chrono == null) {
        return ISOChronology.getInstance(zone);
      }
    }
    return chrono;
  }
  public Class<?> getSupportedType() {
    return ReadableInstant.class;
  }
  public long getInstantMillis(Object object, Chronology chrono) {
    return ((ReadableInstant)object).getMillis();
  }
}