package agency.data;

import agency.*;
import agency.eval.EvaluationGroup;
import agency.vector.VectorIndividual;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

/**
 * Calculates a fitness landscape for a specified population.
 * <p>
 * The fitness landscape calculator is useful for understanding why a population is
 * evolving as it is.   This is a special case for VectorIndividuals whose elements
 * are entirely {@link java.lang.Double}s.  This lets us spread out the samples in a much more effective way.
 */
public class DoubleVectorFitnessLandscapeGenerator implements Runnable {

private static final Fitness zeroFitness = new SimpleFitness(0);

/**
 * Need a link to the environment.  Loaded from a checkpoint file by main().
 */
final Environment env;

/**
 * The populationGroupID, as specified in the run configuration file, of the
 * population we are generating the fitness landscape for.
 */
final String populationGroupID;

/**
 * The populationID, as specified in the run configuration file, of the
 * population we are generating the fitness landscape for.
 */
final String populationID;

/**
 * Where to write the results.
 */
final String outputFile;

// Tracking Variables
/**
 * The population we're calculating the fitness landscape for.
 */
final Population pop;

final         int            baseGeneration;
/**
 * Population size is an integer, and, particularly if it is intended to grow
 * by a small amount, direct manipulation may be unacceptably inaccurate.
 * Consider the worst case, where growth is <0.5 individuals per generation, in
 * which case growth would <i>never</i> occur.  Instead, population growth is
 * tracked here, and converted to an integer population size each generation.
 */

final         double         stdDevsOut;
final         int            numSamplesRequested;
/**
 * The PopulationData object is responsible for outputting the fitness
 * landscape data.  In the case where the population has more than one of these
 * (?) the first one is used.
 */
private final PopulationData writer;
int numSamples;

DoubleVectorIndividualGenerator dvig;

public DoubleVectorFitnessLandscapeGenerator(Environment env, String populationGroupID,
                                             String populationID, String outputFile,
                                             double stdDevsOut, int numSamplesRequested) {
  this.env = env;
  this.populationGroupID = populationGroupID;
  this.populationID = populationID;
  this.outputFile = outputFile;
  this.stdDevsOut = stdDevsOut;
  this.numSamplesRequested = numSamplesRequested;

  baseGeneration = env.getGeneration();
  numSamples = 1;

  PopulationGroup pg = env.getPopulationGroup(populationGroupID);
  pop = pg.getPopulation(populationID).get();
  writer = pop.getPopulationDataOutputs().get(0);

}


public static void main(String[] args) {
  // TODO: Currently in testing, implement command line parsing.

  File serializedFile = new File("checkpoint.gen-2000.ser");
  Environment env = Environment.readCheckpoint(serializedFile);

  DoubleVectorFitnessLandscapeGenerator flg = new DoubleVectorFitnessLandscapeGenerator(env,
          "nsp", "DirectlyEncodedNetworkOperators",
          "deno_landscape-200.csv", 200, 100000);

  flg.run();


}


public void run() {

  // Always just use the first one.
  if (writer == null)
    throw new RuntimeException("Cannot write fitness landscape data for a " +
            "population that does not have any PopulationData objects.");


  dvig = new DoubleVectorIndividualGenerator(pop,stdDevsOut);


  writer.fitnessLandscapeOpen(outputFile);

  while (numSamples < numSamplesRequested)
    step();

  writer.fitnessLandscapeClose();


}

/**
 * In general, follow the same pattern as Agency.evolve.
 */
void step() {

  /*
  We will need to do more fitness evaluations as the population size grows.
  TODO, there's no point in processing any EvaluationGroups that don't
  contain at least one member of the population we're interested in.
   */
  List<EvaluationGroup> unevaluated = new ArrayList<>();

  env.evaluationGroupFactory.createEvaluationGroups(env).forEach(eg -> unevaluated.add(eg));

  List<EvaluationGroup> evaluatedGroups = env.evaluator
          .evaluate(unevaluated.stream()).collect(Collectors.toList());

  Map<Individual, Fitness> aggFitnesses = evaluatedGroups.parallelStream()
          .map(eg -> eg.getResults()).collect(
                  Environment::newFitnessMap,
                  Environment::fitnessAccumulator,
                  Environment::fitnessAccumulator
          );

  // Assign fitness
  aggFitnesses.entrySet().parallelStream().forEach(
          entry -> entry.getKey().setFitness(entry.getValue()));

  pop.allIndividuals().forEach(i -> {
    writer.fitnessLandscapeSample(baseGeneration,
            0, 0, i);
  });

  int popSize = pop.size();

  numSamples += popSize;

  pop.individuals.clear();
  for (int i = 0; i < popSize; i++) {
    pop.individuals.add(dvig.generateIndividual());
  }


}


private static final class DoubleVectorIndividualGenerator {

  double[] min, max, range;

  public DoubleVectorIndividualGenerator(Population pop, double stdDevsOut) {

    VectorIndividual<Double> prototype = (VectorIndividual<Double>) pop.individuals.get(0);

    DescriptiveStatistics[] stats = new DescriptiveStatistics[prototype.getGenomeLength()];
    for (int i = 0; i < stats.length; i++)
      stats[i] = new DescriptiveStatistics();

    for (Individual i : pop.individuals) {
      VectorIndividual<Double> ind = (VectorIndividual<Double>) i;
      for (int j = 0; j < stats.length; j++) {
        stats[j].addValue(((VectorIndividual<Double>) i).get(j));
      }
    }

    min = new double[stats.length];
    max = new double[stats.length];
    range = new double[stats.length];

    for (int i = 0; i < stats.length; i++) {
      double mean = stats[i].getMean();
      double stddev = stats[i].getStandardDeviation();
      min[i] = mean - (stddev * stdDevsOut);
      max[i] = mean + (stddev * stdDevsOut);
      range[i] = max[i] - min[i];
    }


  }

  public VectorIndividual<Double> generateIndividual() {
    VectorIndividual<Double> toReturn = new VectorIndividual<>(min.length);
    Random r = ThreadLocalRandom.current();
    for (int i = 0; i < min.length; i++) {
      double foo = r.nextDouble() * range[i];
      foo += min[i];
      toReturn.set(i,foo);
    }
    return toReturn;
  }

}


}
