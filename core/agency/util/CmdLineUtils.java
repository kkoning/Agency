package agency.util;

import agency.Agency;
import agency.Environment;
import com.sun.istack.internal.NotNull;
import com.sun.istack.internal.Nullable;
import org.apache.commons.cli.*;
import org.apache.commons.io.IOUtils;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.lang.System.err;
import static java.lang.System.out;

/**
 * Unfortunately, the apache commons cli toolkit falls quite short in the
 * usability department, requiring more verbosity in code than is ideal.
 * While this might be appropriate to allow users some flexibility in how the
 * package is used, for our purposes we'd much rather have greater code
 * re-use and consistency among several different command-line utilities.
 */
public class CmdLineUtils {

public static final Option helpOption = Option
        .builder("h")
        .longOpt("help")
        .desc("Print help")
        .build();

public static final Option debugMode = Option
        .builder("d")
        .longOpt("debug")
        .desc("Enable debug mode. Warning: this option could generate a " +
              "large amount of output!")
        .build();

public static final Option checkpointOption = Option
        .builder("c")
        .longOpt("checkpoint")
        .desc("Load an Agency checkpoint from the specified file.")
        .hasArg()
        .build();

public static final Option configFileOption = Option
        .builder("f")
        .longOpt("file")
        .hasArg()
        .desc("Configuration file to load, i.e., for starting a new simulation")
        .build();

public static final Option generations = Option
        .builder("g")
        .longOpt("generations")
        .hasArg()
        .desc("An integer # of generations, i.e., to evolve")
        .build();

public static final Option generationsRequired = Option
        .builder("g")
        .longOpt("generations")
        .required()
        .desc("An integer # of generations, i.e., to evolve")
        .hasArg()
        .build();


public static Optional<Integer> argInteger(CommandLine cmd, Option o) {
  Integer value = null;
  try {
    value = argIntegerThrow(cmd, o);
  } catch (NumberFormatException nfe) {
    // Do nothing; return an empty optional
  }

  return Optional.of(value);
}

/**
 * @param cmd
 *         A parsed command line
 * @param o
 *         An option that was part of that parsing
 * @return The value of the option, as an Integer.
 * @throws NumberFormatException
 *         if the integer cannot be parsed
 */
public static Integer argIntegerThrow(CommandLine cmd, Option o)
throws NumberFormatException {
  return Integer.parseInt(arg(cmd, o));
}

/**
 * @param cmd
 *         A parsed command line
 * @param o
 *         An option that was part of that parsing
 * @return The value of the option, as a String.
 */
public static String arg(CommandLine cmd, Option o) {
  return cmd.getOptionValue(o.getLongOpt());
}

public static Optional<Double> argDouble(CommandLine cmd, Option o) {
  Double value = null;
  try {
    value = argDoubleThrow(cmd, o);
  } catch (NumberFormatException nfe) {
    // Do nothing; return an empty optional.
  }
  return Optional.of(value);
}

public static Double argDoubleThrow(CommandLine cmd, Option o)
throws NumberFormatException {
  return Double.parseDouble(arg(cmd, o));
}

public static Optional<File> argExistingFile(CommandLine cmd, Option o) {
  Optional<File> toReturn = argFile(cmd, o);
  if (toReturn.isPresent()) {
    File file = toReturn.get();
    if (!file.exists()) {
      return Optional.empty();
    }
  }
  return toReturn;
}

public static Optional<File> argFile(CommandLine cmd, Option o) {
  File value = null;
  try {
    value = argFileThrow(cmd, o);
  } catch (Exception e) {
    // Do nothing; return empty optional
  }
  return Optional.of(value);
}

public static File argFileThrow(CommandLine cmd, Option o) {
  return new File(arg(cmd, o));
}

public static CommandLine parseOrHelpAndExit(
        Class<?> mainClass,
        String[] args,
        Options options) {

  CommandLine cl = null;
  try {
    cl = parseThrow(args, options);
  } catch (ParseException pe) {
    printHelp(mainClass, options, pe);
    System.exit(-1);
  }

  if (has(cl, helpOption)) {
    printHelp(mainClass, options);
  }

  return cl;
}

/**
 * @param cmd
 *         A parsed command line
 * @param o
 *         An option that was part of that parsing
 * @return true if the command line contains the specified option.
 */
public static boolean has(CommandLine cmd, Option o) {
  return cmd.hasOption(o.getLongOpt());
}

public static CommandLine parseThrow(
        String[] args,
        Options options)
throws ParseException {
  CommandLineParser parser = new DefaultParser();
  options.addOption(helpOption);
  return parser.parse(options, args);
}

public static void printHelp(
        @NotNull Class<?> mainClass,
        @NotNull Options options) {
  // This is here to differentiate the overloaded call between two method
  // argument types.
  String nullString = null;
  printHelp(mainClass, options, nullString);
}

public static void printHelp(
        @NotNull Class<?> mainClass,
        @NotNull Options options,
        @Nullable ParseException pe) {

  String errorMessage = null;

  if (pe != null) {
    errorMessage = pe.getMessage();
  }
  printHelp(mainClass, options, errorMessage);

}

public static void printHelp(
        @NotNull Class<?> mainClass,
        @NotNull Options options,
        @Nullable String errorMessage) {

  String className = mainClass.getSimpleName();

  // Get resources for header and footer and read them into strings.
  String resourceURL = className + ".usageHeader.txt";
  InputStream headerIS = mainClass
          .getResourceAsStream(resourceURL);
  String headerString = "";
  if (headerIS != null) {
    try {
      headerString = IOUtils.toString(headerIS);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
  InputStream footerIS = mainClass
          .getResourceAsStream(className + ".usageFooter.txt");
  String footerString = "";
  if (footerIS != null) {
    try {
      footerString = IOUtils.toString(footerIS);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  if (errorMessage != null) {
    System.out.println(errorMessage);
  }

  PrintWriter pw = new PrintWriter(System.out);

  HelpFormatter help = new HelpFormatter();
  help.printHelp(pw,
                 80,
                 mainClass.getCanonicalName(),
                 "\n" + headerString + "\nArguments:\n",
                 options,
                 2,
                 2,
                 "\n" + footerString + "\n",
                 true);
  pw.flush();
  pw.close();
}

private static List<String> getResourceFiles(String path) throws IOException {
  List<String> filenames = new ArrayList<>();

  try (
          InputStream in = getResourceAsStream(path);
          BufferedReader br = new BufferedReader(new InputStreamReader(in))) {
    String resource;

    while ((resource = br.readLine()) != null) {
      filenames.add(resource);
    }
  }

  return filenames;
}

private static InputStream getResourceAsStream(String resource) {
  final InputStream in
          = Agency.class
//          .getClassLoader()
.getResourceAsStream(
        resource);

  return in == null ? Agency.class.getResourceAsStream(resource) : in;
}

public static Optional<Environment> loadCheckpointIfSpecified(CommandLine cl) {
  Environment env = null;
  if (CmdLineUtils.has(cl, checkpointOption)) {
    File serializedFile = new File(CmdLineUtils.arg(cl, checkpointOption));
    env = Environment.readCheckpoint(serializedFile);
  }
  return Optional.of(env);
}
}
