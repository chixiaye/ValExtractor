package org.apache.commons.math3.linear;
import org.apache.commons.math3.util.FastMath;

public class RRQRDecomposition extends QRDecomposition  {
  private int[] p;
  private RealMatrix cachedP;
  public RRQRDecomposition(RealMatrix matrix) {
    this(matrix, 0D);
  }
  public RRQRDecomposition(RealMatrix matrix, double threshold) {
    super(matrix, threshold);
  }
  @Override() public DecompositionSolver getSolver() {
    return new Solver(super.getSolver(), this.getP());
  }
  public RealMatrix getP() {
    if(cachedP == null) {
      int n = p.length;
      cachedP = MatrixUtils.createRealMatrix(n, n);
      for(int i = 0; i < n; i++) {
        cachedP.setEntry(p[i], i, 1);
      }
    }
    return cachedP;
  }
  public int getRank(final double dropThreshold) {
    RealMatrix r = getR();
    int rows = r.getRowDimension();
    int columns = r.getColumnDimension();
    int rank = 1;
    double lastNorm = r.getFrobeniusNorm();
    double rNorm = lastNorm;
    while(rank < FastMath.min(rows, columns)){
      double thisNorm = r.getSubMatrix(rank, rows - 1, rank, columns - 1).getFrobeniusNorm();
      if(thisNorm == 0 || (thisNorm / lastNorm) * rNorm < dropThreshold) {
        break ;
      }
      lastNorm = thisNorm;
      rank++;
    }
    return rank;
  }
  @Override() protected void decompose(double[][] qrt) {
    p = new int[qrt.length];
    for(int i = 0; i < p.length; i++) {
      p[i] = i;
    }
    super.decompose(qrt);
  }
  @Override() protected void performHouseholderReflection(int minor, double[][] qrt) {
    double l2NormSquaredMax = 0;
    int l2NormSquaredMaxIndex = minor;
    for(int i = minor; i < qrt.length; i++) {
      double l2NormSquared = 0;
      for(int j = 0; j < qrt[i].length; j++) {
        l2NormSquared += qrt[i][j] * qrt[i][j];
      }
      if(l2NormSquared > l2NormSquaredMax) {
        l2NormSquaredMax = l2NormSquared;
        l2NormSquaredMaxIndex = i;
      }
    }
    if(l2NormSquaredMaxIndex != minor) {
      double[] tmp1 = qrt[minor];
      qrt[minor] = qrt[l2NormSquaredMaxIndex];
      qrt[l2NormSquaredMaxIndex] = tmp1;
      int var_2203 = p[minor];
      int tmp2 = var_2203;
      p[minor] = p[l2NormSquaredMaxIndex];
      p[l2NormSquaredMaxIndex] = tmp2;
    }
    super.performHouseholderReflection(minor, qrt);
  }
  
  private static class Solver implements DecompositionSolver  {
    final private DecompositionSolver upper;
    private RealMatrix p;
    private Solver(final DecompositionSolver upper, final RealMatrix p) {
      super();
      this.upper = upper;
      this.p = p;
    }
    public RealMatrix getInverse() {
      return solve(MatrixUtils.createRealIdentityMatrix(p.getRowDimension()));
    }
    public RealMatrix solve(RealMatrix b) {
      return p.multiply(upper.solve(b));
    }
    public RealVector solve(RealVector b) {
      return p.operate(upper.solve(b));
    }
    public boolean isNonSingular() {
      return upper.isNonSingular();
    }
  }
}