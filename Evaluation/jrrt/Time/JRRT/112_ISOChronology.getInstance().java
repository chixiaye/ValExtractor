package org.joda.time;
import java.lang.reflect.Method;
import java.text.DateFormatSymbols;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import org.joda.time.chrono.ISOChronology;

public class DateTimeUtils  {
  final private static SystemMillisProvider SYSTEM_MILLIS_PROVIDER = new SystemMillisProvider();
  private static volatile MillisProvider cMillisProvider = SYSTEM_MILLIS_PROVIDER;
  private static volatile Map<String, DateTimeZone> cZoneNames;
  static {
    Map<String, DateTimeZone> map = new LinkedHashMap<String, DateTimeZone>();
    map.put("UT", DateTimeZone.UTC);
    map.put("UTC", DateTimeZone.UTC);
    map.put("GMT", DateTimeZone.UTC);
    put(map, "EST", "America/New_York");
    put(map, "EDT", "America/New_York");
    put(map, "CST", "America/Chicago");
    put(map, "CDT", "America/Chicago");
    put(map, "MST", "America/Denver");
    put(map, "MDT", "America/Denver");
    put(map, "PST", "America/Los_Angeles");
    put(map, "PDT", "America/Los_Angeles");
    cZoneNames = Collections.unmodifiableMap(map);
  }
  protected DateTimeUtils() {
    super();
  }
  final public static Chronology getChronology(Chronology chrono) {
    if(chrono == null) {
      return ISOChronology.getInstance();
    }
    return chrono;
  }
  final public static Chronology getInstantChronology(ReadableInstant instant) {
    if(instant == null) {
      return ISOChronology.getInstance();
    }
    Chronology chrono = instant.getChronology();
    if(chrono == null) {
      return ISOChronology.getInstance();
    }
    return chrono;
  }
  final public static Chronology getIntervalChronology(ReadableInstant start, ReadableInstant end) {
    Chronology chrono = null;
    if(start != null) {
      chrono = start.getChronology();
    }
    else 
      if(end != null) {
        chrono = end.getChronology();
      }
    if(chrono == null) {
      chrono = ISOChronology.getInstance();
    }
    return chrono;
  }
  final public static Chronology getIntervalChronology(ReadableInterval interval) {
    if(interval == null) {
      ISOChronology var_112 = ISOChronology.getInstance();
      return var_112;
    }
    Chronology chrono = interval.getChronology();
    if(chrono == null) {
      return ISOChronology.getInstance();
    }
    return chrono;
  }
  final public static DateFormatSymbols getDateFormatSymbols(Locale locale) {
    try {
      Method method = DateFormatSymbols.class.getMethod("getInstance", new Class[]{ Locale.class } );
      return (DateFormatSymbols)method.invoke(null, new Object[]{ locale } );
    }
    catch (Exception ex) {
      return new DateFormatSymbols(locale);
    }
  }
  final public static DateTimeZone getZone(DateTimeZone zone) {
    if(zone == null) {
      return DateTimeZone.getDefault();
    }
    return zone;
  }
  final public static Map<String, DateTimeZone> getDefaultTimeZoneNames() {
    return cZoneNames;
  }
  final public static PeriodType getPeriodType(PeriodType type) {
    if(type == null) {
      return PeriodType.standard();
    }
    return type;
  }
  final public static ReadableInterval getReadableInterval(ReadableInterval interval) {
    if(interval == null) {
      long now = DateTimeUtils.currentTimeMillis();
      interval = new Interval(now, now);
    }
    return interval;
  }
  final public static boolean isContiguous(ReadablePartial partial) {
    if(partial == null) {
      throw new IllegalArgumentException("Partial must not be null");
    }
    DurationFieldType lastType = null;
    for(int i = 0; i < partial.size(); i++) {
      DateTimeField loopField = partial.getField(i);
      if(i > 0) {
        if(loopField.getRangeDurationField() == null || loopField.getRangeDurationField().getType() != lastType) {
          return false;
        }
      }
      lastType = loopField.getDurationField().getType();
    }
    return true;
  }
  final public static double toJulianDay(long epochMillis) {
    double epochDay = epochMillis / 86400000D;
    return epochDay + 2440587.5D;
  }
  final public static long currentTimeMillis() {
    return cMillisProvider.getMillis();
  }
  final public static long fromJulianDay(double julianDay) {
    double epochDay = julianDay - 2440587.5D;
    return (long)(epochDay * 86400000D);
  }
  final public static long getDurationMillis(ReadableDuration duration) {
    if(duration == null) {
      return 0L;
    }
    return duration.getMillis();
  }
  final public static long getInstantMillis(ReadableInstant instant) {
    if(instant == null) {
      return DateTimeUtils.currentTimeMillis();
    }
    return instant.getMillis();
  }
  final public static long toJulianDayNumber(long epochMillis) {
    return (long)Math.floor(toJulianDay(epochMillis) + 0.5D);
  }
  private static void checkPermission() throws SecurityException {
    SecurityManager sm = System.getSecurityManager();
    if(sm != null) {
      sm.checkPermission(new JodaTimePermission("CurrentTime.setProvider"));
    }
  }
  private static void put(Map<String, DateTimeZone> map, String name, String id) {
    try {
      map.put(name, DateTimeZone.forID(id));
    }
    catch (RuntimeException ex) {
    }
  }
  final public static void setCurrentMillisFixed(long fixedMillis) throws SecurityException {
    checkPermission();
    cMillisProvider = new FixedMillisProvider(fixedMillis);
  }
  final public static void setCurrentMillisOffset(long offsetMillis) throws SecurityException {
    checkPermission();
    if(offsetMillis == 0) {
      cMillisProvider = SYSTEM_MILLIS_PROVIDER;
    }
    else {
      cMillisProvider = new OffsetMillisProvider(offsetMillis);
    }
  }
  final public static void setCurrentMillisProvider(MillisProvider millisProvider) throws SecurityException {
    if(millisProvider == null) {
      throw new IllegalArgumentException("The MillisProvider must not be null");
    }
    checkPermission();
    cMillisProvider = millisProvider;
  }
  final public static void setCurrentMillisSystem() throws SecurityException {
    checkPermission();
    cMillisProvider = SYSTEM_MILLIS_PROVIDER;
  }
  final public static void setDefaultTimeZoneNames(Map<String, DateTimeZone> names) {
    cZoneNames = Collections.unmodifiableMap(new HashMap<String, DateTimeZone>(names));
  }
  
  static class FixedMillisProvider implements MillisProvider  {
    final private long iMillis;
    FixedMillisProvider(long fixedMillis) {
      super();
      iMillis = fixedMillis;
    }
    public long getMillis() {
      return iMillis;
    }
  }
  
  public static interface MillisProvider  {
    long getMillis();
  }
  
  static class OffsetMillisProvider implements MillisProvider  {
    final private long iMillis;
    OffsetMillisProvider(long offsetMillis) {
      super();
      iMillis = offsetMillis;
    }
    public long getMillis() {
      return System.currentTimeMillis() + iMillis;
    }
  }
  
  static class SystemMillisProvider implements MillisProvider  {
    public long getMillis() {
      return System.currentTimeMillis();
    }
  }
}