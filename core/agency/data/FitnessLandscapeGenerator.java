package agency.data;

import agency.*;
import agency.eval.EvaluationGroup;
import agency.util.CmdLineUtils;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Calculates a fitness landscape for a specified population.
 * <p>
 * The fitness landscape calculator is useful for understanding why a population
 * is evolving as it is.   Crawling the fitness landscape will be done by
 * running agents through the same breeding process (including mutation, if at
 * all) specified in the config file for that agent population, but with all
 * fitnesses equal to a default fitness to disable selection.
 * <p>
 * This can be a somewhat inefficient way of calculating a fitness landscape,
 * but should provide at least a minimally functional version regardless of the
 * specific species or how it is configured.  There may be more efficient
 * versions (or sub-classes) of this class for specific species types.  For
 * example, see {@link DoubleVectorFitnessLandscapeGenerator}
 */
public class FitnessLandscapeGenerator
        implements Runnable {

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
 * The number of generations out to calculate
 */
final Integer generations;

/**
 * Only calculate the fitness landscape every <i>sampleEvery</i> generations.
 * This is useful for populations with low mutation rates, where it's not very
 * interesting to hugely oversample the area immediately around the existing
 * population.  While a new population will be generated for every generation,
 * the actual fitness measurement (and simulation processing!) will only be done
 * every <i>sampleEvery</i> generations.
 */
final Integer sampleEvery;
/**
 * Between each <i>sampleEvery</i> interval, the population should grow by the
 * ratio specified in <i>growthRatio</i>.  Keep in mind that this is an
 * exponential function, and so can grow very large.  The actual
 * generation-to-generation growth rate will be equal to growthRatio^
 * (1/sampleEvery).
 */
final Double  growthRatio;

// TODO: implement.  Need to save all the individuals from the original
// population in order to do this.
/**
 * Iterate <i>times</i> through the entire process, as a form of oversampling.
 * This may or may not be appropriate to a given type of agent.
 */
final Integer times;
/**
 * Where to write the results.
 */
final String  outputFile;

// Tracking Variables
/**
 * The population we're calculating the fitness landscape for.
 */
final         Population     pop;
final         int            startingPopulationSize;
final         int            baseGeneration;
/**
 * The PopulationData object is responsible for outputting the fitness
 * landscape data.  In the case where the population has more than one of these
 * (?) the first one is used.
 */
private final PopulationData writer;
/**
 * Population size is an integer, and, particularly if it is intended to grow
 * by a small amount, direct manipulation may be unacceptably inaccurate.
 * Consider the worst case, where growth is <0.5 individuals per generation, in
 * which case growth would <i>never</i> occur.  Instead, population growth is
 * tracked here, and converted to an integer population size each generation.
 */
private       double         currentPopulationSizeTarget;
private       int            generationOffset;

private int timesThrough;


public FitnessLandscapeGenerator(
        Environment env, String populationGroupID,
        String populationID, Integer generations,
        Integer sampleEvery, Double growthRatio,
        Integer times, String outputFile) {
  this.env = env;
  this.populationGroupID = populationGroupID;
  this.populationID = populationID;
  this.generations = generations;
  this.sampleEvery = sampleEvery;
  this.growthRatio = growthRatio;
  this.times = times;
  this.outputFile = outputFile;

  baseGeneration = env.getGeneration();

  PopulationGroup pg = env.getPopulationGroup(populationGroupID).get();
  pop = pg.getPopulation(populationID).get();
  writer = pop.getPopulationDataOutputs().get(0);
  startingPopulationSize = pop.size();

}

public static void main(String[] args) {
  // TODO: Currently in testing, implement command line parsing.

  Options options = new Options();

  Option cpf = CmdLineUtils.checkpointOption;
  options.addOption(cpf);

  Option popGroupString = Option.builder()
                                .required()
                                .hasArg()
                                .argName("pg")
                                .longOpt("populationGroup")
                                .build();
  options.addOption(popGroupString);

  Option popString = Option.builder()
                           .required()
                           .hasArg()
                           .argName("pg")
                           .longOpt("populationGroup")
                           .build();
  options.addOption(popString);

  Option generationsString = Option.builder()
                                   .required()
                                   .hasArg()
                                   .argName("g")
                                   .longOpt("generations")
                                   .build();
  options.addOption(generationsString);

  Option sampleEveryString = Option.builder()
                                   .hasArg()
                                   .longOpt("sampleEvery")
                                   .build();
  options.addOption(sampleEveryString);

  Option growthRatioString = Option.builder()
                                   .hasArg()
                                   .argName("r")
                                   .longOpt("growthRatio")
                                   .build();
  options.addOption(growthRatioString);


  Option timesString = Option.builder()
                             .hasArg()
                             .argName("t")
                             .longOpt("times")
                             .build();
  options.addOption(timesString);

  Option outputFileString = Option.builder()
                                  .required()
                                  .hasArg()
                                  .argName("o")
                                  .longOpt("out")
                                  .build();
  options.addOption(outputFileString);



  // FIXME

//  CommandLine cl = new
//
//  // Must read a checkpoint from a file
//  File serializedFile = new File(CmdLineUtils.arg());
//  Environment env = Environment.readCheckpoint(serializedFile);
//
//  FitnessLandscapeGenerator flg =
//          new FitnessLandscapeGenerator(env,
//                                        "videoContent",
//                                        "DirectlyEncodedVideoContentProviders",
//                                        100,
//                                        20,
//                                        2.0,
//                                        5,
//                                        "landscape.csv");
//
//  flg.run();


}


public void run() {

  // Always just use the first one.
  if (writer == null) {
    throw new RuntimeException("Cannot write fitness landscape data for a " +
                               "population that does not have any PopulationData objects.");
  }

  // Initialize starting population target size
  currentPopulationSizeTarget = pop.size();

  // TODO: 1/8/17 go through steps, growth, etc...

  writer.fitnessLandscapeOpen(outputFile);
  for (timesThrough = 0; timesThrough < times; timesThrough++) {
    for (generationOffset = 0; generationOffset < generations;
         generationOffset++) {
      step();
    }
  }
  writer.fitnessLandscapeClose();


}

/**
 * In general, follow the same pattern as Agency.evolve.  This means copying and
 * pasting some code, but
 */
void step() {

  /*
  We will need to do more fitness evaluations as the population size grows.
  TODO, there's no point in processing any EvaluationGroups that don't
  contain at least one member of the population we're interested in.
   */

  // Times to run the evaluation group generator
  int ttregg = (int) (currentPopulationSizeTarget / startingPopulationSize) + 1;

  List<EvaluationGroup> unevaluated = new ArrayList<>();

  for (int i = 0; i < ttregg; i++) {
    env.evaluationGroupFactory.createEvaluationGroups(env)
                              .forEach(eg -> unevaluated.add(eg));
  }

  List<EvaluationGroup> evaluatedGroups = env.evaluator
          .evaluate(unevaluated.stream()).collect(Collectors.toList());

  Map<Individual, Fitness> aggFitnesses = evaluatedGroups.parallelStream()
                                                         .map(eg -> eg.getResults())
                                                         .collect(
                                                                 Environment::newFitnessMap,
                                                                 Environment::fitnessAccumulator,
                                                                 Environment::fitnessAccumulator
                                                         );

  // Assign fitness
  aggFitnesses.entrySet().parallelStream().forEach(
          entry -> entry.getKey().setFitness(entry.getValue()));

  /*
  Now, just ignore all outputs other than the one we're interested in.
   */
  if ((generationOffset % sampleEvery) == 0) {
    pop.allIndividuals().forEach(i -> {
      writer.fitnessLandscapeSample(baseGeneration,
                                    generationOffset, timesThrough, i);
    });
  }

  currentPopulationSizeTarget *= growthRatio();


  /*
  Set fitnesses equal to zero to disable selection.
   */
  pop.allIndividuals().parallel().forEach(i -> i.setFitness(zeroFitness));

  /*
  Adjust acutual population size
   */

  pop.definatelyReproduce(env, (int) currentPopulationSizeTarget);

}


private final double growthRatio() {
  // Fix the growth ratio from sample time to generation time.
  return Math.pow(growthRatio, 1 / sampleEvery);
}


}
