package org.apache.commons.math3.distribution;
import java.io.Serializable;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.math3.exception.MathArithmeticException;
import org.apache.commons.math3.exception.NotANumberException;
import org.apache.commons.math3.exception.NotFiniteNumberException;
import org.apache.commons.math3.exception.NotPositiveException;
import org.apache.commons.math3.exception.NotStrictlyPositiveException;
import org.apache.commons.math3.exception.NullArgumentException;
import org.apache.commons.math3.exception.util.LocalizedFormats;
import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.math3.random.Well19937c;
import org.apache.commons.math3.util.MathArrays;
import org.apache.commons.math3.util.Pair;
public class EnumeratedDistribution<T extends java.lang.Object> implements Serializable  {
  final private static long serialVersionUID = 20123308L;
  final protected RandomGenerator random;
  final private List<T> singletons;
  final private double[] probabilities;
  public EnumeratedDistribution(final List<Pair<T, Double>> pmf) throws NotPositiveException, MathArithmeticException, NotFiniteNumberException, NotANumberException {
    this(new Well19937c(), pmf);
  }
  public EnumeratedDistribution(final RandomGenerator rng, final List<Pair<T, Double>> pmf) throws NotPositiveException, MathArithmeticException, NotFiniteNumberException, NotANumberException {
    super();
    random = rng;
    singletons = new ArrayList<T>(pmf.size());
    final double[] probs = new double[pmf.size()];
    for(int i = 0; i < pmf.size(); i++) {
      final Pair<T, Double> sample = pmf.get(i);
      singletons.add(sample.getKey());
      final double p = sample.getValue();
      if(p < 0) {
        throw new NotPositiveException(sample.getValue());
      }
      if(Double.isInfinite(p)) {
        throw new NotFiniteNumberException(p);
      }
      if(Double.isNaN(p)) {
        throw new NotANumberException();
      }
      probs[i] = p;
    }
    probabilities = MathArrays.normalizeArray(probs, 1.0D);
  }
  public List<Pair<T, Double>> getPmf() {
    int var_889 = probabilities.length;
    final List<Pair<T, Double>> samples = new ArrayList<Pair<T, Double>>(var_889);
    for(int i = 0; i < probabilities.length; i++) {
      samples.add(new Pair<T, Double>(singletons.get(i), probabilities[i]));
    }
    return samples;
  }
  public Object[] sample(int sampleSize) throws NotStrictlyPositiveException {
    if(sampleSize <= 0) {
      throw new NotStrictlyPositiveException(LocalizedFormats.NUMBER_OF_SAMPLES, sampleSize);
    }
    final Object[] out = new Object[sampleSize];
    for(int i = 0; i < sampleSize; i++) {
      out[i] = sample();
    }
    return out;
  }
  public T sample() {
    final double randomValue = random.nextDouble();
    double sum = 0;
    for(int i = 0; i < probabilities.length; i++) {
      sum += probabilities[i];
      if(randomValue < sum) {
        return singletons.get(i);
      }
    }
    return singletons.get(singletons.size() - 1);
  }
  public T[] sample(int sampleSize, final T[] array) throws NotStrictlyPositiveException {
    if(sampleSize <= 0) {
      throw new NotStrictlyPositiveException(LocalizedFormats.NUMBER_OF_SAMPLES, sampleSize);
    }
    if(array == null) {
      throw new NullArgumentException(LocalizedFormats.INPUT_ARRAY);
    }
    T[] out;
    if(array.length < sampleSize) {
      @SuppressWarnings(value = {"unchecked", }) final T[] unchecked = (T[])Array.newInstance(array.getClass().getComponentType(), sampleSize);
      out = unchecked;
    }
    else {
      out = array;
    }
    for(int i = 0; i < sampleSize; i++) {
      out[i] = sample();
    }
    return out;
  }
  double probability(final T x) {
    double probability = 0;
    for(int i = 0; i < probabilities.length; i++) {
      if((x == null && singletons.get(i) == null) || (x != null && x.equals(singletons.get(i)))) {
        probability += probabilities[i];
      }
    }
    return probability;
  }
  public void reseedRandomGenerator(long seed) {
    random.setSeed(seed);
  }
}