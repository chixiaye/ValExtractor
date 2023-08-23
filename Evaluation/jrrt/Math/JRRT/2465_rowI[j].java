package org.apache.commons.math3.linear;
import java.io.Serializable;
import org.apache.commons.math3.Field;
import org.apache.commons.math3.FieldElement;
import org.apache.commons.math3.exception.NoDataException;
import org.apache.commons.math3.exception.DimensionMismatchException;
import org.apache.commons.math3.exception.MathIllegalStateException;
import org.apache.commons.math3.exception.NotStrictlyPositiveException;
import org.apache.commons.math3.exception.NullArgumentException;
import org.apache.commons.math3.exception.NumberIsTooSmallException;
import org.apache.commons.math3.exception.OutOfRangeException;
import org.apache.commons.math3.exception.util.LocalizedFormats;
import org.apache.commons.math3.util.MathArrays;
import org.apache.commons.math3.util.MathUtils;
public class Array2DRowFieldMatrix<T extends org.apache.commons.math3.FieldElement<org.apache.commons.math3.linear.Array2DRowFieldMatrix@T>> extends AbstractFieldMatrix<T> implements Serializable  {
  final private static long serialVersionUID = 7260756672015356458L;
  private T[][] data;
  public Array2DRowFieldMatrix(final Field<T> field) {
    super(field);
  }
  public Array2DRowFieldMatrix(final Field<T> field, final T[] v) {
    super(field);
    final int nRows = v.length;
    data = MathArrays.buildArray(getField(), nRows, 1);
    for(int row = 0; row < nRows; row++) {
      data[row][0] = v[row];
    }
  }
  public Array2DRowFieldMatrix(final Field<T> field, final T[][] d) throws DimensionMismatchException, NullArgumentException, NoDataException {
    super(field);
    copyIn(d);
  }
  public Array2DRowFieldMatrix(final Field<T> field, final T[][] d, final boolean copyArray) throws DimensionMismatchException, NoDataException, NullArgumentException {
    super(field);
    if(copyArray) {
      copyIn(d);
    }
    else {
      MathUtils.checkNotNull(d);
      final int nRows = d.length;
      if(nRows == 0) {
        throw new NoDataException(LocalizedFormats.AT_LEAST_ONE_ROW);
      }
      final int nCols = d[0].length;
      if(nCols == 0) {
        throw new NoDataException(LocalizedFormats.AT_LEAST_ONE_COLUMN);
      }
      for(int r = 1; r < nRows; r++) {
        if(d[r].length != nCols) {
          throw new DimensionMismatchException(nCols, d[r].length);
        }
      }
      data = d;
    }
  }
  public Array2DRowFieldMatrix(final Field<T> field, final int rowDimension, final int columnDimension) throws NotStrictlyPositiveException {
    super(field, rowDimension, columnDimension);
    data = MathArrays.buildArray(field, rowDimension, columnDimension);
  }
  public Array2DRowFieldMatrix(final T[] v) throws NoDataException {
    this(extractField(v), v);
  }
  public Array2DRowFieldMatrix(final T[][] d) throws DimensionMismatchException, NullArgumentException, NoDataException {
    this(extractField(d), d);
  }
  public Array2DRowFieldMatrix(final T[][] d, final boolean copyArray) throws DimensionMismatchException, NoDataException, NullArgumentException {
    this(extractField(d), d, copyArray);
  }
  public Array2DRowFieldMatrix<T> add(final Array2DRowFieldMatrix<T> m) throws MatrixDimensionMismatchException {
    checkAdditionCompatible(m);
    final int rowCount = getRowDimension();
    final int columnCount = getColumnDimension();
    final T[][] outData = MathArrays.buildArray(getField(), rowCount, columnCount);
    for(int row = 0; row < rowCount; row++) {
      final T[] dataRow = data[row];
      final T[] mRow = m.data[row];
      final T[] outDataRow = outData[row];
      for(int col = 0; col < columnCount; col++) {
        outDataRow[col] = dataRow[col].add(mRow[col]);
      }
    }
    return new Array2DRowFieldMatrix<T>(getField(), outData, false);
  }
  public Array2DRowFieldMatrix<T> multiply(final Array2DRowFieldMatrix<T> m) throws DimensionMismatchException {
    checkMultiplicationCompatible(m);
    final int nRows = this.getRowDimension();
    final int nCols = m.getColumnDimension();
    final int nSum = this.getColumnDimension();
    final T[][] outData = MathArrays.buildArray(getField(), nRows, nCols);
    for(int row = 0; row < nRows; row++) {
      final T[] dataRow = data[row];
      final T[] outDataRow = outData[row];
      for(int col = 0; col < nCols; col++) {
        T sum = getField().getZero();
        for(int i = 0; i < nSum; i++) {
          sum = sum.add(dataRow[i].multiply(m.data[i][col]));
        }
        outDataRow[col] = sum;
      }
    }
    return new Array2DRowFieldMatrix<T>(getField(), outData, false);
  }
  public Array2DRowFieldMatrix<T> subtract(final Array2DRowFieldMatrix<T> m) throws MatrixDimensionMismatchException {
    checkSubtractionCompatible(m);
    final int rowCount = getRowDimension();
    final int columnCount = getColumnDimension();
    final T[][] outData = MathArrays.buildArray(getField(), rowCount, columnCount);
    for(int row = 0; row < rowCount; row++) {
      final T[] dataRow = data[row];
      final T[] mRow = m.data[row];
      final T[] outDataRow = outData[row];
      for(int col = 0; col < columnCount; col++) {
        outDataRow[col] = dataRow[col].subtract(mRow[col]);
      }
    }
    return new Array2DRowFieldMatrix<T>(getField(), outData, false);
  }
  @Override() public FieldMatrix<T> copy() {
    return new Array2DRowFieldMatrix<T>(getField(), copyOut(), false);
  }
  @Override() public FieldMatrix<T> createMatrix(final int rowDimension, final int columnDimension) throws NotStrictlyPositiveException {
    return new Array2DRowFieldMatrix<T>(getField(), rowDimension, columnDimension);
  }
  @Override() public T getEntry(final int row, final int column) throws OutOfRangeException {
    checkRowIndex(row);
    checkColumnIndex(column);
    return data[row][column];
  }
  @Override() public T walkInColumnOrder(final FieldMatrixChangingVisitor<T> visitor) {
    final int rows = getRowDimension();
    final int columns = getColumnDimension();
    visitor.start(rows, columns, 0, rows - 1, 0, columns - 1);
    for(int j = 0; j < columns; ++j) {
      for(int i = 0; i < rows; ++i) {
        final T[] rowI = data[i];
        rowI[j] = visitor.visit(i, j, rowI[j]);
      }
    }
    return visitor.end();
  }
  @Override() public T walkInColumnOrder(final FieldMatrixChangingVisitor<T> visitor, final int startRow, final int endRow, final int startColumn, final int endColumn) throws OutOfRangeException, NumberIsTooSmallException {
    checkSubMatrixIndex(startRow, endRow, startColumn, endColumn);
    visitor.start(getRowDimension(), getColumnDimension(), startRow, endRow, startColumn, endColumn);
    for(int j = startColumn; j <= endColumn; ++j) {
      for(int i = startRow; i <= endRow; ++i) {
        final T[] rowI = data[i];
        rowI[j] = visitor.visit(i, j, rowI[j]);
      }
    }
    return visitor.end();
  }
  @Override() public T walkInColumnOrder(final FieldMatrixPreservingVisitor<T> visitor) {
    final int rows = getRowDimension();
    final int columns = getColumnDimension();
    visitor.start(rows, columns, 0, rows - 1, 0, columns - 1);
    for(int j = 0; j < columns; ++j) {
      for(int i = 0; i < rows; ++i) {
        visitor.visit(i, j, data[i][j]);
      }
    }
    return visitor.end();
  }
  @Override() public T walkInColumnOrder(final FieldMatrixPreservingVisitor<T> visitor, final int startRow, final int endRow, final int startColumn, final int endColumn) throws OutOfRangeException, NumberIsTooSmallException {
    checkSubMatrixIndex(startRow, endRow, startColumn, endColumn);
    visitor.start(getRowDimension(), getColumnDimension(), startRow, endRow, startColumn, endColumn);
    for(int j = startColumn; j <= endColumn; ++j) {
      for(int i = startRow; i <= endRow; ++i) {
        visitor.visit(i, j, data[i][j]);
      }
    }
    return visitor.end();
  }
  @Override() public T walkInRowOrder(final FieldMatrixChangingVisitor<T> visitor) {
    final int rows = getRowDimension();
    final int columns = getColumnDimension();
    visitor.start(rows, columns, 0, rows - 1, 0, columns - 1);
    for(int i = 0; i < rows; ++i) {
      final T[] rowI = data[i];
      for(int j = 0; j < columns; ++j) {
        T var_2465 = rowI[j];
        rowI[j] = visitor.visit(i, j, var_2465);
      }
    }
    return visitor.end();
  }
  @Override() public T walkInRowOrder(final FieldMatrixChangingVisitor<T> visitor, final int startRow, final int endRow, final int startColumn, final int endColumn) throws OutOfRangeException, NumberIsTooSmallException {
    checkSubMatrixIndex(startRow, endRow, startColumn, endColumn);
    visitor.start(getRowDimension(), getColumnDimension(), startRow, endRow, startColumn, endColumn);
    for(int i = startRow; i <= endRow; ++i) {
      final T[] rowI = data[i];
      for(int j = startColumn; j <= endColumn; ++j) {
        rowI[j] = visitor.visit(i, j, rowI[j]);
      }
    }
    return visitor.end();
  }
  @Override() public T walkInRowOrder(final FieldMatrixPreservingVisitor<T> visitor) {
    final int rows = getRowDimension();
    final int columns = getColumnDimension();
    visitor.start(rows, columns, 0, rows - 1, 0, columns - 1);
    for(int i = 0; i < rows; ++i) {
      final T[] rowI = data[i];
      for(int j = 0; j < columns; ++j) {
        visitor.visit(i, j, rowI[j]);
      }
    }
    return visitor.end();
  }
  @Override() public T walkInRowOrder(final FieldMatrixPreservingVisitor<T> visitor, final int startRow, final int endRow, final int startColumn, final int endColumn) throws OutOfRangeException, NumberIsTooSmallException {
    checkSubMatrixIndex(startRow, endRow, startColumn, endColumn);
    visitor.start(getRowDimension(), getColumnDimension(), startRow, endRow, startColumn, endColumn);
    for(int i = startRow; i <= endRow; ++i) {
      final T[] rowI = data[i];
      for(int j = startColumn; j <= endColumn; ++j) {
        visitor.visit(i, j, rowI[j]);
      }
    }
    return visitor.end();
  }
  @Override() public T[] operate(final T[] v) throws DimensionMismatchException {
    final int nRows = this.getRowDimension();
    final int nCols = this.getColumnDimension();
    if(v.length != nCols) {
      throw new DimensionMismatchException(v.length, nCols);
    }
    final T[] out = MathArrays.buildArray(getField(), nRows);
    for(int row = 0; row < nRows; row++) {
      final T[] dataRow = data[row];
      T sum = getField().getZero();
      for(int i = 0; i < nCols; i++) {
        sum = sum.add(dataRow[i].multiply(v[i]));
      }
      out[row] = sum;
    }
    return out;
  }
  @Override() public T[] preMultiply(final T[] v) throws DimensionMismatchException {
    final int nRows = getRowDimension();
    final int nCols = getColumnDimension();
    if(v.length != nRows) {
      throw new DimensionMismatchException(v.length, nRows);
    }
    final T[] out = MathArrays.buildArray(getField(), nCols);
    for(int col = 0; col < nCols; ++col) {
      T sum = getField().getZero();
      for(int i = 0; i < nRows; ++i) {
        sum = sum.add(data[i][col].multiply(v[i]));
      }
      out[col] = sum;
    }
    return out;
  }
  private T[][] copyOut() {
    final int nRows = this.getRowDimension();
    final T[][] out = MathArrays.buildArray(getField(), nRows, getColumnDimension());
    for(int i = 0; i < nRows; i++) {
      System.arraycopy(data[i], 0, out[i], 0, data[i].length);
    }
    return out;
  }
  @Override() public T[][] getData() {
    return copyOut();
  }
  public T[][] getDataRef() {
    return data;
  }
  @Override() public int getColumnDimension() {
    return ((data == null) || (data[0] == null)) ? 0 : data[0].length;
  }
  @Override() public int getRowDimension() {
    return (data == null) ? 0 : data.length;
  }
  @Override() public void addToEntry(final int row, final int column, final T increment) throws OutOfRangeException {
    checkRowIndex(row);
    checkColumnIndex(column);
    data[row][column] = data[row][column].add(increment);
  }
  private void copyIn(final T[][] in) throws NullArgumentException, NoDataException, DimensionMismatchException {
    setSubMatrix(in, 0, 0);
  }
  @Override() public void multiplyEntry(final int row, final int column, final T factor) throws OutOfRangeException {
    checkRowIndex(row);
    checkColumnIndex(column);
    data[row][column] = data[row][column].multiply(factor);
  }
  @Override() public void setEntry(final int row, final int column, final T value) throws OutOfRangeException {
    checkRowIndex(row);
    checkColumnIndex(column);
    data[row][column] = value;
  }
  @Override() public void setSubMatrix(final T[][] subMatrix, final int row, final int column) throws OutOfRangeException, NullArgumentException, NoDataException, DimensionMismatchException {
    if(data == null) {
      if(row > 0) {
        throw new MathIllegalStateException(LocalizedFormats.FIRST_ROWS_NOT_INITIALIZED_YET, row);
      }
      if(column > 0) {
        throw new MathIllegalStateException(LocalizedFormats.FIRST_COLUMNS_NOT_INITIALIZED_YET, column);
      }
      final int nRows = subMatrix.length;
      if(nRows == 0) {
        throw new NoDataException(LocalizedFormats.AT_LEAST_ONE_ROW);
      }
      final int nCols = subMatrix[0].length;
      if(nCols == 0) {
        throw new NoDataException(LocalizedFormats.AT_LEAST_ONE_COLUMN);
      }
      data = MathArrays.buildArray(getField(), subMatrix.length, nCols);
      for(int i = 0; i < data.length; ++i) {
        if(subMatrix[i].length != nCols) {
          throw new DimensionMismatchException(nCols, subMatrix[i].length);
        }
        System.arraycopy(subMatrix[i], 0, data[i + row], column, nCols);
      }
    }
    else {
      super.setSubMatrix(subMatrix, row, column);
    }
  }
}