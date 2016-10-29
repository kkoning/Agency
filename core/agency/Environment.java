package agency;

import java.io.Serializable;
import java.util.*;
import java.util.stream.Stream;

import agency.data.*;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import agency.eval.EvaluationGroup;
import agency.eval.EvaluationGroupFactory;
import agency.eval.Evaluator;

public class Environment implements XMLConfigurable, Serializable {

String                 id;
List<PopulationGroup>  populationGroups;
EvaluationGroupFactory evaluationGroupFactory;
AgentModelFactory<?>   agentModelFactory;

List<ModelSummaryData>      modelSummaryDataOutputs;
List<ModelPerStepData>      modelPerStepDataOutputs;
List<EnvironmentStatistics> stats;

transient Evaluator evaluator;

int generation = 0;

public Environment() {
  this.populationGroups = new ArrayList<>();
  this.stats = new ArrayList<>();
  this.modelSummaryDataOutputs = new ArrayList<>();
  this.modelPerStepDataOutputs = new ArrayList<>();
}

@Override
public void readXMLConfig(Element e) {
  // TODO: Read a random seed

  id = e.getAttribute("id");

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

      if (xc instanceof PopulationGroup) {
        populationGroups.add((PopulationGroup) xc);
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
      } else if (xc instanceof ModelPerStepData) {
        modelPerStepDataOutputs.add((ModelPerStepData) xc);
      } else if (xc instanceof ModelSummaryData) {
        modelSummaryDataOutputs.add((ModelSummaryData) xc);
      } else if (xc instanceof EnvironmentStatistics) {
        this.stats.add((EnvironmentStatistics) xc);
      }
    }
  }

  if (populationGroups.size() < 1)
    throw new UnsupportedOperationException("Environment must have at least one Population Group");

  if (evaluationGroupFactory == null)
    throw new UnsupportedOperationException("Environment must have an EvaluationGroupFactory");

  if (evaluator == null)
    throw new UnsupportedOperationException("Environment must have an Evaluator");

  if (agentModelFactory == null)
    throw new UnsupportedOperationException("Environment must have an AgentModelFactory");

}

@Override
public void writeXMLConfig(Element e) {
  Document d = e.getOwnerDocument();
  for (PopulationGroup popGroup : populationGroups) {
    Element popGroupE = Config.createNamedElement(d, popGroup, "PopulationGroup");
    e.appendChild(popGroupE);
  }
  Element egfE = Config.createUnnamedElement(d, evaluationGroupFactory);
  Element amfE = Config.createUnnamedElement(d, agentModelFactory);
  Element evE = Config.createUnnamedElement(d, evaluator);
  e.appendChild(egfE);
  e.appendChild(amfE);
  e.appendChild(evE);

  // Agent Model Data outputs
  // Summary
  for (ModelSummaryData reporter : modelSummaryDataOutputs) {
    Element element = Config.createUnnamedElement(d, reporter);
    e.appendChild(element);
  }
  // and per-step
  for (ModelPerStepData reporter : modelPerStepDataOutputs) {
    Element element = Config.createUnnamedElement(d, reporter);
    e.appendChild(element);
  }


}

@Override
public void resumeFromCheckpoint() {

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
    // summary data
    for (ModelSummaryData reporter : modelSummaryDataOutputs) {
      AgencyData summaryData = eg.getModel().getSummaryData();
      UUID modelUUID = eg.getId();
      reporter.writeSummaryData(generation, modelUUID, summaryData);
    }
    // per-step data
    for (ModelPerStepData reporter : modelPerStepDataOutputs) {
      Map<Integer, AgencyData> perStepData = eg.getPerStepData();
      UUID modelUUID = eg.getId();
      reporter.writePerStepData(generation, modelUUID, perStepData);
    }
  });

  // Aggregate fitnesses
  for (PopulationGroup popGroup : populationGroups) {
    popGroup.aggregateFitnesses(); // Parallelization inside here
  }

  // Do statistics on populations
  populationGroups.parallelStream().forEach(pg -> {
    pg.getPopulations().parallelStream().forEach(p -> {
      for (PopulationData pdo : p.getPopulationDataOutputs()) {
        pdo.writePopulationData(generation, p);
      }
    });
  });

  // TODO: Balance populations

  // Reproduce populations
  for (PopulationGroup popGroup : populationGroups) {
    popGroup.reproduce();
  }

  // Data output
  for (EnvironmentStatistics es : stats) {
    es.calculate(this);
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

public List<PopulationGroup> getPopulationGroups() {
  return populationGroups;
}

/**
 * @param id
 * @return the PopulationGroup in this environment with the specified id, or null if it does not exist.
 */
public PopulationGroup getPopulationGroup(String id) {
  if (id == null)
    return null;
  for (PopulationGroup pGroup : populationGroups) {
    if (id.equals(pGroup.id))
      return pGroup;
  }
  return null;
}

void addPopulationGroup(PopulationGroup pg) {
  this.populationGroups.add(pg);
}

public int getGeneration() {
  return generation;
}


// Stream<Individual> getRandomIndividuals() {
// PopulationMap indexedPop = new PopulationMap();
// Collection<Population> pops =
// getAllPopulations().collect(Collectors.toCollection(ArrayList::new));
// indexedPop.addPopulations(pops);
//
// Random r = ThreadLocalRandom.current();
//
// Stream<Individual> toReturn = Stream.generate(() -> (Integer)
// r.nextInt(indexedPop.totalIndividuals()))
// .map(i -> indexedPop.getIndividual(i));
//
// return toReturn;
// }

// Stream<Population> getAllPopulations() {
// Stream<Population> toReturn = Stream.empty();
// toReturn = Stream.concat(toReturn, populations.stream());
// if (subEnvironments != null)
// for (Environment e : subEnvironments)
// toReturn = Stream.concat(toReturn, e.getAllPopulations());
// return toReturn;
// }

/**
 * @return All the individuals present in this environment, regardless of population.
 */
// Stream<Individual> allIndividuals() {
// Stream<Individual> toReturn = Stream.empty();
// for (Population p : populations)
// toReturn = Stream.concat(toReturn, p.individuals.stream());
// if (subEnvironments != null)
// for (Environment e : subEnvironments)
// toReturn = Stream.concat(toReturn, e.allIndividuals());
// return toReturn;
// }

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
  int individualIndex = 0;

  int totalIndividuals() {
    return individualIndex;
  }

  void addPopulations(Collection<Population> pops) {
    for (Population pop : pops)
      addPopulation(pop);
  }

  void addPopulation(Population pop) {
    put(individualIndex, pop);
    individualIndex += pop.size();
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
