package agency;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.TreeMap;
import java.util.Map.Entry;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import agency.eval.EvaluationGroup;
import agency.eval.EvaluationGroupFactory;
import agency.eval.EvaluationManager;
import agency.eval.Evaluator;
import agency.reproduce.BreedingPipeline;
import agency.vector.ValueLimiter;
import agency.vector.ValueMutator;
import agency.vector.VectorMutationPipeline;

public class Environment implements XMLConfigurable, Serializable {
  private static final long serialVersionUID = 3057043882181403186L;

  int                       generation       = 0;

  Environment               superEnvironment;
  ArrayList<Population>     populations;
  ArrayList<Environment>    subEnvironments;

  EvaluationGroupFactory    evaluationGroupFactory;
  AgentModelFactory<?>      agentModelFactory;

  AgentModelReporter        agentModelReporter;

  transient Evaluator       evaluator;

  public Environment() {
    this.populations = new ArrayList<>();
  }

  public Environment(Environment parent) {
    this();
    this.superEnvironment = parent;
  }

  @Override
  public void readXMLConfig(Element e) {
    // TODO: Read a random seed

    // Things to read:
    // - Populations (check)
    // - EvaluationGroupFactory (check)
    // - Evaluator (check)
    // - AgentModelFactory

    NodeList nl = e.getChildNodes();
    for (int i = 0; i < nl.getLength(); i++) {
      Node node = nl.item(i);
      if (node instanceof Element) {
        Element child = (Element) node;
        XMLConfigurable xc = Config.initializeXMLConfigurable(child);

        if (xc instanceof Population) {
          populations.add((Population) xc);
        } else if (xc instanceof EvaluationGroupFactory) {
          if (evaluationGroupFactory != null)
            throw new UnsupportedOperationException("Population can only have one EvaluationGroupFactory");
          evaluationGroupFactory = (EvaluationGroupFactory) xc;
        } else if (xc instanceof Evaluator) {
          if (evaluator != null)
            throw new UnsupportedOperationException("Population can only have one Evaluator");
          evaluator = (Evaluator) xc;
        } else if (xc instanceof AgentModelFactory) {
          if (agentModelFactory != null)
            throw new UnsupportedOperationException("Population can only have one AgentModelFactory");
          agentModelFactory = (AgentModelFactory<?>) xc;
        } else if (xc instanceof AgentModelReporter) {
          if (agentModelReporter != null)
            throw new UnsupportedOperationException("Population can only have one AgentModelFactory");
          agentModelReporter = (AgentModelReporter) xc;
        }
        
        
      }
    }

    if (populations.size() < 1)
      throw new UnsupportedOperationException("Environment must have at least one Population");

    if (evaluationGroupFactory == null)
      throw new UnsupportedOperationException("Environment must have an EvaluationGroupFactory");

    if (evaluator == null)
      throw new UnsupportedOperationException("Environment must have an Evaluator");

    if (agentModelFactory == null)
      throw new UnsupportedOperationException("Environment must have an AgentModelFactory");

  }

  @Override
  public void writeXMLConfig(Document d, Element e) {
    for (Population pop : populations) {
      Element popE = Config.createNamedElement(d, pop, "Population");
      e.appendChild(popE);
    }
    Element egfE = Config.createUnnamedElement(d, evaluationGroupFactory);
    Element amfE = Config.createUnnamedElement(d, agentModelFactory);
    Element evE = Config.createUnnamedElement(d, evaluator);
    e.appendChild(egfE);
    e.appendChild(amfE);
    e.appendChild(evE);
  }

  public void evolve() {

    Stream<EvaluationGroup> egs = evaluationGroupFactory.createEvaluationGroups(this);
    Stream<EvaluationGroup> evaluatedGroups = evaluator.evaluate(egs);

    // EvaluationGroups have now been run; do stuff with their data
    evaluatedGroups.forEach(eg -> {

      // Pull out all the individuals and their fitness samples
      eg.getResults().forEach(fitMapEnt -> {
        Individual i = fitMapEnt.getKey();
        i.addFitnessSample(fitMapEnt.getValue());
      });

      // Collect data from the agent models
      if (agentModelReporter != null) {  // Data from models is optional here...
        agentModelReporter.perStepData(generation, eg);
        agentModelReporter.summaryData(generation, eg);
      }

    });

    // Aggregate fitnesses
    for (Population pop : populations) {
      pop.aggregateFitnesses(); // Parallelization inside here
    }

    // TODO: Balance populations

    // Reproduce populations
    for (Population pop : populations) {
      pop.reproduce();
    }

    generation++;
  }

  public EvaluationGroupFactory getEvaluationGroupFactory() {
    return evaluationGroupFactory;
  }

  public void setEvaluationGroupFactory(EvaluationGroupFactory evaluationGroupFactory) {
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
    Collection<Population> pops = getAllPopulations().collect(Collectors.toCollection(ArrayList::new));
    indexedPop.addPopulations(pops);

    Random r = ThreadLocalRandom.current();

    Stream<Individual> toReturn = Stream.generate(() -> (Integer) r.nextInt(indexedPop.totalIndividuals()))
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
