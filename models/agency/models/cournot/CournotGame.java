package agency.models.cournot;

import java.util.ArrayList;
import java.util.List;

import agency.AbstractAgentModel;
import agency.Agent;
import agency.Fitness;
import agency.SimpleFitness;

public class CournotGame extends AbstractAgentModel {

// The CournotMarketInfo can be a 1:1 relationship, and kept.
CournotMarketInfo cmi;

List<CournotAgent> agents;
double[][]         quantities;
double[]           prices;
double[][]         revenue;
double[]           consumerSurplus;

/*
 * The following are model parameters.
 */
Double demandConstant    = Double.NaN;
Double demandCoefficient = Double.NaN;
Double minimumQuantity   = Double.NaN;

public CournotGame() {
  agents = new ArrayList<>();
}

@Override
public void init() {
  super.init();

  quantities = new double[maxSteps][agents.size()];
  revenue = new double[maxSteps][agents.size()];
  prices = new double[maxSteps];
  consumerSurplus = new double[maxSteps];

  if (Double.isNaN(demandConstant))
    throw new RuntimeException("demandConstant is NaN.  Has it been set?");
  if (Double.isNaN(demandCoefficient))
    throw new RuntimeException("demandCoefficient is NaN.  Has it been set?");

  cmi = new CournotMarketInfo(this);
}

@Override
public void addAgent(Agent<?, ?> agent) {
  if (agent instanceof CournotAgent)
    agents.add((CournotAgent) agent);
  else
    throw new RuntimeException("CournotGame only accepts CournotAgents");

}

@Override
public Fitness getFitness(Agent<?, ?> agent) {
  int agentIdx = agents.indexOf(agent);
  double totalRevenue = 0d;
  for (int i = 0; i < maxSteps; i++)
    totalRevenue += revenue[i][agentIdx];

  return new SimpleFitness(totalRevenue);
}

@Override
public Object getAgentDetails(Agent<?, ?> agent) {
  // TODO: For now, no agent details.
  return null;
}

@Override
public boolean step() {

  // Get the quantities for each agent
  double totalQuantity = 0d;
  for (int i = 0; i < agents.size(); i++) {
    double desiredQuantity = agents.get(i).produceQty(cmi);
    if (desiredQuantity < minimumQuantity)
      quantities[currentStep][i] = minimumQuantity;
     else
      quantities[currentStep][i] = desiredQuantity;

    totalQuantity += quantities[currentStep][i];
  }

  // Compute the price
  prices[currentStep] = demandConstant - demandCoefficient * totalQuantity;
  if (prices[currentStep] <= 0)
    prices[currentStep] = 0;

  // Compute consumer Surplus
  consumerSurplus[currentStep] = (demandConstant - prices[currentStep]) * totalQuantity / 2;
  
  // Generate revenue
  for (int i = 0; i < agents.size(); i++) {
    revenue[currentStep][i] = prices[currentStep] * quantities[currentStep][i];
  }
  
  this.currentStep++;
  
  return false;
}

@Override
public void finish() {
  // Nothing Required
}

@Override
public Object getSummaryData() {
  return new CournotSummaryData(this);
}

@Override
public Object getStepData() {
  // Return nothing for now.
  return null;
}

}
