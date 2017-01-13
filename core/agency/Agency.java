package agency;

import java.io.File;
import java.util.Optional;
import java.util.logging.Logger;

import agency.eval.DebugEvaluator;
import agency.util.CmdLineUtils;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;

import static java.lang.System.out;
import static agency.util.CmdLineUtils.*;

public class Agency {

public static Logger log = Logger.getGlobal();

public static void main(String[] args) {

  // the Evolutionary Environment
  Environment env = null;
  int generationsToEvolve = 0;

  /*
   * Parse the command line to determine behavior. Agency uses Apache Commons
   * CLI for this purpose. The first step is defining options. These parser
   * can then be interrogated for the value of these options.
   */
  Options options = new Options();

  // Configuration file
  options.addOption(CmdLineUtils.configFileOption);

  // Checkpoint file
  options.addOption(CmdLineUtils.checkpointOption);

  // Specify # of generations to evolve
  options.addOption(generationsRequired);

  // Debug mode is possible; enables the DebugEvaluator
  options.addOption(debugMode);


  CommandLine cmd = CmdLineUtils.parseOrHelpAndExit(Agency.class, args,
                                                    options);


  /*
   * Must either have a new config file or be resuming from checkpoint.
   */
  if (!has(cmd, CmdLineUtils.configFileOption) &&
      !has(cmd, CmdLineUtils.checkpointOption)) {
    CmdLineUtils.printHelp(Agency.class,
                           options,
                           "Must either specify configuration or resume " +
                           "checkpoint");
    return;
  } else if (CmdLineUtils.has(cmd, CmdLineUtils.configFileOption) &&
             CmdLineUtils.has(cmd, CmdLineUtils.checkpointOption)) {
    CmdLineUtils.printHelp(Agency.class,
                           options,
                           "Must either specify EITHER configuration OR " +
                           "resume checkpoint, not both.");
    return;
  }

  Optional<Integer> gensOptional =
          CmdLineUtils.argInteger(cmd, generationsRequired);
  if (!gensOptional.isPresent()) {
    CmdLineUtils.printHelp(Agency.class,
                           options,
                           "# of generations to evolve must be a valid " +
                           "positive integer");
    return;
  }
  generationsToEvolve = gensOptional.get();
  if (generationsToEvolve <= 0) {
    CmdLineUtils.printHelp(Agency.class,
                           options,
                           "# of generations to evolve must be a valid " +
                           "positive integer");
    return;
  }

  /*
   * Load the evolutionary environment
   */
  System.out.println("Attempting to load the evolutionary environment");

  // From a checkpoint, if specified, otherwise from a configuration file.
  if (has(cmd, checkpointOption)) {
    Optional<Environment> opEnv = loadCheckpointIfSpecified(cmd);
    if (opEnv.isPresent()) {
      env = opEnv.get();
    } else {
      printHelp(Agency.class, options, "Could not open checkpoint file.");
      return;
    }
  }
  if (has(cmd, configFileOption)) {
    Optional<File> configFileOp = argExistingFile(cmd, configFileOption);
    if (configFileOp.isPresent()) {
      XMLConfigurable xc =
              Config.getXMLConfigurableFromFile(configFileOp.get());
      env = (Environment) xc;
    } else {
      printHelp(Agency.class, options, "Could not open config file.");
      return;
    }
  }
  if (env == null) {
    printHelp(Agency.class, options, "Unknown error occurred in creating the " +
                                     "environment.  This is likely a bug.");
    return;
  }

  /*
   * If running in debug mode, force the use of the (serial) DebugEvaluator
   */
  if (CmdLineUtils.has(cmd, debugMode)) {
    env.evaluator = new DebugEvaluator();
  }

  /*
   * Actually do the evolution
   */
  System.out.println("Evolving environment for " + generationsToEvolve +
                     " generations");
  while (generationsToEvolve-- > 0) {
    env.evolve();
    printScaledProgress(env.getGeneration());
  }

  /*
   * Save a checkpoint at the end of it.
   */
  System.out.println("Evolution finished.  Saving checkpoint at generation " +
                     env.getGeneration());
  Environment.saveCheckpoint(env);

  // Signal environment to close, so that it can flush and close all files.
  env.close();


}

private static void printScaledProgress(int generation) {
  if (generation < 30) {
    out.println("Generation " + generation);
  } else if (generation == 30) {
    out.println("Generation " + generation + ", now messages every 10");
  } else if (generation < 500) {
    if ((generation % 10) == 0) {
      out.println("Generation " + generation);
    }
  } else if (generation == 500) {
    out.println("Generation " + generation + ", now messages every 100");
  } else {
    if ((generation % 100) == 0) {
      out.println("Generation " + generation);
    }
 }
}

}