package agency;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.TreeMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import agency.eval.EvaluationGroupFactory;
import agency.eval.EvaluationManager;
import agency.eval.Evaluator;
import agency.reproduce.VectorMutationPipeline;

public class Environment implements XMLConfigurable, Serializable {
  private static final long serialVersionUID = 3057043882181403186L;

  static {
    Config.registerClassXMLTag(Environment.class);
  }

  ArrayList<Population>       populations;
  EvaluationGroupFactory      evaluationGroupFactory;
  AgentModelFactory<?>        agentModelFactory;
  ArrayList<Environment>      subEnvironments;
  Environment                 superEnvironment;
  int                         generation = 0;
  transient Evaluator         evaluator;
  transient EvaluationManager evaluationManager;

  public Environment() {
    this.populations = new ArrayList<>();
    evaluationManager = new EvaluationManager();
  }

  public Environment(Environment parent) {
    this();
    this.superEnvironment = parent;
  }

  @Override
  public void readXMLConfig(Element e) {
    List<Element> popEs = Config.getChildElementsWithTag(e, "Population");
    if (popEs.isEmpty())
      throw new IllegalArgumentException(
          "Environment must have at least one population");
    for (Element popE : popEs) {
      Population p = (Population) Config.initializeXMLConfigurable(popE);
      populations.add(p);
    }

    Optional<Element> opEGFE = Config.getChildElementWithTag(e,
        "EvaluationGroupFactory");
    if (!opEGFE.isPresent())
      throw new IllegalArgumentException(
          "Environment must have an evaluation group factory");
    evaluationGroupFactory = (EvaluationGroupFactory) Config
        .initializeXMLConfigurable(opEGFE.get());

    Optional<Element> opAMFE = Config.getChildElementWithTag(e,
        "AgentModelFactory");
    if (!opAMFE.isPresent())
      throw new IllegalArgumentException(
          "Environment must have an AgentModelFactory");
    agentModelFactory = (AgentModelFactory<?>) Config
        .initializeXMLConfigurable(opAMFE.get());

  }

  @Override
  public void writeXMLConfig(Document d, Element e) {
    for (Population pop : populations) {
      Element popE = Config.createNamedElement(d, pop, "Population");
      e.appendChild(popE);
    }
    Element egfE = Config.createNamedElement(d, evaluationGroupFactory,
        "EvaluationGroupFactory");
    Element amfE = Config.createNamedElement(d, agentModelFactory,
        "AgentModelFactory");
    e.appendChild(egfE);
    e.appendChild(amfE);
  }

  public void evolve() {
    evaluationManager.evaluate(this);
    // TODO: Balance populations
    for (Population pop : populations) {
      pop.reproduce();
    }
    generation++;
  }

  public EvaluationGroupFactory getEvaluationGroupFactory() {
    return evaluationGroupFactory;
  }

  public void setEvaluationGroupFactory(
      EvaluationGroupFactory evaluationGroupFactory) {
    this.evaluationGroupFactory = evaluationGroupFactory;
  }

  public AgentModelFactory<?> getAgentModelFactory() {
    return agentModelFactory;
  }

  public void setAgentModelFactory(AgentModelFactory<?> agentModelFactory) {
    this.agentModelFactory = agentModelFactory;
  }

  public void addPopulation(Population pop) {
    populations.add(pop);
  }

  public void addEnvironment(Environment env) {
    if (subEnvironments == null)
      subEnvironments = new ArrayList<Environment>();
    subEnvironments.add(env);
  }

  public ArrayList<Population> getPopulations() {
    return populations;
  }

  Stream<Individual> getRandomIndividuals() {
    PopulationMap indexedPop = new PopulationMap();
    Collection<Population> pops = getAllPopulations()
        .collect(Collectors.toCollection(ArrayList::new));
    indexedPop.addPopulations(pops);

    Random r = ThreadLocalRandom.current();

    Stream<Individual> toReturn = Stream
        .generate(() -> (Integer) r.nextInt(indexedPop.totalIndividuals()))
        .map(i -> indexedPop.getIndividual(i));

    return toReturn;
  }

  Stream<Population> getAllPopulations() {
    Stream<Population> toReturn = Stream.empty();
    toReturn = Stream.concat(toReturn, populations.stream());
    if (subEnvironments != null)
      for (Environment e : subEnvironments)
        toReturn = Stream.concat(toReturn, e.getAllPopulations());
    return toReturn;
  }

  /**
   * @return All the individuals present in this environment, regardless of
   *         population.
   */
  Stream<Individual> allIndividuals() {
    Stream<Individual> toReturn = Stream.empty();
    for (Population p : populations)
      toReturn = Stream.concat(toReturn, p.individuals.stream());
    if (subEnvironments != null)
      for (Environment e : subEnvironments)
        toReturn = Stream.concat(toReturn, e.allIndividuals());
    return toReturn;
  }

  // public Stream<Entry<Individual, Fitness>> allIndividualsWithFitness() {
  // Stream<Entry<Individual, Fitness>> toReturn = Stream.empty();
  // for (Population pop : populations)
  // toReturn = Stream.concat(toReturn, pop.allIndividualsWithFitness());
  // if (subEnvironments.isPresent())
  // for (Environment env : subEnvironments.get())
  // toReturn = Stream.concat(toReturn, env.allIndividualsWithFitness());
  // return toReturn;
  // }

  class PopulationMap extends TreeMap<Integer, Population> {
    private static final long serialVersionUID = 4508501876898154219L;
    int                       individualIndex  = 0;

    void addPopulation(Population pop) {
      put(individualIndex, pop);
      individualIndex += pop.size();
    }

    int totalIndividuals() {
      return individualIndex;
    }

    void addPopulations(Collection<Population> pops) {
      for (Population pop : pops)
        addPopulation(pop);
    }

    Individual getIndividual(Integer position) {
      Integer popStartsAt = lowerKey(position + 1);
      Population pop = get(popStartsAt);
      Integer popIndex = position - popStartsAt;
      Individual toReturn = pop.individuals.get(popIndex);
      return toReturn;
    }

  }

}
