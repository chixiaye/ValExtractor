package org.apache.commons.math3.optimization.direct;
import java.util.Comparator;
import org.apache.commons.math3.optimization.PointValuePair;
import org.apache.commons.math3.analysis.MultivariateFunction;

@Deprecated() public class NelderMeadSimplex extends AbstractSimplex  {
  final private static double DEFAULT_RHO = 1;
  final private static double DEFAULT_KHI = 2;
  final private static double DEFAULT_GAMMA = 0.5D;
  final private static double DEFAULT_SIGMA = 0.5D;
  final private double rho;
  final private double khi;
  final private double gamma;
  final private double sigma;
  public NelderMeadSimplex(final double[] steps) {
    this(steps, DEFAULT_RHO, DEFAULT_KHI, DEFAULT_GAMMA, DEFAULT_SIGMA);
  }
  public NelderMeadSimplex(final double[] steps, final double rho, final double khi, final double gamma, final double sigma) {
    super(steps);
    this.rho = rho;
    this.khi = khi;
    this.gamma = gamma;
    this.sigma = sigma;
  }
  public NelderMeadSimplex(final double[][] referenceSimplex) {
    this(referenceSimplex, DEFAULT_RHO, DEFAULT_KHI, DEFAULT_GAMMA, DEFAULT_SIGMA);
  }
  public NelderMeadSimplex(final double[][] referenceSimplex, final double rho, final double khi, final double gamma, final double sigma) {
    super(referenceSimplex);
    this.rho = rho;
    this.khi = khi;
    this.gamma = gamma;
    this.sigma = sigma;
  }
  public NelderMeadSimplex(final int n) {
    this(n, 1D);
  }
  public NelderMeadSimplex(final int n, double sideLength) {
    this(n, sideLength, DEFAULT_RHO, DEFAULT_KHI, DEFAULT_GAMMA, DEFAULT_SIGMA);
  }
  public NelderMeadSimplex(final int n, double sideLength, final double rho, final double khi, final double gamma, final double sigma) {
    super(n, sideLength);
    this.rho = rho;
    this.khi = khi;
    this.gamma = gamma;
    this.sigma = sigma;
  }
  public NelderMeadSimplex(final int n, final double rho, final double khi, final double gamma, final double sigma) {
    this(n, 1D, rho, khi, gamma, sigma);
  }
  @Override() public void iterate(final MultivariateFunction evaluationFunction, final Comparator<PointValuePair> comparator) {
    final int n = getDimension();
    final PointValuePair best = getPoint(0);
    final PointValuePair secondBest = getPoint(n - 1);
    final PointValuePair worst = getPoint(n);
    final double[] xWorst = worst.getPointRef();
    final double[] centroid = new double[n];
    for(int i = 0; i < n; i++) {
      final double[] x = getPoint(i).getPointRef();
      for(int j = 0; j < n; j++) {
        centroid[j] += x[j];
      }
    }
    final double scaling = 1.0D / n;
    for(int j = 0; j < n; j++) {
      centroid[j] *= scaling;
    }
    final double[] xR = new double[n];
    for(int j = 0; j < n; j++) {
      xR[j] = centroid[j] + rho * (centroid[j] - xWorst[j]);
    }
    final PointValuePair reflected = new PointValuePair(xR, evaluationFunction.value(xR), false);
    if(comparator.compare(best, reflected) <= 0 && comparator.compare(reflected, secondBest) < 0) {
      replaceWorstPoint(reflected, comparator);
    }
    else 
      if(comparator.compare(reflected, best) < 0) {
        final double[] xE = new double[n];
        for(int j = 0; j < n; j++) {
          xE[j] = centroid[j] + khi * (xR[j] - centroid[j]);
        }
        final PointValuePair expanded = new PointValuePair(xE, evaluationFunction.value(xE), false);
        if(comparator.compare(expanded, reflected) < 0) {
          replaceWorstPoint(expanded, comparator);
        }
        else {
          replaceWorstPoint(reflected, comparator);
        }
      }
      else {
        if(comparator.compare(reflected, worst) < 0) {
          final double[] xC = new double[n];
          for(int j = 0; j < n; j++) {
            xC[j] = centroid[j] + gamma * (xR[j] - centroid[j]);
          }
          final PointValuePair outContracted = new PointValuePair(xC, evaluationFunction.value(xC), false);
          if(comparator.compare(outContracted, reflected) <= 0) {
            replaceWorstPoint(outContracted, comparator);
            return ;
          }
        }
        else {
          final double[] xC = new double[n];
          for(int j = 0; j < n; j++) {
            xC[j] = centroid[j] - gamma * (centroid[j] - xWorst[j]);
          }
          final PointValuePair inContracted = new PointValuePair(xC, evaluationFunction.value(xC), false);
          if(comparator.compare(inContracted, worst) < 0) {
            replaceWorstPoint(inContracted, comparator);
            return ;
          }
        }
        final double[] xSmallest = getPoint(0).getPointRef();
        for(int i = 1; i <= n; i++) {
          final double[] x = getPoint(i).getPoint();
          for(int j = 0; j < n; j++) {
            double var_3329 = xSmallest[j];
            x[j] = xSmallest[j] + sigma * (x[j] - var_3329);
          }
          setPoint(i, new PointValuePair(x, Double.NaN, false));
        }
        evaluate(evaluationFunction, comparator);
      }
  }
}