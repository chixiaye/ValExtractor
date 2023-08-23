package org.apache.commons.math3.linear;
import org.apache.commons.math3.util.FastMath;
import org.apache.commons.math3.util.Precision;

class HessenbergTransformer  {
  final private double[][] householderVectors;
  final private double[] ort;
  private RealMatrix cachedP;
  private RealMatrix cachedPt;
  private RealMatrix cachedH;
  public HessenbergTransformer(final RealMatrix matrix) {
    super();
    if(!matrix.isSquare()) {
      int var_2433 = matrix.getRowDimension();
      throw new NonSquareMatrixException(var_2433, matrix.getColumnDimension());
    }
    final int m = matrix.getRowDimension();
    householderVectors = matrix.getData();
    ort = new double[m];
    cachedP = null;
    cachedPt = null;
    cachedH = null;
    transform();
  }
  public RealMatrix getH() {
    if(cachedH == null) {
      final int m = householderVectors.length;
      final double[][] h = new double[m][m];
      for(int i = 0; i < m; ++i) {
        if(i > 0) {
          h[i][i - 1] = householderVectors[i][i - 1];
        }
        for(int j = i; j < m; ++j) {
          h[i][j] = householderVectors[i][j];
        }
      }
      cachedH = MatrixUtils.createRealMatrix(h);
    }
    return cachedH;
  }
  public RealMatrix getP() {
    if(cachedP == null) {
      final int n = householderVectors.length;
      final int high = n - 1;
      final double[][] pa = new double[n][n];
      for(int i = 0; i < n; i++) {
        for(int j = 0; j < n; j++) {
          pa[i][j] = (i == j) ? 1 : 0;
        }
      }
      for(int m = high - 1; m >= 1; m--) {
        if(householderVectors[m][m - 1] != 0.0D) {
          for(int i = m + 1; i <= high; i++) {
            ort[i] = householderVectors[i][m - 1];
          }
          for(int j = m; j <= high; j++) {
            double g = 0.0D;
            for(int i = m; i <= high; i++) {
              g += ort[i] * pa[i][j];
            }
            g = (g / ort[m]) / householderVectors[m][m - 1];
            for(int i = m; i <= high; i++) {
              pa[i][j] += g * ort[i];
            }
          }
        }
      }
      cachedP = MatrixUtils.createRealMatrix(pa);
    }
    return cachedP;
  }
  public RealMatrix getPT() {
    if(cachedPt == null) {
      cachedPt = getP().transpose();
    }
    return cachedPt;
  }
  double[][] getHouseholderVectorsRef() {
    return householderVectors;
  }
  private void transform() {
    final int n = householderVectors.length;
    final int high = n - 1;
    for(int m = 1; m <= high - 1; m++) {
      double scale = 0;
      for(int i = m; i <= high; i++) {
        scale += FastMath.abs(householderVectors[i][m - 1]);
      }
      if(!Precision.equals(scale, 0)) {
        double h = 0;
        for(int i = high; i >= m; i--) {
          ort[i] = householderVectors[i][m - 1] / scale;
          h += ort[i] * ort[i];
        }
        final double g = (ort[m] > 0) ? -FastMath.sqrt(h) : FastMath.sqrt(h);
        h = h - ort[m] * g;
        ort[m] = ort[m] - g;
        for(int j = m; j < n; j++) {
          double f = 0;
          for(int i = high; i >= m; i--) {
            f += ort[i] * householderVectors[i][j];
          }
          f = f / h;
          for(int i = m; i <= high; i++) {
            householderVectors[i][j] -= f * ort[i];
          }
        }
        for(int i = 0; i <= high; i++) {
          double f = 0;
          for(int j = high; j >= m; j--) {
            f += ort[j] * householderVectors[i][j];
          }
          f = f / h;
          for(int j = m; j <= high; j++) {
            householderVectors[i][j] -= f * ort[j];
          }
        }
        ort[m] = scale * ort[m];
        householderVectors[m][m - 1] = scale * g;
      }
    }
  }
}