package agency;

public interface AgentModel {
  // Agent Models evaluate agents...

  /**
   * An agent model must be populated by agents, which, in this system, come
   * from the evolving population. The agent model must accept these agents and
   * add them to the simulation. A typical instance of this function will
   * involve a series of if/else statements checking the agent class with the
   * "instanceof" operator.
   * 
   * @param agent
   *          The agent to add
   */
  public void addAgent(Agent<? extends Individual> agent);

  /**
   * In order for the evolutionary system to function, agent models must assign
   * a fitness to each agent.
   * 
   * @param agent
   * @return
   */
  public Fitness getFitness(Agent<? extends Individual> agent);

  // Evaluators need to do the stepping, since they might pull data
  // at each step.
  /**
   * Steps the model.  This function is typically called by an evaluator, which may 
   * also collect data between steps.
   * 
   * @return true if this model must be stepped again to complete analysis,
   *         false otherwise.
   */
  public boolean step();

  /**
   * @return The maximum number of times that an evaluator should step this
   *         model.
   */
  public int getMaxSteps();

  // Interface for evaluators to pull data from agent models
  /**
   * An object that contains summary data for this agent model. This should be
   * structured as a java object; reflection will be used to extract the
   * property variable names to use as data file headers.
   * 
   * @return An object that contains summary data for this agent model.
   */
  public Object getSummaryData();

  /**
   * An object that contains per-step data for this agent model. This should be
   * structured as a java object; reflection will be used to extract the
   * property variable names to use as data file headers.
   * 
   * @return An object that contains per-step data for this agent model.
   */
  public Object getStepData();
}
