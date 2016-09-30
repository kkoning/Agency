package agency;

public interface AgentModelFactory<T extends AgentModel> extends XMLConfigurable {
T createAgentModel();
}
