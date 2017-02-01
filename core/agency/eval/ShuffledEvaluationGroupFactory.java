package agency.eval;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;
import java.util.stream.Stream;

import agency.*;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Creates {@link EvaluationGroup}s by
 * <ol>
 * <li>Shuffling all the individuals in all populations in this environment
 * <li>Grouping into sets of <em>groupSize</em> individuals
 * <li>Iterating through the previous two steps so that each individual in the
 * population has been evaluated at least <em>timesThrough</em> times.
 * </ol>
 *
 * @author kkoning
 */
public class ShuffledEvaluationGroupFactory
        implements XMLConfigurable, EvaluationGroupFactory {
public static final long serialVersionUID = 1L;

Map<String, Integer> numAgentsFrom;
Integer              numGroups;

public ShuffledEvaluationGroupFactory() {
  numAgentsFrom = new IdentityHashMap<>();
}

@Override
public void readXMLConfig(Element e) {

  try {
    numGroups = Integer.parseInt(e.getAttribute("numGroups"));
  } catch (Exception ex) {
    throw new UnsupportedOperationException(
            "ShuffledEvaluationGroupFactory must specify a numGroups=\"<Integer>\"",
            ex);
  }

  NodeList nl = e.getChildNodes();
  for (int i = 0; i < nl.getLength(); i++) {
    Node node = nl.item(i);
    if (node instanceof Element) {
      Element child = (Element) node;

      String elementName = child.getTagName();
      if (elementName == null)
        throw new UnsupportedOperationException(
                "Null element name for tag in a ShuffledEvaluationGroupFactory");
      if (!elementName.equalsIgnoreCase("AgentSource"))
        throw new UnsupportedOperationException(
                "ShuffledEvaluationGroup must have only AgentSource child elements");

      String popGroupName = child.getAttribute("populationGroup");
      if (popGroupName == null)
        throw new UnsupportedOperationException(
                "AgentSource must specify a populationGroup=\"<id>\"");

      try {
        Integer numAgents = Integer.parseInt(child.getAttribute("numAgents"));
        numAgentsFrom.put(popGroupName, numAgents);
      } catch (Exception ex) {
        throw new UnsupportedOperationException(
                "AgentSource must specify a numAgents=\"<Integer>\"",
                ex);
      }

    }
  }

}

@Override
public void writeXMLConfig(Element e) {
  Document d = e.getOwnerDocument();
  e.setAttribute("numGroups", Integer.toString(numGroups));

  for (Map.Entry<String, Integer> entry : numAgentsFrom.entrySet()) {
    Element childE = d.createElement("AgentSource");
    childE.setAttribute("populationGroup", entry.getKey());
    childE.setAttribute("numAgents", entry.getValue().toString());
    e.appendChild(childE);
  }

}

@Override
public void resumeFromCheckpoint() {
  // Nothing is required.
}

@Override
public Stream<EvaluationGroup> createEvaluationGroups(Environment env) {
  ShuffledEvaluationGroupGenerator segg =
          new ShuffledEvaluationGroupGenerator(numAgentsFrom,env);
  return Stream.generate(segg).limit(numGroups);
}

public static class ShuffledEvaluationGroupGenerator
        implements
        Supplier<EvaluationGroup> {

  SourceList[] sourceLists;
  Environment  env;

  public ShuffledEvaluationGroupGenerator(
          Map<String, Integer> sourceNameMap,
          Environment env) {

    sourceLists = new SourceList[sourceNameMap.size()];
    this.env = env;

    int i = 0;
    for (String popGroupID : sourceNameMap.keySet()) {
      Optional<PopulationGroup> popGroupOp = env.getPopulationGroup(popGroupID);
      if (!popGroupOp.isPresent())
        throw new RuntimeException("Population group " + popGroupID + ", " +
                                   "specified in " +
                                   "ShuffledEvaluationGroupFactory, cannot be" +
                                   " found");
      PopulationGroup pg = popGroupOp.get();
      List<IndPopPair> inds = new ArrayList<>();
      for (Population pop : pg.getPopulations()) {
        for (Individual ind : pop.individuals) {
          IndPopPair ipp = new IndPopPair();
          ipp.i = ind;
          ipp.p = pop;
          inds.add(ipp);
        }
      }
      SourceList sl = new SourceList();
      sl.inds = inds;
      sl.qty = sourceNameMap.get(popGroupID);
      sourceLists[i] = sl;
      i++;
    }

    for (SourceList sl : sourceLists) {
      Collections.shuffle(sl.inds, ThreadLocalRandom.current());
    }
  }


  @Override
  public EvaluationGroup get() {
    EvaluationGroup eg = new EvaluationGroup();
    eg.generation = env.getGeneration();
    eg.setModel(env.getAgentModelFactory().createAgentModel());
    for (SourceList sl : sourceLists) {
      for (int i = 0; i < sl.qty; i++) {
        Agent agent = sl.get();
        eg.addAgent(agent);
      }
    }

    return eg;
  }

}

private static class SourceList
        implements Supplier<Agent> {

  Lock lock = new ReentrantLock();
  List<IndPopPair> inds;
  Integer          qty;

  int position = 0;

  @Override
  public Agent get() {
    Agent toReturn;
    try {
      lock.lock();
      checkAndShuffle();
      IndPopPair ipp = inds.get(position);
      toReturn = ipp.p.createAgent(ipp.i);
      position++;
    } finally {
      lock.unlock();
    }

    return toReturn;
  }

  private void checkAndShuffle() {
    if (position >= inds.size()) {
      position = 0;
      Collections.shuffle(inds,ThreadLocalRandom.current());
    }
  }
}

private static class IndPopPair {
  Individual i;
  Population p;
}


}
