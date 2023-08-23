package org.apache.commons.math3.genetics;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.apache.commons.math3.exception.DimensionMismatchException;
import org.apache.commons.math3.exception.MathIllegalArgumentException;
import org.apache.commons.math3.exception.util.LocalizedFormats;
public class CycleCrossover<T extends java.lang.Object> implements CrossoverPolicy  {
  final private boolean randomStart;
  public CycleCrossover() {
    this(false);
  }
  public CycleCrossover(final boolean randomStart) {
    super();
    this.randomStart = randomStart;
  }
  @SuppressWarnings(value = {"unchecked", }) public ChromosomePair crossover(final Chromosome first, final Chromosome second) throws DimensionMismatchException, MathIllegalArgumentException {
    if(!(first instanceof AbstractListChromosome<?> && second instanceof AbstractListChromosome<?>)) {
      throw new MathIllegalArgumentException(LocalizedFormats.INVALID_FIXED_LENGTH_CHROMOSOME);
    }
    return mate((AbstractListChromosome<T>)first, (AbstractListChromosome<T>)second);
  }
  protected ChromosomePair mate(final AbstractListChromosome<T> first, final AbstractListChromosome<T> second) throws DimensionMismatchException {
    final int length = first.getLength();
    if(length != second.getLength()) {
      throw new DimensionMismatchException(second.getLength(), length);
    }
    List<T> var_1102 = first.getRepresentation();
    final List<T> parent1Rep = var_1102;
    final List<T> parent2Rep = second.getRepresentation();
    final List<T> child1Rep = new ArrayList<T>(second.getRepresentation());
    final List<T> child2Rep = new ArrayList<T>(first.getRepresentation());
    final Set<Integer> visitedIndices = new HashSet<Integer>(length);
    final List<Integer> indices = new ArrayList<Integer>(length);
    int idx = randomStart ? GeneticAlgorithm.getRandomGenerator().nextInt(length) : 0;
    int cycle = 1;
    while(visitedIndices.size() < length){
      indices.add(idx);
      T item = parent2Rep.get(idx);
      idx = parent1Rep.indexOf(item);
      while(idx != indices.get(0)){
        indices.add(idx);
        item = parent2Rep.get(idx);
        idx = parent1Rep.indexOf(item);
      }
      if(cycle++ % 2 != 0) {
        for (int i : indices) {
          T tmp = child1Rep.get(i);
          child1Rep.set(i, child2Rep.get(i));
          child2Rep.set(i, tmp);
        }
      }
      visitedIndices.addAll(indices);
      idx = (indices.get(0) + 1) % length;
      while(visitedIndices.contains(idx) && visitedIndices.size() < length){
        idx++;
        if(idx >= length) {
          idx = 0;
        }
      }
      indices.clear();
    }
    return new ChromosomePair(first.newFixedLengthChromosome(child1Rep), second.newFixedLengthChromosome(child2Rep));
  }
  public boolean isRandomStart() {
    return randomStart;
  }
}