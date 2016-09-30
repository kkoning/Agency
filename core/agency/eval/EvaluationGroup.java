package agency.eval;

import java.io.Serializable;
import java.util.AbstractMap.SimpleEntry;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.UUID;
import java.util.stream.Stream;

import agency.Agent;
import agency.AgentModel;
import agency.Fitness;
import agency.Individual;
import agency.data.AgencyData;

public class EvaluationGroup implements Serializable, Runnable {
private static final long serialVersionUID = 1922150637452370643L;

UUID                                      id     = UUID.randomUUID();
Map<Agent<? extends Individual>, Fitness> agents = new IdentityHashMap<>();

AgentModel model;

// Model data outputs
Object summaryData;
Map<Integer, AgencyData> perStepData = new TreeMap<>();


public Object getSummaryData() {
  return summaryData;
}

public Map<Integer, AgencyData> getPerStepData() {
  return perStepData;
}

public Stream<Entry<Individual, Fitness>> getResults() {
  return agents.entrySet()
          .stream()
          .map(ent -> new SimpleEntry<Individual, Fitness>(ent.getKey().getManager(), ent.getValue()));
}

public void addAgent(Agent<? extends Individual> agent) {
  agents.put(agent, null);
}

@Override
public void run() {
  // Associate the Agents with the model
  for (Agent<? extends Individual> agent : agents.keySet()) {
    model.addAgent(agent);
    agent.setModel(model);
  }

  // Actually run the agent model
  int maxSteps = model.getMaxSteps();
  for (int step = 0; step < maxSteps; step++) {

    // Per-step data; if reported
    AgencyData ad = model.getStepData();
    if (ad != null)
      perStepData.put(step, ad);

    boolean doneEarly = model.step();
    if (doneEarly)
      break;
  }

  // Get summary data
  summaryData = model.getSummaryData();

  for (Agent<? extends Individual> agent : agents.keySet()) {
    updateFitness(agent, model.getFitness(agent));
  }

}

void updateFitness(Agent<? extends Individual> agent, Fitness fitness) {
  if (agents.containsKey(agent))
    agents.put(agent, fitness);
  else
    throw new RuntimeException("Trying to update fitness of agent that does not exist in this EvaluationGroup");
}

@Override
public String toString() {
  StringBuffer sb = new StringBuffer();
  sb.append("EvaluationGroup@" + this.hashCode() + ":[");
  for (Entry<Agent<? extends Individual>, Fitness> entry : agents.entrySet()) {
    String fitnessDescription;
    Fitness fit = entry.getValue();
    if (fit != null)
      fitnessDescription = fit.toString();
    else
      fitnessDescription = "No Fitness";
    sb.append("\n" + entry.getKey() + "->" + fitnessDescription + ",");
  }
  sb.deleteCharAt(sb.length() - 1);
  sb.append("]\n");
  return sb.toString();
}

public AgentModel getModel() {
  return model;
}

public void setModel(AgentModel model) {
  this.model = model;
}

public UUID getId() {
  return id;
}


}
