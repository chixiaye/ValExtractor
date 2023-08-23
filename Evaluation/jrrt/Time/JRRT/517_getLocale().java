package org.joda.time.format;
import java.io.IOException;
import java.io.Writer;
import java.util.Locale;
import org.joda.time.MutablePeriod;
import org.joda.time.Period;
import org.joda.time.PeriodType;
import org.joda.time.ReadWritablePeriod;
import org.joda.time.ReadablePeriod;

public class PeriodFormatter  {
  final private PeriodPrinter iPrinter;
  final private PeriodParser iParser;
  final private Locale iLocale;
  final private PeriodType iParseType;
  public PeriodFormatter(PeriodPrinter printer, PeriodParser parser) {
    super();
    iPrinter = printer;
    iParser = parser;
    iLocale = null;
    iParseType = null;
  }
  private PeriodFormatter(PeriodPrinter printer, PeriodParser parser, Locale locale, PeriodType type) {
    super();
    iPrinter = printer;
    iParser = parser;
    iLocale = locale;
    iParseType = type;
  }
  public Locale getLocale() {
    return iLocale;
  }
  public MutablePeriod parseMutablePeriod(String text) {
    checkParser();
    MutablePeriod period = new MutablePeriod(0, iParseType);
    int newPos = getParser().parseInto(period, text, 0, iLocale);
    if(newPos >= 0) {
      if(newPos >= text.length()) {
        return period;
      }
    }
    else {
      newPos = ~newPos;
    }
    throw new IllegalArgumentException(FormatUtils.createErrorMessage(text, newPos));
  }
  public Period parsePeriod(String text) {
    checkParser();
    return parseMutablePeriod(text).toPeriod();
  }
  public PeriodFormatter withLocale(Locale locale) {
    Locale var_517 = getLocale();
    if(locale == var_517 || (locale != null && locale.equals(getLocale()))) {
      return this;
    }
    return new PeriodFormatter(iPrinter, iParser, locale, iParseType);
  }
  public PeriodFormatter withParseType(PeriodType type) {
    if(type == iParseType) {
      return this;
    }
    return new PeriodFormatter(iPrinter, iParser, iLocale, type);
  }
  public PeriodParser getParser() {
    return iParser;
  }
  public PeriodPrinter getPrinter() {
    return iPrinter;
  }
  public PeriodType getParseType() {
    return iParseType;
  }
  public String print(ReadablePeriod period) {
    checkPrinter();
    checkPeriod(period);
    PeriodPrinter printer = getPrinter();
    StringBuffer buf = new StringBuffer(printer.calculatePrintedLength(period, iLocale));
    printer.printTo(buf, period, iLocale);
    return buf.toString();
  }
  public boolean isParser() {
    return (iParser != null);
  }
  public boolean isPrinter() {
    return (iPrinter != null);
  }
  public int parseInto(ReadWritablePeriod period, String text, int position) {
    checkParser();
    checkPeriod(period);
    return getParser().parseInto(period, text, position, iLocale);
  }
  private void checkParser() {
    if(iParser == null) {
      throw new UnsupportedOperationException("Parsing not supported");
    }
  }
  private void checkPeriod(ReadablePeriod period) {
    if(period == null) {
      throw new IllegalArgumentException("Period must not be null");
    }
  }
  private void checkPrinter() {
    if(iPrinter == null) {
      throw new UnsupportedOperationException("Printing not supported");
    }
  }
  public void printTo(Writer out, ReadablePeriod period) throws IOException {
    checkPrinter();
    checkPeriod(period);
    getPrinter().printTo(out, period, iLocale);
  }
  public void printTo(StringBuffer buf, ReadablePeriod period) {
    checkPrinter();
    checkPeriod(period);
    getPrinter().printTo(buf, period, iLocale);
  }
}