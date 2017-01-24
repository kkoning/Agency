package agency.models.simple;

import java.io.PrintStream;
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
  int    randomInt;
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
public void init() {
  // Unnecessary
}

@Override
public void finish() {
 // Unnecessary
}

@Override
public Fitness getFitness(Agent<? extends Individual> agent) {

  // The agent is assumed to have a VectorInvididual<Integer>
  VectorIndividual<Double> ind = (VectorIndividual<Double>) agent
          .getManager();
  int genomeSize = ind.getGenomeLength();
  double sumOfGenome = 0;
  for (int i = 0; i < ind.getGenomeLength(); i++) {
    Object[] o = ind.getGenome();
    sumOfGenome += (Double) o[i];
  }
  if (sumOfGenome <= 0)
    sumOfGenome = Double.MIN_NORMAL;
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

@Override
public void enableDebug(PrintStream out) {
  // No debugging supported.
}

}
