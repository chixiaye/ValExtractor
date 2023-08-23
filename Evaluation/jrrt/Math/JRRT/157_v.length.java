package org.apache.commons.math3.analysis.differentiation;
import java.io.Serializable;
import org.apache.commons.math3.analysis.UnivariateFunction;
import org.apache.commons.math3.analysis.UnivariateMatrixFunction;
import org.apache.commons.math3.analysis.UnivariateVectorFunction;
import org.apache.commons.math3.exception.MathIllegalArgumentException;
import org.apache.commons.math3.exception.NotPositiveException;
import org.apache.commons.math3.exception.NumberIsTooLargeException;
import org.apache.commons.math3.exception.NumberIsTooSmallException;
import org.apache.commons.math3.util.FastMath;

public class FiniteDifferencesDifferentiator implements UnivariateFunctionDifferentiator, UnivariateVectorFunctionDifferentiator, UnivariateMatrixFunctionDifferentiator, Serializable  {
  final private static long serialVersionUID = 20120917L;
  final private int nbPoints;
  final private double stepSize;
  final private double halfSampleSpan;
  final private double tMin;
  final private double tMax;
  public FiniteDifferencesDifferentiator(final int nbPoints, final double stepSize) throws NotPositiveException, NumberIsTooSmallException {
    this(nbPoints, stepSize, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);
  }
  public FiniteDifferencesDifferentiator(final int nbPoints, final double stepSize, final double tLower, final double tUpper) throws NotPositiveException, NumberIsTooSmallException, NumberIsTooLargeException {
    super();
    if(nbPoints <= 1) {
      throw new NumberIsTooSmallException(stepSize, 1, false);
    }
    this.nbPoints = nbPoints;
    if(stepSize <= 0) {
      throw new NotPositiveException(stepSize);
    }
    this.stepSize = stepSize;
    halfSampleSpan = 0.5D * stepSize * (nbPoints - 1);
    if(2 * halfSampleSpan >= tUpper - tLower) {
      throw new NumberIsTooLargeException(2 * halfSampleSpan, tUpper - tLower, false);
    }
    final double safety = FastMath.ulp(halfSampleSpan);
    this.tMin = tLower + halfSampleSpan + safety;
    this.tMax = tUpper - halfSampleSpan - safety;
  }
  private DerivativeStructure evaluate(final DerivativeStructure t, final double t0, final double[] y) throws NumberIsTooLargeException {
    final double[] top = new double[nbPoints];
    final double[] bottom = new double[nbPoints];
    for(int i = 0; i < nbPoints; ++i) {
      bottom[i] = y[i];
      for(int j = 1; j <= i; ++j) {
        bottom[i - j] = (bottom[i - j + 1] - bottom[i - j]) / (j * stepSize);
      }
      top[i] = bottom[0];
    }
    final int order = t.getOrder();
    final int parameters = t.getFreeParameters();
    final double[] derivatives = t.getAllDerivatives();
    final double dt0 = t.getValue() - t0;
    DerivativeStructure interpolation = new DerivativeStructure(parameters, order, 0.0D);
    DerivativeStructure monomial = null;
    for(int i = 0; i < nbPoints; ++i) {
      if(i == 0) {
        monomial = new DerivativeStructure(parameters, order, 1.0D);
      }
      else {
        derivatives[0] = dt0 - (i - 1) * stepSize;
        final DerivativeStructure deltaX = new DerivativeStructure(parameters, order, derivatives);
        monomial = monomial.multiply(deltaX);
      }
      interpolation = interpolation.add(monomial.multiply(top[i]));
    }
    return interpolation;
  }
  public UnivariateDifferentiableFunction differentiate(final UnivariateFunction function) {
    return new UnivariateDifferentiableFunction() {
        public double value(final double x) throws MathIllegalArgumentException {
          return function.value(x);
        }
        public DerivativeStructure value(final DerivativeStructure t) throws MathIllegalArgumentException {
          if(t.getOrder() >= nbPoints) {
            throw new NumberIsTooLargeException(t.getOrder(), nbPoints, false);
          }
          final double t0 = FastMath.max(FastMath.min(t.getValue(), tMax), tMin) - halfSampleSpan;
          final double[] y = new double[nbPoints];
          for(int i = 0; i < nbPoints; ++i) {
            y[i] = function.value(t0 + i * stepSize);
          }
          return evaluate(t, t0, y);
        }
    };
  }
  public UnivariateDifferentiableMatrixFunction differentiate(final UnivariateMatrixFunction function) {
    return new UnivariateDifferentiableMatrixFunction() {
        public double[][] value(final double x) throws MathIllegalArgumentException {
          return function.value(x);
        }
        public DerivativeStructure[][] value(final DerivativeStructure t) throws MathIllegalArgumentException {
          if(t.getOrder() >= nbPoints) {
            throw new NumberIsTooLargeException(t.getOrder(), nbPoints, false);
          }
          final double t0 = FastMath.max(FastMath.min(t.getValue(), tMax), tMin) - halfSampleSpan;
          double[][][] y = null;
          for(int i = 0; i < nbPoints; ++i) {
            final double[][] v = function.value(t0 + i * stepSize);
            if(i == 0) {
              int var_157 = v.length;
              y = new double[var_157][v[0].length][nbPoints];
            }
            for(int j = 0; j < v.length; ++j) {
              for(int k = 0; k < v[j].length; ++k) {
                y[j][k][i] = v[j][k];
              }
            }
          }
          final DerivativeStructure[][] value = new DerivativeStructure[y.length][y[0].length];
          for(int j = 0; j < value.length; ++j) {
            for(int k = 0; k < y[j].length; ++k) {
              value[j][k] = evaluate(t, t0, y[j][k]);
            }
          }
          return value;
        }
    };
  }
  public UnivariateDifferentiableVectorFunction differentiate(final UnivariateVectorFunction function) {
    return new UnivariateDifferentiableVectorFunction() {
        public double[] value(final double x) throws MathIllegalArgumentException {
          return function.value(x);
        }
        public DerivativeStructure[] value(final DerivativeStructure t) throws MathIllegalArgumentException {
          if(t.getOrder() >= nbPoints) {
            throw new NumberIsTooLargeException(t.getOrder(), nbPoints, false);
          }
          final double t0 = FastMath.max(FastMath.min(t.getValue(), tMax), tMin) - halfSampleSpan;
          double[][] y = null;
          for(int i = 0; i < nbPoints; ++i) {
            final double[] v = function.value(t0 + i * stepSize);
            if(i == 0) {
              y = new double[v.length][nbPoints];
            }
            for(int j = 0; j < v.length; ++j) {
              y[j][i] = v[j];
            }
          }
          final DerivativeStructure[] value = new DerivativeStructure[y.length];
          for(int j = 0; j < value.length; ++j) {
            value[j] = evaluate(t, t0, y[j]);
          }
          return value;
        }
    };
  }
  public double getStepSize() {
    return stepSize;
  }
  public int getNbPoints() {
    return nbPoints;
  }
}