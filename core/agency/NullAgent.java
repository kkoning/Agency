package agency;

public class NullAgent implements Agent<Individual> {
	Individual ind;
	AgentModel model;

	@Override
	public Individual getManager() {
		return ind;
	}

	@Override
	public void setManager(Individual ind) {
		this.ind = ind;
	}

	@Override
	public String toString() {
		return "NullAgent[" + getManager().toString() + "]";
	}

  @Override
  public void setModel(AgentModel model) {
    this.model = model;
  }
	
}
