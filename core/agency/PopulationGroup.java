package agency;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;
import java.util.stream.Stream;

import agency.reproduce.PopulationBalancer;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class PopulationGroup
        implements Serializable, XMLConfigurable {
public static final long   serialVersionUID                       = 1L;
public static final double DEFAULT_TARGET_POPULATION_SIZE_INERTIA = 0.9d;

/**
 A unique name for this PopulationGroup, for reference elsewhere.  (e.g., a
 fitness landscape generator, loading a checkpoint file)
 */
String id;

/**
 A list of the populations contained in this population group.
 */
List<Population> populations;

/**
 The number of individuals that the reproduction system should generate for
 each population, (rounded to the nearest integer, of course).  These numbers
 are determined by a combination of fitness evaluation/comparison (e.g.,
 tournament selection across all populations) and a weighting of the targets
 from the previous generation
 */
List<Double> targetPopulationSizes;

/**
 If a PopulationBalancer is specified in the configuration, it will be used
 to scale population sizes for the next generation, subject to
 populationSizeInertia.
 */
PopulationBalancer balancer;

/**
 The selection process between populations will result in a number of
 individuals a population should contain.  However, to prevent large
 immediate swings (e.g., a population going all but extinct after the first
 generation), some inertia is added to existing population targets.  For
 example, if populationSizeInertia is 0.5, then the targetPopulationSizes
 will be an average of the targetPopulationSizes from the previous generation
 and the targetPopulationSizes suggested by, e.g., cross-population
 tournament selection.  A value of 0.9 would more strongly weigh the
 targetPopulationSizes from the previous generation, 0 would ignore them
 completely, etc...
 <p>
 The default value of 0.9 is specified in
 DEFAULT_TARGET_POPULATION_SIZE_INERTIA.
 */
double populationSizeInertia;


/**
 The target number of _total_ individuals in the population group.  The sum
 of the number of individuals in each population should be equal to
 totalPopulationSize
 */
Integer totalPopulationSize;

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

  String populationSizeInertiaString = e.getAttribute("populationSizeInertia");
  if (populationSizeInertiaString != null) {
    if (!populationSizeInertiaString.equalsIgnoreCase("")) {
      try {
        populationSizeInertia = Double.parseDouble(populationSizeInertiaString);
      } catch (Exception ex) {
        throw new RuntimeException("Could not parse populationSizeInertia of " +
                "'" + populationSizeInertiaString + "' as a double");
      }
    } else {
      populationSizeInertia = DEFAULT_TARGET_POPULATION_SIZE_INERTIA;
    }

  } else {
    populationSizeInertia = DEFAULT_TARGET_POPULATION_SIZE_INERTIA;
  }

  try {
    totalPopulationSize = Integer.parseInt(e.getAttribute("totalSize"));
  } catch (NumberFormatException nfe) {
    throw new UnsupportedOperationException(
            "PopulationGroup must have a totalSize=\"n\"");
  }

  NodeList nl = e.getChildNodes();
  for (int i = 0; i < nl.getLength(); i++) {
    Node node = nl.item(i);
    if (node instanceof Element) {
      Element child = (Element) node;
      XMLConfigurable xc = Config.initializeXMLConfigurable(child);

      if (xc instanceof Population) {
        populations.add((Population) xc);
      } else if (xc instanceof PopulationBalancer) {
        balancer = (PopulationBalancer) xc;
      } else {
        throw new UnsupportedOperationException(
                "Unrecognized element in Population");
      }
    }
  }

  targetPopulationSizes = new ArrayList<>();
  for (Population pop : populations) {
    targetPopulationSizes.add(new Double(pop.initialSize));
  }

}

@Override
public void writeXMLConfig(Element e) {
  Document d = e.getOwnerDocument();
  e.setAttribute("id", id);
  e.setAttribute("totalSize", totalPopulationSize.toString());
  e.setAttribute("populationSizeInertia",
          Double.toString(populationSizeInertia));

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
 Manually add a population to this population group.

 @param population  */
public void addPopulation(Population population) {
  this.populations.add(population);
  this.targetPopulationSizes.add(new Double(population.initialSize));
}

/**
 Balanced the populations based on the PopulationBalancer, if any.
 */
private void balance() {

  // Calculate restriction of population sizes
  int[] baseIndividuals = new int[populations.size()];
  int totalBaseIndividuals = 0;
  for (int i = 0; i < populations.size(); i++) {
    baseIndividuals[i] = populations.get(i).baseSize;
    totalBaseIndividuals += baseIndividuals[i];
  }
  int totalVariableIndividuals = totalPopulationSize - totalBaseIndividuals;

  // Determine the number of individual in each population
  double[] intraPopSelection = balancer.balance(populations);

  // Sum all values and scale to 1.
  double total = 0d;
  for (int i = 0; i < intraPopSelection.length; i++) {
    total += intraPopSelection[i];
  }
  for (int i = 0; i < intraPopSelection.length; i++) {
    intraPopSelection[i] = intraPopSelection[i] / total;
  }

  // Scale by total number of target individuals in the PopulationGroup.
  for (int i = 0; i < intraPopSelection.length; i++) {
    intraPopSelection[i] = intraPopSelection[i] * totalVariableIndividuals;
    intraPopSelection[i] += baseIndividuals[i];
  }

  // Weigh the existing population sizes and update.
  for (int i = 0; i < intraPopSelection.length; i++) {
    double inertialTotal = populationSizeInertia * targetPopulationSizes.get(i);
    double currentTotal = (1d - populationSizeInertia) * intraPopSelection[i];
    targetPopulationSizes.set(i,
            inertialTotal +
                    currentTotal);
  }
}

/**
 Reproduces each of the Populations contained in this PopulationGroup.

 @param env The environment ( */
public void reproduce(Environment env) {
  if (balancer != null)
    balance();

  for (int i = 0; i < populations.size(); i++) {
    populations.get(i).reproduce(env, targetPopulationSizes.get(i).intValue());
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
