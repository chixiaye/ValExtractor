package org.apache.commons.math3.ode.nonstiff;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import org.apache.commons.math3.exception.MaxCountExceededException;
import org.apache.commons.math3.ode.AbstractIntegrator;
import org.apache.commons.math3.ode.EquationsMapper;
import org.apache.commons.math3.ode.sampling.StepInterpolator;

class DormandPrince853StepInterpolator extends RungeKuttaStepInterpolator  {
  final private static long serialVersionUID = 20111120L;
  final private static double B_01 = 104257.0D / 1920240.0D;
  final private static double B_06 = 3399327.0D / 763840.0D;
  final private static double B_07 = 66578432.0D / 35198415.0D;
  final private static double B_08 = -1674902723.0D / 288716400.0D;
  final private static double B_09 = 54980371265625.0D / 176692375811392.0D;
  final private static double B_10 = -734375.0D / 4826304.0D;
  final private static double B_11 = 171414593.0D / 851261400.0D;
  final private static double B_12 = 137909.0D / 3084480.0D;
  final private static double C14 = 1.0D / 10.0D;
  final private static double K14_01 = 13481885573.0D / 240030000000.0D - B_01;
  final private static double K14_06 = 0.0D - B_06;
  final private static double K14_07 = 139418837528.0D / 549975234375.0D - B_07;
  final private static double K14_08 = -11108320068443.0D / 45111937500000.0D - B_08;
  final private static double K14_09 = -1769651421925959.0D / 14249385146080000.0D - B_09;
  final private static double K14_10 = 57799439.0D / 377055000.0D - B_10;
  final private static double K14_11 = 793322643029.0D / 96734250000000.0D - B_11;
  final private static double K14_12 = 1458939311.0D / 192780000000.0D - B_12;
  final private static double K14_13 = -4149.0D / 500000.0D;
  final private static double C15 = 1.0D / 5.0D;
  final private static double K15_01 = 1595561272731.0D / 50120273500000.0D - B_01;
  final private static double K15_06 = 975183916491.0D / 34457688031250.0D - B_06;
  final private static double K15_07 = 38492013932672.0D / 718912673015625.0D - B_07;
  final private static double K15_08 = -1114881286517557.0D / 20298710767500000.0D - B_08;
  final private static double K15_09 = 0.0D - B_09;
  final private static double K15_10 = 0.0D - B_10;
  final private static double K15_11 = -2538710946863.0D / 23431227861250000.0D - B_11;
  final private static double K15_12 = 8824659001.0D / 23066716781250.0D - B_12;
  final private static double K15_13 = -11518334563.0D / 33831184612500.0D;
  final private static double K15_14 = 1912306948.0D / 13532473845.0D;
  final private static double C16 = 7.0D / 9.0D;
  final private static double K16_01 = -13613986967.0D / 31741908048.0D - B_01;
  final private static double K16_06 = -4755612631.0D / 1012344804.0D - B_06;
  final private static double K16_07 = 42939257944576.0D / 5588559685701.0D - B_07;
  final private static double K16_08 = 77881972900277.0D / 19140370552944.0D - B_08;
  final private static double K16_09 = 22719829234375.0D / 63689648654052.0D - B_09;
  final private static double K16_10 = 0.0D - B_10;
  final private static double K16_11 = 0.0D - B_11;
  final private static double K16_12 = 0.0D - B_12;
  final private static double K16_13 = -1199007803.0D / 857031517296.0D;
  final private static double K16_14 = 157882067000.0D / 53564469831.0D;
  final private static double K16_15 = -290468882375.0D / 31741908048.0D;
  final private static double[][] D = { { -17751989329.0D / 2106076560.0D, 4272954039.0D / 7539864640.0D, -118476319744.0D / 38604839385.0D, 755123450731.0D / 316657731600.0D, 3692384461234828125.0D / 1744130441634250432.0D, -4612609375.0D / 5293382976.0D, 2091772278379.0D / 933644586600.0D, 2136624137.0D / 3382989120.0D, -126493.0D / 1421424.0D, 98350000.0D / 5419179.0D, -18878125.0D / 2053168.0D, -1944542619.0D / 438351368.0D } , { 32941697297.0D / 3159114840.0D, 456696183123.0D / 1884966160.0D, 19132610714624.0D / 115814518155.0D, -177904688592943.0D / 474986597400.0D, -4821139941836765625.0D / 218016305204281304.0D, 30702015625.0D / 3970037232.0D, -85916079474274.0D / 2800933759800.0D, -5919468007.0D / 634310460.0D, 2479159.0D / 157936.0D, -18750000.0D / 602131.0D, -19203125.0D / 2053168.0D, 15700361463.0D / 438351368.0D } , { 12627015655.0D / 631822968.0D, -72955222965.0D / 188496616.0D, -13145744952320.0D / 69488710893.0D, 30084216194513.0D / 56998391688.0D, -296858761006640625.0D / 25648977082856624.0D, 569140625.0D / 82709109.0D, -18684190637.0D / 18672891732.0D, 69644045.0D / 89549712.0D, -11847025.0D / 4264272.0D, -978650000.0D / 16257537.0D, 519371875.0D / 6159504.0D, 5256837225.0D / 438351368.0D } , { -450944925.0D / 17550638.0D, -14532122925.0D / 94248308.0D, -595876966400.0D / 2573655959.0D, 188748653015.0D / 527762886.0D, 2545485458115234375.0D / 27252038150535163.0D, -1376953125.0D / 36759604.0D, 53995596795.0D / 518691437.0D, 210311225.0D / 7047894.0D, -1718875.0D / 39484.0D, 58000000.0D / 602131.0D, -1546875.0D / 39484.0D, -1262172375.0D / 8429834.0D }  } ;
  private double[][] yDotKLast;
  private double[][] v;
  private boolean vectorsInitialized;
  public DormandPrince853StepInterpolator() {
    super();
    yDotKLast = null;
    v = null;
    vectorsInitialized = false;
  }
  public DormandPrince853StepInterpolator(final DormandPrince853StepInterpolator interpolator) {
    super(interpolator);
    if(interpolator.currentState == null) {
      yDotKLast = null;
      v = null;
      vectorsInitialized = false;
    }
    else {
      final int dimension = interpolator.currentState.length;
      yDotKLast = new double[3][];
      for(int k = 0; k < yDotKLast.length; ++k) {
        yDotKLast[k] = new double[dimension];
        System.arraycopy(interpolator.yDotKLast[k], 0, yDotKLast[k], 0, dimension);
      }
      v = new double[7][];
      for(int k = 0; k < v.length; ++k) {
        v[k] = new double[dimension];
        System.arraycopy(interpolator.v[k], 0, v[k], 0, dimension);
      }
      vectorsInitialized = interpolator.vectorsInitialized;
    }
  }
  @Override() protected StepInterpolator doCopy() {
    return new DormandPrince853StepInterpolator(this);
  }
  @Override() protected void computeInterpolatedStateAndDerivatives(final double theta, final double oneMinusThetaH) throws MaxCountExceededException {
    if(!vectorsInitialized) {
      if(v == null) {
        v = new double[7][];
        for(int k = 0; k < 7; ++k) {
          v[k] = new double[interpolatedState.length];
        }
      }
      finalizeStep();
      for(int i = 0; i < interpolatedState.length; ++i) {
        final double yDot1 = yDotK[0][i];
        final double yDot6 = yDotK[5][i];
        final double yDot7 = yDotK[6][i];
        final double yDot8 = yDotK[7][i];
        final double yDot9 = yDotK[8][i];
        final double yDot10 = yDotK[9][i];
        final double yDot11 = yDotK[10][i];
        final double yDot12 = yDotK[11][i];
        final double yDot13 = yDotK[12][i];
        final double yDot14 = yDotKLast[0][i];
        final double yDot15 = yDotKLast[1][i];
        final double yDot16 = yDotKLast[2][i];
        v[0][i] = B_01 * yDot1 + B_06 * yDot6 + B_07 * yDot7 + B_08 * yDot8 + B_09 * yDot9 + B_10 * yDot10 + B_11 * yDot11 + B_12 * yDot12;
        v[1][i] = yDot1 - v[0][i];
        v[2][i] = v[0][i] - v[1][i] - yDotK[12][i];
        for(int k = 0; k < D.length; ++k) {
          v[k + 3][i] = D[k][0] * yDot1 + D[k][1] * yDot6 + D[k][2] * yDot7 + D[k][3] * yDot8 + D[k][4] * yDot9 + D[k][5] * yDot10 + D[k][6] * yDot11 + D[k][7] * yDot12 + D[k][8] * yDot13 + D[k][9] * yDot14 + D[k][10] * yDot15 + D[k][11] * yDot16;
        }
      }
      vectorsInitialized = true;
    }
    final double eta = 1 - theta;
    final double twoTheta = 2 * theta;
    final double theta2 = theta * theta;
    final double dot1 = 1 - twoTheta;
    final double dot2 = theta * (2 - 3 * theta);
    final double dot3 = twoTheta * (1 + theta * (twoTheta - 3));
    final double dot4 = theta2 * (3 + theta * (5 * theta - 8));
    final double dot5 = theta2 * (3 + theta * (-12 + theta * (15 - 6 * theta)));
    final double dot6 = theta2 * theta * (4 + theta * (-15 + theta * (18 - 7 * theta)));
    if((previousState != null) && (theta <= 0.5D)) {
      for(int i = 0; i < interpolatedState.length; ++i) {
        interpolatedState[i] = previousState[i] + theta * h * (v[0][i] + eta * (v[1][i] + theta * (v[2][i] + eta * (v[3][i] + theta * (v[4][i] + eta * (v[5][i] + theta * (v[6][i])))))));
        interpolatedDerivatives[i] = v[0][i] + dot1 * v[1][i] + dot2 * v[2][i] + dot3 * v[3][i] + dot4 * v[4][i] + dot5 * v[5][i] + dot6 * v[6][i];
      }
    }
    else {
      for(int i = 0; i < interpolatedState.length; ++i) {
        interpolatedState[i] = currentState[i] - oneMinusThetaH * (v[0][i] - theta * (v[1][i] + theta * (v[2][i] + eta * (v[3][i] + theta * (v[4][i] + eta * (v[5][i] + theta * (v[6][i])))))));
        interpolatedDerivatives[i] = v[0][i] + dot1 * v[1][i] + dot2 * v[2][i] + dot3 * v[3][i] + dot4 * v[4][i] + dot5 * v[5][i] + dot6 * v[6][i];
      }
    }
  }
  @Override() protected void doFinalize() throws MaxCountExceededException {
    if(currentState == null) {
      return ;
    }
    double s;
    final double[] yTmp = new double[currentState.length];
    final double pT = getGlobalPreviousTime();
    for(int j = 0; j < currentState.length; ++j) {
      s = K14_01 * yDotK[0][j] + K14_06 * yDotK[5][j] + K14_07 * yDotK[6][j] + K14_08 * yDotK[7][j] + K14_09 * yDotK[8][j] + K14_10 * yDotK[9][j] + K14_11 * yDotK[10][j] + K14_12 * yDotK[11][j] + K14_13 * yDotK[12][j];
      yTmp[j] = currentState[j] + h * s;
    }
    double[] var_2817 = yDotKLast[0];
    integrator.computeDerivatives(pT + C14 * h, yTmp, var_2817);
    for(int j = 0; j < currentState.length; ++j) {
      s = K15_01 * yDotK[0][j] + K15_06 * yDotK[5][j] + K15_07 * yDotK[6][j] + K15_08 * yDotK[7][j] + K15_09 * yDotK[8][j] + K15_10 * yDotK[9][j] + K15_11 * yDotK[10][j] + K15_12 * yDotK[11][j] + K15_13 * yDotK[12][j] + K15_14 * yDotKLast[0][j];
      yTmp[j] = currentState[j] + h * s;
    }
    integrator.computeDerivatives(pT + C15 * h, yTmp, yDotKLast[1]);
    for(int j = 0; j < currentState.length; ++j) {
      s = K16_01 * yDotK[0][j] + K16_06 * yDotK[5][j] + K16_07 * yDotK[6][j] + K16_08 * yDotK[7][j] + K16_09 * yDotK[8][j] + K16_10 * yDotK[9][j] + K16_11 * yDotK[10][j] + K16_12 * yDotK[11][j] + K16_13 * yDotK[12][j] + K16_14 * yDotKLast[0][j] + K16_15 * yDotKLast[1][j];
      yTmp[j] = currentState[j] + h * s;
    }
    integrator.computeDerivatives(pT + C16 * h, yTmp, yDotKLast[2]);
  }
  @Override() public void readExternal(final ObjectInput in) throws IOException, ClassNotFoundException {
    yDotKLast = new double[3][];
    final int dimension = in.readInt();
    yDotKLast[0] = (dimension < 0) ? null : new double[dimension];
    yDotKLast[1] = (dimension < 0) ? null : new double[dimension];
    yDotKLast[2] = (dimension < 0) ? null : new double[dimension];
    for(int i = 0; i < dimension; ++i) {
      yDotKLast[0][i] = in.readDouble();
      yDotKLast[1][i] = in.readDouble();
      yDotKLast[2][i] = in.readDouble();
    }
    super.readExternal(in);
  }
  @Override() public void reinitialize(final AbstractIntegrator integrator, final double[] y, final double[][] yDotK, final boolean forward, final EquationsMapper primaryMapper, final EquationsMapper[] secondaryMappers) {
    super.reinitialize(integrator, y, yDotK, forward, primaryMapper, secondaryMappers);
    final int dimension = currentState.length;
    yDotKLast = new double[3][];
    for(int k = 0; k < yDotKLast.length; ++k) {
      yDotKLast[k] = new double[dimension];
    }
    v = new double[7][];
    for(int k = 0; k < v.length; ++k) {
      v[k] = new double[dimension];
    }
    vectorsInitialized = false;
  }
  @Override() public void storeTime(final double t) {
    super.storeTime(t);
    vectorsInitialized = false;
  }
  @Override() public void writeExternal(final ObjectOutput out) throws IOException {
    try {
      finalizeStep();
    }
    catch (MaxCountExceededException mcee) {
      final IOException ioe = new IOException(mcee.getLocalizedMessage());
      ioe.initCause(mcee);
      throw ioe;
    }
    final int dimension = (currentState == null) ? -1 : currentState.length;
    out.writeInt(dimension);
    for(int i = 0; i < dimension; ++i) {
      out.writeDouble(yDotKLast[0][i]);
      out.writeDouble(yDotKLast[1][i]);
      out.writeDouble(yDotKLast[2][i]);
    }
    super.writeExternal(out);
  }
}