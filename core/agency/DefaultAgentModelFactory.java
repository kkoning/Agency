package agency;

import java.lang.reflect.Field;
import java.nio.channels.UnsupportedAddressTypeException;
import java.util.IdentityHashMap;
import java.util.Map;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class DefaultAgentModelFactory implements AgentModelFactory {

  Class<? extends AgentModel> agentModelClass;
  Map<Field, Object>          parameters;

  public DefaultAgentModelFactory() {
    super();
    parameters = new IdentityHashMap<>();
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

    // Set parameters
    for (Map.Entry<Field, Object> entry : parameters.entrySet()) {
      Field f = entry.getKey();
      Object v = entry.getValue();
      try {
        f.set(model, v);
      } catch (IllegalArgumentException | IllegalAccessException e) {
        throw new UnsupportedOperationException("Cannot set parameter " + f.getName() + ".  Is it accessible?", e);
      }
      
      
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
      throw new RuntimeException("Agent Model class " + className + " not found.  Check classpath?");
    }

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
        Field f = null;
        if (nameString == null)
          throw new UnsupportedOperationException("Parameter must specify a name=\"<id>\" which matches a field in a the AgentModel class");

        try {
          f = modelClass.getDeclaredField(nameString);
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
    agentModelClass = (Class<? extends AgentModel>) modelClass;
  }

  @Override
  public void writeXMLConfig(Document d, Element e) {
    e.setAttribute("modelClass", agentModelClass.getCanonicalName());
    
    for (Map.Entry<Field, Object> entry : parameters.entrySet()) {
      Element childE = d.createElement("Parameter");
      childE.setAttribute("name", entry.getKey().getName());
      childE.setAttribute("value", entry.getValue().toString());
      e.appendChild(childE);
    }

    
    
  }

}
