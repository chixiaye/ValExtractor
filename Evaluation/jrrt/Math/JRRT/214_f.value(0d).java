package org.apache.commons.math3.analysis.integration.gauss;
import org.apache.commons.math3.analysis.UnivariateFunction;
import org.apache.commons.math3.exception.DimensionMismatchException;
import org.apache.commons.math3.exception.NonMonotonicSequenceException;
import org.apache.commons.math3.util.Pair;

public class SymmetricGaussIntegrator extends GaussIntegrator  {
  public SymmetricGaussIntegrator(Pair<double[], double[]> pointsAndWeights) throws NonMonotonicSequenceException {
    this(pointsAndWeights.getFirst(), pointsAndWeights.getSecond());
  }
  public SymmetricGaussIntegrator(double[] points, double[] weights) throws NonMonotonicSequenceException, DimensionMismatchException {
    super(points, weights);
  }
  @Override() public double integrate(UnivariateFunction f) {
    final int ruleLength = getNumberOfPoints();
    if(ruleLength == 1) {
      return getWeight(0) * f.value(0D);
    }
    final int iMax = ruleLength / 2;
    double s = 0;
    double c = 0;
    for(int i = 0; i < iMax; i++) {
      final double p = getPoint(i);
      final double w = getWeight(i);
      final double f1 = f.value(p);
      final double f2 = f.value(-p);
      final double y = w * (f1 + f2) - c;
      final double t = s + y;
      c = (t - s) - y;
      s = t;
    }
    if(ruleLength % 2 != 0) {
      final double w = getWeight(iMax);
      double var_214 = f.value(0D);
      final double y = w * var_214 - c;
      final double t = s + y;
      s = t;
    }
    return s;
  }
}