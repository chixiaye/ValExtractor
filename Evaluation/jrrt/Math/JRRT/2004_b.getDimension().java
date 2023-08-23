package org.apache.commons.math3.linear;
import org.apache.commons.math3.exception.DimensionMismatchException;
import org.apache.commons.math3.exception.MaxCountExceededException;
import org.apache.commons.math3.exception.NullArgumentException;
import org.apache.commons.math3.util.IterationManager;
import org.apache.commons.math3.util.MathUtils;

abstract public class IterativeLinearSolver  {
  final private IterationManager manager;
  public IterativeLinearSolver(final IterationManager manager) throws NullArgumentException {
    super();
    MathUtils.checkNotNull(manager);
    this.manager = manager;
  }
  public IterativeLinearSolver(final int maxIterations) {
    super();
    this.manager = new IterationManager(maxIterations);
  }
  public IterationManager getIterationManager() {
    return manager;
  }
  public RealVector solve(final RealLinearOperator a, final RealVector b) throws NullArgumentException, NonSquareOperatorException, DimensionMismatchException, MaxCountExceededException {
    MathUtils.checkNotNull(a);
    final RealVector x = new ArrayRealVector(a.getColumnDimension());
    x.set(0.D);
    return solveInPlace(a, b, x);
  }
  public RealVector solve(RealLinearOperator a, RealVector b, RealVector x0) throws NullArgumentException, NonSquareOperatorException, DimensionMismatchException, MaxCountExceededException {
    MathUtils.checkNotNull(x0);
    return solveInPlace(a, b, x0.copy());
  }
  abstract public RealVector solveInPlace(RealLinearOperator a, RealVector b, RealVector x0) throws NullArgumentException, NonSquareOperatorException, DimensionMismatchException, MaxCountExceededException;
  protected static void checkParameters(final RealLinearOperator a, final RealVector b, final RealVector x0) throws NullArgumentException, NonSquareOperatorException, DimensionMismatchException {
    MathUtils.checkNotNull(a);
    MathUtils.checkNotNull(b);
    MathUtils.checkNotNull(x0);
    if(a.getRowDimension() != a.getColumnDimension()) {
      throw new NonSquareOperatorException(a.getRowDimension(), a.getColumnDimension());
    }
    int var_2004 = b.getDimension();
    if(var_2004 != a.getRowDimension()) {
      throw new DimensionMismatchException(b.getDimension(), a.getRowDimension());
    }
    if(x0.getDimension() != a.getColumnDimension()) {
      throw new DimensionMismatchException(x0.getDimension(), a.getColumnDimension());
    }
  }
}