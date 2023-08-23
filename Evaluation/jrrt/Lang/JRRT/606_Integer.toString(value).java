package org.apache.commons.lang3.time;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.text.DateFormat;
import java.text.DateFormatSymbols;
import java.text.FieldPosition;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.apache.commons.lang3.Validate;

public class FastDatePrinter implements DatePrinter, Serializable  {
  final private static long serialVersionUID = 1L;
  final public static int FULL = DateFormat.FULL;
  final public static int LONG = DateFormat.LONG;
  final public static int MEDIUM = DateFormat.MEDIUM;
  final public static int SHORT = DateFormat.SHORT;
  final private String mPattern;
  final private TimeZone mTimeZone;
  final private Locale mLocale;
  private transient Rule[] mRules;
  private transient int mMaxLengthEstimate;
  private static ConcurrentMap<TimeZoneDisplayKey, String> cTimeZoneDisplayCache = new ConcurrentHashMap<TimeZoneDisplayKey, String>(7);
  protected FastDatePrinter(final String pattern, final TimeZone timeZone, final Locale locale) {
    super();
    mPattern = pattern;
    mTimeZone = timeZone;
    mLocale = locale;
    init();
  }
  private GregorianCalendar newCalendar() {
    return new GregorianCalendar(mTimeZone, mLocale);
  }
  protected List<Rule> parsePattern() {
    final DateFormatSymbols symbols = new DateFormatSymbols(mLocale);
    final List<Rule> rules = new ArrayList<Rule>();
    final String[] ERAs = symbols.getEras();
    final String[] months = symbols.getMonths();
    final String[] shortMonths = symbols.getShortMonths();
    final String[] weekdays = symbols.getWeekdays();
    final String[] shortWeekdays = symbols.getShortWeekdays();
    final String[] AmPmStrings = symbols.getAmPmStrings();
    final int length = mPattern.length();
    final int[] indexRef = new int[1];
    for(int i = 0; i < length; i++) {
      indexRef[0] = i;
      final String token = parseToken(mPattern, indexRef);
      i = indexRef[0];
      final int tokenLen = token.length();
      if(tokenLen == 0) {
        break ;
      }
      Rule rule;
      final char c = token.charAt(0);
      switch (c){
        case 'G':
        rule = new TextField(Calendar.ERA, ERAs);
        break ;
        case 'y':
        if(tokenLen == 2) {
          rule = TwoDigitYearField.INSTANCE;
        }
        else {
          rule = selectNumberRule(Calendar.YEAR, tokenLen < 4 ? 4 : tokenLen);
        }
        break ;
        case 'M':
        if(tokenLen >= 4) {
          rule = new TextField(Calendar.MONTH, months);
        }
        else 
          if(tokenLen == 3) {
            rule = new TextField(Calendar.MONTH, shortMonths);
          }
          else 
            if(tokenLen == 2) {
              rule = TwoDigitMonthField.INSTANCE;
            }
            else {
              rule = UnpaddedMonthField.INSTANCE;
            }
        break ;
        case 'd':
        rule = selectNumberRule(Calendar.DAY_OF_MONTH, tokenLen);
        break ;
        case 'h':
        rule = new TwelveHourField(selectNumberRule(Calendar.HOUR, tokenLen));
        break ;
        case 'H':
        rule = selectNumberRule(Calendar.HOUR_OF_DAY, tokenLen);
        break ;
        case 'm':
        rule = selectNumberRule(Calendar.MINUTE, tokenLen);
        break ;
        case 's':
        rule = selectNumberRule(Calendar.SECOND, tokenLen);
        break ;
        case 'S':
        rule = selectNumberRule(Calendar.MILLISECOND, tokenLen);
        break ;
        case 'E':
        rule = new TextField(Calendar.DAY_OF_WEEK, tokenLen < 4 ? shortWeekdays : weekdays);
        break ;
        case 'D':
        rule = selectNumberRule(Calendar.DAY_OF_YEAR, tokenLen);
        break ;
        case 'F':
        rule = selectNumberRule(Calendar.DAY_OF_WEEK_IN_MONTH, tokenLen);
        break ;
        case 'w':
        rule = selectNumberRule(Calendar.WEEK_OF_YEAR, tokenLen);
        break ;
        case 'W':
        rule = selectNumberRule(Calendar.WEEK_OF_MONTH, tokenLen);
        break ;
        case 'a':
        rule = new TextField(Calendar.AM_PM, AmPmStrings);
        break ;
        case 'k':
        rule = new TwentyFourHourField(selectNumberRule(Calendar.HOUR_OF_DAY, tokenLen));
        break ;
        case 'K':
        rule = selectNumberRule(Calendar.HOUR, tokenLen);
        break ;
        case 'z':
        if(tokenLen >= 4) {
          rule = new TimeZoneNameRule(mTimeZone, mLocale, TimeZone.LONG);
        }
        else {
          rule = new TimeZoneNameRule(mTimeZone, mLocale, TimeZone.SHORT);
        }
        break ;
        case 'Z':
        if(tokenLen == 1) {
          rule = TimeZoneNumberRule.INSTANCE_NO_COLON;
        }
        else {
          rule = TimeZoneNumberRule.INSTANCE_COLON;
        }
        break ;
        case '\'':
        final String sub = token.substring(1);
        if(sub.length() == 1) {
          rule = new CharacterLiteral(sub.charAt(0));
        }
        else {
          rule = new StringLiteral(sub);
        }
        break ;
        default:
        throw new IllegalArgumentException("Illegal pattern component: " + token);
      }
      rules.add(rule);
    }
    return rules;
  }
  @Override() public Locale getLocale() {
    return mLocale;
  }
  protected NumberRule selectNumberRule(final int field, final int padding) {
    switch (padding){
      case 1:
      return new UnpaddedNumberField(field);
      case 2:
      return new TwoDigitNumberField(field);
      default:
      return new PaddedNumberField(field, padding);
    }
  }
  private String applyRulesToString(final Calendar c) {
    return applyRules(c, new StringBuffer(mMaxLengthEstimate)).toString();
  }
  @Override() public String format(final Calendar calendar) {
    return format(calendar, new StringBuffer(mMaxLengthEstimate)).toString();
  }
  @Override() public String format(final Date date) {
    final Calendar c = newCalendar();
    c.setTime(date);
    return applyRulesToString(c);
  }
  @Override() public String format(final long millis) {
    final Calendar c = newCalendar();
    c.setTimeInMillis(millis);
    return applyRulesToString(c);
  }
  @Override() public String getPattern() {
    return mPattern;
  }
  static String getTimeZoneDisplay(final TimeZone tz, final boolean daylight, final int style, final Locale locale) {
    final TimeZoneDisplayKey key = new TimeZoneDisplayKey(tz, daylight, style, locale);
    String value = cTimeZoneDisplayCache.get(key);
    if(value == null) {
      value = tz.getDisplayName(daylight, style, locale);
      final String prior = cTimeZoneDisplayCache.putIfAbsent(key, value);
      if(prior != null) {
        value = prior;
      }
    }
    return value;
  }
  protected String parseToken(final String pattern, final int[] indexRef) {
    final StringBuilder buf = new StringBuilder();
    int i = indexRef[0];
    final int length = pattern.length();
    char c = pattern.charAt(i);
    if(c >= 'A' && c <= 'Z' || c >= 'a' && c <= 'z') {
      buf.append(c);
      while(i + 1 < length){
        final char peek = pattern.charAt(i + 1);
        if(peek == c) {
          buf.append(c);
          i++;
        }
        else {
          break ;
        }
      }
    }
    else {
      buf.append('\'');
      boolean inLiteral = false;
      for(; i < length; i++) {
        c = pattern.charAt(i);
        if(c == '\'') {
          if(i + 1 < length && pattern.charAt(i + 1) == '\'') {
            i++;
            buf.append(c);
          }
          else {
            inLiteral = !inLiteral;
          }
        }
        else 
          if(!inLiteral && (c >= 'A' && c <= 'Z' || c >= 'a' && c <= 'z')) {
            i--;
            break ;
          }
          else {
            buf.append(c);
          }
      }
    }
    indexRef[0] = i;
    return buf.toString();
  }
  @Override() public String toString() {
    return "FastDatePrinter[" + mPattern + "," + mLocale + "," + mTimeZone.getID() + "]";
  }
  protected StringBuffer applyRules(final Calendar calendar, final StringBuffer buf) {
    for (final Rule rule : mRules) {
      rule.appendTo(buf, calendar);
    }
    return buf;
  }
  @Override() public StringBuffer format(final Object obj, final StringBuffer toAppendTo, final FieldPosition pos) {
    if(obj instanceof Date) {
      return format((Date)obj, toAppendTo);
    }
    else 
      if(obj instanceof Calendar) {
        return format((Calendar)obj, toAppendTo);
      }
      else 
        if(obj instanceof Long) {
          return format(((Long)obj).longValue(), toAppendTo);
        }
        else {
          throw new IllegalArgumentException("Unknown class: " + (obj == null ? "<null>" : obj.getClass().getName()));
        }
  }
  @Override() public StringBuffer format(final Calendar calendar, final StringBuffer buf) {
    return applyRules(calendar, buf);
  }
  @Override() public StringBuffer format(final Date date, final StringBuffer buf) {
    final Calendar c = newCalendar();
    c.setTime(date);
    return applyRules(c, buf);
  }
  @Override() public StringBuffer format(final long millis, final StringBuffer buf) {
    return format(new Date(millis), buf);
  }
  @Override() public TimeZone getTimeZone() {
    return mTimeZone;
  }
  @Override() public boolean equals(final Object obj) {
    if(obj instanceof FastDatePrinter == false) {
      return false;
    }
    final FastDatePrinter other = (FastDatePrinter)obj;
    return mPattern.equals(other.mPattern) && mTimeZone.equals(other.mTimeZone) && mLocale.equals(other.mLocale);
  }
  public int getMaxLengthEstimate() {
    return mMaxLengthEstimate;
  }
  @Override() public int hashCode() {
    return mPattern.hashCode() + 13 * (mTimeZone.hashCode() + 13 * mLocale.hashCode());
  }
  private void init() {
    final List<Rule> rulesList = parsePattern();
    mRules = rulesList.toArray(new Rule[rulesList.size()]);
    int len = 0;
    for(int i = mRules.length; --i >= 0; ) {
      len += mRules[i].estimateLength();
    }
    mMaxLengthEstimate = len;
  }
  private void readObject(final ObjectInputStream in) throws IOException, ClassNotFoundException {
    in.defaultReadObject();
    init();
  }
  
  private static class CharacterLiteral implements Rule  {
    final private char mValue;
    CharacterLiteral(final char value) {
      super();
      mValue = value;
    }
    @Override() public int estimateLength() {
      return 1;
    }
    @Override() public void appendTo(final StringBuffer buffer, final Calendar calendar) {
      buffer.append(mValue);
    }
  }
  
  private interface NumberRule extends Rule  {
    void appendTo(StringBuffer buffer, int value);
  }
  
  private static class PaddedNumberField implements NumberRule  {
    final private int mField;
    final private int mSize;
    PaddedNumberField(final int field, final int size) {
      super();
      if(size < 3) {
        throw new IllegalArgumentException();
      }
      mField = field;
      mSize = size;
    }
    @Override() public int estimateLength() {
      return 4;
    }
    @Override() final public void appendTo(final StringBuffer buffer, final int value) {
      if(value < 100) {
        for(int i = mSize; --i >= 2; ) {
          buffer.append('0');
        }
        buffer.append((char)(value / 10 + '0'));
        buffer.append((char)(value % 10 + '0'));
      }
      else {
        int digits;
        if(value < 1000) {
          digits = 3;
        }
        else {
          Validate.isTrue(value > -1, "Negative values should not be possible", value);
          String var_606 = Integer.toString(value);
          digits = var_606.length();
        }
        for(int i = mSize; --i >= digits; ) {
          buffer.append('0');
        }
        buffer.append(Integer.toString(value));
      }
    }
    @Override() public void appendTo(final StringBuffer buffer, final Calendar calendar) {
      appendTo(buffer, calendar.get(mField));
    }
  }
  
  private interface Rule  {
    int estimateLength();
    void appendTo(StringBuffer buffer, Calendar calendar);
  }
  
  private static class StringLiteral implements Rule  {
    final private String mValue;
    StringLiteral(final String value) {
      super();
      mValue = value;
    }
    @Override() public int estimateLength() {
      return mValue.length();
    }
    @Override() public void appendTo(final StringBuffer buffer, final Calendar calendar) {
      buffer.append(mValue);
    }
  }
  
  private static class TextField implements Rule  {
    final private int mField;
    final private String[] mValues;
    TextField(final int field, final String[] values) {
      super();
      mField = field;
      mValues = values;
    }
    @Override() public int estimateLength() {
      int max = 0;
      for(int i = mValues.length; --i >= 0; ) {
        final int len = mValues[i].length();
        if(len > max) {
          max = len;
        }
      }
      return max;
    }
    @Override() public void appendTo(final StringBuffer buffer, final Calendar calendar) {
      buffer.append(mValues[calendar.get(mField)]);
    }
  }
  
  private static class TimeZoneDisplayKey  {
    final private TimeZone mTimeZone;
    final private int mStyle;
    final private Locale mLocale;
    TimeZoneDisplayKey(final TimeZone timeZone, final boolean daylight, int style, final Locale locale) {
      super();
      mTimeZone = timeZone;
      if(daylight) {
        style |= 0x80000000;
      }
      mStyle = style;
      mLocale = locale;
    }
    @Override() public boolean equals(final Object obj) {
      if(this == obj) {
        return true;
      }
      if(obj instanceof TimeZoneDisplayKey) {
        final TimeZoneDisplayKey other = (TimeZoneDisplayKey)obj;
        return mTimeZone.equals(other.mTimeZone) && mStyle == other.mStyle && mLocale.equals(other.mLocale);
      }
      return false;
    }
    @Override() public int hashCode() {
      return (mStyle * 31 + mLocale.hashCode()) * 31 + mTimeZone.hashCode();
    }
  }
  
  private static class TimeZoneNameRule implements Rule  {
    final private Locale mLocale;
    final private int mStyle;
    final private String mStandard;
    final private String mDaylight;
    TimeZoneNameRule(final TimeZone timeZone, final Locale locale, final int style) {
      super();
      mLocale = locale;
      mStyle = style;
      mStandard = getTimeZoneDisplay(timeZone, false, style, locale);
      mDaylight = getTimeZoneDisplay(timeZone, true, style, locale);
    }
    @Override() public int estimateLength() {
      return Math.max(mStandard.length(), mDaylight.length());
    }
    @Override() public void appendTo(final StringBuffer buffer, final Calendar calendar) {
      final TimeZone zone = calendar.getTimeZone();
      if(zone.useDaylightTime() && calendar.get(Calendar.DST_OFFSET) != 0) {
        buffer.append(getTimeZoneDisplay(zone, true, mStyle, mLocale));
      }
      else {
        buffer.append(getTimeZoneDisplay(zone, false, mStyle, mLocale));
      }
    }
  }
  
  private static class TimeZoneNumberRule implements Rule  {
    final static TimeZoneNumberRule INSTANCE_COLON = new TimeZoneNumberRule(true);
    final static TimeZoneNumberRule INSTANCE_NO_COLON = new TimeZoneNumberRule(false);
    final boolean mColon;
    TimeZoneNumberRule(final boolean colon) {
      super();
      mColon = colon;
    }
    @Override() public int estimateLength() {
      return 5;
    }
    @Override() public void appendTo(final StringBuffer buffer, final Calendar calendar) {
      int offset = calendar.get(Calendar.ZONE_OFFSET) + calendar.get(Calendar.DST_OFFSET);
      if(offset < 0) {
        buffer.append('-');
        offset = -offset;
      }
      else {
        buffer.append('+');
      }
      final int hours = offset / (60 * 60 * 1000);
      buffer.append((char)(hours / 10 + '0'));
      buffer.append((char)(hours % 10 + '0'));
      if(mColon) {
        buffer.append(':');
      }
      final int minutes = offset / (60 * 1000) - 60 * hours;
      buffer.append((char)(minutes / 10 + '0'));
      buffer.append((char)(minutes % 10 + '0'));
    }
  }
  
  private static class TwelveHourField implements NumberRule  {
    final private NumberRule mRule;
    TwelveHourField(final NumberRule rule) {
      super();
      mRule = rule;
    }
    @Override() public int estimateLength() {
      return mRule.estimateLength();
    }
    @Override() public void appendTo(final StringBuffer buffer, final int value) {
      mRule.appendTo(buffer, value);
    }
    @Override() public void appendTo(final StringBuffer buffer, final Calendar calendar) {
      int value = calendar.get(Calendar.HOUR);
      if(value == 0) {
        value = calendar.getLeastMaximum(Calendar.HOUR) + 1;
      }
      mRule.appendTo(buffer, value);
    }
  }
  
  private static class TwentyFourHourField implements NumberRule  {
    final private NumberRule mRule;
    TwentyFourHourField(final NumberRule rule) {
      super();
      mRule = rule;
    }
    @Override() public int estimateLength() {
      return mRule.estimateLength();
    }
    @Override() public void appendTo(final StringBuffer buffer, final int value) {
      mRule.appendTo(buffer, value);
    }
    @Override() public void appendTo(final StringBuffer buffer, final Calendar calendar) {
      int value = calendar.get(Calendar.HOUR_OF_DAY);
      if(value == 0) {
        value = calendar.getMaximum(Calendar.HOUR_OF_DAY) + 1;
      }
      mRule.appendTo(buffer, value);
    }
  }
  
  private static class TwoDigitMonthField implements NumberRule  {
    final static TwoDigitMonthField INSTANCE = new TwoDigitMonthField();
    TwoDigitMonthField() {
      super();
    }
    @Override() public int estimateLength() {
      return 2;
    }
    @Override() final public void appendTo(final StringBuffer buffer, final int value) {
      buffer.append((char)(value / 10 + '0'));
      buffer.append((char)(value % 10 + '0'));
    }
    @Override() public void appendTo(final StringBuffer buffer, final Calendar calendar) {
      appendTo(buffer, calendar.get(Calendar.MONTH) + 1);
    }
  }
  
  private static class TwoDigitNumberField implements NumberRule  {
    final private int mField;
    TwoDigitNumberField(final int field) {
      super();
      mField = field;
    }
    @Override() public int estimateLength() {
      return 2;
    }
    @Override() final public void appendTo(final StringBuffer buffer, final int value) {
      if(value < 100) {
        buffer.append((char)(value / 10 + '0'));
        buffer.append((char)(value % 10 + '0'));
      }
      else {
        buffer.append(Integer.toString(value));
      }
    }
    @Override() public void appendTo(final StringBuffer buffer, final Calendar calendar) {
      appendTo(buffer, calendar.get(mField));
    }
  }
  
  private static class TwoDigitYearField implements NumberRule  {
    final static TwoDigitYearField INSTANCE = new TwoDigitYearField();
    TwoDigitYearField() {
      super();
    }
    @Override() public int estimateLength() {
      return 2;
    }
    @Override() final public void appendTo(final StringBuffer buffer, final int value) {
      buffer.append((char)(value / 10 + '0'));
      buffer.append((char)(value % 10 + '0'));
    }
    @Override() public void appendTo(final StringBuffer buffer, final Calendar calendar) {
      appendTo(buffer, calendar.get(Calendar.YEAR) % 100);
    }
  }
  
  private static class UnpaddedMonthField implements NumberRule  {
    final static UnpaddedMonthField INSTANCE = new UnpaddedMonthField();
    UnpaddedMonthField() {
      super();
    }
    @Override() public int estimateLength() {
      return 2;
    }
    @Override() final public void appendTo(final StringBuffer buffer, final int value) {
      if(value < 10) {
        buffer.append((char)(value + '0'));
      }
      else {
        buffer.append((char)(value / 10 + '0'));
        buffer.append((char)(value % 10 + '0'));
      }
    }
    @Override() public void appendTo(final StringBuffer buffer, final Calendar calendar) {
      appendTo(buffer, calendar.get(Calendar.MONTH) + 1);
    }
  }
  
  private static class UnpaddedNumberField implements NumberRule  {
    final private int mField;
    UnpaddedNumberField(final int field) {
      super();
      mField = field;
    }
    @Override() public int estimateLength() {
      return 4;
    }
    @Override() final public void appendTo(final StringBuffer buffer, final int value) {
      if(value < 10) {
        buffer.append((char)(value + '0'));
      }
      else 
        if(value < 100) {
          buffer.append((char)(value / 10 + '0'));
          buffer.append((char)(value % 10 + '0'));
        }
        else {
          buffer.append(Integer.toString(value));
        }
    }
    @Override() public void appendTo(final StringBuffer buffer, final Calendar calendar) {
      appendTo(buffer, calendar.get(mField));
    }
  }
}