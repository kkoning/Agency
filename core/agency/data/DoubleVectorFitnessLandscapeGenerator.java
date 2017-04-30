package agency.data;

import agency.*;
import agency.eval.EvaluationGroup;
import agency.eval.Evaluator;
import agency.eval.LocalEvaluator;
import agency.eval.LocalParallelEvaluator;
import agency.util.CmdLineUtils;
import agency.vector.VectorIndividual;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import java.io.File;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

/**
 * Calculates a fitness landscape for a specified population.
 * <p>
 * The fitness landscape calculator is useful for understanding why a population
 * is evolving as it is.   This is a special case for VectorIndividuals whose
 * elements are entirely {@link java.lang.Double}s.  This lets us spread out the
 * samples in a much more effective way.
 */
public class DoubleVectorFitnessLandscapeGenerator
        implements Runnable {

private static final int numSamplesNotice = 1000;

/**
 * Need a link to the environment.  Loaded from a checkpoint file by main().
 */
final Environment env;


final Population sourcePopulation;

/**
 * Where to write the results.
 */
final String outputFile;

/**
 * The generation for which
 */
final int baseGeneration;
final int numSamplesRequested;

/**
 * The PopulationData object is responsible for outputting the fitness
 * landscape data.  In the case where the population has more than one of these
 * (?) the first one is used.
 */
private final PopulationData writer;

private final Evaluator evaluator;

int                             numSamples;
DoubleVectorIndividualGenerator dvig;

public DoubleVectorFitnessLandscapeGenerator(
        Environment env,
        Evaluator evaluator,
        Population sourcePopulation,
        DoubleVectorIndividualGenerator dvig,
        PopulationData writer,
        String outputFile,
        int numSamplesRequested) {
  this.env = env;
  this.sourcePopulation = sourcePopulation;
  this.dvig = dvig;
  this.writer = writer;
  this.outputFile = outputFile;
  this.numSamplesRequested = numSamplesRequested;
  this.evaluator = evaluator;

  baseGeneration = env.getGeneration();
  numSamples = 0;
}


public static void main(String[] args) {
  // TODO: Currently in testing, implement command line parsing.

  /*
   Process command-line options
   */

  Options options = new Options();

  // Checkpoint
  Option checkpointOption = (Option) CmdLineUtils.checkpointOption.clone();
  checkpointOption.setRequired(true);
  options.addOption(checkpointOption);

  // PopulationGroupID
  Option popGroupOption = Option
          .builder("pg")
          .longOpt("populationGroup")
          .required()
          .desc("The population group containing the population to generate " +
                "the fitness landscape for.  As specified in the " +
                "configuration file, e.g., <PopulationGroup id=\"arg\">...")
          .hasArg()
          .argName("id")
          .build();
  options.addOption(popGroupOption);

  Option popOption = Option
          .builder("p")
          .longOpt("population")
          .required()
          .desc("The population to generate the fitness landscape for.  As " +
                "specified in the configuration file, e.g., " +
                "<Population id=\"arg\">...")
          .hasArg()
          .argName("id")
          .build();
  options.addOption(popOption);

  Option stdDevOption = Option
          .builder("s")
          .longOpt("sigma")
          .desc("Specify the size of the fitness landscape to probe in terms" +
                "of the number of standard deviations from the mean of each " +
                "existing loci.")
          .hasArg()
          .build();
  options.addOption(stdDevOption);

  Option fixedRangeOption = Option
          .builder("fr")
          .longOpt("fixedRange")
          .desc("Specify the size of the fitness landscape to probe in terms " +
                "of a fixed numeric range from the mean of each existing loci" +
                ".  The same value is used for each loci.  If you need to " +
                "specify a different value for each loci, use --variableRange" +
                " instead.")
          .hasArg()
          .build();
  options.addOption(fixedRangeOption);

  // TODO: Figure this out / test.
  Option variableRangeOption = Option
          .builder("vr")
          .longOpt("variableRange")
          .desc("Specify the size of the fitness landscape to probe in terms " +
                "of a numeric range from the mean of each loci.  You must " +
                "specify a +/- range for each loci, separated by the ':' " +
                "character.")
          .hasArgs()
          .valueSeparator(':')
          .build();
  options.addOption(variableRangeOption);

  Option outputFile = Option
          .builder("o")
          .longOpt("output")
          .desc("The name of the file to write the results.")
          .hasArg()
          .required()
          .build();
  options.addOption(outputFile);

  Option numSamples = Option
          .builder("n")
          .longOpt("numSamples")
          .desc("How many samples to take.  The actual amount taken may be " +
                "slightly greater, because this utility always uses a " +
                "multiple of the size of the population.")
          .hasArg()
          .required()
          .build();
  options.addOption(numSamples);

  // Options related to which evaluator to use.
  Option parallelEvaluator = Option
          .builder("pe")
          .longOpt("parallelEvaluator")
          .desc("Use a local ParallelEvaluator to run the agent models that " +
                "produce the fitness samples.  This option may be useful in " +
                "moving from a parameter sweep performed on a serial cluster " +
                "to a rapid fitness landscape generation on a workstation.")
          .build();
  options.addOption(parallelEvaluator);

  Option serialEvaluator = Option
          .builder("se")
          .longOpt("serialEvaluator")
          .desc("Use a local (serial) LocalEvaluator to run the agent models " +
                "that produce the fitness samples.  This option may be useful" +
                " on HPCC clusters that expect the use of only a single " +
                "simultaneous thread.")
          .build();
  options.addOption(serialEvaluator);

  Option configEvaluator = Option
          .builder("ce")
          .longOpt("configEvaluator")
          .desc("Use the Evaluator originally specified in the configuration " +
                "file (the same one as used in the evolutionary run) to run " +
                "the agent models that produce the fitness samples.  This is " +
                "the option that is used by default, and thus this argument " +
                "is unnecessary except for purposes of providing clarity.")
          .build();
  options.addOption(configEvaluator);

  CommandLine cmd = CmdLineUtils.parseOrHelpAndExit(
          DoubleVectorFitnessLandscapeGenerator.class,
          args,
          options);

  // Exactly one range option must be selected.
  int rangeOptions = 0;
  if (CmdLineUtils.has(cmd, stdDevOption))
    rangeOptions++;
  if (CmdLineUtils.has(cmd, fixedRangeOption))
    rangeOptions++;
  if (CmdLineUtils.has(cmd, variableRangeOption))
    rangeOptions++;
  if (rangeOptions == 0) {
    CmdLineUtils.printHelp(DoubleVectorFitnessLandscapeGenerator.class,
                           options,
                           "You must select at least one of -sd, -fr, or -vr");
    return;
  }
  if (rangeOptions == 0) {
    CmdLineUtils.printHelp(DoubleVectorFitnessLandscapeGenerator.class,
                           options,
                           "You must select at least one of -sd, -fr, or -vr");
    return;
  } else if (rangeOptions >= 2) {
    CmdLineUtils.printHelp(DoubleVectorFitnessLandscapeGenerator.class,
                           options,
                           "You must select ONLY ONE of -sd, -fr, or -vr");
    return;
  }

  /*
   Set up and run the fitness landscape generator.
   */
  // Load the environment
  Optional<Environment> envOp = CmdLineUtils.loadCheckpointIfSpecified(cmd);
  if (!envOp.isPresent()) {
    System.out.println("ERROR: Could not load specified checkpoint file.");
    return;
  }
  Environment env = envOp.get();

  // Find the population
  Optional<PopulationGroup> popGroupOp =
          env.getPopulationGroup(CmdLineUtils.arg(cmd, popGroupOption));
  if (!popGroupOp.isPresent()) {
    System.out.println("ERROR: Could not find the PopulationGroup with id=\""
                       + CmdLineUtils.arg(cmd, popGroupOption) + "\".");
    return;
  }
  PopulationGroup popGroup = popGroupOp.get();
  Optional<Population> popOp = popGroup.getPopulation(
          CmdLineUtils.arg(cmd, popOption));
  if (!popOp.isPresent()) {
    System.out.println("ERROR: Could not find the Population with id=\""
                       + CmdLineUtils.arg(cmd, popOption) + "\" in the " +
                       "Population group with id=\"" +
                       CmdLineUtils.arg(cmd, popGroupOption) + "\".");
    return;
  }
  Population pop = popOp.get();

  PopulationData writer = pop.getPopulationDataOutputs().get(0);
  if (writer == null)
    throw new RuntimeException("Target population must have at least one " +
                               "PopulationData output object.");

  Optional<Integer> numSampOp = CmdLineUtils.argInteger(cmd, numSamples);
  if (!numSampOp.isPresent()) {
    CmdLineUtils.printHelp(DoubleVectorFitnessLandscapeGenerator.class,
                           options,
                           "You must select an integer number of samples. " +
                           "e.g., -n 100000");
    return;
  }

  // Initialize the individual generator
  DoubleVectorIndividualGenerator dvig =
          new DoubleVectorIndividualGenerator(pop);

  // Calculate the range boundaries for each command-line option.
  if (CmdLineUtils.has(cmd, fixedRangeOption)) {
    // For fixed range option.
    Optional<Double> fixedRangeOp = CmdLineUtils.argDouble(cmd,
                                                           fixedRangeOption);
    if (!fixedRangeOp.isPresent()) {
      CmdLineUtils.printHelp(DoubleVectorFitnessLandscapeGenerator.class,
                             options,
                             "A fixed range requires an argument that " +
                             "specifies the range and is a valid number.");
      return;
    } else {
      dvig.setFixedRange(fixedRangeOp.get());
    }
  } else if (CmdLineUtils.has(cmd, variableRangeOption)) {
    // For the Variable Range Option
    Optional<Double> vrOp = CmdLineUtils.argDouble(cmd,
                                                   variableRangeOption);
    if (!vrOp.isPresent()) {
      CmdLineUtils.printHelp(DoubleVectorFitnessLandscapeGenerator.class,
                             options,
                             "A range in terms of standard deviations from" +
                             " the mean of each loci requires the number of" +
                             " standard deviations to be specified in an " +
                             "argument.  E.g., '-sd 15'.");
      return;
    } else {
      Optional<double[]> doubListOp =
              CmdLineUtils.argDoubleList(cmd,
                                         variableRangeOption);

      // Make sure the argument indeed contains a list of doubles
      if (!doubListOp.isPresent()) {
        CmdLineUtils.printHelp(DoubleVectorFitnessLandscapeGenerator.class,
                               options,
                               "ERROR: There was an error in parsing the list " +
                               "of values to be used as bounds for each loci.");
        return;
      }
      double[] rangeList = doubListOp.get();

      // Make sure the length of the vectors match.
      if ((rangeList.length != dvig.populationMean.length)) {
        CmdLineUtils.printHelp(DoubleVectorFitnessLandscapeGenerator.class,
                               options,
                               "ERROR: The length of the vector of ranges " +
                               "specified on the command line must be equal " +
                               "to the length of the species' genome.  " +
                               "Specified list of length " + rangeList.length
                               + ", population genome of length " +
                               dvig.populationMean.length);
        return;
      }

      // Finally, everything should be set...
      dvig.setVariableRange(rangeList);
    }
  } else if (CmdLineUtils.has(cmd, stdDevOption)) {
    // For stdDeviationsRangeOption
    Optional<Double> sdOp = CmdLineUtils.argDouble(cmd,
                                                   stdDevOption);
    if (!sdOp.isPresent()) {
      CmdLineUtils.printHelp(DoubleVectorFitnessLandscapeGenerator.class,
                             options,
                             "A range in terms of standard deviations from" +
                             " the mean of each loci requires the number of" +
                             " standard deviations to be specified in an " +
                             "argument.  E.g., '-sd 15'.");
      return;
    } else {
      dvig.setStdDeviationsRange(sdOp.get());
    }
  }

  // Last thing to do, figure out which evaluator to use.
  Evaluator evaluator;
  if (CmdLineUtils.has(cmd, parallelEvaluator))
    evaluator = new LocalParallelEvaluator();
  else if (CmdLineUtils.has(cmd, serialEvaluator))
    evaluator = new LocalEvaluator();
  else
    evaluator = env.evaluator;


  // Finally, we have everything we need to set up the fitness landscape
  // generator... (most of the work above was in preparing the individual
  // generator, or the argument 'dvig' to this function.
  DoubleVectorFitnessLandscapeGenerator flg =
          new DoubleVectorFitnessLandscapeGenerator(
                  env,
                  evaluator,
                  pop,
                  dvig,
                  writer,
                  CmdLineUtils.arg(cmd, outputFile),
                  numSampOp.get());

  flg.run();
}

/**
 * After the constructor has been called, everything should be set up
 * appropriately.  run() simply calls step() repeatedly until a sufficient
 * number of samples have been generated, then flushes and closes output files.
 */
public void run() {
  writer.fitnessLandscapeOpen(outputFile);
  int nextNotice = numSamplesNotice;
  while (numSamples < numSamplesRequested) {
    step();
    if (numSamples > nextNotice) {
      System.out.println(nextNotice + " samples generated");
      nextNotice += numSamplesNotice;
    }
  }
  writer.fitnessLandscapeClose();
}

/**
 * In general, follow the same pattern as Agency.evolve.
 */
void step() {

  /*
   * We will need to do more fitness evaluations as the population size grows.
   * TODO, there's no point in processing any EvaluationGroups that don't
   * contain at least one member of the population we're interested in.
   */


  List<EvaluationGroup> unevaluated = new ArrayList<>();
  env.evaluationGroupFactory.createEvaluationGroups(env)
                            .forEach(eg -> unevaluated.add(eg));

  List<EvaluationGroup> evaluatedGroups = evaluator
          .evaluate(unevaluated.stream()).collect(Collectors.toList());

  Map<Individual, Fitness> aggFitnesses =
          evaluatedGroups.parallelStream()
                         .map(eg -> eg.getResults())
                         .collect(
                                 Environment::newFitnessMap,
                                 Environment::fitnessAccumulator,
                                 Environment::fitnessAccumulator
                         );

  // Assign fitness
  aggFitnesses.entrySet().parallelStream().forEach(
          entry -> entry.getKey().setFitness(entry.getValue()));

  sourcePopulation.individuals.stream().forEach(i -> {
    writer.fitnessLandscapeSample(baseGeneration,
                                  0, 0, i);
  });

  int popSize = sourcePopulation.size();

  numSamples += popSize;

  sourcePopulation.individuals.clear();
  for (int i = 0; i < popSize; i++) {
    sourcePopulation.individuals.add(dvig.generateIndividual());
  }


}


private static final class DoubleVectorIndividualGenerator {
  Population               sourcePop;
  VectorIndividual<Double> prototype;
  double[]                 populationMin;
  double[]                 populationMean;
  double[]                 populationSD;
  double[]                 populationMax;
  double[]                 landscapeLow;
  double[]                 landscapeRange;

  boolean initialized;

  public DoubleVectorIndividualGenerator(Population sourcePop) {
    this.sourcePop = sourcePop;
    this.prototype = (VectorIndividual<Double>) sourcePop.individuals.get(0);

    DescriptiveStatistics[] stats =
            new DescriptiveStatistics[prototype.getGenomeLength()];
    for (int i = 0; i < stats.length; i++) {
      stats[i] = new DescriptiveStatistics();
    }

    for (Individual i : sourcePop.individuals) {
      VectorIndividual<Double> ind = (VectorIndividual<Double>) i;
      for (int j = 0; j < stats.length; j++) {
        stats[j].addValue(((VectorIndividual<Double>) i).gene(j));
      }
    }

    populationMin = new double[stats.length];
    populationMean = new double[stats.length];
    populationSD = new double[stats.length];
    populationMax = new double[stats.length];
    landscapeLow = new double[stats.length];
    landscapeRange = new double[stats.length];

    for (int i = 0; i < stats.length; i++) {
      populationMin[i] = stats[i].getMin();
      populationMean[i] = stats[i].getMean();
      populationSD[i] = stats[i].getStandardDeviation();
      populationMax[i] = stats[i].getMax();
    }
  }


  public void setFixedRange(double fixedRange) {
    for (int i = 0; i < populationMean.length; i++) {
      landscapeLow[i] = populationMean[i] - fixedRange;
      landscapeRange[i] = fixedRange + fixedRange;
    }
    initialized = true;
  }

  public void setStdDeviationsRange(double stdDeviations) {
    for (int i = 0; i < populationMean.length; i++) {
      landscapeLow[i] = populationMean[i] - (populationSD[i] * stdDeviations);
      landscapeRange[i] = populationSD[i] * 2 * stdDeviations;
    }
    initialized = true;
  }

  public void setVariableRange(double[] variableRange) {
    // Check that the array sizes match.
    if (variableRange.length != populationMean.length)
      throw new RuntimeException("VariableRange must be an array of values " +
                                 "equal to the size of the genome.");
    for (int i = 0; i < populationMean.length; i++) {
      landscapeLow[i] = populationMean[i] - variableRange[i];
      landscapeRange[i] = variableRange[i] * 2;
    }
    initialized = true;
  }

  public VectorIndividual<Double> generateIndividual() {
    VectorIndividual<Double> toReturn =
            new VectorIndividual<>(populationMean.length);
    Random r = ThreadLocalRandom.current();
    for (int i = 0; i < populationMean.length; i++) {
      double foo = r.nextDouble() * landscapeRange[i];
      foo += landscapeLow[i];
      toReturn.changeGene(i, foo);
    }
    return toReturn;
  }

}


}
