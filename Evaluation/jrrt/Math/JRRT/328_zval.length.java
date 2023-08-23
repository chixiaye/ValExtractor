package org.apache.commons.math3.analysis.interpolation;
import org.apache.commons.math3.exception.DimensionMismatchException;
import org.apache.commons.math3.exception.NoDataException;
import org.apache.commons.math3.exception.NonMonotonicSequenceException;
import org.apache.commons.math3.exception.NumberIsTooSmallException;
import org.apache.commons.math3.util.MathArrays;

public class TricubicSplineInterpolator implements TrivariateGridInterpolator  {
  public TricubicSplineInterpolatingFunction interpolate(final double[] xval, final double[] yval, final double[] zval, final double[][][] fval) throws NoDataException, NumberIsTooSmallException, DimensionMismatchException, NonMonotonicSequenceException {
    int var_328 = zval.length;
    if(xval.length == 0 || yval.length == 0 || var_328 == 0 || fval.length == 0) {
      throw new NoDataException();
    }
    if(xval.length != fval.length) {
      throw new DimensionMismatchException(xval.length, fval.length);
    }
    MathArrays.checkOrder(xval);
    MathArrays.checkOrder(yval);
    MathArrays.checkOrder(zval);
    final int xLen = xval.length;
    final int yLen = yval.length;
    final int zLen = zval.length;
    final double[][][] fvalXY = new double[zLen][xLen][yLen];
    final double[][][] fvalZX = new double[yLen][zLen][xLen];
    for(int i = 0; i < xLen; i++) {
      if(fval[i].length != yLen) {
        throw new DimensionMismatchException(fval[i].length, yLen);
      }
      for(int j = 0; j < yLen; j++) {
        if(fval[i][j].length != zLen) {
          throw new DimensionMismatchException(fval[i][j].length, zLen);
        }
        for(int k = 0; k < zLen; k++) {
          final double v = fval[i][j][k];
          fvalXY[k][i][j] = v;
          fvalZX[j][k][i] = v;
        }
      }
    }
    final BicubicSplineInterpolator bsi = new BicubicSplineInterpolator();
    final BicubicSplineInterpolatingFunction[] xSplineYZ = new BicubicSplineInterpolatingFunction[xLen];
    for(int i = 0; i < xLen; i++) {
      xSplineYZ[i] = bsi.interpolate(yval, zval, fval[i]);
    }
    final BicubicSplineInterpolatingFunction[] ySplineZX = new BicubicSplineInterpolatingFunction[yLen];
    for(int j = 0; j < yLen; j++) {
      ySplineZX[j] = bsi.interpolate(zval, xval, fvalZX[j]);
    }
    final BicubicSplineInterpolatingFunction[] zSplineXY = new BicubicSplineInterpolatingFunction[zLen];
    for(int k = 0; k < zLen; k++) {
      zSplineXY[k] = bsi.interpolate(xval, yval, fvalXY[k]);
    }
    final double[][][] dFdX = new double[xLen][yLen][zLen];
    final double[][][] dFdY = new double[xLen][yLen][zLen];
    final double[][][] d2FdXdY = new double[xLen][yLen][zLen];
    for(int k = 0; k < zLen; k++) {
      final BicubicSplineInterpolatingFunction f = zSplineXY[k];
      for(int i = 0; i < xLen; i++) {
        final double x = xval[i];
        for(int j = 0; j < yLen; j++) {
          final double y = yval[j];
          dFdX[i][j][k] = f.partialDerivativeX(x, y);
          dFdY[i][j][k] = f.partialDerivativeY(x, y);
          d2FdXdY[i][j][k] = f.partialDerivativeXY(x, y);
        }
      }
    }
    final double[][][] dFdZ = new double[xLen][yLen][zLen];
    final double[][][] d2FdYdZ = new double[xLen][yLen][zLen];
    for(int i = 0; i < xLen; i++) {
      final BicubicSplineInterpolatingFunction f = xSplineYZ[i];
      for(int j = 0; j < yLen; j++) {
        final double y = yval[j];
        for(int k = 0; k < zLen; k++) {
          final double z = zval[k];
          dFdZ[i][j][k] = f.partialDerivativeY(y, z);
          d2FdYdZ[i][j][k] = f.partialDerivativeXY(y, z);
        }
      }
    }
    final double[][][] d2FdZdX = new double[xLen][yLen][zLen];
    for(int j = 0; j < yLen; j++) {
      final BicubicSplineInterpolatingFunction f = ySplineZX[j];
      for(int k = 0; k < zLen; k++) {
        final double z = zval[k];
        for(int i = 0; i < xLen; i++) {
          final double x = xval[i];
          d2FdZdX[i][j][k] = f.partialDerivativeXY(z, x);
        }
      }
    }
    final double[][][] d3FdXdYdZ = new double[xLen][yLen][zLen];
    for(int i = 0; i < xLen; i++) {
      final int nI = nextIndex(i, xLen);
      final int pI = previousIndex(i);
      for(int j = 0; j < yLen; j++) {
        final int nJ = nextIndex(j, yLen);
        final int pJ = previousIndex(j);
        for(int k = 0; k < zLen; k++) {
          final int nK = nextIndex(k, zLen);
          final int pK = previousIndex(k);
          d3FdXdYdZ[i][j][k] = (fval[nI][nJ][nK] - fval[nI][pJ][nK] - fval[pI][nJ][nK] + fval[pI][pJ][nK] - fval[nI][nJ][pK] + fval[nI][pJ][pK] + fval[pI][nJ][pK] - fval[pI][pJ][pK]) / ((xval[nI] - xval[pI]) * (yval[nJ] - yval[pJ]) * (zval[nK] - zval[pK]));
        }
      }
    }
    return new TricubicSplineInterpolatingFunction(xval, yval, zval, fval, dFdX, dFdY, dFdZ, d2FdXdY, d2FdZdX, d2FdYdZ, d3FdXdYdZ);
  }
  private int nextIndex(int i, int max) {
    final int index = i + 1;
    return index < max ? index : index - 1;
  }
  private int previousIndex(int i) {
    final int index = i - 1;
    return index >= 0 ? index : 0;
  }
}