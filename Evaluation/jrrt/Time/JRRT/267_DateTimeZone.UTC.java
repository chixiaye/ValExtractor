package org.joda.time.chrono;
import java.util.HashMap;
import java.util.Locale;
import org.joda.time.Chronology;
import org.joda.time.DateTime;
import org.joda.time.DateTimeField;
import org.joda.time.DateTimeZone;
import org.joda.time.DurationField;
import org.joda.time.MutableDateTime;
import org.joda.time.ReadableDateTime;
import org.joda.time.field.DecoratedDateTimeField;
import org.joda.time.field.DecoratedDurationField;
import org.joda.time.field.FieldUtils;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

final public class LimitChronology extends AssembledChronology  {
  final private static long serialVersionUID = 7670866536893052522L;
  final DateTime iLowerLimit;
  final DateTime iUpperLimit;
  private transient LimitChronology iWithUTC;
  private LimitChronology(Chronology base, DateTime lowerLimit, DateTime upperLimit) {
    super(base, null);
    iLowerLimit = lowerLimit;
    iUpperLimit = upperLimit;
  }
  public Chronology withUTC() {
    return withZone(DateTimeZone.UTC);
  }
  public Chronology withZone(DateTimeZone zone) {
    if(zone == null) {
      zone = DateTimeZone.getDefault();
    }
    if(zone == getZone()) {
      return this;
    }
    DateTimeZone var_267 = DateTimeZone.UTC;
    if(zone == var_267 && iWithUTC != null) {
      return iWithUTC;
    }
    DateTime lowerLimit = iLowerLimit;
    if(lowerLimit != null) {
      MutableDateTime mdt = lowerLimit.toMutableDateTime();
      mdt.setZoneRetainFields(zone);
      lowerLimit = mdt.toDateTime();
    }
    DateTime upperLimit = iUpperLimit;
    if(upperLimit != null) {
      MutableDateTime mdt = upperLimit.toMutableDateTime();
      mdt.setZoneRetainFields(zone);
      upperLimit = mdt.toDateTime();
    }
    LimitChronology chrono = getInstance(getBase().withZone(zone), lowerLimit, upperLimit);
    if(zone == DateTimeZone.UTC) {
      iWithUTC = chrono;
    }
    return chrono;
  }
  public DateTime getLowerLimit() {
    return iLowerLimit;
  }
  public DateTime getUpperLimit() {
    return iUpperLimit;
  }
  private DateTimeField convertField(DateTimeField field, HashMap<Object, Object> converted) {
    if(field == null || !field.isSupported()) {
      return field;
    }
    if(converted.containsKey(field)) {
      return (DateTimeField)converted.get(field);
    }
    LimitDateTimeField limitField = new LimitDateTimeField(field, convertField(field.getDurationField(), converted), convertField(field.getRangeDurationField(), converted), convertField(field.getLeapDurationField(), converted));
    converted.put(field, limitField);
    return limitField;
  }
  private DurationField convertField(DurationField field, HashMap<Object, Object> converted) {
    if(field == null || !field.isSupported()) {
      return field;
    }
    if(converted.containsKey(field)) {
      return (DurationField)converted.get(field);
    }
    LimitDurationField limitField = new LimitDurationField(field);
    converted.put(field, limitField);
    return limitField;
  }
  public static LimitChronology getInstance(Chronology base, ReadableDateTime lowerLimit, ReadableDateTime upperLimit) {
    if(base == null) {
      throw new IllegalArgumentException("Must supply a chronology");
    }
    lowerLimit = lowerLimit == null ? null : lowerLimit.toDateTime();
    upperLimit = upperLimit == null ? null : upperLimit.toDateTime();
    if(lowerLimit != null && upperLimit != null) {
      if(!lowerLimit.isBefore(upperLimit)) {
        throw new IllegalArgumentException("The lower limit must be come before than the upper limit");
      }
    }
    return new LimitChronology(base, (DateTime)lowerLimit, (DateTime)upperLimit);
  }
  public String toString() {
    return "LimitChronology[" + getBase().toString() + ", " + (getLowerLimit() == null ? "NoLimit" : getLowerLimit().toString()) + ", " + (getUpperLimit() == null ? "NoLimit" : getUpperLimit().toString()) + ']';
  }
  public boolean equals(Object obj) {
    if(this == obj) {
      return true;
    }
    if(obj instanceof LimitChronology == false) {
      return false;
    }
    LimitChronology chrono = (LimitChronology)obj;
    return getBase().equals(chrono.getBase()) && FieldUtils.equals(getLowerLimit(), chrono.getLowerLimit()) && FieldUtils.equals(getUpperLimit(), chrono.getUpperLimit());
  }
  public int hashCode() {
    int hash = 317351877;
    hash += (getLowerLimit() != null ? getLowerLimit().hashCode() : 0);
    hash += (getUpperLimit() != null ? getUpperLimit().hashCode() : 0);
    hash += getBase().hashCode() * 7;
    return hash;
  }
  public long getDateTimeMillis(int year, int monthOfYear, int dayOfMonth, int millisOfDay) throws IllegalArgumentException {
    long instant = getBase().getDateTimeMillis(year, monthOfYear, dayOfMonth, millisOfDay);
    checkLimits(instant, "resulting");
    return instant;
  }
  public long getDateTimeMillis(int year, int monthOfYear, int dayOfMonth, int hourOfDay, int minuteOfHour, int secondOfMinute, int millisOfSecond) throws IllegalArgumentException {
    long instant = getBase().getDateTimeMillis(year, monthOfYear, dayOfMonth, hourOfDay, minuteOfHour, secondOfMinute, millisOfSecond);
    checkLimits(instant, "resulting");
    return instant;
  }
  public long getDateTimeMillis(long instant, int hourOfDay, int minuteOfHour, int secondOfMinute, int millisOfSecond) throws IllegalArgumentException {
    checkLimits(instant, null);
    instant = getBase().getDateTimeMillis(instant, hourOfDay, minuteOfHour, secondOfMinute, millisOfSecond);
    checkLimits(instant, "resulting");
    return instant;
  }
  protected void assemble(Fields fields) {
    HashMap<Object, Object> converted = new HashMap<Object, Object>();
    fields.eras = convertField(fields.eras, converted);
    fields.centuries = convertField(fields.centuries, converted);
    fields.years = convertField(fields.years, converted);
    fields.months = convertField(fields.months, converted);
    fields.weekyears = convertField(fields.weekyears, converted);
    fields.weeks = convertField(fields.weeks, converted);
    fields.days = convertField(fields.days, converted);
    fields.halfdays = convertField(fields.halfdays, converted);
    fields.hours = convertField(fields.hours, converted);
    fields.minutes = convertField(fields.minutes, converted);
    fields.seconds = convertField(fields.seconds, converted);
    fields.millis = convertField(fields.millis, converted);
    fields.year = convertField(fields.year, converted);
    fields.yearOfEra = convertField(fields.yearOfEra, converted);
    fields.yearOfCentury = convertField(fields.yearOfCentury, converted);
    fields.centuryOfEra = convertField(fields.centuryOfEra, converted);
    fields.era = convertField(fields.era, converted);
    fields.dayOfWeek = convertField(fields.dayOfWeek, converted);
    fields.dayOfMonth = convertField(fields.dayOfMonth, converted);
    fields.dayOfYear = convertField(fields.dayOfYear, converted);
    fields.monthOfYear = convertField(fields.monthOfYear, converted);
    fields.weekOfWeekyear = convertField(fields.weekOfWeekyear, converted);
    fields.weekyear = convertField(fields.weekyear, converted);
    fields.weekyearOfCentury = convertField(fields.weekyearOfCentury, converted);
    fields.millisOfSecond = convertField(fields.millisOfSecond, converted);
    fields.millisOfDay = convertField(fields.millisOfDay, converted);
    fields.secondOfMinute = convertField(fields.secondOfMinute, converted);
    fields.secondOfDay = convertField(fields.secondOfDay, converted);
    fields.minuteOfHour = convertField(fields.minuteOfHour, converted);
    fields.minuteOfDay = convertField(fields.minuteOfDay, converted);
    fields.hourOfDay = convertField(fields.hourOfDay, converted);
    fields.hourOfHalfday = convertField(fields.hourOfHalfday, converted);
    fields.clockhourOfDay = convertField(fields.clockhourOfDay, converted);
    fields.clockhourOfHalfday = convertField(fields.clockhourOfHalfday, converted);
    fields.halfdayOfDay = convertField(fields.halfdayOfDay, converted);
  }
  void checkLimits(long instant, String desc) {
    DateTime limit;
    if((limit = iLowerLimit) != null && instant < limit.getMillis()) {
      throw new LimitException(desc, true);
    }
    if((limit = iUpperLimit) != null && instant >= limit.getMillis()) {
      throw new LimitException(desc, false);
    }
  }
  
  private class LimitDateTimeField extends DecoratedDateTimeField  {
    @SuppressWarnings(value = {"unused", }) final private static long serialVersionUID = -2435306746995699312L;
    final private DurationField iDurationField;
    final private DurationField iRangeDurationField;
    final private DurationField iLeapDurationField;
    LimitDateTimeField(DateTimeField field, DurationField durationField, DurationField rangeDurationField, DurationField leapDurationField) {
      super(field, field.getType());
      iDurationField = durationField;
      iRangeDurationField = rangeDurationField;
      iLeapDurationField = leapDurationField;
    }
    final public DurationField getDurationField() {
      return iDurationField;
    }
    final public DurationField getLeapDurationField() {
      return iLeapDurationField;
    }
    final public DurationField getRangeDurationField() {
      return iRangeDurationField;
    }
    public String getAsShortText(long instant, Locale locale) {
      checkLimits(instant, null);
      return getWrappedField().getAsShortText(instant, locale);
    }
    public String getAsText(long instant, Locale locale) {
      checkLimits(instant, null);
      return getWrappedField().getAsText(instant, locale);
    }
    public boolean isLeap(long instant) {
      checkLimits(instant, null);
      return getWrappedField().isLeap(instant);
    }
    public int get(long instant) {
      checkLimits(instant, null);
      return getWrappedField().get(instant);
    }
    public int getDifference(long minuendInstant, long subtrahendInstant) {
      checkLimits(minuendInstant, "minuend");
      checkLimits(subtrahendInstant, "subtrahend");
      return getWrappedField().getDifference(minuendInstant, subtrahendInstant);
    }
    public int getLeapAmount(long instant) {
      checkLimits(instant, null);
      return getWrappedField().getLeapAmount(instant);
    }
    public int getMaximumShortTextLength(Locale locale) {
      return getWrappedField().getMaximumShortTextLength(locale);
    }
    public int getMaximumTextLength(Locale locale) {
      return getWrappedField().getMaximumTextLength(locale);
    }
    public int getMaximumValue(long instant) {
      checkLimits(instant, null);
      return getWrappedField().getMaximumValue(instant);
    }
    public int getMinimumValue(long instant) {
      checkLimits(instant, null);
      return getWrappedField().getMinimumValue(instant);
    }
    public long add(long instant, int amount) {
      checkLimits(instant, null);
      long result = getWrappedField().add(instant, amount);
      checkLimits(result, "resulting");
      return result;
    }
    public long add(long instant, long amount) {
      checkLimits(instant, null);
      long result = getWrappedField().add(instant, amount);
      checkLimits(result, "resulting");
      return result;
    }
    public long addWrapField(long instant, int amount) {
      checkLimits(instant, null);
      long result = getWrappedField().addWrapField(instant, amount);
      checkLimits(result, "resulting");
      return result;
    }
    public long getDifferenceAsLong(long minuendInstant, long subtrahendInstant) {
      checkLimits(minuendInstant, "minuend");
      checkLimits(subtrahendInstant, "subtrahend");
      return getWrappedField().getDifferenceAsLong(minuendInstant, subtrahendInstant);
    }
    public long remainder(long instant) {
      checkLimits(instant, null);
      long result = getWrappedField().remainder(instant);
      checkLimits(result, "resulting");
      return result;
    }
    public long roundCeiling(long instant) {
      checkLimits(instant, null);
      long result = getWrappedField().roundCeiling(instant);
      checkLimits(result, "resulting");
      return result;
    }
    public long roundFloor(long instant) {
      checkLimits(instant, null);
      long result = getWrappedField().roundFloor(instant);
      checkLimits(result, "resulting");
      return result;
    }
    public long roundHalfCeiling(long instant) {
      checkLimits(instant, null);
      long result = getWrappedField().roundHalfCeiling(instant);
      checkLimits(result, "resulting");
      return result;
    }
    public long roundHalfEven(long instant) {
      checkLimits(instant, null);
      long result = getWrappedField().roundHalfEven(instant);
      checkLimits(result, "resulting");
      return result;
    }
    public long roundHalfFloor(long instant) {
      checkLimits(instant, null);
      long result = getWrappedField().roundHalfFloor(instant);
      checkLimits(result, "resulting");
      return result;
    }
    public long set(long instant, int value) {
      checkLimits(instant, null);
      long result = getWrappedField().set(instant, value);
      checkLimits(result, "resulting");
      return result;
    }
    public long set(long instant, String text, Locale locale) {
      checkLimits(instant, null);
      long result = getWrappedField().set(instant, text, locale);
      checkLimits(result, "resulting");
      return result;
    }
  }
  
  private class LimitDurationField extends DecoratedDurationField  {
    final private static long serialVersionUID = 8049297699408782284L;
    LimitDurationField(DurationField field) {
      super(field, field.getType());
    }
    public int getDifference(long minuendInstant, long subtrahendInstant) {
      checkLimits(minuendInstant, "minuend");
      checkLimits(subtrahendInstant, "subtrahend");
      return getWrappedField().getDifference(minuendInstant, subtrahendInstant);
    }
    public int getValue(long duration, long instant) {
      checkLimits(instant, null);
      return getWrappedField().getValue(duration, instant);
    }
    public long add(long instant, int amount) {
      checkLimits(instant, null);
      long result = getWrappedField().add(instant, amount);
      checkLimits(result, "resulting");
      return result;
    }
    public long add(long instant, long amount) {
      checkLimits(instant, null);
      long result = getWrappedField().add(instant, amount);
      checkLimits(result, "resulting");
      return result;
    }
    public long getDifferenceAsLong(long minuendInstant, long subtrahendInstant) {
      checkLimits(minuendInstant, "minuend");
      checkLimits(subtrahendInstant, "subtrahend");
      return getWrappedField().getDifferenceAsLong(minuendInstant, subtrahendInstant);
    }
    public long getMillis(int value, long instant) {
      checkLimits(instant, null);
      return getWrappedField().getMillis(value, instant);
    }
    public long getMillis(long value, long instant) {
      checkLimits(instant, null);
      return getWrappedField().getMillis(value, instant);
    }
    public long getValueAsLong(long duration, long instant) {
      checkLimits(instant, null);
      return getWrappedField().getValueAsLong(duration, instant);
    }
  }
  
  private class LimitException extends IllegalArgumentException  {
    final private static long serialVersionUID = -5924689995607498581L;
    final private boolean iIsLow;
    LimitException(String desc, boolean isLow) {
      super(desc);
      iIsLow = isLow;
    }
    public String getMessage() {
      StringBuffer buf = new StringBuffer(85);
      buf.append("The");
      String desc = super.getMessage();
      if(desc != null) {
        buf.append(' ');
        buf.append(desc);
      }
      buf.append(" instant is ");
      DateTimeFormatter p = ISODateTimeFormat.dateTime();
      p = p.withChronology(getBase());
      if(iIsLow) {
        buf.append("below the supported minimum of ");
        p.printTo(buf, getLowerLimit().getMillis());
      }
      else {
        buf.append("above the supported maximum of ");
        p.printTo(buf, getUpperLimit().getMillis());
      }
      buf.append(" (");
      buf.append(getBase());
      buf.append(')');
      return buf.toString();
    }
    public String toString() {
      return "IllegalArgumentException: " + getMessage();
    }
  }
}