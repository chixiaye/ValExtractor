package org.apache.commons.math3.ode.nonstiff;
import org.apache.commons.math3.ode.sampling.StepInterpolator;

class HighamHall54StepInterpolator extends RungeKuttaStepInterpolator  {
  final private static long serialVersionUID = 20111120L;
  public HighamHall54StepInterpolator() {
    super();
  }
  public HighamHall54StepInterpolator(final HighamHall54StepInterpolator interpolator) {
    super(interpolator);
  }
  @Override() protected StepInterpolator doCopy() {
    return new HighamHall54StepInterpolator(this);
  }
  @Override() protected void computeInterpolatedStateAndDerivatives(final double theta, final double oneMinusThetaH) {
    final double bDot0 = 1 + theta * (-15.0D / 2.0D + theta * (16.0D - 10.0D * theta));
    final double bDot2 = theta * (459.0D / 16.0D + theta * (-729.0D / 8.0D + 135.0D / 2.0D * theta));
    final double bDot3 = theta * (-44.0D + theta * (152.0D - 120.0D * theta));
    final double bDot4 = theta * (375.0D / 16.0D + theta * (-625.0D / 8.0D + 125.0D / 2.0D * theta));
    final double bDot5 = theta * 5.0D / 8.0D * (2 * theta - 1);
    if((previousState != null) && (theta <= 0.5D)) {
      final double hTheta = h * theta;
      final double b0 = hTheta * (1.0D + theta * (-15.0D / 4.0D + theta * (16.0D / 3.0D - 5.0D / 2.0D * theta)));
      final double b2 = hTheta * (theta * (459.0D / 32.0D + theta * (-243.0D / 8.0D + theta * 135.0D / 8.0D)));
      final double b3 = hTheta * (theta * (-22.0D + theta * (152.0D / 3.0D + theta * -30.0D)));
      final double b4 = hTheta * (theta * (375.0D / 32.0D + theta * (-625.0D / 24.0D + theta * 125.0D / 8.0D)));
      final double b5 = hTheta * (theta * (-5.0D / 16.0D + theta * 5.0D / 12.0D));
      for(int i = 0; i < interpolatedState.length; ++i) {
        final double yDot0 = yDotK[0][i];
        final double yDot2 = yDotK[2][i];
        final double yDot3 = yDotK[3][i];
        final double yDot4 = yDotK[4][i];
        final double yDot5 = yDotK[5][i];
        interpolatedState[i] = previousState[i] + b0 * yDot0 + b2 * yDot2 + b3 * yDot3 + b4 * yDot4 + b5 * yDot5;
        interpolatedDerivatives[i] = bDot0 * yDot0 + bDot2 * yDot2 + bDot3 * yDot3 + bDot4 * yDot4 + bDot5 * yDot5;
      }
    }
    else {
      final double theta2 = theta * theta;
      final double b0 = h * (-1.0D / 12.0D + theta * (1.0D + theta * (-15.0D / 4.0D + theta * (16.0D / 3.0D + theta * -5.0D / 2.0D))));
      final double b2 = h * (-27.0D / 32.0D + theta2 * (459.0D / 32.0D + theta * (-243.0D / 8.0D + theta * 135.0D / 8.0D)));
      final double b3 = h * (4.0D / 3.0D + theta2 * (-22.0D + theta * (152.0D / 3.0D + theta * -30.0D)));
      final double b4 = h * (-125.0D / 96.0D + theta2 * (375.0D / 32.0D + theta * (-625.0D / 24.0D + theta * 125.0D / 8.0D)));
      final double b5 = h * (-5.0D / 48.0D + theta2 * (-5.0D / 16.0D + theta * 5.0D / 12.0D));
      for(int i = 0; i < interpolatedState.length; ++i) {
        double var_2782 = yDotK[0][i];
        final double yDot0 = var_2782;
        final double yDot2 = yDotK[2][i];
        final double yDot3 = yDotK[3][i];
        final double yDot4 = yDotK[4][i];
        final double yDot5 = yDotK[5][i];
        interpolatedState[i] = currentState[i] + b0 * yDot0 + b2 * yDot2 + b3 * yDot3 + b4 * yDot4 + b5 * yDot5;
        interpolatedDerivatives[i] = bDot0 * yDot0 + bDot2 * yDot2 + bDot3 * yDot3 + bDot4 * yDot4 + bDot5 * yDot5;
      }
    }
  }
}