package org.apache.commons.math3.analysis.interpolation;
import org.apache.commons.math3.analysis.UnivariateFunction;
import org.apache.commons.math3.util.MathUtils;
import org.apache.commons.math3.util.MathArrays;
import org.apache.commons.math3.exception.MathIllegalArgumentException;
import org.apache.commons.math3.exception.NonMonotonicSequenceException;
import org.apache.commons.math3.exception.NumberIsTooSmallException;

public class UnivariatePeriodicInterpolator implements UnivariateInterpolator  {
  final public static int DEFAULT_EXTEND = 5;
  final private UnivariateInterpolator interpolator;
  final private double period;
  final private int extend;
  public UnivariatePeriodicInterpolator(UnivariateInterpolator interpolator, double period) {
    this(interpolator, period, DEFAULT_EXTEND);
  }
  public UnivariatePeriodicInterpolator(UnivariateInterpolator interpolator, double period, int extend) {
    super();
    this.interpolator = interpolator;
    this.period = period;
    this.extend = extend;
  }
  public UnivariateFunction interpolate(double[] xval, double[] yval) throws NumberIsTooSmallException, NonMonotonicSequenceException {
    if(xval.length < extend) {
      throw new NumberIsTooSmallException(xval.length, extend, true);
    }
    MathArrays.checkOrder(xval);
    final double offset = xval[0];
    final int len = xval.length + extend * 2;
    final double[] x = new double[len];
    final double[] y = new double[len];
    for(int i = 0; i < xval.length; i++) {
      final int index = i + extend;
      double var_417 = MathUtils.reduce(xval[i], period, offset);
      x[index] = var_417;
      y[index] = yval[i];
    }
    for(int i = 0; i < extend; i++) {
      int index = xval.length - extend + i;
      x[i] = MathUtils.reduce(xval[index], period, offset) - period;
      y[i] = yval[index];
      index = len - extend + i;
      x[index] = MathUtils.reduce(xval[i], period, offset) + period;
      y[index] = yval[i];
    }
    MathArrays.sortInPlace(x, y);
    final UnivariateFunction f = interpolator.interpolate(x, y);
    return new UnivariateFunction() {
        public double value(final double x) throws MathIllegalArgumentException {
          return f.value(MathUtils.reduce(x, period, offset));
        }
    };
  }
}