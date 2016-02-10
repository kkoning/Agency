package agency;

public interface Agent<T extends Individual> {
	
	public T getIndividual();
	public void setIndividual(T ind);
}
