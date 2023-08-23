package org.joda.time.field;
import org.joda.time.Chronology;
import org.joda.time.DateTimeField;

public class LenientDateTimeField extends DelegatedDateTimeField  {
  final private static long serialVersionUID = 8714085824173290599L;
  final private Chronology iBase;
  protected LenientDateTimeField(DateTimeField field, Chronology base) {
    super(field);
    iBase = base;
  }
  public static DateTimeField getInstance(DateTimeField field, Chronology base) {
    if(field == null) {
      return null;
    }
    if(field instanceof StrictDateTimeField) {
      field = ((StrictDateTimeField)field).getWrappedField();
    }
    if(field.isLenient()) {
      return field;
    }
    return new LenientDateTimeField(field, base);
  }
  final public boolean isLenient() {
    return true;
  }
  public long set(long instant, int value) {
    org.joda.time.DateTimeZone var_452 = iBase.getZone();
    long localInstant = var_452.convertUTCToLocal(instant);
    long difference = FieldUtils.safeSubtract(value, get(instant));
    localInstant = getType().getField(iBase.withUTC()).add(localInstant, difference);
    return iBase.getZone().convertLocalToUTC(localInstant, false, instant);
  }
}