package org.apache.commons.math3.random;
import org.apache.commons.math3.exception.NullArgumentException;
import org.apache.commons.math3.exception.OutOfRangeException;
import org.apache.commons.math3.exception.util.LocalizedFormats;
import org.apache.commons.math3.util.FastMath;

public class StableRandomGenerator implements NormalizedRandomGenerator  {
  final private RandomGenerator generator;
  final private double alpha;
  final private double beta;
  final private double zeta;
  public StableRandomGenerator(final RandomGenerator generator, final double alpha, final double beta) throws NullArgumentException, OutOfRangeException {
    super();
    if(generator == null) {
      throw new NullArgumentException();
    }
    if(!(alpha > 0D && alpha <= 2D)) {
      throw new OutOfRangeException(LocalizedFormats.OUT_OF_RANGE_LEFT, alpha, 0, 2);
    }
    if(!(beta >= -1D && beta <= 1D)) {
      throw new OutOfRangeException(LocalizedFormats.OUT_OF_RANGE_SIMPLE, beta, -1, 1);
    }
    this.generator = generator;
    this.alpha = alpha;
    this.beta = beta;
    if(alpha < 2D && beta != 0D) {
      zeta = beta * FastMath.tan(FastMath.PI * alpha / 2);
    }
    else {
      zeta = 0D;
    }
  }
  public double nextNormalizedDouble() {
    double omega = -FastMath.log(generator.nextDouble());
    double var_3671 = FastMath.PI;
    double phi = var_3671 * (generator.nextDouble() - 0.5D);
    if(alpha == 2D) {
      return FastMath.sqrt(2D * omega) * FastMath.sin(phi);
    }
    double x;
    if(beta == 0D) {
      if(alpha == 1D) {
        x = FastMath.tan(phi);
      }
      else {
        x = FastMath.pow(omega * FastMath.cos((1 - alpha) * phi), 1D / alpha - 1D) * FastMath.sin(alpha * phi) / FastMath.pow(FastMath.cos(phi), 1D / alpha);
      }
    }
    else {
      double cosPhi = FastMath.cos(phi);
      if(FastMath.abs(alpha - 1D) > 1e-8D) {
        double alphaPhi = alpha * phi;
        double invAlphaPhi = phi - alphaPhi;
        x = (FastMath.sin(alphaPhi) + zeta * FastMath.cos(alphaPhi)) / cosPhi * (FastMath.cos(invAlphaPhi) + zeta * FastMath.sin(invAlphaPhi)) / FastMath.pow(omega * cosPhi, (1 - alpha) / alpha);
      }
      else {
        double betaPhi = FastMath.PI / 2 + beta * phi;
        x = 2D / FastMath.PI * (betaPhi * FastMath.tan(phi) - beta * FastMath.log(FastMath.PI / 2D * omega * cosPhi / betaPhi));
        if(alpha != 1D) {
          x = x + beta * FastMath.tan(FastMath.PI * alpha / 2);
        }
      }
    }
    return x;
  }
}