package org.apache.commons.math3.linear;
import java.util.ArrayList;
import org.apache.commons.math3.Field;
import org.apache.commons.math3.FieldElement;
import org.apache.commons.math3.exception.DimensionMismatchException;
import org.apache.commons.math3.exception.NoDataException;
import org.apache.commons.math3.exception.NotPositiveException;
import org.apache.commons.math3.exception.NotStrictlyPositiveException;
import org.apache.commons.math3.exception.NullArgumentException;
import org.apache.commons.math3.exception.NumberIsTooSmallException;
import org.apache.commons.math3.exception.OutOfRangeException;
import org.apache.commons.math3.exception.util.LocalizedFormats;
import org.apache.commons.math3.util.MathArrays;
abstract public class AbstractFieldMatrix<T extends org.apache.commons.math3.FieldElement<org.apache.commons.math3.linear.AbstractFieldMatrix@T>> implements FieldMatrix<T>  {
  final private Field<T> field;
  protected AbstractFieldMatrix() {
    super();
    field = null;
  }
  protected AbstractFieldMatrix(final Field<T> field) {
    super();
    this.field = field;
  }
  protected AbstractFieldMatrix(final Field<T> field, final int rowDimension, final int columnDimension) throws NotStrictlyPositiveException {
    super();
    if(rowDimension <= 0) {
      throw new NotStrictlyPositiveException(LocalizedFormats.DIMENSION, rowDimension);
    }
    if(columnDimension <= 0) {
      throw new NotStrictlyPositiveException(LocalizedFormats.DIMENSION, columnDimension);
    }
    this.field = field;
  }
  protected static  <T extends org.apache.commons.math3.FieldElement<org.apache.commons.math3.linear.T>> Field<T> extractField(final T[] d) throws NoDataException {
    if(d.length == 0) {
      throw new NoDataException(LocalizedFormats.AT_LEAST_ONE_ROW);
    }
    return d[0].getField();
  }
  protected static  <T extends org.apache.commons.math3.FieldElement<org.apache.commons.math3.linear.T>> Field<T> extractField(final T[][] d) throws NoDataException, NullArgumentException {
    if(d == null) {
      throw new NullArgumentException();
    }
    if(d.length == 0) {
      throw new NoDataException(LocalizedFormats.AT_LEAST_ONE_ROW);
    }
    if(d[0].length == 0) {
      throw new NoDataException(LocalizedFormats.AT_LEAST_ONE_COLUMN);
    }
    return d[0][0].getField();
  }
  public Field<T> getField() {
    return field;
  }
  public FieldMatrix<T> add(FieldMatrix<T> m) throws MatrixDimensionMismatchException {
    checkAdditionCompatible(m);
    final int rowCount = getRowDimension();
    final int columnCount = getColumnDimension();
    final FieldMatrix<T> out = createMatrix(rowCount, columnCount);
    for(int row = 0; row < rowCount; ++row) {
      for(int col = 0; col < columnCount; ++col) {
        out.setEntry(row, col, getEntry(row, col).add(m.getEntry(row, col)));
      }
    }
    return out;
  }
  abstract public FieldMatrix<T> copy();
  abstract public FieldMatrix<T> createMatrix(final int rowDimension, final int columnDimension) throws NotStrictlyPositiveException;
  public FieldMatrix<T> getColumnMatrix(final int column) throws OutOfRangeException {
    checkColumnIndex(column);
    final int nRows = getRowDimension();
    final FieldMatrix<T> out = createMatrix(nRows, 1);
    for(int i = 0; i < nRows; ++i) {
      out.setEntry(i, 0, getEntry(i, column));
    }
    return out;
  }
  public FieldMatrix<T> getRowMatrix(final int row) throws OutOfRangeException {
    checkRowIndex(row);
    final int nCols = getColumnDimension();
    final FieldMatrix<T> out = createMatrix(1, nCols);
    for(int i = 0; i < nCols; ++i) {
      out.setEntry(0, i, getEntry(row, i));
    }
    return out;
  }
  public FieldMatrix<T> getSubMatrix(final int startRow, final int endRow, final int startColumn, final int endColumn) throws NumberIsTooSmallException, OutOfRangeException {
    checkSubMatrixIndex(startRow, endRow, startColumn, endColumn);
    final FieldMatrix<T> subMatrix = createMatrix(endRow - startRow + 1, endColumn - startColumn + 1);
    for(int i = startRow; i <= endRow; ++i) {
      for(int j = startColumn; j <= endColumn; ++j) {
        subMatrix.setEntry(i - startRow, j - startColumn, getEntry(i, j));
      }
    }
    return subMatrix;
  }
  public FieldMatrix<T> getSubMatrix(final int[] selectedRows, final int[] selectedColumns) throws NoDataException, NullArgumentException, OutOfRangeException {
    checkSubMatrixIndex(selectedRows, selectedColumns);
    final FieldMatrix<T> subMatrix = createMatrix(selectedRows.length, selectedColumns.length);
    subMatrix.walkInOptimizedOrder(new DefaultFieldMatrixChangingVisitor<T>(field.getZero()) {
        @Override() public T visit(final int row, final int column, final T value) {
          return getEntry(selectedRows[row], selectedColumns[column]);
        }
    });
    return subMatrix;
  }
  public FieldMatrix<T> multiply(final FieldMatrix<T> m) throws DimensionMismatchException {
    checkMultiplicationCompatible(m);
    final int nRows = getRowDimension();
    final int nCols = m.getColumnDimension();
    final int nSum = getColumnDimension();
    final FieldMatrix<T> out = createMatrix(nRows, nCols);
    for(int row = 0; row < nRows; ++row) {
      for(int col = 0; col < nCols; ++col) {
        T sum = field.getZero();
        for(int i = 0; i < nSum; ++i) {
          sum = sum.add(getEntry(row, i).multiply(m.getEntry(i, col)));
        }
        out.setEntry(row, col, sum);
      }
    }
    return out;
  }
  public FieldMatrix<T> power(final int p) throws NonSquareMatrixException, NotPositiveException {
    if(p < 0) {
      throw new NotPositiveException(p);
    }
    if(!isSquare()) {
      throw new NonSquareMatrixException(getRowDimension(), getColumnDimension());
    }
    if(p == 0) {
      return MatrixUtils.createFieldIdentityMatrix(this.getField(), this.getRowDimension());
    }
    if(p == 1) {
      return this.copy();
    }
    final int power = p - 1;
    final char[] binaryRepresentation = Integer.toBinaryString(power).toCharArray();
    final ArrayList<Integer> nonZeroPositions = new ArrayList<Integer>();
    for(int i = 0; i < binaryRepresentation.length; ++i) {
      if(binaryRepresentation[i] == '1') {
        final int pos = binaryRepresentation.length - i - 1;
        nonZeroPositions.add(pos);
      }
    }
    ArrayList<FieldMatrix<T>> results = new ArrayList<FieldMatrix<T>>(binaryRepresentation.length);
    results.add(0, this.copy());
    for(int i = 1; i < binaryRepresentation.length; ++i) {
      final FieldMatrix<T> s = results.get(i - 1);
      final FieldMatrix<T> r = s.multiply(s);
      results.add(i, r);
    }
    FieldMatrix<T> result = this.copy();
    for (Integer i : nonZeroPositions) {
      result = result.multiply(results.get(i));
    }
    return result;
  }
  public FieldMatrix<T> preMultiply(final FieldMatrix<T> m) throws DimensionMismatchException {
    return m.multiply(this);
  }
  public FieldMatrix<T> scalarAdd(final T d) {
    final int rowCount = getRowDimension();
    final int columnCount = getColumnDimension();
    final FieldMatrix<T> out = createMatrix(rowCount, columnCount);
    for(int row = 0; row < rowCount; ++row) {
      for(int col = 0; col < columnCount; ++col) {
        out.setEntry(row, col, getEntry(row, col).add(d));
      }
    }
    return out;
  }
  public FieldMatrix<T> scalarMultiply(final T d) {
    final int rowCount = getRowDimension();
    final int columnCount = getColumnDimension();
    final FieldMatrix<T> out = createMatrix(rowCount, columnCount);
    for(int row = 0; row < rowCount; ++row) {
      for(int col = 0; col < columnCount; ++col) {
        out.setEntry(row, col, getEntry(row, col).multiply(d));
      }
    }
    return out;
  }
  public FieldMatrix<T> subtract(final FieldMatrix<T> m) throws MatrixDimensionMismatchException {
    checkSubtractionCompatible(m);
    final int rowCount = getRowDimension();
    final int columnCount = getColumnDimension();
    final FieldMatrix<T> out = createMatrix(rowCount, columnCount);
    for(int row = 0; row < rowCount; ++row) {
      for(int col = 0; col < columnCount; ++col) {
        out.setEntry(row, col, getEntry(row, col).subtract(m.getEntry(row, col)));
      }
    }
    return out;
  }
  public FieldMatrix<T> transpose() {
    final int nRows = getRowDimension();
    final int nCols = getColumnDimension();
    final FieldMatrix<T> out = createMatrix(nCols, nRows);
    walkInOptimizedOrder(new DefaultFieldMatrixPreservingVisitor<T>(field.getZero()) {
        @Override() public void visit(final int row, final int column, final T value) {
          out.setEntry(column, row, value);
        }
    });
    return out;
  }
  public FieldVector<T> getColumnVector(final int column) throws OutOfRangeException {
    return new ArrayFieldVector<T>(field, getColumn(column), false);
  }
  public FieldVector<T> getRowVector(final int row) throws OutOfRangeException {
    return new ArrayFieldVector<T>(field, getRow(row), false);
  }
  public FieldVector<T> operate(final FieldVector<T> v) throws DimensionMismatchException {
    try {
      return new ArrayFieldVector<T>(field, operate(((ArrayFieldVector<T>)v).getDataRef()), false);
    }
    catch (ClassCastException cce) {
      final int nRows = getRowDimension();
      final int nCols = getColumnDimension();
      if(v.getDimension() != nCols) {
        throw new DimensionMismatchException(v.getDimension(), nCols);
      }
      final T[] out = MathArrays.buildArray(field, nRows);
      for(int row = 0; row < nRows; ++row) {
        T sum = field.getZero();
        for(int i = 0; i < nCols; ++i) {
          sum = sum.add(getEntry(row, i).multiply(v.getEntry(i)));
        }
        out[row] = sum;
      }
      return new ArrayFieldVector<T>(field, out, false);
    }
  }
  public FieldVector<T> preMultiply(final FieldVector<T> v) throws DimensionMismatchException {
    try {
      return new ArrayFieldVector<T>(field, preMultiply(((ArrayFieldVector<T>)v).getDataRef()), false);
    }
    catch (ClassCastException cce) {
      final int nRows = getRowDimension();
      final int nCols = getColumnDimension();
      if(v.getDimension() != nRows) {
        throw new DimensionMismatchException(v.getDimension(), nRows);
      }
      final T[] out = MathArrays.buildArray(field, nCols);
      for(int col = 0; col < nCols; ++col) {
        T sum = field.getZero();
        for(int i = 0; i < nRows; ++i) {
          sum = sum.add(getEntry(i, col).multiply(v.getEntry(i)));
        }
        out[col] = sum;
      }
      return new ArrayFieldVector<T>(field, out, false);
    }
  }
  @Override() public String toString() {
    final int nRows = getRowDimension();
    final int nCols = getColumnDimension();
    final StringBuffer res = new StringBuffer();
    String fullClassName = getClass().getName();
    String shortClassName = fullClassName.substring(fullClassName.lastIndexOf('.') + 1);
    res.append(shortClassName).append("{");
    for(int i = 0; i < nRows; ++i) {
      if(i > 0) {
        res.append(",");
      }
      res.append("{");
      for(int j = 0; j < nCols; ++j) {
        if(j > 0) {
          res.append(",");
        }
        res.append(getEntry(i, j));
      }
      res.append("}");
    }
    res.append("}");
    return res.toString();
  }
  abstract public T getEntry(int row, int column) throws OutOfRangeException;
  public T getTrace() throws NonSquareMatrixException {
    final int nRows = getRowDimension();
    final int nCols = getColumnDimension();
    if(nRows != nCols) {
      throw new NonSquareMatrixException(nRows, nCols);
    }
    T trace = field.getZero();
    for(int i = 0; i < nRows; ++i) {
      trace = trace.add(getEntry(i, i));
    }
    return trace;
  }
  public T walkInColumnOrder(final FieldMatrixChangingVisitor<T> visitor) {
    final int rows = getRowDimension();
    final int columns = getColumnDimension();
    visitor.start(rows, columns, 0, rows - 1, 0, columns - 1);
    for(int column = 0; column < columns; ++column) {
      for(int row = 0; row < rows; ++row) {
        final T oldValue = getEntry(row, column);
        final T newValue = visitor.visit(row, column, oldValue);
        setEntry(row, column, newValue);
      }
    }
    return visitor.end();
  }
  public T walkInColumnOrder(final FieldMatrixChangingVisitor<T> visitor, final int startRow, final int endRow, final int startColumn, final int endColumn) throws NumberIsTooSmallException, OutOfRangeException {
    checkSubMatrixIndex(startRow, endRow, startColumn, endColumn);
    visitor.start(getRowDimension(), getColumnDimension(), startRow, endRow, startColumn, endColumn);
    for(int column = startColumn; column <= endColumn; ++column) {
      for(int row = startRow; row <= endRow; ++row) {
        final T oldValue = getEntry(row, column);
        final T newValue = visitor.visit(row, column, oldValue);
        setEntry(row, column, newValue);
      }
    }
    return visitor.end();
  }
  public T walkInColumnOrder(final FieldMatrixPreservingVisitor<T> visitor) {
    final int rows = getRowDimension();
    final int columns = getColumnDimension();
    visitor.start(rows, columns, 0, rows - 1, 0, columns - 1);
    for(int column = 0; column < columns; ++column) {
      for(int row = 0; row < rows; ++row) {
        visitor.visit(row, column, getEntry(row, column));
      }
    }
    return visitor.end();
  }
  public T walkInColumnOrder(final FieldMatrixPreservingVisitor<T> visitor, final int startRow, final int endRow, final int startColumn, final int endColumn) throws NumberIsTooSmallException, OutOfRangeException {
    checkSubMatrixIndex(startRow, endRow, startColumn, endColumn);
    visitor.start(getRowDimension(), getColumnDimension(), startRow, endRow, startColumn, endColumn);
    for(int column = startColumn; column <= endColumn; ++column) {
      for(int row = startRow; row <= endRow; ++row) {
        visitor.visit(row, column, getEntry(row, column));
      }
    }
    return visitor.end();
  }
  public T walkInOptimizedOrder(final FieldMatrixChangingVisitor<T> visitor) {
    return walkInRowOrder(visitor);
  }
  public T walkInOptimizedOrder(final FieldMatrixChangingVisitor<T> visitor, final int startRow, final int endRow, final int startColumn, final int endColumn) throws NumberIsTooSmallException, OutOfRangeException {
    return walkInRowOrder(visitor, startRow, endRow, startColumn, endColumn);
  }
  public T walkInOptimizedOrder(final FieldMatrixPreservingVisitor<T> visitor) {
    return walkInRowOrder(visitor);
  }
  public T walkInOptimizedOrder(final FieldMatrixPreservingVisitor<T> visitor, final int startRow, final int endRow, final int startColumn, final int endColumn) throws NumberIsTooSmallException, OutOfRangeException {
    return walkInRowOrder(visitor, startRow, endRow, startColumn, endColumn);
  }
  public T walkInRowOrder(final FieldMatrixChangingVisitor<T> visitor) {
    final int rows = getRowDimension();
    final int columns = getColumnDimension();
    visitor.start(rows, columns, 0, rows - 1, 0, columns - 1);
    for(int row = 0; row < rows; ++row) {
      for(int column = 0; column < columns; ++column) {
        final T oldValue = getEntry(row, column);
        final T newValue = visitor.visit(row, column, oldValue);
        setEntry(row, column, newValue);
      }
    }
    return visitor.end();
  }
  public T walkInRowOrder(final FieldMatrixChangingVisitor<T> visitor, final int startRow, final int endRow, final int startColumn, final int endColumn) throws NumberIsTooSmallException, OutOfRangeException {
    checkSubMatrixIndex(startRow, endRow, startColumn, endColumn);
    visitor.start(getRowDimension(), getColumnDimension(), startRow, endRow, startColumn, endColumn);
    for(int row = startRow; row <= endRow; ++row) {
      for(int column = startColumn; column <= endColumn; ++column) {
        final T oldValue = getEntry(row, column);
        final T newValue = visitor.visit(row, column, oldValue);
        setEntry(row, column, newValue);
      }
    }
    return visitor.end();
  }
  public T walkInRowOrder(final FieldMatrixPreservingVisitor<T> visitor) {
    final int rows = getRowDimension();
    final int columns = getColumnDimension();
    visitor.start(rows, columns, 0, rows - 1, 0, columns - 1);
    for(int row = 0; row < rows; ++row) {
      for(int column = 0; column < columns; ++column) {
        visitor.visit(row, column, getEntry(row, column));
      }
    }
    return visitor.end();
  }
  public T walkInRowOrder(final FieldMatrixPreservingVisitor<T> visitor, final int startRow, final int endRow, final int startColumn, final int endColumn) throws NumberIsTooSmallException, OutOfRangeException {
    checkSubMatrixIndex(startRow, endRow, startColumn, endColumn);
    visitor.start(getRowDimension(), getColumnDimension(), startRow, endRow, startColumn, endColumn);
    for(int row = startRow; row <= endRow; ++row) {
      for(int column = startColumn; column <= endColumn; ++column) {
        visitor.visit(row, column, getEntry(row, column));
      }
    }
    return visitor.end();
  }
  @Deprecated() protected static  <T extends org.apache.commons.math3.FieldElement<org.apache.commons.math3.linear.T>> T[] buildArray(final Field<T> field, final int length) {
    return MathArrays.buildArray(field, length);
  }
  public T[] getColumn(final int column) throws OutOfRangeException {
    checkColumnIndex(column);
    final int nRows = getRowDimension();
    final T[] out = MathArrays.buildArray(field, nRows);
    for(int i = 0; i < nRows; ++i) {
      out[i] = getEntry(i, column);
    }
    return out;
  }
  public T[] getRow(final int row) throws OutOfRangeException {
    checkRowIndex(row);
    final int nCols = getColumnDimension();
    final T[] out = MathArrays.buildArray(field, nCols);
    for(int i = 0; i < nCols; ++i) {
      out[i] = getEntry(row, i);
    }
    return out;
  }
  public T[] operate(final T[] v) throws DimensionMismatchException {
    final int nRows = getRowDimension();
    final int nCols = getColumnDimension();
    if(v.length != nCols) {
      throw new DimensionMismatchException(v.length, nCols);
    }
    final T[] out = MathArrays.buildArray(field, nRows);
    for(int row = 0; row < nRows; ++row) {
      T sum = field.getZero();
      for(int i = 0; i < nCols; ++i) {
        sum = sum.add(getEntry(row, i).multiply(v[i]));
      }
      out[row] = sum;
    }
    return out;
  }
  public T[] preMultiply(final T[] v) throws DimensionMismatchException {
    final int nRows = getRowDimension();
    final int nCols = getColumnDimension();
    if(v.length != nRows) {
      throw new DimensionMismatchException(v.length, nRows);
    }
    final T[] out = MathArrays.buildArray(field, nCols);
    for(int col = 0; col < nCols; ++col) {
      T sum = field.getZero();
      for(int i = 0; i < nRows; ++i) {
        sum = sum.add(getEntry(i, col).multiply(v[i]));
      }
      out[col] = sum;
    }
    return out;
  }
  @Deprecated() protected static  <T extends org.apache.commons.math3.FieldElement<org.apache.commons.math3.linear.T>> T[][] buildArray(final Field<T> field, final int rows, final int columns) {
    return MathArrays.buildArray(field, rows, columns);
  }
  public T[][] getData() {
    final T[][] data = MathArrays.buildArray(field, getRowDimension(), getColumnDimension());
    for(int i = 0; i < data.length; ++i) {
      final T[] dataI = data[i];
      for(int j = 0; j < dataI.length; ++j) {
        dataI[j] = getEntry(i, j);
      }
    }
    return data;
  }
  @Override() public boolean equals(final Object object) {
    if(object == this) {
      return true;
    }
    if(object instanceof FieldMatrix<?> == false) {
      return false;
    }
    FieldMatrix<?> m = (FieldMatrix<?>)object;
    final int nRows = getRowDimension();
    final int nCols = getColumnDimension();
    if(m.getColumnDimension() != nCols || m.getRowDimension() != nRows) {
      return false;
    }
    for(int row = 0; row < nRows; ++row) {
      for(int col = 0; col < nCols; ++col) {
        if(!getEntry(row, col).equals(m.getEntry(row, col))) {
          return false;
        }
      }
    }
    return true;
  }
  public boolean isSquare() {
    return getColumnDimension() == getRowDimension();
  }
  abstract public int getColumnDimension();
  abstract public int getRowDimension();
  @Override() public int hashCode() {
    int ret = 322562;
    final int nRows = getRowDimension();
    final int nCols = getColumnDimension();
    ret = ret * 31 + nRows;
    ret = ret * 31 + nCols;
    for(int row = 0; row < nRows; ++row) {
      for(int col = 0; col < nCols; ++col) {
        ret = ret * 31 + (11 * (row + 1) + 17 * (col + 1)) * getEntry(row, col).hashCode();
      }
    }
    return ret;
  }
  abstract public void addToEntry(int row, int column, T increment) throws OutOfRangeException;
  protected void checkAdditionCompatible(final FieldMatrix<T> m) throws MatrixDimensionMismatchException {
    if((getRowDimension() != m.getRowDimension()) || (getColumnDimension() != m.getColumnDimension())) {
      throw new MatrixDimensionMismatchException(m.getRowDimension(), m.getColumnDimension(), getRowDimension(), getColumnDimension());
    }
  }
  protected void checkColumnIndex(final int column) throws OutOfRangeException {
    if(column < 0 || column >= getColumnDimension()) {
      throw new OutOfRangeException(LocalizedFormats.COLUMN_INDEX, column, 0, getColumnDimension() - 1);
    }
  }
  protected void checkMultiplicationCompatible(final FieldMatrix<T> m) throws DimensionMismatchException {
    if(getColumnDimension() != m.getRowDimension()) {
      throw new DimensionMismatchException(m.getRowDimension(), getColumnDimension());
    }
  }
  protected void checkRowIndex(final int row) throws OutOfRangeException {
    if(row < 0 || row >= getRowDimension()) {
      throw new OutOfRangeException(LocalizedFormats.ROW_INDEX, row, 0, getRowDimension() - 1);
    }
  }
  protected void checkSubMatrixIndex(final int startRow, final int endRow, final int startColumn, final int endColumn) throws NumberIsTooSmallException, OutOfRangeException {
    checkRowIndex(startRow);
    checkRowIndex(endRow);
    if(endRow < startRow) {
      throw new NumberIsTooSmallException(LocalizedFormats.INITIAL_ROW_AFTER_FINAL_ROW, endRow, startRow, true);
    }
    checkColumnIndex(startColumn);
    checkColumnIndex(endColumn);
    if(endColumn < startColumn) {
      throw new NumberIsTooSmallException(LocalizedFormats.INITIAL_COLUMN_AFTER_FINAL_COLUMN, endColumn, startColumn, true);
    }
  }
  protected void checkSubMatrixIndex(final int[] selectedRows, final int[] selectedColumns) throws NoDataException, NullArgumentException, OutOfRangeException {
    if(selectedRows == null || selectedColumns == null) {
      throw new NullArgumentException();
    }
    if(selectedRows.length == 0 || selectedColumns.length == 0) {
      throw new NoDataException();
    }
    for (final int row : selectedRows) {
      checkRowIndex(row);
    }
    for (final int column : selectedColumns) {
      checkColumnIndex(column);
    }
  }
  protected void checkSubtractionCompatible(final FieldMatrix<T> m) throws MatrixDimensionMismatchException {
    if((getRowDimension() != m.getRowDimension()) || (getColumnDimension() != m.getColumnDimension())) {
      throw new MatrixDimensionMismatchException(m.getRowDimension(), m.getColumnDimension(), getRowDimension(), getColumnDimension());
    }
  }
  public void copySubMatrix(final int startRow, final int endRow, final int startColumn, final int endColumn, final T[][] destination) throws MatrixDimensionMismatchException, NumberIsTooSmallException, OutOfRangeException {
    checkSubMatrixIndex(startRow, endRow, startColumn, endColumn);
    final int rowsCount = endRow + 1 - startRow;
    final int columnsCount = endColumn + 1 - startColumn;
    if((destination.length < rowsCount) || (destination[0].length < columnsCount)) {
      throw new MatrixDimensionMismatchException(destination.length, destination[0].length, rowsCount, columnsCount);
    }
    walkInOptimizedOrder(new DefaultFieldMatrixPreservingVisitor<T>(field.getZero()) {
        private int startRow;
        private int startColumn;
        @Override() public void start(final int rows, final int columns, final int startRow, final int endRow, final int startColumn, final int endColumn) {
          this.startRow = startRow;
          this.startColumn = startColumn;
        }
        @Override() public void visit(final int row, final int column, final T value) {
          destination[row - startRow][column - startColumn] = value;
        }
    }, startRow, endRow, startColumn, endColumn);
  }
  public void copySubMatrix(int[] selectedRows, int[] selectedColumns, T[][] destination) throws MatrixDimensionMismatchException, NoDataException, NullArgumentException, OutOfRangeException {
    checkSubMatrixIndex(selectedRows, selectedColumns);
    if((destination.length < selectedRows.length) || (destination[0].length < selectedColumns.length)) {
      throw new MatrixDimensionMismatchException(destination.length, destination[0].length, selectedRows.length, selectedColumns.length);
    }
    for(int i = 0; i < selectedRows.length; i++) {
      final T[] destinationI = destination[i];
      for(int j = 0; j < selectedColumns.length; j++) {
        destinationI[j] = getEntry(selectedRows[i], selectedColumns[j]);
      }
    }
  }
  abstract public void multiplyEntry(int row, int column, T factor) throws OutOfRangeException;
  public void setColumn(final int column, final T[] array) throws OutOfRangeException, MatrixDimensionMismatchException {
    checkColumnIndex(column);
    final int nRows = getRowDimension();
    if(array.length != nRows) {
      throw new MatrixDimensionMismatchException(array.length, 1, nRows, 1);
    }
    for(int i = 0; i < nRows; ++i) {
      setEntry(i, column, array[i]);
    }
  }
  public void setColumnMatrix(final int column, final FieldMatrix<T> matrix) throws OutOfRangeException, MatrixDimensionMismatchException {
    checkColumnIndex(column);
    final int nRows = getRowDimension();
    if((matrix.getRowDimension() != nRows) || (matrix.getColumnDimension() != 1)) {
      throw new MatrixDimensionMismatchException(matrix.getRowDimension(), matrix.getColumnDimension(), nRows, 1);
    }
    for(int i = 0; i < nRows; ++i) {
      setEntry(i, column, matrix.getEntry(i, 0));
    }
  }
  public void setColumnVector(final int column, final FieldVector<T> vector) throws OutOfRangeException, MatrixDimensionMismatchException {
    checkColumnIndex(column);
    final int nRows = getRowDimension();
    if(vector.getDimension() != nRows) {
      throw new MatrixDimensionMismatchException(vector.getDimension(), 1, nRows, 1);
    }
    for(int i = 0; i < nRows; ++i) {
      setEntry(i, column, vector.getEntry(i));
    }
  }
  abstract public void setEntry(int row, int column, T value) throws OutOfRangeException;
  public void setRow(final int row, final T[] array) throws OutOfRangeException, MatrixDimensionMismatchException {
    checkRowIndex(row);
    final int nCols = getColumnDimension();
    int var_2490 = array.length;
    if(var_2490 != nCols) {
      throw new MatrixDimensionMismatchException(1, array.length, 1, nCols);
    }
    for(int i = 0; i < nCols; ++i) {
      setEntry(row, i, array[i]);
    }
  }
  public void setRowMatrix(final int row, final FieldMatrix<T> matrix) throws OutOfRangeException, MatrixDimensionMismatchException {
    checkRowIndex(row);
    final int nCols = getColumnDimension();
    if((matrix.getRowDimension() != 1) || (matrix.getColumnDimension() != nCols)) {
      throw new MatrixDimensionMismatchException(matrix.getRowDimension(), matrix.getColumnDimension(), 1, nCols);
    }
    for(int i = 0; i < nCols; ++i) {
      setEntry(row, i, matrix.getEntry(0, i));
    }
  }
  public void setRowVector(final int row, final FieldVector<T> vector) throws OutOfRangeException, MatrixDimensionMismatchException {
    checkRowIndex(row);
    final int nCols = getColumnDimension();
    if(vector.getDimension() != nCols) {
      throw new MatrixDimensionMismatchException(1, vector.getDimension(), 1, nCols);
    }
    for(int i = 0; i < nCols; ++i) {
      setEntry(row, i, vector.getEntry(i));
    }
  }
  public void setSubMatrix(final T[][] subMatrix, final int row, final int column) throws DimensionMismatchException, OutOfRangeException, NoDataException, NullArgumentException {
    if(subMatrix == null) {
      throw new NullArgumentException();
    }
    final int nRows = subMatrix.length;
    if(nRows == 0) {
      throw new NoDataException(LocalizedFormats.AT_LEAST_ONE_ROW);
    }
    final int nCols = subMatrix[0].length;
    if(nCols == 0) {
      throw new NoDataException(LocalizedFormats.AT_LEAST_ONE_COLUMN);
    }
    for(int r = 1; r < nRows; ++r) {
      if(subMatrix[r].length != nCols) {
        throw new DimensionMismatchException(nCols, subMatrix[r].length);
      }
    }
    checkRowIndex(row);
    checkColumnIndex(column);
    checkRowIndex(nRows + row - 1);
    checkColumnIndex(nCols + column - 1);
    for(int i = 0; i < nRows; ++i) {
      for(int j = 0; j < nCols; ++j) {
        setEntry(row + i, column + j, subMatrix[i][j]);
      }
    }
  }
}