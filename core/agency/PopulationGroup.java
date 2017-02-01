package agency;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;
import java.util.stream.Stream;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class PopulationGroup
        implements Serializable, XMLConfigurable {
public static final long serialVersionUID = 1L;


String           id;
List<Population> populations;
Integer          totalPopulationSize;

public PopulationGroup() {
  populations = new ArrayList<>();
  id = UUID.randomUUID().toString();
}

@Override
public void readXMLConfig(Element e) {

  String idString = e.getAttribute("id");
  if (idString != null)
    if (!idString.equalsIgnoreCase(""))
      id = idString;

  try {
    totalPopulationSize = Integer.parseInt(e.getAttribute("totalSize"));
  } catch (NumberFormatException nfe) {
    throw new UnsupportedOperationException(
            "PopulationGroup must have a totalSize=<Integer>");
  }


  NodeList nl = e.getChildNodes();
  for (int i = 0; i < nl.getLength(); i++) {
    Node node = nl.item(i);
    if (node instanceof Element) {
      Element child = (Element) node;
      XMLConfigurable xc = Config.initializeXMLConfigurable(child);

      if (xc instanceof Population) {
        populations.add((Population) xc);
      } else {
        throw new UnsupportedOperationException(
                "Unrecognized element in Population");
      }

    }
  }

}

@Override
public void writeXMLConfig(Element e) {
  Document d = e.getOwnerDocument();
  e.setAttribute("id", id);
  e.setAttribute("totalSize", totalPopulationSize.toString());
  for (Population pop : populations) {
    Element subPop = Config.createUnnamedElement(d, pop);
    e.appendChild(subPop);
  }

}

@Override
public void resumeFromCheckpoint() {
  populations.forEach(Population::resumeFromCheckpoint);
}

/**
 * Manually add a population to this population group.
 *
 * @param population
 */
public void addPopulation(Population population) {
  this.populations.add(population);
}

/**
 * Reproduces each of the Populations contained in this PopulationGroup.
 * Currently does not balance population sizes based on fitness.
 *
 * TODO: Code for balancing population sizes based on fitness.
 *
 * @param env The environment (
 */
public void reproduce(Environment env) {
  for (Population pop : populations) {
    pop.reproduce(env);
  }
}

public List<Population> getPopulations() {
  return populations;
}

public Optional<Population> getPopulation(String popID) {
  Population toReturn = null;
  for (Population p : populations) {
    if (p.getId().equals(popID)) {
      if (toReturn != null)
        throw new RuntimeException("Population Group " + getId() +
                                   " has two populations with id = " + popID);
      else {
        toReturn = p;
        break;
      }

    }
  }
  return Optional.of(toReturn);
}

public String getId() {
  return id;
}

public void close() {
  for (Population pop : populations) {
    pop.close();
  }
}


}
