package org.apache.commons.math3.analysis.solvers;
import org.apache.commons.math3.complex.Complex;
import org.apache.commons.math3.complex.ComplexUtils;
import org.apache.commons.math3.analysis.polynomials.PolynomialFunction;
import org.apache.commons.math3.exception.NoBracketingException;
import org.apache.commons.math3.exception.NullArgumentException;
import org.apache.commons.math3.exception.NoDataException;
import org.apache.commons.math3.exception.TooManyEvaluationsException;
import org.apache.commons.math3.exception.NumberIsTooLargeException;
import org.apache.commons.math3.exception.util.LocalizedFormats;
import org.apache.commons.math3.util.FastMath;

public class LaguerreSolver extends AbstractPolynomialSolver  {
  final private static double DEFAULT_ABSOLUTE_ACCURACY = 1e-6D;
  final private ComplexSolver complexSolver = new ComplexSolver();
  public LaguerreSolver() {
    this(DEFAULT_ABSOLUTE_ACCURACY);
  }
  public LaguerreSolver(double absoluteAccuracy) {
    super(absoluteAccuracy);
  }
  public LaguerreSolver(double relativeAccuracy, double absoluteAccuracy) {
    super(relativeAccuracy, absoluteAccuracy);
  }
  public LaguerreSolver(double relativeAccuracy, double absoluteAccuracy, double functionValueAccuracy) {
    super(relativeAccuracy, absoluteAccuracy, functionValueAccuracy);
  }
  public Complex solveComplex(double[] coefficients, double initial) throws NullArgumentException, NoDataException, TooManyEvaluationsException {
    setup(Integer.MAX_VALUE, new PolynomialFunction(coefficients), Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, initial);
    return complexSolver.solve(ComplexUtils.convertToComplex(coefficients), new Complex(initial, 0D));
  }
  public Complex[] solveAllComplex(double[] coefficients, double initial) throws NullArgumentException, NoDataException, TooManyEvaluationsException {
    setup(Integer.MAX_VALUE, new PolynomialFunction(coefficients), Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, initial);
    return complexSolver.solveAll(ComplexUtils.convertToComplex(coefficients), new Complex(initial, 0D));
  }
  @Override() public double doSolve() throws TooManyEvaluationsException, NumberIsTooLargeException, NoBracketingException {
    final double min = getMin();
    final double max = getMax();
    final double initial = getStartValue();
    final double functionValueAccuracy = getFunctionValueAccuracy();
    verifySequence(min, initial, max);
    final double yInitial = computeObjectiveValue(initial);
    if(FastMath.abs(yInitial) <= functionValueAccuracy) {
      return initial;
    }
    final double yMin = computeObjectiveValue(min);
    if(FastMath.abs(yMin) <= functionValueAccuracy) {
      return min;
    }
    if(yInitial * yMin < 0) {
      return laguerre(min, initial, yMin, yInitial);
    }
    final double yMax = computeObjectiveValue(max);
    if(FastMath.abs(yMax) <= functionValueAccuracy) {
      return max;
    }
    if(yInitial * yMax < 0) {
      return laguerre(initial, max, yInitial, yMax);
    }
    throw new NoBracketingException(min, max, yMin, yMax);
  }
  @Deprecated() public double laguerre(double lo, double hi, double fLo, double fHi) {
    final Complex[] c = ComplexUtils.convertToComplex(getCoefficients());
    final Complex initial = new Complex(0.5D * (lo + hi), 0);
    final Complex z = complexSolver.solve(c, initial);
    if(complexSolver.isRoot(lo, hi, z)) {
      return z.getReal();
    }
    else {
      double r = Double.NaN;
      Complex[] root = complexSolver.solveAll(c, initial);
      for(int i = 0; i < root.length; i++) {
        if(complexSolver.isRoot(lo, hi, root[i])) {
          r = root[i].getReal();
          break ;
        }
      }
      return r;
    }
  }
  
  private class ComplexSolver  {
    public Complex solve(Complex[] coefficients, Complex initial) throws NullArgumentException, NoDataException, TooManyEvaluationsException {
      if(coefficients == null) {
        throw new NullArgumentException();
      }
      final int n = coefficients.length - 1;
      if(n == 0) {
        throw new NoDataException(LocalizedFormats.POLYNOMIAL);
      }
      final double absoluteAccuracy = getAbsoluteAccuracy();
      final double relativeAccuracy = getRelativeAccuracy();
      final double functionValueAccuracy = getFunctionValueAccuracy();
      final Complex nC = new Complex(n, 0);
      final Complex n1C = new Complex(n - 1, 0);
      Complex z = initial;
      Complex oldz = new Complex(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY);
      while(true){
        Complex pv = coefficients[n];
        Complex dv = Complex.ZERO;
        Complex d2v = Complex.ZERO;
        for(int j = n - 1; j >= 0; j--) {
          d2v = dv.add(z.multiply(d2v));
          dv = pv.add(z.multiply(dv));
          pv = coefficients[j].add(z.multiply(pv));
        }
        d2v = d2v.multiply(new Complex(2.0D, 0.0D));
        final double tolerance = FastMath.max(relativeAccuracy * z.abs(), absoluteAccuracy);
        if((z.subtract(oldz)).abs() <= tolerance) {
          return z;
        }
        if(pv.abs() <= functionValueAccuracy) {
          return z;
        }
        final Complex G = dv.divide(pv);
        final Complex G2 = G.multiply(G);
        final Complex H = G2.subtract(d2v.divide(pv));
        final Complex delta = n1C.multiply((nC.multiply(H)).subtract(G2));
        final Complex deltaSqrt = delta.sqrt();
        final Complex dplus = G.add(deltaSqrt);
        final Complex dminus = G.subtract(deltaSqrt);
        final Complex denominator = dplus.abs() > dminus.abs() ? dplus : dminus;
        if(denominator.equals(new Complex(0.0D, 0.0D))) {
          z = z.add(new Complex(absoluteAccuracy, absoluteAccuracy));
          oldz = new Complex(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY);
        }
        else {
          oldz = z;
          z = z.subtract(nC.divide(denominator));
        }
        incrementEvaluationCount();
      }
    }
    public Complex[] solveAll(Complex[] coefficients, Complex initial) throws NullArgumentException, NoDataException, TooManyEvaluationsException {
      if(coefficients == null) {
        throw new NullArgumentException();
      }
      final int n = coefficients.length - 1;
      if(n == 0) {
        throw new NoDataException(LocalizedFormats.POLYNOMIAL);
      }
      final Complex[] c = new Complex[n + 1];
      for(int i = 0; i <= n; i++) {
        c[i] = coefficients[i];
      }
      final Complex[] root = new Complex[n];
      for(int i = 0; i < n; i++) {
        final Complex[] subarray = new Complex[n - i + 1];
        System.arraycopy(c, 0, subarray, 0, subarray.length);
        root[i] = solve(subarray, initial);
        Complex newc = c[n - i];
        Complex oldc = null;
        for(int j = n - i - 1; j >= 0; j--) {
          oldc = c[j];
          c[j] = newc;
          Complex var_499 = root[i];
          newc = oldc.add(newc.multiply(var_499));
        }
      }
      return root;
    }
    public boolean isRoot(double min, double max, Complex z) {
      if(isSequence(min, z.getReal(), max)) {
        double tolerance = FastMath.max(getRelativeAccuracy() * z.abs(), getAbsoluteAccuracy());
        return (FastMath.abs(z.getImaginary()) <= tolerance) || (z.abs() <= getFunctionValueAccuracy());
      }
      return false;
    }
  }
}