package org.joda.time;
import java.io.Serializable;
import org.joda.time.base.BaseInterval;
import org.joda.time.field.FieldUtils;
import org.joda.time.format.ISODateTimeFormat;
import org.joda.time.format.ISOPeriodFormat;

public class MutableInterval extends BaseInterval implements ReadWritableInterval, Cloneable, Serializable  {
  final private static long serialVersionUID = -5982824024992428470L;
  public MutableInterval() {
    super(0L, 0L, null);
  }
  public MutableInterval(Object interval) {
    super(interval, null);
  }
  public MutableInterval(Object interval, Chronology chronology) {
    super(interval, chronology);
  }
  public MutableInterval(ReadableDuration duration, ReadableInstant end) {
    super(duration, end);
  }
  public MutableInterval(ReadableInstant start, ReadableDuration duration) {
    super(start, duration);
  }
  public MutableInterval(ReadableInstant start, ReadableInstant end) {
    super(start, end);
  }
  public MutableInterval(ReadableInstant start, ReadablePeriod period) {
    super(start, period);
  }
  public MutableInterval(ReadablePeriod period, ReadableInstant end) {
    super(period, end);
  }
  public MutableInterval(long startInstant, long endInstant) {
    super(startInstant, endInstant, null);
  }
  public MutableInterval(long startInstant, long endInstant, Chronology chronology) {
    super(startInstant, endInstant, chronology);
  }
  public MutableInterval copy() {
    return (MutableInterval)clone();
  }
  public static MutableInterval parse(String str) {
    return new MutableInterval(str);
  }
  public Object clone() {
    try {
      return super.clone();
    }
    catch (CloneNotSupportedException ex) {
      throw new InternalError("Clone error");
    }
  }
  public void setChronology(Chronology chrono) {
    super.setInterval(getStartMillis(), getEndMillis(), chrono);
  }
  public void setDurationAfterStart(long duration) {
    setEndMillis(FieldUtils.safeAdd(getStartMillis(), duration));
  }
  public void setDurationAfterStart(ReadableDuration duration) {
    long durationMillis = DateTimeUtils.getDurationMillis(duration);
    setEndMillis(FieldUtils.safeAdd(getStartMillis(), durationMillis));
  }
  public void setDurationBeforeEnd(long duration) {
    setStartMillis(FieldUtils.safeAdd(getEndMillis(), -duration));
  }
  public void setDurationBeforeEnd(ReadableDuration duration) {
    long durationMillis = DateTimeUtils.getDurationMillis(duration);
    setStartMillis(FieldUtils.safeAdd(getEndMillis(), -durationMillis));
  }
  public void setEnd(ReadableInstant end) {
    long endMillis = DateTimeUtils.getInstantMillis(end);
    super.setInterval(getStartMillis(), endMillis, getChronology());
  }
  public void setEndMillis(long endInstant) {
    super.setInterval(getStartMillis(), endInstant, getChronology());
  }
  public void setInterval(long startInstant, long endInstant) {
    super.setInterval(startInstant, endInstant, getChronology());
  }
  public void setInterval(ReadableInstant start, ReadableInstant end) {
    if(start == null && end == null) {
      long now = DateTimeUtils.currentTimeMillis();
      setInterval(now, now);
    }
    else {
      long startMillis = DateTimeUtils.getInstantMillis(start);
      long endMillis = DateTimeUtils.getInstantMillis(end);
      Chronology chrono = DateTimeUtils.getInstantChronology(start);
      super.setInterval(startMillis, endMillis, chrono);
    }
  }
  public void setInterval(ReadableInterval interval) {
    if(interval == null) {
      throw new IllegalArgumentException("Interval must not be null");
    }
    long startMillis = interval.getStartMillis();
    long endMillis = interval.getEndMillis();
    Chronology chrono = interval.getChronology();
    super.setInterval(startMillis, endMillis, chrono);
  }
  public void setPeriodAfterStart(ReadablePeriod period) {
    if(period == null) {
      long var_119 = getStartMillis();
      setEndMillis(var_119);
    }
    else {
      setEndMillis(getChronology().add(period, getStartMillis(), 1));
    }
  }
  public void setPeriodBeforeEnd(ReadablePeriod period) {
    if(period == null) {
      setStartMillis(getEndMillis());
    }
    else {
      setStartMillis(getChronology().add(period, getEndMillis(), -1));
    }
  }
  public void setStart(ReadableInstant start) {
    long startMillis = DateTimeUtils.getInstantMillis(start);
    super.setInterval(startMillis, getEndMillis(), getChronology());
  }
  public void setStartMillis(long startInstant) {
    super.setInterval(startInstant, getEndMillis(), getChronology());
  }
}