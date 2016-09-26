package agency;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import agency.eval.FitnessAggregator;
import agency.reproduce.BreedingPipeline;
import agency.util.RandomStream;
import agency.vector.ValueMutator;

public class Population implements Serializable, XMLConfigurable {
  private static final long serialVersionUID = 4940155716065322170L;

  int generation = 0;
  String                        id;
  Integer                       initialSize;
  List<Individual>              individuals;
  IndividualFactory<Individual> indFactory;
  AgentFactory                  agentFactory;
  FitnessAggregator             fitnessAggregator;
  BreedingPipeline              breedingPipeline;

  public Population() {

  }

  public Population(IndividualFactory<? extends Individual> indFactory,
                    AgentFactory agentFactory, BreedingPipeline bp, int initialSize) {
    this.id = UUID.randomUUID().toString();
    this.indFactory = (IndividualFactory<Individual>) indFactory;
    this.agentFactory = agentFactory;
    this.breedingPipeline = bp;
    this.initialSize = initialSize;


    initializePopulation(initialSize);
  }

  @Override
  public void readXMLConfig(Element e) {

    String idString = e.getAttribute("id");
    if (idString != null)
      if (!idString.equalsIgnoreCase(""))
        id = idString;

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
        } else if (xc instanceof FitnessAggregator) {
          if (fitnessAggregator != null) // Can only have one FitnessAggregator
            throw new UnsupportedOperationException(
                    "Population cannot have more than one FitnessAggregator");
          fitnessAggregator = (FitnessAggregator) xc;
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

    if (fitnessAggregator == null)
      throw new UnsupportedOperationException(
              "Population must have an FitnessAggregator to evolve the population");

    initialSize = Integer.parseInt(e.getAttribute("initialSize"));
    initializePopulation(initialSize);
  }

  @Override
  public void writeXMLConfig(Document d, Element e) {
    e.setAttribute("initialSize", initialSize.toString());
    Element indFactoryE = Config.createUnnamedElement(d, indFactory);
    Element agentFactoryE = Config.createUnnamedElement(d, agentFactory);
    Element fitAggE = Config.createUnnamedElement(d, fitnessAggregator);
    Element breedPipeE = Config.createUnnamedElement(d, breedingPipeline);
    e.appendChild(indFactoryE);
    e.appendChild(fitAggE);
    e.appendChild(agentFactoryE);
    e.appendChild(breedPipeE);
  }

  public void aggregateFitnesses() {
    individuals.stream().parallel().forEach(ind -> {
      ind.aggregateFitness(fitnessAggregator);
    });
  }

  public void reproduce() {
    reproduce(this.size());
  }

  public void reproduce(int populationSize) {
    breedingPipeline.setSourcePopulation(this);
    List<Individual> newPopulation = IntStream.range(0, populationSize)
            .mapToObj((i) -> {
              Individual ind = breedingPipeline.generate();
              return ind;
            }).collect(Collectors.toList());

    individuals = newPopulation;

  }

  Agent<? extends Individual> createAgent(Individual ind) {
    return agentFactory.createAgent(ind);
  }

  public int size() {
    return individuals.size();
  }

  public Stream<Agent<? extends Individual>> allAgents() {
    return individuals.stream().map(i -> createAgent(i));
  }

  public Stream<Individual> allIndividuals() {
    return individuals.stream();
  }

  public Stream<Agent<? extends Individual>> randomAgents() {
    return randomIndividuals().map(i -> createAgent(i));
  }

  public Stream<Individual> randomIndividuals() {
    RandomStream<Individual> rs = new RandomStream<>();
    return rs.randomStream(individuals);

  }

  public Stream<Agent<? extends Individual>> shuffledAgents() {
    return shuffledIndividuals().map(i -> createAgent(i));
  }

  Stream<Individual> shuffledIndividuals() {
    RandomStream<Individual> rs = new RandomStream<>();
    return rs.shuffledStream(individuals);
  }

  void initializePopulation(int size) {
    individuals = new ArrayList<>();
    // fitness = new IdentityHashMap<>();
    IntStream.range(0, size).forEach(i -> {
      Individual ind = indFactory.create();
      individuals.add(ind);
    });
  }

  // @Override
  // public String toString() {
  // return "Population [individuals=" + individuals + ", indFactory=" +
  // indFactory + ", agentFactory="
  // + agentFactory + "]";
  // }

  @Override
  public String toString() {
    StringBuffer sb = new StringBuffer();
    sb.append("Population#" + this.hashCode() + "{\n indFactory=" + indFactory
            + ",\n agentFactory=" + agentFactory + ",\n individuals=[");
    if (individuals != null)
      for (Individual ind : individuals) {
        String fitnessDescription;
        Optional<Fitness> fit = ind.getFitness();
        if (fit.isPresent())
          fitnessDescription = fit.get().toString();
        else
          fitnessDescription = "No Fitness";
        sb.append("\n" + ind + "->" + fitnessDescription + ",");
      }
    sb.deleteCharAt(sb.length() - 1);
    sb.append("]\n}");
    return sb.toString();
  }

}
