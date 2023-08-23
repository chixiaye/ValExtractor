package org.joda.time.base;
import org.joda.convert.ToString;
import org.joda.time.Duration;
import org.joda.time.Period;
import org.joda.time.ReadableDuration;
import org.joda.time.ReadableInstant;
import org.joda.time.format.FormatUtils;

abstract public class AbstractDuration implements ReadableDuration  {
  protected AbstractDuration() {
    super();
  }
  public Duration toDuration() {
    return new Duration(getMillis());
  }
  public Period toPeriod() {
    return new Period(getMillis());
  }
  @ToString() public String toString() {
    long millis = getMillis();
    StringBuffer buf = new StringBuffer();
    buf.append("PT");
    boolean negative = (millis < 0);
    FormatUtils.appendUnpaddedInteger(buf, millis);
    while(buf.length() < (negative ? 7 : 6)){
      buf.insert(negative ? 3 : 2, "0");
    }
    if((millis / 1000) * 1000 == millis) {
      int var_135 = buf.length();
      buf.setLength(var_135 - 3);
    }
    else {
      buf.insert(buf.length() - 3, ".");
    }
    buf.append('S');
    return buf.toString();
  }
  public boolean equals(Object duration) {
    if(this == duration) {
      return true;
    }
    if(duration instanceof ReadableDuration == false) {
      return false;
    }
    ReadableDuration other = (ReadableDuration)duration;
    return (getMillis() == other.getMillis());
  }
  public boolean isEqual(ReadableDuration duration) {
    if(duration == null) {
      duration = Duration.ZERO;
    }
    return compareTo(duration) == 0;
  }
  public boolean isLongerThan(ReadableDuration duration) {
    if(duration == null) {
      duration = Duration.ZERO;
    }
    return compareTo(duration) > 0;
  }
  public boolean isShorterThan(ReadableDuration duration) {
    if(duration == null) {
      duration = Duration.ZERO;
    }
    return compareTo(duration) < 0;
  }
  public int compareTo(ReadableDuration other) {
    long thisMillis = this.getMillis();
    long otherMillis = other.getMillis();
    if(thisMillis < otherMillis) {
      return -1;
    }
    if(thisMillis > otherMillis) {
      return 1;
    }
    return 0;
  }
  public int hashCode() {
    long len = getMillis();
    return (int)(len ^ (len >>> 32));
  }
}