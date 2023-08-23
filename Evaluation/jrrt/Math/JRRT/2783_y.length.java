package org.apache.commons.math3.ode.nonstiff;
import java.util.Arrays;
import org.apache.commons.math3.exception.DimensionMismatchException;
import org.apache.commons.math3.exception.MaxCountExceededException;
import org.apache.commons.math3.exception.NoBracketingException;
import org.apache.commons.math3.exception.NumberIsTooSmallException;
import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.RealMatrixPreservingVisitor;
import org.apache.commons.math3.ode.EquationsMapper;
import org.apache.commons.math3.ode.ExpandableStatefulODE;
import org.apache.commons.math3.ode.sampling.NordsieckStepInterpolator;
import org.apache.commons.math3.util.FastMath;

public class AdamsMoultonIntegrator extends AdamsIntegrator  {
  final private static String METHOD_NAME = "Adams-Moulton";
  public AdamsMoultonIntegrator(final int nSteps, final double minStep, final double maxStep, final double scalAbsoluteTolerance, final double scalRelativeTolerance) throws NumberIsTooSmallException {
    super(METHOD_NAME, nSteps, nSteps + 1, minStep, maxStep, scalAbsoluteTolerance, scalRelativeTolerance);
  }
  public AdamsMoultonIntegrator(final int nSteps, final double minStep, final double maxStep, final double[] vecAbsoluteTolerance, final double[] vecRelativeTolerance) throws IllegalArgumentException {
    super(METHOD_NAME, nSteps, nSteps + 1, minStep, maxStep, vecAbsoluteTolerance, vecRelativeTolerance);
  }
  @Override() public void integrate(final ExpandableStatefulODE equations, final double t) throws NumberIsTooSmallException, DimensionMismatchException, MaxCountExceededException, NoBracketingException {
    sanityChecks(equations, t);
    setEquations(equations);
    final boolean forward = t > equations.getTime();
    final double[] y0 = equations.getCompleteState();
    final double[] y = y0.clone();
    final double[] yDot = new double[y.length];
    final double[] yTmp = new double[y.length];
    final double[] predictedScaled = new double[y.length];
    Array2DRowRealMatrix nordsieckTmp = null;
    final NordsieckStepInterpolator interpolator = new NordsieckStepInterpolator();
    interpolator.reinitialize(y, forward, equations.getPrimaryMapper(), equations.getSecondaryMappers());
    initIntegration(equations.getTime(), y0, t);
    start(equations.getTime(), y, t);
    interpolator.reinitialize(stepStart, stepSize, scaled, nordsieck);
    interpolator.storeTime(stepStart);
    double hNew = stepSize;
    interpolator.rescale(hNew);
    isLastStep = false;
    do {
      double error = 10;
      while(error >= 1.0D){
        stepSize = hNew;
        final double stepEnd = stepStart + stepSize;
        interpolator.setInterpolatedTime(stepEnd);
        final ExpandableStatefulODE expandable = getExpandable();
        final EquationsMapper primary = expandable.getPrimaryMapper();
        primary.insertEquationData(interpolator.getInterpolatedState(), yTmp);
        int index = 0;
        for (final EquationsMapper secondary : expandable.getSecondaryMappers()) {
          secondary.insertEquationData(interpolator.getInterpolatedSecondaryState(index), yTmp);
          ++index;
        }
        computeDerivatives(stepEnd, yTmp, yDot);
        for(int j = 0; j < y0.length; ++j) {
          predictedScaled[j] = stepSize * yDot[j];
        }
        nordsieckTmp = updateHighOrderDerivativesPhase1(nordsieck);
        updateHighOrderDerivativesPhase2(scaled, predictedScaled, nordsieckTmp);
        error = nordsieckTmp.walkInOptimizedOrder(new Corrector(y, predictedScaled, yTmp));
        if(error >= 1.0D) {
          final double factor = computeStepGrowShrinkFactor(error);
          hNew = filterStep(stepSize * factor, forward, false);
          interpolator.rescale(hNew);
        }
      }
      final double stepEnd = stepStart + stepSize;
      computeDerivatives(stepEnd, yTmp, yDot);
      final double[] correctedScaled = new double[y0.length];
      for(int j = 0; j < y0.length; ++j) {
        correctedScaled[j] = stepSize * yDot[j];
      }
      updateHighOrderDerivativesPhase2(predictedScaled, correctedScaled, nordsieckTmp);
      int var_2783 = y.length;
      System.arraycopy(yTmp, 0, y, 0, var_2783);
      interpolator.reinitialize(stepEnd, stepSize, correctedScaled, nordsieckTmp);
      interpolator.storeTime(stepStart);
      interpolator.shift();
      interpolator.storeTime(stepEnd);
      stepStart = acceptStep(interpolator, y, yDot, t);
      scaled = correctedScaled;
      nordsieck = nordsieckTmp;
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
  
  private class Corrector implements RealMatrixPreservingVisitor  {
    final private double[] previous;
    final private double[] scaled;
    final private double[] before;
    final private double[] after;
    public Corrector(final double[] previous, final double[] scaled, final double[] state) {
      super();
      this.previous = previous;
      this.scaled = scaled;
      this.after = state;
      this.before = state.clone();
    }
    public double end() {
      double error = 0;
      for(int i = 0; i < after.length; ++i) {
        after[i] += previous[i] + scaled[i];
        if(i < mainSetDimension) {
          final double yScale = FastMath.max(FastMath.abs(previous[i]), FastMath.abs(after[i]));
          final double tol = (vecAbsoluteTolerance == null) ? (scalAbsoluteTolerance + scalRelativeTolerance * yScale) : (vecAbsoluteTolerance[i] + vecRelativeTolerance[i] * yScale);
          final double ratio = (after[i] - before[i]) / tol;
          error += ratio * ratio;
        }
      }
      return FastMath.sqrt(error / mainSetDimension);
    }
    public void start(int rows, int columns, int startRow, int endRow, int startColumn, int endColumn) {
      Arrays.fill(after, 0.0D);
    }
    public void visit(int row, int column, double value) {
      if((row & 0x1) == 0) {
        after[column] -= value;
      }
      else {
        after[column] += value;
      }
    }
  }
}