package agency;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class DefaultAgentModelFactory implements AgentModelFactory {

String              className;
Map<String, String> parameterStrings;

transient Class<? extends AgentModel> agentModelClass;
transient Map<Field, Object>          parameters;

public DefaultAgentModelFactory() {
  super();
  parameterStrings = new HashMap<>();
}

public DefaultAgentModelFactory(Class<? extends AgentModel> agentModelClass) {
  this.agentModelClass = agentModelClass;
}

@SuppressWarnings("unchecked")
@Override
public void readXMLConfig(Element e) {
  className = e.getAttribute("modelClass");

  NodeList nl = e.getChildNodes();
  for (int i = 0; i < nl.getLength(); i++) {
    Node node = nl.item(i);
    if (node instanceof Element) {
      Element child = (Element) node;

      String elementName = child.getTagName();
      if (elementName == null)
        throw new UnsupportedOperationException("Null element name for tag in a DefaultAgentModelFactory");
      if (!elementName.equalsIgnoreCase("Parameter"))
        throw new UnsupportedOperationException("DefaultAgentModelFactory must have only Parameter child elements");

      // Find the field
      String nameString = child.getAttribute("name");
      if (nameString == null)
        throw new UnsupportedOperationException("Parameter must specify a name=\"<id>\" which matches a field in a the AgentModel class");

      String valueString = child.getAttribute("value");
      if (valueString == null)
        throw new UnsupportedOperationException("Parameter must specify a value=\"<foo>\"");

      parameterStrings.put(nameString, valueString);

    }
  }

  initModelClass();
  initParameters();
}

private void initModelClass() {
  Class<?> modelClass = null;
  try {
    modelClass = Class.forName(className);
  } catch (ClassNotFoundException e1) {
    throw new RuntimeException("Agent Model class " + className + " not found.  Check classpath?");
  }

  // TODO: fix! add a check
  // if (!modelClass. isAssignableFrom(AgentModel.class))
  // throw new IllegalArgumentException(
  // "Agent model class must implement AgentModel interface");
  agentModelClass = (Class<? extends AgentModel>) modelClass;

}

private void initParameters() {

  /*
   * The strings from the configuration are stored in the parameterStrings map.  Reflection is
   * needed to detect these fields so that they can be set.  This function does that, and saves
   * the reflected Field objects in the Map<Field,Object> parameters.  Since this map cannot
   * be saved between checkpoints (the Field class is not serializable), this function must
   * be called again in the case of restarting from a checkpoint.
   *
   * Must be called _after_ initModelClass().
   */
  parameters = new IdentityHashMap<>();
  for (Map.Entry<String, String> parameterString : parameterStrings.entrySet()) {
    Field f;
    try {
      f = agentModelClass.getDeclaredField(parameterString.getKey());
      f.setAccessible(true);
    } catch (NoSuchFieldException e1) {
      throw new UnsupportedOperationException(
              "Parameter \"" + parameterString.getKey() +
                      "\" does not appear to be the name of a field in the specified AgentModel class");
    } catch (SecurityException e1) {
      throw new UnsupportedOperationException(
              "Parameter \"" + parameterString.getKey() +
                      "\" corresponds to a field in an AgentModel class which threw a SecurityException", e1);
    }

    // Parse the value of the parameter consistently with the type of the target field.
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
public void writeXMLConfig(Element e) {
  Document d = e.getOwnerDocument();
  e.setAttribute("modelClass", agentModelClass.getCanonicalName());

  for (Map.Entry<String, String> entry : parameterStrings.entrySet()) {
    Element childE = d.createElement("Parameter");
    childE.setAttribute("name", entry.getKey());
    childE.setAttribute("value", entry.getValue());
    e.appendChild(childE);
  }

}

@Override
public void resumeFromCheckpoint() {
  /*
   * Re-initialize values that could not be serialized.
   */
  initModelClass();
  initParameters();
}

@Override
public AgentModel createAgentModel() {
  /*
   * First, create a new instance of the agent model
   */
  AgentModel model;
  try {
    model = agentModelClass.newInstance();
  } catch (InstantiationException | IllegalAccessException e) {
    throw new RuntimeException(
            "Cannot instantiate AgentModel of class " + agentModelClass.getCanonicalName() +
                    ".  Ensure there is a no-argument constructor?", e);
  }

  /*
   * Next, set the parameters from the configuration file using the reflected / saved values.
   */
  for (Map.Entry<Field, Object> entry : parameters.entrySet()) {
    Field f = entry.getKey();
    Object v = entry.getValue();
    try {
      f.set(model, v);
    } catch (IllegalArgumentException | IllegalAccessException e) {
      throw new UnsupportedOperationException("Cannot set parameter " + f.getName() + ".  Is it accessible?", e);
    }

  }

  /*
   * Populating the model with agents is performed by the EvaluationGroup.
   */
  return model;
}

@Override
public void close() {
  // Unnecessary
}


}
