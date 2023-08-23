package org.joda.time.chrono;
import java.lang.ref.WeakReference;
import java.text.DateFormatSymbols;
import java.util.Locale;
import java.util.TreeMap;
import java.util.WeakHashMap;
import org.joda.time.DateTimeFieldType;
import org.joda.time.DateTimeUtils;
import org.joda.time.IllegalFieldValueException;

class GJLocaleSymbols  {
  final private static int FAST_CACHE_SIZE = 64;
  final private static GJLocaleSymbols[] cFastCache = new GJLocaleSymbols[FAST_CACHE_SIZE];
  private static WeakHashMap<Locale, GJLocaleSymbols> cCache = new WeakHashMap<Locale, GJLocaleSymbols>();
  final private WeakReference<Locale> iLocale;
  final private String[] iEras;
  final private String[] iDaysOfWeek;
  final private String[] iShortDaysOfWeek;
  final private String[] iMonths;
  final private String[] iShortMonths;
  final private String[] iHalfday;
  final private TreeMap<String, Integer> iParseEras;
  final private TreeMap<String, Integer> iParseDaysOfWeek;
  final private TreeMap<String, Integer> iParseMonths;
  final private int iMaxEraLength;
  final private int iMaxDayOfWeekLength;
  final private int iMaxShortDayOfWeekLength;
  final private int iMaxMonthLength;
  final private int iMaxShortMonthLength;
  final private int iMaxHalfdayLength;
  private GJLocaleSymbols(Locale locale) {
    super();
    iLocale = new WeakReference<Locale>(locale);
    DateFormatSymbols dfs = DateTimeUtils.getDateFormatSymbols(locale);
    iEras = dfs.getEras();
    iDaysOfWeek = realignDaysOfWeek(dfs.getWeekdays());
    iShortDaysOfWeek = realignDaysOfWeek(dfs.getShortWeekdays());
    iMonths = realignMonths(dfs.getMonths());
    iShortMonths = realignMonths(dfs.getShortMonths());
    iHalfday = dfs.getAmPmStrings();
    Integer[] integers = new Integer[13];
    for(int i = 0; i < 13; i++) {
      integers[i] = Integer.valueOf(i);
    }
    iParseEras = new TreeMap<String, Integer>(String.CASE_INSENSITIVE_ORDER);
    addSymbols(iParseEras, iEras, integers);
    if("en".equals(locale.getLanguage())) {
      iParseEras.put("BCE", integers[0]);
      iParseEras.put("CE", integers[1]);
    }
    java.util.Comparator<String> var_368 = String.CASE_INSENSITIVE_ORDER;
    iParseDaysOfWeek = new TreeMap<String, Integer>(var_368);
    addSymbols(iParseDaysOfWeek, iDaysOfWeek, integers);
    addSymbols(iParseDaysOfWeek, iShortDaysOfWeek, integers);
    addNumerals(iParseDaysOfWeek, 1, 7, integers);
    iParseMonths = new TreeMap<String, Integer>(String.CASE_INSENSITIVE_ORDER);
    addSymbols(iParseMonths, iMonths, integers);
    addSymbols(iParseMonths, iShortMonths, integers);
    addNumerals(iParseMonths, 1, 12, integers);
    iMaxEraLength = maxLength(iEras);
    iMaxDayOfWeekLength = maxLength(iDaysOfWeek);
    iMaxShortDayOfWeekLength = maxLength(iShortDaysOfWeek);
    iMaxMonthLength = maxLength(iMonths);
    iMaxShortMonthLength = maxLength(iShortMonths);
    iMaxHalfdayLength = maxLength(iHalfday);
  }
  public static GJLocaleSymbols forLocale(Locale locale) {
    if(locale == null) {
      locale = Locale.getDefault();
    }
    int index = System.identityHashCode(locale) & (FAST_CACHE_SIZE - 1);
    GJLocaleSymbols symbols = cFastCache[index];
    if(symbols != null && symbols.iLocale.get() == locale) {
      return symbols;
    }
    synchronized(cCache) {
      symbols = cCache.get(locale);
      if(symbols == null) {
        symbols = new GJLocaleSymbols(locale);
        cCache.put(locale, symbols);
      }
    }
    cFastCache[index] = symbols;
    return symbols;
  }
  public String dayOfWeekValueToShortText(int value) {
    return iShortDaysOfWeek[value];
  }
  public String dayOfWeekValueToText(int value) {
    return iDaysOfWeek[value];
  }
  public String eraValueToText(int value) {
    return iEras[value];
  }
  public String halfdayValueToText(int value) {
    return iHalfday[value];
  }
  public String monthOfYearValueToShortText(int value) {
    return iShortMonths[value];
  }
  public String monthOfYearValueToText(int value) {
    return iMonths[value];
  }
  private static String[] realignDaysOfWeek(String[] daysOfWeek) {
    String[] a = new String[8];
    for(int i = 1; i < 8; i++) {
      a[i] = daysOfWeek[(i < 7) ? i + 1 : 1];
    }
    return a;
  }
  private static String[] realignMonths(String[] months) {
    String[] a = new String[13];
    for(int i = 1; i < 13; i++) {
      a[i] = months[i - 1];
    }
    return a;
  }
  public int dayOfWeekTextToValue(String text) {
    Integer day = iParseDaysOfWeek.get(text);
    if(day != null) {
      return day.intValue();
    }
    throw new IllegalFieldValueException(DateTimeFieldType.dayOfWeek(), text);
  }
  public int eraTextToValue(String text) {
    Integer era = iParseEras.get(text);
    if(era != null) {
      return era.intValue();
    }
    throw new IllegalFieldValueException(DateTimeFieldType.era(), text);
  }
  public int getDayOfWeekMaxShortTextLength() {
    return iMaxShortDayOfWeekLength;
  }
  public int getDayOfWeekMaxTextLength() {
    return iMaxDayOfWeekLength;
  }
  public int getEraMaxTextLength() {
    return iMaxEraLength;
  }
  public int getHalfdayMaxTextLength() {
    return iMaxHalfdayLength;
  }
  public int getMonthMaxShortTextLength() {
    return iMaxShortMonthLength;
  }
  public int getMonthMaxTextLength() {
    return iMaxMonthLength;
  }
  public int halfdayTextToValue(String text) {
    String[] halfday = iHalfday;
    for(int i = halfday.length; --i >= 0; ) {
      if(halfday[i].equalsIgnoreCase(text)) {
        return i;
      }
    }
    throw new IllegalFieldValueException(DateTimeFieldType.halfdayOfDay(), text);
  }
  private static int maxLength(String[] a) {
    int max = 0;
    for(int i = a.length; --i >= 0; ) {
      String s = a[i];
      if(s != null) {
        int len = s.length();
        if(len > max) {
          max = len;
        }
      }
    }
    return max;
  }
  public int monthOfYearTextToValue(String text) {
    Integer month = iParseMonths.get(text);
    if(month != null) {
      return month.intValue();
    }
    throw new IllegalFieldValueException(DateTimeFieldType.monthOfYear(), text);
  }
  private static void addNumerals(TreeMap<String, Integer> map, int start, int end, Integer[] integers) {
    for(int i = start; i <= end; i++) {
      map.put(String.valueOf(i).intern(), integers[i]);
    }
  }
  private static void addSymbols(TreeMap<String, Integer> map, String[] symbols, Integer[] integers) {
    for(int i = symbols.length; --i >= 0; ) {
      String symbol = symbols[i];
      if(symbol != null) {
        map.put(symbol, integers[i]);
      }
    }
  }
}