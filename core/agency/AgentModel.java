package agency;

public interface AgentModel {
	// Agent Models evaluate agents...
  public void addAgent(Agent<? extends Individual> agent);
	public Fitness getFitness(Agent<? extends Individual> agent);
	
	// Evaluators need to do the stepping, since they might pull data 
	// at each step.
	/**
	 * Steps the model.
	 * 
	 * @return true if this model must be stepped again to complete analysis,
	 *  false otherwise.
	 */
	public boolean step();
	public int getMaxSteps();
	
	
	
	// Interface for evaluators to pull data from agent models
	public Object getSummaryData();
	public Object getStepData();
}
