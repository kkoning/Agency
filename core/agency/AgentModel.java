package agency;

import agency.data.AgencyData;

import java.io.PrintStream;

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
 *         The agent to add
 */
void addAgent(Agent agent);

/**
 * In order for the evolutionary system to function, agent models must assign
 * a fitness to each agent.
 *
 * @param agent
 * @return
 */
Fitness getFitness(Agent agent);

/**
 * In many cases it will be useful to have more detail about an agent's
 * performance than the composite fitness metric.  For understanding the
 * simulation dynamics, it can be useful to observe some of the components of
 * that fitness, or actions that lead to it.  This may also be useful in
 * combination with the generation of fitness landscapes, in cases where the
 * behavior is not specified directly by the genome itself.  (E.g., an
 * offered price depends on information about the market environment, rather
 * than being directly encoded into a genome).
 *
 * @param agent
 * @return
 */
Object getAgentDetails(Agent agent);

/**
 * This function is called after all agents have been added to the model,
 * but before an evaluator starts stepping it. As the name implies, this gives
 * agent models an opportunity to perform any initialization steps needed
 * before step() is called.
 */
void init();

/**
 * Steps the model.  This function is typically called by an evaluator, which may
 * also collect data between steps.
 *
 * @return false if the simulation should be terminated before maxSteps, true
 * otherwise.
 */
boolean step();

/**
 * This function is called after all steps have been completed, but before
 * fitness evaluation takes place.  This allows agent models to clean up
 * after themselves (e.g., if any resources were obtained during
 * initialization that must now be released) and make any adjustments to
 * agent fitness before it is collected for use by the evolutionary algorithm.
 */
void finish();

/**
 * @return The maximum number of times that an evaluator should step this model.
 */
int getMaxSteps();

// Interface for evaluators to pull data from agent models

/**
 * An object that contains summary data for this agent model. This should be
 * structured as a java object; reflection will be used to extract the
 * property variable names to use as data file headers.  Note that <b>only
 * public fields will be written!</b>
 *
 * @return An object that contains summary data for this agent model.
 */
Object getSummaryData();

/**
 * An object that contains per-step data for this agent model. This should be
 * structured as a java object; reflection will be used to extract the
 * property variable names to use as data file headers.
 *
 * @return An object that contains per-step data for this agent model.
 */
Object getStepData();

/**
 * This function is called before the start of an agent model, and indicates
 * that it should print a detailed log of its operation to the provided
 * PrintStream.  Actually providing an implmentation of this function
 * is optional; it can be ignored, though, of course, the debugging functionality
 * it would otherwise provide will not be available.
 *
 * @param out The PrintStream that the debugging log should be written to.
 */
void enableDebug(PrintStream out);

}
