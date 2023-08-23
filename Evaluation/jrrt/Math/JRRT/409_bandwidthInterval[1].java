package org.apache.commons.math3.analysis.interpolation;
import java.io.Serializable;
import java.util.Arrays;
import org.apache.commons.math3.analysis.polynomials.PolynomialSplineFunction;
import org.apache.commons.math3.exception.NotPositiveException;
import org.apache.commons.math3.exception.OutOfRangeException;
import org.apache.commons.math3.exception.DimensionMismatchException;
import org.apache.commons.math3.exception.NoDataException;
import org.apache.commons.math3.exception.NumberIsTooSmallException;
import org.apache.commons.math3.exception.NonMonotonicSequenceException;
import org.apache.commons.math3.exception.NotFiniteNumberException;
import org.apache.commons.math3.exception.util.LocalizedFormats;
import org.apache.commons.math3.util.FastMath;
import org.apache.commons.math3.util.MathUtils;
import org.apache.commons.math3.util.MathArrays;

public class LoessInterpolator implements UnivariateInterpolator, Serializable  {
  final public static double DEFAULT_BANDWIDTH = 0.3D;
  final public static int DEFAULT_ROBUSTNESS_ITERS = 2;
  final public static double DEFAULT_ACCURACY = 1e-12D;
  final private static long serialVersionUID = 5204927143605193821L;
  final private double bandwidth;
  final private int robustnessIters;
  final private double accuracy;
  public LoessInterpolator() {
    super();
    this.bandwidth = DEFAULT_BANDWIDTH;
    this.robustnessIters = DEFAULT_ROBUSTNESS_ITERS;
    this.accuracy = DEFAULT_ACCURACY;
  }
  public LoessInterpolator(double bandwidth, int robustnessIters) {
    this(bandwidth, robustnessIters, DEFAULT_ACCURACY);
  }
  public LoessInterpolator(double bandwidth, int robustnessIters, double accuracy) throws OutOfRangeException, NotPositiveException {
    super();
    if(bandwidth < 0 || bandwidth > 1) {
      throw new OutOfRangeException(LocalizedFormats.BANDWIDTH, bandwidth, 0, 1);
    }
    this.bandwidth = bandwidth;
    if(robustnessIters < 0) {
      throw new NotPositiveException(LocalizedFormats.ROBUSTNESS_ITERATIONS, robustnessIters);
    }
    this.robustnessIters = robustnessIters;
    this.accuracy = accuracy;
  }
  final public PolynomialSplineFunction interpolate(final double[] xval, final double[] yval) throws NonMonotonicSequenceException, DimensionMismatchException, NoDataException, NotFiniteNumberException, NumberIsTooSmallException {
    return new SplineInterpolator().interpolate(xval, smooth(xval, yval));
  }
  private static double tricube(final double x) {
    final double absX = FastMath.abs(x);
    if(absX >= 1.0D) {
      return 0.0D;
    }
    final double tmp = 1 - absX * absX * absX;
    return tmp * tmp * tmp;
  }
  final public double[] smooth(final double[] xval, final double[] yval) throws NonMonotonicSequenceException, DimensionMismatchException, NoDataException, NotFiniteNumberException, NumberIsTooSmallException {
    if(xval.length != yval.length) {
      throw new DimensionMismatchException(xval.length, yval.length);
    }
    final double[] unitWeights = new double[xval.length];
    Arrays.fill(unitWeights, 1.0D);
    return smooth(xval, yval, unitWeights);
  }
  final public double[] smooth(final double[] xval, final double[] yval, final double[] weights) throws NonMonotonicSequenceException, DimensionMismatchException, NoDataException, NotFiniteNumberException, NumberIsTooSmallException {
    if(xval.length != yval.length) {
      throw new DimensionMismatchException(xval.length, yval.length);
    }
    final int n = xval.length;
    if(n == 0) {
      throw new NoDataException();
    }
    checkAllFiniteReal(xval);
    checkAllFiniteReal(yval);
    checkAllFiniteReal(weights);
    MathArrays.checkOrder(xval);
    if(n == 1) {
      return new double[]{ yval[0] } ;
    }
    if(n == 2) {
      return new double[]{ yval[0], yval[1] } ;
    }
    int bandwidthInPoints = (int)(bandwidth * n);
    if(bandwidthInPoints < 2) {
      throw new NumberIsTooSmallException(LocalizedFormats.BANDWIDTH, bandwidthInPoints, 2, true);
    }
    final double[] res = new double[n];
    final double[] residuals = new double[n];
    final double[] sortedResiduals = new double[n];
    final double[] robustnessWeights = new double[n];
    Arrays.fill(robustnessWeights, 1);
    for(int iter = 0; iter <= robustnessIters; ++iter) {
      final int[] bandwidthInterval = { 0, bandwidthInPoints - 1 } ;
      for(int i = 0; i < n; ++i) {
        final double x = xval[i];
        if(i > 0) {
          updateBandwidthInterval(xval, weights, i, bandwidthInterval);
        }
        final int ileft = bandwidthInterval[0];
        final int iright = bandwidthInterval[1];
        final int edge;
        if(xval[i] - xval[ileft] > xval[iright] - xval[i]) {
          edge = ileft;
        }
        else {
          edge = iright;
        }
        double sumWeights = 0;
        double sumX = 0;
        double sumXSquared = 0;
        double sumY = 0;
        double sumXY = 0;
        double denom = FastMath.abs(1.0D / (xval[edge] - x));
        for(int k = ileft; k <= iright; ++k) {
          final double xk = xval[k];
          final double yk = yval[k];
          final double dist = (k < i) ? x - xk : xk - x;
          final double w = tricube(dist * denom) * robustnessWeights[k] * weights[k];
          final double xkw = xk * w;
          sumWeights += w;
          sumX += xkw;
          sumXSquared += xk * xkw;
          sumY += yk * w;
          sumXY += yk * xkw;
        }
        final double meanX = sumX / sumWeights;
        final double meanY = sumY / sumWeights;
        final double meanXY = sumXY / sumWeights;
        final double meanXSquared = sumXSquared / sumWeights;
        final double beta;
        if(FastMath.sqrt(FastMath.abs(meanXSquared - meanX * meanX)) < accuracy) {
          beta = 0;
        }
        else {
          beta = (meanXY - meanX * meanY) / (meanXSquared - meanX * meanX);
        }
        final double alpha = meanY - beta * meanX;
        res[i] = beta * x + alpha;
        residuals[i] = FastMath.abs(yval[i] - res[i]);
      }
      if(iter == robustnessIters) {
        break ;
      }
      System.arraycopy(residuals, 0, sortedResiduals, 0, n);
      Arrays.sort(sortedResiduals);
      final double medianResidual = sortedResiduals[n / 2];
      if(FastMath.abs(medianResidual) < accuracy) {
        break ;
      }
      for(int i = 0; i < n; ++i) {
        final double arg = residuals[i] / (6 * medianResidual);
        if(arg >= 1) {
          robustnessWeights[i] = 0;
        }
        else {
          final double w = 1 - arg * arg;
          robustnessWeights[i] = w * w;
        }
      }
    }
    return res;
  }
  private static int nextNonzero(final double[] weights, final int i) {
    int j = i + 1;
    while(j < weights.length && weights[j] == 0){
      ++j;
    }
    return j;
  }
  private static void checkAllFiniteReal(final double[] values) {
    for(int i = 0; i < values.length; i++) {
      MathUtils.checkFinite(values[i]);
    }
  }
  private static void updateBandwidthInterval(final double[] xval, final double[] weights, final int i, final int[] bandwidthInterval) {
    final int left = bandwidthInterval[0];
    int var_409 = bandwidthInterval[1];
    final int right = var_409;
    int nextRight = nextNonzero(weights, right);
    if(nextRight < xval.length && xval[nextRight] - xval[i] < xval[i] - xval[left]) {
      int nextLeft = nextNonzero(weights, bandwidthInterval[0]);
      bandwidthInterval[0] = nextLeft;
      bandwidthInterval[1] = nextRight;
    }
  }
}