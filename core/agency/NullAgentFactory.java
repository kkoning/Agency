package agency;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class NullAgentFactory
        implements AgentFactory {
Individual ind;

@Override
public Agent<? extends Individual> createAgent(Individual ind) {
  NullAgent na = new NullAgent();
  na.setManager(ind);
  return na;
}

@Override
public void readXMLConfig(Element e) {
  // No configuration necessary; agent has no behavior/memory
}

@Override
public void writeXMLConfig(Element e) {
  // No configuration necessary; agent has no behavior/memory
}

@Override
public void resumeFromCheckpoint() {
  // No configuration necessary; agent has no behavior/memory
}


}
