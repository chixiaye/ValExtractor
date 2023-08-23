package org.joda.time.base;
import java.util.Date;
import org.joda.convert.ToString;
import org.joda.time.Chronology;
import org.joda.time.DateTime;
import org.joda.time.DateTimeField;
import org.joda.time.DateTimeFieldType;
import org.joda.time.DateTimeUtils;
import org.joda.time.DateTimeZone;
import org.joda.time.Instant;
import org.joda.time.MutableDateTime;
import org.joda.time.ReadableInstant;
import org.joda.time.chrono.ISOChronology;
import org.joda.time.field.FieldUtils;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

abstract public class AbstractInstant implements ReadableInstant  {
  protected AbstractInstant() {
    super();
  }
  public Date toDate() {
    return new Date(getMillis());
  }
  public DateTime toDateTime() {
    return new DateTime(getMillis(), getZone());
  }
  public DateTime toDateTime(Chronology chronology) {
    return new DateTime(getMillis(), chronology);
  }
  public DateTime toDateTime(DateTimeZone zone) {
    Chronology chrono = DateTimeUtils.getChronology(getChronology());
    chrono = chrono.withZone(zone);
    return new DateTime(getMillis(), chrono);
  }
  public DateTime toDateTimeISO() {
    return new DateTime(getMillis(), ISOChronology.getInstance(getZone()));
  }
  public DateTimeZone getZone() {
    return getChronology().getZone();
  }
  public Instant toInstant() {
    return new Instant(getMillis());
  }
  public MutableDateTime toMutableDateTime() {
    return new MutableDateTime(getMillis(), getZone());
  }
  public MutableDateTime toMutableDateTime(Chronology chronology) {
    return new MutableDateTime(getMillis(), chronology);
  }
  public MutableDateTime toMutableDateTime(DateTimeZone zone) {
    Chronology chrono = DateTimeUtils.getChronology(getChronology());
    chrono = chrono.withZone(zone);
    return new MutableDateTime(getMillis(), chrono);
  }
  public MutableDateTime toMutableDateTimeISO() {
    return new MutableDateTime(getMillis(), ISOChronology.getInstance(getZone()));
  }
  @ToString() public String toString() {
    return ISODateTimeFormat.dateTime().print(this);
  }
  public String toString(DateTimeFormatter formatter) {
    if(formatter == null) {
      return toString();
    }
    return formatter.print(this);
  }
  public boolean equals(Object readableInstant) {
    if(this == readableInstant) {
      return true;
    }
    if(readableInstant instanceof ReadableInstant == false) {
      return false;
    }
    ReadableInstant otherInstant = (ReadableInstant)readableInstant;
    return getMillis() == otherInstant.getMillis() && FieldUtils.equals(getChronology(), otherInstant.getChronology());
  }
  public boolean isAfter(long instant) {
    return (getMillis() > instant);
  }
  public boolean isAfter(ReadableInstant instant) {
    long instantMillis = DateTimeUtils.getInstantMillis(instant);
    return isAfter(instantMillis);
  }
  public boolean isAfterNow() {
    return isAfter(DateTimeUtils.currentTimeMillis());
  }
  public boolean isBefore(long instant) {
    return (getMillis() < instant);
  }
  public boolean isBefore(ReadableInstant instant) {
    long instantMillis = DateTimeUtils.getInstantMillis(instant);
    return isBefore(instantMillis);
  }
  public boolean isBeforeNow() {
    return isBefore(DateTimeUtils.currentTimeMillis());
  }
  public boolean isEqual(long instant) {
    return (getMillis() == instant);
  }
  public boolean isEqual(ReadableInstant instant) {
    long instantMillis = DateTimeUtils.getInstantMillis(instant);
    return isEqual(instantMillis);
  }
  public boolean isEqualNow() {
    return isEqual(DateTimeUtils.currentTimeMillis());
  }
  public boolean isSupported(DateTimeFieldType type) {
    if(type == null) {
      return false;
    }
    return type.getField(getChronology()).isSupported();
  }
  public int compareTo(ReadableInstant other) {
    if(this == other) {
      return 0;
    }
    long otherMillis = other.getMillis();
    long thisMillis = getMillis();
    if(thisMillis == otherMillis) {
      return 0;
    }
    if(thisMillis < otherMillis) {
      return -1;
    }
    else {
      return 1;
    }
  }
  public int get(DateTimeField field) {
    if(field == null) {
      throw new IllegalArgumentException("The DateTimeField must not be null");
    }
    return field.get(getMillis());
  }
  public int get(DateTimeFieldType type) {
    if(type == null) {
      throw new IllegalArgumentException("The DateTimeFieldType must not be null");
    }
    return type.getField(getChronology()).get(getMillis());
  }
  public int hashCode() {
    long var_136 = getMillis();
    return ((int)(var_136 ^ (getMillis() >>> 32))) + (getChronology().hashCode());
  }
}