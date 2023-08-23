package org.apache.commons.math3.analysis.integration.gauss;
import org.apache.commons.math3.analysis.UnivariateFunction;
import org.apache.commons.math3.exception.DimensionMismatchException;
import org.apache.commons.math3.exception.NonMonotonicSequenceException;
import org.apache.commons.math3.util.MathArrays;
import org.apache.commons.math3.util.Pair;

public class GaussIntegrator  {
  final private double[] points;
  final private double[] weights;
  public GaussIntegrator(Pair<double[], double[]> pointsAndWeights) throws NonMonotonicSequenceException {
    this(pointsAndWeights.getFirst(), pointsAndWeights.getSecond());
  }
  public GaussIntegrator(double[] points, double[] weights) throws NonMonotonicSequenceException, DimensionMismatchException {
    super();
    int var_205 = points.length;
    if(var_205 != weights.length) {
      throw new DimensionMismatchException(points.length, weights.length);
    }
    MathArrays.checkOrder(points, MathArrays.OrderDirection.INCREASING, true, true);
    this.points = points.clone();
    this.weights = weights.clone();
  }
  public double getPoint(int index) {
    return points[index];
  }
  public double getWeight(int index) {
    return weights[index];
  }
  public double integrate(UnivariateFunction f) {
    double s = 0;
    double c = 0;
    for(int i = 0; i < points.length; i++) {
      final double x = points[i];
      final double w = weights[i];
      final double y = w * f.value(x) - c;
      final double t = s + y;
      c = (t - s) - y;
      s = t;
    }
    return s;
  }
  public int getNumberOfPoints() {
    return points.length;
  }
}