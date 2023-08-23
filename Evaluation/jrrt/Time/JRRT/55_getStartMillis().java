package org.joda.time;
import java.io.Serializable;
import org.joda.time.base.BaseInterval;
import org.joda.time.chrono.ISOChronology;
import org.joda.time.format.ISODateTimeFormat;
import org.joda.time.format.ISOPeriodFormat;

final public class Interval extends BaseInterval implements ReadableInterval, Serializable  {
  final private static long serialVersionUID = 4922451897541386752L;
  public Interval(Object interval) {
    super(interval, null);
  }
  public Interval(Object interval, Chronology chronology) {
    super(interval, chronology);
  }
  public Interval(ReadableDuration duration, ReadableInstant end) {
    super(duration, end);
  }
  public Interval(ReadableInstant start, ReadableDuration duration) {
    super(start, duration);
  }
  public Interval(ReadableInstant start, ReadableInstant end) {
    super(start, end);
  }
  public Interval(ReadableInstant start, ReadablePeriod period) {
    super(start, period);
  }
  public Interval(ReadablePeriod period, ReadableInstant end) {
    super(period, end);
  }
  public Interval(long startInstant, long endInstant) {
    super(startInstant, endInstant, null);
  }
  public Interval(long startInstant, long endInstant, Chronology chronology) {
    super(startInstant, endInstant, chronology);
  }
  public Interval(long startInstant, long endInstant, DateTimeZone zone) {
    super(startInstant, endInstant, ISOChronology.getInstance(zone));
  }
  public Interval gap(ReadableInterval interval) {
    interval = DateTimeUtils.getReadableInterval(interval);
    long otherStart = interval.getStartMillis();
    long otherEnd = interval.getEndMillis();
    long thisStart = getStartMillis();
    long thisEnd = getEndMillis();
    if(thisStart > otherEnd) {
      return new Interval(otherEnd, thisStart, getChronology());
    }
    else 
      if(otherStart > thisEnd) {
        return new Interval(thisEnd, otherStart, getChronology());
      }
      else {
        return null;
      }
  }
  public Interval overlap(ReadableInterval interval) {
    interval = DateTimeUtils.getReadableInterval(interval);
    if(overlaps(interval) == false) {
      return null;
    }
    long start = Math.max(getStartMillis(), interval.getStartMillis());
    long end = Math.min(getEndMillis(), interval.getEndMillis());
    return new Interval(start, end, getChronology());
  }
  public static Interval parse(String str) {
    return new Interval(str);
  }
  public Interval toInterval() {
    return this;
  }
  public Interval withChronology(Chronology chronology) {
    if(getChronology() == chronology) {
      return this;
    }
    return new Interval(getStartMillis(), getEndMillis(), chronology);
  }
  public Interval withDurationAfterStart(ReadableDuration duration) {
    long durationMillis = DateTimeUtils.getDurationMillis(duration);
    if(durationMillis == toDurationMillis()) {
      return this;
    }
    Chronology chrono = getChronology();
    long startMillis = getStartMillis();
    long endMillis = chrono.add(startMillis, durationMillis, 1);
    return new Interval(startMillis, endMillis, chrono);
  }
  public Interval withDurationBeforeEnd(ReadableDuration duration) {
    long durationMillis = DateTimeUtils.getDurationMillis(duration);
    if(durationMillis == toDurationMillis()) {
      return this;
    }
    Chronology chrono = getChronology();
    long endMillis = getEndMillis();
    long startMillis = chrono.add(endMillis, durationMillis, -1);
    return new Interval(startMillis, endMillis, chrono);
  }
  public Interval withEnd(ReadableInstant end) {
    long endMillis = DateTimeUtils.getInstantMillis(end);
    return withEndMillis(endMillis);
  }
  public Interval withEndMillis(long endInstant) {
    if(endInstant == getEndMillis()) {
      return this;
    }
    return new Interval(getStartMillis(), endInstant, getChronology());
  }
  public Interval withPeriodAfterStart(ReadablePeriod period) {
    if(period == null) {
      return withDurationAfterStart(null);
    }
    Chronology chrono = getChronology();
    long startMillis = getStartMillis();
    long endMillis = chrono.add(period, startMillis, 1);
    return new Interval(startMillis, endMillis, chrono);
  }
  public Interval withPeriodBeforeEnd(ReadablePeriod period) {
    if(period == null) {
      return withDurationBeforeEnd(null);
    }
    Chronology chrono = getChronology();
    long endMillis = getEndMillis();
    long startMillis = chrono.add(period, endMillis, -1);
    return new Interval(startMillis, endMillis, chrono);
  }
  public Interval withStart(ReadableInstant start) {
    long startMillis = DateTimeUtils.getInstantMillis(start);
    return withStartMillis(startMillis);
  }
  public Interval withStartMillis(long startInstant) {
    if(startInstant == getStartMillis()) {
      return this;
    }
    return new Interval(startInstant, getEndMillis(), getChronology());
  }
  public boolean abuts(ReadableInterval interval) {
    if(interval == null) {
      long now = DateTimeUtils.currentTimeMillis();
      long var_55 = getStartMillis();
      return (var_55 == now || getEndMillis() == now);
    }
    else {
      return (interval.getEndMillis() == getStartMillis() || getEndMillis() == interval.getStartMillis());
    }
  }
}