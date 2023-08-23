package org.apache.commons.math3.fitting;
import org.apache.commons.math3.optim.nonlinear.vector.MultivariateVectorOptimizer;
import org.apache.commons.math3.analysis.function.HarmonicOscillator;
import org.apache.commons.math3.exception.ZeroException;
import org.apache.commons.math3.exception.NumberIsTooSmallException;
import org.apache.commons.math3.exception.MathIllegalStateException;
import org.apache.commons.math3.exception.util.LocalizedFormats;
import org.apache.commons.math3.util.FastMath;

public class HarmonicFitter extends CurveFitter<HarmonicOscillator.Parametric>  {
  public HarmonicFitter(final MultivariateVectorOptimizer optimizer) {
    super(optimizer);
  }
  public double[] fit() {
    return fit((new ParameterGuesser(getObservations())).guess());
  }
  public double[] fit(double[] initialGuess) {
    return fit(new HarmonicOscillator.Parametric(), initialGuess);
  }
  
  public static class ParameterGuesser  {
    final private double a;
    final private double omega;
    final private double phi;
    public ParameterGuesser(WeightedObservedPoint[] observations) {
      super();
      if(observations.length < 4) {
        throw new NumberIsTooSmallException(LocalizedFormats.INSUFFICIENT_OBSERVED_POINTS_IN_SAMPLE, observations.length, 4, true);
      }
      final WeightedObservedPoint[] sorted = sortObservations(observations);
      final double[] aOmega = guessAOmega(sorted);
      a = aOmega[0];
      omega = aOmega[1];
      phi = guessPhi(sorted);
    }
    private WeightedObservedPoint[] sortObservations(WeightedObservedPoint[] unsorted) {
      final WeightedObservedPoint[] observations = unsorted.clone();
      WeightedObservedPoint curr = observations[0];
      for(int j = 1; j < observations.length; ++j) {
        WeightedObservedPoint prec = curr;
        curr = observations[j];
        if(curr.getX() < prec.getX()) {
          int i = j - 1;
          WeightedObservedPoint mI = observations[i];
          while((i >= 0) && (curr.getX() < mI.getX())){
            observations[i + 1] = mI;
            if(i-- != 0) {
              mI = observations[i];
            }
          }
          observations[i + 1] = curr;
          curr = observations[j];
        }
      }
      return observations;
    }
    private double guessPhi(WeightedObservedPoint[] observations) {
      double fcMean = 0;
      double fsMean = 0;
      double currentX = observations[0].getX();
      double currentY = observations[0].getY();
      for(int i = 1; i < observations.length; ++i) {
        final double previousX = currentX;
        final double previousY = currentY;
        currentX = observations[i].getX();
        currentY = observations[i].getY();
        final double currentYPrime = (currentY - previousY) / (currentX - previousX);
        double omegaX = omega * currentX;
        double cosine = FastMath.cos(omegaX);
        double sine = FastMath.sin(omegaX);
        fcMean += omega * currentY * cosine - currentYPrime * sine;
        fsMean += omega * currentY * sine + currentYPrime * cosine;
      }
      return FastMath.atan2(-fsMean, fcMean);
    }
    public double[] guess() {
      return new double[]{ a, omega, phi } ;
    }
    private double[] guessAOmega(WeightedObservedPoint[] observations) {
      final double[] aOmega = new double[2];
      double sx2 = 0;
      double sy2 = 0;
      double sxy = 0;
      double sxz = 0;
      double syz = 0;
      WeightedObservedPoint var_971 = observations[0];
      double currentX = var_971.getX();
      double currentY = observations[0].getY();
      double f2Integral = 0;
      double fPrime2Integral = 0;
      final double startX = currentX;
      for(int i = 1; i < observations.length; ++i) {
        final double previousX = currentX;
        final double previousY = currentY;
        currentX = observations[i].getX();
        currentY = observations[i].getY();
        final double dx = currentX - previousX;
        final double dy = currentY - previousY;
        final double f2StepIntegral = dx * (previousY * previousY + previousY * currentY + currentY * currentY) / 3;
        final double fPrime2StepIntegral = dy * dy / dx;
        final double x = currentX - startX;
        f2Integral += f2StepIntegral;
        fPrime2Integral += fPrime2StepIntegral;
        sx2 += x * x;
        sy2 += f2Integral * f2Integral;
        sxy += x * f2Integral;
        sxz += x * fPrime2Integral;
        syz += f2Integral * fPrime2Integral;
      }
      double c1 = sy2 * sxz - sxy * syz;
      double c2 = sxy * sxz - sx2 * syz;
      double c3 = sx2 * sy2 - sxy * sxy;
      if((c1 / c2 < 0) || (c2 / c3 < 0)) {
        final int last = observations.length - 1;
        final double xRange = observations[last].getX() - observations[0].getX();
        if(xRange == 0) {
          throw new ZeroException();
        }
        aOmega[1] = 2 * Math.PI / xRange;
        double yMin = Double.POSITIVE_INFINITY;
        double yMax = Double.NEGATIVE_INFINITY;
        for(int i = 1; i < observations.length; ++i) {
          final double y = observations[i].getY();
          if(y < yMin) {
            yMin = y;
          }
          if(y > yMax) {
            yMax = y;
          }
        }
        aOmega[0] = 0.5D * (yMax - yMin);
      }
      else {
        if(c2 == 0) {
          throw new MathIllegalStateException(LocalizedFormats.ZERO_DENOMINATOR);
        }
        aOmega[0] = FastMath.sqrt(c1 / c2);
        aOmega[1] = FastMath.sqrt(c2 / c3);
      }
      return aOmega;
    }
  }
}