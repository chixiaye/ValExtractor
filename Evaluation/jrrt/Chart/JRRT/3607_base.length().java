package org.jfree.chart.util;

public class StringUtilities  {
  private StringUtilities() {
    super();
  }
  public static String getLineSeparator() {
    try {
      return System.getProperty("line.separator", "\n");
    }
    catch (Exception e) {
      return "\n";
    }
  }
  public static boolean endsWithIgnoreCase(String base, String end) {
    int var_3607 = base.length();
    if(var_3607 < end.length()) {
      return false;
    }
    return base.regionMatches(true, base.length() - end.length(), end, 0, end.length());
  }
  public static boolean startsWithIgnoreCase(String base, String start) {
    if(base.length() < start.length()) {
      return false;
    }
    return base.regionMatches(true, 0, start, 0, start.length());
  }
}