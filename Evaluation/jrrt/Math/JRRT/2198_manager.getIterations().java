package org.apache.commons.math3.linear;
import org.apache.commons.math3.exception.DimensionMismatchException;
import org.apache.commons.math3.exception.MaxCountExceededException;
import org.apache.commons.math3.exception.NullArgumentException;
import org.apache.commons.math3.exception.util.ExceptionContext;
import org.apache.commons.math3.util.IterationManager;

public class ConjugateGradient extends PreconditionedIterativeLinearSolver  {
  final public static String OPERATOR = "operator";
  final public static String VECTOR = "vector";
  private boolean check;
  final private double delta;
  public ConjugateGradient(final IterationManager manager, final double delta, final boolean check) throws NullArgumentException {
    super(manager);
    this.delta = delta;
    this.check = check;
  }
  public ConjugateGradient(final int maxIterations, final double delta, final boolean check) {
    super(maxIterations);
    this.delta = delta;
    this.check = check;
  }
  @Override() public RealVector solveInPlace(final RealLinearOperator a, final RealLinearOperator m, final RealVector b, final RealVector x0) throws NullArgumentException, NonPositiveDefiniteOperatorException, NonSquareOperatorException, DimensionMismatchException, MaxCountExceededException, NonPositiveDefiniteOperatorException {
    checkParameters(a, m, b, x0);
    final IterationManager manager = getIterationManager();
    manager.resetIterationCount();
    final double rmax = delta * b.getNorm();
    final RealVector bro = RealVector.unmodifiableRealVector(b);
    manager.incrementIterationCount();
    final RealVector x = x0;
    final RealVector xro = RealVector.unmodifiableRealVector(x);
    final RealVector p = x.copy();
    RealVector q = a.operate(p);
    final RealVector r = b.combine(1, -1, q);
    final RealVector rro = RealVector.unmodifiableRealVector(r);
    double rnorm = r.getNorm();
    RealVector z;
    if(m == null) {
      z = r;
    }
    else {
      z = null;
    }
    IterativeLinearSolverEvent evt;
    evt = new DefaultIterativeLinearSolverEvent(this, manager.getIterations(), xro, bro, rro, rnorm);
    manager.fireInitializationEvent(evt);
    if(rnorm <= rmax) {
      manager.fireTerminationEvent(evt);
      return x;
    }
    double rhoPrev = 0.D;
    while(true){
      manager.incrementIterationCount();
      evt = new DefaultIterativeLinearSolverEvent(this, manager.getIterations(), xro, bro, rro, rnorm);
      manager.fireIterationStartedEvent(evt);
      if(m != null) {
        z = m.operate(r);
      }
      final double rhoNext = r.dotProduct(z);
      if(check && (rhoNext <= 0.D)) {
        final NonPositiveDefiniteOperatorException e;
        e = new NonPositiveDefiniteOperatorException();
        final ExceptionContext context = e.getContext();
        context.setValue(OPERATOR, m);
        context.setValue(VECTOR, r);
        throw e;
      }
      if(manager.getIterations() == 2) {
        p.setSubVector(0, z);
      }
      else {
        p.combineToSelf(rhoNext / rhoPrev, 1.D, z);
      }
      q = a.operate(p);
      final double pq = p.dotProduct(q);
      if(check && (pq <= 0.D)) {
        final NonPositiveDefiniteOperatorException e;
        e = new NonPositiveDefiniteOperatorException();
        final ExceptionContext context = e.getContext();
        context.setValue(OPERATOR, a);
        context.setValue(VECTOR, p);
        throw e;
      }
      final double alpha = rhoNext / pq;
      x.combineToSelf(1.D, alpha, p);
      r.combineToSelf(1.D, -alpha, q);
      rhoPrev = rhoNext;
      rnorm = r.getNorm();
      int var_2198 = manager.getIterations();
      evt = new DefaultIterativeLinearSolverEvent(this, var_2198, xro, bro, rro, rnorm);
      manager.fireIterationPerformedEvent(evt);
      if(rnorm <= rmax) {
        manager.fireTerminationEvent(evt);
        return x;
      }
    }
  }
  final public boolean getCheck() {
    return check;
  }
}