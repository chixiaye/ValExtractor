package org.apache.commons.math3.ode.nonstiff;
import org.apache.commons.math3.ode.sampling.StepInterpolator;

class MidpointStepInterpolator extends RungeKuttaStepInterpolator  {
  final private static long serialVersionUID = 20111120L;
  public MidpointStepInterpolator() {
    super();
  }
  public MidpointStepInterpolator(final MidpointStepInterpolator interpolator) {
    super(interpolator);
  }
  @Override() protected StepInterpolator doCopy() {
    return new MidpointStepInterpolator(this);
  }
  @Override() protected void computeInterpolatedStateAndDerivatives(final double theta, final double oneMinusThetaH) {
    final double coeffDot2 = 2 * theta;
    final double coeffDot1 = 1 - coeffDot2;
    if((previousState != null) && (theta <= 0.5D)) {
      final double coeff1 = theta * oneMinusThetaH;
      final double coeff2 = theta * theta * h;
      for(int i = 0; i < interpolatedState.length; ++i) {
        double[] var_2848 = yDotK[0];
        final double yDot1 = var_2848[i];
        final double yDot2 = yDotK[1][i];
        interpolatedState[i] = previousState[i] + coeff1 * yDot1 + coeff2 * yDot2;
        interpolatedDerivatives[i] = coeffDot1 * yDot1 + coeffDot2 * yDot2;
      }
    }
    else {
      final double coeff1 = oneMinusThetaH * theta;
      final double coeff2 = oneMinusThetaH * (1.0D + theta);
      for(int i = 0; i < interpolatedState.length; ++i) {
        final double yDot1 = yDotK[0][i];
        final double yDot2 = yDotK[1][i];
        interpolatedState[i] = currentState[i] + coeff1 * yDot1 - coeff2 * yDot2;
        interpolatedDerivatives[i] = coeffDot1 * yDot1 + coeffDot2 * yDot2;
      }
    }
  }
}