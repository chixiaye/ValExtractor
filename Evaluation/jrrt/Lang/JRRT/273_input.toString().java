package org.apache.commons.lang3;
import java.io.IOException;
import java.io.Writer;
import org.apache.commons.lang3.text.translate.AggregateTranslator;
import org.apache.commons.lang3.text.translate.CharSequenceTranslator;
import org.apache.commons.lang3.text.translate.EntityArrays;
import org.apache.commons.lang3.text.translate.JavaUnicodeEscaper;
import org.apache.commons.lang3.text.translate.LookupTranslator;
import org.apache.commons.lang3.text.translate.NumericEntityUnescaper;
import org.apache.commons.lang3.text.translate.OctalUnescaper;
import org.apache.commons.lang3.text.translate.UnicodeUnescaper;

public class StringEscapeUtils  {
  final public static CharSequenceTranslator ESCAPE_JAVA = new LookupTranslator(new String[][]{ { "\"", "\\\"" } , { "\\", "\\\\" }  } ).with(new LookupTranslator(EntityArrays.JAVA_CTRL_CHARS_ESCAPE())).with(JavaUnicodeEscaper.outsideOf(32, 0x7f));
  final public static CharSequenceTranslator ESCAPE_ECMASCRIPT = new AggregateTranslator(new LookupTranslator(new String[][]{ { "\'", "\\\'" } , { "\"", "\\\"" } , { "\\", "\\\\" } , { "/", "\\/" }  } ), new LookupTranslator(EntityArrays.JAVA_CTRL_CHARS_ESCAPE()), JavaUnicodeEscaper.outsideOf(32, 0x7f));
  final public static CharSequenceTranslator ESCAPE_JSON = new AggregateTranslator(new LookupTranslator(new String[][]{ { "\"", "\\\"" } , { "\\", "\\\\" } , { "/", "\\/" }  } ), new LookupTranslator(EntityArrays.JAVA_CTRL_CHARS_ESCAPE()), JavaUnicodeEscaper.outsideOf(32, 0x7f));
  final public static CharSequenceTranslator ESCAPE_XML = new AggregateTranslator(new LookupTranslator(EntityArrays.BASIC_ESCAPE()), new LookupTranslator(EntityArrays.APOS_ESCAPE()));
  final public static CharSequenceTranslator ESCAPE_HTML3 = new AggregateTranslator(new LookupTranslator(EntityArrays.BASIC_ESCAPE()), new LookupTranslator(EntityArrays.ISO8859_1_ESCAPE()));
  final public static CharSequenceTranslator ESCAPE_HTML4 = new AggregateTranslator(new LookupTranslator(EntityArrays.BASIC_ESCAPE()), new LookupTranslator(EntityArrays.ISO8859_1_ESCAPE()), new LookupTranslator(EntityArrays.HTML40_EXTENDED_ESCAPE()));
  final public static CharSequenceTranslator ESCAPE_CSV = new CsvEscaper();
  final public static CharSequenceTranslator UNESCAPE_JAVA = new AggregateTranslator(new OctalUnescaper(), new UnicodeUnescaper(), new LookupTranslator(EntityArrays.JAVA_CTRL_CHARS_UNESCAPE()), new LookupTranslator(new String[][]{ { "\\\\", "\\" } , { "\\\"", "\"" } , { "\\\'", "\'" } , { "\\", "" }  } ));
  final public static CharSequenceTranslator UNESCAPE_ECMASCRIPT = UNESCAPE_JAVA;
  final public static CharSequenceTranslator UNESCAPE_JSON = UNESCAPE_JAVA;
  final public static CharSequenceTranslator UNESCAPE_HTML3 = new AggregateTranslator(new LookupTranslator(EntityArrays.BASIC_UNESCAPE()), new LookupTranslator(EntityArrays.ISO8859_1_UNESCAPE()), new NumericEntityUnescaper());
  final public static CharSequenceTranslator UNESCAPE_HTML4 = new AggregateTranslator(new LookupTranslator(EntityArrays.BASIC_UNESCAPE()), new LookupTranslator(EntityArrays.ISO8859_1_UNESCAPE()), new LookupTranslator(EntityArrays.HTML40_EXTENDED_UNESCAPE()), new NumericEntityUnescaper());
  final public static CharSequenceTranslator UNESCAPE_XML = new AggregateTranslator(new LookupTranslator(EntityArrays.BASIC_UNESCAPE()), new LookupTranslator(EntityArrays.APOS_UNESCAPE()), new NumericEntityUnescaper());
  final public static CharSequenceTranslator UNESCAPE_CSV = new CsvUnescaper();
  public StringEscapeUtils() {
    super();
  }
  final public static String escapeCsv(final String input) {
    return ESCAPE_CSV.translate(input);
  }
  final public static String escapeEcmaScript(final String input) {
    return ESCAPE_ECMASCRIPT.translate(input);
  }
  final public static String escapeHtml3(final String input) {
    return ESCAPE_HTML3.translate(input);
  }
  final public static String escapeHtml4(final String input) {
    return ESCAPE_HTML4.translate(input);
  }
  final public static String escapeJava(final String input) {
    return ESCAPE_JAVA.translate(input);
  }
  final public static String escapeJson(final String input) {
    return ESCAPE_JSON.translate(input);
  }
  final public static String escapeXml(final String input) {
    return ESCAPE_XML.translate(input);
  }
  final public static String unescapeCsv(final String input) {
    return UNESCAPE_CSV.translate(input);
  }
  final public static String unescapeEcmaScript(final String input) {
    return UNESCAPE_ECMASCRIPT.translate(input);
  }
  final public static String unescapeHtml3(final String input) {
    return UNESCAPE_HTML3.translate(input);
  }
  final public static String unescapeHtml4(final String input) {
    return UNESCAPE_HTML4.translate(input);
  }
  final public static String unescapeJava(final String input) {
    return UNESCAPE_JAVA.translate(input);
  }
  final public static String unescapeJson(final String input) {
    return UNESCAPE_JSON.translate(input);
  }
  final public static String unescapeXml(final String input) {
    return UNESCAPE_XML.translate(input);
  }
  
  static class CsvEscaper extends CharSequenceTranslator  {
    final private static char CSV_DELIMITER = ',';
    final private static char CSV_QUOTE = '\"';
    final private static String CSV_QUOTE_STR = String.valueOf(CSV_QUOTE);
    final private static char[] CSV_SEARCH_CHARS = new char[]{ CSV_DELIMITER, CSV_QUOTE, CharUtils.CR, CharUtils.LF } ;
    @Override() public int translate(final CharSequence input, final int index, final Writer out) throws IOException {
      if(index != 0) {
        throw new IllegalStateException("CsvEscaper should never reach the [1] index");
      }
      if(StringUtils.containsNone(input.toString(), CSV_SEARCH_CHARS)) {
        out.write(input.toString());
      }
      else {
        out.write(CSV_QUOTE);
        out.write(StringUtils.replace(input.toString(), CSV_QUOTE_STR, CSV_QUOTE_STR + CSV_QUOTE_STR));
        out.write(CSV_QUOTE);
      }
      return input.length();
    }
  }
  
  static class CsvUnescaper extends CharSequenceTranslator  {
    final private static char CSV_DELIMITER = ',';
    final private static char CSV_QUOTE = '\"';
    final private static String CSV_QUOTE_STR = String.valueOf(CSV_QUOTE);
    final private static char[] CSV_SEARCH_CHARS = new char[]{ CSV_DELIMITER, CSV_QUOTE, CharUtils.CR, CharUtils.LF } ;
    @Override() public int translate(final CharSequence input, final int index, final Writer out) throws IOException {
      if(index != 0) {
        throw new IllegalStateException("CsvUnescaper should never reach the [1] index");
      }
      if(input.charAt(0) != CSV_QUOTE || input.charAt(input.length() - 1) != CSV_QUOTE) {
        String var_273 = input.toString();
        out.write(var_273);
        return input.length();
      }
      final String quoteless = input.subSequence(1, input.length() - 1).toString();
      if(StringUtils.containsAny(quoteless, CSV_SEARCH_CHARS)) {
        out.write(StringUtils.replace(quoteless, CSV_QUOTE_STR + CSV_QUOTE_STR, CSV_QUOTE_STR));
      }
      else {
        out.write(input.toString());
      }
      return input.length();
    }
  }
}