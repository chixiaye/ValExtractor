package org.apache.commons.math3.linear;
import org.apache.commons.math3.exception.DimensionMismatchException;
import org.apache.commons.math3.exception.MaxCountExceededException;
import org.apache.commons.math3.exception.NullArgumentException;
import org.apache.commons.math3.exception.util.ExceptionContext;
import org.apache.commons.math3.util.FastMath;
import org.apache.commons.math3.util.IterationManager;
import org.apache.commons.math3.util.MathUtils;

public class SymmLQ extends PreconditionedIterativeLinearSolver  {
  final private static String OPERATOR = "operator";
  final private static String THRESHOLD = "threshold";
  final private static String VECTOR = "vector";
  final private static String VECTOR1 = "vector1";
  final private static String VECTOR2 = "vector2";
  final private boolean check;
  final private double delta;
  public SymmLQ(final IterationManager manager, final double delta, final boolean check) {
    super(manager);
    this.delta = delta;
    this.check = check;
  }
  public SymmLQ(final int maxIterations, final double delta, final boolean check) {
    super(maxIterations);
    this.delta = delta;
    this.check = check;
  }
  @Override() public RealVector solve(final RealLinearOperator a, final RealLinearOperator m, final RealVector b) throws NullArgumentException, NonSquareOperatorException, DimensionMismatchException, MaxCountExceededException, NonSelfAdjointOperatorException, NonPositiveDefiniteOperatorException, IllConditionedOperatorException {
    MathUtils.checkNotNull(a);
    final RealVector x = new ArrayRealVector(a.getColumnDimension());
    return solveInPlace(a, m, b, x, false, 0.D);
  }
  public RealVector solve(final RealLinearOperator a, final RealLinearOperator m, final RealVector b, final boolean goodb, final double shift) throws NullArgumentException, NonSquareOperatorException, DimensionMismatchException, MaxCountExceededException, NonSelfAdjointOperatorException, NonPositiveDefiniteOperatorException, IllConditionedOperatorException {
    MathUtils.checkNotNull(a);
    final RealVector x = new ArrayRealVector(a.getColumnDimension());
    return solveInPlace(a, m, b, x, goodb, shift);
  }
  @Override() public RealVector solve(final RealLinearOperator a, final RealLinearOperator m, final RealVector b, final RealVector x) throws NullArgumentException, NonSquareOperatorException, DimensionMismatchException, NonSelfAdjointOperatorException, NonPositiveDefiniteOperatorException, IllConditionedOperatorException, MaxCountExceededException {
    MathUtils.checkNotNull(x);
    return solveInPlace(a, m, b, x.copy(), false, 0.D);
  }
  @Override() public RealVector solve(final RealLinearOperator a, final RealVector b) throws NullArgumentException, NonSquareOperatorException, DimensionMismatchException, NonSelfAdjointOperatorException, IllConditionedOperatorException, MaxCountExceededException {
    MathUtils.checkNotNull(a);
    final RealVector x = new ArrayRealVector(a.getColumnDimension());
    x.set(0.D);
    return solveInPlace(a, null, b, x, false, 0.D);
  }
  public RealVector solve(final RealLinearOperator a, final RealVector b, final boolean goodb, final double shift) throws NullArgumentException, NonSquareOperatorException, DimensionMismatchException, NonSelfAdjointOperatorException, IllConditionedOperatorException, MaxCountExceededException {
    MathUtils.checkNotNull(a);
    final RealVector x = new ArrayRealVector(a.getColumnDimension());
    return solveInPlace(a, null, b, x, goodb, shift);
  }
  @Override() public RealVector solve(final RealLinearOperator a, final RealVector b, final RealVector x) throws NullArgumentException, NonSquareOperatorException, DimensionMismatchException, NonSelfAdjointOperatorException, IllConditionedOperatorException, MaxCountExceededException {
    MathUtils.checkNotNull(x);
    return solveInPlace(a, null, b, x.copy(), false, 0.D);
  }
  @Override() public RealVector solveInPlace(final RealLinearOperator a, final RealLinearOperator m, final RealVector b, final RealVector x) throws NullArgumentException, NonSquareOperatorException, DimensionMismatchException, NonSelfAdjointOperatorException, NonPositiveDefiniteOperatorException, IllConditionedOperatorException, MaxCountExceededException {
    return solveInPlace(a, m, b, x, false, 0.D);
  }
  public RealVector solveInPlace(final RealLinearOperator a, final RealLinearOperator m, final RealVector b, final RealVector x, final boolean goodb, final double shift) throws NullArgumentException, NonSquareOperatorException, DimensionMismatchException, NonSelfAdjointOperatorException, NonPositiveDefiniteOperatorException, IllConditionedOperatorException, MaxCountExceededException {
    checkParameters(a, m, b, x);
    final IterationManager manager = getIterationManager();
    manager.resetIterationCount();
    manager.incrementIterationCount();
    final State state;
    state = new State(a, m, b, goodb, shift, delta, check);
    state.init();
    state.refineSolution(x);
    IterativeLinearSolverEvent event;
    event = new DefaultIterativeLinearSolverEvent(this, manager.getIterations(), x, b, state.getNormOfResidual());
    if(state.bEqualsNullVector()) {
      manager.fireTerminationEvent(event);
      return x;
    }
    final boolean earlyStop;
    earlyStop = state.betaEqualsZero() || state.hasConverged();
    manager.fireInitializationEvent(event);
    if(!earlyStop) {
      do {
        manager.incrementIterationCount();
        event = new DefaultIterativeLinearSolverEvent(this, manager.getIterations(), x, b, state.getNormOfResidual());
        manager.fireIterationStartedEvent(event);
        state.update();
        state.refineSolution(x);
        event = new DefaultIterativeLinearSolverEvent(this, manager.getIterations(), x, b, state.getNormOfResidual());
        manager.fireIterationPerformedEvent(event);
      }while(!state.hasConverged());
    }
    event = new DefaultIterativeLinearSolverEvent(this, manager.getIterations(), x, b, state.getNormOfResidual());
    manager.fireTerminationEvent(event);
    return x;
  }
  @Override() public RealVector solveInPlace(final RealLinearOperator a, final RealVector b, final RealVector x) throws NullArgumentException, NonSquareOperatorException, DimensionMismatchException, NonSelfAdjointOperatorException, IllConditionedOperatorException, MaxCountExceededException {
    return solveInPlace(a, null, b, x, false, 0.D);
  }
  final public boolean getCheck() {
    return check;
  }
  
  private static class State  {
    final static double CBRT_MACH_PREC;
    final static double MACH_PREC;
    final private RealLinearOperator a;
    final private RealVector b;
    final private boolean check;
    final private double delta;
    private double beta;
    private double beta1;
    private double bstep;
    private double cgnorm;
    private double dbar;
    private double gammaZeta;
    private double gbar;
    private double gmax;
    private double gmin;
    final private boolean goodb;
    private boolean hasConverged;
    private double lqnorm;
    final private RealLinearOperator m;
    private double minusEpsZeta;
    final private RealVector mb;
    private double oldb;
    private RealVector r1;
    private RealVector r2;
    private double rnorm;
    final private double shift;
    private double snprod;
    private double tnorm;
    private RealVector wbar;
    final private RealVector xL;
    private RealVector y;
    private double ynorm2;
    private boolean bIsNull;
    static {
      MACH_PREC = FastMath.ulp(1.D);
      CBRT_MACH_PREC = FastMath.cbrt(MACH_PREC);
    }
    public State(final RealLinearOperator a, final RealLinearOperator m, final RealVector b, final boolean goodb, final double shift, final double delta, final boolean check) {
      super();
      this.a = a;
      this.m = m;
      this.b = b;
      this.xL = new ArrayRealVector(b.getDimension());
      this.goodb = goodb;
      this.shift = shift;
      this.mb = m == null ? b : m.operate(b);
      this.hasConverged = false;
      this.check = check;
      this.delta = delta;
    }
    boolean bEqualsNullVector() {
      return bIsNull;
    }
    boolean betaEqualsZero() {
      return beta < MACH_PREC;
    }
    boolean hasConverged() {
      return hasConverged;
    }
    double getNormOfResidual() {
      return rnorm;
    }
    private static void checkSymmetry(final RealLinearOperator l, final RealVector x, final RealVector y, final RealVector z) throws NonSelfAdjointOperatorException {
      final double s = y.dotProduct(y);
      final double t = x.dotProduct(z);
      final double epsa = (s + MACH_PREC) * CBRT_MACH_PREC;
      if(FastMath.abs(s - t) > epsa) {
        final NonSelfAdjointOperatorException e;
        e = new NonSelfAdjointOperatorException();
        final ExceptionContext context = e.getContext();
        context.setValue(SymmLQ.OPERATOR, l);
        context.setValue(SymmLQ.VECTOR1, x);
        context.setValue(SymmLQ.VECTOR2, y);
        context.setValue(SymmLQ.THRESHOLD, Double.valueOf(epsa));
        throw e;
      }
    }
    private static void daxpbypz(final double a, final RealVector x, final double b, final RealVector y, final RealVector z) {
      final int n = z.getDimension();
      for(int i = 0; i < n; i++) {
        final double zi;
        zi = a * x.getEntry(i) + b * y.getEntry(i) + z.getEntry(i);
        z.setEntry(i, zi);
      }
    }
    private static void daxpy(final double a, final RealVector x, final RealVector y) {
      final int n = x.getDimension();
      for(int i = 0; i < n; i++) {
        y.setEntry(i, a * x.getEntry(i) + y.getEntry(i));
      }
    }
    void init() {
      this.xL.set(0.D);
      this.r1 = this.b.copy();
      this.y = this.m == null ? this.b.copy() : this.m.operate(this.r1);
      if((this.m != null) && this.check) {
        checkSymmetry(this.m, this.r1, this.y, this.m.operate(this.y));
      }
      this.beta1 = this.r1.dotProduct(this.y);
      if(this.beta1 < 0.D) {
        throwNPDLOException(this.m, this.y);
      }
      if(this.beta1 == 0.D) {
        this.bIsNull = true;
        return ;
      }
      this.bIsNull = false;
      this.beta1 = FastMath.sqrt(this.beta1);
      final RealVector v = this.y.mapMultiply(1.D / this.beta1);
      this.y = this.a.operate(v);
      if(this.check) {
        checkSymmetry(this.a, v, this.y, this.a.operate(this.y));
      }
      daxpy(-this.shift, v, this.y);
      final double alpha = v.dotProduct(this.y);
      daxpy(-alpha / this.beta1, this.r1, this.y);
      final double vty = v.dotProduct(this.y);
      final double vtv = v.dotProduct(v);
      daxpy(-vty / vtv, v, this.y);
      this.r2 = this.y.copy();
      if(this.m != null) {
        this.y = this.m.operate(this.r2);
      }
      this.oldb = this.beta1;
      this.beta = this.r2.dotProduct(this.y);
      if(this.beta < 0.D) {
        throwNPDLOException(this.m, this.y);
      }
      this.beta = FastMath.sqrt(this.beta);
      this.cgnorm = this.beta1;
      this.gbar = alpha;
      this.dbar = this.beta;
      this.gammaZeta = this.beta1;
      this.minusEpsZeta = 0.D;
      this.bstep = 0.D;
      this.snprod = 1.D;
      this.tnorm = alpha * alpha + this.beta * this.beta;
      this.ynorm2 = 0.D;
      this.gmax = FastMath.abs(alpha) + MACH_PREC;
      this.gmin = this.gmax;
      if(this.goodb) {
        this.wbar = new ArrayRealVector(this.a.getRowDimension());
        this.wbar.set(0.D);
      }
      else {
        this.wbar = v;
      }
      updateNorms();
    }
    void refineSolution(final RealVector x) {
      final int n = this.xL.getDimension();
      if(lqnorm < cgnorm) {
        if(!goodb) {
          x.setSubVector(0, this.xL);
        }
        else {
          final double step = bstep / beta1;
          for(int i = 0; i < n; i++) {
            final double bi = mb.getEntry(i);
            RealVector var_2309 = this.xL;
            final double xi = var_2309.getEntry(i);
            x.setEntry(i, xi + step * bi);
          }
        }
      }
      else {
        final double anorm = FastMath.sqrt(tnorm);
        final double diag = gbar == 0.D ? anorm * MACH_PREC : gbar;
        final double zbar = gammaZeta / diag;
        final double step = (bstep + snprod * zbar) / beta1;
        if(!goodb) {
          for(int i = 0; i < n; i++) {
            final double xi = this.xL.getEntry(i);
            final double wi = wbar.getEntry(i);
            x.setEntry(i, xi + zbar * wi);
          }
        }
        else {
          for(int i = 0; i < n; i++) {
            final double xi = this.xL.getEntry(i);
            final double wi = wbar.getEntry(i);
            final double bi = mb.getEntry(i);
            x.setEntry(i, xi + zbar * wi + step * bi);
          }
        }
      }
    }
    private static void throwNPDLOException(final RealLinearOperator l, final RealVector v) throws NonPositiveDefiniteOperatorException {
      final NonPositiveDefiniteOperatorException e;
      e = new NonPositiveDefiniteOperatorException();
      final ExceptionContext context = e.getContext();
      context.setValue(OPERATOR, l);
      context.setValue(VECTOR, v);
      throw e;
    }
    void update() {
      final RealVector v = y.mapMultiply(1.D / beta);
      y = a.operate(v);
      daxpbypz(-shift, v, -beta / oldb, r1, y);
      final double alpha = v.dotProduct(y);
      daxpy(-alpha / beta, r2, y);
      r1 = r2;
      r2 = y;
      if(m != null) {
        y = m.operate(r2);
      }
      oldb = beta;
      beta = r2.dotProduct(y);
      if(beta < 0.D) {
        throwNPDLOException(m, y);
      }
      beta = FastMath.sqrt(beta);
      tnorm += alpha * alpha + oldb * oldb + beta * beta;
      final double gamma = FastMath.sqrt(gbar * gbar + oldb * oldb);
      final double c = gbar / gamma;
      final double s = oldb / gamma;
      final double deltak = c * dbar + s * alpha;
      gbar = s * dbar - c * alpha;
      final double eps = s * beta;
      dbar = -c * beta;
      final double zeta = gammaZeta / gamma;
      final double zetaC = zeta * c;
      final double zetaS = zeta * s;
      final int n = xL.getDimension();
      for(int i = 0; i < n; i++) {
        final double xi = xL.getEntry(i);
        final double vi = v.getEntry(i);
        final double wi = wbar.getEntry(i);
        xL.setEntry(i, xi + wi * zetaC + vi * zetaS);
        wbar.setEntry(i, wi * s - vi * c);
      }
      bstep += snprod * c * zeta;
      snprod *= s;
      gmax = FastMath.max(gmax, gamma);
      gmin = FastMath.min(gmin, gamma);
      ynorm2 += zeta * zeta;
      gammaZeta = minusEpsZeta - deltak * zeta;
      minusEpsZeta = -eps * zeta;
      updateNorms();
    }
    private void updateNorms() {
      final double anorm = FastMath.sqrt(tnorm);
      final double ynorm = FastMath.sqrt(ynorm2);
      final double epsa = anorm * MACH_PREC;
      final double epsx = anorm * ynorm * MACH_PREC;
      final double epsr = anorm * ynorm * delta;
      final double diag = gbar == 0.D ? epsa : gbar;
      lqnorm = FastMath.sqrt(gammaZeta * gammaZeta + minusEpsZeta * minusEpsZeta);
      final double qrnorm = snprod * beta1;
      cgnorm = qrnorm * beta / FastMath.abs(diag);
      final double acond;
      if(lqnorm <= cgnorm) {
        acond = gmax / gmin;
      }
      else {
        acond = gmax / FastMath.min(gmin, FastMath.abs(diag));
      }
      if(acond * MACH_PREC >= 0.1D) {
        throw new IllConditionedOperatorException(acond);
      }
      if(beta1 <= epsx) {
        throw new SingularOperatorException();
      }
      rnorm = FastMath.min(cgnorm, lqnorm);
      hasConverged = (cgnorm <= epsx) || (cgnorm <= epsr);
    }
  }
}