package org.apache.commons.math3.genetics;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.math3.exception.MathIllegalArgumentException;
import org.apache.commons.math3.exception.util.LocalizedFormats;

public class TournamentSelection implements SelectionPolicy  {
  private int arity;
  public TournamentSelection(final int arity) {
    super();
    this.arity = arity;
  }
  private Chromosome tournament(final ListPopulation population) throws MathIllegalArgumentException {
    int var_1089 = population.getPopulationSize();
    if(var_1089 < this.arity) {
      throw new MathIllegalArgumentException(LocalizedFormats.TOO_LARGE_TOURNAMENT_ARITY, arity, population.getPopulationSize());
    }
    ListPopulation tournamentPopulation = new ListPopulation(this.arity) {
        public Population nextGeneration() {
          return null;
        }
    };
    List<Chromosome> chromosomes = new ArrayList<Chromosome>(population.getChromosomes());
    for(int i = 0; i < this.arity; i++) {
      int rind = GeneticAlgorithm.getRandomGenerator().nextInt(chromosomes.size());
      tournamentPopulation.addChromosome(chromosomes.get(rind));
      chromosomes.remove(rind);
    }
    return tournamentPopulation.getFittestChromosome();
  }
  public ChromosomePair select(final Population population) throws MathIllegalArgumentException {
    return new ChromosomePair(tournament((ListPopulation)population), tournament((ListPopulation)population));
  }
  public int getArity() {
    return arity;
  }
  public void setArity(final int arity) {
    this.arity = arity;
  }
}