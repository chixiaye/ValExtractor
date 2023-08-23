package org.apache.commons.math3.ode.nonstiff;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.math3.fraction.BigFraction;
import org.apache.commons.math3.linear.Array2DRowFieldMatrix;
import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.ArrayFieldVector;
import org.apache.commons.math3.linear.FieldDecompositionSolver;
import org.apache.commons.math3.linear.FieldLUDecomposition;
import org.apache.commons.math3.linear.FieldMatrix;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.QRDecomposition;
import org.apache.commons.math3.linear.RealMatrix;

public class AdamsNordsieckTransformer  {
  final private static Map<Integer, AdamsNordsieckTransformer> CACHE = new HashMap<Integer, AdamsNordsieckTransformer>();
  final private Array2DRowRealMatrix update;
  final private double[] c1;
  private AdamsNordsieckTransformer(final int nSteps) {
    super();
    FieldMatrix<BigFraction> bigP = buildP(nSteps);
    FieldDecompositionSolver<BigFraction> pSolver = new FieldLUDecomposition<BigFraction>(bigP).getSolver();
    BigFraction[] u = new BigFraction[nSteps];
    Arrays.fill(u, BigFraction.ONE);
    BigFraction[] bigC1 = pSolver.solve(new ArrayFieldVector<BigFraction>(u, false)).toArray();
    BigFraction[][] shiftedP = bigP.getData();
    for(int i = shiftedP.length - 1; i > 0; --i) {
      shiftedP[i] = shiftedP[i - 1];
    }
    shiftedP[0] = new BigFraction[nSteps];
    Arrays.fill(shiftedP[0], BigFraction.ZERO);
    FieldMatrix<BigFraction> bigMSupdate = pSolver.solve(new Array2DRowFieldMatrix<BigFraction>(shiftedP, false));
    update = MatrixUtils.bigFractionMatrixToRealMatrix(bigMSupdate);
    c1 = new double[nSteps];
    for(int i = 0; i < nSteps; ++i) {
      c1[i] = bigC1[i].doubleValue();
    }
  }
  public static AdamsNordsieckTransformer getInstance(final int nSteps) {
    synchronized(CACHE) {
      AdamsNordsieckTransformer t = CACHE.get(nSteps);
      if(t == null) {
        t = new AdamsNordsieckTransformer(nSteps);
        CACHE.put(nSteps, t);
      }
      return t;
    }
  }
  public Array2DRowRealMatrix initializeHighOrderDerivatives(final double h, final double[] t, final double[][] y, final double[][] yDot) {
    final double[][] a = new double[2 * (y.length - 1)][c1.length];
    int var_2642 = y.length;
    final double[][] b = new double[2 * (var_2642 - 1)][y[0].length];
    final double[] y0 = y[0];
    final double[] yDot0 = yDot[0];
    for(int i = 1; i < y.length; ++i) {
      final double di = t[i] - t[0];
      final double ratio = di / h;
      double dikM1Ohk = 1 / h;
      final double[] aI = a[2 * i - 2];
      final double[] aDotI = a[2 * i - 1];
      for(int j = 0; j < aI.length; ++j) {
        dikM1Ohk *= ratio;
        aI[j] = di * dikM1Ohk;
        aDotI[j] = (j + 2) * dikM1Ohk;
      }
      final double[] yI = y[i];
      final double[] yDotI = yDot[i];
      final double[] bI = b[2 * i - 2];
      final double[] bDotI = b[2 * i - 1];
      for(int j = 0; j < yI.length; ++j) {
        bI[j] = yI[j] - y0[j] - di * yDot0[j];
        bDotI[j] = yDotI[j] - yDot0[j];
      }
    }
    QRDecomposition decomposition;
    decomposition = new QRDecomposition(new Array2DRowRealMatrix(a, false));
    RealMatrix x = decomposition.getSolver().solve(new Array2DRowRealMatrix(b, false));
    return new Array2DRowRealMatrix(x.getData(), false);
  }
  public Array2DRowRealMatrix updateHighOrderDerivativesPhase1(final Array2DRowRealMatrix highOrder) {
    return update.multiply(highOrder);
  }
  private FieldMatrix<BigFraction> buildP(final int nSteps) {
    final BigFraction[][] pData = new BigFraction[nSteps][nSteps];
    for(int i = 0; i < pData.length; ++i) {
      final BigFraction[] pI = pData[i];
      final int factor = -(i + 1);
      int aj = factor;
      for(int j = 0; j < pI.length; ++j) {
        pI[j] = new BigFraction(aj * (j + 2));
        aj *= factor;
      }
    }
    return new Array2DRowFieldMatrix<BigFraction>(pData, false);
  }
  public int getNSteps() {
    return c1.length;
  }
  public void updateHighOrderDerivativesPhase2(final double[] start, final double[] end, final Array2DRowRealMatrix highOrder) {
    final double[][] data = highOrder.getDataRef();
    for(int i = 0; i < data.length; ++i) {
      final double[] dataI = data[i];
      final double c1I = c1[i];
      for(int j = 0; j < dataI.length; ++j) {
        dataI[j] += c1I * (start[j] - end[j]);
      }
    }
  }
}