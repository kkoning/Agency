package agency;

import java.util.*;

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

}
