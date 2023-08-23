package org.apache.commons.math3.distribution;
import java.util.List;
import java.util.ArrayList;
import org.apache.commons.math3.exception.DimensionMismatchException;
import org.apache.commons.math3.exception.NotPositiveException;
import org.apache.commons.math3.exception.MathArithmeticException;
import org.apache.commons.math3.exception.util.LocalizedFormats;
import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.math3.random.Well19937c;
import org.apache.commons.math3.util.Pair;
public class MixtureMultivariateRealDistribution<T extends org.apache.commons.math3.distribution.MultivariateRealDistribution> extends AbstractMultivariateRealDistribution  {
  final private double[] weight;
  final private List<T> distribution;
  public MixtureMultivariateRealDistribution(List<Pair<Double, T>> components) {
    this(new Well19937c(), components);
  }
  public MixtureMultivariateRealDistribution(RandomGenerator rng, List<Pair<Double, T>> components) {
    super(rng, components.get(0).getSecond().getDimension());
    final int numComp = components.size();
    final int dim = getDimension();
    double weightSum = 0;
    for(int i = 0; i < numComp; i++) {
      final Pair<Double, T> comp = components.get(i);
      int var_906 = comp.getSecond().getDimension();
      if(var_906 != dim) {
        throw new DimensionMismatchException(comp.getSecond().getDimension(), dim);
      }
      if(comp.getFirst() < 0) {
        throw new NotPositiveException(comp.getFirst());
      }
      weightSum += comp.getFirst();
    }
    if(Double.isInfinite(weightSum)) {
      throw new MathArithmeticException(LocalizedFormats.OVERFLOW);
    }
    distribution = new ArrayList<T>();
    weight = new double[numComp];
    for(int i = 0; i < numComp; i++) {
      final Pair<Double, T> comp = components.get(i);
      weight[i] = comp.getFirst() / weightSum;
      distribution.add(comp.getSecond());
    }
  }
  public List<Pair<Double, T>> getComponents() {
    final List<Pair<Double, T>> list = new ArrayList<Pair<Double, T>>(weight.length);
    for(int i = 0; i < weight.length; i++) {
      list.add(new Pair<Double, T>(weight[i], distribution.get(i)));
    }
    return list;
  }
  public double density(final double[] values) {
    double p = 0;
    for(int i = 0; i < weight.length; i++) {
      p += weight[i] * distribution.get(i).density(values);
    }
    return p;
  }
  @Override() public double[] sample() {
    double[] vals = null;
    final double randomValue = random.nextDouble();
    double sum = 0;
    for(int i = 0; i < weight.length; i++) {
      sum += weight[i];
      if(randomValue <= sum) {
        vals = distribution.get(i).sample();
        break ;
      }
    }
    if(vals == null) {
      vals = distribution.get(weight.length - 1).sample();
    }
    return vals;
  }
  @Override() public void reseedRandomGenerator(long seed) {
    super.reseedRandomGenerator(seed);
    for(int i = 0; i < distribution.size(); i++) {
      distribution.get(i).reseedRandomGenerator(i + 1 + seed);
    }
  }
}