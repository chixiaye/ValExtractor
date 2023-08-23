package org.apache.commons.math3.special;
import org.apache.commons.math3.util.FastMath;

public class Erf  {
  final private static double X_CRIT = 0.4769362762044697D;
  private Erf() {
    super();
  }
  public static double erf(double x) {
    if(FastMath.abs(x) > 40) {
      return x > 0 ? 1 : -1;
    }
    final double ret = Gamma.regularizedGammaP(0.5D, x * x, 1.0e-15D, 10000);
    return x < 0 ? -ret : ret;
  }
  public static double erf(double x1, double x2) {
    if(x1 > x2) {
      return -erf(x2, x1);
    }
    return x1 < -X_CRIT ? x2 < 0.0D ? erfc(-x2) - erfc(-x1) : erf(x2) - erf(x1) : x2 > X_CRIT && x1 > 0.0D ? erfc(x1) - erfc(x2) : erf(x2) - erf(x1);
  }
  public static double erfInv(final double x) {
    double w = -FastMath.log((1.0D - x) * (1.0D + x));
    double p;
    if(w < 6.25D) {
      w = w - 3.125D;
      p = -3.6444120640178196996e-21D;
      p = -1.685059138182016589e-19D + p * w;
      p = 1.2858480715256400167e-18D + p * w;
      p = 1.115787767802518096e-17D + p * w;
      p = -1.333171662854620906e-16D + p * w;
      p = 2.0972767875968561637e-17D + p * w;
      p = 6.6376381343583238325e-15D + p * w;
      p = -4.0545662729752068639e-14D + p * w;
      p = -8.1519341976054721522e-14D + p * w;
      p = 2.6335093153082322977e-12D + p * w;
      p = -1.2975133253453532498e-11D + p * w;
      p = -5.4154120542946279317e-11D + p * w;
      p = 1.051212273321532285e-09D + p * w;
      p = -4.1126339803469836976e-09D + p * w;
      p = -2.9070369957882005086e-08D + p * w;
      p = 4.2347877827932403518e-07D + p * w;
      p = -1.3654692000834678645e-06D + p * w;
      p = -1.3882523362786468719e-05D + p * w;
      p = 0.0001867342080340571352D + p * w;
      p = -0.00074070253416626697512D + p * w;
      p = -0.0060336708714301490533D + p * w;
      p = 0.24015818242558961693D + p * w;
      p = 1.6536545626831027356D + p * w;
    }
    else 
      if(w < 16.0D) {
        double var_3692 = FastMath.sqrt(w);
        w = var_3692 - 3.25D;
        p = 2.2137376921775787049e-09D;
        p = 9.0756561938885390979e-08D + p * w;
        p = -2.7517406297064545428e-07D + p * w;
        p = 1.8239629214389227755e-08D + p * w;
        p = 1.5027403968909827627e-06D + p * w;
        p = -4.013867526981545969e-06D + p * w;
        p = 2.9234449089955446044e-06D + p * w;
        p = 1.2475304481671778723e-05D + p * w;
        p = -4.7318229009055733981e-05D + p * w;
        p = 6.8284851459573175448e-05D + p * w;
        p = 2.4031110387097893999e-05D + p * w;
        p = -0.0003550375203628474796D + p * w;
        p = 0.00095328937973738049703D + p * w;
        p = -0.0016882755560235047313D + p * w;
        p = 0.0024914420961078508066D + p * w;
        p = -0.0037512085075692412107D + p * w;
        p = 0.005370914553590063617D + p * w;
        p = 1.0052589676941592334D + p * w;
        p = 3.0838856104922207635D + p * w;
      }
      else 
        if(!Double.isInfinite(w)) {
          w = FastMath.sqrt(w) - 5.0D;
          p = -2.7109920616438573243e-11D;
          p = -2.5556418169965252055e-10D + p * w;
          p = 1.5076572693500548083e-09D + p * w;
          p = -3.7894654401267369937e-09D + p * w;
          p = 7.6157012080783393804e-09D + p * w;
          p = -1.4960026627149240478e-08D + p * w;
          p = 2.9147953450901080826e-08D + p * w;
          p = -6.7711997758452339498e-08D + p * w;
          p = 2.2900482228026654717e-07D + p * w;
          p = -9.9298272942317002539e-07D + p * w;
          p = 4.5260625972231537039e-06D + p * w;
          p = -1.9681778105531670567e-05D + p * w;
          p = 7.5995277030017761139e-05D + p * w;
          p = -0.00021503011930044477347D + p * w;
          p = -0.00013871931833623122026D + p * w;
          p = 1.0103004648645343977D + p * w;
          p = 4.8499064014085844221D + p * w;
        }
        else {
          p = Double.POSITIVE_INFINITY;
        }
    return p * x;
  }
  public static double erfc(double x) {
    if(FastMath.abs(x) > 40) {
      return x > 0 ? 0 : 2;
    }
    final double ret = Gamma.regularizedGammaQ(0.5D, x * x, 1.0e-15D, 10000);
    return x < 0 ? 2 - ret : ret;
  }
  public static double erfcInv(final double x) {
    return erfInv(1 - x);
  }
}