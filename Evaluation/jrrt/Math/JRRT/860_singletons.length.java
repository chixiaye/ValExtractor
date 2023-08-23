package org.apache.commons.math3.distribution;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.math3.exception.DimensionMismatchException;
import org.apache.commons.math3.exception.MathArithmeticException;
import org.apache.commons.math3.exception.NotANumberException;
import org.apache.commons.math3.exception.NotFiniteNumberException;
import org.apache.commons.math3.exception.NotPositiveException;
import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.math3.random.Well19937c;
import org.apache.commons.math3.util.Pair;

public class EnumeratedIntegerDistribution extends AbstractIntegerDistribution  {
  final private static long serialVersionUID = 20130308L;
  final protected EnumeratedDistribution<Integer> innerDistribution;
  public EnumeratedIntegerDistribution(final RandomGenerator rng, final int[] singletons, final double[] probabilities) throws DimensionMismatchException, NotPositiveException, MathArithmeticException, NotFiniteNumberException, NotANumberException {
    super(rng);
    if(singletons.length != probabilities.length) {
      int var_860 = singletons.length;
      throw new DimensionMismatchException(probabilities.length, var_860);
    }
    final List<Pair<Integer, Double>> samples = new ArrayList<Pair<Integer, Double>>(singletons.length);
    for(int i = 0; i < singletons.length; i++) {
      samples.add(new Pair<Integer, Double>(singletons[i], probabilities[i]));
    }
    innerDistribution = new EnumeratedDistribution<Integer>(rng, samples);
  }
  public EnumeratedIntegerDistribution(final int[] singletons, final double[] probabilities) throws DimensionMismatchException, NotPositiveException, MathArithmeticException, NotFiniteNumberException, NotANumberException {
    this(new Well19937c(), singletons, probabilities);
  }
  public boolean isSupportConnected() {
    return true;
  }
  public double cumulativeProbability(final int x) {
    double probability = 0;
    for (final Pair<Integer, Double> sample : innerDistribution.getPmf()) {
      if(sample.getKey() <= x) {
        probability += sample.getValue();
      }
    }
    return probability;
  }
  public double getNumericalMean() {
    double mean = 0;
    for (final Pair<Integer, Double> sample : innerDistribution.getPmf()) {
      mean += sample.getValue() * sample.getKey();
    }
    return mean;
  }
  public double getNumericalVariance() {
    double mean = 0;
    double meanOfSquares = 0;
    for (final Pair<Integer, Double> sample : innerDistribution.getPmf()) {
      mean += sample.getValue() * sample.getKey();
      meanOfSquares += sample.getValue() * sample.getKey() * sample.getKey();
    }
    return meanOfSquares - mean * mean;
  }
  public double probability(final int x) {
    return innerDistribution.probability(x);
  }
  public int getSupportLowerBound() {
    int min = Integer.MAX_VALUE;
    for (final Pair<Integer, Double> sample : innerDistribution.getPmf()) {
      if(sample.getKey() < min && sample.getValue() > 0) {
        min = sample.getKey();
      }
    }
    return min;
  }
  public int getSupportUpperBound() {
    int max = Integer.MIN_VALUE;
    for (final Pair<Integer, Double> sample : innerDistribution.getPmf()) {
      if(sample.getKey() > max && sample.getValue() > 0) {
        max = sample.getKey();
      }
    }
    return max;
  }
  @Override() public int sample() {
    return innerDistribution.sample();
  }
}