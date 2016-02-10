package agency;

public interface AgentFactory extends XMLConfigurable {
	Agent<? extends Individual> createAgent(Individual ind);
}
