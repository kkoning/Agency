package agency;

import java.util.Optional;

public class NullAgent implements Agent<Individual,AgentModel> {
public static final long serialVersionUID = 1L;

Fitness fitness;
Individual ind;
AgentModel model;

@Override
public void init() {
  // No init required
}

@Override
public void step(AgentModel model, int step, Optional<Double> substep) {
  // Do nothing
}

@Override
public Individual getManager() {
  return ind;
}
@Override
public void setManager(Individual ind) {
  this.ind = ind;
}

@Override
public void setModel(AgentModel model) {
  this.model = model;
}

@Override
public AgentModel getModel() {
  return model;
}

@Override
public Fitness getFitness() {
  return fitness;
}

public void setFitness(Fitness fitness) {
  this.fitness = fitness;
}



}
