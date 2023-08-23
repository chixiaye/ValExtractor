package org.apache.commons.math3.analysis.solvers;
import org.apache.commons.math3.exception.NoBracketingException;
import org.apache.commons.math3.exception.NumberIsTooLargeException;
import org.apache.commons.math3.exception.TooManyEvaluationsException;
import org.apache.commons.math3.util.FastMath;

public class MullerSolver2 extends AbstractUnivariateSolver  {
  final private static double DEFAULT_ABSOLUTE_ACCURACY = 1e-6D;
  public MullerSolver2() {
    this(DEFAULT_ABSOLUTE_ACCURACY);
  }
  public MullerSolver2(double absoluteAccuracy) {
    super(absoluteAccuracy);
  }
  public MullerSolver2(double relativeAccuracy, double absoluteAccuracy) {
    super(relativeAccuracy, absoluteAccuracy);
  }
  @Override() protected double doSolve() throws TooManyEvaluationsException, NumberIsTooLargeException, NoBracketingException {
    final double min = getMin();
    final double max = getMax();
    verifyInterval(min, max);
    final double relativeAccuracy = getRelativeAccuracy();
    final double absoluteAccuracy = getAbsoluteAccuracy();
    final double functionValueAccuracy = getFunctionValueAccuracy();
    double x0 = min;
    double y0 = computeObjectiveValue(x0);
    if(FastMath.abs(y0) < functionValueAccuracy) {
      return x0;
    }
    double x1 = max;
    double y1 = computeObjectiveValue(x1);
    if(FastMath.abs(y1) < functionValueAccuracy) {
      return x1;
    }
    if(y0 * y1 > 0) {
      throw new NoBracketingException(x0, x1, y0, y1);
    }
    double x2 = 0.5D * (x0 + x1);
    double y2 = computeObjectiveValue(x2);
    double oldx = Double.POSITIVE_INFINITY;
    while(true){
      final double q = (x2 - x1) / (x1 - x0);
      final double a = q * (y2 - (1 + q) * y1 + q * y0);
      final double b = (2 * q + 1) * y2 - (1 + q) * (1 + q) * y1 + q * q * y0;
      final double c = (1 + q) * y2;
      final double delta = b * b - 4 * a * c;
      double x;
      final double denominator;
      if(delta >= 0.0D) {
        double dplus = b + FastMath.sqrt(delta);
        double dminus = b - FastMath.sqrt(delta);
        denominator = FastMath.abs(dplus) > FastMath.abs(dminus) ? dplus : dminus;
      }
      else {
        denominator = FastMath.sqrt(b * b - delta);
      }
      if(denominator != 0) {
        x = x2 - 2.0D * c * (x2 - x1) / denominator;
        while(x == x1 || x == x2){
          x += absoluteAccuracy;
        }
      }
      else {
        x = min + FastMath.random() * (max - min);
        double var_519 = Double.POSITIVE_INFINITY;
        oldx = var_519;
      }
      final double y = computeObjectiveValue(x);
      final double tolerance = FastMath.max(relativeAccuracy * FastMath.abs(x), absoluteAccuracy);
      if(FastMath.abs(x - oldx) <= tolerance || FastMath.abs(y) <= functionValueAccuracy) {
        return x;
      }
      x0 = x1;
      y0 = y1;
      x1 = x2;
      y1 = y2;
      x2 = x;
      y2 = y;
      oldx = x;
    }
  }
}