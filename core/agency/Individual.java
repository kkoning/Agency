package agency;

import java.io.Serializable;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.apache.commons.lang3.SerializationUtils;
import agency.eval.FitnessAggregator;

/**
 * All individuals in Agency must implement this interface.  It is recommended
 * that they also inherit AbstractIndividual where possible.  If not, beware that
 * fitness samples may be added in parallel.
 * <p>
 * Note that identity for individuals is must take into account the fact that
 * Individuals may be evaluated out-of-process, and object identity must
 * still be maintained.  Because there are cases where two separate individuals
 * may have the same genome/data, the equality of that data is insufficient to
 * uniquely identify them.  Therefore a UUID is used for this purpose.
 *
 * @author kkoning
 */
public interface Individual extends Serializable, Cloneable {

UUID getUUID();

void setUUID(UUID uuid);

/**
 * An individual may have a variable number of parents, typically from 0 to 2.
 * Those which are
 * generated, e.g., at the start of a simulation to create the initial
 * population, may have no parents at all.  Individuals that have been
 * through mutation only may have only one parent, and individuals
 * that have been subject to crossover may have two parents.
 *
 * @return a list of this individual's parents.
 */
List<UUID> getParentIDs();

/**
 * The complete set of parents should be known at the time of creation, so
 * this method is provided rather than an addParent() method...
 *
 * @param parentIDs
 */
void setParentIDs(List<UUID> parentIDs);

/**
 * While the observed fitness of an individual may vary based on the other
 * individuals present in each AgentModel, evolutionary processes require
 * that the individual have an overall fitness so that it can be compared
 * against any other individual in its PopulationGroup.  This will typically
 * be a SimpleFitness, but may be something more complex, e.g. for multi-objective
 * maximization.
 *
 * @return this individual's fitness.
 */
Fitness getFitness();

/** @param fitness */
void setFitness(Fitness fitness);

/**
 * Each time an individual participates in an AgentModel, a different fitness
 * value may be observed.  The true (risk-neutral) fitness would be an average
 * over all probable combinations of agents, but this is not practical to
 * calculate.  However, it may be appropriate to have more than one sample of
 * this fitness.  Observed fitnesses added here will later be aggregated by
 * aggregateFitness();
 *
 * @param fitness
 */
void addFitnessSample(Fitness fitness);

/**
 * aggregateFitness is called once each generation.  The default implementation
 * uses a specified FitnessAggregator (specified by the configuration for the
 * Population) to produce a single fitness.
 *
 * @param fitnessAggregator
 */
default void aggregateFitness(FitnessAggregator fitnessAggregator) {
  List<Fitness> fitnessSamples = getFitnessSamples();
  Fitness aggregatedFitness = fitnessAggregator.reduce(fitnessSamples.stream());
  setFitness(aggregatedFitness);
  clearFitnessSamples();
}

/**
 * A list of fitness samples is used by the default implementation of
 * aggregateFitness().
 *
 * @return a list of all fitness samples since the last time clearFitnessSamples() was called.
 */
List<Fitness> getFitnessSamples();

/**
 * Reset the list of fitness samples.  Called between generations.
 */
void clearFitnessSamples();

}
