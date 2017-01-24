package agency.eval;

import java.io.Serializable;
import java.util.*;

import agency.Agent;
import agency.AgentModel;
import agency.Fitness;
import agency.Individual;
import agency.data.AgencyData;

public class EvaluationGroup
        implements Serializable, Runnable {
public static final long serialVersionUID = 1L;

UUID        id     = UUID.randomUUID();
List<Agent> agents = new ArrayList<>();

AgentModel model;

// Model data outputs
Object summaryData;
Map<Integer, Object> perStepData = new TreeMap<>();
boolean              finished    = false;
Map<Individual, Fitness>  results = new IdentityHashMap<>();

int generation;

public Object getSummaryData() {
  return summaryData;
}

public Map<Integer, Object> getPerStepData() {
  return perStepData;
}

public Map<Individual, Fitness> getResults() { return results; }

public void collectResults() {
  for (Agent agent : agents) {
    results.put(agent.getManager(), model.getFitness(agent));
  }
}

public void addAgent(Agent<? extends Individual> agent) {
  agents.add(agent);
}

@Override
public void run() {

  if (finished)
    throw new RuntimeException("Cannot run an EvaluationGroup twice.");

  // Associate the Agents with the model and vice versa
  for (Agent agent : agents) {
    model.addAgent(agent);
    agent.setModel(model);
  }

  model.init();

  // Actually run the agent model
  int maxSteps = model.getMaxSteps();
  for (int step = 0; step < maxSteps; step++) {

    // Per-step data; if reported
    Object data = model.getStepData();
    if (data != null)
      perStepData.put(step, data);

    boolean doneEarly = model.step();
    if (doneEarly)
      break;
  }

  model.finish();

  // Get summary data
  summaryData = model.getSummaryData();

  // Mark as finished to prevent accidental re-execution of model.
  finished = true;

  // collect the fitness results
  collectResults();

  // Allow agent model to be garbage collected.
  model = null;

}

@Override
public String toString() {
  StringBuffer sb = new StringBuffer();
  sb.append("EvaluationGroup#" + this.hashCode() + ":[");
  for (Agent agent : agents) {
    String fitnessDescription;
    Fitness fit = model.getFitness(agent);
    if (fit != null)
      fitnessDescription = fit.toString();
    else
      fitnessDescription = "No Fitness";
    sb.append("\n" + agent + "->" + fitnessDescription + ",");
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
