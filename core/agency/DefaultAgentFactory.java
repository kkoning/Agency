package agency;

import java.lang.reflect.Field;
import java.util.IdentityHashMap;
import java.util.Map;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class DefaultAgentFactory implements AgentFactory {

Class<? extends Agent> agentClass;
Map<Field, Object>     parameters;

public DefaultAgentFactory() {
  super();
  parameters = new IdentityHashMap<>();
}

@Override
public Agent<? extends Individual> createAgent(Individual ind) {
  Agent agent;
  try {
    agent = agentClass.newInstance();
  } catch (InstantiationException | IllegalAccessException e) {
    throw new RuntimeException(
            "Cannot instantiate agent of class " + agentClass.getCanonicalName() + ".  Ensure there is a no-argument constructor?", e);
  }

  // Set parameters
  for (Map.Entry<Field, Object> entry : parameters.entrySet()) {
    Field f = entry.getKey();
    Object v = entry.getValue();
    try {
      f.set(agent, v);
    } catch (IllegalArgumentException | IllegalAccessException e) {
      throw new UnsupportedOperationException("Cannot set parameter " + f.getName() + ".  Is it accessible?", e);
    }

  }

  // Set individual
  agent.setManager((Individual) ind);

  return agent;
}

@Override
public void readXMLConfig(Element e) {
  String className = e.getAttribute("agentClass");
  Class<?> agentClass = null;
  try {
    agentClass = Class.forName(className);
  } catch (ClassNotFoundException e1) {
    throw new RuntimeException("Agent class " + className + " not found.  Check classpath?");
  }

  NodeList nl = e.getChildNodes();
  for (int i = 0; i < nl.getLength(); i++) {
    Node node = nl.item(i);
    if (node instanceof Element) {
      Element child = (Element) node;

      String elementName = child.getTagName();
      if (elementName == null)
        throw new UnsupportedOperationException("Null element name for tag in a DefaultAgentFactory");
      if (!elementName.equalsIgnoreCase("Parameter"))
        throw new UnsupportedOperationException("DefaultAgentFactory must have only Parameter child elements");

      // Find the field
      String nameString = child.getAttribute("name");
      Field f = null;
      if (nameString == null)
        throw new UnsupportedOperationException("Parameter must specify a name=\"<id>\" which matches a field in a the Agent class");

      try {
        f = agentClass.getField(nameString);
        f.setAccessible(true);
      } catch (NoSuchFieldException e1) {
        throw new UnsupportedOperationException(
                "Parameter \"" + nameString + "\" does not appear to be the name of a field in the specified AgentModel class");
      } catch (SecurityException e1) {
        throw new UnsupportedOperationException(
                "Parameter \"" + nameString + "\" corresponds to a field in an AgentModel class which threw a SecurityException", e1);
      }

      String valueString = child.getAttribute("value");
      if (valueString == null)
        throw new UnsupportedOperationException("Parameter must specify a value=\"<foo>\"");

      Class<?> fieldClass = f.getType();
      if (fieldClass.equals(Double.class)) {
        parameters.put(f, Double.parseDouble(valueString));
      } else if (fieldClass.equals(Integer.class)) {
        parameters.put(f, Integer.parseInt(valueString));
      } else if (fieldClass.equals(Boolean.class)) {
        parameters.put(f, Boolean.parseBoolean(valueString));
      } else {
        throw new UnsupportedOperationException(
                this.getClass().getName() + " only supports parameter types of Double, Integer, and Boolean");
      }

    }
  }

  // TODO: fix! add a check
  // if (!modelClass. isAssignableFrom(AgentModel.class))
  // throw new IllegalArgumentException(
  // "Agent model class must implement AgentModel interface");
  this.agentClass = (Class<? extends Agent>) agentClass;

}

@Override
public void writeXMLConfig(Element e) {
  Document d = e.getOwnerDocument();
  e.setAttribute("agentClass", agentClass.getCanonicalName());

  for (Map.Entry<Field, Object> entry : parameters.entrySet()) {
    Element childE = d.createElement("Parameter");
    childE.setAttribute("name", entry.getKey().getName());
    childE.setAttribute("value", entry.getValue().toString());
    e.appendChild(childE);
  }

}

@Override
public void resumeFromCheckpoint() {

}

}
