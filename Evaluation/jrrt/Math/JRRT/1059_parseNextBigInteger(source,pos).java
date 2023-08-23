package org.apache.commons.math3.fraction;
import java.io.Serializable;
import java.math.BigInteger;
import java.text.FieldPosition;
import java.text.NumberFormat;
import java.text.ParsePosition;
import java.util.Locale;
import org.apache.commons.math3.exception.MathIllegalArgumentException;
import org.apache.commons.math3.exception.MathParseException;
import org.apache.commons.math3.exception.util.LocalizedFormats;

public class BigFractionFormat extends AbstractFormat implements Serializable  {
  final private static long serialVersionUID = -2932167925527338976L;
  public BigFractionFormat() {
    super();
  }
  public BigFractionFormat(final NumberFormat format) {
    super(format);
  }
  public BigFractionFormat(final NumberFormat numeratorFormat, final NumberFormat denominatorFormat) {
    super(numeratorFormat, denominatorFormat);
  }
  @Override() public BigFraction parse(final String source) throws MathParseException {
    final ParsePosition parsePosition = new ParsePosition(0);
    final BigFraction result = parse(source, parsePosition);
    if(parsePosition.getIndex() == 0) {
      throw new MathParseException(source, parsePosition.getErrorIndex(), BigFraction.class);
    }
    return result;
  }
  @Override() public BigFraction parse(final String source, final ParsePosition pos) {
    final int initialIndex = pos.getIndex();
    parseAndIgnoreWhitespace(source, pos);
    final BigInteger num = parseNextBigInteger(source, pos);
    if(num == null) {
      pos.setIndex(initialIndex);
      return null;
    }
    final int startIndex = pos.getIndex();
    final char c = parseNextCharacter(source, pos);
    switch (c){
      case 0:
      return new BigFraction(num);
      case '/':
      break ;
      default:
      pos.setIndex(initialIndex);
      pos.setErrorIndex(startIndex);
      return null;
    }
    parseAndIgnoreWhitespace(source, pos);
    BigInteger var_1059 = parseNextBigInteger(source, pos);
    final BigInteger den = var_1059;
    if(den == null) {
      pos.setIndex(initialIndex);
      return null;
    }
    return new BigFraction(num, den);
  }
  public static BigFractionFormat getImproperInstance() {
    return getImproperInstance(Locale.getDefault());
  }
  public static BigFractionFormat getImproperInstance(final Locale locale) {
    return new BigFractionFormat(getDefaultNumberFormat(locale));
  }
  public static BigFractionFormat getProperInstance() {
    return getProperInstance(Locale.getDefault());
  }
  public static BigFractionFormat getProperInstance(final Locale locale) {
    return new ProperBigFractionFormat(getDefaultNumberFormat(locale));
  }
  protected BigInteger parseNextBigInteger(final String source, final ParsePosition pos) {
    final int start = pos.getIndex();
    int end = (source.charAt(start) == '-') ? (start + 1) : start;
    while((end < source.length()) && Character.isDigit(source.charAt(end))){
      ++end;
    }
    try {
      BigInteger n = new BigInteger(source.substring(start, end));
      pos.setIndex(end);
      return n;
    }
    catch (NumberFormatException nfe) {
      pos.setErrorIndex(start);
      return null;
    }
  }
  public static Locale[] getAvailableLocales() {
    return NumberFormat.getAvailableLocales();
  }
  public static String formatBigFraction(final BigFraction f) {
    return getImproperInstance().format(f);
  }
  @Override() public StringBuffer format(final Object obj, final StringBuffer toAppendTo, final FieldPosition pos) {
    final StringBuffer ret;
    if(obj instanceof BigFraction) {
      ret = format((BigFraction)obj, toAppendTo, pos);
    }
    else 
      if(obj instanceof BigInteger) {
        ret = format(new BigFraction((BigInteger)obj), toAppendTo, pos);
      }
      else 
        if(obj instanceof Number) {
          ret = format(new BigFraction(((Number)obj).doubleValue()), toAppendTo, pos);
        }
        else {
          throw new MathIllegalArgumentException(LocalizedFormats.CANNOT_FORMAT_OBJECT_TO_FRACTION);
        }
    return ret;
  }
  public StringBuffer format(final BigFraction BigFraction, final StringBuffer toAppendTo, final FieldPosition pos) {
    pos.setBeginIndex(0);
    pos.setEndIndex(0);
    getNumeratorFormat().format(BigFraction.getNumerator(), toAppendTo, pos);
    toAppendTo.append(" / ");
    getDenominatorFormat().format(BigFraction.getDenominator(), toAppendTo, pos);
    return toAppendTo;
  }
}