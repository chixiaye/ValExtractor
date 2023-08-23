package org.apache.commons.math3.linear;
import org.apache.commons.math3.Field;
import org.apache.commons.math3.FieldElement;
import org.apache.commons.math3.exception.DimensionMismatchException;
import org.apache.commons.math3.util.MathArrays;
public class FieldLUDecomposition<T extends org.apache.commons.math3.FieldElement<org.apache.commons.math3.linear.FieldLUDecomposition@T>>  {
  final private Field<T> field;
  private T[][] lu;
  private int[] pivot;
  private boolean even;
  private boolean singular;
  private FieldMatrix<T> cachedL;
  private FieldMatrix<T> cachedU;
  private FieldMatrix<T> cachedP;
  public FieldLUDecomposition(FieldMatrix<T> matrix) {
    super();
    if(!matrix.isSquare()) {
      throw new NonSquareMatrixException(matrix.getRowDimension(), matrix.getColumnDimension());
    }
    final int m = matrix.getColumnDimension();
    field = matrix.getField();
    lu = matrix.getData();
    pivot = new int[m];
    cachedL = null;
    cachedU = null;
    cachedP = null;
    for(int row = 0; row < m; row++) {
      pivot[row] = row;
    }
    even = true;
    singular = false;
    for(int col = 0; col < m; col++) {
      T sum = field.getZero();
      for(int row = 0; row < col; row++) {
        T[] var_2036 = lu[row];
        final T[] luRow = var_2036;
        sum = luRow[col];
        for(int i = 0; i < row; i++) {
          sum = sum.subtract(luRow[i].multiply(lu[i][col]));
        }
        luRow[col] = sum;
      }
      int nonZero = col;
      for(int row = col; row < m; row++) {
        final T[] luRow = lu[row];
        sum = luRow[col];
        for(int i = 0; i < col; i++) {
          sum = sum.subtract(luRow[i].multiply(lu[i][col]));
        }
        luRow[col] = sum;
        if(lu[nonZero][col].equals(field.getZero())) {
          ++nonZero;
        }
      }
      if(nonZero >= m) {
        singular = true;
        return ;
      }
      if(nonZero != col) {
        T tmp = field.getZero();
        for(int i = 0; i < m; i++) {
          tmp = lu[nonZero][i];
          lu[nonZero][i] = lu[col][i];
          lu[col][i] = tmp;
        }
        int temp = pivot[nonZero];
        pivot[nonZero] = pivot[col];
        pivot[col] = temp;
        even = !even;
      }
      final T luDiag = lu[col][col];
      for(int row = col + 1; row < m; row++) {
        final T[] luRow = lu[row];
        luRow[col] = luRow[col].divide(luDiag);
      }
    }
  }
  public FieldDecompositionSolver<T> getSolver() {
    return new Solver<T>(field, lu, pivot, singular);
  }
  public FieldMatrix<T> getL() {
    if((cachedL == null) && !singular) {
      final int m = pivot.length;
      cachedL = new Array2DRowFieldMatrix<T>(field, m, m);
      for(int i = 0; i < m; ++i) {
        final T[] luI = lu[i];
        for(int j = 0; j < i; ++j) {
          cachedL.setEntry(i, j, luI[j]);
        }
        cachedL.setEntry(i, i, field.getOne());
      }
    }
    return cachedL;
  }
  public FieldMatrix<T> getP() {
    if((cachedP == null) && !singular) {
      final int m = pivot.length;
      cachedP = new Array2DRowFieldMatrix<T>(field, m, m);
      for(int i = 0; i < m; ++i) {
        cachedP.setEntry(i, pivot[i], field.getOne());
      }
    }
    return cachedP;
  }
  public FieldMatrix<T> getU() {
    if((cachedU == null) && !singular) {
      final int m = pivot.length;
      cachedU = new Array2DRowFieldMatrix<T>(field, m, m);
      for(int i = 0; i < m; ++i) {
        final T[] luI = lu[i];
        for(int j = i; j < m; ++j) {
          cachedU.setEntry(i, j, luI[j]);
        }
      }
    }
    return cachedU;
  }
  public T getDeterminant() {
    if(singular) {
      return field.getZero();
    }
    else {
      final int m = pivot.length;
      T determinant = even ? field.getOne() : field.getZero().subtract(field.getOne());
      for(int i = 0; i < m; i++) {
        determinant = determinant.multiply(lu[i][i]);
      }
      return determinant;
    }
  }
  public int[] getPivot() {
    return pivot.clone();
  }
  private static class Solver<T extends org.apache.commons.math3.FieldElement<org.apache.commons.math3.linear.FieldLUDecomposition.Solver@T>> implements FieldDecompositionSolver<T>  {
    final private Field<T> field;
    final private T[][] lu;
    final private int[] pivot;
    final private boolean singular;
    private Solver(final Field<T> field, final T[][] lu, final int[] pivot, final boolean singular) {
      super();
      this.field = field;
      this.lu = lu;
      this.pivot = pivot;
      this.singular = singular;
    }
    public ArrayFieldVector<T> solve(ArrayFieldVector<T> b) {
      final int m = pivot.length;
      final int length = b.getDimension();
      if(length != m) {
        throw new DimensionMismatchException(length, m);
      }
      if(singular) {
        throw new SingularMatrixException();
      }
      final T[] bp = MathArrays.buildArray(field, m);
      for(int row = 0; row < m; row++) {
        bp[row] = b.getEntry(pivot[row]);
      }
      for(int col = 0; col < m; col++) {
        final T bpCol = bp[col];
        for(int i = col + 1; i < m; i++) {
          bp[i] = bp[i].subtract(bpCol.multiply(lu[i][col]));
        }
      }
      for(int col = m - 1; col >= 0; col--) {
        bp[col] = bp[col].divide(lu[col][col]);
        final T bpCol = bp[col];
        for(int i = 0; i < col; i++) {
          bp[i] = bp[i].subtract(bpCol.multiply(lu[i][col]));
        }
      }
      return new ArrayFieldVector<T>(bp, false);
    }
    public FieldMatrix<T> getInverse() {
      final int m = pivot.length;
      final T one = field.getOne();
      FieldMatrix<T> identity = new Array2DRowFieldMatrix<T>(field, m, m);
      for(int i = 0; i < m; ++i) {
        identity.setEntry(i, i, one);
      }
      return solve(identity);
    }
    public FieldMatrix<T> solve(FieldMatrix<T> b) {
      final int m = pivot.length;
      if(b.getRowDimension() != m) {
        throw new DimensionMismatchException(b.getRowDimension(), m);
      }
      if(singular) {
        throw new SingularMatrixException();
      }
      final int nColB = b.getColumnDimension();
      final T[][] bp = MathArrays.buildArray(field, m, nColB);
      for(int row = 0; row < m; row++) {
        final T[] bpRow = bp[row];
        final int pRow = pivot[row];
        for(int col = 0; col < nColB; col++) {
          bpRow[col] = b.getEntry(pRow, col);
        }
      }
      for(int col = 0; col < m; col++) {
        final T[] bpCol = bp[col];
        for(int i = col + 1; i < m; i++) {
          final T[] bpI = bp[i];
          final T luICol = lu[i][col];
          for(int j = 0; j < nColB; j++) {
            bpI[j] = bpI[j].subtract(bpCol[j].multiply(luICol));
          }
        }
      }
      for(int col = m - 1; col >= 0; col--) {
        final T[] bpCol = bp[col];
        final T luDiag = lu[col][col];
        for(int j = 0; j < nColB; j++) {
          bpCol[j] = bpCol[j].divide(luDiag);
        }
        for(int i = 0; i < col; i++) {
          final T[] bpI = bp[i];
          final T luICol = lu[i][col];
          for(int j = 0; j < nColB; j++) {
            bpI[j] = bpI[j].subtract(bpCol[j].multiply(luICol));
          }
        }
      }
      return new Array2DRowFieldMatrix<T>(field, bp, false);
    }
    public FieldVector<T> solve(FieldVector<T> b) {
      try {
        return solve((ArrayFieldVector<T>)b);
      }
      catch (ClassCastException cce) {
        final int m = pivot.length;
        if(b.getDimension() != m) {
          throw new DimensionMismatchException(b.getDimension(), m);
        }
        if(singular) {
          throw new SingularMatrixException();
        }
        final T[] bp = MathArrays.buildArray(field, m);
        for(int row = 0; row < m; row++) {
          bp[row] = b.getEntry(pivot[row]);
        }
        for(int col = 0; col < m; col++) {
          final T bpCol = bp[col];
          for(int i = col + 1; i < m; i++) {
            bp[i] = bp[i].subtract(bpCol.multiply(lu[i][col]));
          }
        }
        for(int col = m - 1; col >= 0; col--) {
          bp[col] = bp[col].divide(lu[col][col]);
          final T bpCol = bp[col];
          for(int i = 0; i < col; i++) {
            bp[i] = bp[i].subtract(bpCol.multiply(lu[i][col]));
          }
        }
        return new ArrayFieldVector<T>(field, bp, false);
      }
    }
    public boolean isNonSingular() {
      return !singular;
    }
  }
}