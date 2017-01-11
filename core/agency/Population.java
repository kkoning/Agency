package agency;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import agency.data.PopulationData;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import agency.reproduce.BreedingPipeline;
import agency.util.RandomStream;

public class Population implements Serializable, XMLConfigurable {

private static final long serialVersionUID = 4940155716065322170L;
public List<Individual> individuals;

String  id;
Integer initialSize;
IndividualFactory<Individual> indFactory;
AgentFactory                  agentFactory;
BreedingPipeline              breedingPipeline;
List<PopulationData>          populationDataOutputs;

Integer evolveCycle;
int     evolveCycleStart, evolveCycleStop;

public Population(IndividualFactory<? extends Individual> indFactory,
                  AgentFactory agentFactory, BreedingPipeline bp, int initialSize) {
  this();
  this.indFactory = (IndividualFactory<Individual>) indFactory;
  this.agentFactory = agentFactory;
  this.breedingPipeline = bp;
  this.initialSize = initialSize;


  initializePopulation(initialSize);
}

public Population() {
  this.id = UUID.randomUUID().toString();
  populationDataOutputs = new ArrayList<>();
}

void initializePopulation(int size) {
  individuals = new ArrayList<>();
  // fitness = new IdentityHashMap<>();
  IntStream.range(0, size).forEach(i -> {
    Individual ind = indFactory.create();
    individuals.add(ind);
  });
}

@Override
public void readXMLConfig(Element e) {

  String idString = e.getAttribute("id");
  if (idString != null)
    if (!idString.equalsIgnoreCase(""))
      id = idString;

  String evolveCycleString = e.getAttribute("evolveCycle");
  if (evolveCycleString != null) {
    if (!evolveCycleString.equalsIgnoreCase("")) {
      // TODO: Better error messages
      this.evolveCycle = Integer.parseInt(evolveCycleString);
      String evolveCycleStartString = e.getAttribute("evolveCycleStart");
      String evolveCycleStopString = e.getAttribute("evolveCycleStop");
      this.evolveCycleStart = Integer.parseInt(evolveCycleStartString);
      this.evolveCycleStop = Integer.parseInt(evolveCycleStopString);
    }
  }


  NodeList nl = e.getChildNodes();
  for (int i = 0; i < nl.getLength(); i++) {
    Node node = nl.item(i);
    if (node instanceof Element) {
      Element child = (Element) node;
      XMLConfigurable xc = Config.initializeXMLConfigurable(child);

      if (xc instanceof IndividualFactory) {
        if (indFactory != null) // Can only have one IndividualFactory
          throw new UnsupportedOperationException(
                  "Population cannot have more than one IndividualFactory");
        indFactory = (IndividualFactory<Individual>) xc;
      } else if (xc instanceof AgentFactory) {
        if (agentFactory != null) // Can only have one AgentFactory
          throw new UnsupportedOperationException(
                  "Population cannot have more than one AgentFactory");
        agentFactory = (AgentFactory) xc;
      } else if (xc instanceof BreedingPipeline) {
        if (breedingPipeline != null) // Can only have one IndividualFactory
          throw new UnsupportedOperationException(
                  "Population cannot have more than one BreedingPipeline");
        breedingPipeline = (BreedingPipeline) xc;
      } else if (xc instanceof PopulationData) {
        PopulationData pd = (PopulationData) xc;
        populationDataOutputs.add(pd);
      } else {
        throw new UnsupportedOperationException("Unrecognized element in Population");
      }

    }
  }

  if (indFactory == null)
    throw new UnsupportedOperationException(
            "Population must have an IndividualFactory to initialize the population");

  if (agentFactory == null)
    throw new UnsupportedOperationException(
            "Population must have an AgentFactory to convert individuals to Agents");

  if (breedingPipeline == null)
    throw new UnsupportedOperationException(
            "Population must have an BreedingPipeline to evolve the population");

  initialSize = Integer.parseInt(e.getAttribute("initialSize"));
  initializePopulation(initialSize);
}

@Override
public void writeXMLConfig(Element e) {
  Document d = e.getOwnerDocument();
  e.setAttribute("initialSize", initialSize.toString());
  Element indFactoryE = Config.createUnnamedElement(d, indFactory);
  Element agentFactoryE = Config.createUnnamedElement(d, agentFactory);
  Element breedPipeE = Config.createUnnamedElement(d, breedingPipeline);
  e.appendChild(indFactoryE);
  e.appendChild(agentFactoryE);
  e.appendChild(breedPipeE);

  for (PopulationData popData : populationDataOutputs) {
    Element pde = Config.createUnnamedElement(d, popData);
    e.appendChild(pde);
  }

}

@Override
public void resumeFromCheckpoint() {
  indFactory.resumeFromCheckpoint();
  agentFactory.resumeFromCheckpoint();
  breedingPipeline.resumeFromCheckpoint();
  populationDataOutputs.forEach(XMLConfigurable::resumeFromCheckpoint);
}

public List<PopulationData> getPopulationDataOutputs() {
  return populationDataOutputs;
}

public void reproduce(Environment env) {
  reproduce(env, this.size());
}

public void reproduce(Environment env, int populationSize) {
  if (evolveCycle != null) {
    int genInCycle = env.generation % evolveCycle;
    if (genInCycle <= evolveCycleStart)
      return; // without a selection/breeding phase
    if (genInCycle > evolveCycleStop)
      return; // without a selection/breeding phase
  }
  definatelyReproduce(env, populationSize);
}

public void definatelyReproduce(Environment env, int populationSize) {

  breedingPipeline.setSourcePopulation(this);
  List<Individual> newPopulation = IntStream.range(0, populationSize)
          .mapToObj((i) -> {
            Individual ind = breedingPipeline.generate();
            return ind;
          }).collect(Collectors.toList());

  individuals = newPopulation;

}

public int size() {
  return individuals.size();
}

public Stream<Individual> allIndividuals() {
  return individuals.stream();
}

public Stream<Individual> randomIndividuals() {
  RandomStream<Individual> rs = new RandomStream<>();
  return rs.randomStream(individuals);

}

public Stream<Agent<? extends Individual>> shuffledAgents() {
  return shuffledIndividuals().map(i -> createAgent(i));
}

protected Agent<? extends Individual> createAgent(Individual ind) {
  return agentFactory.createAgent(ind);
}

Stream<Individual> shuffledIndividuals() {
  RandomStream<Individual> rs = new RandomStream<>();
  return rs.shuffledStream(individuals);
}

@Override
public String toString() {
  StringBuffer sb = new StringBuffer();
  sb.append("Population#" + this.hashCode() + "{\n indFactory=" + indFactory
          + ",\n agentFactory=" + agentFactory + ",\n individuals=[");
  if (individuals != null)
    for (Individual ind : individuals) {
      String fitnessDescription;
      Fitness fit = ind.getFitness();
      if (fit != null)
        fitnessDescription = fit.toString();
      else
        fitnessDescription = "No Fitness";
      sb.append("\n" + ind + "->" + fitnessDescription + ",");
    }
  sb.deleteCharAt(sb.length() - 1);
  sb.append("]\n}");
  return sb.toString();
}

public String getId() {
  return id;
}

public AgentFactory getAgentFactory() {
  return agentFactory;
}

}
