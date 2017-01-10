package agency.data;

import agency.Fitness;
import agency.Individual;
import agency.Population;
import agency.SimpleFitness;
import agency.vector.VectorIndividual;
import org.w3c.dom.Element;

import java.io.File;
import java.util.*;

/**
 Created by liara on 10/1/16.
 */
public class DefaultVectorIndividualData extends DataOutput implements PopulationData {

Integer everyNGenerations;

transient DataOutput fitnessLandscapeWriter;

@Override
public void readXMLConfig(Element e) {
  super.readXMLConfig(e);
  String generationsString = e.getAttribute("generations");
  if (generationsString != null) {
    if (!generationsString.isEmpty()) {
      everyNGenerations = Integer.parseInt(generationsString);
    }
  }

}

@Override
public void writeXMLConfig(Element e) {
  super.writeXMLConfig(e);
  if (everyNGenerations != null) {
    e.setAttribute("generations", everyNGenerations.toString());
  }
}

@Override
public void writePopulationData(int generation, Population pop) {

  // Sanity checks
  if (pop == null)
    throw new RuntimeException("Population was null?!");
  if (pop.size() == 0)
    return;

  if (everyNGenerations != null) {
    if (!((generation % everyNGenerations) == 0)) {
      return;
    }
  }


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

@Override
public void fitnessLandscapeOpen(String outputFile) {
  fitnessLandscapeWriter = new DataOutput(outputFile);
}

@Override
public void fitnessLandscapeSample(int baseGeneration, int generationOffset,
                                   int sampleSet, Individual ind) {
  FitnessLandscapeData fld = new FitnessLandscapeData();
  fld.baseGeneration = baseGeneration;
  fld.generationOffset = generationOffset;
  fld.sampleSet = sampleSet;

  VectorIndividual<Object> vi;
  try {
    vi = (VectorIndividual<Object>) ind;
  } catch (ClassCastException cce) {
    // TODO better error message.
    throw new RuntimeException(cce);
  }
  fld.genome = Arrays.asList(vi.getGenome());
  SimpleFitness sf = (SimpleFitness) vi.getFitness();
  fld.fitness = sf.getAverageFitness();

  fitnessLandscapeWriter.write(fld);
}

@Override
public void fitnessLandscapeClose() {
  fitnessLandscapeWriter.close();
}

private static class FitnessLandscapeData implements AgencyData {
  int baseGeneration;
  int generationOffset;
  int sampleSet;
  double fitness;
  List<Object> genome;

  @Override
  public List<String> getHeaders() {
    List<String> headers = new ArrayList<>();
    headers.add("baseGeneration");
    headers.add("generationOffset");
    headers.add("sampleSet");
    headers.add("fitness");
    for (int i = 0; i < genome.size(); i++)
      headers.add("loc_" + i);
    return headers;
  }

  @Override
  public List<Object> getValues() {
    List<Object> values = new ArrayList<>();
    values.add(baseGeneration);
    values.add(generationOffset);
    values.add(sampleSet);
    values.add(fitness);
    for (int i = 0; i < genome.size(); i++)
      values.add(genome.get(i));
    return values;
  }
}

private static class Data implements AgencyData {
  Integer      generation;
  String       individual;
  String       parent1;
  String       parent2;
  Double       fitness;
  List<Object> genome;

  @Override
  public List<String> getHeaders() {
    List<String> headers = new ArrayList<>();
    headers.add("generation");
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
