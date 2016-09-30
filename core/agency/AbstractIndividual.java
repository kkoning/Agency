package agency;

import java.util.*;

import agency.eval.FitnessAggregator;

/**
 * A base class for different types of individuals.  It is recommended
 * that new classes of individuals inherit from AbstractIndividual (or at least
 * use encapsulation)
 */
public abstract class AbstractIndividual implements Individual {
private static final long serialVersionUID        = 1451854714980257824L;
private static final int  estimatedFitnessSamples = 5;


List<UUID> parentIDs;
Fitness    fitness;
List<Fitness> fitnessSamples = Collections.synchronizedList(new ArrayList<>(estimatedFitnessSamples));
private UUID uuid = UUID.randomUUID();

@Override
public UUID getUUID() {
  return uuid;
}

@Override
public void setUUID(UUID uuid) {
  this.uuid = uuid;
}

@Override
public List<UUID> getParentIDs() {
  return parentIDs;
}

@Override
public void setParentIDs(List<UUID> parentIDs) {
  this.parentIDs = parentIDs;
}

@Override
public Fitness getFitness() {
  return fitness;
}

public void setFitness(Fitness fitness) {
  this.fitness = fitness;
}

@Override
public void addFitnessSample(Fitness fitness) {
  fitnessSamples.add(fitness);
}

@Override
public List<Fitness> getFitnessSamples() {
  return fitnessSamples;
}

@Override
public void clearFitnessSamples() {
  fitnessSamples.clear();
}

}
