package agency;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import agency.reproduce.BreedingPipeline;
import agency.util.RandomStream;

public class Population implements Serializable, XMLConfigurable {
  private static final long serialVersionUID = 4940155716065322170L;

  static {
    Config.registerClassXMLTag(Population.class);
  }

  int                           generation = 0;
  Integer                       initialSize;
  List<Individual>              individuals;
  IndividualFactory<Individual> indFactory;
  AgentFactory                  agentFactory;
  BreedingPipeline              breedingPipeline;

  public Population() {

  }

  public Population(IndividualFactory<? extends Individual> indFactory,
      AgentFactory agentFactory, BreedingPipeline bp, int initialSize) {
    this.indFactory = (IndividualFactory<Individual>) indFactory;
    this.agentFactory = agentFactory;
    this.breedingPipeline = bp;
    this.initialSize = initialSize;

    initializePopulation(initialSize);
  }

  @Override
  public void readXMLConfig(Element e) {

    initialSize = Integer.parseInt(e.getAttribute("initialSize"));
    Element indFactoryE = Config.getChildElementWithTag(e, "IndividualFactory")
        .get();
    Element agentFactoryE = Config.getChildElementWithTag(e, "AgentFactory")
        .get();
    Element breedPipeE = Config.getChildElementWithTag(e, "BreedingPipeline")
        .get();
    indFactory = (IndividualFactory<Individual>) Config
        .initializeXMLConfigurable(indFactoryE);
    agentFactory = (AgentFactory) Config
        .initializeXMLConfigurable(agentFactoryE);
    breedingPipeline = (BreedingPipeline) Config
        .initializeXMLConfigurable(breedPipeE);

    initializePopulation(initialSize);
  }

  @Override
  public void writeXMLConfig(Document d, Element e) {
    e.setAttribute("initialSize", initialSize.toString());
    Element indFactoryE = Config.createNamedElement(d, indFactory,
        "IndividualFactory");
    Element agentFactoryE = Config.createNamedElement(d, agentFactory,
        "AgentFactory");
    Element breedPipeE = Config.createNamedElement(d, breedingPipeline,
        "BreedingPipeline");
    e.appendChild(agentFactoryE);
    e.appendChild(indFactoryE);
    e.appendChild(breedPipeE);
  }

  public void reproduce() {
    reproduce(this.size());
  }

  public void reproduce(int populationSize) {
    breedingPipeline.setSourcePopulation(this);
    List<Individual> newPopulation = IntStream.range(0, populationSize)
        .mapToObj((i) -> {
          Individual ind = breedingPipeline.generate();
          indFactory.limit(ind);
          return ind;
        }).collect(Collectors.toList());

    individuals = newPopulation;

  }

  Agent<? extends Individual> createAgent(Individual ind) {
    return agentFactory.createAgent(ind);
  }

  // public Stream<Individual> buildReproductionStream(Stream<Individual>
  // source) {
  //
  // return source;
  // }

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
