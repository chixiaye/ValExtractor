package org.apache.commons.math3.ode.nonstiff;
import org.apache.commons.math3.util.FastMath;

public class DormandPrince853Integrator extends EmbeddedRungeKuttaIntegrator  {
  final private static String METHOD_NAME = "Dormand-Prince 8 (5, 3)";
  final private static double[] STATIC_C = { (12.0D - 2.0D * FastMath.sqrt(6.0D)) / 135.0D, (6.0D - FastMath.sqrt(6.0D)) / 45.0D, (6.0D - FastMath.sqrt(6.0D)) / 30.0D, (6.0D + FastMath.sqrt(6.0D)) / 30.0D, 1.0D / 3.0D, 1.0D / 4.0D, 4.0D / 13.0D, 127.0D / 195.0D, 3.0D / 5.0D, 6.0D / 7.0D, 1.0D, 1.0D } ;
  final private static double[][] STATIC_A = { { (12.0D - 2.0D * FastMath.sqrt(6.0D)) / 135.0D } , { (6.0D - FastMath.sqrt(6.0D)) / 180.0D, (6.0D - FastMath.sqrt(6.0D)) / 60.0D } , { (6.0D - FastMath.sqrt(6.0D)) / 120.0D, 0.0D, (6.0D - FastMath.sqrt(6.0D)) / 40.0D } , { (462.0D + 107.0D * FastMath.sqrt(6.0D)) / 3000.0D, 0.0D, (-402.0D - 197.0D * FastMath.sqrt(6.0D)) / 1000.0D, (168.0D + 73.0D * FastMath.sqrt(6.0D)) / 375.0D } , { 1.0D / 27.0D, 0.0D, 0.0D, (16.0D + FastMath.sqrt(6.0D)) / 108.0D, (16.0D - FastMath.sqrt(6.0D)) / 108.0D } , { 19.0D / 512.0D, 0.0D, 0.0D, (118.0D + 23.0D * FastMath.sqrt(6.0D)) / 1024.0D, (118.0D - 23.0D * FastMath.sqrt(6.0D)) / 1024.0D, -9.0D / 512.0D } , { 13772.0D / 371293.0D, 0.0D, 0.0D, (51544.0D + 4784.0D * FastMath.sqrt(6.0D)) / 371293.0D, (51544.0D - 4784.0D * FastMath.sqrt(6.0D)) / 371293.0D, -5688.0D / 371293.0D, 3072.0D / 371293.0D } , { 58656157643.0D / 93983540625.0D, 0.0D, 0.0D, (-1324889724104.0D - 318801444819.0D * FastMath.sqrt(6.0D)) / 626556937500.0D, (-1324889724104.0D + 318801444819.0D * FastMath.sqrt(6.0D)) / 626556937500.0D, 96044563816.0D / 3480871875.0D, 5682451879168.0D / 281950621875.0D, -165125654.0D / 3796875.0D } , { 8909899.0D / 18653125.0D, 0.0D, 0.0D, (-4521408.0D - 1137963.0D * FastMath.sqrt(6.0D)) / 2937500.0D, (-4521408.0D + 1137963.0D * FastMath.sqrt(6.0D)) / 2937500.0D, 96663078.0D / 4553125.0D, 2107245056.0D / 137915625.0D, -4913652016.0D / 147609375.0D, -78894270.0D / 3880452869.0D } , { -20401265806.0D / 21769653311.0D, 0.0D, 0.0D, (354216.0D + 94326.0D * FastMath.sqrt(6.0D)) / 112847.0D, (354216.0D - 94326.0D * FastMath.sqrt(6.0D)) / 112847.0D, -43306765128.0D / 5313852383.0D, -20866708358144.0D / 1126708119789.0D, 14886003438020.0D / 654632330667.0D, 35290686222309375.0D / 14152473387134411.0D, -1477884375.0D / 485066827.0D } , { 39815761.0D / 17514443.0D, 0.0D, 0.0D, (-3457480.0D - 960905.0D * FastMath.sqrt(6.0D)) / 551636.0D, (-3457480.0D + 960905.0D * FastMath.sqrt(6.0D)) / 551636.0D, -844554132.0D / 47026969.0D, 8444996352.0D / 302158619.0D, -2509602342.0D / 877790785.0D, -28388795297996250.0D / 3199510091356783.0D, 226716250.0D / 18341897.0D, 1371316744.0D / 2131383595.0D } , { 104257.0D / 1920240.0D, 0.0D, 0.0D, 0.0D, 0.0D, 3399327.0D / 763840.0D, 66578432.0D / 35198415.0D, -1674902723.0D / 288716400.0D, 54980371265625.0D / 176692375811392.0D, -734375.0D / 4826304.0D, 171414593.0D / 851261400.0D, 137909.0D / 3084480.0D }  } ;
  final private static double[] STATIC_B = { 104257.0D / 1920240.0D, 0.0D, 0.0D, 0.0D, 0.0D, 3399327.0D / 763840.0D, 66578432.0D / 35198415.0D, -1674902723.0D / 288716400.0D, 54980371265625.0D / 176692375811392.0D, -734375.0D / 4826304.0D, 171414593.0D / 851261400.0D, 137909.0D / 3084480.0D, 0.0D } ;
  final private static double E1_01 = 116092271.0D / 8848465920.0D;
  final private static double E1_06 = -1871647.0D / 1527680.0D;
  final private static double E1_07 = -69799717.0D / 140793660.0D;
  final private static double E1_08 = 1230164450203.0D / 739113984000.0D;
  final private static double E1_09 = -1980813971228885.0D / 5654156025964544.0D;
  final private static double E1_10 = 464500805.0D / 1389975552.0D;
  final private static double E1_11 = 1606764981773.0D / 19613062656000.0D;
  final private static double E1_12 = -137909.0D / 6168960.0D;
  final private static double E2_01 = -364463.0D / 1920240.0D;
  final private static double E2_06 = 3399327.0D / 763840.0D;
  final private static double E2_07 = 66578432.0D / 35198415.0D;
  final private static double E2_08 = -1674902723.0D / 288716400.0D;
  final private static double E2_09 = -74684743568175.0D / 176692375811392.0D;
  final private static double E2_10 = -734375.0D / 4826304.0D;
  final private static double E2_11 = 171414593.0D / 851261400.0D;
  final private static double E2_12 = 69869.0D / 3084480.0D;
  public DormandPrince853Integrator(final double minStep, final double maxStep, final double scalAbsoluteTolerance, final double scalRelativeTolerance) {
    super(METHOD_NAME, true, STATIC_C, STATIC_A, STATIC_B, new DormandPrince853StepInterpolator(), minStep, maxStep, scalAbsoluteTolerance, scalRelativeTolerance);
  }
  public DormandPrince853Integrator(final double minStep, final double maxStep, final double[] vecAbsoluteTolerance, final double[] vecRelativeTolerance) {
    super(METHOD_NAME, true, STATIC_C, STATIC_A, STATIC_B, new DormandPrince853StepInterpolator(), minStep, maxStep, vecAbsoluteTolerance, vecRelativeTolerance);
  }
  @Override() protected double estimateError(final double[][] yDotK, final double[] y0, final double[] y1, final double h) {
    double error1 = 0;
    double error2 = 0;
    for(int j = 0; j < mainSetDimension; ++j) {
      final double errSum1 = E1_01 * yDotK[0][j] + E1_06 * yDotK[5][j] + E1_07 * yDotK[6][j] + E1_08 * yDotK[7][j] + E1_09 * yDotK[8][j] + E1_10 * yDotK[9][j] + E1_11 * yDotK[10][j] + E1_12 * yDotK[11][j];
      double[] var_2865 = yDotK[6];
      final double errSum2 = E2_01 * yDotK[0][j] + E2_06 * yDotK[5][j] + E2_07 * var_2865[j] + E2_08 * yDotK[7][j] + E2_09 * yDotK[8][j] + E2_10 * yDotK[9][j] + E2_11 * yDotK[10][j] + E2_12 * yDotK[11][j];
      final double yScale = FastMath.max(FastMath.abs(y0[j]), FastMath.abs(y1[j]));
      final double tol = (vecAbsoluteTolerance == null) ? (scalAbsoluteTolerance + scalRelativeTolerance * yScale) : (vecAbsoluteTolerance[j] + vecRelativeTolerance[j] * yScale);
      final double ratio1 = errSum1 / tol;
      error1 += ratio1 * ratio1;
      final double ratio2 = errSum2 / tol;
      error2 += ratio2 * ratio2;
    }
    double den = error1 + 0.01D * error2;
    if(den <= 0.0D) {
      den = 1.0D;
    }
    return FastMath.abs(h) * error1 / FastMath.sqrt(mainSetDimension * den);
  }
  @Override() public int getOrder() {
    return 8;
  }
}