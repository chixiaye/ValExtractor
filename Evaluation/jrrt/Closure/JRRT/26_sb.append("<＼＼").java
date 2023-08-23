package com.google.debugging.sourcemap;
import java.io.IOException;
import java.nio.charset.CharsetEncoder;

class Util  {
  final private static char[] HEX_CHARS = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' } ;
  static String escapeString(String s) {
    return escapeString(s, '\"', "\\\"", "\'", "\\\\", null);
  }
  static String escapeString(String s, char quote, String doublequoteEscape, String singlequoteEscape, String backslashEscape, CharsetEncoder outputCharsetEncoder) {
    StringBuilder sb = new StringBuilder(s.length() + 2);
    sb.append(quote);
    for(int i = 0; i < s.length(); i++) {
      char c = s.charAt(i);
      switch (c){
        case '\n':
        sb.append("\\n");
        break ;
        case '\r':
        sb.append("\\r");
        break ;
        case '\t':
        sb.append("\\t");
        break ;
        case '\\':
        sb.append(backslashEscape);
        break ;
        case '\"':
        sb.append(doublequoteEscape);
        break ;
        case '\'':
        sb.append(singlequoteEscape);
        break ;
        case '>':
        if(i >= 2 && ((s.charAt(i - 1) == '-' && s.charAt(i - 2) == '-') || (s.charAt(i - 1) == ']' && s.charAt(i - 2) == ']'))) {
          sb.append("\\>");
        }
        else {
          sb.append(c);
        }
        break ;
        case '<':
        final String END_SCRIPT = "/script";
        final String START_COMMENT = "!--";
        if(s.regionMatches(true, i + 1, END_SCRIPT, 0, END_SCRIPT.length())) {
          StringBuilder var_26 = sb.append("<\\");
        }
        else 
          if(s.regionMatches(false, i + 1, START_COMMENT, 0, START_COMMENT.length())) {
            sb.append("<\\");
          }
          else {
            sb.append(c);
          }
        break ;
        default:
        if(outputCharsetEncoder != null) {
          if(outputCharsetEncoder.canEncode(c)) {
            sb.append(c);
          }
          else {
            appendCharAsHex(sb, c);
          }
        }
        else {
          if(c > 0x1f && c <= 0x7f) {
            sb.append(c);
          }
          else {
            appendCharAsHex(sb, c);
          }
        }
      }
    }
    sb.append(quote);
    return sb.toString();
  }
  @SuppressWarnings(value = {"cast", }) private static void appendCharAsHex(StringBuilder sb, char c) {
    try {
      appendHexJavaScriptRepresentation(sb, (int)c);
    }
    catch (IOException ex) {
      throw new RuntimeException(ex);
    }
  }
  private static void appendHexJavaScriptRepresentation(Appendable out, int codePoint) throws IOException {
    if(Character.isSupplementaryCodePoint(codePoint)) {
      char[] surrogates = Character.toChars(codePoint);
      appendHexJavaScriptRepresentation(out, surrogates[0]);
      appendHexJavaScriptRepresentation(out, surrogates[1]);
      return ;
    }
    out.append("\\u").append(HEX_CHARS[(codePoint >>> 12) & 0xf]).append(HEX_CHARS[(codePoint >>> 8) & 0xf]).append(HEX_CHARS[(codePoint >>> 4) & 0xf]).append(HEX_CHARS[codePoint & 0xf]);
  }
}