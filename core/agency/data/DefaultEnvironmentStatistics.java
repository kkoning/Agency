package agency.data;

import agency.*;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.Optional;

/**
 * Created by liara on 9/26/16.
 */
public class DefaultEnvironmentStatistics implements XMLConfigurable, EnvironmentStatistics {

ReflectionDataOutput rdo;
String fileName = "DefaultEnvironmentStatistics.tsv";

@Override
public void readXMLConfig(Element e) {
  openFile();
}

@Override
public void writeXMLConfig(Element e) {

}

@Override
public void resumeFromCheckpoint() {

}

private void openFile() {
  rdo = new ReflectionDataOutput();
  rdo.outputFileName = this.fileName;
}

@Override
public void calculate(Environment env) {
  for (PopulationGroup pg : env.getPopulationGroups()) {
    for (Population p : pg.getPopulations()) {
      Data d = new Data();
      d.generation = env.getGeneration();
      d.populationGroup = pg.getId();
      d.population = p.getId();
      d.numIndividuals = p.size();

      // Calculate statistics on (assumed) SimpleFitnesses
      DescriptiveStatistics stats = new DescriptiveStatistics();
      p.allIndividuals().forEach(i -> {
        Optional<Fitness> fitness = i.getFitness();
        fitness.ifPresent(f -> {
          try {
            SimpleFitness sf = (SimpleFitness) f;
            stats.addValue(sf.getFitness());
          } catch (ClassCastException cce) {
            throw new RuntimeException("DefaultEnvironmentStatistics assumes individuals with SimpleFitness");
          }
        });
      });

      d.simpleFitnessMin = stats.getMin();
      d.simpleFitnessMean = stats.getMean();
      d.simpleFitnessSD = stats.getStandardDeviation();
      d.simpleFitnessMax = stats.getMax();
      rdo.write(d);
    }

  }
}

class Data {
  Integer generation;
  String  populationGroup;
  String  population;
  Integer numIndividuals;
  Double  simpleFitnessMin;
  Double  simpleFitnessMean;
  Double  simpleFitnessSD;
  Double  simpleFitnessMax;
}


}
