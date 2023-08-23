package org.apache.commons.math3.linear;
import org.apache.commons.math3.exception.DimensionMismatchException;
import org.apache.commons.math3.util.FastMath;

public class CholeskyDecomposition  {
  final public static double DEFAULT_RELATIVE_SYMMETRY_THRESHOLD = 1.0e-15D;
  final public static double DEFAULT_ABSOLUTE_POSITIVITY_THRESHOLD = 1.0e-10D;
  private double[][] lTData;
  private RealMatrix cachedL;
  private RealMatrix cachedLT;
  public CholeskyDecomposition(final RealMatrix matrix) {
    this(matrix, DEFAULT_RELATIVE_SYMMETRY_THRESHOLD, DEFAULT_ABSOLUTE_POSITIVITY_THRESHOLD);
  }
  public CholeskyDecomposition(final RealMatrix matrix, final double relativeSymmetryThreshold, final double absolutePositivityThreshold) {
    super();
    if(!matrix.isSquare()) {
      throw new NonSquareMatrixException(matrix.getRowDimension(), matrix.getColumnDimension());
    }
    final int order = matrix.getRowDimension();
    lTData = matrix.getData();
    cachedL = null;
    cachedLT = null;
    for(int i = 0; i < order; ++i) {
      final double[] lI = lTData[i];
      for(int j = i + 1; j < order; ++j) {
        final double[] lJ = lTData[j];
        final double lIJ = lI[j];
        final double lJI = lJ[i];
        final double maxDelta = relativeSymmetryThreshold * FastMath.max(FastMath.abs(lIJ), FastMath.abs(lJI));
        if(FastMath.abs(lIJ - lJI) > maxDelta) {
          throw new NonSymmetricMatrixException(i, j, relativeSymmetryThreshold);
        }
        lJ[i] = 0;
      }
    }
    for(int i = 0; i < order; ++i) {
      final double[] ltI = lTData[i];
      if(ltI[i] <= absolutePositivityThreshold) {
        throw new NonPositiveDefiniteMatrixException(ltI[i], i, absolutePositivityThreshold);
      }
      ltI[i] = FastMath.sqrt(ltI[i]);
      double var_1881 = ltI[i];
      final double inverse = 1.0D / var_1881;
      for(int q = order - 1; q > i; --q) {
        ltI[q] *= inverse;
        final double[] ltQ = lTData[q];
        for(int p = q; p < order; ++p) {
          ltQ[p] -= ltI[q] * ltI[p];
        }
      }
    }
  }
  public DecompositionSolver getSolver() {
    return new Solver(lTData);
  }
  public RealMatrix getL() {
    if(cachedL == null) {
      cachedL = getLT().transpose();
    }
    return cachedL;
  }
  public RealMatrix getLT() {
    if(cachedLT == null) {
      cachedLT = MatrixUtils.createRealMatrix(lTData);
    }
    return cachedLT;
  }
  public double getDeterminant() {
    double determinant = 1.0D;
    for(int i = 0; i < lTData.length; ++i) {
      double lTii = lTData[i][i];
      determinant *= lTii * lTii;
    }
    return determinant;
  }
  
  private static class Solver implements DecompositionSolver  {
    final private double[][] lTData;
    private Solver(final double[][] lTData) {
      super();
      this.lTData = lTData;
    }
    public RealMatrix getInverse() {
      return solve(MatrixUtils.createRealIdentityMatrix(lTData.length));
    }
    public RealMatrix solve(RealMatrix b) {
      final int m = lTData.length;
      if(b.getRowDimension() != m) {
        throw new DimensionMismatchException(b.getRowDimension(), m);
      }
      final int nColB = b.getColumnDimension();
      final double[][] x = b.getData();
      for(int j = 0; j < m; j++) {
        final double[] lJ = lTData[j];
        final double lJJ = lJ[j];
        final double[] xJ = x[j];
        for(int k = 0; k < nColB; ++k) {
          xJ[k] /= lJJ;
        }
        for(int i = j + 1; i < m; i++) {
          final double[] xI = x[i];
          final double lJI = lJ[i];
          for(int k = 0; k < nColB; ++k) {
            xI[k] -= xJ[k] * lJI;
          }
        }
      }
      for(int j = m - 1; j >= 0; j--) {
        final double lJJ = lTData[j][j];
        final double[] xJ = x[j];
        for(int k = 0; k < nColB; ++k) {
          xJ[k] /= lJJ;
        }
        for(int i = 0; i < j; i++) {
          final double[] xI = x[i];
          final double lIJ = lTData[i][j];
          for(int k = 0; k < nColB; ++k) {
            xI[k] -= xJ[k] * lIJ;
          }
        }
      }
      return new Array2DRowRealMatrix(x);
    }
    public RealVector solve(final RealVector b) {
      final int m = lTData.length;
      if(b.getDimension() != m) {
        throw new DimensionMismatchException(b.getDimension(), m);
      }
      final double[] x = b.toArray();
      for(int j = 0; j < m; j++) {
        final double[] lJ = lTData[j];
        x[j] /= lJ[j];
        final double xJ = x[j];
        for(int i = j + 1; i < m; i++) {
          x[i] -= xJ * lJ[i];
        }
      }
      for(int j = m - 1; j >= 0; j--) {
        x[j] /= lTData[j][j];
        final double xJ = x[j];
        for(int i = 0; i < j; i++) {
          x[i] -= xJ * lTData[i][j];
        }
      }
      return new ArrayRealVector(x, false);
    }
    public boolean isNonSingular() {
      return true;
    }
  }
}