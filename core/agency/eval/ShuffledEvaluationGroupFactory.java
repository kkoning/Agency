package agency.eval;

import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Stream;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import agency.Agent;
import agency.Environment;
import agency.Individual;
import agency.PopulationGroup;
import agency.XMLConfigurable;

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
public class ShuffledEvaluationGroupFactory implements XMLConfigurable, EvaluationGroupFactory {

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
    throw new UnsupportedOperationException("ShuffledEvaluationGroupFactory must specify a numGroups=\"<Integer>\"", ex);
  }

  NodeList nl = e.getChildNodes();
  for (int i = 0; i < nl.getLength(); i++) {
    Node node = nl.item(i);
    if (node instanceof Element) {
      Element child = (Element) node;

      String elementName = child.getTagName();
      if (elementName == null)
        throw new UnsupportedOperationException("Null element name for tag in a ShuffledEvaluationGroupFactory");
      if (!elementName.equalsIgnoreCase("AgentSource"))
        throw new UnsupportedOperationException("ShuffledEvaluationGroup must have only AgentSource child elements");

      String popGroupName = child.getAttribute("populationGroup");
      if (popGroupName == null)
        throw new UnsupportedOperationException("AgentSource must specify a populationGroup=\"<id>\"");

      try {
        Integer numAgents = Integer.parseInt(child.getAttribute("numAgents"));
        numAgentsFrom.put(popGroupName, numAgents);
      } catch (Exception ex) {
        throw new UnsupportedOperationException("AgentSource must specify a numAgents=\"<Integer>\"", ex);
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

}

@Override
public Stream<EvaluationGroup> createEvaluationGroups(Environment env) {

  ShuffledEvaluationGroupHelper segh = new ShuffledEvaluationGroupHelper(env);

  for (Map.Entry<String, Integer> entry : numAgentsFrom.entrySet()) {
    PopulationGroup pg = env.getPopulationGroup(entry.getKey());
    if (pg == null)
      throw new UnsupportedOperationException("ShuffledEvaluationGroupFactory cannot find PopulationGroup with id=" + entry.getKey());

    Iterator<Agent<? extends Individual>> popIterator = pg.shuffledAgentStream().iterator();
    segh.addPopulationGroup(popIterator, entry.getValue());
  }

  Stream<EvaluationGroup> toReturn;
  toReturn = Stream.generate(segh);
  return toReturn.limit(numGroups);


}

static class ShuffledEvaluationGroupHelper implements Supplier<EvaluationGroup> {

  Map<Iterator<Agent<? extends Individual>>, Integer> na;
  Environment                                         env;

  ShuffledEvaluationGroupHelper(Environment env) {
    na = new IdentityHashMap<>();
    this.env = env;
  }

  void addPopulationGroup(Iterator<Agent<? extends Individual>> agentSource, Integer numAgents) {
    na.put(agentSource, numAgents);
  }

  @Override
  synchronized public EvaluationGroup get() {
    EvaluationGroup eg = new EvaluationGroup();
    eg.setModel(env.getAgentModelFactory().createAgentModel());
    for (Map.Entry<Iterator<Agent<? extends Individual>>, Integer> entry : na.entrySet()) {
      for (int i = 0; i < entry.getValue(); i++) {
        Agent<? extends Individual> agent = entry.getKey().next();
        eg.addAgent(agent);
      }

    }
    return eg;
  }

}

}
