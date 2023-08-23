package org.apache.commons.math3.complex;
import java.text.FieldPosition;
import java.text.NumberFormat;
import java.text.ParsePosition;
import java.util.Locale;
import org.apache.commons.math3.exception.MathIllegalArgumentException;
import org.apache.commons.math3.exception.MathParseException;
import org.apache.commons.math3.exception.NoDataException;
import org.apache.commons.math3.exception.NullArgumentException;
import org.apache.commons.math3.exception.util.LocalizedFormats;
import org.apache.commons.math3.util.CompositeFormat;

public class ComplexFormat  {
  final private static String DEFAULT_IMAGINARY_CHARACTER = "i";
  final private String imaginaryCharacter;
  final private NumberFormat imaginaryFormat;
  final private NumberFormat realFormat;
  public ComplexFormat() {
    super();
    this.imaginaryCharacter = DEFAULT_IMAGINARY_CHARACTER;
    this.imaginaryFormat = CompositeFormat.getDefaultNumberFormat();
    this.realFormat = imaginaryFormat;
  }
  public ComplexFormat(NumberFormat format) throws NullArgumentException {
    super();
    if(format == null) {
      throw new NullArgumentException(LocalizedFormats.IMAGINARY_FORMAT);
    }
    this.imaginaryCharacter = DEFAULT_IMAGINARY_CHARACTER;
    this.imaginaryFormat = format;
    this.realFormat = format;
  }
  public ComplexFormat(NumberFormat realFormat, NumberFormat imaginaryFormat) throws NullArgumentException {
    super();
    if(imaginaryFormat == null) {
      throw new NullArgumentException(LocalizedFormats.IMAGINARY_FORMAT);
    }
    if(realFormat == null) {
      throw new NullArgumentException(LocalizedFormats.REAL_FORMAT);
    }
    this.imaginaryCharacter = DEFAULT_IMAGINARY_CHARACTER;
    this.imaginaryFormat = imaginaryFormat;
    this.realFormat = realFormat;
  }
  public ComplexFormat(String imaginaryCharacter) throws NullArgumentException, NoDataException {
    this(imaginaryCharacter, CompositeFormat.getDefaultNumberFormat());
  }
  public ComplexFormat(String imaginaryCharacter, NumberFormat format) throws NullArgumentException, NoDataException {
    this(imaginaryCharacter, format, format);
  }
  public ComplexFormat(String imaginaryCharacter, NumberFormat realFormat, NumberFormat imaginaryFormat) throws NullArgumentException, NoDataException {
    super();
    if(imaginaryCharacter == null) {
      throw new NullArgumentException();
    }
    if(imaginaryCharacter.length() == 0) {
      throw new NoDataException();
    }
    if(imaginaryFormat == null) {
      throw new NullArgumentException(LocalizedFormats.IMAGINARY_FORMAT);
    }
    if(realFormat == null) {
      throw new NullArgumentException(LocalizedFormats.REAL_FORMAT);
    }
    this.imaginaryCharacter = imaginaryCharacter;
    this.imaginaryFormat = imaginaryFormat;
    this.realFormat = realFormat;
  }
  public Complex parse(String source) throws MathParseException {
    ParsePosition parsePosition = new ParsePosition(0);
    Complex result = parse(source, parsePosition);
    if(parsePosition.getIndex() == 0) {
      throw new MathParseException(source, parsePosition.getErrorIndex(), Complex.class);
    }
    return result;
  }
  public Complex parse(String source, ParsePosition pos) {
    int initialIndex = pos.getIndex();
    CompositeFormat.parseAndIgnoreWhitespace(source, pos);
    Number re = CompositeFormat.parseNumber(source, getRealFormat(), pos);
    if(re == null) {
      pos.setIndex(initialIndex);
      return null;
    }
    int startIndex = pos.getIndex();
    char c = CompositeFormat.parseNextCharacter(source, pos);
    int sign = 0;
    switch (c){
      case 0:
      return new Complex(re.doubleValue(), 0.0D);
      case '-':
      sign = -1;
      break ;
      case '+':
      sign = 1;
      break ;
      default:
      pos.setIndex(initialIndex);
      pos.setErrorIndex(startIndex);
      return null;
    }
    CompositeFormat.parseAndIgnoreWhitespace(source, pos);
    Number im = CompositeFormat.parseNumber(source, getRealFormat(), pos);
    if(im == null) {
      pos.setIndex(initialIndex);
      return null;
    }
    if(!CompositeFormat.parseFixedstring(source, getImaginaryCharacter(), pos)) {
      return null;
    }
    return new Complex(re.doubleValue(), im.doubleValue() * sign);
  }
  public static ComplexFormat getInstance() {
    return getInstance(Locale.getDefault());
  }
  public static ComplexFormat getInstance(String imaginaryCharacter, Locale locale) throws NullArgumentException, NoDataException {
    NumberFormat f = CompositeFormat.getDefaultNumberFormat(locale);
    return new ComplexFormat(imaginaryCharacter, f);
  }
  public static ComplexFormat getInstance(Locale locale) {
    NumberFormat f = CompositeFormat.getDefaultNumberFormat(locale);
    return new ComplexFormat(f);
  }
  public static Locale[] getAvailableLocales() {
    return NumberFormat.getAvailableLocales();
  }
  public NumberFormat getImaginaryFormat() {
    return imaginaryFormat;
  }
  public NumberFormat getRealFormat() {
    return realFormat;
  }
  public String format(Double c) {
    return format(new Complex(c, 0), new StringBuffer(), new FieldPosition(0)).toString();
  }
  public String format(Complex c) {
    return format(c, new StringBuffer(), new FieldPosition(0)).toString();
  }
  public String getImaginaryCharacter() {
    return imaginaryCharacter;
  }
  public StringBuffer format(Object obj, StringBuffer toAppendTo, FieldPosition pos) throws MathIllegalArgumentException {
    StringBuffer ret = null;
    if(obj instanceof Complex) {
      ret = format((Complex)obj, toAppendTo, pos);
    }
    else 
      if(obj instanceof Number) {
        ret = format(new Complex(((Number)obj).doubleValue(), 0.0D), toAppendTo, pos);
      }
      else {
        throw new MathIllegalArgumentException(LocalizedFormats.CANNOT_FORMAT_INSTANCE_AS_COMPLEX, obj.getClass().getName());
      }
    return ret;
  }
  public StringBuffer format(Complex complex, StringBuffer toAppendTo, FieldPosition pos) {
    pos.setBeginIndex(0);
    pos.setEndIndex(0);
    double re = complex.getReal();
    CompositeFormat.formatDouble(re, getRealFormat(), toAppendTo, pos);
    double im = complex.getImaginary();
    StringBuffer imAppendTo;
    if(im < 0.0D) {
      toAppendTo.append(" - ");
      imAppendTo = formatImaginary(-im, new StringBuffer(), pos);
      toAppendTo.append(imAppendTo);
      toAppendTo.append(getImaginaryCharacter());
    }
    else 
      if(im > 0.0D || Double.isNaN(im)) {
        toAppendTo.append(" + ");
        imAppendTo = formatImaginary(im, new StringBuffer(), pos);
        toAppendTo.append(imAppendTo);
        String var_531 = getImaginaryCharacter();
        toAppendTo.append(var_531);
      }
    return toAppendTo;
  }
  private StringBuffer formatImaginary(double absIm, StringBuffer toAppendTo, FieldPosition pos) {
    pos.setBeginIndex(0);
    pos.setEndIndex(0);
    CompositeFormat.formatDouble(absIm, getImaginaryFormat(), toAppendTo, pos);
    if(toAppendTo.toString().equals("1")) {
      toAppendTo.setLength(0);
    }
    return toAppendTo;
  }
}