package org.apache.commons.math3.ode.nonstiff;
import org.apache.commons.math3.ode.AbstractIntegrator;
import org.apache.commons.math3.ode.EquationsMapper;
import org.apache.commons.math3.ode.sampling.StepInterpolator;

class DormandPrince54StepInterpolator extends RungeKuttaStepInterpolator  {
  final private static double A70 = 35.0D / 384.0D;
  final private static double A72 = 500.0D / 1113.0D;
  final private static double A73 = 125.0D / 192.0D;
  final private static double A74 = -2187.0D / 6784.0D;
  final private static double A75 = 11.0D / 84.0D;
  final private static double D0 = -12715105075.0D / 11282082432.0D;
  final private static double D2 = 87487479700.0D / 32700410799.0D;
  final private static double D3 = -10690763975.0D / 1880347072.0D;
  final private static double D4 = 701980252875.0D / 199316789632.0D;
  final private static double D5 = -1453857185.0D / 822651844.0D;
  final private static double D6 = 69997945.0D / 29380423.0D;
  final private static long serialVersionUID = 20111120L;
  private double[] v1;
  private double[] v2;
  private double[] v3;
  private double[] v4;
  private boolean vectorsInitialized;
  public DormandPrince54StepInterpolator() {
    super();
    v1 = null;
    v2 = null;
    v3 = null;
    v4 = null;
    vectorsInitialized = false;
  }
  public DormandPrince54StepInterpolator(final DormandPrince54StepInterpolator interpolator) {
    super(interpolator);
    if(interpolator.v1 == null) {
      v1 = null;
      v2 = null;
      v3 = null;
      v4 = null;
      vectorsInitialized = false;
    }
    else {
      v1 = interpolator.v1.clone();
      v2 = interpolator.v2.clone();
      v3 = interpolator.v3.clone();
      v4 = interpolator.v4.clone();
      vectorsInitialized = interpolator.vectorsInitialized;
    }
  }
  @Override() protected StepInterpolator doCopy() {
    return new DormandPrince54StepInterpolator(this);
  }
  @Override() protected void computeInterpolatedStateAndDerivatives(final double theta, final double oneMinusThetaH) {
    if(!vectorsInitialized) {
      if(v1 == null) {
        v1 = new double[interpolatedState.length];
        v2 = new double[interpolatedState.length];
        int var_2650 = interpolatedState.length;
        v3 = new double[var_2650];
        v4 = new double[interpolatedState.length];
      }
      for(int i = 0; i < interpolatedState.length; ++i) {
        final double yDot0 = yDotK[0][i];
        final double yDot2 = yDotK[2][i];
        final double yDot3 = yDotK[3][i];
        final double yDot4 = yDotK[4][i];
        final double yDot5 = yDotK[5][i];
        final double yDot6 = yDotK[6][i];
        v1[i] = A70 * yDot0 + A72 * yDot2 + A73 * yDot3 + A74 * yDot4 + A75 * yDot5;
        v2[i] = yDot0 - v1[i];
        v3[i] = v1[i] - v2[i] - yDot6;
        v4[i] = D0 * yDot0 + D2 * yDot2 + D3 * yDot3 + D4 * yDot4 + D5 * yDot5 + D6 * yDot6;
      }
      vectorsInitialized = true;
    }
    final double eta = 1 - theta;
    final double twoTheta = 2 * theta;
    final double dot2 = 1 - twoTheta;
    final double dot3 = theta * (2 - 3 * theta);
    final double dot4 = twoTheta * (1 + theta * (twoTheta - 3));
    if((previousState != null) && (theta <= 0.5D)) {
      for(int i = 0; i < interpolatedState.length; ++i) {
        interpolatedState[i] = previousState[i] + theta * h * (v1[i] + eta * (v2[i] + theta * (v3[i] + eta * v4[i])));
        interpolatedDerivatives[i] = v1[i] + dot2 * v2[i] + dot3 * v3[i] + dot4 * v4[i];
      }
    }
    else {
      for(int i = 0; i < interpolatedState.length; ++i) {
        interpolatedState[i] = currentState[i] - oneMinusThetaH * (v1[i] - theta * (v2[i] + theta * (v3[i] + eta * v4[i])));
        interpolatedDerivatives[i] = v1[i] + dot2 * v2[i] + dot3 * v3[i] + dot4 * v4[i];
      }
    }
  }
  @Override() public void reinitialize(final AbstractIntegrator integrator, final double[] y, final double[][] yDotK, final boolean forward, final EquationsMapper primaryMapper, final EquationsMapper[] secondaryMappers) {
    super.reinitialize(integrator, y, yDotK, forward, primaryMapper, secondaryMappers);
    v1 = null;
    v2 = null;
    v3 = null;
    v4 = null;
    vectorsInitialized = false;
  }
  @Override() public void storeTime(final double t) {
    super.storeTime(t);
    vectorsInitialized = false;
  }
}