package agency;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Supplier;
import java.util.stream.Stream;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class PopulationGroup implements Serializable, XMLConfigurable {

  private static final long serialVersionUID = -4325119773362195331L;

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
      throw new UnsupportedOperationException("PopulationGroup must have a totalSize=<Integer>");
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
          throw new UnsupportedOperationException("Unrecognized element in Population");
        }

      }
    }

  }

  public Stream<Agent<? extends Individual>> shuffledAgentStream() {
    return Stream.generate(new ShuffledAgentStreamHelper());
  }

  /**
   * Manually add a population to this population group.
   *
   * @param population
   */
  void addPopulationGroup(Population population) {
    this.populations.add(population);
  }

  class ShuffledAgentStreamHelper implements Supplier<Agent<? extends Individual>> {
    ArrayList<IndividualPopulationPair> inds = new ArrayList<>();
    Iterator<IndividualPopulationPair> it;

    ShuffledAgentStreamHelper() {
      for (Population pop : populations) {
        for (Individual ind : pop.individuals) {
          IndividualPopulationPair ipp = new IndividualPopulationPair();
          ipp.ind = ind;
          ipp.p = pop;
          inds.add(ipp);
        }
      }
      Collections.shuffle(inds, ThreadLocalRandom.current());
      it = inds.iterator();
    }

    @Override
    public Agent<? extends Individual> get() {
      if (it.hasNext())
        return it.next().newAgent();
      else {
        Collections.shuffle(inds, ThreadLocalRandom.current());
        it = inds.iterator();
        return it.next().newAgent();
      }

    }
  }

  public static class IndividualPopulationPair {
    Individual ind;
    Population p;

    public Agent<? extends Individual> newAgent() {
      return p.createAgent(ind);
    }
  }

  public void aggregateFitnesses() {
    for (Population pop : populations) {
      pop.aggregateFitnesses(); // Parallelization inside here
    }
  }

  public void reproduce() {
    for (Population pop : populations) {
      pop.reproduce();
    }
  }

  public List<Population> getPopulations() {
    return populations;
  }

  @Override
  public void writeXMLConfig(Document d, Element e) {
    e.setAttribute("id", id);
    e.setAttribute("totalSize", totalPopulationSize.toString());
    for (Population pop : populations) {
      Element subPop = Config.createUnnamedElement(d, pop);
      e.appendChild(subPop);
    }

  }

}
