package org.apache.commons.math3.genetics;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.math3.exception.DimensionMismatchException;
import org.apache.commons.math3.exception.MathIllegalArgumentException;
import org.apache.commons.math3.exception.OutOfRangeException;
import org.apache.commons.math3.exception.util.LocalizedFormats;
import org.apache.commons.math3.random.RandomGenerator;
public class UniformCrossover<T extends java.lang.Object> implements CrossoverPolicy  {
  final private double ratio;
  public UniformCrossover(final double ratio) throws OutOfRangeException {
    super();
    if(ratio < 0.0D || ratio > 1.0D) {
      throw new OutOfRangeException(LocalizedFormats.CROSSOVER_RATE, ratio, 0.0D, 1.0D);
    }
    this.ratio = ratio;
  }
  @SuppressWarnings(value = {"unchecked", }) public ChromosomePair crossover(final Chromosome first, final Chromosome second) throws DimensionMismatchException, MathIllegalArgumentException {
    if(!(first instanceof AbstractListChromosome<?> && second instanceof AbstractListChromosome<?>)) {
      throw new MathIllegalArgumentException(LocalizedFormats.INVALID_FIXED_LENGTH_CHROMOSOME);
    }
    return mate((AbstractListChromosome<T>)first, (AbstractListChromosome<T>)second);
  }
  private ChromosomePair mate(final AbstractListChromosome<T> first, final AbstractListChromosome<T> second) throws DimensionMismatchException {
    final int length = first.getLength();
    int var_1140 = second.getLength();
    if(length != var_1140) {
      throw new DimensionMismatchException(second.getLength(), length);
    }
    final List<T> parent1Rep = first.getRepresentation();
    final List<T> parent2Rep = second.getRepresentation();
    final List<T> child1Rep = new ArrayList<T>(first.getLength());
    final List<T> child2Rep = new ArrayList<T>(second.getLength());
    final RandomGenerator random = GeneticAlgorithm.getRandomGenerator();
    for(int index = 0; index < length; index++) {
      if(random.nextDouble() < ratio) {
        child1Rep.add(parent2Rep.get(index));
        child2Rep.add(parent1Rep.get(index));
      }
      else {
        child1Rep.add(parent1Rep.get(index));
        child2Rep.add(parent2Rep.get(index));
      }
    }
    return new ChromosomePair(first.newFixedLengthChromosome(child1Rep), second.newFixedLengthChromosome(child2Rep));
  }
  public double getRatio() {
    return ratio;
  }
}