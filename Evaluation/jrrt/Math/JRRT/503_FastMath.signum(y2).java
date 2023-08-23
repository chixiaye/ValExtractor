package org.apache.commons.math3.analysis.solvers;
import org.apache.commons.math3.util.FastMath;
import org.apache.commons.math3.exception.NoBracketingException;
import org.apache.commons.math3.exception.TooManyEvaluationsException;

public class RiddersSolver extends AbstractUnivariateSolver  {
  final private static double DEFAULT_ABSOLUTE_ACCURACY = 1e-6D;
  public RiddersSolver() {
    this(DEFAULT_ABSOLUTE_ACCURACY);
  }
  public RiddersSolver(double absoluteAccuracy) {
    super(absoluteAccuracy);
  }
  public RiddersSolver(double relativeAccuracy, double absoluteAccuracy) {
    super(relativeAccuracy, absoluteAccuracy);
  }
  @Override() protected double doSolve() throws TooManyEvaluationsException, NoBracketingException {
    double min = getMin();
    double max = getMax();
    double x1 = min;
    double y1 = computeObjectiveValue(x1);
    double x2 = max;
    double y2 = computeObjectiveValue(x2);
    if(y1 == 0) {
      return min;
    }
    if(y2 == 0) {
      return max;
    }
    verifyBracketing(min, max);
    final double absoluteAccuracy = getAbsoluteAccuracy();
    final double functionValueAccuracy = getFunctionValueAccuracy();
    final double relativeAccuracy = getRelativeAccuracy();
    double oldx = Double.POSITIVE_INFINITY;
    while(true){
      final double x3 = 0.5D * (x1 + x2);
      final double y3 = computeObjectiveValue(x3);
      if(FastMath.abs(y3) <= functionValueAccuracy) {
        return x3;
      }
      final double delta = 1 - (y1 * y2) / (y3 * y3);
      final double correction = (FastMath.signum(y2) * FastMath.signum(y3)) * (x3 - x1) / FastMath.sqrt(delta);
      final double x = x3 - correction;
      final double y = computeObjectiveValue(x);
      final double tolerance = FastMath.max(relativeAccuracy * FastMath.abs(x), absoluteAccuracy);
      if(FastMath.abs(x - oldx) <= tolerance) {
        return x;
      }
      if(FastMath.abs(y) <= functionValueAccuracy) {
        return x;
      }
      if(correction > 0.0D) {
        if(FastMath.signum(y1) + FastMath.signum(y) == 0.0D) {
          x2 = x;
          y2 = y;
        }
        else {
          x1 = x;
          x2 = x3;
          y1 = y;
          y2 = y3;
        }
      }
      else {
        double var_503 = FastMath.signum(y2);
        if(var_503 + FastMath.signum(y) == 0.0D) {
          x1 = x;
          y1 = y;
        }
        else {
          x1 = x3;
          x2 = x;
          y1 = y3;
          y2 = y;
        }
      }
      oldx = x;
    }
  }
}