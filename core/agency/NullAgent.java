package agency;

public class NullAgent implements Agent<Individual> {
public static final long serialVersionUID = 1L;

Individual ind;
AgentModel model;

@Override
public String toString() {
  return "NullAgent[" + getManager().toString() + "]";
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
