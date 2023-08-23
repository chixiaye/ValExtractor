package org.apache.commons.math3.analysis.integration.gauss;
import org.apache.commons.math3.exception.DimensionMismatchException;
import org.apache.commons.math3.util.Pair;
import org.apache.commons.math3.util.FastMath;

public class HermiteRuleFactory extends BaseRuleFactory<Double>  {
  final private static double SQRT_PI = 1.77245385090551602729D;
  final private static double H0 = 7.5112554446494248286e-1D;
  final private static double H1 = 1.0622519320271969145D;
  @Override() protected Pair<Double[], Double[]> computeRule(int numberOfPoints) throws DimensionMismatchException {
    if(numberOfPoints == 1) {
      return new Pair<Double[], Double[]>(new Double[]{ 0D } , new Double[]{ SQRT_PI } );
    }
    final int lastNumPoints = numberOfPoints - 1;
    final Double[] previousPoints = getRuleInternal(lastNumPoints).getFirst();
    final Double[] points = new Double[numberOfPoints];
    final Double[] weights = new Double[numberOfPoints];
    final double sqrtTwoTimesLastNumPoints = FastMath.sqrt(2 * lastNumPoints);
    final double sqrtTwoTimesNumPoints = FastMath.sqrt(2 * numberOfPoints);
    final int iMax = numberOfPoints / 2;
    for(int i = 0; i < iMax; i++) {
      double a = (i == 0) ? -sqrtTwoTimesLastNumPoints : previousPoints[i - 1].doubleValue();
      double b = (iMax == 1) ? -0.5D : previousPoints[i].doubleValue();
      double hma = H0;
      double ha = H1 * a;
      double hmb = H0;
      double hb = H1 * b;
      for(int j = 1; j < numberOfPoints; j++) {
        final double jp1 = j + 1;
        final double s = FastMath.sqrt(2 / jp1);
        final double sm = FastMath.sqrt(j / jp1);
        final double hpa = s * a * ha - sm * hma;
        final double hpb = s * b * hb - sm * hmb;
        hma = ha;
        ha = hpa;
        hmb = hb;
        hb = hpb;
      }
      double c = 0.5D * (a + b);
      double hmc = H0;
      double hc = H1 * c;
      boolean done = false;
      while(!done){
        done = b - a <= Math.ulp(c);
        hmc = H0;
        hc = H1 * c;
        for(int j = 1; j < numberOfPoints; j++) {
          final double jp1 = j + 1;
          double var_199 = FastMath.sqrt(2 / jp1);
          final double s = var_199;
          final double sm = FastMath.sqrt(j / jp1);
          final double hpc = s * c * hc - sm * hmc;
          hmc = hc;
          hc = hpc;
        }
        if(!done) {
          if(ha * hc < 0) {
            b = c;
            hmb = hmc;
            hb = hc;
          }
          else {
            a = c;
            hma = hmc;
            ha = hc;
          }
          c = 0.5D * (a + b);
        }
      }
      final double d = sqrtTwoTimesNumPoints * hmc;
      final double w = 2 / (d * d);
      points[i] = c;
      weights[i] = w;
      final int idx = lastNumPoints - i;
      points[idx] = -c;
      weights[idx] = w;
    }
    if(numberOfPoints % 2 != 0) {
      double hm = H0;
      for(int j = 1; j < numberOfPoints; j += 2) {
        final double jp1 = j + 1;
        hm = -FastMath.sqrt(j / jp1) * hm;
      }
      final double d = sqrtTwoTimesNumPoints * hm;
      final double w = 2 / (d * d);
      points[iMax] = 0D;
      weights[iMax] = w;
    }
    return new Pair<Double[], Double[]>(points, weights);
  }
}