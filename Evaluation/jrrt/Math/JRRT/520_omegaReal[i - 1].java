package org.apache.commons.math3.complex;
import java.io.Serializable;
import org.apache.commons.math3.exception.MathIllegalArgumentException;
import org.apache.commons.math3.exception.MathIllegalStateException;
import org.apache.commons.math3.exception.OutOfRangeException;
import org.apache.commons.math3.exception.ZeroException;
import org.apache.commons.math3.exception.util.LocalizedFormats;
import org.apache.commons.math3.util.FastMath;

public class RootsOfUnity implements Serializable  {
  final private static long serialVersionUID = 20120201L;
  private int omegaCount;
  private double[] omegaReal;
  private double[] omegaImaginaryCounterClockwise;
  private double[] omegaImaginaryClockwise;
  private boolean isCounterClockWise;
  public RootsOfUnity() {
    super();
    omegaCount = 0;
    omegaReal = null;
    omegaImaginaryCounterClockwise = null;
    omegaImaginaryClockwise = null;
    isCounterClockWise = true;
  }
  public synchronized boolean isCounterClockWise() throws MathIllegalStateException {
    if(omegaCount == 0) {
      throw new MathIllegalStateException(LocalizedFormats.ROOTS_OF_UNITY_NOT_COMPUTED_YET);
    }
    return isCounterClockWise;
  }
  public synchronized double getImaginary(int k) throws MathIllegalStateException, OutOfRangeException {
    if(omegaCount == 0) {
      throw new MathIllegalStateException(LocalizedFormats.ROOTS_OF_UNITY_NOT_COMPUTED_YET);
    }
    if((k < 0) || (k >= omegaCount)) {
      throw new OutOfRangeException(LocalizedFormats.OUT_OF_RANGE_ROOT_OF_UNITY_INDEX, Integer.valueOf(k), Integer.valueOf(0), Integer.valueOf(omegaCount - 1));
    }
    return isCounterClockWise ? omegaImaginaryCounterClockwise[k] : omegaImaginaryClockwise[k];
  }
  public synchronized double getReal(int k) throws MathIllegalStateException, MathIllegalArgumentException {
    if(omegaCount == 0) {
      throw new MathIllegalStateException(LocalizedFormats.ROOTS_OF_UNITY_NOT_COMPUTED_YET);
    }
    if((k < 0) || (k >= omegaCount)) {
      throw new OutOfRangeException(LocalizedFormats.OUT_OF_RANGE_ROOT_OF_UNITY_INDEX, Integer.valueOf(k), Integer.valueOf(0), Integer.valueOf(omegaCount - 1));
    }
    return omegaReal[k];
  }
  public synchronized int getNumberOfRoots() {
    return omegaCount;
  }
  public synchronized void computeRoots(int n) throws ZeroException {
    if(n == 0) {
      throw new ZeroException(LocalizedFormats.CANNOT_COMPUTE_0TH_ROOT_OF_UNITY);
    }
    isCounterClockWise = n > 0;
    final int absN = FastMath.abs(n);
    if(absN == omegaCount) {
      return ;
    }
    final double t = 2.0D * FastMath.PI / absN;
    final double cosT = FastMath.cos(t);
    final double sinT = FastMath.sin(t);
    omegaReal = new double[absN];
    omegaImaginaryCounterClockwise = new double[absN];
    omegaImaginaryClockwise = new double[absN];
    omegaReal[0] = 1.0D;
    omegaImaginaryCounterClockwise[0] = 0.0D;
    omegaImaginaryClockwise[0] = 0.0D;
    for(int i = 1; i < absN; i++) {
      double var_520 = omegaReal[i - 1];
      omegaReal[i] = var_520 * cosT - omegaImaginaryCounterClockwise[i - 1] * sinT;
      omegaImaginaryCounterClockwise[i] = omegaReal[i - 1] * sinT + omegaImaginaryCounterClockwise[i - 1] * cosT;
      omegaImaginaryClockwise[i] = -omegaImaginaryCounterClockwise[i];
    }
    omegaCount = absN;
  }
}