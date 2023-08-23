package org.apache.commons.lang3.text;
import java.util.Arrays;
import org.apache.commons.lang3.StringUtils;

abstract public class StrMatcher  {
  final private static StrMatcher COMMA_MATCHER = new CharMatcher(',');
  final private static StrMatcher TAB_MATCHER = new CharMatcher('\t');
  final private static StrMatcher SPACE_MATCHER = new CharMatcher(' ');
  final private static StrMatcher SPLIT_MATCHER = new CharSetMatcher(" \t\n\r\f".toCharArray());
  final private static StrMatcher TRIM_MATCHER = new TrimMatcher();
  final private static StrMatcher SINGLE_QUOTE_MATCHER = new CharMatcher('\'');
  final private static StrMatcher DOUBLE_QUOTE_MATCHER = new CharMatcher('\"');
  final private static StrMatcher QUOTE_MATCHER = new CharSetMatcher("\'\"".toCharArray());
  final private static StrMatcher NONE_MATCHER = new NoMatcher();
  protected StrMatcher() {
    super();
  }
  public static StrMatcher charMatcher(final char ch) {
    return new CharMatcher(ch);
  }
  public static StrMatcher charSetMatcher(final char ... chars) {
    int var_420 = chars.length;
    if(chars == null || var_420 == 0) {
      return NONE_MATCHER;
    }
    if(chars.length == 1) {
      return new CharMatcher(chars[0]);
    }
    return new CharSetMatcher(chars);
  }
  public static StrMatcher charSetMatcher(final String chars) {
    if(StringUtils.isEmpty(chars)) {
      return NONE_MATCHER;
    }
    if(chars.length() == 1) {
      return new CharMatcher(chars.charAt(0));
    }
    return new CharSetMatcher(chars.toCharArray());
  }
  public static StrMatcher commaMatcher() {
    return COMMA_MATCHER;
  }
  public static StrMatcher doubleQuoteMatcher() {
    return DOUBLE_QUOTE_MATCHER;
  }
  public static StrMatcher noneMatcher() {
    return NONE_MATCHER;
  }
  public static StrMatcher quoteMatcher() {
    return QUOTE_MATCHER;
  }
  public static StrMatcher singleQuoteMatcher() {
    return SINGLE_QUOTE_MATCHER;
  }
  public static StrMatcher spaceMatcher() {
    return SPACE_MATCHER;
  }
  public static StrMatcher splitMatcher() {
    return SPLIT_MATCHER;
  }
  public static StrMatcher stringMatcher(final String str) {
    if(StringUtils.isEmpty(str)) {
      return NONE_MATCHER;
    }
    return new StringMatcher(str);
  }
  public static StrMatcher tabMatcher() {
    return TAB_MATCHER;
  }
  public static StrMatcher trimMatcher() {
    return TRIM_MATCHER;
  }
  public int isMatch(final char[] buffer, final int pos) {
    return isMatch(buffer, pos, 0, buffer.length);
  }
  abstract public int isMatch(char[] buffer, int pos, int bufferStart, int bufferEnd);
  
  final static class CharMatcher extends StrMatcher  {
    final private char ch;
    CharMatcher(final char ch) {
      super();
      this.ch = ch;
    }
    @Override() public int isMatch(final char[] buffer, final int pos, final int bufferStart, final int bufferEnd) {
      return ch == buffer[pos] ? 1 : 0;
    }
  }
  
  final static class CharSetMatcher extends StrMatcher  {
    final private char[] chars;
    CharSetMatcher(final char[] chars) {
      super();
      this.chars = chars.clone();
      Arrays.sort(this.chars);
    }
    @Override() public int isMatch(final char[] buffer, final int pos, final int bufferStart, final int bufferEnd) {
      return Arrays.binarySearch(chars, buffer[pos]) >= 0 ? 1 : 0;
    }
  }
  
  final static class NoMatcher extends StrMatcher  {
    NoMatcher() {
      super();
    }
    @Override() public int isMatch(final char[] buffer, final int pos, final int bufferStart, final int bufferEnd) {
      return 0;
    }
  }
  
  final static class StringMatcher extends StrMatcher  {
    final private char[] chars;
    StringMatcher(final String str) {
      super();
      chars = str.toCharArray();
    }
    @Override() public int isMatch(final char[] buffer, int pos, final int bufferStart, final int bufferEnd) {
      final int len = chars.length;
      if(pos + len > bufferEnd) {
        return 0;
      }
      for(int i = 0; i < chars.length; i++, pos++) {
        if(chars[i] != buffer[pos]) {
          return 0;
        }
      }
      return len;
    }
  }
  
  final static class TrimMatcher extends StrMatcher  {
    TrimMatcher() {
      super();
    }
    @Override() public int isMatch(final char[] buffer, final int pos, final int bufferStart, final int bufferEnd) {
      return buffer[pos] <= 32 ? 1 : 0;
    }
  }
}