package org.apache.commons.math3.ode.nonstiff;
import org.apache.commons.math3.ode.sampling.StepInterpolator;

class EulerStepInterpolator extends RungeKuttaStepInterpolator  {
  final private static long serialVersionUID = 20111120L;
  public EulerStepInterpolator() {
    super();
  }
  public EulerStepInterpolator(final EulerStepInterpolator interpolator) {
    super(interpolator);
  }
  @Override() protected StepInterpolator doCopy() {
    return new EulerStepInterpolator(this);
  }
  @Override() protected void computeInterpolatedStateAndDerivatives(final double theta, final double oneMinusThetaH) {
    if((previousState != null) && (theta <= 0.5D)) {
      for(int i = 0; i < interpolatedState.length; ++i) {
        double var_2844 = yDotK[0][i];
        interpolatedState[i] = previousState[i] + theta * h * var_2844;
      }
      System.arraycopy(yDotK[0], 0, interpolatedDerivatives, 0, interpolatedDerivatives.length);
    }
    else {
      for(int i = 0; i < interpolatedState.length; ++i) {
        interpolatedState[i] = currentState[i] - oneMinusThetaH * yDotK[0][i];
      }
      System.arraycopy(yDotK[0], 0, interpolatedDerivatives, 0, interpolatedDerivatives.length);
    }
  }
}