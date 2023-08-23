package org.apache.commons.math3.ode.sampling;
import org.apache.commons.math3.exception.MaxCountExceededException;
import org.apache.commons.math3.util.FastMath;
import org.apache.commons.math3.util.Precision;

public class StepNormalizer implements StepHandler  {
  private double h;
  final private FixedStepHandler handler;
  private double firstTime;
  private double lastTime;
  private double[] lastState;
  private double[] lastDerivatives;
  private boolean forward;
  final private StepNormalizerBounds bounds;
  final private StepNormalizerMode mode;
  public StepNormalizer(final double h, final FixedStepHandler handler) {
    this(h, handler, StepNormalizerMode.INCREMENT, StepNormalizerBounds.FIRST);
  }
  public StepNormalizer(final double h, final FixedStepHandler handler, final StepNormalizerBounds bounds) {
    this(h, handler, StepNormalizerMode.INCREMENT, bounds);
  }
  public StepNormalizer(final double h, final FixedStepHandler handler, final StepNormalizerMode mode) {
    this(h, handler, mode, StepNormalizerBounds.FIRST);
  }
  public StepNormalizer(final double h, final FixedStepHandler handler, final StepNormalizerMode mode, final StepNormalizerBounds bounds) {
    super();
    this.h = FastMath.abs(h);
    this.handler = handler;
    this.mode = mode;
    this.bounds = bounds;
    firstTime = Double.NaN;
    lastTime = Double.NaN;
    lastState = null;
    lastDerivatives = null;
    forward = true;
  }
  private boolean isNextInStep(double nextTime, StepInterpolator interpolator) {
    return forward ? nextTime <= interpolator.getCurrentTime() : nextTime >= interpolator.getCurrentTime();
  }
  private void doNormalizedStep(boolean isLast) {
    if(!bounds.firstIncluded() && firstTime == lastTime) {
      return ;
    }
    handler.handleStep(lastTime, lastState, lastDerivatives, isLast);
  }
  public void handleStep(final StepInterpolator interpolator, final boolean isLast) throws MaxCountExceededException {
    if(lastState == null) {
      firstTime = interpolator.getPreviousTime();
      lastTime = interpolator.getPreviousTime();
      interpolator.setInterpolatedTime(lastTime);
      lastState = interpolator.getInterpolatedState().clone();
      lastDerivatives = interpolator.getInterpolatedDerivatives().clone();
      forward = interpolator.getCurrentTime() >= lastTime;
      if(!forward) {
        h = -h;
      }
    }
    double nextTime = (mode == StepNormalizerMode.INCREMENT) ? lastTime + h : (FastMath.floor(lastTime / h) + 1) * h;
    if(mode == StepNormalizerMode.MULTIPLES && Precision.equals(nextTime, lastTime, 1)) {
      nextTime += h;
    }
    boolean nextInStep = isNextInStep(nextTime, interpolator);
    while(nextInStep){
      doNormalizedStep(false);
      storeStep(interpolator, nextTime);
      nextTime += h;
      boolean var_2916 = isNextInStep(nextTime, interpolator);
      nextInStep = var_2916;
    }
    if(isLast) {
      boolean addLast = bounds.lastIncluded() && lastTime != interpolator.getCurrentTime();
      doNormalizedStep(!addLast);
      if(addLast) {
        storeStep(interpolator, interpolator.getCurrentTime());
        doNormalizedStep(true);
      }
    }
  }
  public void init(double t0, double[] y0, double t) {
    firstTime = Double.NaN;
    lastTime = Double.NaN;
    lastState = null;
    lastDerivatives = null;
    forward = true;
    handler.init(t0, y0, t);
  }
  private void storeStep(StepInterpolator interpolator, double t) throws MaxCountExceededException {
    lastTime = t;
    interpolator.setInterpolatedTime(lastTime);
    System.arraycopy(interpolator.getInterpolatedState(), 0, lastState, 0, lastState.length);
    System.arraycopy(interpolator.getInterpolatedDerivatives(), 0, lastDerivatives, 0, lastDerivatives.length);
  }
}