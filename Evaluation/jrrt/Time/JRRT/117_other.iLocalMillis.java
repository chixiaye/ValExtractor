package org.joda.time;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import org.joda.convert.FromString;
import org.joda.convert.ToString;
import org.joda.time.base.BaseLocal;
import org.joda.time.chrono.ISOChronology;
import org.joda.time.convert.ConverterManager;
import org.joda.time.convert.PartialConverter;
import org.joda.time.field.AbstractReadableInstantFieldProperty;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

final public class LocalTime extends BaseLocal implements ReadablePartial, Serializable  {
  final private static long serialVersionUID = -12873158713873L;
  final public static LocalTime MIDNIGHT = new LocalTime(0, 0, 0, 0);
  final private static int HOUR_OF_DAY = 0;
  final private static int MINUTE_OF_HOUR = 1;
  final private static int SECOND_OF_MINUTE = 2;
  final private static int MILLIS_OF_SECOND = 3;
  final private static Set<DurationFieldType> TIME_DURATION_TYPES = new HashSet<DurationFieldType>();
  static {
    TIME_DURATION_TYPES.add(DurationFieldType.millis());
    TIME_DURATION_TYPES.add(DurationFieldType.seconds());
    TIME_DURATION_TYPES.add(DurationFieldType.minutes());
    TIME_DURATION_TYPES.add(DurationFieldType.hours());
  }
  final private long iLocalMillis;
  final private Chronology iChronology;
  public LocalTime() {
    this(DateTimeUtils.currentTimeMillis(), ISOChronology.getInstance());
  }
  public LocalTime(Chronology chronology) {
    this(DateTimeUtils.currentTimeMillis(), chronology);
  }
  public LocalTime(DateTimeZone zone) {
    this(DateTimeUtils.currentTimeMillis(), ISOChronology.getInstance(zone));
  }
  public LocalTime(Object instant) {
    this(instant, (Chronology)null);
  }
  public LocalTime(Object instant, Chronology chronology) {
    super();
    PartialConverter converter = ConverterManager.getInstance().getPartialConverter(instant);
    chronology = converter.getChronology(instant, chronology);
    chronology = DateTimeUtils.getChronology(chronology);
    iChronology = chronology.withUTC();
    int[] values = converter.getPartialValues(this, instant, chronology, ISODateTimeFormat.localTimeParser());
    iLocalMillis = iChronology.getDateTimeMillis(0L, values[0], values[1], values[2], values[3]);
  }
  public LocalTime(Object instant, DateTimeZone zone) {
    super();
    PartialConverter converter = ConverterManager.getInstance().getPartialConverter(instant);
    Chronology chronology = converter.getChronology(instant, zone);
    chronology = DateTimeUtils.getChronology(chronology);
    iChronology = chronology.withUTC();
    int[] values = converter.getPartialValues(this, instant, chronology, ISODateTimeFormat.localTimeParser());
    iLocalMillis = iChronology.getDateTimeMillis(0L, values[0], values[1], values[2], values[3]);
  }
  public LocalTime(int hourOfDay, int minuteOfHour) {
    this(hourOfDay, minuteOfHour, 0, 0, ISOChronology.getInstanceUTC());
  }
  public LocalTime(int hourOfDay, int minuteOfHour, int secondOfMinute) {
    this(hourOfDay, minuteOfHour, secondOfMinute, 0, ISOChronology.getInstanceUTC());
  }
  public LocalTime(int hourOfDay, int minuteOfHour, int secondOfMinute, int millisOfSecond) {
    this(hourOfDay, minuteOfHour, secondOfMinute, millisOfSecond, ISOChronology.getInstanceUTC());
  }
  public LocalTime(int hourOfDay, int minuteOfHour, int secondOfMinute, int millisOfSecond, Chronology chronology) {
    super();
    chronology = DateTimeUtils.getChronology(chronology).withUTC();
    long instant = chronology.getDateTimeMillis(0L, hourOfDay, minuteOfHour, secondOfMinute, millisOfSecond);
    iChronology = chronology;
    iLocalMillis = instant;
  }
  public LocalTime(long instant) {
    this(instant, ISOChronology.getInstance());
  }
  public LocalTime(long instant, Chronology chronology) {
    super();
    chronology = DateTimeUtils.getChronology(chronology);
    long localMillis = chronology.getZone().getMillisKeepLocal(DateTimeZone.UTC, instant);
    chronology = chronology.withUTC();
    iLocalMillis = chronology.millisOfDay().get(localMillis);
    iChronology = chronology;
  }
  public LocalTime(long instant, DateTimeZone zone) {
    this(instant, ISOChronology.getInstance(zone));
  }
  public Chronology getChronology() {
    return iChronology;
  }
  public DateTime toDateTimeToday() {
    return toDateTimeToday(null);
  }
  public DateTime toDateTimeToday(DateTimeZone zone) {
    Chronology chrono = getChronology().withZone(zone);
    long instantMillis = DateTimeUtils.currentTimeMillis();
    long resolved = chrono.set(this, instantMillis);
    return new DateTime(resolved, chrono);
  }
  protected DateTimeField getField(int index, Chronology chrono) {
    switch (index){
      case HOUR_OF_DAY:
      return chrono.hourOfDay();
      case MINUTE_OF_HOUR:
      return chrono.minuteOfHour();
      case SECOND_OF_MINUTE:
      return chrono.secondOfMinute();
      case MILLIS_OF_SECOND:
      return chrono.millisOfSecond();
      default:
      throw new IndexOutOfBoundsException("Invalid index: " + index);
    }
  }
  public static LocalTime fromCalendarFields(Calendar calendar) {
    if(calendar == null) {
      throw new IllegalArgumentException("The calendar must not be null");
    }
    return new LocalTime(calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), calendar.get(Calendar.SECOND), calendar.get(Calendar.MILLISECOND));
  }
  @SuppressWarnings(value = {"deprecation", }) public static LocalTime fromDateFields(Date date) {
    if(date == null) {
      throw new IllegalArgumentException("The date must not be null");
    }
    return new LocalTime(date.getHours(), date.getMinutes(), date.getSeconds(), (((int)(date.getTime() % 1000)) + 1000) % 1000);
  }
  public static LocalTime fromMillisOfDay(long millisOfDay) {
    return fromMillisOfDay(millisOfDay, null);
  }
  public static LocalTime fromMillisOfDay(long millisOfDay, Chronology chrono) {
    chrono = DateTimeUtils.getChronology(chrono).withUTC();
    return new LocalTime(millisOfDay, chrono);
  }
  public LocalTime minus(ReadablePeriod period) {
    return withPeriodAdded(period, -1);
  }
  public LocalTime minusHours(int hours) {
    if(hours == 0) {
      return this;
    }
    long instant = getChronology().hours().subtract(getLocalMillis(), hours);
    return withLocalMillis(instant);
  }
  public LocalTime minusMillis(int millis) {
    if(millis == 0) {
      return this;
    }
    long instant = getChronology().millis().subtract(getLocalMillis(), millis);
    return withLocalMillis(instant);
  }
  public LocalTime minusMinutes(int minutes) {
    if(minutes == 0) {
      return this;
    }
    long instant = getChronology().minutes().subtract(getLocalMillis(), minutes);
    return withLocalMillis(instant);
  }
  public LocalTime minusSeconds(int seconds) {
    if(seconds == 0) {
      return this;
    }
    long instant = getChronology().seconds().subtract(getLocalMillis(), seconds);
    return withLocalMillis(instant);
  }
  public static LocalTime now() {
    return new LocalTime();
  }
  public static LocalTime now(Chronology chronology) {
    if(chronology == null) {
      throw new NullPointerException("Chronology must not be null");
    }
    return new LocalTime(chronology);
  }
  public static LocalTime now(DateTimeZone zone) {
    if(zone == null) {
      throw new NullPointerException("Zone must not be null");
    }
    return new LocalTime(zone);
  }
  @FromString() public static LocalTime parse(String str) {
    return parse(str, ISODateTimeFormat.localTimeParser());
  }
  public static LocalTime parse(String str, DateTimeFormatter formatter) {
    return formatter.parseLocalTime(str);
  }
  public LocalTime plus(ReadablePeriod period) {
    return withPeriodAdded(period, 1);
  }
  public LocalTime plusHours(int hours) {
    if(hours == 0) {
      return this;
    }
    long instant = getChronology().hours().add(getLocalMillis(), hours);
    return withLocalMillis(instant);
  }
  public LocalTime plusMillis(int millis) {
    if(millis == 0) {
      return this;
    }
    long instant = getChronology().millis().add(getLocalMillis(), millis);
    return withLocalMillis(instant);
  }
  public LocalTime plusMinutes(int minutes) {
    if(minutes == 0) {
      return this;
    }
    long instant = getChronology().minutes().add(getLocalMillis(), minutes);
    return withLocalMillis(instant);
  }
  public LocalTime plusSeconds(int seconds) {
    if(seconds == 0) {
      return this;
    }
    long instant = getChronology().seconds().add(getLocalMillis(), seconds);
    return withLocalMillis(instant);
  }
  public LocalTime withField(DateTimeFieldType fieldType, int value) {
    if(fieldType == null) {
      throw new IllegalArgumentException("Field must not be null");
    }
    if(isSupported(fieldType) == false) {
      throw new IllegalArgumentException("Field \'" + fieldType + "\' is not supported");
    }
    long instant = fieldType.getField(getChronology()).set(getLocalMillis(), value);
    return withLocalMillis(instant);
  }
  public LocalTime withFieldAdded(DurationFieldType fieldType, int amount) {
    if(fieldType == null) {
      throw new IllegalArgumentException("Field must not be null");
    }
    if(isSupported(fieldType) == false) {
      throw new IllegalArgumentException("Field \'" + fieldType + "\' is not supported");
    }
    if(amount == 0) {
      return this;
    }
    long instant = fieldType.getField(getChronology()).add(getLocalMillis(), amount);
    return withLocalMillis(instant);
  }
  public LocalTime withFields(ReadablePartial partial) {
    if(partial == null) {
      return this;
    }
    return withLocalMillis(getChronology().set(partial, getLocalMillis()));
  }
  public LocalTime withHourOfDay(int hour) {
    return withLocalMillis(getChronology().hourOfDay().set(getLocalMillis(), hour));
  }
  LocalTime withLocalMillis(long newMillis) {
    return (newMillis == getLocalMillis() ? this : new LocalTime(newMillis, getChronology()));
  }
  public LocalTime withMillisOfDay(int millis) {
    return withLocalMillis(getChronology().millisOfDay().set(getLocalMillis(), millis));
  }
  public LocalTime withMillisOfSecond(int millis) {
    return withLocalMillis(getChronology().millisOfSecond().set(getLocalMillis(), millis));
  }
  public LocalTime withMinuteOfHour(int minute) {
    return withLocalMillis(getChronology().minuteOfHour().set(getLocalMillis(), minute));
  }
  public LocalTime withPeriodAdded(ReadablePeriod period, int scalar) {
    if(period == null || scalar == 0) {
      return this;
    }
    long instant = getChronology().add(period, getLocalMillis(), scalar);
    return withLocalMillis(instant);
  }
  public LocalTime withSecondOfMinute(int second) {
    return withLocalMillis(getChronology().secondOfMinute().set(getLocalMillis(), second));
  }
  private Object readResolve() {
    if(iChronology == null) {
      return new LocalTime(iLocalMillis, ISOChronology.getInstanceUTC());
    }
    if(DateTimeZone.UTC.equals(iChronology.getZone()) == false) {
      return new LocalTime(iLocalMillis, iChronology.withUTC());
    }
    return this;
  }
  public Property hourOfDay() {
    return new Property(this, getChronology().hourOfDay());
  }
  public Property millisOfDay() {
    return new Property(this, getChronology().millisOfDay());
  }
  public Property millisOfSecond() {
    return new Property(this, getChronology().millisOfSecond());
  }
  public Property minuteOfHour() {
    return new Property(this, getChronology().minuteOfHour());
  }
  public Property property(DateTimeFieldType fieldType) {
    if(fieldType == null) {
      throw new IllegalArgumentException("The DateTimeFieldType must not be null");
    }
    if(isSupported(fieldType) == false) {
      throw new IllegalArgumentException("Field \'" + fieldType + "\' is not supported");
    }
    return new Property(this, fieldType.getField(getChronology()));
  }
  public Property secondOfMinute() {
    return new Property(this, getChronology().secondOfMinute());
  }
  @ToString() public String toString() {
    return ISODateTimeFormat.time().print(this);
  }
  public String toString(String pattern) {
    if(pattern == null) {
      return toString();
    }
    return DateTimeFormat.forPattern(pattern).print(this);
  }
  public String toString(String pattern, Locale locale) throws IllegalArgumentException {
    if(pattern == null) {
      return toString();
    }
    return DateTimeFormat.forPattern(pattern).withLocale(locale).print(this);
  }
  public boolean equals(Object partial) {
    if(this == partial) {
      return true;
    }
    if(partial instanceof LocalTime) {
      LocalTime other = (LocalTime)partial;
      if(iChronology.equals(other.iChronology)) {
        return iLocalMillis == other.iLocalMillis;
      }
    }
    return super.equals(partial);
  }
  public boolean isSupported(DateTimeFieldType type) {
    if(type == null) {
      return false;
    }
    if(isSupported(type.getDurationType()) == false) {
      return false;
    }
    DurationFieldType range = type.getRangeDurationType();
    return (isSupported(range) || range == DurationFieldType.days());
  }
  public boolean isSupported(DurationFieldType type) {
    if(type == null) {
      return false;
    }
    DurationField field = type.getField(getChronology());
    if(TIME_DURATION_TYPES.contains(type) || field.getUnitMillis() < getChronology().days().getUnitMillis()) {
      return field.isSupported();
    }
    return false;
  }
  public int compareTo(ReadablePartial partial) {
    if(this == partial) {
      return 0;
    }
    if(partial instanceof LocalTime) {
      LocalTime other = (LocalTime)partial;
      if(iChronology.equals(other.iChronology)) {
        long var_117 = other.iLocalMillis;
        return (iLocalMillis < var_117 ? -1 : (iLocalMillis == other.iLocalMillis ? 0 : 1));
      }
    }
    return super.compareTo(partial);
  }
  public int get(DateTimeFieldType fieldType) {
    if(fieldType == null) {
      throw new IllegalArgumentException("The DateTimeFieldType must not be null");
    }
    if(isSupported(fieldType) == false) {
      throw new IllegalArgumentException("Field \'" + fieldType + "\' is not supported");
    }
    return fieldType.getField(getChronology()).get(getLocalMillis());
  }
  public int getHourOfDay() {
    return getChronology().hourOfDay().get(getLocalMillis());
  }
  public int getMillisOfDay() {
    return getChronology().millisOfDay().get(getLocalMillis());
  }
  public int getMillisOfSecond() {
    return getChronology().millisOfSecond().get(getLocalMillis());
  }
  public int getMinuteOfHour() {
    return getChronology().minuteOfHour().get(getLocalMillis());
  }
  public int getSecondOfMinute() {
    return getChronology().secondOfMinute().get(getLocalMillis());
  }
  public int getValue(int index) {
    switch (index){
      case HOUR_OF_DAY:
      return getChronology().hourOfDay().get(getLocalMillis());
      case MINUTE_OF_HOUR:
      return getChronology().minuteOfHour().get(getLocalMillis());
      case SECOND_OF_MINUTE:
      return getChronology().secondOfMinute().get(getLocalMillis());
      case MILLIS_OF_SECOND:
      return getChronology().millisOfSecond().get(getLocalMillis());
      default:
      throw new IndexOutOfBoundsException("Invalid index: " + index);
    }
  }
  public int size() {
    return 4;
  }
  protected long getLocalMillis() {
    return iLocalMillis;
  }
  
  final public static class Property extends AbstractReadableInstantFieldProperty  {
    final private static long serialVersionUID = -325842547277223L;
    private transient LocalTime iInstant;
    private transient DateTimeField iField;
    Property(LocalTime instant, DateTimeField field) {
      super();
      iInstant = instant;
      iField = field;
    }
    protected Chronology getChronology() {
      return iInstant.getChronology();
    }
    public DateTimeField getField() {
      return iField;
    }
    public LocalTime addCopy(int value) {
      return iInstant.withLocalMillis(iField.add(iInstant.getLocalMillis(), value));
    }
    public LocalTime addCopy(long value) {
      return iInstant.withLocalMillis(iField.add(iInstant.getLocalMillis(), value));
    }
    public LocalTime addNoWrapToCopy(int value) {
      long millis = iField.add(iInstant.getLocalMillis(), value);
      long rounded = iInstant.getChronology().millisOfDay().get(millis);
      if(rounded != millis) {
        throw new IllegalArgumentException("The addition exceeded the boundaries of LocalTime");
      }
      return iInstant.withLocalMillis(millis);
    }
    public LocalTime addWrapFieldToCopy(int value) {
      return iInstant.withLocalMillis(iField.addWrapField(iInstant.getLocalMillis(), value));
    }
    public LocalTime getLocalTime() {
      return iInstant;
    }
    public LocalTime roundCeilingCopy() {
      return iInstant.withLocalMillis(iField.roundCeiling(iInstant.getLocalMillis()));
    }
    public LocalTime roundFloorCopy() {
      return iInstant.withLocalMillis(iField.roundFloor(iInstant.getLocalMillis()));
    }
    public LocalTime roundHalfCeilingCopy() {
      return iInstant.withLocalMillis(iField.roundHalfCeiling(iInstant.getLocalMillis()));
    }
    public LocalTime roundHalfEvenCopy() {
      return iInstant.withLocalMillis(iField.roundHalfEven(iInstant.getLocalMillis()));
    }
    public LocalTime roundHalfFloorCopy() {
      return iInstant.withLocalMillis(iField.roundHalfFloor(iInstant.getLocalMillis()));
    }
    public LocalTime setCopy(int value) {
      return iInstant.withLocalMillis(iField.set(iInstant.getLocalMillis(), value));
    }
    public LocalTime setCopy(String text) {
      return setCopy(text, null);
    }
    public LocalTime setCopy(String text, Locale locale) {
      return iInstant.withLocalMillis(iField.set(iInstant.getLocalMillis(), text, locale));
    }
    public LocalTime withMaximumValue() {
      return setCopy(getMaximumValue());
    }
    public LocalTime withMinimumValue() {
      return setCopy(getMinimumValue());
    }
    protected long getMillis() {
      return iInstant.getLocalMillis();
    }
    private void readObject(ObjectInputStream oos) throws IOException, ClassNotFoundException {
      iInstant = (LocalTime)oos.readObject();
      DateTimeFieldType type = (DateTimeFieldType)oos.readObject();
      iField = type.getField(iInstant.getChronology());
    }
    private void writeObject(ObjectOutputStream oos) throws IOException {
      oos.writeObject(iInstant);
      oos.writeObject(iField.getType());
    }
  }
}