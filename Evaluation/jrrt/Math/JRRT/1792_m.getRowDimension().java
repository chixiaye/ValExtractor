package org.apache.commons.math3.linear;
import org.apache.commons.math3.exception.DimensionMismatchException;
import org.apache.commons.math3.exception.MaxCountExceededException;
import org.apache.commons.math3.exception.NullArgumentException;
import org.apache.commons.math3.util.IterationManager;
import org.apache.commons.math3.util.MathUtils;

abstract public class PreconditionedIterativeLinearSolver extends IterativeLinearSolver  {
  public PreconditionedIterativeLinearSolver(final IterationManager manager) throws NullArgumentException {
    super(manager);
  }
  public PreconditionedIterativeLinearSolver(final int maxIterations) {
    super(maxIterations);
  }
  public RealVector solve(RealLinearOperator a, RealLinearOperator m, RealVector b) throws NullArgumentException, NonSquareOperatorException, DimensionMismatchException, MaxCountExceededException {
    MathUtils.checkNotNull(a);
    final RealVector x = new ArrayRealVector(a.getColumnDimension());
    return solveInPlace(a, m, b, x);
  }
  public RealVector solve(final RealLinearOperator a, final RealLinearOperator m, final RealVector b, final RealVector x0) throws NullArgumentException, NonSquareOperatorException, DimensionMismatchException, MaxCountExceededException {
    MathUtils.checkNotNull(x0);
    return solveInPlace(a, m, b, x0.copy());
  }
  @Override() public RealVector solve(final RealLinearOperator a, final RealVector b) throws NullArgumentException, NonSquareOperatorException, DimensionMismatchException, MaxCountExceededException {
    MathUtils.checkNotNull(a);
    final RealVector x = new ArrayRealVector(a.getColumnDimension());
    x.set(0.D);
    return solveInPlace(a, null, b, x);
  }
  @Override() public RealVector solve(final RealLinearOperator a, final RealVector b, final RealVector x0) throws NullArgumentException, NonSquareOperatorException, DimensionMismatchException, MaxCountExceededException {
    MathUtils.checkNotNull(x0);
    return solveInPlace(a, null, b, x0.copy());
  }
  abstract public RealVector solveInPlace(RealLinearOperator a, RealLinearOperator m, RealVector b, RealVector x0) throws NullArgumentException, NonSquareOperatorException, DimensionMismatchException, MaxCountExceededException;
  @Override() public RealVector solveInPlace(final RealLinearOperator a, final RealVector b, final RealVector x0) throws NullArgumentException, NonSquareOperatorException, DimensionMismatchException, MaxCountExceededException {
    return solveInPlace(a, null, b, x0);
  }
  protected static void checkParameters(final RealLinearOperator a, final RealLinearOperator m, final RealVector b, final RealVector x0) throws NullArgumentException, NonSquareOperatorException, DimensionMismatchException {
    checkParameters(a, b, x0);
    if(m != null) {
      if(m.getColumnDimension() != m.getRowDimension()) {
        throw new NonSquareOperatorException(m.getColumnDimension(), m.getRowDimension());
      }
      if(m.getRowDimension() != a.getRowDimension()) {
        int var_1792 = m.getRowDimension();
        throw new DimensionMismatchException(var_1792, a.getRowDimension());
      }
    }
  }
}