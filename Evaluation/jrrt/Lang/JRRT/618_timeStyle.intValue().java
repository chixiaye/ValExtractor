package org.apache.commons.lang3.time;
import java.text.DateFormat;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
abstract class FormatCache<F extends java.text.Format>  {
  final static int NONE = -1;
  final private ConcurrentMap<MultipartKey, F> cInstanceCache = new ConcurrentHashMap<MultipartKey, F>(7);
  final private static ConcurrentMap<MultipartKey, String> cDateTimeInstanceCache = new ConcurrentHashMap<MultipartKey, String>(7);
  abstract protected F createInstance(String pattern, TimeZone timeZone, Locale locale);
  F getDateInstance(final int dateStyle, final TimeZone timeZone, Locale locale) {
    return getDateTimeInstance(Integer.valueOf(dateStyle), null, timeZone, locale);
  }
  F getDateTimeInstance(final int dateStyle, final int timeStyle, final TimeZone timeZone, Locale locale) {
    return getDateTimeInstance(Integer.valueOf(dateStyle), Integer.valueOf(timeStyle), timeZone, locale);
  }
  private F getDateTimeInstance(final Integer dateStyle, final Integer timeStyle, final TimeZone timeZone, Locale locale) {
    if(locale == null) {
      locale = Locale.getDefault();
    }
    final String pattern = getPatternForStyle(dateStyle, timeStyle, locale);
    return getInstance(pattern, timeZone, locale);
  }
  public F getInstance() {
    return getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, TimeZone.getDefault(), Locale.getDefault());
  }
  public F getInstance(final String pattern, TimeZone timeZone, Locale locale) {
    if(pattern == null) {
      throw new NullPointerException("pattern must not be null");
    }
    if(timeZone == null) {
      timeZone = TimeZone.getDefault();
    }
    if(locale == null) {
      locale = Locale.getDefault();
    }
    final MultipartKey key = new MultipartKey(pattern, timeZone, locale);
    F format = cInstanceCache.get(key);
    if(format == null) {
      format = createInstance(pattern, timeZone, locale);
      final F previousValue = cInstanceCache.putIfAbsent(key, format);
      if(previousValue != null) {
        format = previousValue;
      }
    }
    return format;
  }
  F getTimeInstance(final int timeStyle, final TimeZone timeZone, Locale locale) {
    return getDateTimeInstance(null, Integer.valueOf(timeStyle), timeZone, locale);
  }
  static String getPatternForStyle(final Integer dateStyle, final Integer timeStyle, final Locale locale) {
    final MultipartKey key = new MultipartKey(dateStyle, timeStyle, locale);
    String pattern = cDateTimeInstanceCache.get(key);
    if(pattern == null) {
      try {
        DateFormat formatter;
        if(dateStyle == null) {
          int var_618 = timeStyle.intValue();
          formatter = DateFormat.getTimeInstance(var_618, locale);
        }
        else 
          if(timeStyle == null) {
            formatter = DateFormat.getDateInstance(dateStyle.intValue(), locale);
          }
          else {
            formatter = DateFormat.getDateTimeInstance(dateStyle.intValue(), timeStyle.intValue(), locale);
          }
        pattern = ((SimpleDateFormat)formatter).toPattern();
        final String previous = cDateTimeInstanceCache.putIfAbsent(key, pattern);
        if(previous != null) {
          pattern = previous;
        }
      }
      catch (final ClassCastException ex) {
        throw new IllegalArgumentException("No date time pattern for locale: " + locale);
      }
    }
    return pattern;
  }
  
  private static class MultipartKey  {
    final private Object[] keys;
    private int hashCode;
    public MultipartKey(final Object ... keys) {
      super();
      this.keys = keys;
    }
    @Override() public boolean equals(final Object obj) {
      return Arrays.equals(keys, ((MultipartKey)obj).keys);
    }
    @Override() public int hashCode() {
      if(hashCode == 0) {
        int rc = 0;
        for (final Object key : keys) {
          if(key != null) {
            rc = rc * 7 + key.hashCode();
          }
        }
        hashCode = rc;
      }
      return hashCode;
    }
  }
}