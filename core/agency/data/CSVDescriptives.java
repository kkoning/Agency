package agency.data;

import org.apache.commons.cli.*;
import org.apache.commons.math3.stat.descriptive.SummaryStatistics;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;


/**
 * This class takes a data file generated by Agency and creates summary files
 * with descriptive statistics, aggregated by generation.  It assumes that
 * lines in the file are sorted by generation and monotonically increasing.
 * <p>
 * Created by liara on 11/21/16.
 */
public class CSVDescriptives implements Runnable {

File                sourceFile;
BufferedReader      input;
String[]            sourceColNames;
SummaryStatistics[] stats;

DataOutput min, mean, stddev, max;

int curGen = 0;

public static void main(String[] args) throws Exception {

  /*
   * Parse the command line to determine behavior. Agency uses Apache Commons
   * CLI for this purpose. The first step is defining options. These parsers
   * can then be interrogated for the value of these options.
   */
  Options options = new Options();

  Option inputFileOption = Option.builder("f").longOpt("file").hasArg().required(true).type(String.class)
          .desc("Configuration file to load, for starting a new simulation").build();
  options.addOption(inputFileOption);

  CommandLineParser parser = new DefaultParser();
  CommandLine cmd;
  try {
    cmd = parser.parse(options, args);
  } catch (ParseException e) {
    System.out.println(e.getMessage());
    return;
  }

  String fileName = cmd.getOptionValue("file");

  CSVDescriptives self = new CSVDescriptives(fileName);
  self.run();


}

public CSVDescriptives(String fileName) throws Exception {
  sourceFile = new File(fileName);
}

@Override
public void run() {
  try {

    input = new BufferedReader(new FileReader(sourceFile));

    String line = input.readLine();
    sourceColNames = processInputLine(line);

    stats = new SummaryStatistics[sourceColNames.length];
    resetDescriptives();

    // Open output files
    min = createSumPrinter(sourceFile, "min");
    mean = createSumPrinter(sourceFile, "mean");
    stddev = createSumPrinter(sourceFile, "stddev");
    max = createSumPrinter(sourceFile, "max");

    line = input.readLine();
    while (line != null) {
      String[] data = processInputLine(line);

      Integer gen = Integer.parseInt(data[0]);
      if (gen > curGen) {
        nextGeneration();
        curGen = gen;
      }
      injestLine(data);


      line = input.readLine();
    }
    nextGeneration();

    // Close streams
    min.close();
    mean.close();
    stddev.close();
    max.close();
  } catch (IOException e) {
    e.printStackTrace();
  }

}

private static String[] processInputLine(String input) {
  String[] toReturn = input.split(",");
  return toReturn;
}


void injestLine(String[] data) {
  for (int i = 0; i < stats.length; i++) {
    String valString = data[i];
    Double val;
    try {
      val = Double.parseDouble(valString);
    } catch (Exception e) {
      val = Double.NaN;
    }
    stats[i].addValue(val);
  }
}

void nextGeneration() throws IOException {
  // output values
  printSummaryRecord(min, CSVDescriptives::min);
  printSummaryRecord(mean, CSVDescriptives::mean);
  printSummaryRecord(stddev, CSVDescriptives::stddev);
  printSummaryRecord(max, CSVDescriptives::max);

  // reset statistics
  resetDescriptives();
}

void printSummaryRecord(DataOutput out, Function<SummaryStatistics, Double> sumFunc) throws IOException {
  List<Object> values = new ArrayList<>();
  values.add(curGen);
  for (int i = 1; i < stats.length; i++) {
    Double value = sumFunc.apply(stats[i]);
    values.add(value);
  }
  values.add(stats[0].getN());

  out.writeLine(values);
}


static Double min(SummaryStatistics stats) {
  return stats.getMin();
}

static Double mean(SummaryStatistics stats) {
  return stats.getMean();
}

static Double stddev(SummaryStatistics stats) {
  return stats.getStandardDeviation();
}

static Double max(SummaryStatistics stats) {
  return stats.getMax();
}


DataOutput createSumPrinter(File source, String sumLabel) throws IOException {

  String sourceFilename = source.getName();
  String destFilename = sourceFilename.replaceFirst("(\\.csv)$", "_" + sumLabel + ".csv");
  DataOutput toReturn = new DataOutput(source.getParentFile().getCanonicalPath() + File.separator + destFilename);

  List<Object> headers = new ArrayList<>();

  for (String s : sourceColNames)
    headers.add(s);
  headers.add("N");

  toReturn.writeLine(headers);

  return toReturn;
}


private void resetDescriptives() {
  for (int i = 0; i < sourceColNames.length; i++) {
    stats[i] = new SummaryStatistics();
  }

}


}
