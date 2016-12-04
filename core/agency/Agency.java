package agency;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.logging.Logger;

import agency.eval.DebugEvaluator;
import agency.eval.LocalEvaluator;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.io.IOUtils;

import static java.lang.System.out;

public class Agency {

public static Logger log = Logger.getGlobal();

public static void main(String[] args) {
  // the Evolutionary Environment
  Environment env = null;

    /*
     * Parse the command line to determine behavior. Agency uses Apache Commons
     * CLI for this purpose. The first step is defining options. These parser
     * can then be interrogated for the value of these options.
     */
  Options options = new Options();

  // Configuration file
  Option configFile = Option.builder("f").longOpt("file").hasArg().type(String.class)
          .desc("Configuration file to load, for starting a new simulation").build();
  options.addOption(configFile);

  // Checpoint file
  Option checkpointFile = Option.builder().longOpt("checkpoint").hasArg().build();
  options.addOption(checkpointFile);

  // Specify # of generations to evolve
  Option generations = Option.builder("g").required(true).longOpt("generations").hasArg().type(Integer.class).build();
  options.addOption(generations);

  Option debugMode = Option.builder("d").longOpt("debug").hasArg(false).required(false).build();
  options.addOption(debugMode);

  // Help
  Option help = Option.builder().longOpt("help").build();
  options.addOption(help);

  // Actually parse the command line, according to the options built above.
  CommandLineParser parser = new DefaultParser();
  CommandLine cmd;
  try {
    cmd = parser.parse(options, args);
  } catch (ParseException e) {
    System.out.println(e.getMessage());
    printHelp(options);
    return;
  }

    /*
     * Must either have a new config file or be resuming from checkpoint.
     */
  if (!has(cmd, configFile) && !has(cmd, checkpointFile)) {
    printHelp(options, "Must either specify configuration or resume checkpoint");
    return;
  } else if (has(cmd, configFile) && has(cmd, checkpointFile)) {
    printHelp(options, "Must either specify EITHER configuration OR resume checkpoint, not both.");
    return;
  }
  /*
   * Validate a positive integer # of generations to evolve.
   */
  int generationsToEvolve = 0;
  try {
    generationsToEvolve = Integer.parseInt(arg(cmd, generations));
  } catch (NumberFormatException e) {
    printHelp(options, "# of generations to evolve must be an integer");
    return;
  }
  if (generationsToEvolve <= 0) {
    printHelp(options, "# of generations to evolve must be positive");
    return;
  }

  /*
   * Load the evolutionary environment
   */
  System.out.println("Attempting to load the evolutionary environment");
  // From the config file, if specified
  if (has(cmd, configFile)) {
    XMLConfigurable xc = Config.getXMLConfigurableFromFile(arg(cmd, configFile));
    env = (Environment) xc;
  } else if (has(cmd, checkpointFile)) {
    // From a checkpoint file, if specified.
    File serializedFile = new File(arg(cmd, checkpointFile));
    env = Environment.readCheckpoint(serializedFile);
  }

  if (env == null) {
    out.println("Unknown error in creating environment, please file a bug report.");
    return;
  }

  /*
   * If running in debug mode, force the use of the (serial) LocalEvaluator
   */
  if (has(cmd,debugMode)) {
    env.evaluator = new DebugEvaluator();
  }

  System.out.println("Evolving environment for " + generationsToEvolve + " generations");
  while (generationsToEvolve-- > 0) {
    env.evolve();
    out.println("Generation " + env.generation);
  }

  System.out.println("Saving checkpoint at generation " + env.getGeneration());
  Environment.saveCheckpoint(env);


}

static boolean has(CommandLine cmd, Option o) {
  return cmd.hasOption(o.getLongOpt());
}

static String arg(CommandLine cmd, Option o) {
  return cmd.getOptionValue(o.getLongOpt());
}

static void printHelp(Options options) {
  printHelp(options, null);
}

static void printHelp(Options options, String error) {

  if (error != null)
    System.out.println(error);

    /*
     * Read the file doc/usage.txt so that it can be presented as help.
     */
  InputStream usageText = Agency.class.getResourceAsStream("usage.txt");
  StringWriter sw = new StringWriter();
  try {
    IOUtils.copy(usageText, sw);
  } catch (IOException e) {
    out.println("Error: could not load usage.txt");
  }

  HelpFormatter help = new HelpFormatter();
  help.printHelp(sw.toString(), options);
}

}
