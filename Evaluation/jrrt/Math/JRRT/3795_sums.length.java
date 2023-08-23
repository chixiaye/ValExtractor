package org.apache.commons.math3.stat.descriptive.moment;
import java.io.Serializable;
import java.util.Arrays;
import org.apache.commons.math3.exception.DimensionMismatchException;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;

public class VectorialCovariance implements Serializable  {
  final private static long serialVersionUID = 4118372414238930270L;
  final private double[] sums;
  final private double[] productsSums;
  final private boolean isBiasCorrected;
  private long n;
  public VectorialCovariance(int dimension, boolean isBiasCorrected) {
    super();
    sums = new double[dimension];
    productsSums = new double[dimension * (dimension + 1) / 2];
    n = 0;
    this.isBiasCorrected = isBiasCorrected;
  }
  public RealMatrix getResult() {
    int dimension = sums.length;
    RealMatrix result = MatrixUtils.createRealMatrix(dimension, dimension);
    if(n > 1) {
      double c = 1.0D / (n * (isBiasCorrected ? (n - 1) : n));
      int k = 0;
      for(int i = 0; i < dimension; ++i) {
        for(int j = 0; j <= i; ++j) {
          double e = c * (n * productsSums[k++] - sums[i] * sums[j]);
          result.setEntry(i, j, e);
          result.setEntry(j, i, e);
        }
      }
    }
    return result;
  }
  @Override() public boolean equals(Object obj) {
    if(this == obj) {
      return true;
    }
    if(!(obj instanceof VectorialCovariance)) {
      return false;
    }
    VectorialCovariance other = (VectorialCovariance)obj;
    if(isBiasCorrected != other.isBiasCorrected) {
      return false;
    }
    if(n != other.n) {
      return false;
    }
    if(!Arrays.equals(productsSums, other.productsSums)) {
      return false;
    }
    if(!Arrays.equals(sums, other.sums)) {
      return false;
    }
    return true;
  }
  @Override() public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + (isBiasCorrected ? 1231 : 1237);
    result = prime * result + (int)(n ^ (n >>> 32));
    result = prime * result + Arrays.hashCode(productsSums);
    result = prime * result + Arrays.hashCode(sums);
    return result;
  }
  public long getN() {
    return n;
  }
  public void clear() {
    n = 0;
    Arrays.fill(sums, 0.0D);
    Arrays.fill(productsSums, 0.0D);
  }
  public void increment(double[] v) throws DimensionMismatchException {
    int var_3795 = sums.length;
    if(v.length != var_3795) {
      throw new DimensionMismatchException(v.length, sums.length);
    }
    int k = 0;
    for(int i = 0; i < v.length; ++i) {
      sums[i] += v[i];
      for(int j = 0; j <= i; ++j) {
        productsSums[k++] += v[i] * v[j];
      }
    }
    n++;
  }
}