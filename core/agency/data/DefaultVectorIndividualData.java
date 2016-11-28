package agency.data;

import agency.Fitness;
import agency.Individual;
import agency.Population;
import agency.SimpleFitness;
import agency.vector.VectorIndividual;

import java.util.*;

/**
 * Created by liara on 10/1/16.
 */
public class DefaultVectorIndividualData extends DataOutput implements PopulationData {

@Override
public void writePopulationData(int generation, Population pop) {

  // Sanity checks
  if (pop == null)
    throw new RuntimeException("Population was null?!");
  if (pop.size() == 0)
    return;

  // Do a check only on the first individual
  Individual testIndividual = pop.allIndividuals().findFirst().get();
  if (!(testIndividual instanceof VectorIndividual))
    throw new RuntimeException("only VectorIndividuals supported");

  // Main loop
  pop.allIndividuals().forEach(i -> {
    VectorIndividual vi = (VectorIndividual) i;

    Data d = new Data();

    // Identification
    d.generation = generation;
    d.populationName = pop.getId();
    d.individual = i.getUUID().toString();

    // Parents
    List<UUID> parents = i.getParentIDs();
    if (parents == null) {
      // Do nothing;
    } else if (parents.size() >= 1)
      d.parent1 = parents.get(0).toString();
    else if (parents.size() >= 2)
      d.parent2 = parents.get(1).toString();

    // Fitness (only supports SimpleFitness)
    Fitness f = i.getFitness();
    if (f instanceof SimpleFitness) {
      d.fitness = ((SimpleFitness) f).getAverageFitness();
    }

    // Genome
    d.genome = Arrays.asList(vi.getGenome());
    write(d);
  });

}

private static class Data implements AgencyData {
  Integer      generation;
  String       populationName;
  String       individual;
  String       parent1;
  String       parent2;
  Double       fitness;
  List<Object> genome;

  @Override
  public List<String> getHeaders() {
    List<String> headers = new ArrayList<>();
    headers.add("generation");
    headers.add("populationName");
    headers.add("individual");
    headers.add("parent1");
    headers.add("parent2");
    headers.add("fitness");
    for (int i = 0; i < genome.size(); i++)
      headers.add("loc_" + i);
    return headers;
  }

  @Override
  public List<Object> getValues() {
    List<Object> values = new ArrayList<>();
    values.add(generation);
    values.add(populationName);
    values.add(individual);
    values.add(parent1);
    values.add(parent2);
    values.add(fitness);
    for (int i = 0; i < genome.size(); i++)
      values.add(genome.get(i));
    return values;
  }
}

}
