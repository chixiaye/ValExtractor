package org.apache.commons.lang3;

public class CharSetUtils  {
  public CharSetUtils() {
    super();
  }
  public static String delete(final String str, final String ... set) {
    if(StringUtils.isEmpty(str) || deepEmpty(set)) {
      return str;
    }
    return modify(str, set, false);
  }
  public static String keep(final String str, final String ... set) {
    if(str == null) {
      return null;
    }
    if(str.isEmpty() || deepEmpty(set)) {
      return StringUtils.EMPTY;
    }
    return modify(str, set, true);
  }
  private static String modify(final String str, final String[] set, final boolean expect) {
    final CharSet chars = CharSet.getInstance(set);
    final StringBuilder buffer = new StringBuilder(str.length());
    final char[] chrs = str.toCharArray();
    final int sz = chrs.length;
    for(int i = 0; i < sz; i++) {
      char var_245 = chrs[i];
      if(chars.contains(var_245) == expect) {
        buffer.append(chrs[i]);
      }
    }
    return buffer.toString();
  }
  public static String squeeze(final String str, final String ... set) {
    if(StringUtils.isEmpty(str) || deepEmpty(set)) {
      return str;
    }
    final CharSet chars = CharSet.getInstance(set);
    final StringBuilder buffer = new StringBuilder(str.length());
    final char[] chrs = str.toCharArray();
    final int sz = chrs.length;
    char lastChar = ' ';
    char ch = ' ';
    for(int i = 0; i < sz; i++) {
      ch = chrs[i];
      if(ch == lastChar && i != 0 && chars.contains(ch)) {
        continue ;
      }
      buffer.append(ch);
      lastChar = ch;
    }
    return buffer.toString();
  }
  public static boolean containsAny(final String str, final String ... set) {
    if(StringUtils.isEmpty(str) || deepEmpty(set)) {
      return false;
    }
    final CharSet chars = CharSet.getInstance(set);
    for (final char c : str.toCharArray()) {
      if(chars.contains(c)) {
        return true;
      }
    }
    return false;
  }
  private static boolean deepEmpty(final String[] strings) {
    if(strings != null) {
      for (final String s : strings) {
        if(StringUtils.isNotEmpty(s)) {
          return false;
        }
      }
    }
    return true;
  }
  public static int count(final String str, final String ... set) {
    if(StringUtils.isEmpty(str) || deepEmpty(set)) {
      return 0;
    }
    final CharSet chars = CharSet.getInstance(set);
    int count = 0;
    for (final char c : str.toCharArray()) {
      if(chars.contains(c)) {
        count++;
      }
    }
    return count;
  }
}