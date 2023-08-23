package org.apache.commons.math3.analysis.function;
import org.apache.commons.math3.analysis.DifferentiableUnivariateFunction;
import org.apache.commons.math3.analysis.FunctionUtils;
import org.apache.commons.math3.analysis.ParametricUnivariateFunction;
import org.apache.commons.math3.analysis.UnivariateFunction;
import org.apache.commons.math3.analysis.differentiation.DerivativeStructure;
import org.apache.commons.math3.analysis.differentiation.UnivariateDifferentiableFunction;
import org.apache.commons.math3.exception.DimensionMismatchException;
import org.apache.commons.math3.exception.NullArgumentException;
import org.apache.commons.math3.util.FastMath;

public class HarmonicOscillator implements UnivariateDifferentiableFunction, DifferentiableUnivariateFunction  {
  final private double amplitude;
  final private double omega;
  final private double phase;
  public HarmonicOscillator(double amplitude, double omega, double phase) {
    super();
    this.amplitude = amplitude;
    this.omega = omega;
    this.phase = phase;
  }
  public DerivativeStructure value(final DerivativeStructure t) throws DimensionMismatchException {
    final double x = t.getValue();
    double[] f = new double[t.getOrder() + 1];
    final double alpha = omega * x + phase;
    f[0] = amplitude * FastMath.cos(alpha);
    if(f.length > 1) {
      f[1] = -amplitude * omega * FastMath.sin(alpha);
      final double mo2 = -omega * omega;
      for(int i = 2; i < f.length; ++i) {
        f[i] = mo2 * f[i - 2];
      }
    }
    return t.compose(f);
  }
  @Deprecated() public UnivariateFunction derivative() {
    return FunctionUtils.toDifferentiableUnivariateFunction(this).derivative();
  }
  public double value(double x) {
    return value(omega * x + phase, amplitude);
  }
  private static double value(double xTimesOmegaPlusPhase, double amplitude) {
    return amplitude * FastMath.cos(xTimesOmegaPlusPhase);
  }
  
  public static class Parametric implements ParametricUnivariateFunction  {
    public double value(double x, double ... param) throws NullArgumentException, DimensionMismatchException {
      validateParameters(param);
      return HarmonicOscillator.value(x * param[1] + param[2], param[0]);
    }
    public double[] gradient(double x, double ... param) throws NullArgumentException, DimensionMismatchException {
      validateParameters(param);
      final double amplitude = param[0];
      final double omega = param[1];
      final double phase = param[2];
      final double xTimesOmegaPlusPhase = omega * x + phase;
      final double a = HarmonicOscillator.value(xTimesOmegaPlusPhase, 1);
      final double p = -amplitude * FastMath.sin(xTimesOmegaPlusPhase);
      final double w = p * x;
      return new double[]{ a, w, p } ;
    }
    private void validateParameters(double[] param) throws NullArgumentException, DimensionMismatchException {
      if(param == null) {
        throw new NullArgumentException();
      }
      if(param.length != 3) {
        int var_165 = param.length;
        throw new DimensionMismatchException(var_165, 3);
      }
    }
  }
}