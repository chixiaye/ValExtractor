package org.apache.commons.math3.ode;
import org.apache.commons.math3.exception.DimensionMismatchException;
import org.apache.commons.math3.exception.MaxCountExceededException;
import org.apache.commons.math3.exception.NoBracketingException;
import org.apache.commons.math3.exception.NumberIsTooSmallException;
import org.apache.commons.math3.exception.util.LocalizedFormats;
import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.ode.nonstiff.AdaptiveStepsizeIntegrator;
import org.apache.commons.math3.ode.nonstiff.DormandPrince853Integrator;
import org.apache.commons.math3.ode.sampling.StepHandler;
import org.apache.commons.math3.ode.sampling.StepInterpolator;
import org.apache.commons.math3.util.FastMath;

abstract public class MultistepIntegrator extends AdaptiveStepsizeIntegrator  {
  protected double[] scaled;
  protected Array2DRowRealMatrix nordsieck;
  private FirstOrderIntegrator starter;
  final private int nSteps;
  private double exp;
  private double safety;
  private double minReduction;
  private double maxGrowth;
  protected MultistepIntegrator(final String name, final int nSteps, final int order, final double minStep, final double maxStep, final double scalAbsoluteTolerance, final double scalRelativeTolerance) throws NumberIsTooSmallException {
    super(name, minStep, maxStep, scalAbsoluteTolerance, scalRelativeTolerance);
    if(nSteps < 2) {
      throw new NumberIsTooSmallException(LocalizedFormats.INTEGRATION_METHOD_NEEDS_AT_LEAST_TWO_PREVIOUS_POINTS, nSteps, 2, true);
    }
    starter = new DormandPrince853Integrator(minStep, maxStep, scalAbsoluteTolerance, scalRelativeTolerance);
    this.nSteps = nSteps;
    exp = -1.0D / order;
    setSafety(0.9D);
    setMinReduction(0.2D);
    setMaxGrowth(FastMath.pow(2.0D, -exp));
  }
  protected MultistepIntegrator(final String name, final int nSteps, final int order, final double minStep, final double maxStep, final double[] vecAbsoluteTolerance, final double[] vecRelativeTolerance) {
    super(name, minStep, maxStep, vecAbsoluteTolerance, vecRelativeTolerance);
    starter = new DormandPrince853Integrator(minStep, maxStep, vecAbsoluteTolerance, vecRelativeTolerance);
    this.nSteps = nSteps;
    exp = -1.0D / order;
    setSafety(0.9D);
    setMinReduction(0.2D);
    setMaxGrowth(FastMath.pow(2.0D, -exp));
  }
  abstract protected Array2DRowRealMatrix initializeHighOrderDerivatives(final double h, final double[] t, final double[][] y, final double[][] yDot);
  public ODEIntegrator getStarterIntegrator() {
    return starter;
  }
  protected double computeStepGrowShrinkFactor(final double error) {
    return FastMath.min(maxGrowth, FastMath.max(minReduction, safety * FastMath.pow(error, exp)));
  }
  public double getMaxGrowth() {
    return maxGrowth;
  }
  public double getMinReduction() {
    return minReduction;
  }
  public double getSafety() {
    return safety;
  }
  public void setMaxGrowth(final double maxGrowth) {
    this.maxGrowth = maxGrowth;
  }
  public void setMinReduction(final double minReduction) {
    this.minReduction = minReduction;
  }
  public void setSafety(final double safety) {
    this.safety = safety;
  }
  public void setStarterIntegrator(FirstOrderIntegrator starterIntegrator) {
    this.starter = starterIntegrator;
  }
  protected void start(final double t0, final double[] y0, final double t) throws DimensionMismatchException, NumberIsTooSmallException, MaxCountExceededException, NoBracketingException {
    starter.clearEventHandlers();
    starter.clearStepHandlers();
    starter.addStepHandler(new NordsieckInitializer(nSteps, y0.length));
    try {
      if(starter instanceof AbstractIntegrator) {
        ((AbstractIntegrator)starter).integrate(getExpandable(), t);
      }
      else {
        starter.integrate(new FirstOrderDifferentialEquations() {
            public int getDimension() {
              return getExpandable().getTotalDimension();
            }
            public void computeDerivatives(double t, double[] y, double[] yDot) {
              getExpandable().computeDerivatives(t, y, yDot);
            }
        }, t0, y0, t, new double[y0.length]);
      }
    }
    catch (InitializationCompletedMarkerException icme) {
      getEvaluationsCounter().incrementCount(starter.getEvaluations());
    }
    starter.clearStepHandlers();
  }
  
  private static class InitializationCompletedMarkerException extends RuntimeException  {
    final private static long serialVersionUID = -1914085471038046418L;
    public InitializationCompletedMarkerException() {
      super((Throwable)null);
    }
  }
  
  private class NordsieckInitializer implements StepHandler  {
    private int count;
    final private double[] t;
    final private double[][] y;
    final private double[][] yDot;
    public NordsieckInitializer(final int nSteps, final int n) {
      super();
      this.count = 0;
      this.t = new double[nSteps];
      this.y = new double[nSteps][n];
      this.yDot = new double[nSteps][n];
    }
    public void handleStep(StepInterpolator interpolator, boolean isLast) throws MaxCountExceededException {
      final double prev = interpolator.getPreviousTime();
      final double curr = interpolator.getCurrentTime();
      if(count == 0) {
        interpolator.setInterpolatedTime(prev);
        t[0] = prev;
        final ExpandableStatefulODE expandable = getExpandable();
        final EquationsMapper primary = expandable.getPrimaryMapper();
        primary.insertEquationData(interpolator.getInterpolatedState(), y[count]);
        primary.insertEquationData(interpolator.getInterpolatedDerivatives(), yDot[count]);
        int index = 0;
        for (final EquationsMapper secondary : expandable.getSecondaryMappers()) {
          secondary.insertEquationData(interpolator.getInterpolatedSecondaryState(index), y[count]);
          secondary.insertEquationData(interpolator.getInterpolatedSecondaryDerivatives(index), yDot[count]);
          ++index;
        }
      }
      ++count;
      interpolator.setInterpolatedTime(curr);
      t[count] = curr;
      final ExpandableStatefulODE expandable = getExpandable();
      final EquationsMapper primary = expandable.getPrimaryMapper();
      primary.insertEquationData(interpolator.getInterpolatedState(), y[count]);
      primary.insertEquationData(interpolator.getInterpolatedDerivatives(), yDot[count]);
      int index = 0;
      for (final EquationsMapper secondary : expandable.getSecondaryMappers()) {
        secondary.insertEquationData(interpolator.getInterpolatedSecondaryState(index), y[count]);
        secondary.insertEquationData(interpolator.getInterpolatedSecondaryDerivatives(index), yDot[count]);
        ++index;
      }
      if(count == t.length - 1) {
        double var_2547 = t[0];
        stepStart = var_2547;
        stepSize = (t[t.length - 1] - t[0]) / (t.length - 1);
        scaled = yDot[0].clone();
        for(int j = 0; j < scaled.length; ++j) {
          scaled[j] *= stepSize;
        }
        nordsieck = initializeHighOrderDerivatives(stepSize, t, y, yDot);
        throw new InitializationCompletedMarkerException();
      }
    }
    public void init(double t0, double[] y0, double time) {
    }
  }
  
  public interface NordsieckTransformer  {
    Array2DRowRealMatrix initializeHighOrderDerivatives(final double h, final double[] t, final double[][] y, final double[][] yDot);
  }
}