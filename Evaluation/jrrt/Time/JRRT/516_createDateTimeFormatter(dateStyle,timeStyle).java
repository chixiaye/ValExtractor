package org.joda.time.format;
import java.io.IOException;
import java.io.Writer;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import org.joda.time.Chronology;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.ReadablePartial;

public class DateTimeFormat  {
  final static int FULL = 0;
  final static int LONG = 1;
  final static int MEDIUM = 2;
  final static int SHORT = 3;
  final static int NONE = 4;
  final static int DATE = 0;
  final static int TIME = 1;
  final static int DATETIME = 2;
  final private static int PATTERN_CACHE_SIZE = 500;
  final private static Map<String, DateTimeFormatter> PATTERN_CACHE = new LinkedHashMap<String, DateTimeFormatter>(7) {
      final private static long serialVersionUID = 23L;
      @Override() protected boolean removeEldestEntry(final Map.Entry<String, DateTimeFormatter> eldest) {
        return size() > PATTERN_CACHE_SIZE;
      }
  };
  final private static DateTimeFormatter[] STYLE_CACHE = new DateTimeFormatter[25];
  protected DateTimeFormat() {
    super();
  }
  private static DateTimeFormatter createDateTimeFormatter(int dateStyle, int timeStyle) {
    int type = DATETIME;
    if(dateStyle == NONE) {
      type = TIME;
    }
    else 
      if(timeStyle == NONE) {
        type = DATE;
      }
    StyleFormatter llf = new StyleFormatter(dateStyle, timeStyle, type);
    return new DateTimeFormatter(llf, llf);
  }
  private static DateTimeFormatter createFormatterForPattern(String pattern) {
    if(pattern == null || pattern.length() == 0) {
      throw new IllegalArgumentException("Invalid pattern specification");
    }
    DateTimeFormatter formatter = null;
    synchronized(PATTERN_CACHE) {
      formatter = PATTERN_CACHE.get(pattern);
      if(formatter == null) {
        DateTimeFormatterBuilder builder = new DateTimeFormatterBuilder();
        parsePatternTo(builder, pattern);
        formatter = builder.toFormatter();
        PATTERN_CACHE.put(pattern, formatter);
      }
    }
    return formatter;
  }
  private static DateTimeFormatter createFormatterForStyle(String style) {
    if(style == null || style.length() != 2) {
      throw new IllegalArgumentException("Invalid style specification: " + style);
    }
    int dateStyle = selectStyle(style.charAt(0));
    int timeStyle = selectStyle(style.charAt(1));
    if(dateStyle == NONE && timeStyle == NONE) {
      throw new IllegalArgumentException("Style \'--\' is invalid");
    }
    return createFormatterForStyleIndex(dateStyle, timeStyle);
  }
  private static DateTimeFormatter createFormatterForStyleIndex(int dateStyle, int timeStyle) {
    int index = ((dateStyle << 2) + dateStyle) + timeStyle;
    if(index >= STYLE_CACHE.length) {
      DateTimeFormatter var_516 = createDateTimeFormatter(dateStyle, timeStyle);
      return var_516;
    }
    DateTimeFormatter f = null;
    synchronized(STYLE_CACHE) {
      f = STYLE_CACHE[index];
      if(f == null) {
        f = createDateTimeFormatter(dateStyle, timeStyle);
        STYLE_CACHE[index] = f;
      }
    }
    return f;
  }
  public static DateTimeFormatter forPattern(String pattern) {
    return createFormatterForPattern(pattern);
  }
  public static DateTimeFormatter forStyle(String style) {
    return createFormatterForStyle(style);
  }
  public static DateTimeFormatter fullDate() {
    return createFormatterForStyleIndex(FULL, NONE);
  }
  public static DateTimeFormatter fullDateTime() {
    return createFormatterForStyleIndex(FULL, FULL);
  }
  public static DateTimeFormatter fullTime() {
    return createFormatterForStyleIndex(NONE, FULL);
  }
  public static DateTimeFormatter longDate() {
    return createFormatterForStyleIndex(LONG, NONE);
  }
  public static DateTimeFormatter longDateTime() {
    return createFormatterForStyleIndex(LONG, LONG);
  }
  public static DateTimeFormatter longTime() {
    return createFormatterForStyleIndex(NONE, LONG);
  }
  public static DateTimeFormatter mediumDate() {
    return createFormatterForStyleIndex(MEDIUM, NONE);
  }
  public static DateTimeFormatter mediumDateTime() {
    return createFormatterForStyleIndex(MEDIUM, MEDIUM);
  }
  public static DateTimeFormatter mediumTime() {
    return createFormatterForStyleIndex(NONE, MEDIUM);
  }
  public static DateTimeFormatter shortDate() {
    return createFormatterForStyleIndex(SHORT, NONE);
  }
  public static DateTimeFormatter shortDateTime() {
    return createFormatterForStyleIndex(SHORT, SHORT);
  }
  public static DateTimeFormatter shortTime() {
    return createFormatterForStyleIndex(NONE, SHORT);
  }
  private static String parseToken(String pattern, int[] indexRef) {
    StringBuilder buf = new StringBuilder();
    int i = indexRef[0];
    int length = pattern.length();
    char c = pattern.charAt(i);
    if(c >= 'A' && c <= 'Z' || c >= 'a' && c <= 'z') {
      buf.append(c);
      while(i + 1 < length){
        char peek = pattern.charAt(i + 1);
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
  public static String patternForStyle(String style, Locale locale) {
    DateTimeFormatter formatter = createFormatterForStyle(style);
    if(locale == null) {
      locale = Locale.getDefault();
    }
    return ((StyleFormatter)formatter.getPrinter()).getPattern(locale);
  }
  private static boolean isNumericToken(String token) {
    int tokenLen = token.length();
    if(tokenLen > 0) {
      char c = token.charAt(0);
      switch (c){
        case 'c':
        case 'C':
        case 'x':
        case 'y':
        case 'Y':
        case 'd':
        case 'h':
        case 'H':
        case 'm':
        case 's':
        case 'S':
        case 'e':
        case 'D':
        case 'F':
        case 'w':
        case 'W':
        case 'k':
        case 'K':
        return true;
        case 'M':
        if(tokenLen <= 2) {
          return true;
        }
      }
    }
    return false;
  }
  private static int selectStyle(char ch) {
    switch (ch){
      case 'S':
      return SHORT;
      case 'M':
      return MEDIUM;
      case 'L':
      return LONG;
      case 'F':
      return FULL;
      case '-':
      return NONE;
      default:
      throw new IllegalArgumentException("Invalid style character: " + ch);
    }
  }
  static void appendPatternTo(DateTimeFormatterBuilder builder, String pattern) {
    parsePatternTo(builder, pattern);
  }
  private static void parsePatternTo(DateTimeFormatterBuilder builder, String pattern) {
    int length = pattern.length();
    int[] indexRef = new int[1];
    for(int i = 0; i < length; i++) {
      indexRef[0] = i;
      String token = parseToken(pattern, indexRef);
      i = indexRef[0];
      int tokenLen = token.length();
      if(tokenLen == 0) {
        break ;
      }
      char c = token.charAt(0);
      switch (c){
        case 'G':
        builder.appendEraText();
        break ;
        case 'C':
        builder.appendCenturyOfEra(tokenLen, tokenLen);
        break ;
        case 'x':
        case 'y':
        case 'Y':
        if(tokenLen == 2) {
          boolean lenientParse = true;
          if(i + 1 < length) {
            indexRef[0]++;
            if(isNumericToken(parseToken(pattern, indexRef))) {
              lenientParse = false;
            }
            indexRef[0]--;
          }
          switch (c){
            case 'x':
            builder.appendTwoDigitWeekyear(new DateTime().getWeekyear() - 30, lenientParse);
            break ;
            case 'y':
            case 'Y':
            default:
            builder.appendTwoDigitYear(new DateTime().getYear() - 30, lenientParse);
            break ;
          }
        }
        else {
          int maxDigits = 9;
          if(i + 1 < length) {
            indexRef[0]++;
            if(isNumericToken(parseToken(pattern, indexRef))) {
              maxDigits = tokenLen;
            }
            indexRef[0]--;
          }
          switch (c){
            case 'x':
            builder.appendWeekyear(tokenLen, maxDigits);
            break ;
            case 'y':
            builder.appendYear(tokenLen, maxDigits);
            break ;
            case 'Y':
            builder.appendYearOfEra(tokenLen, maxDigits);
            break ;
          }
        }
        break ;
        case 'M':
        if(tokenLen >= 3) {
          if(tokenLen >= 4) {
            builder.appendMonthOfYearText();
          }
          else {
            builder.appendMonthOfYearShortText();
          }
        }
        else {
          builder.appendMonthOfYear(tokenLen);
        }
        break ;
        case 'd':
        builder.appendDayOfMonth(tokenLen);
        break ;
        case 'a':
        builder.appendHalfdayOfDayText();
        break ;
        case 'h':
        builder.appendClockhourOfHalfday(tokenLen);
        break ;
        case 'H':
        builder.appendHourOfDay(tokenLen);
        break ;
        case 'k':
        builder.appendClockhourOfDay(tokenLen);
        break ;
        case 'K':
        builder.appendHourOfHalfday(tokenLen);
        break ;
        case 'm':
        builder.appendMinuteOfHour(tokenLen);
        break ;
        case 's':
        builder.appendSecondOfMinute(tokenLen);
        break ;
        case 'S':
        builder.appendFractionOfSecond(tokenLen, tokenLen);
        break ;
        case 'e':
        builder.appendDayOfWeek(tokenLen);
        break ;
        case 'E':
        if(tokenLen >= 4) {
          builder.appendDayOfWeekText();
        }
        else {
          builder.appendDayOfWeekShortText();
        }
        break ;
        case 'D':
        builder.appendDayOfYear(tokenLen);
        break ;
        case 'w':
        builder.appendWeekOfWeekyear(tokenLen);
        break ;
        case 'z':
        if(tokenLen >= 4) {
          builder.appendTimeZoneName();
        }
        else {
          builder.appendTimeZoneShortName(null);
        }
        break ;
        case 'Z':
        if(tokenLen == 1) {
          builder.appendTimeZoneOffset(null, "Z", false, 2, 2);
        }
        else 
          if(tokenLen == 2) {
            builder.appendTimeZoneOffset(null, "Z", true, 2, 2);
          }
          else {
            builder.appendTimeZoneId();
          }
        break ;
        case '\'':
        String sub = token.substring(1);
        if(sub.length() == 1) {
          builder.appendLiteral(sub.charAt(0));
        }
        else {
          builder.appendLiteral(new String(sub));
        }
        break ;
        default:
        throw new IllegalArgumentException("Illegal pattern component: " + token);
      }
    }
  }
  
  static class StyleFormatter implements DateTimePrinter, DateTimeParser  {
    final private static Map<String, DateTimeFormatter> cCache = new HashMap<String, DateTimeFormatter>();
    final private int iDateStyle;
    final private int iTimeStyle;
    final private int iType;
    StyleFormatter(int dateStyle, int timeStyle, int type) {
      super();
      iDateStyle = dateStyle;
      iTimeStyle = timeStyle;
      iType = type;
    }
    private DateTimeFormatter getFormatter(Locale locale) {
      locale = (locale == null ? Locale.getDefault() : locale);
      String key = Integer.toString(iType + (iDateStyle << 4) + (iTimeStyle << 8)) + locale.toString();
      DateTimeFormatter f = null;
      synchronized(cCache) {
        f = cCache.get(key);
        if(f == null) {
          String pattern = getPattern(locale);
          f = DateTimeFormat.forPattern(pattern);
          cCache.put(key, f);
        }
      }
      return f;
    }
    String getPattern(Locale locale) {
      DateFormat f = null;
      switch (iType){
        case DATE:
        f = DateFormat.getDateInstance(iDateStyle, locale);
        break ;
        case TIME:
        f = DateFormat.getTimeInstance(iTimeStyle, locale);
        break ;
        case DATETIME:
        f = DateFormat.getDateTimeInstance(iDateStyle, iTimeStyle, locale);
        break ;
      }
      if(f instanceof SimpleDateFormat == false) {
        throw new IllegalArgumentException("No datetime pattern for locale: " + locale);
      }
      return ((SimpleDateFormat)f).toPattern();
    }
    public int estimateParsedLength() {
      return 40;
    }
    public int estimatePrintedLength() {
      return 40;
    }
    public int parseInto(DateTimeParserBucket bucket, String text, int position) {
      DateTimeParser p = getFormatter(bucket.getLocale()).getParser();
      return p.parseInto(bucket, text, position);
    }
    public void printTo(Writer out, long instant, Chronology chrono, int displayOffset, DateTimeZone displayZone, Locale locale) throws IOException {
      DateTimePrinter p = getFormatter(locale).getPrinter();
      p.printTo(out, instant, chrono, displayOffset, displayZone, locale);
    }
    public void printTo(Writer out, ReadablePartial partial, Locale locale) throws IOException {
      DateTimePrinter p = getFormatter(locale).getPrinter();
      p.printTo(out, partial, locale);
    }
    public void printTo(StringBuffer buf, long instant, Chronology chrono, int displayOffset, DateTimeZone displayZone, Locale locale) {
      DateTimePrinter p = getFormatter(locale).getPrinter();
      p.printTo(buf, instant, chrono, displayOffset, displayZone, locale);
    }
    public void printTo(StringBuffer buf, ReadablePartial partial, Locale locale) {
      DateTimePrinter p = getFormatter(locale).getPrinter();
      p.printTo(buf, partial, locale);
    }
  }
}