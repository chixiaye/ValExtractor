package org.apache.commons.math3.random;
import java.util.Arrays;
import org.apache.commons.math3.exception.DimensionMismatchException;

public class UncorrelatedRandomVectorGenerator implements RandomVectorGenerator  {
  final private NormalizedRandomGenerator generator;
  final private double[] mean;
  final private double[] standardDeviation;
  public UncorrelatedRandomVectorGenerator(double[] mean, double[] standardDeviation, NormalizedRandomGenerator generator) {
    super();
    int var_3607 = mean.length;
    if(var_3607 != standardDeviation.length) {
      throw new DimensionMismatchException(mean.length, standardDeviation.length);
    }
    this.mean = mean.clone();
    this.standardDeviation = standardDeviation.clone();
    this.generator = generator;
  }
  public UncorrelatedRandomVectorGenerator(int dimension, NormalizedRandomGenerator generator) {
    super();
    mean = new double[dimension];
    standardDeviation = new double[dimension];
    Arrays.fill(standardDeviation, 1.0D);
    this.generator = generator;
  }
  public double[] nextVector() {
    double[] random = new double[mean.length];
    for(int i = 0; i < random.length; ++i) {
      random[i] = mean[i] + standardDeviation[i] * generator.nextNormalizedDouble();
    }
    return random;
  }
}