package org.apache.commons.math3.stat.descriptive.moment;
import java.io.Serializable;
import java.util.Arrays;
import org.apache.commons.math3.exception.DimensionMismatchException;

public class VectorialMean implements Serializable  {
  final private static long serialVersionUID = 8223009086481006892L;
  final private Mean[] means;
  public VectorialMean(int dimension) {
    super();
    means = new Mean[dimension];
    for(int i = 0; i < dimension; ++i) {
      means[i] = new Mean();
    }
  }
  @Override() public boolean equals(Object obj) {
    if(this == obj) {
      return true;
    }
    if(!(obj instanceof VectorialMean)) {
      return false;
    }
    VectorialMean other = (VectorialMean)obj;
    if(!Arrays.equals(means, other.means)) {
      return false;
    }
    return true;
  }
  public double[] getResult() {
    double[] result = new double[means.length];
    for(int i = 0; i < result.length; ++i) {
      result[i] = means[i].getResult();
    }
    return result;
  }
  @Override() public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + Arrays.hashCode(means);
    return result;
  }
  public long getN() {
    return (means.length == 0) ? 0 : means[0].getN();
  }
  public void increment(double[] v) throws DimensionMismatchException {
    if(v.length != means.length) {
      int var_3793 = v.length;
      throw new DimensionMismatchException(var_3793, means.length);
    }
    for(int i = 0; i < v.length; ++i) {
      means[i].increment(v[i]);
    }
  }
}