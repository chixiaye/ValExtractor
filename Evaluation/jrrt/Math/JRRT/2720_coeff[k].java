package org.apache.commons.math3.ode.nonstiff;
import org.apache.commons.math3.analysis.solvers.UnivariateSolver;
import org.apache.commons.math3.exception.DimensionMismatchException;
import org.apache.commons.math3.exception.MaxCountExceededException;
import org.apache.commons.math3.exception.NoBracketingException;
import org.apache.commons.math3.exception.NumberIsTooSmallException;
import org.apache.commons.math3.ode.ExpandableStatefulODE;
import org.apache.commons.math3.ode.events.EventHandler;
import org.apache.commons.math3.ode.sampling.AbstractStepInterpolator;
import org.apache.commons.math3.ode.sampling.StepHandler;
import org.apache.commons.math3.util.FastMath;

public class GraggBulirschStoerIntegrator extends AdaptiveStepsizeIntegrator  {
  final private static String METHOD_NAME = "Gragg-Bulirsch-Stoer";
  private int maxOrder;
  private int[] sequence;
  private int[] costPerStep;
  private double[] costPerTimeUnit;
  private double[] optimalStep;
  private double[][] coeff;
  private boolean performTest;
  private int maxChecks;
  private int maxIter;
  private double stabilityReduction;
  private double stepControl1;
  private double stepControl2;
  private double stepControl3;
  private double stepControl4;
  private double orderControl1;
  private double orderControl2;
  private boolean useInterpolationError;
  private int mudif;
  public GraggBulirschStoerIntegrator(final double minStep, final double maxStep, final double scalAbsoluteTolerance, final double scalRelativeTolerance) {
    super(METHOD_NAME, minStep, maxStep, scalAbsoluteTolerance, scalRelativeTolerance);
    setStabilityCheck(true, -1, -1, -1);
    setControlFactors(-1, -1, -1, -1);
    setOrderControl(-1, -1, -1);
    setInterpolationControl(true, -1);
  }
  public GraggBulirschStoerIntegrator(final double minStep, final double maxStep, final double[] vecAbsoluteTolerance, final double[] vecRelativeTolerance) {
    super(METHOD_NAME, minStep, maxStep, vecAbsoluteTolerance, vecRelativeTolerance);
    setStabilityCheck(true, -1, -1, -1);
    setControlFactors(-1, -1, -1, -1);
    setOrderControl(-1, -1, -1);
    setInterpolationControl(true, -1);
  }
  private boolean tryStep(final double t0, final double[] y0, final double step, final int k, final double[] scale, final double[][] f, final double[] yMiddle, final double[] yEnd, final double[] yTmp) throws MaxCountExceededException, DimensionMismatchException {
    final int n = sequence[k];
    final double subStep = step / n;
    final double subStep2 = 2 * subStep;
    double t = t0 + subStep;
    for(int i = 0; i < y0.length; ++i) {
      yTmp[i] = y0[i];
      yEnd[i] = y0[i] + subStep * f[0][i];
    }
    computeDerivatives(t, yEnd, f[1]);
    for(int j = 1; j < n; ++j) {
      if(2 * j == n) {
        System.arraycopy(yEnd, 0, yMiddle, 0, y0.length);
      }
      t += subStep;
      for(int i = 0; i < y0.length; ++i) {
        final double middle = yEnd[i];
        yEnd[i] = yTmp[i] + subStep2 * f[j][i];
        yTmp[i] = middle;
      }
      computeDerivatives(t, yEnd, f[j + 1]);
      if(performTest && (j <= maxChecks) && (k < maxIter)) {
        double initialNorm = 0.0D;
        for(int l = 0; l < scale.length; ++l) {
          final double ratio = f[0][l] / scale[l];
          initialNorm += ratio * ratio;
        }
        double deltaNorm = 0.0D;
        for(int l = 0; l < scale.length; ++l) {
          final double ratio = (f[j + 1][l] - f[0][l]) / scale[l];
          deltaNorm += ratio * ratio;
        }
        if(deltaNorm > 4 * FastMath.max(1.0e-15D, initialNorm)) {
          return false;
        }
      }
    }
    for(int i = 0; i < y0.length; ++i) {
      yEnd[i] = 0.5D * (yTmp[i] + yEnd[i] + subStep * f[n][i]);
    }
    return true;
  }
  @Override() public void addEventHandler(final EventHandler function, final double maxCheckInterval, final double convergence, final int maxIterationCount, final UnivariateSolver solver) {
    super.addEventHandler(function, maxCheckInterval, convergence, maxIterationCount, solver);
    initializeArrays();
  }
  @Override() public void addStepHandler(final StepHandler handler) {
    super.addStepHandler(handler);
    initializeArrays();
  }
  private void extrapolate(final int offset, final int k, final double[][] diag, final double[] last) {
    for(int j = 1; j < k; ++j) {
      for(int i = 0; i < last.length; ++i) {
        diag[k - j - 1][i] = diag[k - j][i] + coeff[k + offset][j - 1] * (diag[k - j][i] - diag[k - j - 1][i]);
      }
    }
    for(int i = 0; i < last.length; ++i) {
      last[i] = diag[0][i] + coeff[k + offset][k - 1] * (diag[0][i] - last[i]);
    }
  }
  private void initializeArrays() {
    final int size = maxOrder / 2;
    if((sequence == null) || (sequence.length != size)) {
      sequence = new int[size];
      costPerStep = new int[size];
      coeff = new double[size][];
      costPerTimeUnit = new double[size];
      optimalStep = new double[size];
    }
    for(int k = 0; k < size; ++k) {
      sequence[k] = 4 * k + 2;
    }
    costPerStep[0] = sequence[0] + 1;
    for(int k = 1; k < size; ++k) {
      costPerStep[k] = costPerStep[k - 1] + sequence[k];
    }
    for(int k = 0; k < size; ++k) {
      coeff[k] = (k > 0) ? new double[k] : null;
      for(int l = 0; l < k; ++l) {
        final double ratio = ((double)sequence[k]) / sequence[k - l - 1];
        double[] var_2720 = coeff[k];
        var_2720[l] = 1.0D / (ratio * ratio - 1.0D);
      }
    }
  }
  @Override() public void integrate(final ExpandableStatefulODE equations, final double t) throws NumberIsTooSmallException, DimensionMismatchException, MaxCountExceededException, NoBracketingException {
    sanityChecks(equations, t);
    setEquations(equations);
    final boolean forward = t > equations.getTime();
    final double[] y0 = equations.getCompleteState();
    final double[] y = y0.clone();
    final double[] yDot0 = new double[y.length];
    final double[] y1 = new double[y.length];
    final double[] yTmp = new double[y.length];
    final double[] yTmpDot = new double[y.length];
    final double[][] diagonal = new double[sequence.length - 1][];
    final double[][] y1Diag = new double[sequence.length - 1][];
    for(int k = 0; k < sequence.length - 1; ++k) {
      diagonal[k] = new double[y.length];
      y1Diag[k] = new double[y.length];
    }
    final double[][][] fk = new double[sequence.length][][];
    for(int k = 0; k < sequence.length; ++k) {
      fk[k] = new double[sequence[k] + 1][];
      fk[k][0] = yDot0;
      for(int l = 0; l < sequence[k]; ++l) {
        fk[k][l + 1] = new double[y0.length];
      }
    }
    if(y != y0) {
      System.arraycopy(y0, 0, y, 0, y0.length);
    }
    final double[] yDot1 = new double[y0.length];
    final double[][] yMidDots = new double[1 + 2 * sequence.length][y0.length];
    final double[] scale = new double[mainSetDimension];
    rescale(y, y, scale);
    final double tol = (vecRelativeTolerance == null) ? scalRelativeTolerance : vecRelativeTolerance[0];
    final double log10R = FastMath.log10(FastMath.max(1.0e-10D, tol));
    int targetIter = FastMath.max(1, FastMath.min(sequence.length - 2, (int)FastMath.floor(0.5D - 0.6D * log10R)));
    final AbstractStepInterpolator interpolator = new GraggBulirschStoerStepInterpolator(y, yDot0, y1, yDot1, yMidDots, forward, equations.getPrimaryMapper(), equations.getSecondaryMappers());
    interpolator.storeTime(equations.getTime());
    stepStart = equations.getTime();
    double hNew = 0;
    double maxError = Double.MAX_VALUE;
    boolean previousRejected = false;
    boolean firstTime = true;
    boolean newStep = true;
    boolean firstStepAlreadyComputed = false;
    initIntegration(equations.getTime(), y0, t);
    costPerTimeUnit[0] = 0;
    isLastStep = false;
    do {
      double error;
      boolean reject = false;
      if(newStep) {
        interpolator.shift();
        if(!firstStepAlreadyComputed) {
          computeDerivatives(stepStart, y, yDot0);
        }
        if(firstTime) {
          hNew = initializeStep(forward, 2 * targetIter + 1, scale, stepStart, y, yDot0, yTmp, yTmpDot);
        }
        newStep = false;
      }
      stepSize = hNew;
      if((forward && (stepStart + stepSize > t)) || ((!forward) && (stepStart + stepSize < t))) {
        stepSize = t - stepStart;
      }
      final double nextT = stepStart + stepSize;
      isLastStep = forward ? (nextT >= t) : (nextT <= t);
      int k = -1;
      for(boolean loop = true; loop; ) {
        ++k;
        if(!tryStep(stepStart, y, stepSize, k, scale, fk[k], (k == 0) ? yMidDots[0] : diagonal[k - 1], (k == 0) ? y1 : y1Diag[k - 1], yTmp)) {
          hNew = FastMath.abs(filterStep(stepSize * stabilityReduction, forward, false));
          reject = true;
          loop = false;
        }
        else {
          if(k > 0) {
            extrapolate(0, k, y1Diag, y1);
            rescale(y, y1, scale);
            error = 0;
            for(int j = 0; j < mainSetDimension; ++j) {
              final double e = FastMath.abs(y1[j] - y1Diag[0][j]) / scale[j];
              error += e * e;
            }
            error = FastMath.sqrt(error / mainSetDimension);
            if((error > 1.0e15D) || ((k > 1) && (error > maxError))) {
              hNew = FastMath.abs(filterStep(stepSize * stabilityReduction, forward, false));
              reject = true;
              loop = false;
            }
            else {
              maxError = FastMath.max(4 * error, 1.0D);
              final double exp = 1.0D / (2 * k + 1);
              double fac = stepControl2 / FastMath.pow(error / stepControl1, exp);
              final double pow = FastMath.pow(stepControl3, exp);
              fac = FastMath.max(pow / stepControl4, FastMath.min(1 / pow, fac));
              optimalStep[k] = FastMath.abs(filterStep(stepSize * fac, forward, true));
              costPerTimeUnit[k] = costPerStep[k] / optimalStep[k];
              switch (k - targetIter){
                case -1:
                if((targetIter > 1) && !previousRejected) {
                  if(error <= 1.0D) {
                    loop = false;
                  }
                  else {
                    final double ratio = ((double)sequence[targetIter] * sequence[targetIter + 1]) / (sequence[0] * sequence[0]);
                    if(error > ratio * ratio) {
                      reject = true;
                      loop = false;
                      targetIter = k;
                      if((targetIter > 1) && (costPerTimeUnit[targetIter - 1] < orderControl1 * costPerTimeUnit[targetIter])) {
                        --targetIter;
                      }
                      hNew = optimalStep[targetIter];
                    }
                  }
                }
                break ;
                case 0:
                if(error <= 1.0D) {
                  loop = false;
                }
                else {
                  final double ratio = ((double)sequence[k + 1]) / sequence[0];
                  if(error > ratio * ratio) {
                    reject = true;
                    loop = false;
                    if((targetIter > 1) && (costPerTimeUnit[targetIter - 1] < orderControl1 * costPerTimeUnit[targetIter])) {
                      --targetIter;
                    }
                    hNew = optimalStep[targetIter];
                  }
                }
                break ;
                case 1:
                if(error > 1.0D) {
                  reject = true;
                  if((targetIter > 1) && (costPerTimeUnit[targetIter - 1] < orderControl1 * costPerTimeUnit[targetIter])) {
                    --targetIter;
                  }
                  hNew = optimalStep[targetIter];
                }
                loop = false;
                break ;
                default:
                if((firstTime || isLastStep) && (error <= 1.0D)) {
                  loop = false;
                }
                break ;
              }
            }
          }
        }
      }
      if(!reject) {
        computeDerivatives(stepStart + stepSize, y1, yDot1);
      }
      double hInt = getMaxStep();
      if(!reject) {
        for(int j = 1; j <= k; ++j) {
          extrapolate(0, j, diagonal, yMidDots[0]);
        }
        final int mu = 2 * k - mudif + 3;
        for(int l = 0; l < mu; ++l) {
          final int l2 = l / 2;
          double factor = FastMath.pow(0.5D * sequence[l2], l);
          int middleIndex = fk[l2].length / 2;
          for(int i = 0; i < y0.length; ++i) {
            yMidDots[l + 1][i] = factor * fk[l2][middleIndex + l][i];
          }
          for(int j = 1; j <= k - l2; ++j) {
            factor = FastMath.pow(0.5D * sequence[j + l2], l);
            middleIndex = fk[l2 + j].length / 2;
            for(int i = 0; i < y0.length; ++i) {
              diagonal[j - 1][i] = factor * fk[l2 + j][middleIndex + l][i];
            }
            extrapolate(l2, j, diagonal, yMidDots[l + 1]);
          }
          for(int i = 0; i < y0.length; ++i) {
            yMidDots[l + 1][i] *= stepSize;
          }
          for(int j = (l + 1) / 2; j <= k; ++j) {
            for(int m = fk[j].length - 1; m >= 2 * (l + 1); --m) {
              for(int i = 0; i < y0.length; ++i) {
                fk[j][m][i] -= fk[j][m - 2][i];
              }
            }
          }
        }
        if(mu >= 0) {
          final GraggBulirschStoerStepInterpolator gbsInterpolator = (GraggBulirschStoerStepInterpolator)interpolator;
          gbsInterpolator.computeCoefficients(mu, stepSize);
          if(useInterpolationError) {
            final double interpError = gbsInterpolator.estimateError(scale);
            hInt = FastMath.abs(stepSize / FastMath.max(FastMath.pow(interpError, 1.0D / (mu + 4)), 0.01D));
            if(interpError > 10.0D) {
              hNew = hInt;
              reject = true;
            }
          }
        }
      }
      if(!reject) {
        interpolator.storeTime(stepStart + stepSize);
        stepStart = acceptStep(interpolator, y1, yDot1, t);
        interpolator.storeTime(stepStart);
        System.arraycopy(y1, 0, y, 0, y0.length);
        System.arraycopy(yDot1, 0, yDot0, 0, y0.length);
        firstStepAlreadyComputed = true;
        int optimalIter;
        if(k == 1) {
          optimalIter = 2;
          if(previousRejected) {
            optimalIter = 1;
          }
        }
        else 
          if(k <= targetIter) {
            optimalIter = k;
            if(costPerTimeUnit[k - 1] < orderControl1 * costPerTimeUnit[k]) {
              optimalIter = k - 1;
            }
            else 
              if(costPerTimeUnit[k] < orderControl2 * costPerTimeUnit[k - 1]) {
                optimalIter = FastMath.min(k + 1, sequence.length - 2);
              }
          }
          else {
            optimalIter = k - 1;
            if((k > 2) && (costPerTimeUnit[k - 2] < orderControl1 * costPerTimeUnit[k - 1])) {
              optimalIter = k - 2;
            }
            if(costPerTimeUnit[k] < orderControl2 * costPerTimeUnit[optimalIter]) {
              optimalIter = FastMath.min(k, sequence.length - 2);
            }
          }
        if(previousRejected) {
          targetIter = FastMath.min(optimalIter, k);
          hNew = FastMath.min(FastMath.abs(stepSize), optimalStep[targetIter]);
        }
        else {
          if(optimalIter <= k) {
            hNew = optimalStep[optimalIter];
          }
          else {
            if((k < targetIter) && (costPerTimeUnit[k] < orderControl2 * costPerTimeUnit[k - 1])) {
              hNew = filterStep(optimalStep[k] * costPerStep[optimalIter + 1] / costPerStep[k], forward, false);
            }
            else {
              hNew = filterStep(optimalStep[k] * costPerStep[optimalIter] / costPerStep[k], forward, false);
            }
          }
          targetIter = optimalIter;
        }
        newStep = true;
      }
      hNew = FastMath.min(hNew, hInt);
      if(!forward) {
        hNew = -hNew;
      }
      firstTime = false;
      if(reject) {
        isLastStep = false;
        previousRejected = true;
      }
      else {
        previousRejected = false;
      }
    }while(!isLastStep);
    equations.setTime(stepStart);
    equations.setCompleteState(y);
    resetInternalState();
  }
  private void rescale(final double[] y1, final double[] y2, final double[] scale) {
    if(vecAbsoluteTolerance == null) {
      for(int i = 0; i < scale.length; ++i) {
        final double yi = FastMath.max(FastMath.abs(y1[i]), FastMath.abs(y2[i]));
        scale[i] = scalAbsoluteTolerance + scalRelativeTolerance * yi;
      }
    }
    else {
      for(int i = 0; i < scale.length; ++i) {
        final double yi = FastMath.max(FastMath.abs(y1[i]), FastMath.abs(y2[i]));
        scale[i] = vecAbsoluteTolerance[i] + vecRelativeTolerance[i] * yi;
      }
    }
  }
  public void setControlFactors(final double control1, final double control2, final double control3, final double control4) {
    if((control1 < 0.0001D) || (control1 > 0.9999D)) {
      this.stepControl1 = 0.65D;
    }
    else {
      this.stepControl1 = control1;
    }
    if((control2 < 0.0001D) || (control2 > 0.9999D)) {
      this.stepControl2 = 0.94D;
    }
    else {
      this.stepControl2 = control2;
    }
    if((control3 < 0.0001D) || (control3 > 0.9999D)) {
      this.stepControl3 = 0.02D;
    }
    else {
      this.stepControl3 = control3;
    }
    if((control4 < 1.0001D) || (control4 > 999.9D)) {
      this.stepControl4 = 4.0D;
    }
    else {
      this.stepControl4 = control4;
    }
  }
  public void setInterpolationControl(final boolean useInterpolationErrorForControl, final int mudifControlParameter) {
    this.useInterpolationError = useInterpolationErrorForControl;
    if((mudifControlParameter <= 0) || (mudifControlParameter >= 7)) {
      this.mudif = 4;
    }
    else {
      this.mudif = mudifControlParameter;
    }
  }
  public void setOrderControl(final int maximalOrder, final double control1, final double control2) {
    if((maximalOrder <= 6) || (maximalOrder % 2 != 0)) {
      this.maxOrder = 18;
    }
    if((control1 < 0.0001D) || (control1 > 0.9999D)) {
      this.orderControl1 = 0.8D;
    }
    else {
      this.orderControl1 = control1;
    }
    if((control2 < 0.0001D) || (control2 > 0.9999D)) {
      this.orderControl2 = 0.9D;
    }
    else {
      this.orderControl2 = control2;
    }
    initializeArrays();
  }
  public void setStabilityCheck(final boolean performStabilityCheck, final int maxNumIter, final int maxNumChecks, final double stepsizeReductionFactor) {
    this.performTest = performStabilityCheck;
    this.maxIter = (maxNumIter <= 0) ? 2 : maxNumIter;
    this.maxChecks = (maxNumChecks <= 0) ? 1 : maxNumChecks;
    if((stepsizeReductionFactor < 0.0001D) || (stepsizeReductionFactor > 0.9999D)) {
      this.stabilityReduction = 0.5D;
    }
    else {
      this.stabilityReduction = stepsizeReductionFactor;
    }
  }
}