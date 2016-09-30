package agency.models.simple;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

import agency.data.AgencyData;
import org.apache.commons.lang3.RandomUtils;
import agency.Agent;
import agency.AgentModel;
import agency.Fitness;
import agency.Individual;
import agency.SimpleFitness;
import agency.vector.VectorIndividual;

public class MaximumValue implements AgentModel {

  Double scalingFactor = 1.0;
  
  public class SummaryData implements AgencyData {
    int randomInt;
    String label;

    @Override
    public List<String> getHeaders() {
      List<String> headers = new ArrayList<>();
      headers.add("randomInt");
      headers.add("label");
      return headers;
    }

    @Override
    public List<Object> getValues() {
      List<Object> values = new ArrayList<>();
      values.add(randomInt);
      values.add(label);
      return values;
    }
  }

  @Override
  public Fitness getFitness(Agent<? extends Individual> agent) {
    
    // The agent is assumed to have a VectorInvididual<Integer>
    VectorIndividual<Integer> ind = (VectorIndividual<Integer>) agent
        .getManager();
    int genomeSize = ind.getGenomeLength();
    double sumOfGenome = IntStream.range(0, genomeSize).mapToDouble((i) -> {
      return (double) ind.get(i);
    }).sum();

    Fitness fit = new SimpleFitness(sumOfGenome * scalingFactor);
    return fit;
  }

  @Override
  public void addAgent(Agent<? extends Individual> agent) {
    // Don't need to add them to a simulation, they can be evaluated directly
    // based on the content of their genome.
  }

  @Override
  public boolean step() {
    // Stepping this model isn't needed; individuals can be evaluated directly.
    return false;  // don't stop early
  }

  @Override
  public int getMaxSteps() {
    return 3;
  }
  

  @Override
  public AgencyData getSummaryData() {
    SummaryData sd = new SummaryData();
    sd.label = "foo";
    sd.randomInt = RandomUtils.nextInt(0, 100_000);
    return sd;
  }

  @Override
  public AgencyData getStepData() {
    SummaryData sd = new SummaryData();
    sd.label = "bar";
    sd.randomInt = RandomUtils.nextInt(0, 100_000);
    return sd;
  }

}
