package org.apache.commons.math3.fraction;
import java.math.BigInteger;
import java.text.FieldPosition;
import java.text.NumberFormat;
import java.text.ParsePosition;
import org.apache.commons.math3.exception.util.LocalizedFormats;
import org.apache.commons.math3.exception.NullArgumentException;

public class ProperBigFractionFormat extends BigFractionFormat  {
  final private static long serialVersionUID = -6337346779577272307L;
  private NumberFormat wholeFormat;
  public ProperBigFractionFormat() {
    this(getDefaultNumberFormat());
  }
  public ProperBigFractionFormat(final NumberFormat format) {
    this(format, (NumberFormat)format.clone(), (NumberFormat)format.clone());
  }
  public ProperBigFractionFormat(final NumberFormat wholeFormat, final NumberFormat numeratorFormat, final NumberFormat denominatorFormat) {
    super(numeratorFormat, denominatorFormat);
    setWholeFormat(wholeFormat);
  }
  @Override() public BigFraction parse(final String source, final ParsePosition pos) {
    BigFraction ret = super.parse(source, pos);
    if(ret != null) {
      return ret;
    }
    final int initialIndex = pos.getIndex();
    parseAndIgnoreWhitespace(source, pos);
    BigInteger whole = parseNextBigInteger(source, pos);
    if(whole == null) {
      pos.setIndex(initialIndex);
      return null;
    }
    parseAndIgnoreWhitespace(source, pos);
    BigInteger num = parseNextBigInteger(source, pos);
    if(num == null) {
      pos.setIndex(initialIndex);
      return null;
    }
    if(num.compareTo(BigInteger.ZERO) < 0) {
      pos.setIndex(initialIndex);
      return null;
    }
    int var_1062 = pos.getIndex();
    final int startIndex = var_1062;
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
    final BigInteger den = parseNextBigInteger(source, pos);
    if(den == null) {
      pos.setIndex(initialIndex);
      return null;
    }
    if(den.compareTo(BigInteger.ZERO) < 0) {
      pos.setIndex(initialIndex);
      return null;
    }
    boolean wholeIsNeg = whole.compareTo(BigInteger.ZERO) < 0;
    if(wholeIsNeg) {
      whole = whole.negate();
    }
    num = whole.multiply(den).add(num);
    if(wholeIsNeg) {
      num = num.negate();
    }
    return new BigFraction(num, den);
  }
  public NumberFormat getWholeFormat() {
    return wholeFormat;
  }
  @Override() public StringBuffer format(final BigFraction fraction, final StringBuffer toAppendTo, final FieldPosition pos) {
    pos.setBeginIndex(0);
    pos.setEndIndex(0);
    BigInteger num = fraction.getNumerator();
    BigInteger den = fraction.getDenominator();
    BigInteger whole = num.divide(den);
    num = num.remainder(den);
    if(!BigInteger.ZERO.equals(whole)) {
      getWholeFormat().format(whole, toAppendTo, pos);
      toAppendTo.append(' ');
      if(num.compareTo(BigInteger.ZERO) < 0) {
        num = num.negate();
      }
    }
    getNumeratorFormat().format(num, toAppendTo, pos);
    toAppendTo.append(" / ");
    getDenominatorFormat().format(den, toAppendTo, pos);
    return toAppendTo;
  }
  public void setWholeFormat(final NumberFormat format) {
    if(format == null) {
      throw new NullArgumentException(LocalizedFormats.WHOLE_FORMAT);
    }
    this.wholeFormat = format;
  }
}