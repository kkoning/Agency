package agency.eval;

import java.util.OptionalDouble;
import java.util.stream.Stream;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import agency.Fitness;
import agency.SimpleFitness;
import agency.XMLConfigurable;

public class MeanSimpleFitnessAggregator
    implements XMLConfigurable, FitnessAggregator {

  @Override
  public SimpleFitness reduce(Stream<Fitness> fitnesses) {
    OptionalDouble aggFitness = fitnesses.map(f -> (SimpleFitness) f)
        .mapToDouble(f -> f.getFitness()).average();

    if (aggFitness.isPresent()) {
      if (aggFitness.getAsDouble() == Double.NaN)
        throw new RuntimeException("NaN Fitness");
      else
        return new SimpleFitness(aggFitness.getAsDouble());
    } else {
      throw new RuntimeException("Fitness aggregated to null?");
    }

  }

  @Override
  public void readXMLConfig(Element e) {
    // No configuration reuired
  }

  @Override
  public void writeXMLConfig(Document d, Element e) {
    // No configuration reuired
  }

}
