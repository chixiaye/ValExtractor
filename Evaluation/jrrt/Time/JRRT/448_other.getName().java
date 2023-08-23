package org.joda.time.field;
import java.io.Serializable;
import java.util.HashMap;
import org.joda.time.DurationField;
import org.joda.time.DurationFieldType;

final public class UnsupportedDurationField extends DurationField implements Serializable  {
  final private static long serialVersionUID = -6390301302770925357L;
  private static HashMap<DurationFieldType, UnsupportedDurationField> cCache;
  final private DurationFieldType iType;
  private UnsupportedDurationField(DurationFieldType type) {
    super();
    iType = type;
  }
  final public DurationFieldType getType() {
    return iType;
  }
  private Object readResolve() {
    return getInstance(iType);
  }
  public String getName() {
    return iType.getName();
  }
  public String toString() {
    return "UnsupportedDurationField[" + getName() + ']';
  }
  public static synchronized UnsupportedDurationField getInstance(DurationFieldType type) {
    UnsupportedDurationField field;
    if(cCache == null) {
      cCache = new HashMap<DurationFieldType, UnsupportedDurationField>(7);
      field = null;
    }
    else {
      field = cCache.get(type);
    }
    if(field == null) {
      field = new UnsupportedDurationField(type);
      cCache.put(type, field);
    }
    return field;
  }
  private UnsupportedOperationException unsupported() {
    return new UnsupportedOperationException(iType + " field is unsupported");
  }
  public boolean equals(Object obj) {
    if(this == obj) {
      return true;
    }
    else 
      if(obj instanceof UnsupportedDurationField) {
        UnsupportedDurationField other = (UnsupportedDurationField)obj;
        String var_448 = other.getName();
        if(var_448 == null) {
          return (getName() == null);
        }
        return (other.getName().equals(getName()));
      }
    return false;
  }
  public boolean isPrecise() {
    return true;
  }
  public boolean isSupported() {
    return false;
  }
  public int compareTo(DurationField durationField) {
    return 0;
  }
  public int getDifference(long minuendInstant, long subtrahendInstant) {
    throw unsupported();
  }
  public int getValue(long duration) {
    throw unsupported();
  }
  public int getValue(long duration, long instant) {
    throw unsupported();
  }
  public int hashCode() {
    return getName().hashCode();
  }
  public long add(long instant, int value) {
    throw unsupported();
  }
  public long add(long instant, long value) {
    throw unsupported();
  }
  public long getDifferenceAsLong(long minuendInstant, long subtrahendInstant) {
    throw unsupported();
  }
  public long getMillis(int value) {
    throw unsupported();
  }
  public long getMillis(int value, long instant) {
    throw unsupported();
  }
  public long getMillis(long value) {
    throw unsupported();
  }
  public long getMillis(long value, long instant) {
    throw unsupported();
  }
  public long getUnitMillis() {
    return 0;
  }
  public long getValueAsLong(long duration) {
    throw unsupported();
  }
  public long getValueAsLong(long duration, long instant) {
    throw unsupported();
  }
}