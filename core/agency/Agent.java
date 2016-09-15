package agency;

/**
 * An agent in a simulation is conceptually separate from an individual in an
 * evolving population. The agent can have state which is specific to an
 * instance of an agent model, while an individual's state is tied to the
 * evolutionary system as a whole. For example, an agent may have an account
 * balance tied to its actions within a simulation, whereas an individual may be
 * participating in several agent simulations at once--if state were stored in
 * the instance of individual, that state would be shared across several agent
 * simulations simultaneously. Instead, it is important that each agent model be
 * independent.
 * 
 * One way to think of this is of an Agent as like a firm, and an Individual is
 * like the CEO of that firm.
 * 
 * @author kkoning
 *
 * @param <T>
 */
public interface Agent<T extends Individual> {
  public T getManager();
  public void setManager(T ind);
  public void setModel(AgentModel model);
}
