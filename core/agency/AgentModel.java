package agency;

public interface AgentModel extends Runnable {
	public void addAgent(Agent<? extends Individual> agent);
	public Fitness getFitness(Agent<? extends Individual> agent);
}
