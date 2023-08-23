package org.apache.commons.math3.ode;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.math3.exception.DimensionMismatchException;
import org.apache.commons.math3.exception.MathIllegalArgumentException;
import org.apache.commons.math3.exception.MaxCountExceededException;
import org.apache.commons.math3.exception.util.LocalizedFormats;
import org.apache.commons.math3.ode.sampling.StepHandler;
import org.apache.commons.math3.ode.sampling.StepInterpolator;
import org.apache.commons.math3.util.FastMath;

public class ContinuousOutputModel implements StepHandler, Serializable  {
  final private static long serialVersionUID = -1417964919405031606L;
  private double initialTime;
  private double finalTime;
  private boolean forward;
  private int index;
  private List<StepInterpolator> steps;
  public ContinuousOutputModel() {
    super();
    steps = new ArrayList<StepInterpolator>();
    initialTime = Double.NaN;
    finalTime = Double.NaN;
    forward = true;
    index = 0;
  }
  public double getFinalTime() {
    return finalTime;
  }
  public double getInitialTime() {
    return initialTime;
  }
  public double getInterpolatedTime() {
    return steps.get(index).getInterpolatedTime();
  }
  public double[] getInterpolatedSecondaryState(final int secondaryStateIndex) throws MaxCountExceededException {
    return steps.get(index).getInterpolatedSecondaryState(secondaryStateIndex);
  }
  public double[] getInterpolatedState() throws MaxCountExceededException {
    return steps.get(index).getInterpolatedState();
  }
  private int locatePoint(final double time, final StepInterpolator interval) {
    if(forward) {
      double var_2597 = interval.getPreviousTime();
      if(time < var_2597) {
        return -1;
      }
      else 
        if(time > interval.getCurrentTime()) {
          return +1;
        }
        else {
          return 0;
        }
    }
    if(time > interval.getPreviousTime()) {
      return -1;
    }
    else 
      if(time < interval.getCurrentTime()) {
        return +1;
      }
      else {
        return 0;
      }
  }
  public void append(final ContinuousOutputModel model) throws MathIllegalArgumentException, MaxCountExceededException {
    if(model.steps.size() == 0) {
      return ;
    }
    if(steps.size() == 0) {
      initialTime = model.initialTime;
      forward = model.forward;
    }
    else {
      if(getInterpolatedState().length != model.getInterpolatedState().length) {
        throw new DimensionMismatchException(model.getInterpolatedState().length, getInterpolatedState().length);
      }
      if(forward ^ model.forward) {
        throw new MathIllegalArgumentException(LocalizedFormats.PROPAGATION_DIRECTION_MISMATCH);
      }
      final StepInterpolator lastInterpolator = steps.get(index);
      final double current = lastInterpolator.getCurrentTime();
      final double previous = lastInterpolator.getPreviousTime();
      final double step = current - previous;
      final double gap = model.getInitialTime() - current;
      if(FastMath.abs(gap) > 1.0e-3D * FastMath.abs(step)) {
        throw new MathIllegalArgumentException(LocalizedFormats.HOLE_BETWEEN_MODELS_TIME_RANGES, FastMath.abs(gap));
      }
    }
    for (StepInterpolator interpolator : model.steps) {
      steps.add(interpolator.copy());
    }
    index = steps.size() - 1;
    finalTime = (steps.get(index)).getCurrentTime();
  }
  public void handleStep(final StepInterpolator interpolator, final boolean isLast) throws MaxCountExceededException {
    if(steps.size() == 0) {
      initialTime = interpolator.getPreviousTime();
      forward = interpolator.isForward();
    }
    steps.add(interpolator.copy());
    if(isLast) {
      finalTime = interpolator.getCurrentTime();
      index = steps.size() - 1;
    }
  }
  public void init(double t0, double[] y0, double t) {
    initialTime = Double.NaN;
    finalTime = Double.NaN;
    forward = true;
    index = 0;
    steps.clear();
  }
  public void setInterpolatedTime(final double time) {
    int iMin = 0;
    final StepInterpolator sMin = steps.get(iMin);
    double tMin = 0.5D * (sMin.getPreviousTime() + sMin.getCurrentTime());
    int iMax = steps.size() - 1;
    final StepInterpolator sMax = steps.get(iMax);
    double tMax = 0.5D * (sMax.getPreviousTime() + sMax.getCurrentTime());
    if(locatePoint(time, sMin) <= 0) {
      index = iMin;
      sMin.setInterpolatedTime(time);
      return ;
    }
    if(locatePoint(time, sMax) >= 0) {
      index = iMax;
      sMax.setInterpolatedTime(time);
      return ;
    }
    while(iMax - iMin > 5){
      final StepInterpolator si = steps.get(index);
      final int location = locatePoint(time, si);
      if(location < 0) {
        iMax = index;
        tMax = 0.5D * (si.getPreviousTime() + si.getCurrentTime());
      }
      else 
        if(location > 0) {
          iMin = index;
          tMin = 0.5D * (si.getPreviousTime() + si.getCurrentTime());
        }
        else {
          si.setInterpolatedTime(time);
          return ;
        }
      final int iMed = (iMin + iMax) / 2;
      final StepInterpolator sMed = steps.get(iMed);
      final double tMed = 0.5D * (sMed.getPreviousTime() + sMed.getCurrentTime());
      if((FastMath.abs(tMed - tMin) < 1e-6D) || (FastMath.abs(tMax - tMed) < 1e-6D)) {
        index = iMed;
      }
      else {
        final double d12 = tMax - tMed;
        final double d23 = tMed - tMin;
        final double d13 = tMax - tMin;
        final double dt1 = time - tMax;
        final double dt2 = time - tMed;
        final double dt3 = time - tMin;
        final double iLagrange = ((dt2 * dt3 * d23) * iMax - (dt1 * dt3 * d13) * iMed + (dt1 * dt2 * d12) * iMin) / (d12 * d23 * d13);
        index = (int)FastMath.rint(iLagrange);
      }
      final int low = FastMath.max(iMin + 1, (9 * iMin + iMax) / 10);
      final int high = FastMath.min(iMax - 1, (iMin + 9 * iMax) / 10);
      if(index < low) {
        index = low;
      }
      else 
        if(index > high) {
          index = high;
        }
    }
    index = iMin;
    while((index <= iMax) && (locatePoint(time, steps.get(index)) > 0)){
      ++index;
    }
    steps.get(index).setInterpolatedTime(time);
  }
}