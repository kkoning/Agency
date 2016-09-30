package agency.data;

import agency.*;
import com.sun.xml.internal.ws.api.streaming.XMLStreamReaderFactory;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Created by liara on 9/26/16.
 */
public class DefaultEnvironmentStatistics extends DataOutput implements EnvironmentStatistics {

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
        Optional<Fitness> fitness = Optional.of(i.getFitness());
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
      this.write(d);
    }

  }
}

class Data implements AgencyData {
  Integer generation;
  String  populationGroup;
  String  population;
  Integer numIndividuals;
  Double  simpleFitnessMin;
  Double  simpleFitnessMean;
  Double  simpleFitnessSD;
  Double  simpleFitnessMax;

  @Override
  public List<String> getHeaders() {
    List<String> headers = new ArrayList<>();
    headers.add("generation");
    headers.add("populationGroup");
    headers.add("population");
    headers.add("numIndividuals");
    headers.add("simpleFitnessMin");
    headers.add("simpleFitnessMean");
    headers.add("simpleFitnessSD");
    headers.add("simpleFitnessMax");
    return headers;
  }

  @Override
  public List<Object> getValues() {
    List<Object> values = new ArrayList<>();
    values.add(generation);
    values.add(populationGroup);
    values.add(population);
    values.add(numIndividuals);
    values.add(simpleFitnessMin);
    values.add(simpleFitnessMean);
    values.add(simpleFitnessSD);
    values.add(simpleFitnessMax);
    return values;
  }
}


}
