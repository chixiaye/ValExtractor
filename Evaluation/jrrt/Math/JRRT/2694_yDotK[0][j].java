package org.apache.commons.math3.ode.nonstiff;
import org.apache.commons.math3.exception.DimensionMismatchException;
import org.apache.commons.math3.exception.MaxCountExceededException;
import org.apache.commons.math3.exception.NoBracketingException;
import org.apache.commons.math3.exception.NumberIsTooSmallException;
import org.apache.commons.math3.ode.AbstractIntegrator;
import org.apache.commons.math3.ode.ExpandableStatefulODE;
import org.apache.commons.math3.util.FastMath;

abstract public class RungeKuttaIntegrator extends AbstractIntegrator  {
  final private double[] c;
  final private double[][] a;
  final private double[] b;
  final private RungeKuttaStepInterpolator prototype;
  final private double step;
  protected RungeKuttaIntegrator(final String name, final double[] c, final double[][] a, final double[] b, final RungeKuttaStepInterpolator prototype, final double step) {
    super(name);
    this.c = c;
    this.a = a;
    this.b = b;
    this.prototype = prototype;
    this.step = FastMath.abs(step);
  }
  @Override() public void integrate(final ExpandableStatefulODE equations, final double t) throws NumberIsTooSmallException, DimensionMismatchException, MaxCountExceededException, NoBracketingException {
    sanityChecks(equations, t);
    setEquations(equations);
    final boolean forward = t > equations.getTime();
    final double[] y0 = equations.getCompleteState();
    final double[] y = y0.clone();
    final int stages = c.length + 1;
    final double[][] yDotK = new double[stages][];
    for(int i = 0; i < stages; ++i) {
      yDotK[i] = new double[y0.length];
    }
    final double[] yTmp = y0.clone();
    final double[] yDotTmp = new double[y0.length];
    final RungeKuttaStepInterpolator interpolator = (RungeKuttaStepInterpolator)prototype.copy();
    interpolator.reinitialize(this, yTmp, yDotK, forward, equations.getPrimaryMapper(), equations.getSecondaryMappers());
    interpolator.storeTime(equations.getTime());
    stepStart = equations.getTime();
    stepSize = forward ? step : -step;
    initIntegration(equations.getTime(), y0, t);
    isLastStep = false;
    do {
      interpolator.shift();
      computeDerivatives(stepStart, y, yDotK[0]);
      for(int k = 1; k < stages; ++k) {
        for(int j = 0; j < y0.length; ++j) {
          double sum = a[k - 1][0] * yDotK[0][j];
          for(int l = 1; l < k; ++l) {
            sum += a[k - 1][l] * yDotK[l][j];
          }
          yTmp[j] = y[j] + stepSize * sum;
        }
        computeDerivatives(stepStart + c[k - 1] * stepSize, yTmp, yDotK[k]);
      }
      for(int j = 0; j < y0.length; ++j) {
        double var_2694 = yDotK[0][j];
        double sum = b[0] * var_2694;
        for(int l = 1; l < stages; ++l) {
          sum += b[l] * yDotK[l][j];
        }
        yTmp[j] = y[j] + stepSize * sum;
      }
      interpolator.storeTime(stepStart + stepSize);
      System.arraycopy(yTmp, 0, y, 0, y0.length);
      System.arraycopy(yDotK[stages - 1], 0, yDotTmp, 0, y0.length);
      stepStart = acceptStep(interpolator, y, yDotTmp, t);
      if(!isLastStep) {
        interpolator.storeTime(stepStart);
        final double nextT = stepStart + stepSize;
        final boolean nextIsLast = forward ? (nextT >= t) : (nextT <= t);
        if(nextIsLast) {
          stepSize = t - stepStart;
        }
      }
    }while(!isLastStep);
    equations.setTime(stepStart);
    equations.setCompleteState(y);
    stepStart = Double.NaN;
    stepSize = Double.NaN;
  }
}