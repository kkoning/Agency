package agency;

import java.util.Optional;

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
 * <p>
 * One way to think of this is of an Agent as like a firm, and an Individual is
 * like the CEO of that firm.
 *
 * @param <T>
 * @author kkoning
 */
public interface Agent<N extends Individual, M extends AgentModel> {

void init();

void step(M model, int step, Optional<Double> substep);

N getManager();

void setManager(N ind);

void setModel(M model);

}
