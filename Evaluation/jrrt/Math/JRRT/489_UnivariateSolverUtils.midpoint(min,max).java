package org.apache.commons.math3.analysis.solvers;
import org.apache.commons.math3.util.FastMath;
import org.apache.commons.math3.exception.TooManyEvaluationsException;

public class BisectionSolver extends AbstractUnivariateSolver  {
  final private static double DEFAULT_ABSOLUTE_ACCURACY = 1e-6D;
  public BisectionSolver() {
    this(DEFAULT_ABSOLUTE_ACCURACY);
  }
  public BisectionSolver(double absoluteAccuracy) {
    super(absoluteAccuracy);
  }
  public BisectionSolver(double relativeAccuracy, double absoluteAccuracy) {
    super(relativeAccuracy, absoluteAccuracy);
  }
  @Override() protected double doSolve() throws TooManyEvaluationsException {
    double min = getMin();
    double max = getMax();
    verifyInterval(min, max);
    final double absoluteAccuracy = getAbsoluteAccuracy();
    double m;
    double fm;
    double fmin;
    while(true){
      m = UnivariateSolverUtils.midpoint(min, max);
      fmin = computeObjectiveValue(min);
      fm = computeObjectiveValue(m);
      if(fm * fmin > 0) {
        min = m;
      }
      else {
        max = m;
      }
      if(FastMath.abs(max - min) <= absoluteAccuracy) {
        double var_489 = UnivariateSolverUtils.midpoint(min, max);
        m = var_489;
        return m;
      }
    }
  }
}