package org.apache.commons.math3.fraction;
import java.text.FieldPosition;
import java.text.NumberFormat;
import java.text.ParsePosition;
import java.util.Locale;
import org.apache.commons.math3.exception.MathIllegalArgumentException;
import org.apache.commons.math3.exception.MathParseException;
import org.apache.commons.math3.exception.util.LocalizedFormats;

public class FractionFormat extends AbstractFormat  {
  final private static long serialVersionUID = 3008655719530972611L;
  public FractionFormat() {
    super();
  }
  public FractionFormat(final NumberFormat format) {
    super(format);
  }
  public FractionFormat(final NumberFormat numeratorFormat, final NumberFormat denominatorFormat) {
    super(numeratorFormat, denominatorFormat);
  }
  @Override() public Fraction parse(final String source) throws MathParseException {
    final ParsePosition parsePosition = new ParsePosition(0);
    final Fraction result = parse(source, parsePosition);
    if(parsePosition.getIndex() == 0) {
      throw new MathParseException(source, parsePosition.getErrorIndex(), Fraction.class);
    }
    return result;
  }
  @Override() public Fraction parse(final String source, final ParsePosition pos) {
    int var_1074 = pos.getIndex();
    final int initialIndex = var_1074;
    parseAndIgnoreWhitespace(source, pos);
    final Number num = getNumeratorFormat().parse(source, pos);
    if(num == null) {
      pos.setIndex(initialIndex);
      return null;
    }
    final int startIndex = pos.getIndex();
    final char c = parseNextCharacter(source, pos);
    switch (c){
      case 0:
      return new Fraction(num.intValue(), 1);
      case '/':
      break ;
      default:
      pos.setIndex(initialIndex);
      pos.setErrorIndex(startIndex);
      return null;
    }
    parseAndIgnoreWhitespace(source, pos);
    final Number den = getDenominatorFormat().parse(source, pos);
    if(den == null) {
      pos.setIndex(initialIndex);
      return null;
    }
    return new Fraction(num.intValue(), den.intValue());
  }
  public static FractionFormat getImproperInstance() {
    return getImproperInstance(Locale.getDefault());
  }
  public static FractionFormat getImproperInstance(final Locale locale) {
    return new FractionFormat(getDefaultNumberFormat(locale));
  }
  public static FractionFormat getProperInstance() {
    return getProperInstance(Locale.getDefault());
  }
  public static FractionFormat getProperInstance(final Locale locale) {
    return new ProperFractionFormat(getDefaultNumberFormat(locale));
  }
  public static Locale[] getAvailableLocales() {
    return NumberFormat.getAvailableLocales();
  }
  protected static NumberFormat getDefaultNumberFormat() {
    return getDefaultNumberFormat(Locale.getDefault());
  }
  public static String formatFraction(Fraction f) {
    return getImproperInstance().format(f);
  }
  @Override() public StringBuffer format(final Object obj, final StringBuffer toAppendTo, final FieldPosition pos) throws FractionConversionException, MathIllegalArgumentException {
    StringBuffer ret = null;
    if(obj instanceof Fraction) {
      ret = format((Fraction)obj, toAppendTo, pos);
    }
    else 
      if(obj instanceof Number) {
        ret = format(new Fraction(((Number)obj).doubleValue()), toAppendTo, pos);
      }
      else {
        throw new MathIllegalArgumentException(LocalizedFormats.CANNOT_FORMAT_OBJECT_TO_FRACTION);
      }
    return ret;
  }
  public StringBuffer format(final Fraction fraction, final StringBuffer toAppendTo, final FieldPosition pos) {
    pos.setBeginIndex(0);
    pos.setEndIndex(0);
    getNumeratorFormat().format(fraction.getNumerator(), toAppendTo, pos);
    toAppendTo.append(" / ");
    getDenominatorFormat().format(fraction.getDenominator(), toAppendTo, pos);
    return toAppendTo;
  }
}