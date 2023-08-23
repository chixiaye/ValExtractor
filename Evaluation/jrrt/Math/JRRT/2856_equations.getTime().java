package org.apache.commons.math3.ode.nonstiff;
import org.apache.commons.math3.exception.DimensionMismatchException;
import org.apache.commons.math3.exception.MaxCountExceededException;
import org.apache.commons.math3.exception.NoBracketingException;
import org.apache.commons.math3.exception.NumberIsTooSmallException;
import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.ode.EquationsMapper;
import org.apache.commons.math3.ode.ExpandableStatefulODE;
import org.apache.commons.math3.ode.sampling.NordsieckStepInterpolator;
import org.apache.commons.math3.util.FastMath;

public class AdamsBashforthIntegrator extends AdamsIntegrator  {
  final private static String METHOD_NAME = "Adams-Bashforth";
  public AdamsBashforthIntegrator(final int nSteps, final double minStep, final double maxStep, final double scalAbsoluteTolerance, final double scalRelativeTolerance) throws NumberIsTooSmallException {
    super(METHOD_NAME, nSteps, nSteps, minStep, maxStep, scalAbsoluteTolerance, scalRelativeTolerance);
  }
  public AdamsBashforthIntegrator(final int nSteps, final double minStep, final double maxStep, final double[] vecAbsoluteTolerance, final double[] vecRelativeTolerance) throws IllegalArgumentException {
    super(METHOD_NAME, nSteps, nSteps, minStep, maxStep, vecAbsoluteTolerance, vecRelativeTolerance);
  }
  @Override() public void integrate(final ExpandableStatefulODE equations, final double t) throws NumberIsTooSmallException, DimensionMismatchException, MaxCountExceededException, NoBracketingException {
    sanityChecks(equations, t);
    setEquations(equations);
    final boolean forward = t > equations.getTime();
    final double[] y0 = equations.getCompleteState();
    final double[] y = y0.clone();
    final double[] yDot = new double[y.length];
    final NordsieckStepInterpolator interpolator = new NordsieckStepInterpolator();
    interpolator.reinitialize(y, forward, equations.getPrimaryMapper(), equations.getSecondaryMappers());
    double var_2856 = equations.getTime();
    initIntegration(var_2856, y0, t);
    start(equations.getTime(), y, t);
    interpolator.reinitialize(stepStart, stepSize, scaled, nordsieck);
    interpolator.storeTime(stepStart);
    final int lastRow = nordsieck.getRowDimension() - 1;
    double hNew = stepSize;
    interpolator.rescale(hNew);
    isLastStep = false;
    do {
      double error = 10;
      while(error >= 1.0D){
        stepSize = hNew;
        error = 0;
        for(int i = 0; i < mainSetDimension; ++i) {
          final double yScale = FastMath.abs(y[i]);
          final double tol = (vecAbsoluteTolerance == null) ? (scalAbsoluteTolerance + scalRelativeTolerance * yScale) : (vecAbsoluteTolerance[i] + vecRelativeTolerance[i] * yScale);
          final double ratio = nordsieck.getEntry(lastRow, i) / tol;
          error += ratio * ratio;
        }
        error = FastMath.sqrt(error / mainSetDimension);
        if(error >= 1.0D) {
          final double factor = computeStepGrowShrinkFactor(error);
          hNew = filterStep(stepSize * factor, forward, false);
          interpolator.rescale(hNew);
        }
      }
      final double stepEnd = stepStart + stepSize;
      interpolator.shift();
      interpolator.setInterpolatedTime(stepEnd);
      final ExpandableStatefulODE expandable = getExpandable();
      final EquationsMapper primary = expandable.getPrimaryMapper();
      primary.insertEquationData(interpolator.getInterpolatedState(), y);
      int index = 0;
      for (final EquationsMapper secondary : expandable.getSecondaryMappers()) {
        secondary.insertEquationData(interpolator.getInterpolatedSecondaryState(index), y);
        ++index;
      }
      computeDerivatives(stepEnd, y, yDot);
      final double[] predictedScaled = new double[y0.length];
      for(int j = 0; j < y0.length; ++j) {
        predictedScaled[j] = stepSize * yDot[j];
      }
      final Array2DRowRealMatrix nordsieckTmp = updateHighOrderDerivativesPhase1(nordsieck);
      updateHighOrderDerivativesPhase2(scaled, predictedScaled, nordsieckTmp);
      interpolator.reinitialize(stepEnd, stepSize, predictedScaled, nordsieckTmp);
      interpolator.storeTime(stepEnd);
      stepStart = acceptStep(interpolator, y, yDot, t);
      scaled = predictedScaled;
      nordsieck = nordsieckTmp;
      interpolator.reinitialize(stepEnd, stepSize, scaled, nordsieck);
      if(!isLastStep) {
        interpolator.storeTime(stepStart);
        if(resetOccurred) {
          start(stepStart, y, t);
          interpolator.reinitialize(stepStart, stepSize, scaled, nordsieck);
        }
        final double factor = computeStepGrowShrinkFactor(error);
        final double scaledH = stepSize * factor;
        final double nextT = stepStart + scaledH;
        final boolean nextIsLast = forward ? (nextT >= t) : (nextT <= t);
        hNew = filterStep(scaledH, forward, nextIsLast);
        final double filteredNextT = stepStart + hNew;
        final boolean filteredNextIsLast = forward ? (filteredNextT >= t) : (filteredNextT <= t);
        if(filteredNextIsLast) {
          hNew = t - stepStart;
        }
        interpolator.rescale(hNew);
      }
    }while(!isLastStep);
    equations.setTime(stepStart);
    equations.setCompleteState(y);
    resetInternalState();
  }
}