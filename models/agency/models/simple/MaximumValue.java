package agency.models.simple;

import java.util.stream.IntStream;
import agency.Agent;
import agency.AgentModel;
import agency.Fitness;
import agency.Individual;
import agency.SimpleFitness;
import agency.vector.VectorIndividual;

public class MaximumValue implements AgentModel {

  @Override
  public void run() {
    // NO SIMULATION NECESSARY
  }

  @Override
  public Fitness getFitness(Agent<?> agent) {
    VectorIndividual<Integer> ind = (VectorIndividual<Integer>) agent
        .getIndividual();
    int genomeSize = ind.getGenomeLength();
    double sumOfGenome = IntStream.range(0, genomeSize).mapToDouble((i) -> {
      return (double) ind.get(i);
    }).sum();

    Fitness fit = new SimpleFitness(sumOfGenome);
    return fit;
  }

  @Override
  public void addAgent(Agent<? extends Individual> agent) {
    // Don't need to add them, can evaulate directly.
  }

}
