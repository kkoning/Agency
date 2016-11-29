package agency;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class DefaultAgentFactory implements AgentFactory {

String              agentClassName;
Map<String, String> parameterStrings;

transient Class<? extends Agent> agentClass;
transient Map<Field, Object>     parameters;

public DefaultAgentFactory() {
  super();
  parameterStrings = new HashMap<>();
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

private void initReflection() {
  parameters = new IdentityHashMap<>();
  agentClass = null;

  try {
    agentClass = (Class<? extends Agent>) Class.forName(agentClassName);
  } catch (ClassNotFoundException e1) {
    throw new RuntimeException("Agent class " + agentClassName + " not found.  Check classpath?");
  }

  for (Map.Entry<String, String> parameterString : parameterStrings.entrySet()) {
    Field f = null;
    try {
      f = agentClass.getField(parameterString.getKey());
      f.setAccessible(true);
    } catch (NoSuchFieldException e1) {
      throw new UnsupportedOperationException(
              "Parameter \"" + parameterString.getKey() + "\" does not appear to be the name of a field in the specified AgentModel class");
    } catch (SecurityException e1) {
      throw new UnsupportedOperationException(
              "Parameter \"" + parameterString.getKey() + "\" corresponds to a field in an AgentModel class which threw a SecurityException", e1);
    }

    Class<?> fieldClass = f.getType();
    if (fieldClass.equals(Double.class)) {
      parameters.put(f, Double.parseDouble(parameterString.getValue()));
    } else if (fieldClass.equals(Integer.class)) {
      parameters.put(f, Integer.parseInt(parameterString.getValue()));
    } else if (fieldClass.equals(Boolean.class)) {
      parameters.put(f, Boolean.parseBoolean(parameterString.getValue()));
    } else {
      throw new UnsupportedOperationException(
              this.getClass().getName() + " only supports parameter types of Double, Integer, and Boolean");
    }
  }
}

@Override
public void readXMLConfig(Element e) {
  agentClassName = e.getAttribute("agentClass");

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

      /*
       * Read the name/value strings and store in Map<String,String> parameterStrings for later reflection
       * by initReflection().
       */
      String nameString = child.getAttribute("name");
      if (nameString == null)
        throw new UnsupportedOperationException("Parameter must specify a name=\"<id>\" which matches a field in a the Agent class");
      String valueString = child.getAttribute("value");
      if (valueString == null)
        throw new UnsupportedOperationException("Parameter must specify a value=\"<foo>\"");
      parameterStrings.put(nameString,valueString);

    }
  }

  initReflection();
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
  initReflection();
}

}
