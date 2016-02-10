package agency;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class DefaultAgentModelFactory implements AgentModelFactory {

  static {
    Config.registerClassXMLTag(DefaultAgentModelFactory.class);
  }

  Class<? extends AgentModel> agentModelClass;

  public DefaultAgentModelFactory() {
    super();
  }

  public DefaultAgentModelFactory(Class<? extends AgentModel> agentModelClass) {
    this.agentModelClass = agentModelClass;
  }

  @Override
  public AgentModel createAgentModel() {
    AgentModel model;
    try {
      model = agentModelClass.newInstance();
    } catch (InstantiationException | IllegalAccessException e) {
      throw new RuntimeException(e);
    }
    return model;
  }

  @SuppressWarnings("unchecked")
  @Override
  public void readXMLConfig(Element e) {
    String className = e.getAttribute("modelClass");
    Class<?> modelClass = null;
    try {
      modelClass = Class.forName(className);
    } catch (ClassNotFoundException e1) {
      throw new RuntimeException(
          "Agent Model class " + className + " not found.  Check classpath?");
    }
    if (!modelClass.isAssignableFrom(AgentModel.class))
      throw new IllegalArgumentException(
          "Agent model class must implement AgentModel interface");
    agentModelClass = (Class<? extends AgentModel>) modelClass;
  }

  @Override
  public void writeXMLConfig(Document d, Element e) {
    e.setAttribute("modelClass", agentModelClass.getCanonicalName());
  }

}
