package agency.eval;

import java.io.Serializable;
import java.util.AbstractMap.SimpleEntry;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.stream.Stream;

import agency.Agent;
import agency.AgentModel;
import agency.Fitness;
import agency.Individual;

public class EvaluationGroup implements Serializable, Runnable {
	private static final long serialVersionUID = 1922150637452370643L;

	UUID id;
	Map<Agent<? extends Individual>, Fitness> agents = new IdentityHashMap<>();

	AgentModel model;
	

	public Stream<Entry<Individual, Fitness>> getResults() {
		return agents.entrySet()
				.stream()
				.map(ent -> new SimpleEntry<Individual,Fitness>(ent.getKey().getIndividual(), ent.getValue()));
	}

	public void addAgent(Agent<? extends Individual> agent) {
		agents.put(agent, null);
	}

	void updateFitness(Agent<? extends Individual> agent, Fitness fitness) {
		if (agents.containsKey(agent))
			agents.put(agent, fitness);
		else
			throw new RuntimeException("Trying to update fitness of agent that does not exist in this EvaluationGroup");
	}

	@Override
	public void run() {
		for (Agent<? extends Individual> agent : agents.keySet())
			model.addAgent(agent);

		model.run();

		for (Agent<? extends Individual> agent : agents.keySet()) {
			updateFitness(agent, model.getFitness(agent));
		}

	}

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("EvaluationGroup@" + this.hashCode() + ":[");
		for (Entry<Agent<? extends Individual>, Fitness> entry : agents.entrySet()) {
			String fitnessDescription;
			Fitness fit = entry.getValue();
			if (fit != null)
				fitnessDescription = fit.toString();
			else
				fitnessDescription = "No Fitness";
			sb.append("\n" + entry.getKey() + "->" + fitnessDescription + ",");
		}
		sb.deleteCharAt(sb.length() - 1);
		sb.append("]\n");
		return sb.toString();
	}
	
	public AgentModel getModel() {
		return model;
	}

	public void setModel(AgentModel model) {
		this.model = model;
	}
	
	public UUID getId() {
		return id;
	}


}
