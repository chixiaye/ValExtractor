package org.apache.commons.math3.linear;
import java.io.Serializable;
import org.apache.commons.math3.exception.DimensionMismatchException;
import org.apache.commons.math3.exception.NotStrictlyPositiveException;
import org.apache.commons.math3.exception.NumberIsTooLargeException;
import org.apache.commons.math3.exception.OutOfRangeException;
import org.apache.commons.math3.util.OpenIntToDoubleHashMap;

@Deprecated() public class OpenMapRealMatrix extends AbstractRealMatrix implements SparseRealMatrix, Serializable  {
  final private static long serialVersionUID = -5962461716457143437L;
  final private int rows;
  final private int columns;
  final private OpenIntToDoubleHashMap entries;
  public OpenMapRealMatrix(OpenMapRealMatrix matrix) {
    super();
    this.rows = matrix.rows;
    this.columns = matrix.columns;
    this.entries = new OpenIntToDoubleHashMap(matrix.entries);
  }
  public OpenMapRealMatrix(int rowDimension, int columnDimension) throws NotStrictlyPositiveException, NumberIsTooLargeException {
    super(rowDimension, columnDimension);
    long lRow = rowDimension;
    long lCol = columnDimension;
    int var_2510 = Integer.MAX_VALUE;
    if(lRow * lCol >= var_2510) {
      throw new NumberIsTooLargeException(lRow * lCol, Integer.MAX_VALUE, false);
    }
    this.rows = rowDimension;
    this.columns = columnDimension;
    this.entries = new OpenIntToDoubleHashMap(0.0D);
  }
  public OpenMapRealMatrix add(OpenMapRealMatrix m) throws MatrixDimensionMismatchException {
    MatrixUtils.checkAdditionCompatible(this, m);
    final OpenMapRealMatrix out = new OpenMapRealMatrix(this);
    for(org.apache.commons.math3.util.OpenIntToDoubleHashMap.Iterator iterator = m.entries.iterator(); iterator.hasNext(); ) {
      iterator.advance();
      final int row = iterator.key() / columns;
      final int col = iterator.key() - row * columns;
      out.setEntry(row, col, getEntry(row, col) + iterator.value());
    }
    return out;
  }
  @Override() public OpenMapRealMatrix copy() {
    return new OpenMapRealMatrix(this);
  }
  @Override() public OpenMapRealMatrix createMatrix(int rowDimension, int columnDimension) throws NotStrictlyPositiveException, NumberIsTooLargeException {
    return new OpenMapRealMatrix(rowDimension, columnDimension);
  }
  public OpenMapRealMatrix multiply(OpenMapRealMatrix m) throws DimensionMismatchException, NumberIsTooLargeException {
    MatrixUtils.checkMultiplicationCompatible(this, m);
    final int outCols = m.getColumnDimension();
    OpenMapRealMatrix out = new OpenMapRealMatrix(rows, outCols);
    for(org.apache.commons.math3.util.OpenIntToDoubleHashMap.Iterator iterator = entries.iterator(); iterator.hasNext(); ) {
      iterator.advance();
      final double value = iterator.value();
      final int key = iterator.key();
      final int i = key / columns;
      final int k = key % columns;
      for(int j = 0; j < outCols; ++j) {
        final int rightKey = m.computeKey(k, j);
        if(m.entries.containsKey(rightKey)) {
          final int outKey = out.computeKey(i, j);
          final double outValue = out.entries.get(outKey) + value * m.entries.get(rightKey);
          if(outValue == 0.0D) {
            out.entries.remove(outKey);
          }
          else {
            out.entries.put(outKey, outValue);
          }
        }
      }
    }
    return out;
  }
  public OpenMapRealMatrix subtract(OpenMapRealMatrix m) throws MatrixDimensionMismatchException {
    MatrixUtils.checkAdditionCompatible(this, m);
    final OpenMapRealMatrix out = new OpenMapRealMatrix(this);
    for(org.apache.commons.math3.util.OpenIntToDoubleHashMap.Iterator iterator = m.entries.iterator(); iterator.hasNext(); ) {
      iterator.advance();
      final int row = iterator.key() / columns;
      final int col = iterator.key() - row * columns;
      out.setEntry(row, col, getEntry(row, col) - iterator.value());
    }
    return out;
  }
  @Override() public OpenMapRealMatrix subtract(final RealMatrix m) throws MatrixDimensionMismatchException {
    try {
      return subtract((OpenMapRealMatrix)m);
    }
    catch (ClassCastException cce) {
      return (OpenMapRealMatrix)super.subtract(m);
    }
  }
  @Override() public RealMatrix multiply(final RealMatrix m) throws DimensionMismatchException, NumberIsTooLargeException {
    try {
      return multiply((OpenMapRealMatrix)m);
    }
    catch (ClassCastException cce) {
      MatrixUtils.checkMultiplicationCompatible(this, m);
      final int outCols = m.getColumnDimension();
      final BlockRealMatrix out = new BlockRealMatrix(rows, outCols);
      for(org.apache.commons.math3.util.OpenIntToDoubleHashMap.Iterator iterator = entries.iterator(); iterator.hasNext(); ) {
        iterator.advance();
        final double value = iterator.value();
        final int key = iterator.key();
        final int i = key / columns;
        final int k = key % columns;
        for(int j = 0; j < outCols; ++j) {
          out.addToEntry(i, j, value * m.getEntry(k, j));
        }
      }
      return out;
    }
  }
  @Override() public double getEntry(int row, int column) throws OutOfRangeException {
    MatrixUtils.checkRowIndex(this, row);
    MatrixUtils.checkColumnIndex(this, column);
    return entries.get(computeKey(row, column));
  }
  private int computeKey(int row, int column) {
    return row * columns + column;
  }
  @Override() public int getColumnDimension() {
    return columns;
  }
  @Override() public int getRowDimension() {
    return rows;
  }
  @Override() public void addToEntry(int row, int column, double increment) throws OutOfRangeException {
    MatrixUtils.checkRowIndex(this, row);
    MatrixUtils.checkColumnIndex(this, column);
    final int key = computeKey(row, column);
    final double value = entries.get(key) + increment;
    if(value == 0.0D) {
      entries.remove(key);
    }
    else {
      entries.put(key, value);
    }
  }
  @Override() public void multiplyEntry(int row, int column, double factor) throws OutOfRangeException {
    MatrixUtils.checkRowIndex(this, row);
    MatrixUtils.checkColumnIndex(this, column);
    final int key = computeKey(row, column);
    final double value = entries.get(key) * factor;
    if(value == 0.0D) {
      entries.remove(key);
    }
    else {
      entries.put(key, value);
    }
  }
  @Override() public void setEntry(int row, int column, double value) throws OutOfRangeException {
    MatrixUtils.checkRowIndex(this, row);
    MatrixUtils.checkColumnIndex(this, column);
    if(value == 0.0D) {
      entries.remove(computeKey(row, column));
    }
    else {
      entries.put(computeKey(row, column), value);
    }
  }
}