package org.apache.commons.math3.optimization.direct;
import org.apache.commons.math3.util.Incrementor;
import org.apache.commons.math3.exception.MaxCountExceededException;
import org.apache.commons.math3.exception.TooManyEvaluationsException;
import org.apache.commons.math3.exception.DimensionMismatchException;
import org.apache.commons.math3.exception.NullArgumentException;
import org.apache.commons.math3.analysis.MultivariateVectorFunction;
import org.apache.commons.math3.optimization.OptimizationData;
import org.apache.commons.math3.optimization.InitialGuess;
import org.apache.commons.math3.optimization.Target;
import org.apache.commons.math3.optimization.Weight;
import org.apache.commons.math3.optimization.BaseMultivariateVectorOptimizer;
import org.apache.commons.math3.optimization.ConvergenceChecker;
import org.apache.commons.math3.optimization.PointVectorValuePair;
import org.apache.commons.math3.optimization.SimpleVectorValueChecker;
import org.apache.commons.math3.linear.RealMatrix;
abstract @Deprecated() public class BaseAbstractMultivariateVectorOptimizer<FUNC extends org.apache.commons.math3.analysis.MultivariateVectorFunction> implements BaseMultivariateVectorOptimizer<FUNC>  {
  final protected Incrementor evaluations = new Incrementor();
  private ConvergenceChecker<PointVectorValuePair> checker;
  private double[] target;
  private RealMatrix weightMatrix;
  @Deprecated() private double[] weight;
  private double[] start;
  private FUNC function;
  @Deprecated() protected BaseAbstractMultivariateVectorOptimizer() {
    this(new SimpleVectorValueChecker());
  }
  protected BaseAbstractMultivariateVectorOptimizer(ConvergenceChecker<PointVectorValuePair> checker) {
    super();
    this.checker = checker;
  }
  public ConvergenceChecker<PointVectorValuePair> getConvergenceChecker() {
    return checker;
  }
  protected FUNC getObjectiveFunction() {
    return function;
  }
  abstract protected PointVectorValuePair doOptimize();
  @Deprecated() public PointVectorValuePair optimize(int maxEval, FUNC f, double[] t, double[] w, double[] startPoint) {
    return optimizeInternal(maxEval, f, t, w, startPoint);
  }
  protected PointVectorValuePair optimize(int maxEval, FUNC f, OptimizationData ... optData) throws TooManyEvaluationsException, DimensionMismatchException {
    return optimizeInternal(maxEval, f, optData);
  }
  @Deprecated() protected PointVectorValuePair optimizeInternal(final int maxEval, final FUNC f, final double[] t, final double[] w, final double[] startPoint) {
    if(f == null) {
      throw new NullArgumentException();
    }
    if(t == null) {
      throw new NullArgumentException();
    }
    if(w == null) {
      throw new NullArgumentException();
    }
    if(startPoint == null) {
      throw new NullArgumentException();
    }
    if(t.length != w.length) {
      int var_3449 = t.length;
      throw new DimensionMismatchException(var_3449, w.length);
    }
    return optimizeInternal(maxEval, f, new Target(t), new Weight(w), new InitialGuess(startPoint));
  }
  protected PointVectorValuePair optimizeInternal(int maxEval, FUNC f, OptimizationData ... optData) throws TooManyEvaluationsException, DimensionMismatchException {
    evaluations.setMaximalCount(maxEval);
    evaluations.resetCount();
    function = f;
    parseOptimizationData(optData);
    checkParameters();
    setUp();
    return doOptimize();
  }
  public RealMatrix getWeight() {
    return weightMatrix.copy();
  }
  protected double[] computeObjectiveValue(double[] point) {
    try {
      evaluations.incrementCount();
    }
    catch (MaxCountExceededException e) {
      throw new TooManyEvaluationsException(e.getMax());
    }
    return function.value(point);
  }
  public double[] getStartPoint() {
    return start.clone();
  }
  public double[] getTarget() {
    return target.clone();
  }
  @Deprecated() protected double[] getTargetRef() {
    return target;
  }
  @Deprecated() protected double[] getWeightRef() {
    return weight;
  }
  public int getEvaluations() {
    return evaluations.getCount();
  }
  public int getMaxEvaluations() {
    return evaluations.getMaximalCount();
  }
  private void checkParameters() {
    if(target.length != weightMatrix.getColumnDimension()) {
      throw new DimensionMismatchException(target.length, weightMatrix.getColumnDimension());
    }
  }
  private void parseOptimizationData(OptimizationData ... optData) {
    for (OptimizationData data : optData) {
      if(data instanceof Target) {
        target = ((Target)data).getTarget();
        continue ;
      }
      if(data instanceof Weight) {
        weightMatrix = ((Weight)data).getWeight();
        continue ;
      }
      if(data instanceof InitialGuess) {
        start = ((InitialGuess)data).getInitialGuess();
        continue ;
      }
    }
  }
  protected void setUp() {
    final int dim = target.length;
    weight = new double[dim];
    for(int i = 0; i < dim; i++) {
      weight[i] = weightMatrix.getEntry(i, i);
    }
  }
}