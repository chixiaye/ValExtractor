package org.joda.time.chrono;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import org.joda.time.Chronology;
import org.joda.time.DateTimeFieldType;
import org.joda.time.DateTimeZone;
import org.joda.time.field.DividedDateTimeField;
import org.joda.time.field.RemainderDateTimeField;

final public class ISOChronology extends AssembledChronology  {
  final private static long serialVersionUID = -6212696554273812441L;
  final private static ISOChronology INSTANCE_UTC;
  final private static int FAST_CACHE_SIZE = 64;
  final private static ISOChronology[] cFastCache;
  final private static Map<DateTimeZone, ISOChronology> cCache = new HashMap<DateTimeZone, ISOChronology>();
  static {
    cFastCache = new ISOChronology[FAST_CACHE_SIZE];
    INSTANCE_UTC = new ISOChronology(GregorianChronology.getInstanceUTC());
    cCache.put(DateTimeZone.UTC, INSTANCE_UTC);
  }
  private ISOChronology(Chronology base) {
    super(base, null);
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
  public static ISOChronology getInstance() {
    return getInstance(DateTimeZone.getDefault());
  }
  public static ISOChronology getInstance(DateTimeZone zone) {
    if(zone == null) {
      zone = DateTimeZone.getDefault();
    }
    int index = System.identityHashCode(zone) & (FAST_CACHE_SIZE - 1);
    ISOChronology var_355 = cFastCache[index];
    ISOChronology chrono = var_355;
    if(chrono != null && chrono.getZone() == zone) {
      return chrono;
    }
    synchronized(cCache) {
      chrono = cCache.get(zone);
      if(chrono == null) {
        chrono = new ISOChronology(ZonedChronology.getInstance(INSTANCE_UTC, zone));
        cCache.put(zone, chrono);
      }
    }
    cFastCache[index] = chrono;
    return chrono;
  }
  public static ISOChronology getInstanceUTC() {
    return INSTANCE_UTC;
  }
  private Object writeReplace() {
    return new Stub(getZone());
  }
  public String toString() {
    String str = "ISOChronology";
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
    if(obj instanceof ISOChronology) {
      ISOChronology chrono = (ISOChronology)obj;
      return getZone().equals(chrono.getZone());
    }
    return false;
  }
  public int hashCode() {
    return "ISO".hashCode() * 11 + getZone().hashCode();
  }
  protected void assemble(Fields fields) {
    if(getBase().getZone() == DateTimeZone.UTC) {
      fields.centuryOfEra = new DividedDateTimeField(ISOYearOfEraDateTimeField.INSTANCE, DateTimeFieldType.centuryOfEra(), 100);
      fields.centuries = fields.centuryOfEra.getDurationField();
      fields.yearOfCentury = new RemainderDateTimeField((DividedDateTimeField)fields.centuryOfEra, DateTimeFieldType.yearOfCentury());
      fields.weekyearOfCentury = new RemainderDateTimeField((DividedDateTimeField)fields.centuryOfEra, fields.weekyears, DateTimeFieldType.weekyearOfCentury());
    }
  }
  
  final private static class Stub implements Serializable  {
    final private static long serialVersionUID = -6212696554273812441L;
    private transient DateTimeZone iZone;
    Stub(DateTimeZone zone) {
      super();
      iZone = zone;
    }
    private Object readResolve() {
      return ISOChronology.getInstance(iZone);
    }
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
      iZone = (DateTimeZone)in.readObject();
    }
    private void writeObject(ObjectOutputStream out) throws IOException {
      out.writeObject(iZone);
    }
  }
}