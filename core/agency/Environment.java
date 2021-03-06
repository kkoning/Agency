package agency;

import java.io.*;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.FutureTask;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import agency.data.*;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import agency.eval.EvaluationGroup;
import agency.eval.EvaluationGroupFactory;
import agency.eval.Evaluator;

public class Environment
        implements XMLConfigurable, Serializable {

public static final long serialVersionUID = 1L;

String                id;
List<PopulationGroup> populationGroups;
public EvaluationGroupFactory evaluationGroupFactory;
AgentModelFactory<?> agentModelFactory;

List<ModelSummaryData>      modelSummaryDataOutputs;
List<ModelPerStepData>      modelPerStepDataOutputs;
List<EnvironmentStatistics> stats;

transient ZipOutputStream checkpointsArchive;
Integer checkpointEvery;

public Evaluator evaluator;

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

  String checkpointEveryString = e.getAttribute("checkpointEvery");
  if (checkpointEveryString != null) {
    if (!checkpointEveryString.isEmpty()) {
      checkpointEvery = Integer.parseInt(checkpointEveryString);
      openCheckpointArchive();
    }
  }

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
          throw new UnsupportedOperationException(
                  "Population can only have one EvaluationGroupFactory");
        evaluationGroupFactory = (EvaluationGroupFactory) xc;
      } else if (xc instanceof Evaluator) {
        if (evaluator != null)
          throw new UnsupportedOperationException(
                  "Population can only have one Evaluator");
        evaluator = (Evaluator) xc;
      } else if (xc instanceof AgentModelFactory) {
        if (agentModelFactory != null)
          throw new UnsupportedOperationException(
                  "Population can only have one AgentModelFactory");
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
    throw new UnsupportedOperationException(
            "Environment must have at least one Population Group");

  if (evaluationGroupFactory == null)
    throw new UnsupportedOperationException(
            "Environment must have an EvaluationGroupFactory");

  if (evaluator == null)
    throw new UnsupportedOperationException("Environment must have an Evaluator");

  if (agentModelFactory == null)
    throw new UnsupportedOperationException(
            "Environment must have an AgentModelFactory");

}


private void openCheckpointArchive() {
  try {
    File f = new File("checkpoints-fromGen" + generation + ".zip");
    FileOutputStream fos = new FileOutputStream(f);
    checkpointsArchive = new ZipOutputStream(fos);
  } catch (IOException e) {
    e.printStackTrace();
  }
}





@Override
public void writeXMLConfig(Element e) {
  Document d = e.getOwnerDocument();
  for (PopulationGroup popGroup : populationGroups) {
    Element popGroupE =
            Config.createNamedElement(d, popGroup, "PopulationGroup");
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
  for (PopulationGroup pg : populationGroups) {
    pg.resumeFromCheckpoint();
  }
  evaluationGroupFactory.resumeFromCheckpoint();
  agentModelFactory.resumeFromCheckpoint();
  modelSummaryDataOutputs.forEach(XMLConfigurable::resumeFromCheckpoint);
  modelPerStepDataOutputs.forEach(XMLConfigurable::resumeFromCheckpoint);
  stats.forEach(XMLConfigurable::resumeFromCheckpoint);
  evaluator.resumeFromCheckpoint();
}

public static void fitnessAccumulator(
        Map<Individual, Fitness> collecting,
        Map<Individual, Fitness> collected) {
  for (Map.Entry<Individual, Fitness> newEntry : collected.entrySet()) {
    Fitness existing = collecting.get(newEntry.getKey());
    if (existing == null) {
      collecting.put(newEntry.getKey(), newEntry.getValue());
    } else {
      existing.combine(newEntry.getValue());
    }
  }
}

public static Map<Individual, Fitness> newFitnessMap() {
  return new IdentityHashMap<>();
}

// TODO: Shouldn't this be called somewhere?
private void closeCheckpointArchive() {
  if (checkpointsArchive == null)
    return;
  try {
    checkpointsArchive.flush();
    checkpointsArchive.close();
  } catch (IOException e) {
    e.printStackTrace();
  }
}

public void evolve() {

  Stream<EvaluationGroup> egs =
          evaluationGroupFactory.createEvaluationGroups(this);
  List<EvaluationGroup> evaluatedGroups =
          evaluator.evaluate(egs).collect(Collectors.toList());

  // Aggregate fitnesses
  Map<Individual, Fitness> aggFitnesses = evaluatedGroups.parallelStream()
                                                         .map(eg -> eg.getResults())
                                                         .collect(
                                                                 Environment::newFitnessMap,
                                                                 Environment::fitnessAccumulator,
                                                                 Environment::fitnessAccumulator
                                                         );

  // Assign fitness
  aggFitnesses.entrySet().parallelStream().forEach(
          entry -> entry.getKey().setFitness(entry.getValue()));

  /*
   * Do data output tasks, in parallel.  There should (ideally) be a 1:1 mapping between
   * output files and FutureTask objects, so that there are no race conditions in the
   * data output.  Also, these should all be read-only operations; further processing
   * that modifies the population state (e.g., reproduction) must take place only after
   * all of these tasks have finished.
   */
  ExecutorService executorService = ForkJoinPool.commonPool();
  List<FutureTask> outputTasks = new ArrayList<>();
  for (ModelSummaryData reporter : modelSummaryDataOutputs) {
    ModelSummaryDataHelper msdh = new ModelSummaryDataHelper(getGeneration(),
                                                             reporter,
                                                             evaluatedGroups);
    FutureTask<Boolean> task = new FutureTask<>(msdh, true);
    outputTasks.add(task);
    executorService.execute(task);
  }
  for (ModelPerStepData reporter : modelPerStepDataOutputs) {
    ModelStepDataHelper msdh =
            new ModelStepDataHelper(generation, reporter, evaluatedGroups);
    FutureTask<Boolean> task = new FutureTask<>(msdh, true);
    outputTasks.add(task);
    executorService.execute(task);
  }
  for (PopulationGroup pg : populationGroups) {
    for (Population p : pg.getPopulations()) {
      for (PopulationData pdo : p.getPopulationDataOutputs()) {
        PopulationDataOutputHelper pdoh =
                new PopulationDataOutputHelper(generation, pdo, p);
        FutureTask<Boolean> task = new FutureTask<>(pdoh, true);
        outputTasks.add(task);
        executorService.execute(task);
      }
    }
  }
  for (EnvironmentStatistics es : stats) {
    EnvStatsOutputHelper esoh = new EnvStatsOutputHelper(es, this);
    FutureTask<Boolean> task = new FutureTask<>(esoh, true);
    outputTasks.add(task);
    executorService.execute(task);
  }
  // Wait for the data output tasks to finish.
  for (FutureTask<Boolean> task : outputTasks) {
    try {
      task.get();
    } catch (InterruptedException e) {
      e.printStackTrace();
    } catch (ExecutionException e) {
      e.printStackTrace();
    }
  }
  /*
   * Data output should now be complete.  It should be safe to modify the population state
   * for the next generation.
   */
  int foo = 9; // breakpoint


  // TODO: Balance populations inside of populationGroups.

  // Reproduce populations
  populationGroups.parallelStream().forEach(pg -> pg.reproduce(this));
  //  for (PopulationGroup popGroup : populationGroups) {
  //    popGroup.reproduce();
  //  }


  // Save checkpoint, if appropriate
  if (checkpointEvery != null)
    if ((generation % checkpointEvery) == 0) {
      saveCheckpoint(this);
    }

  generation++;
}public static void saveCheckpoint(Environment env) {
  // TODO: Under construction.
  try {
    String testFileName = "checkpoint.gen-" + env.getGeneration() + ".ser";
    File file = new File(testFileName);
    FileOutputStream fos = new FileOutputStream(file);
    ObjectOutputStream oos = new ObjectOutputStream(fos);
    oos.writeObject(env);
  } catch (Exception e) {
    throw new RuntimeException(e);
  }

}

private void saveCheckpointToArchive() {
  // TODO: Better error handling

  try {
    ZipEntry e = new ZipEntry("checkpoint.gen-" + generation + ".ser");
    checkpointsArchive.putNextEntry(e);
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    ObjectOutputStream oos = new ObjectOutputStream(baos);
    oos.writeObject(this);
    oos.flush();
    oos.close();

    checkpointsArchive.write(baos.toByteArray(), 0, baos.size());
    checkpointsArchive.closeEntry();
    checkpointsArchive.flush();
  } catch (IOException e1) {
    e1.printStackTrace();
  }

}public static Environment readCheckpoint(File serializedFile) {
  try {
    FileInputStream fis = new FileInputStream(serializedFile);
    ObjectInputStream ois = new ObjectInputStream(fis);
    Object o = ois.readObject();
    Environment env = (Environment) o;
    env.resumeFromCheckpoint();
    return env;
  } catch (Exception e) {
    throw new RuntimeException("ERROR: Could not load checkpoint from " +
                               serializedFile + ".  Reason: " + e.getMessage());
  }
}

public int getGeneration() {
  return generation;
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
 * @return the PopulationGroup in this environment with the specified id, or
 * null if it does not exist.
 */
public Optional<PopulationGroup> getPopulationGroup(String id) {
  if (id == null)
    return null;
  for (PopulationGroup pGroup : populationGroups) {
    if (id.equals(pGroup.id))
      return Optional.of(pGroup);
  }
  return Optional.empty();
}

public void close() {

  // The checkpoints archive, if active.
  if (checkpointsArchive != null) {
    try {
      checkpointsArchive.flush();
      checkpointsArchive.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  for (PopulationGroup pg : populationGroups) {
    pg.close();
  }

  evaluator.close();
  agentModelFactory.close();
  for (ModelSummaryData msd : modelSummaryDataOutputs) {
    msd.close();
  }
  for (ModelPerStepData mpsd : modelPerStepDataOutputs) {
    mpsd.close();
  }
  for (EnvironmentStatistics es : stats) {
    es.close();
  }

}

void addPopulationGroup(PopulationGroup pg) {
  this.populationGroups.add(pg);
}

private static class EnvStatsOutputHelper
        implements Runnable {
  EnvironmentStatistics stats;
  Environment           env;

  public EnvStatsOutputHelper(EnvironmentStatistics stats, Environment env) {
    this.stats = stats;
    this.env = env;
  }

  @Override
  public void run() {
    stats.calculate(env);
  }
}

private static class ModelSummaryDataHelper
        implements Runnable {
  int                   generation;
  ModelSummaryData      reporter;
  List<EvaluationGroup> evaluatedGroups;

  ModelSummaryDataHelper(
          int generation,
          ModelSummaryData reporter,
          List<EvaluationGroup> evaluatedGroups) {
    this.generation = generation;
    this.reporter = reporter;
    this.evaluatedGroups = evaluatedGroups;
  }

  @Override
  public void run() {
    for (EvaluationGroup eg : evaluatedGroups) {
      Object summaryData = eg.getSummaryData();
      UUID modelUUID = eg.getId();
      reporter.writeSummaryData(generation, modelUUID, summaryData);
    }
  }
}

private static class ModelStepDataHelper
        implements Runnable {
  int                   generation;
  ModelPerStepData      reporter;
  List<EvaluationGroup> evaluatedGroups;

  ModelStepDataHelper(
          int generation,
          ModelPerStepData reporter,
          List<EvaluationGroup> evaluatedGroups) {
    this.generation = generation;
    this.reporter = reporter;
    this.evaluatedGroups = evaluatedGroups;
  }

  @Override
  public void run() {
    for (EvaluationGroup eg : evaluatedGroups) {
      List<EvaluationGroup.PerStepData> perStepData = eg.getPerStepData();
      UUID modelUUID = eg.getId();
      reporter.writePerStepData(generation, modelUUID, perStepData);
    }
  }
}

private static class PopulationDataOutputHelper
        implements Runnable {
  int            generation;
  PopulationData pdo;
  Population     population;

  public PopulationDataOutputHelper(
          int generation,
          PopulationData pdo,
          Population population) {
    this.generation = generation;
    this.pdo = pdo;
    this.population = population;
  }

  @Override
  public void run() {
    pdo.writePopulationData(generation, population);
  }
}

class PopulationMap
        extends TreeMap<Integer, Population> {
  private static final long serialVersionUID = 4508501876898154219L;
  int individualIndex = 0;

  int totalIndividuals() {
    return individualIndex;
  }

  void addPopulations(Collection<Population> pops) {
    for (Population pop : pops) {
      addPopulation(pop);
    }
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
