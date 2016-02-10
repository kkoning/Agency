package agency;

public class NullAgent implements Agent<Individual> {
	Individual ind;

	@Override
	public Individual getIndividual() {
		return ind;
	}

	@Override
	public void setIndividual(Individual ind) {
		this.ind = ind;
	}

	@Override
	public String toString() {
		return "NullAgent[" + getIndividual().toString() + "]";
	}
	
	
	
}
