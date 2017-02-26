package agency;

import java.util.Optional;

public class NullAgent implements Agent {
public static final long serialVersionUID = 1L;

Individual ind;
AgentModel model;

@Override
public String toString() {
  return "NullAgent[" + getManager().toString() + "]";
}

@Override
public void init() {
  // Do nothing
}

@Override
public void step(AgentModel model, int step, Optional substep) {
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

}
