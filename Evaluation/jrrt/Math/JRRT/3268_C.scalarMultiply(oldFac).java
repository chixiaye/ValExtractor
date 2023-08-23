package org.apache.commons.math3.optimization.direct;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.apache.commons.math3.analysis.MultivariateFunction;
import org.apache.commons.math3.exception.DimensionMismatchException;
import org.apache.commons.math3.exception.NotPositiveException;
import org.apache.commons.math3.exception.NotStrictlyPositiveException;
import org.apache.commons.math3.exception.OutOfRangeException;
import org.apache.commons.math3.exception.TooManyEvaluationsException;
import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.EigenDecomposition;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.optimization.ConvergenceChecker;
import org.apache.commons.math3.optimization.OptimizationData;
import org.apache.commons.math3.optimization.GoalType;
import org.apache.commons.math3.optimization.MultivariateOptimizer;
import org.apache.commons.math3.optimization.PointValuePair;
import org.apache.commons.math3.optimization.SimpleValueChecker;
import org.apache.commons.math3.random.MersenneTwister;
import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.math3.util.MathArrays;

@Deprecated() public class CMAESOptimizer extends BaseAbstractMultivariateSimpleBoundsOptimizer<MultivariateFunction> implements MultivariateOptimizer  {
  final public static int DEFAULT_CHECKFEASABLECOUNT = 0;
  final public static double DEFAULT_STOPFITNESS = 0;
  final public static boolean DEFAULT_ISACTIVECMA = true;
  final public static int DEFAULT_MAXITERATIONS = 30000;
  final public static int DEFAULT_DIAGONALONLY = 0;
  final public static RandomGenerator DEFAULT_RANDOMGENERATOR = new MersenneTwister();
  private int lambda;
  private boolean isActiveCMA;
  private int checkFeasableCount;
  private double[] inputSigma;
  private int dimension;
  private int diagonalOnly = 0;
  private boolean isMinimize = true;
  private boolean generateStatistics = false;
  private int maxIterations;
  private double stopFitness;
  private double stopTolUpX;
  private double stopTolX;
  private double stopTolFun;
  private double stopTolHistFun;
  private int mu;
  private double logMu2;
  private RealMatrix weights;
  private double mueff;
  private double sigma;
  private double cc;
  private double cs;
  private double damps;
  private double ccov1;
  private double ccovmu;
  private double chiN;
  private double ccov1Sep;
  private double ccovmuSep;
  private RealMatrix xmean;
  private RealMatrix pc;
  private RealMatrix ps;
  private double normps;
  private RealMatrix B;
  private RealMatrix D;
  private RealMatrix BD;
  private RealMatrix diagD;
  private RealMatrix C;
  private RealMatrix diagC;
  private int iterations;
  private double[] fitnessHistory;
  private int historySize;
  private RandomGenerator random;
  private List<Double> statisticsSigmaHistory = new ArrayList<Double>();
  private List<RealMatrix> statisticsMeanHistory = new ArrayList<RealMatrix>();
  private List<Double> statisticsFitnessHistory = new ArrayList<Double>();
  private List<RealMatrix> statisticsDHistory = new ArrayList<RealMatrix>();
  @Deprecated() public CMAESOptimizer() {
    this(0);
  }
  @Deprecated() public CMAESOptimizer(int lambda) {
    this(lambda, null, DEFAULT_MAXITERATIONS, DEFAULT_STOPFITNESS, DEFAULT_ISACTIVECMA, DEFAULT_DIAGONALONLY, DEFAULT_CHECKFEASABLECOUNT, DEFAULT_RANDOMGENERATOR, false, null);
  }
  @Deprecated() public CMAESOptimizer(int lambda, double[] inputSigma) {
    this(lambda, inputSigma, DEFAULT_MAXITERATIONS, DEFAULT_STOPFITNESS, DEFAULT_ISACTIVECMA, DEFAULT_DIAGONALONLY, DEFAULT_CHECKFEASABLECOUNT, DEFAULT_RANDOMGENERATOR, false);
  }
  @Deprecated() public CMAESOptimizer(int lambda, double[] inputSigma, int maxIterations, double stopFitness, boolean isActiveCMA, int diagonalOnly, int checkFeasableCount, RandomGenerator random, boolean generateStatistics) {
    this(lambda, inputSigma, maxIterations, stopFitness, isActiveCMA, diagonalOnly, checkFeasableCount, random, generateStatistics, new SimpleValueChecker());
  }
  @Deprecated() public CMAESOptimizer(int lambda, double[] inputSigma, int maxIterations, double stopFitness, boolean isActiveCMA, int diagonalOnly, int checkFeasableCount, RandomGenerator random, boolean generateStatistics, ConvergenceChecker<PointValuePair> checker) {
    super(checker);
    this.lambda = lambda;
    this.inputSigma = inputSigma == null ? null : (double[])inputSigma.clone();
    this.maxIterations = maxIterations;
    this.stopFitness = stopFitness;
    this.isActiveCMA = isActiveCMA;
    this.diagonalOnly = diagonalOnly;
    this.checkFeasableCount = checkFeasableCount;
    this.random = random;
    this.generateStatistics = generateStatistics;
  }
  public CMAESOptimizer(int maxIterations, double stopFitness, boolean isActiveCMA, int diagonalOnly, int checkFeasableCount, RandomGenerator random, boolean generateStatistics, ConvergenceChecker<PointValuePair> checker) {
    super(checker);
    this.maxIterations = maxIterations;
    this.stopFitness = stopFitness;
    this.isActiveCMA = isActiveCMA;
    this.diagonalOnly = diagonalOnly;
    this.checkFeasableCount = checkFeasableCount;
    this.random = random;
    this.generateStatistics = generateStatistics;
  }
  public List<Double> getStatisticsFitnessHistory() {
    return statisticsFitnessHistory;
  }
  public List<Double> getStatisticsSigmaHistory() {
    return statisticsSigmaHistory;
  }
  public List<RealMatrix> getStatisticsDHistory() {
    return statisticsDHistory;
  }
  public List<RealMatrix> getStatisticsMeanHistory() {
    return statisticsMeanHistory;
  }
  @Override() protected PointValuePair doOptimize() {
    checkParameters();
    isMinimize = getGoalType().equals(GoalType.MINIMIZE);
    final FitnessFunction fitfun = new FitnessFunction();
    final double[] guess = getStartPoint();
    dimension = guess.length;
    initializeCMA(guess);
    iterations = 0;
    double bestValue = fitfun.value(guess);
    push(fitnessHistory, bestValue);
    PointValuePair optimum = new PointValuePair(getStartPoint(), isMinimize ? bestValue : -bestValue);
    PointValuePair lastResult = null;
    generationLoop:
      for(iterations = 1; iterations <= maxIterations; iterations++) {
        final RealMatrix arz = randn1(dimension, lambda);
        final RealMatrix arx = zeros(dimension, lambda);
        final double[] fitness = new double[lambda];
        for(int k = 0; k < lambda; k++) {
          RealMatrix arxk = null;
          for(int i = 0; i < checkFeasableCount + 1; i++) {
            if(diagonalOnly <= 0) {
              arxk = xmean.add(BD.multiply(arz.getColumnMatrix(k)).scalarMultiply(sigma));
            }
            else {
              arxk = xmean.add(times(diagD, arz.getColumnMatrix(k)).scalarMultiply(sigma));
            }
            if(i >= checkFeasableCount || fitfun.isFeasible(arxk.getColumn(0))) {
              break ;
            }
            arz.setColumn(k, randn(dimension));
          }
          copyColumn(arxk, 0, arx, k);
          try {
            fitness[k] = fitfun.value(arx.getColumn(k));
          }
          catch (TooManyEvaluationsException e) {
            break generationLoop;
          }
        }
        final int[] arindex = sortedIndices(fitness);
        final RealMatrix xold = xmean;
        final RealMatrix bestArx = selectColumns(arx, MathArrays.copyOf(arindex, mu));
        xmean = bestArx.multiply(weights);
        final RealMatrix bestArz = selectColumns(arz, MathArrays.copyOf(arindex, mu));
        final RealMatrix zmean = bestArz.multiply(weights);
        final boolean hsig = updateEvolutionPaths(zmean, xold);
        if(diagonalOnly <= 0) {
          updateCovariance(hsig, bestArx, arz, arindex, xold);
        }
        else {
          updateCovarianceDiagonalOnly(hsig, bestArz);
        }
        sigma *= Math.exp(Math.min(1, (normps / chiN - 1) * cs / damps));
        final double bestFitness = fitness[arindex[0]];
        final double worstFitness = fitness[arindex[arindex.length - 1]];
        if(bestValue > bestFitness) {
          bestValue = bestFitness;
          lastResult = optimum;
          optimum = new PointValuePair(fitfun.repair(bestArx.getColumn(0)), isMinimize ? bestFitness : -bestFitness);
          if(getConvergenceChecker() != null && lastResult != null && getConvergenceChecker().converged(iterations, optimum, lastResult)) {
            break generationLoop;
          }
        }
        if(stopFitness != 0 && bestFitness < (isMinimize ? stopFitness : -stopFitness)) {
          break generationLoop;
        }
        final double[] sqrtDiagC = sqrt(diagC).getColumn(0);
        final double[] pcCol = pc.getColumn(0);
        for(int i = 0; i < dimension; i++) {
          if(sigma * Math.max(Math.abs(pcCol[i]), sqrtDiagC[i]) > stopTolX) {
            break ;
          }
          if(i >= dimension - 1) {
            break generationLoop;
          }
        }
        for(int i = 0; i < dimension; i++) {
          if(sigma * sqrtDiagC[i] > stopTolUpX) {
            break generationLoop;
          }
        }
        final double historyBest = min(fitnessHistory);
        final double historyWorst = max(fitnessHistory);
        if(iterations > 2 && Math.max(historyWorst, worstFitness) - Math.min(historyBest, bestFitness) < stopTolFun) {
          break generationLoop;
        }
        if(iterations > fitnessHistory.length && historyWorst - historyBest < stopTolHistFun) {
          break generationLoop;
        }
        if(max(diagD) / min(diagD) > 1e7D) {
          break generationLoop;
        }
        if(getConvergenceChecker() != null) {
          final PointValuePair current = new PointValuePair(bestArx.getColumn(0), isMinimize ? bestFitness : -bestFitness);
          if(lastResult != null && getConvergenceChecker().converged(iterations, current, lastResult)) {
            break generationLoop;
          }
          lastResult = current;
        }
        if(bestValue == fitness[arindex[(int)(0.1D + lambda / 4.D)]]) {
          sigma = sigma * Math.exp(0.2D + cs / damps);
        }
        if(iterations > 2 && Math.max(historyWorst, bestFitness) - Math.min(historyBest, bestFitness) == 0) {
          sigma = sigma * Math.exp(0.2D + cs / damps);
        }
        push(fitnessHistory, bestFitness);
        fitfun.setValueRange(worstFitness - bestFitness);
        if(generateStatistics) {
          statisticsSigmaHistory.add(sigma);
          statisticsFitnessHistory.add(bestFitness);
          statisticsMeanHistory.add(xmean.transpose());
          statisticsDHistory.add(diagD.transpose().scalarMultiply(1E5D));
        }
      }
    return optimum;
  }
  @Override() protected PointValuePair optimizeInternal(int maxEval, MultivariateFunction f, GoalType goalType, OptimizationData ... optData) {
    parseOptimizationData(optData);
    return super.optimizeInternal(maxEval, f, goalType, optData);
  }
  private static RealMatrix diag(final RealMatrix m) {
    if(m.getColumnDimension() == 1) {
      final double[][] d = new double[m.getRowDimension()][m.getRowDimension()];
      for(int i = 0; i < m.getRowDimension(); i++) {
        d[i][i] = m.getEntry(i, 0);
      }
      return new Array2DRowRealMatrix(d, false);
    }
    else {
      final double[][] d = new double[m.getRowDimension()][1];
      for(int i = 0; i < m.getColumnDimension(); i++) {
        d[i][0] = m.getEntry(i, i);
      }
      return new Array2DRowRealMatrix(d, false);
    }
  }
  private static RealMatrix divide(final RealMatrix m, final RealMatrix n) {
    final double[][] d = new double[m.getRowDimension()][m.getColumnDimension()];
    for(int r = 0; r < m.getRowDimension(); r++) {
      for(int c = 0; c < m.getColumnDimension(); c++) {
        d[r][c] = m.getEntry(r, c) / n.getEntry(r, c);
      }
    }
    return new Array2DRowRealMatrix(d, false);
  }
  private static RealMatrix eye(int n, int m) {
    final double[][] d = new double[n][m];
    for(int r = 0; r < n; r++) {
      if(r < m) {
        d[r][r] = 1;
      }
    }
    return new Array2DRowRealMatrix(d, false);
  }
  private static RealMatrix log(final RealMatrix m) {
    final double[][] d = new double[m.getRowDimension()][m.getColumnDimension()];
    for(int r = 0; r < m.getRowDimension(); r++) {
      for(int c = 0; c < m.getColumnDimension(); c++) {
        d[r][c] = Math.log(m.getEntry(r, c));
      }
    }
    return new Array2DRowRealMatrix(d, false);
  }
  private static RealMatrix ones(int n, int m) {
    final double[][] d = new double[n][m];
    for(int r = 0; r < n; r++) {
      Arrays.fill(d[r], 1);
    }
    return new Array2DRowRealMatrix(d, false);
  }
  private RealMatrix randn1(int size, int popSize) {
    final double[][] d = new double[size][popSize];
    for(int r = 0; r < size; r++) {
      for(int c = 0; c < popSize; c++) {
        d[r][c] = random.nextGaussian();
      }
    }
    return new Array2DRowRealMatrix(d, false);
  }
  private static RealMatrix repmat(final RealMatrix mat, int n, int m) {
    final int rd = mat.getRowDimension();
    final int cd = mat.getColumnDimension();
    final double[][] d = new double[n * rd][m * cd];
    for(int r = 0; r < n * rd; r++) {
      for(int c = 0; c < m * cd; c++) {
        d[r][c] = mat.getEntry(r % rd, c % cd);
      }
    }
    return new Array2DRowRealMatrix(d, false);
  }
  private static RealMatrix selectColumns(final RealMatrix m, final int[] cols) {
    final double[][] d = new double[m.getRowDimension()][cols.length];
    for(int r = 0; r < m.getRowDimension(); r++) {
      for(int c = 0; c < cols.length; c++) {
        d[r][c] = m.getEntry(r, cols[c]);
      }
    }
    return new Array2DRowRealMatrix(d, false);
  }
  private static RealMatrix sequence(double start, double end, double step) {
    final int size = (int)((end - start) / step + 1);
    final double[][] d = new double[size][1];
    double value = start;
    for(int r = 0; r < size; r++) {
      d[r][0] = value;
      value += step;
    }
    return new Array2DRowRealMatrix(d, false);
  }
  private static RealMatrix sqrt(final RealMatrix m) {
    final double[][] d = new double[m.getRowDimension()][m.getColumnDimension()];
    for(int r = 0; r < m.getRowDimension(); r++) {
      for(int c = 0; c < m.getColumnDimension(); c++) {
        d[r][c] = Math.sqrt(m.getEntry(r, c));
      }
    }
    return new Array2DRowRealMatrix(d, false);
  }
  private static RealMatrix square(final RealMatrix m) {
    final double[][] d = new double[m.getRowDimension()][m.getColumnDimension()];
    for(int r = 0; r < m.getRowDimension(); r++) {
      for(int c = 0; c < m.getColumnDimension(); c++) {
        double e = m.getEntry(r, c);
        d[r][c] = e * e;
      }
    }
    return new Array2DRowRealMatrix(d, false);
  }
  private static RealMatrix sumRows(final RealMatrix m) {
    final double[][] d = new double[1][m.getColumnDimension()];
    for(int c = 0; c < m.getColumnDimension(); c++) {
      double sum = 0;
      for(int r = 0; r < m.getRowDimension(); r++) {
        sum += m.getEntry(r, c);
      }
      d[0][c] = sum;
    }
    return new Array2DRowRealMatrix(d, false);
  }
  private static RealMatrix times(final RealMatrix m, final RealMatrix n) {
    final double[][] d = new double[m.getRowDimension()][m.getColumnDimension()];
    for(int r = 0; r < m.getRowDimension(); r++) {
      for(int c = 0; c < m.getColumnDimension(); c++) {
        d[r][c] = m.getEntry(r, c) * n.getEntry(r, c);
      }
    }
    return new Array2DRowRealMatrix(d, false);
  }
  private static RealMatrix triu(final RealMatrix m, int k) {
    final double[][] d = new double[m.getRowDimension()][m.getColumnDimension()];
    for(int r = 0; r < m.getRowDimension(); r++) {
      for(int c = 0; c < m.getColumnDimension(); c++) {
        d[r][c] = r <= c - k ? m.getEntry(r, c) : 0;
      }
    }
    return new Array2DRowRealMatrix(d, false);
  }
  private static RealMatrix zeros(int n, int m) {
    return new Array2DRowRealMatrix(n, m);
  }
  private boolean updateEvolutionPaths(RealMatrix zmean, RealMatrix xold) {
    ps = ps.scalarMultiply(1 - cs).add(B.multiply(zmean).scalarMultiply(Math.sqrt(cs * (2 - cs) * mueff)));
    normps = ps.getFrobeniusNorm();
    final boolean hsig = normps / Math.sqrt(1 - Math.pow(1 - cs, 2 * iterations)) / chiN < 1.4D + 2 / ((double)dimension + 1);
    pc = pc.scalarMultiply(1 - cc);
    if(hsig) {
      pc = pc.add(xmean.subtract(xold).scalarMultiply(Math.sqrt(cc * (2 - cc) * mueff) / sigma));
    }
    return hsig;
  }
  private static double max(final double[] m) {
    double max = -Double.MAX_VALUE;
    for(int r = 0; r < m.length; r++) {
      if(max < m[r]) {
        max = m[r];
      }
    }
    return max;
  }
  private static double max(final RealMatrix m) {
    double max = -Double.MAX_VALUE;
    for(int r = 0; r < m.getRowDimension(); r++) {
      for(int c = 0; c < m.getColumnDimension(); c++) {
        double e = m.getEntry(r, c);
        if(max < e) {
          max = e;
        }
      }
    }
    return max;
  }
  private static double min(final double[] m) {
    double min = Double.MAX_VALUE;
    for(int r = 0; r < m.length; r++) {
      if(min > m[r]) {
        min = m[r];
      }
    }
    return min;
  }
  private static double min(final RealMatrix m) {
    double min = Double.MAX_VALUE;
    for(int r = 0; r < m.getRowDimension(); r++) {
      for(int c = 0; c < m.getColumnDimension(); c++) {
        double e = m.getEntry(r, c);
        if(min > e) {
          min = e;
        }
      }
    }
    return min;
  }
  private double[] randn(int size) {
    final double[] randn = new double[size];
    for(int i = 0; i < size; i++) {
      randn[i] = random.nextGaussian();
    }
    return randn;
  }
  private static int[] inverse(final int[] indices) {
    final int[] inverse = new int[indices.length];
    for(int i = 0; i < indices.length; i++) {
      inverse[indices[i]] = i;
    }
    return inverse;
  }
  private static int[] reverse(final int[] indices) {
    final int[] reverse = new int[indices.length];
    for(int i = 0; i < indices.length; i++) {
      reverse[i] = indices[indices.length - i - 1];
    }
    return reverse;
  }
  private int[] sortedIndices(final double[] doubles) {
    final DoubleIndex[] dis = new DoubleIndex[doubles.length];
    for(int i = 0; i < doubles.length; i++) {
      dis[i] = new DoubleIndex(doubles[i], i);
    }
    Arrays.sort(dis);
    final int[] indices = new int[doubles.length];
    for(int i = 0; i < doubles.length; i++) {
      indices[i] = dis[i].index;
    }
    return indices;
  }
  private void checkParameters() {
    final double[] init = getStartPoint();
    final double[] lB = getLowerBound();
    final double[] uB = getUpperBound();
    if(inputSigma != null) {
      if(inputSigma.length != init.length) {
        throw new DimensionMismatchException(inputSigma.length, init.length);
      }
      for(int i = 0; i < init.length; i++) {
        if(inputSigma[i] < 0) {
          throw new NotPositiveException(inputSigma[i]);
        }
        if(inputSigma[i] > uB[i] - lB[i]) {
          throw new OutOfRangeException(inputSigma[i], 0, uB[i] - lB[i]);
        }
      }
    }
  }
  private static void copyColumn(final RealMatrix m1, int col1, RealMatrix m2, int col2) {
    for(int i = 0; i < m1.getRowDimension(); i++) {
      m2.setEntry(i, col2, m1.getEntry(i, col1));
    }
  }
  private void initializeCMA(double[] guess) {
    if(lambda <= 0) {
      lambda = 4 + (int)(3 * Math.log(dimension));
    }
    final double[][] sigmaArray = new double[guess.length][1];
    for(int i = 0; i < guess.length; i++) {
      sigmaArray[i][0] = inputSigma == null ? 0.3D : inputSigma[i];
    }
    final RealMatrix insigma = new Array2DRowRealMatrix(sigmaArray, false);
    sigma = max(insigma);
    stopTolUpX = 1e3D * max(insigma);
    stopTolX = 1e-11D * max(insigma);
    stopTolFun = 1e-12D;
    stopTolHistFun = 1e-13D;
    mu = lambda / 2;
    logMu2 = Math.log(mu + 0.5D);
    weights = log(sequence(1, mu, 1)).scalarMultiply(-1).scalarAdd(logMu2);
    double sumw = 0;
    double sumwq = 0;
    for(int i = 0; i < mu; i++) {
      double w = weights.getEntry(i, 0);
      sumw += w;
      sumwq += w * w;
    }
    weights = weights.scalarMultiply(1 / sumw);
    mueff = sumw * sumw / sumwq;
    cc = (4 + mueff / dimension) / (dimension + 4 + 2 * mueff / dimension);
    cs = (mueff + 2) / (dimension + mueff + 3.D);
    damps = (1 + 2 * Math.max(0, Math.sqrt((mueff - 1) / (dimension + 1)) - 1)) * Math.max(0.3D, 1 - dimension / (1e-6D + maxIterations)) + cs;
    ccov1 = 2 / ((dimension + 1.3D) * (dimension + 1.3D) + mueff);
    ccovmu = Math.min(1 - ccov1, 2 * (mueff - 2 + 1 / mueff) / ((dimension + 2) * (dimension + 2) + mueff));
    ccov1Sep = Math.min(1, ccov1 * (dimension + 1.5D) / 3);
    ccovmuSep = Math.min(1 - ccov1, ccovmu * (dimension + 1.5D) / 3);
    chiN = Math.sqrt(dimension) * (1 - 1 / ((double)4 * dimension) + 1 / ((double)21 * dimension * dimension));
    xmean = MatrixUtils.createColumnRealMatrix(guess);
    diagD = insigma.scalarMultiply(1 / sigma);
    diagC = square(diagD);
    pc = zeros(dimension, 1);
    ps = zeros(dimension, 1);
    normps = ps.getFrobeniusNorm();
    B = eye(dimension, dimension);
    D = ones(dimension, 1);
    BD = times(B, repmat(diagD.transpose(), dimension, 1));
    C = B.multiply(diag(square(D)).multiply(B.transpose()));
    historySize = 10 + (int)(3 * 10 * dimension / (double)lambda);
    fitnessHistory = new double[historySize];
    for(int i = 0; i < historySize; i++) {
      fitnessHistory[i] = Double.MAX_VALUE;
    }
  }
  private void parseOptimizationData(OptimizationData ... optData) {
    for (OptimizationData data : optData) {
      if(data instanceof Sigma) {
        inputSigma = ((Sigma)data).getSigma();
        continue ;
      }
      if(data instanceof PopulationSize) {
        lambda = ((PopulationSize)data).getPopulationSize();
        continue ;
      }
    }
  }
  private static void push(double[] vals, double val) {
    for(int i = vals.length - 1; i > 0; i--) {
      vals[i] = vals[i - 1];
    }
    vals[0] = val;
  }
  private void updateBD(double negccov) {
    if(ccov1 + ccovmu + negccov > 0 && (iterations % 1.D / (ccov1 + ccovmu + negccov) / dimension / 10.D) < 1) {
      C = triu(C, 0).add(triu(C, 1).transpose());
      final EigenDecomposition eig = new EigenDecomposition(C);
      B = eig.getV();
      D = eig.getD();
      diagD = diag(D);
      if(min(diagD) <= 0) {
        for(int i = 0; i < dimension; i++) {
          if(diagD.getEntry(i, 0) < 0) {
            diagD.setEntry(i, 0, 0);
          }
        }
        final double tfac = max(diagD) / 1e14D;
        C = C.add(eye(dimension, dimension).scalarMultiply(tfac));
        diagD = diagD.add(ones(dimension, 1).scalarMultiply(tfac));
      }
      if(max(diagD) > 1e14D * min(diagD)) {
        final double tfac = max(diagD) / 1e14D - min(diagD);
        C = C.add(eye(dimension, dimension).scalarMultiply(tfac));
        diagD = diagD.add(ones(dimension, 1).scalarMultiply(tfac));
      }
      diagC = diag(C);
      diagD = sqrt(diagD);
      BD = times(B, repmat(diagD.transpose(), dimension, 1));
    }
  }
  private void updateCovariance(boolean hsig, final RealMatrix bestArx, final RealMatrix arz, final int[] arindex, final RealMatrix xold) {
    double negccov = 0;
    if(ccov1 + ccovmu > 0) {
      final RealMatrix arpos = bestArx.subtract(repmat(xold, 1, mu)).scalarMultiply(1 / sigma);
      final RealMatrix roneu = pc.multiply(pc.transpose()).scalarMultiply(ccov1);
      double oldFac = hsig ? 0 : ccov1 * cc * (2 - cc);
      oldFac += 1 - ccov1 - ccovmu;
      if(isActiveCMA) {
        negccov = (1 - ccovmu) * 0.25D * mueff / (Math.pow(dimension + 2, 1.5D) + 2 * mueff);
        final double negminresidualvariance = 0.66D;
        final double negalphaold = 0.5D;
        final int[] arReverseIndex = reverse(arindex);
        RealMatrix arzneg = selectColumns(arz, MathArrays.copyOf(arReverseIndex, mu));
        RealMatrix arnorms = sqrt(sumRows(square(arzneg)));
        final int[] idxnorms = sortedIndices(arnorms.getRow(0));
        final RealMatrix arnormsSorted = selectColumns(arnorms, idxnorms);
        final int[] idxReverse = reverse(idxnorms);
        final RealMatrix arnormsReverse = selectColumns(arnorms, idxReverse);
        arnorms = divide(arnormsReverse, arnormsSorted);
        final int[] idxInv = inverse(idxnorms);
        final RealMatrix arnormsInv = selectColumns(arnorms, idxInv);
        final double negcovMax = (1 - negminresidualvariance) / square(arnormsInv).multiply(weights).getEntry(0, 0);
        if(negccov > negcovMax) {
          negccov = negcovMax;
        }
        arzneg = times(arzneg, repmat(arnormsInv, dimension, 1));
        final RealMatrix artmp = BD.multiply(arzneg);
        final RealMatrix Cneg = artmp.multiply(diag(weights)).multiply(artmp.transpose());
        oldFac += negalphaold * negccov;
        C = C.scalarMultiply(oldFac).add(roneu).add(arpos.scalarMultiply(ccovmu + (1 - negalphaold) * negccov).multiply(times(repmat(weights, 1, dimension), arpos.transpose()))).subtract(Cneg.scalarMultiply(negccov));
      }
      else {
        RealMatrix var_3268 = C.scalarMultiply(oldFac);
        C = var_3268.add(roneu).add(arpos.scalarMultiply(ccovmu).multiply(times(repmat(weights, 1, dimension), arpos.transpose())));
      }
    }
    updateBD(negccov);
  }
  private void updateCovarianceDiagonalOnly(boolean hsig, final RealMatrix bestArz) {
    double oldFac = hsig ? 0 : ccov1Sep * cc * (2 - cc);
    oldFac += 1 - ccov1Sep - ccovmuSep;
    diagC = diagC.scalarMultiply(oldFac).add(square(pc).scalarMultiply(ccov1Sep)).add((times(diagC, square(bestArz).multiply(weights))).scalarMultiply(ccovmuSep));
    diagD = sqrt(diagC);
    if(diagonalOnly > 1 && iterations > diagonalOnly) {
      diagonalOnly = 0;
      B = eye(dimension, dimension);
      BD = diag(diagD);
      C = diag(diagC);
    }
  }
  
  private static class DoubleIndex implements Comparable<DoubleIndex>  {
    final private double value;
    final private int index;
    DoubleIndex(double value, int index) {
      super();
      this.value = value;
      this.index = index;
    }
    @Override() public boolean equals(Object other) {
      if(this == other) {
        return true;
      }
      if(other instanceof DoubleIndex) {
        return Double.compare(value, ((DoubleIndex)other).value) == 0;
      }
      return false;
    }
    public int compareTo(DoubleIndex o) {
      return Double.compare(value, o.value);
    }
    @Override() public int hashCode() {
      long bits = Double.doubleToLongBits(value);
      return (int)((1438542 ^ (bits >>> 32) ^ bits) & 0xffffffff);
    }
  }
  
  private class FitnessFunction  {
    private double valueRange;
    final private boolean isRepairMode;
    public FitnessFunction() {
      super();
      valueRange = 1;
      isRepairMode = true;
    }
    public boolean isFeasible(final double[] x) {
      final double[] lB = CMAESOptimizer.this.getLowerBound();
      final double[] uB = CMAESOptimizer.this.getUpperBound();
      for(int i = 0; i < x.length; i++) {
        if(x[i] < lB[i]) {
          return false;
        }
        if(x[i] > uB[i]) {
          return false;
        }
      }
      return true;
    }
    private double penalty(final double[] x, final double[] repaired) {
      double penalty = 0;
      for(int i = 0; i < x.length; i++) {
        double diff = Math.abs(x[i] - repaired[i]);
        penalty += diff * valueRange;
      }
      return isMinimize ? penalty : -penalty;
    }
    public double value(final double[] point) {
      double value;
      if(isRepairMode) {
        double[] repaired = repair(point);
        value = CMAESOptimizer.this.computeObjectiveValue(repaired) + penalty(point, repaired);
      }
      else {
        value = CMAESOptimizer.this.computeObjectiveValue(point);
      }
      return isMinimize ? value : -value;
    }
    private double[] repair(final double[] x) {
      final double[] lB = CMAESOptimizer.this.getLowerBound();
      final double[] uB = CMAESOptimizer.this.getUpperBound();
      final double[] repaired = new double[x.length];
      for(int i = 0; i < x.length; i++) {
        if(x[i] < lB[i]) {
          repaired[i] = lB[i];
        }
        else 
          if(x[i] > uB[i]) {
            repaired[i] = uB[i];
          }
          else {
            repaired[i] = x[i];
          }
      }
      return repaired;
    }
    public void setValueRange(double valueRange) {
      this.valueRange = valueRange;
    }
  }
  
  public static class PopulationSize implements OptimizationData  {
    final private int lambda;
    public PopulationSize(int size) throws NotStrictlyPositiveException {
      super();
      if(size <= 0) {
        throw new NotStrictlyPositiveException(size);
      }
      lambda = size;
    }
    public int getPopulationSize() {
      return lambda;
    }
  }
  
  public static class Sigma implements OptimizationData  {
    final private double[] sigma;
    public Sigma(double[] s) throws NotPositiveException {
      super();
      for(int i = 0; i < s.length; i++) {
        if(s[i] < 0) {
          throw new NotPositiveException(s[i]);
        }
      }
      sigma = s.clone();
    }
    public double[] getSigma() {
      return sigma.clone();
    }
  }
}