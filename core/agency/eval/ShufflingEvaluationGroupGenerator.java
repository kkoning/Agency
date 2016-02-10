package agency.eval;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;
import agency.Agent;
import agency.AgentModelFactory;
import agency.Environment;
import agency.Individual;

public class ShufflingEvaluationGroupGenerator {
  List<Agent<?>>       agents;
  int                  position = 0;
  int                  groupSize;
  AgentModelFactory<?> amf;

  ShufflingEvaluationGroupGenerator(List<Agent<? extends Individual>> agentList,
      int groupSize, AgentModelFactory<?> amf) {
    this.groupSize = groupSize;
    this.amf = amf;
    this.agents = new ArrayList<>(agentList.size());
    this.agents.addAll(agentList);
    Collections.shuffle(this.agents);
  }

  EvaluationGroup generate() {
    EvaluationGroup eg = new EvaluationGroup();
    eg.setModel(amf.createAgentModel());
    for (int i = 0; i < groupSize; i++) {
      if (position == agents.size()) {
        Collections.shuffle(agents);
        position = 0;
      }
      eg.addAgent(agents.get(position));
      position++;
    }
    return eg;
  }

}
