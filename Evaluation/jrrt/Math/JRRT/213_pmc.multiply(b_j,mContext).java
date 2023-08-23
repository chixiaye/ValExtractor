package org.apache.commons.math3.analysis.integration.gauss;
import java.math.BigDecimal;
import java.math.MathContext;
import org.apache.commons.math3.exception.DimensionMismatchException;
import org.apache.commons.math3.util.Pair;

public class LegendreHighPrecisionRuleFactory extends BaseRuleFactory<BigDecimal>  {
  final private MathContext mContext;
  final private BigDecimal two;
  final private BigDecimal minusOne;
  final private BigDecimal oneHalf;
  public LegendreHighPrecisionRuleFactory() {
    this(MathContext.DECIMAL128);
  }
  public LegendreHighPrecisionRuleFactory(MathContext mContext) {
    super();
    this.mContext = mContext;
    two = new BigDecimal("2", mContext);
    minusOne = new BigDecimal("-1", mContext);
    oneHalf = new BigDecimal("0.5", mContext);
  }
  @Override() protected Pair<BigDecimal[], BigDecimal[]> computeRule(int numberOfPoints) throws DimensionMismatchException {
    if(numberOfPoints == 1) {
      return new Pair<BigDecimal[], BigDecimal[]>(new BigDecimal[]{ BigDecimal.ZERO } , new BigDecimal[]{ two } );
    }
    final BigDecimal[] previousPoints = getRuleInternal(numberOfPoints - 1).getFirst();
    final BigDecimal[] points = new BigDecimal[numberOfPoints];
    final BigDecimal[] weights = new BigDecimal[numberOfPoints];
    final int iMax = numberOfPoints / 2;
    for(int i = 0; i < iMax; i++) {
      BigDecimal a = (i == 0) ? minusOne : previousPoints[i - 1];
      BigDecimal b = (iMax == 1) ? BigDecimal.ONE : previousPoints[i];
      BigDecimal pma = BigDecimal.ONE;
      BigDecimal pa = a;
      BigDecimal pmb = BigDecimal.ONE;
      BigDecimal pb = b;
      for(int j = 1; j < numberOfPoints; j++) {
        final BigDecimal b_two_j_p_1 = new BigDecimal(2 * j + 1, mContext);
        final BigDecimal b_j = new BigDecimal(j, mContext);
        final BigDecimal b_j_p_1 = new BigDecimal(j + 1, mContext);
        BigDecimal tmp1 = a.multiply(b_two_j_p_1, mContext);
        tmp1 = pa.multiply(tmp1, mContext);
        BigDecimal tmp2 = pma.multiply(b_j, mContext);
        BigDecimal ppa = tmp1.subtract(tmp2, mContext);
        ppa = ppa.divide(b_j_p_1, mContext);
        tmp1 = b.multiply(b_two_j_p_1, mContext);
        tmp1 = pb.multiply(tmp1, mContext);
        tmp2 = pmb.multiply(b_j, mContext);
        BigDecimal ppb = tmp1.subtract(tmp2, mContext);
        ppb = ppb.divide(b_j_p_1, mContext);
        pma = pa;
        pa = ppa;
        pmb = pb;
        pb = ppb;
      }
      BigDecimal c = a.add(b, mContext).multiply(oneHalf, mContext);
      BigDecimal pmc = BigDecimal.ONE;
      BigDecimal pc = c;
      boolean done = false;
      while(!done){
        BigDecimal tmp1 = b.subtract(a, mContext);
        BigDecimal tmp2 = c.ulp().multiply(BigDecimal.TEN, mContext);
        done = tmp1.compareTo(tmp2) <= 0;
        pmc = BigDecimal.ONE;
        pc = c;
        for(int j = 1; j < numberOfPoints; j++) {
          final BigDecimal b_two_j_p_1 = new BigDecimal(2 * j + 1, mContext);
          final BigDecimal b_j = new BigDecimal(j, mContext);
          final BigDecimal b_j_p_1 = new BigDecimal(j + 1, mContext);
          tmp1 = c.multiply(b_two_j_p_1, mContext);
          tmp1 = pc.multiply(tmp1, mContext);
          tmp2 = pmc.multiply(b_j, mContext);
          BigDecimal ppc = tmp1.subtract(tmp2, mContext);
          ppc = ppc.divide(b_j_p_1, mContext);
          pmc = pc;
          pc = ppc;
        }
        if(!done) {
          if(pa.signum() * pc.signum() <= 0) {
            b = c;
            pmb = pmc;
            pb = pc;
          }
          else {
            a = c;
            pma = pmc;
            pa = pc;
          }
          c = a.add(b, mContext).multiply(oneHalf, mContext);
        }
      }
      final BigDecimal nP = new BigDecimal(numberOfPoints, mContext);
      BigDecimal tmp1 = pmc.subtract(c.multiply(pc, mContext), mContext);
      tmp1 = tmp1.multiply(nP);
      tmp1 = tmp1.pow(2, mContext);
      BigDecimal tmp2 = c.pow(2, mContext);
      tmp2 = BigDecimal.ONE.subtract(tmp2, mContext);
      tmp2 = tmp2.multiply(two, mContext);
      tmp2 = tmp2.divide(tmp1, mContext);
      points[i] = c;
      weights[i] = tmp2;
      final int idx = numberOfPoints - i - 1;
      points[idx] = c.negate(mContext);
      weights[idx] = tmp2;
    }
    if(numberOfPoints % 2 != 0) {
      BigDecimal pmc = BigDecimal.ONE;
      for(int j = 1; j < numberOfPoints; j += 2) {
        final BigDecimal b_j = new BigDecimal(j, mContext);
        final BigDecimal b_j_p_1 = new BigDecimal(j + 1, mContext);
        BigDecimal var_213 = pmc.multiply(b_j, mContext);
        pmc = var_213;
        pmc = pmc.divide(b_j_p_1, mContext);
        pmc = pmc.negate(mContext);
      }
      final BigDecimal nP = new BigDecimal(numberOfPoints, mContext);
      BigDecimal tmp1 = pmc.multiply(nP, mContext);
      tmp1 = tmp1.pow(2, mContext);
      BigDecimal tmp2 = two.divide(tmp1, mContext);
      points[iMax] = BigDecimal.ZERO;
      weights[iMax] = tmp2;
    }
    return new Pair<BigDecimal[], BigDecimal[]>(points, weights);
  }
}