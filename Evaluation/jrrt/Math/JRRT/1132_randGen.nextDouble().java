package org.apache.commons.math3.genetics;
import org.apache.commons.math3.exception.OutOfRangeException;
import org.apache.commons.math3.exception.util.LocalizedFormats;
import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.math3.random.JDKRandomGenerator;

public class GeneticAlgorithm  {
  private static RandomGenerator randomGenerator = new JDKRandomGenerator();
  final private CrossoverPolicy crossoverPolicy;
  final private double crossoverRate;
  final private MutationPolicy mutationPolicy;
  final private double mutationRate;
  final private SelectionPolicy selectionPolicy;
  private int generationsEvolved = 0;
  public GeneticAlgorithm(final CrossoverPolicy crossoverPolicy, final double crossoverRate, final MutationPolicy mutationPolicy, final double mutationRate, final SelectionPolicy selectionPolicy) throws OutOfRangeException {
    super();
    if(crossoverRate < 0 || crossoverRate > 1) {
      throw new OutOfRangeException(LocalizedFormats.CROSSOVER_RATE, crossoverRate, 0, 1);
    }
    if(mutationRate < 0 || mutationRate > 1) {
      throw new OutOfRangeException(LocalizedFormats.MUTATION_RATE, mutationRate, 0, 1);
    }
    this.crossoverPolicy = crossoverPolicy;
    this.crossoverRate = crossoverRate;
    this.mutationPolicy = mutationPolicy;
    this.mutationRate = mutationRate;
    this.selectionPolicy = selectionPolicy;
  }
  public CrossoverPolicy getCrossoverPolicy() {
    return crossoverPolicy;
  }
  public MutationPolicy getMutationPolicy() {
    return mutationPolicy;
  }
  public Population evolve(final Population initial, final StoppingCondition condition) {
    Population current = initial;
    generationsEvolved = 0;
    while(!condition.isSatisfied(current)){
      current = nextGeneration(current);
      generationsEvolved++;
    }
    return current;
  }
  public Population nextGeneration(final Population current) {
    Population nextGeneration = current.nextGeneration();
    RandomGenerator randGen = getRandomGenerator();
    while(nextGeneration.getPopulationSize() < nextGeneration.getPopulationLimit()){
      ChromosomePair pair = getSelectionPolicy().select(current);
      if(randGen.nextDouble() < getCrossoverRate()) {
        pair = getCrossoverPolicy().crossover(pair.getFirst(), pair.getSecond());
      }
      double var_1132 = randGen.nextDouble();
      if(var_1132 < getMutationRate()) {
        pair = new ChromosomePair(getMutationPolicy().mutate(pair.getFirst()), getMutationPolicy().mutate(pair.getSecond()));
      }
      nextGeneration.addChromosome(pair.getFirst());
      if(nextGeneration.getPopulationSize() < nextGeneration.getPopulationLimit()) {
        nextGeneration.addChromosome(pair.getSecond());
      }
    }
    return nextGeneration;
  }
  public static synchronized RandomGenerator getRandomGenerator() {
    return randomGenerator;
  }
  public SelectionPolicy getSelectionPolicy() {
    return selectionPolicy;
  }
  public double getCrossoverRate() {
    return crossoverRate;
  }
  public double getMutationRate() {
    return mutationRate;
  }
  public int getGenerationsEvolved() {
    return generationsEvolved;
  }
  public static synchronized void setRandomGenerator(final RandomGenerator random) {
    randomGenerator = random;
  }
}