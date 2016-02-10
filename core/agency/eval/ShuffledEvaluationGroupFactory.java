package agency.eval;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import agency.Agent;
import agency.Config;
import agency.Environment;
import agency.Individual;
import agency.Population;
import agency.vector.FlatIntegerInitializer;

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
 *
 */
public class ShuffledEvaluationGroupFactory implements EvaluationGroupFactory {
  static {
    Config.registerClassXMLTag(ShuffledEvaluationGroupFactory.class);
  }

  int groupSize;
  int timesThrough;

  public ShuffledEvaluationGroupFactory() {
  }

  public ShuffledEvaluationGroupFactory(int groupSize, int timesThrough) {
    this.groupSize = groupSize;
    this.timesThrough = timesThrough;
  }

  @Override
  public void readXMLConfig(Element e) {
    groupSize = Integer.parseInt(e.getAttribute("groupSize"));
    timesThrough = Integer.parseInt(e.getAttribute("timesThrough"));
  }

  @Override
  public void writeXMLConfig(Document d, Element e) {
    e.setAttribute("groupSize", Integer.toString(groupSize));
    e.setAttribute("timesThrough", Integer.toString(timesThrough));
  }

  @Override
  public Stream<EvaluationGroup> createEvaluationGroups(Environment env) {

    // First, collect all individuals in the environment into a flat list.
    List<Agent<? extends Individual>> allAgentList;
    Stream<Agent<? extends Individual>> allAgentStream = Stream.empty();
    for (Population pop : env.getPopulations()) {
      allAgentStream = Stream.concat(allAgentStream, pop.allAgents());
    }
    allAgentList = allAgentStream.collect(Collectors.toList());

    // Obviously, we can't create groups that are larger than the entire
    // population...
    if (allAgentList.size() < groupSize)
      throw new RuntimeException(
          "Not enough individuals (" + allAgentList.size()
              + ") to fill one evaluation group (needed " + groupSize + ")");

    // This object generates EvaluationGroups from the list of all agents.
    ShufflingEvaluationGroupGenerator segc = new ShufflingEvaluationGroupGenerator(
        allAgentList, groupSize, env.getAgentModelFactory());

    int numEvaluations = (allAgentList.size() * timesThrough);
    int numGroups = numEvaluations / groupSize;

    // If things don't match exactly, go slightly over rather than slightly
    // under.
    if ((numEvaluations % groupSize) != 0)
      numGroups++;

    Stream<EvaluationGroup> toReturn;
    toReturn = Stream.generate(segc::generate).limit(numGroups);

    return toReturn;
  }

  public int getGroupSize() {
    return groupSize;
  }

  public void setGroupSize(int groupSize) {
    this.groupSize = groupSize;
  }

  public int getTimesThrough() {
    return timesThrough;
  }

  public void setTimesThrough(int timesThrough) {
    this.timesThrough = timesThrough;
  }

}
