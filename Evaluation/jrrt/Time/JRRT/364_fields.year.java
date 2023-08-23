package org.joda.time.chrono;
import java.util.HashMap;
import java.util.Map;
import org.joda.time.Chronology;
import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;
import org.joda.time.DateTimeField;
import org.joda.time.DateTimeFieldType;
import org.joda.time.DateTimeZone;
import org.joda.time.DurationFieldType;
import org.joda.time.field.DelegatedDateTimeField;
import org.joda.time.field.DividedDateTimeField;
import org.joda.time.field.OffsetDateTimeField;
import org.joda.time.field.RemainderDateTimeField;
import org.joda.time.field.SkipUndoDateTimeField;
import org.joda.time.field.UnsupportedDurationField;

final public class BuddhistChronology extends AssembledChronology  {
  final private static long serialVersionUID = -3474595157769370126L;
  final public static int BE = DateTimeConstants.CE;
  final private static DateTimeField ERA_FIELD = new BasicSingleEraDateTimeField("BE");
  final private static int BUDDHIST_OFFSET = 543;
  final private static Map<DateTimeZone, BuddhistChronology> cCache = new HashMap<DateTimeZone, BuddhistChronology>();
  final private static BuddhistChronology INSTANCE_UTC = getInstance(DateTimeZone.UTC);
  private BuddhistChronology(Chronology base, Object param) {
    super(base, param);
  }
  public static BuddhistChronology getInstance() {
    return getInstance(DateTimeZone.getDefault());
  }
  public static synchronized BuddhistChronology getInstance(DateTimeZone zone) {
    if(zone == null) {
      zone = DateTimeZone.getDefault();
    }
    BuddhistChronology chrono;
    synchronized(cCache) {
      chrono = cCache.get(zone);
      if(chrono == null) {
        chrono = new BuddhistChronology(GJChronology.getInstance(zone, null), null);
        DateTime lowerLimit = new DateTime(1, 1, 1, 0, 0, 0, 0, chrono);
        chrono = new BuddhistChronology(LimitChronology.getInstance(chrono, lowerLimit, null), "");
        cCache.put(zone, chrono);
      }
    }
    return chrono;
  }
  public static BuddhistChronology getInstanceUTC() {
    return INSTANCE_UTC;
  }
  public Chronology withUTC() {
    return INSTANCE_UTC;
  }
  public Chronology withZone(DateTimeZone zone) {
    if(zone == null) {
      zone = DateTimeZone.getDefault();
    }
    if(zone == getZone()) {
      return this;
    }
    return getInstance(zone);
  }
  private Object readResolve() {
    Chronology base = getBase();
    return base == null ? getInstanceUTC() : getInstance(base.getZone());
  }
  public String toString() {
    String str = "BuddhistChronology";
    DateTimeZone zone = getZone();
    if(zone != null) {
      str = str + '[' + zone.getID() + ']';
    }
    return str;
  }
  public boolean equals(Object obj) {
    if(this == obj) {
      return true;
    }
    if(obj instanceof BuddhistChronology) {
      BuddhistChronology chrono = (BuddhistChronology)obj;
      return getZone().equals(chrono.getZone());
    }
    return false;
  }
  public int hashCode() {
    return "Buddhist".hashCode() * 11 + getZone().hashCode();
  }
  protected void assemble(Fields fields) {
    if(getParam() == null) {
      fields.eras = UnsupportedDurationField.getInstance(DurationFieldType.eras());
      DateTimeField var_364 = fields.year;
      DateTimeField field = var_364;
      fields.year = new OffsetDateTimeField(new SkipUndoDateTimeField(this, field), BUDDHIST_OFFSET);
      field = fields.yearOfEra;
      fields.yearOfEra = new DelegatedDateTimeField(fields.year, fields.eras, DateTimeFieldType.yearOfEra());
      field = fields.weekyear;
      fields.weekyear = new OffsetDateTimeField(new SkipUndoDateTimeField(this, field), BUDDHIST_OFFSET);
      field = new OffsetDateTimeField(fields.yearOfEra, 99);
      fields.centuryOfEra = new DividedDateTimeField(field, fields.eras, DateTimeFieldType.centuryOfEra(), 100);
      fields.centuries = fields.centuryOfEra.getDurationField();
      field = new RemainderDateTimeField((DividedDateTimeField)fields.centuryOfEra);
      fields.yearOfCentury = new OffsetDateTimeField(field, DateTimeFieldType.yearOfCentury(), 1);
      field = new RemainderDateTimeField(fields.weekyear, fields.centuries, DateTimeFieldType.weekyearOfCentury(), 100);
      fields.weekyearOfCentury = new OffsetDateTimeField(field, DateTimeFieldType.weekyearOfCentury(), 1);
      fields.era = ERA_FIELD;
    }
  }
}