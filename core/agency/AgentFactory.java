package agency;

public interface AgentFactory extends XMLConfigurable {
Agent createAgent(Individual ind);
}
