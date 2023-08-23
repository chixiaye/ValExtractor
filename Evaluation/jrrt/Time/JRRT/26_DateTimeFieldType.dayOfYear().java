package org.joda.time;
import java.io.Serializable;
import java.util.Comparator;
import org.joda.time.convert.ConverterManager;
import org.joda.time.convert.InstantConverter;

public class DateTimeComparator implements Comparator<Object>, Serializable  {
  final private static long serialVersionUID = -6097339773320178364L;
  final private static DateTimeComparator ALL_INSTANCE = new DateTimeComparator(null, null);
  final private static DateTimeComparator DATE_INSTANCE = new DateTimeComparator(DateTimeFieldType.dayOfYear(), null);
  final private static DateTimeComparator TIME_INSTANCE = new DateTimeComparator(null, DateTimeFieldType.dayOfYear());
  final private DateTimeFieldType iLowerLimit;
  final private DateTimeFieldType iUpperLimit;
  protected DateTimeComparator(DateTimeFieldType lowerLimit, DateTimeFieldType upperLimit) {
    super();
    iLowerLimit = lowerLimit;
    iUpperLimit = upperLimit;
  }
  public static DateTimeComparator getDateOnlyInstance() {
    return DATE_INSTANCE;
  }
  public static DateTimeComparator getInstance() {
    return ALL_INSTANCE;
  }
  public static DateTimeComparator getInstance(DateTimeFieldType lowerLimit) {
    return getInstance(lowerLimit, null);
  }
  public static DateTimeComparator getInstance(DateTimeFieldType lowerLimit, DateTimeFieldType upperLimit) {
    if(lowerLimit == null && upperLimit == null) {
      return ALL_INSTANCE;
    }
    DateTimeFieldType var_26 = DateTimeFieldType.dayOfYear();
    if(lowerLimit == var_26 && upperLimit == null) {
      return DATE_INSTANCE;
    }
    if(lowerLimit == null && upperLimit == DateTimeFieldType.dayOfYear()) {
      return TIME_INSTANCE;
    }
    return new DateTimeComparator(lowerLimit, upperLimit);
  }
  public static DateTimeComparator getTimeOnlyInstance() {
    return TIME_INSTANCE;
  }
  public DateTimeFieldType getLowerLimit() {
    return iLowerLimit;
  }
  public DateTimeFieldType getUpperLimit() {
    return iUpperLimit;
  }
  private Object readResolve() {
    return getInstance(iLowerLimit, iUpperLimit);
  }
  public String toString() {
    if(iLowerLimit == iUpperLimit) {
      return "DateTimeComparator[" + (iLowerLimit == null ? "" : iLowerLimit.getName()) + "]";
    }
    else {
      return "DateTimeComparator[" + (iLowerLimit == null ? "" : iLowerLimit.getName()) + "-" + (iUpperLimit == null ? "" : iUpperLimit.getName()) + "]";
    }
  }
  public boolean equals(Object object) {
    if(object instanceof DateTimeComparator) {
      DateTimeComparator other = (DateTimeComparator)object;
      return (iLowerLimit == other.getLowerLimit() || (iLowerLimit != null && iLowerLimit.equals(other.getLowerLimit()))) && (iUpperLimit == other.getUpperLimit() || (iUpperLimit != null && iUpperLimit.equals(other.getUpperLimit())));
    }
    return false;
  }
  public int compare(Object lhsObj, Object rhsObj) {
    InstantConverter conv = ConverterManager.getInstance().getInstantConverter(lhsObj);
    Chronology lhsChrono = conv.getChronology(lhsObj, (Chronology)null);
    long lhsMillis = conv.getInstantMillis(lhsObj, lhsChrono);
    conv = ConverterManager.getInstance().getInstantConverter(rhsObj);
    Chronology rhsChrono = conv.getChronology(rhsObj, (Chronology)null);
    long rhsMillis = conv.getInstantMillis(rhsObj, rhsChrono);
    if(iLowerLimit != null) {
      lhsMillis = iLowerLimit.getField(lhsChrono).roundFloor(lhsMillis);
      rhsMillis = iLowerLimit.getField(rhsChrono).roundFloor(rhsMillis);
    }
    if(iUpperLimit != null) {
      lhsMillis = iUpperLimit.getField(lhsChrono).remainder(lhsMillis);
      rhsMillis = iUpperLimit.getField(rhsChrono).remainder(rhsMillis);
    }
    if(lhsMillis < rhsMillis) {
      return -1;
    }
    else 
      if(lhsMillis > rhsMillis) {
        return 1;
      }
      else {
        return 0;
      }
  }
  public int hashCode() {
    return (iLowerLimit == null ? 0 : iLowerLimit.hashCode()) + (123 * (iUpperLimit == null ? 0 : iUpperLimit.hashCode()));
  }
}