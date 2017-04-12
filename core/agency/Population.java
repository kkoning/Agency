package agency;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
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

/**
 One of the core features of Agency is that the number of individuals in any
 given population varies along with the relative fitness of a population as
 compared to the other popoulations in its populationGroup.
 */
public class Population
        implements Serializable, XMLConfigurable {
public static final long serialVersionUID = 1L;

public List<Individual> individuals;

/**
 A unique ID so that the population can be referred to elsewhere, e.g. to
 uniquely identify this population in output statistics.  If not
 specified in the &lt;Population&rt; element, a random UUID is used.
 */
String                        id;

/**
 During the first generation, there is no fitness history on which to
 calculate evolved population sizes as compared with other Populations in the
 same PopulationGroup.  That size is specified here.
 */
Integer                       initialSize;

/**
 While the number of individuals in a Population vary, it may be desirable to
 maintain a minimum number of individuals so that the Population does not go
 extinct.  The variable <i>baseSize</i> tells the inter-Population selection
 system that this population should always have at least this number of
 individuals.
 */
Integer                       baseSize;

/**
 This object is used to create new individuals, not based on any previous
 individuals.  It is used to create the initial set of Individuals in the
 Population at generation 0.
 */
IndividualFactory<Individual> indFactory;

/**
 Individuals cannot be used directly; there must be some mapping of the
 genetic information to behavior within the agent-based model.  This is the
 function of the Agent class.  The AgentFactory produces a new agent based on
 the specified individual.

 Something that's important to note is that, while the lifetime of an Agent
 object is only for a single simulation, and therefore should never be
 present in two agent models simultaneously, the Individual object _is_.  It
 is therefore important to ensure all agent-model specific state be contained
 in the Agent object, and none in the Individual object.
 */
AgentFactory                  agentFactory;

/**
 The BreedingPipeline is modeled after the excellent system in ECJ.  A series
 of selectors, mutators, limiters, etc... is used to evolve the population
 between generations.
 */
BreedingPipeline              breedingPipeline;

/**
 These objects are responsible for writing data about this population to
 output files for analysis by other programs, i.e., a graphing or statistics
 package.
 */
List<PopulationData>          populationDataOutputs;

/**
 This variable is used when the population should not go through evolution
 every generation.  The evolveCycle specifies the number of generations in
 the cycle, while evolveCycleStart and evolveCycleStop, as expected, specify
 the generation _within_ that cycle that this Population evolves.

 For example, one may have three populations evolving on a 30-generation
 cycle.  The first population evolves in generations 0-9, the second
 population in generations 10-19, and the third in generations 20-29.
 */
Integer evolveCycle;
int     evolveCycleStart, evolveCycleStop;

/**
 * Constructor for when called programatically.  The default constructor
 * assumes initialization from readXMLConfig().
 *
 * @param indFactory
 *         A source of individuals to initialize the population.
 * @param agentFactory
 *         A way to create an agent whose behavior is based on an individual.
 * @param bp
 *         Used to generate the next generation of individuals.
 * @param initialSize
 *         The number of individuals initially present in the population.  This
 *         may change over time as this population is more or less fit as
 *         compared with the other populations in this PopulationGroup.
 */
public Population(
        IndividualFactory<? extends Individual> indFactory,
        AgentFactory agentFactory, BreedingPipeline bp, int initialSize) {
  this();
  this.indFactory = (IndividualFactory<Individual>) indFactory;
  this.agentFactory = agentFactory;
  this.breedingPipeline = bp;
  this.initialSize = initialSize;

  initializePopulation(initialSize);
}


/**
 * Default constructor.  Called when initialization is assumed from
 * readXMLConfig().  If you are calling this function not while reading a
 * configuration file, you likely want the other constructor.
 */
public Population() {
  this.id = UUID.randomUUID().toString();
  populationDataOutputs = new ArrayList<>();
}

/**
 * Creates the initial set of individuals from this.indFactory
 *
 * @param size
 */
private void initializePopulation(int size) {
  individuals = new ArrayList<>();
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
        throw new UnsupportedOperationException(
                "Unrecognized element in Population");
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

/**
 * This function is used for creating fitness landscapes of existing
 * populations; the fitness landscape generator needs to be able to write
 * fitness samples, and won't know the details of how to do that itself.
 *
 * @return A list of population data outputs
 */
public List<PopulationData> getPopulationDataOutputs() {
  return populationDataOutputs;
}

/**
 * Generate a new set of individuals from the existing individuals, based on
 * their fitness.
 *
 * @param env
 *         The current environment, passed for reference to occasionally
 *         important variables such as the current generation #.
 */
public void reproduce(Environment env) {
  reproduce(env, this.size());
}

/**
 * Generate a new set of individuals from the existing individuals, based on
 * their fitness.
 *
 * @param env
 *         The current environment, passed for reference to occasionally
 *         important variables such as the current generation #.
 * @param populationSize
 *         The number of individuals that will be present in the next
 *         generation.  The target size may change depending on the fitness of
 *         this population as compared with other populations in a
 *         PopulationGroup.
 */
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

/**
 * Generate a new set of individuals from the existing individuals, based on
 * their fitness.  Unlike the reproduce()
 *
 * @param env
 *         The current environment, passed for reference to occasionally
 *         important variables such as the current generation #.
 * @param populationSize
 *         The number of individuals that will be present in the next
 *         generation.  The target size may change depending on the fitness of
 *         this population as compared with other populations in a
 *         PopulationGroup.
 */
public void definatelyReproduce(Environment env, int populationSize) {

  breedingPipeline.setSourcePopulation(this);
  List<Individual> newPopulation = IntStream.range(0, populationSize)
                                            .mapToObj((i) -> {
                                              Individual ind =
                                                      breedingPipeline.generate();
                                              return ind;
                                            }).collect(Collectors.toList());

  individuals = newPopulation;

}

/**
 * @return the number of individuals currently in this population.
 */
public int size() {
  return individuals.size();
}

/**
 * Creates a new agent based on the specified individual.  Uses the
 * AgentFactory associated with this population (typically as specified in
 * the configuration file).
 *
 * @param ind
 * @return
 */
public Agent createAgent(Individual ind) {
  return agentFactory.createAgent(ind);
}

@Override
public String toString() {
  return "Population{" +
         "id='" + id + '\'' +
         '}';
}

/**
 * Closes the PopulationDataOutputs associated with this Population.
 */
public void close() {
  for (PopulationData pd : populationDataOutputs) {
    pd.close();
  }
}

public String getId() {
  return id;
}


}
