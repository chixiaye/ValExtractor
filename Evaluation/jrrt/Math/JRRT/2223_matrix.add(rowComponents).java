package org.apache.commons.math3.linear;
import java.text.FieldPosition;
import java.text.NumberFormat;
import java.text.ParsePosition;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.apache.commons.math3.exception.MathParseException;
import org.apache.commons.math3.util.CompositeFormat;

public class RealMatrixFormat  {
  final private static String DEFAULT_PREFIX = "{";
  final private static String DEFAULT_SUFFIX = "}";
  final private static String DEFAULT_ROW_PREFIX = "{";
  final private static String DEFAULT_ROW_SUFFIX = "}";
  final private static String DEFAULT_ROW_SEPARATOR = ",";
  final private static String DEFAULT_COLUMN_SEPARATOR = ",";
  final private String prefix;
  final private String suffix;
  final private String rowPrefix;
  final private String rowSuffix;
  final private String rowSeparator;
  final private String columnSeparator;
  final private NumberFormat format;
  public RealMatrixFormat() {
    this(DEFAULT_PREFIX, DEFAULT_SUFFIX, DEFAULT_ROW_PREFIX, DEFAULT_ROW_SUFFIX, DEFAULT_ROW_SEPARATOR, DEFAULT_COLUMN_SEPARATOR, CompositeFormat.getDefaultNumberFormat());
  }
  public RealMatrixFormat(final NumberFormat format) {
    this(DEFAULT_PREFIX, DEFAULT_SUFFIX, DEFAULT_ROW_PREFIX, DEFAULT_ROW_SUFFIX, DEFAULT_ROW_SEPARATOR, DEFAULT_COLUMN_SEPARATOR, format);
  }
  public RealMatrixFormat(final String prefix, final String suffix, final String rowPrefix, final String rowSuffix, final String rowSeparator, final String columnSeparator) {
    this(prefix, suffix, rowPrefix, rowSuffix, rowSeparator, columnSeparator, CompositeFormat.getDefaultNumberFormat());
  }
  public RealMatrixFormat(final String prefix, final String suffix, final String rowPrefix, final String rowSuffix, final String rowSeparator, final String columnSeparator, final NumberFormat format) {
    super();
    this.prefix = prefix;
    this.suffix = suffix;
    this.rowPrefix = rowPrefix;
    this.rowSuffix = rowSuffix;
    this.rowSeparator = rowSeparator;
    this.columnSeparator = columnSeparator;
    this.format = format;
    this.format.setGroupingUsed(false);
  }
  public static Locale[] getAvailableLocales() {
    return NumberFormat.getAvailableLocales();
  }
  public NumberFormat getFormat() {
    return format;
  }
  public RealMatrix parse(String source) {
    final ParsePosition parsePosition = new ParsePosition(0);
    final RealMatrix result = parse(source, parsePosition);
    if(parsePosition.getIndex() == 0) {
      throw new MathParseException(source, parsePosition.getErrorIndex(), Array2DRowRealMatrix.class);
    }
    return result;
  }
  public RealMatrix parse(String source, ParsePosition pos) {
    int initialIndex = pos.getIndex();
    final String trimmedPrefix = prefix.trim();
    final String trimmedSuffix = suffix.trim();
    final String trimmedRowPrefix = rowPrefix.trim();
    final String trimmedRowSuffix = rowSuffix.trim();
    final String trimmedColumnSeparator = columnSeparator.trim();
    final String trimmedRowSeparator = rowSeparator.trim();
    CompositeFormat.parseAndIgnoreWhitespace(source, pos);
    if(!CompositeFormat.parseFixedstring(source, trimmedPrefix, pos)) {
      return null;
    }
    List<List<Number>> matrix = new ArrayList<List<Number>>();
    List<Number> rowComponents = new ArrayList<Number>();
    for(boolean loop = true; loop; ) {
      if(!rowComponents.isEmpty()) {
        CompositeFormat.parseAndIgnoreWhitespace(source, pos);
        if(!CompositeFormat.parseFixedstring(source, trimmedColumnSeparator, pos)) {
          if(trimmedRowSuffix.length() != 0 && !CompositeFormat.parseFixedstring(source, trimmedRowSuffix, pos)) {
            return null;
          }
          else {
            CompositeFormat.parseAndIgnoreWhitespace(source, pos);
            if(CompositeFormat.parseFixedstring(source, trimmedRowSeparator, pos)) {
              matrix.add(rowComponents);
              rowComponents = new ArrayList<Number>();
              continue ;
            }
            else {
              loop = false;
            }
          }
        }
      }
      else {
        CompositeFormat.parseAndIgnoreWhitespace(source, pos);
        if(trimmedRowPrefix.length() != 0 && !CompositeFormat.parseFixedstring(source, trimmedRowPrefix, pos)) {
          return null;
        }
      }
      if(loop) {
        CompositeFormat.parseAndIgnoreWhitespace(source, pos);
        Number component = CompositeFormat.parseNumber(source, format, pos);
        if(component != null) {
          rowComponents.add(component);
        }
        else {
          if(rowComponents.isEmpty()) {
            loop = false;
          }
          else {
            pos.setIndex(initialIndex);
            return null;
          }
        }
      }
    }
    if(!rowComponents.isEmpty()) {
      boolean var_2223 = matrix.add(rowComponents);
    }
    CompositeFormat.parseAndIgnoreWhitespace(source, pos);
    if(!CompositeFormat.parseFixedstring(source, trimmedSuffix, pos)) {
      return null;
    }
    if(matrix.isEmpty()) {
      pos.setIndex(initialIndex);
      return null;
    }
    double[][] data = new double[matrix.size()][];
    int row = 0;
    for (List<Number> rowList : matrix) {
      data[row] = new double[rowList.size()];
      for(int i = 0; i < rowList.size(); i++) {
        data[row][i] = rowList.get(i).doubleValue();
      }
      row++;
    }
    return MatrixUtils.createRealMatrix(data);
  }
  public static RealMatrixFormat getInstance() {
    return getInstance(Locale.getDefault());
  }
  public static RealMatrixFormat getInstance(final Locale locale) {
    return new RealMatrixFormat(CompositeFormat.getDefaultNumberFormat(locale));
  }
  public String format(RealMatrix m) {
    return format(m, new StringBuffer(), new FieldPosition(0)).toString();
  }
  public String getColumnSeparator() {
    return columnSeparator;
  }
  public String getPrefix() {
    return prefix;
  }
  public String getRowPrefix() {
    return rowPrefix;
  }
  public String getRowSeparator() {
    return rowSeparator;
  }
  public String getRowSuffix() {
    return rowSuffix;
  }
  public String getSuffix() {
    return suffix;
  }
  public StringBuffer format(RealMatrix matrix, StringBuffer toAppendTo, FieldPosition pos) {
    pos.setBeginIndex(0);
    pos.setEndIndex(0);
    toAppendTo.append(prefix);
    final int rows = matrix.getRowDimension();
    for(int i = 0; i < rows; ++i) {
      toAppendTo.append(rowPrefix);
      for(int j = 0; j < matrix.getColumnDimension(); ++j) {
        if(j > 0) {
          toAppendTo.append(columnSeparator);
        }
        CompositeFormat.formatDouble(matrix.getEntry(i, j), format, toAppendTo, pos);
      }
      toAppendTo.append(rowSuffix);
      if(i < rows - 1) {
        toAppendTo.append(rowSeparator);
      }
    }
    toAppendTo.append(suffix);
    return toAppendTo;
  }
}