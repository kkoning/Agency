package agency.reproduce;

import java.util.Iterator;
import java.util.Optional;
import java.util.Random;
import java.util.TreeMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.logging.Logger;

import org.apache.commons.lang3.SerializationUtils;
import org.w3c.dom.Element;
import agency.Fitness;
import agency.Individual;
import agency.Population;
import agency.SimpleFitness;
import agency.XMLConfigurable;

public class FitnessProportionalSelector implements BreedingPipeline, XMLConfigurable {
private static Logger                      log            = Logger.getLogger(FitnessProportionalSelector.class.getCanonicalName());
private static double                      fitCeilingWarn = Double.MAX_VALUE / 2;
private        TreeMap<Double, Individual> individuals    = new TreeMap<>();
private double totalFitness;

public FitnessProportionalSelector() {
}

@Override
public void readXMLConfig(Element e) {
  // No configuration needed
}

@Override
public void writeXMLConfig(Element e) {
  // No configuration needed
}

@Override
public void resumeFromCheckpoint() {

}

@Override
public Individual generate() {
  Random r = ThreadLocalRandom.current();
  double target = r.nextDouble() * totalFitness;
  Individual toReturn = individuals.ceilingEntry(target).getValue();
  return SerializationUtils.clone(toReturn);
}

@Override
public void setSourcePopulation(Population pop) {
  // Clear out old values
  individuals.clear();
  totalFitness = 0d;

  // Put the population into a tree map by cumulative fitness.
  // Order is not important, proportionality comes from size of range
  // that maps to an individual.
  Iterator<Individual> i = pop.allIndividuals().iterator();
  boolean haveWarnedIfEmpty = false;
  while (i.hasNext()) {
    Individual ind = i.next();
    Optional<Fitness> opFit = Optional.of(ind.getFitness());
    SimpleFitness fit = null;
    if (!opFit.isPresent()) {
      if (!haveWarnedIfEmpty) {
        log.warning("Population " + pop + " using fitness proportional selection with null fitness");
        haveWarnedIfEmpty = true;
        continue;
      }
    } else {
      Fitness genericFit = opFit.get();
      if (!(genericFit instanceof SimpleFitness))
        throw new RuntimeException("Cannot use proportional selection without SimpleFitness");

      fit = (SimpleFitness) genericFit;

    }
    // We should be gauranteed to have a SimpleFitness now...
    // TODO, make sure it is positive and non-zero.
    double fitness = fit.getAverageFitness();
    if (fitness <= 0) {
      log.warning("Fitness proportional selection excluded an individual with fitness <=0.\n" +
              "Individual was: " + ind.toString());
      continue;
    }

    totalFitness += fit.getAverageFitness();
    individuals.put(totalFitness, ind);
  }

  if (totalFitness >= fitCeilingWarn)
    log.warning("Fitnesses are sufficiently large that fitness proportional selection may be somewhat less accurate");
}
}
