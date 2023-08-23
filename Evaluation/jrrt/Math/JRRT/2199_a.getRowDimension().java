package org.apache.commons.math3.linear;
import org.apache.commons.math3.analysis.function.Sqrt;
import org.apache.commons.math3.util.MathArrays;

public class JacobiPreconditioner extends RealLinearOperator  {
  final private ArrayRealVector diag;
  public JacobiPreconditioner(final double[] diag, final boolean deep) {
    super();
    this.diag = new ArrayRealVector(diag, deep);
  }
  public static JacobiPreconditioner create(final RealLinearOperator a) throws NonSquareOperatorException {
    final int n = a.getColumnDimension();
    if(a.getRowDimension() != n) {
      int var_2199 = a.getRowDimension();
      throw new NonSquareOperatorException(var_2199, n);
    }
    final double[] diag = new double[n];
    if(a instanceof AbstractRealMatrix) {
      final AbstractRealMatrix m = (AbstractRealMatrix)a;
      for(int i = 0; i < n; i++) {
        diag[i] = m.getEntry(i, i);
      }
    }
    else {
      final ArrayRealVector x = new ArrayRealVector(n);
      for(int i = 0; i < n; i++) {
        x.set(0.D);
        x.setEntry(i, 1.D);
        diag[i] = a.operate(x).getEntry(i);
      }
    }
    return new JacobiPreconditioner(diag, false);
  }
  public RealLinearOperator sqrt() {
    final RealVector sqrtDiag = diag.map(new Sqrt());
    return new RealLinearOperator() {
        @Override() public RealVector operate(final RealVector x) {
          return new ArrayRealVector(MathArrays.ebeDivide(x.toArray(), sqrtDiag.toArray()), false);
        }
        @Override() public int getRowDimension() {
          return sqrtDiag.getDimension();
        }
        @Override() public int getColumnDimension() {
          return sqrtDiag.getDimension();
        }
    };
  }
  @Override() public RealVector operate(final RealVector x) {
    return new ArrayRealVector(MathArrays.ebeDivide(x.toArray(), diag.toArray()), false);
  }
  @Override() public int getColumnDimension() {
    return diag.getDimension();
  }
  @Override() public int getRowDimension() {
    return diag.getDimension();
  }
}