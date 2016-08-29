package agency;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class NullAgentFactory implements AgentFactory {
  
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
  public void writeXMLConfig(Document d, Element e) {
    // No configuration necessary; agent has no behavior/memory
  }

}
