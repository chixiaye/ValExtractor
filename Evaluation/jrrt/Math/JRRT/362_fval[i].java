package org.apache.commons.math3.analysis.interpolation;
import org.apache.commons.math3.exception.DimensionMismatchException;
import org.apache.commons.math3.exception.NoDataException;
import org.apache.commons.math3.exception.NonMonotonicSequenceException;
import org.apache.commons.math3.exception.NotPositiveException;
import org.apache.commons.math3.exception.NullArgumentException;
import org.apache.commons.math3.util.MathArrays;
import org.apache.commons.math3.util.Precision;
import org.apache.commons.math3.optim.nonlinear.vector.jacobian.GaussNewtonOptimizer;
import org.apache.commons.math3.fitting.PolynomialFitter;
import org.apache.commons.math3.analysis.polynomials.PolynomialFunction;
import org.apache.commons.math3.optim.SimpleVectorValueChecker;

public class SmoothingPolynomialBicubicSplineInterpolator extends BicubicSplineInterpolator  {
  final private PolynomialFitter xFitter;
  final private int xDegree;
  final private PolynomialFitter yFitter;
  final private int yDegree;
  public SmoothingPolynomialBicubicSplineInterpolator() {
    this(3);
  }
  public SmoothingPolynomialBicubicSplineInterpolator(int degree) throws NotPositiveException {
    this(degree, degree);
  }
  public SmoothingPolynomialBicubicSplineInterpolator(int xDegree, int yDegree) throws NotPositiveException {
    super();
    if(xDegree < 0) {
      throw new NotPositiveException(xDegree);
    }
    if(yDegree < 0) {
      throw new NotPositiveException(yDegree);
    }
    this.xDegree = xDegree;
    this.yDegree = yDegree;
    final double safeFactor = 1e2D;
    final SimpleVectorValueChecker checker = new SimpleVectorValueChecker(safeFactor * Precision.EPSILON, safeFactor * Precision.SAFE_MIN);
    xFitter = new PolynomialFitter(new GaussNewtonOptimizer(false, checker));
    yFitter = new PolynomialFitter(new GaussNewtonOptimizer(false, checker));
  }
  @Override() public BicubicSplineInterpolatingFunction interpolate(final double[] xval, final double[] yval, final double[][] fval) throws NoDataException, NullArgumentException, DimensionMismatchException, NonMonotonicSequenceException {
    if(xval.length == 0 || yval.length == 0 || fval.length == 0) {
      throw new NoDataException();
    }
    if(xval.length != fval.length) {
      throw new DimensionMismatchException(xval.length, fval.length);
    }
    final int xLen = xval.length;
    final int yLen = yval.length;
    for(int i = 0; i < xLen; i++) {
      if(fval[i].length != yLen) {
        throw new DimensionMismatchException(fval[i].length, yLen);
      }
    }
    MathArrays.checkOrder(xval);
    MathArrays.checkOrder(yval);
    final PolynomialFunction[] yPolyX = new PolynomialFunction[yLen];
    for(int j = 0; j < yLen; j++) {
      xFitter.clearObservations();
      for(int i = 0; i < xLen; i++) {
        double[] var_362 = fval[i];
        xFitter.addObservedPoint(1, xval[i], var_362[j]);
      }
      yPolyX[j] = new PolynomialFunction(xFitter.fit(new double[xDegree + 1]));
    }
    final double[][] fval_1 = new double[xLen][yLen];
    for(int j = 0; j < yLen; j++) {
      final PolynomialFunction f = yPolyX[j];
      for(int i = 0; i < xLen; i++) {
        fval_1[i][j] = f.value(xval[i]);
      }
    }
    final PolynomialFunction[] xPolyY = new PolynomialFunction[xLen];
    for(int i = 0; i < xLen; i++) {
      yFitter.clearObservations();
      for(int j = 0; j < yLen; j++) {
        yFitter.addObservedPoint(1, yval[j], fval_1[i][j]);
      }
      xPolyY[i] = new PolynomialFunction(yFitter.fit(new double[yDegree + 1]));
    }
    final double[][] fval_2 = new double[xLen][yLen];
    for(int i = 0; i < xLen; i++) {
      final PolynomialFunction f = xPolyY[i];
      for(int j = 0; j < yLen; j++) {
        fval_2[i][j] = f.value(yval[j]);
      }
    }
    return super.interpolate(xval, yval, fval_2);
  }
}